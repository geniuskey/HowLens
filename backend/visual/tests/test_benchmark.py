"""Deterministic fake HTTP sandbox checks; external Evaluation files are read only."""
import asyncio
import base64
import importlib.util
import json
import os
import tempfile
import unittest
from io import BytesIO
from pathlib import Path
from unittest.mock import patch

import httpx
import jsonschema
from PIL import Image
from visual.benchmark import run_pilot, load_protocol, plan, bytes_digest, validate_options
from visual.benchmark_http import SandboxImageClient, BenchmarkFailure

PROTOCOL = Path(os.environ.get("HOWLENS_BENCHMARK_PROTOCOL_DIR", "backend/visual/.venv/eval-protocol"))


def raw_png():
    with Image.new("RGB", (1024, 1024), "#ABCDEF") as image:
        out = BytesIO()
        image.save(out, format="PNG")
        return out.getvalue()


class BenchmarkTests(unittest.IsolatedAsyncioTestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.output = Path(self.temp.name) / "session"
        self.cases, self.schema = load_protocol(PROTOCOL)

    def client(self, handler):
        return SandboxImageClient(api_key="offline-key", transport=httpx.MockTransport(handler))

    async def run_case(self, handler, **settings):
        return await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output, execute=True,
                               budget_usd=6, paid_authorization="TEST-OFFLINE", client=self.client(handler), **settings)

    async def test_dry_run_zero_calls_no_env_read_and_schema_parity(self):
        def forbidden(request):
            self.fail("Dry run must not call HTTP")
        with patch.dict(os.environ, {}, clear=True), patch("visual.benchmark.os.environ.get", side_effect=AssertionError("env read")):
            rows, artifacts = await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output, client=self.client(forbidden))
        self.assertEqual(artifacts["attempted_calls"], 0)
        self.assertEqual(artifacts["measurement_source"], "not_run")
        self.assertEqual(len(rows), 6)
        for row in rows:
            jsonschema.validate(row, self.schema)
            self.assertEqual(row["status"], "dry_run")
            self.assertIsNone(row["latency_ms"])
            self.assertIsNone(row["actual_cost_usd"])
        # Compare against the actual read-only Evaluation planner, not a mirror test.
        spec = importlib.util.spec_from_file_location("eval_public_protocol", PROTOCOL / "protocol.py")
        protocol = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(protocol)
        expected = {r["run_id"]: r for model in ("gpt-image-1.5", "gpt-image-1-mini")
                    for r in protocol.plan(self.cases, model, rows[0]["settings"])}
        self.assertEqual({r["run_id"]: r for r in rows}, expected)

    async def test_success_exact_settings_raw_hash_and_offline_score(self):
        calls = []
        raw = raw_png()
        def handler(request):
            body = json.loads(request.content)
            calls.append(body)
            self.assertEqual(body["size"], "1024x1024")
            self.assertEqual(body["quality"], "low")
            self.assertEqual(body["n"], 1)
            self.assertNotIn("response_format", body)
            return httpx.Response(200, headers={"x-request-id": "req_test"}, json={"data": [{"b64_json": base64.b64encode(raw).decode()}],
                "usage": {"input_tokens": 50, "output_tokens": 20, "private": "never save"}})
        ticks = iter(range(12))
        rows, artifacts = await self.run_case(handler, clock=lambda: next(ticks))
        self.assertEqual(len(calls), 6)
        self.assertEqual(artifacts["measurement_source"], "offline")
        for i, row in enumerate(rows):
            jsonschema.validate(row, self.schema)
            self.assertEqual(row["status"], "success")
            self.assertEqual(row["latency_ms"], 1000)
            self.assertEqual(row["request_id"], "req_test")
            self.assertIsNone(row["actual_cost_usd"])
            self.assertIsNone(row["billing_evidence"])
            self.assertIsNone(row["model_snapshot"])
            saved = artifacts["runs"][row["run_id"]]
            self.assertEqual(Path(saved["raw_path"]).read_bytes(), raw)
            self.assertEqual(saved["raw_sha256"], bytes_digest(raw))
            self.assertEqual(saved["normalized_sha256"], bytes_digest(Path(row["output_path"]).read_bytes()))
            self.assertEqual(row["usage"], {"input_tokens": 50, "output_tokens": 20})
            self.assertEqual(row["panels"][8]["box"], [682, 682, 1023, 1023])
        self.assertEqual([c["model"] for c in calls], ["gpt-image-1.5", "gpt-image-1-mini"] * 3)
        self.assertEqual(calls[0]["prompt"], calls[1]["prompt"])
        spec = importlib.util.spec_from_file_location("eval_public_protocol", PROTOCOL / "protocol.py")
        protocol = importlib.util.module_from_spec(spec); spec.loader.exec_module(protocol)
        scored = protocol.score(self.cases[0], rows[0], rows[0]["output_path"])
        self.assertEqual(scored["verdict"], "pending_human_review")
        self.assertFalse(scored["product_safety_approval"])
        self.assertEqual(protocol.summarize(rows)["live_groups"], {})

    async def test_failure_timeout_and_invalid_output_no_retry(self):
        calls = []
        def handler(request):
            calls.append(request)
            if len(calls) == 1:
                return httpx.Response(429, headers={"x-request-id": "req_failure"}, text="secret body")
            if len(calls) == 2:
                raise httpx.ReadTimeout("secret timeout")
            return httpx.Response(200, json={"data": [{"b64_json": "!!!"}]})
        rows, artifacts = await self.run_case(handler)
        self.assertEqual(len(calls), 6)
        self.assertEqual([r["status"] for r in rows[:3]], ["api_error", "timeout", "api_error"])
        self.assertEqual(rows[0]["request_id"], "req_failure")
        self.assertTrue(all(r["attempt"] == 1 for r in rows))
        self.assertNotIn("secret", (self.output / "results.jsonl").read_text(encoding="utf-8"))
        self.assertEqual(artifacts["reserved_usd"], 6)

    async def test_call_limit_counts_failed_attempts(self):
        calls = []
        def handler(request):
            calls.append(request)
            return httpx.Response(500)
        rows, artifacts = await self.run_case(handler, max_calls=2)
        self.assertEqual(len(calls), 2)
        self.assertEqual(artifacts["attempted_calls"], 2)
        self.assertTrue(all(r["error_code"] == "call_limit" for r in rows[2:]))
        self.assertTrue(all(r["measurement_source"] == "not_run" for r in rows[2:]))

    async def test_budget_reservation_stop_before_next_call(self):
        calls = []
        def handler(request):
            calls.append(request)
            return httpx.Response(500)
        rows, artifacts = await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output, execute=True,
            budget_usd=1.5, reservation_usd=1, paid_authorization="TEST", client=self.client(handler))
        self.assertEqual(len(calls), 1)
        self.assertEqual(artifacts["reserved_usd"], 1)
        self.assertTrue(all(r["error_code"] == "reservation_budget_limit" for r in rows[1:]))

    async def test_total_timeout_and_cancel_recorded(self):
        async def handler(request):
            await asyncio.sleep(3)
            return httpx.Response(200)
        client = self.client(handler)
        client.timeout_seconds = .01  # Test-only shortened default.
        rows, _ = await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output, execute=True,
            max_calls=1, budget_usd=1, paid_authorization="TEST", client=client)
        self.assertEqual(rows[0]["status"], "timeout")
        async def cancelled(request):
            raise asyncio.CancelledError()
        self.output = Path(self.temp.name) / "cancel"
        rows, artifacts = await self.run_case(cancelled)
        self.assertEqual(artifacts["attempted_calls"], 1)
        self.assertTrue(all(r["status"] == "cancelled" for r in rows))

    async def test_missing_authorization_bad_cap_and_no_overwrite(self):
        for settings in [{"max_calls": 7}, {"max_calls": 0}, {"max_calls": True},
                         {"budget_usd": float("nan")}, {"timeout_seconds": 301},
                         {"execute": True, "budget_usd": 1}]:
            with self.assertRaises(ValueError):
                await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output, **settings)
        await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output)
        with self.assertRaises(FileExistsError):
            await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output)

    async def test_wrong_prompt_protocol_rejected_before_call(self):
        changed = Path(self.temp.name) / "protocol"; changed.mkdir()
        cases = list(self.cases)
        cases[0] = dict(cases[0], prompt="different prompt")
        (changed / "cases.jsonl").write_text("\n".join(json.dumps(c) for c in cases))
        (changed / "run-result.schema.json").write_text(json.dumps(self.schema))
        with self.assertRaises(ValueError):
            await run_pilot(protocol_dir=changed, output_dir=self.output)

    async def test_http_bounds_and_corrupt_png(self):
        from visual import benchmark_http
        for response in [httpx.Response(200, content=b"not json"),
                         httpx.Response(200, json={"data": [{"b64_json": base64.b64encode(b"corrupt").decode()}]})]:
            with self.assertRaises(BenchmarkFailure):
                await self.client(lambda req: response).generate(model="gpt-image-1.5", prompt="synthetic")
        with patch.object(benchmark_http, "MAX_HTTP_BYTES", 10):
            with self.assertRaises(BenchmarkFailure) as caught:
                await self.client(lambda req: httpx.Response(200, content=b"x" * 11)).generate(model="gpt-image-1.5", prompt="synthetic")
            self.assertEqual(caught.exception.code, "response_limit")
        calls = []
        client = self.client(lambda req: calls.append(req))
        for model, prompt in [("arbitrary", "synthetic"), ("gpt-image-1-mini", "x" * 32001)]:
            with self.assertRaises(ValueError):
                await client.generate(model=model, prompt=prompt)
        self.assertEqual(calls, [])

    async def test_subprecision_budget_rejected_before_env_or_call(self):
        calls = []
        client = self.client(lambda request: calls.append(request))
        with patch("visual.benchmark.os.environ.get", side_effect=AssertionError("env read")):
            for budget, reservation in [(1e-10, 1e-10), (1, 1e-10), (1e-10, 1)]:
                with self.assertRaises(ValueError):
                    await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output,
                        execute=True, max_calls=6, budget_usd=budget, reservation_usd=reservation,
                        paid_authorization="TEST", client=client)
        self.assertEqual(calls, [])
        self.assertFalse(self.output.exists())

    async def test_minimum_precision_budget_allows_exactly_one_call(self):
        calls = []
        def handler(request):
            calls.append(request)
            return httpx.Response(500)
        rows, artifacts = await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output,
            execute=True, max_calls=6, budget_usd=1e-8, reservation_usd=1e-8,
            paid_authorization="TEST", client=self.client(handler))
        self.assertEqual(len(calls), 1)
        self.assertEqual(artifacts["reserved_usd"], 1e-8)
        self.assertTrue(all(r["error_code"] == "reservation_budget_limit" for r in rows[1:]))

    async def test_fractional_budget_has_no_float_drift_or_tolerance(self):
        calls = []
        def handler(request):
            calls.append(request)
            return httpx.Response(500)
        rows, artifacts = await run_pilot(protocol_dir=PROTOCOL, output_dir=self.output,
            execute=True, budget_usd=.3, reservation_usd=.1,
            paid_authorization="TEST", client=self.client(handler))
        self.assertEqual(len(calls), 3)
        self.assertEqual(artifacts["reserved_usd"], .3)
        self.assertTrue(all(r["error_code"] == "reservation_budget_limit" for r in rows[3:]))

    async def test_cancellation_during_body_preserves_observed_headers(self):
        calls = []
        class CancelledBody(httpx.AsyncByteStream):
            async def __aiter__(self):
                yield b'{"data":'
                raise asyncio.CancelledError("private cancellation detail")
        def handler(request):
            calls.append(request)
            return httpx.Response(200, headers={"x-request-id": "req_cancelled"}, stream=CancelledBody())
        rows, artifacts = await self.run_case(handler)
        self.assertEqual(len(calls), 1)
        self.assertEqual(artifacts["attempted_calls"], 1)
        self.assertEqual(rows[0]["status"], "cancelled")
        self.assertEqual(rows[0]["http_status"], 200)
        self.assertEqual(rows[0]["request_id"], "req_cancelled")
        self.assertIsNone(rows[0]["usage"])
        self.assertGreaterEqual(rows[0]["latency_ms"], 0)
        self.assertTrue(all(r["request_id"] is None and r["http_status"] is None for r in rows[1:]))
        self.assertNotIn("private cancellation detail", (self.output / "results.jsonl").read_text(encoding="utf-8"))
        for row in rows:
            jsonschema.validate(row, self.schema)
