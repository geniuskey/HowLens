"""Bounded quality presets tested with fake HTTP, never paid image generation."""
import asyncio
import base64
import csv
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import httpx
from visual.benchmark import run_pilot, load_protocol, plan
from visual.benchmark_config import (SUNBURST, FLARE, PRESETS, MODEL_QUALITIES,
                                    comparison_specs, request_parameters, cost_estimate)
from visual.benchmark_http import SandboxImageClient
from test_benchmark import PROTOCOL, raw_png


class QualityTests(unittest.IsolatedAsyncioTestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name) / "session"
        self.cases, _ = load_protocol(PROTOCOL)

    def client(self, handler):
        return SandboxImageClient(api_key="sk-offline-secret", transport=httpx.MockTransport(handler))

    async def run_fake(self, handler, **options):
        return await run_pilot(protocol_dir=PROTOCOL, output_dir=self.root,
                               execute=True, budget_usd=6, paid_authorization="TEST-OFFLINE",
                               client=self.client(handler), preset="paired-sunburst", **options)

    async def test_every_preset_bounded_and_pairs_use_same_prompt(self):
        for preset in PRESETS:
            rows = plan(self.cases, preset)
            self.assertEqual(len(rows), 6)
            self.assertTrue(all(r["settings"]["concurrency"] == 1 and r["settings"]["retries"] == 0 for r in rows))
            for row in rows:
                request_parameters(row["model"], row["settings"]["quality"])
        for preset, model in (("paired-sunburst", SUNBURST), ("paired-flare", FLARE),
                              ("paired-mini", "gpt-image-1-mini")):
            rows = plan(self.cases, preset)
            for i in (0, 2, 4):
                self.assertEqual(rows[i]["model"], model)
                self.assertEqual(rows[i]["prompt_sha256"], rows[i+1]["prompt_sha256"])
                self.assertEqual(rows[i]["reference_assets"], rows[i+1]["reference_assets"])
                self.assertEqual([rows[i]["settings"]["quality"], rows[i+1]["settings"]["quality"]], ["low", "medium"])
                self.assertNotEqual(rows[i]["run_id"], rows[i+1]["run_id"])
        self.assertEqual({r["settings"]["quality"] for r in plan(self.cases, "quality-screen")}, {"low", "medium", "high"})

    async def test_configuration_rejected_before_credentials_and_files(self):
        good = {"case_id": "TEST-server", "model": SUNBURST, "quality": "medium"}
        bad = [{"runs": [good] * 7}, {"runs": [good] * 2}, {"runs": []},
               {"runs": [dict(good, reasoning_effort="high")]},
               {"runs": [dict(good, seed=1)]}, {"runs": [dict(good, quality="auto")]},
               {"runs": [dict(good, model="invented-model")]},
               {"runs": [dict(good, case_id="web-photo")]},
               {"runs": [dict(good, model="gpt-image-1-mini", quality="xhigh")]},
               {"runs": [dict(good, model=[SUNBURST])]}]
        with patch("visual.benchmark.os.environ.get", side_effect=AssertionError("credential read")):
            for configuration in bad:
                with self.assertRaises(ValueError):
                    await run_pilot(protocol_dir=PROTOCOL, output_dir=self.root, configuration=configuration,
                                    execute=True, budget_usd=6, paid_authorization="TEST")
            for settings in ({"budget_usd": 6.00000001}, {"max_calls": 7},
                             {"preset": "paired-flare", "execute": True, "reservation_usd": .99,
                              "budget_usd": 6, "paid_authorization": "TEST"}):
                with self.assertRaises(ValueError):
                    await run_pilot(protocol_dir=PROTOCOL, output_dir=self.root, **settings)
        self.assertFalse(self.root.exists())

    async def test_all_official_controls_no_reasoning_parameter_sent(self):
        calls = []
        def handler(request):
            body = json.loads(request.content)
            calls.append(body)
            self.assertNotIn("reasoning_effort", body)
            self.assertNotIn("input_fidelity", body)
            return httpx.Response(200, json={"data": [{"b64_json": base64.b64encode(raw_png()).decode()}]})
        client = self.client(handler)
        for model, qualities in MODEL_QUALITIES.items():
            for quality in qualities:
                # Mock-only: verifies exact controls without exercising paid models.
                await client.generate(model=model, quality=quality, reasoning_effort=None, prompt="synthetic")
        before = len(calls)
        with self.assertRaises(ValueError):
            await client.generate(model=SUNBURST, prompt="synthetic", reasoning_effort="high")
        self.assertEqual(len(calls), before)

    async def test_paired_receipts_durable_before_call_sequential_and_unscored(self):
        active, calls = 0, []
        raw = raw_png()
        async def handler(request):
            nonlocal active
            active += 1
            self.assertEqual(active, 1)
            body = json.loads(request.content)
            calls.append(body)
            ledger = json.loads((self.root / "artifacts.json").read_text(encoding="utf-8"))
            events = [json.loads(s) for s in (self.root / "receipts.jsonl").read_text().splitlines()]
            self.assertEqual(ledger["attempted_calls"], len(calls))
            self.assertEqual(ledger["reserved_usd"], len(calls))
            self.assertEqual(events[-1]["event"], "reserved_before_call")
            self.assertEqual(ledger["runs"][events[-1]["run_id"]]["status"], "reserved")
            await asyncio.sleep(0)
            active -= 1
            return httpx.Response(200, headers={"x-request-id": "req_quality"}, json={
                "model": SUNBURST, "quality": body["quality"], "size": "1024x1024", "output_format": "png",
                "data": [{"b64_json": base64.b64encode(raw).decode()}],
                "usage": {"input_tokens": 100, "output_tokens": 196,
                          "input_tokens_details": {"text_tokens": 100, "image_tokens": 0},
                          "private": "sk-offline-secret"}})
        rows, artifacts = await self.run_fake(handler)
        self.assertEqual([c["quality"] for c in calls], ["low", "medium"] * 3)
        self.assertEqual(artifacts["attempted_calls"], 6)
        self.assertEqual(artifacts["reserved_usd"], 6)
        self.assertIsNone(artifacts["winner"])
        self.assertIsNone(artifacts["p95_latency_ms"])
        for row in rows:
            entry = artifacts["runs"][row["run_id"]]
            self.assertEqual(entry["model_requested"], SUNBURST)
            self.assertEqual(entry["model_returned"], SUNBURST)
            self.assertEqual(entry["quality_returned"], row["settings"]["quality"])
            self.assertTrue(entry["returned_quality_matches_request"])
            self.assertIsNone(entry["reasoning_effort"])
            self.assertIn("no reasoning_effort", entry["reasoning_effort_reason"])
            self.assertEqual(entry["cost"]["estimated_total_cost_usd"], .00638)
            self.assertIsNone(entry["cost"]["actual_receipt_cost_usd"])
            self.assertIsNone(entry["human_quality"])
            self.assertIsNone(entry["reference_fidelity"])
            self.assertEqual(entry["reference_sha256"], [])
            self.assertFalse(row["product_safety_approval"])
        with (self.root / "human-comparison.csv").open(encoding="utf-8", newline="") as stream:
            comparisons = list(csv.DictReader(stream))
        self.assertEqual(len(comparisons), 3)
        self.assertTrue(all(r["reviewer"] == r["preferred_quality"] == r["reference_fidelity"] == "" for r in comparisons))
        self.assertIn('raw.png', (self.root / "human-comparison.html").read_text())
        for name in ("results.jsonl", "artifacts.json", "receipts.jsonl", "human-comparison.html", "human-comparison.csv"):
            self.assertNotIn("sk-offline-secret", (self.root / name).read_text(encoding="utf-8"))

    async def test_dry_paired_no_key_or_http_human_sheet_blank(self):
        with patch("visual.benchmark.os.environ.get", side_effect=AssertionError("env read")):
            rows, artifacts = await run_pilot(protocol_dir=PROTOCOL, output_dir=self.root, preset="paired-sunburst")
        self.assertEqual(artifacts["attempted_calls"], 0)
        self.assertTrue(all(r["latency_ms"] is None and r["actual_cost_usd"] is None for r in rows))
        self.assertTrue((self.root / "human-comparison.csv").exists())
        self.assertFalse(any(json.loads(s)["event"] == "reserved_before_call" for s in (self.root / "receipts.jsonl").read_text().splitlines()))

    async def test_private_or_missing_returned_model_is_null_not_inferred(self):
        def handler(request):
            return httpx.Response(200, headers={"x-request-id": "sk-offline-secret"}, json={
                "model": "sk-offline-secret", "data": [{"b64_json": base64.b64encode(raw_png()).decode()}]})
        rows, artifacts = await self.run_fake(handler, max_calls=1)
        entry = artifacts["runs"][rows[0]["run_id"]]
        self.assertIsNone(entry["model_returned"])
        self.assertIsNone(rows[0]["request_id"])
        self.assertTrue(all(r["status"] == "cancelled" for r in rows[1:]))
        self.assertNotIn("sk-offline-secret", (self.root / "artifacts.json").read_text())

    async def test_paired_cancellation_stops_and_preserves_reservation_receipt(self):
        calls = []
        def handler(request):
            calls.append(request)
            raise asyncio.CancelledError("private cancellation detail")
        rows, artifacts = await self.run_fake(handler)
        self.assertEqual(len(calls), 1)
        self.assertEqual(artifacts["reserved_usd"], 1)
        self.assertTrue(all(r["status"] == "cancelled" for r in rows))
        events = [json.loads(s) for s in (self.root / "receipts.jsonl").read_text().splitlines()]
        self.assertEqual(events[0]["event"], "reserved_before_call")
        self.assertEqual(events[1]["status"], "cancelled")
        self.assertTrue(all(e["event"] == "not_attempted" for e in events[2:]))
        self.assertTrue((self.root / "human-comparison.html").exists())

    async def test_receipt_write_failure_prevents_http(self):
        calls = []
        with patch("visual.benchmark.receipt", side_effect=OSError("disk failure")):
            with self.assertRaises(OSError):
                await self.run_fake(lambda request: calls.append(request))
        self.assertEqual(calls, [])
        self.assertEqual(json.loads((self.root / "artifacts.json").read_text())["reserved_usd"], 1)

    def test_estimate_is_not_receipt_and_never_double_counts(self):
        usage = {"input_tokens": 100, "input_tokens_details": {"text_tokens": 100, "image_tokens": 0}, "output_tokens": 1000}
        estimate = cost_estimate("gpt-image-1-mini", "medium", usage)
        self.assertEqual(estimate["estimated_total_cost_usd"], .0082)
        self.assertEqual(estimate["estimated_output_cost_usd"], .008)
        self.assertIsNone(estimate["actual_receipt_cost_usd"])
        self.assertIsNone(cost_estimate(SUNBURST, "high", {"input_tokens": 100, "output_tokens": 100})["estimated_total_cost_usd"])
        self.assertIsNone(cost_estimate("chatgpt-image-latest", "low", usage)["estimated_total_cost_usd"])
