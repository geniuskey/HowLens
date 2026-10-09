"""Opt-in direct image sandbox pilot. Default is a credential-free, zero-call plan."""
from __future__ import annotations

import argparse
import asyncio
import hashlib
import json
import math
import os
import re
import time
from datetime import datetime, timezone
from io import BytesIO
from pathlib import Path

import jsonschema
from PIL import Image

from .benchmark_http import SandboxImageClient, BenchmarkFailure, PILOT_MODELS

PROTOCOL_COMMIT = "0234159c2a5b4cc9443e79de30e90ef5586661d2"
CASES_DIGEST = "88437eba5d2ad085fe5f51c1cd8b71c5440e84e05337e354d2ab3ec20ee6f561"
SCHEMA_DIGEST = "bdfc79a1300b336b291f3aae45b637aa8b7cbb08a28bcb3016a76feff1bd2f50"
SETTINGS = {"size": "1024x1024", "quality": "low", "format": "png", "concurrency": 1, "retries": 0}
MAX_INPUT_BYTES = 1024 * 1024


def digest(value):
    # Exact Evaluation protocol v1 digest; not raw UTF-8 prompt SHA.
    return hashlib.sha256(json.dumps(value, sort_keys=True, ensure_ascii=False).encode()).hexdigest()


def bytes_digest(value):
    return hashlib.sha256(value).hexdigest()


def read_bounded(path):
    with Path(path).open("rb") as stream:
        content = stream.read(MAX_INPUT_BYTES + 1)
    if len(content) > MAX_INPUT_BYTES:
        raise ValueError("Protocol input exceeds 1 MiB")
    return content.decode("utf-8-sig")


def load_protocol(directory):
    root = Path(directory)
    cases = [json.loads(line) for line in read_bounded(root / "cases.jsonl").splitlines() if line.strip()]
    schema = json.loads(read_bounded(root / "run-result.schema.json"))
    if digest(cases) != CASES_DIGEST or digest(schema) != SCHEMA_DIGEST:
        raise ValueError("Expected unchanged Evaluation cases/schema at the pinned protocol commit")
    if len(cases) != 3 or {c.get("case_id") for c in cases} != {"TEST-server", "TEST-cobot", "TEST-ups"}:
        raise ValueError("Expected the three Evaluation synthetic pilot cases")
    for case in cases:
        if (case.get("sandbox_only") is not True or case.get("asset_basis") != "synthetic"
                or case.get("reference_assets") != [] or not isinstance(case.get("prompt"), str)
                or not 1 <= len(case["prompt"]) <= 32000
                or not isinstance(case.get("steps"), list) or not 1 <= len(case["steps"]) <= 9
                or not isinstance(case.get("scene_step_ids"), list) or len(case["scene_step_ids"]) != 9):
            raise ValueError("Invalid synthetic pilot case")
        step_ids = {step["step_id"] for step in case["steps"]}
        if not set(case["scene_step_ids"]) <= step_ids:
            raise ValueError("Unknown scene step ID")
    jsonschema.Draft202012Validator.check_schema(schema)
    return cases, schema


def plan(cases):
    rows = []
    # Interleave model order on identical cases, rather than finish one model first.
    for case in cases:
        for model in PILOT_MODELS:
            rows.append({"schema_version": "1", "run_id": digest([case, model, SETTINGS])[:20],
                "case_id": case["case_id"], "track": case["track"], "model": model,
                "settings": dict(SETTINGS), "prompt": case["prompt"], "prompt_sha256": digest(case["prompt"]),
                "case_sha256": digest(case), "reference_assets": [], "scene_step_ids": case["scene_step_ids"],
                "text_sha256": digest(case["steps"]), "criteria_version": "draft-awaiting-team-lead",
                "sandbox_only": True, "status": "dry_run", "attempt": 1, "paid_authorization": None,
                "measurement_source": "not_run", "provider": None, "model_snapshot": None,
                "seed": None, "retry_reason": None, "billing_evidence": None, "panels": [],
                "latency_ms": None, "actual_cost_usd": None, "usage": None, "output_path": None,
                "http_status": None, "error_code": None, "request_id": None, "started_at_utc": None,
                "raw_dimensions": None, "normalized_dimensions": None, "normalization": None,
                "human_review": None, "product_safety_approval": False})
    return rows


def validate_options(*, max_calls, timeout_seconds, budget_usd, reservation_usd, execute, paid_authorization):
    if type(max_calls) is not int or not 1 <= max_calls <= 6:
        raise ValueError("max_calls must be an integer 1-6")
    for value in (timeout_seconds, budget_usd, reservation_usd):
        if type(value) not in (int, float) or not math.isfinite(value) or value < 0:
            raise ValueError("Budget/timeout settings must be finite nonnegative numbers")
    if not 1 <= timeout_seconds <= 300 or reservation_usd <= 0:
        raise ValueError("Timeout must be 1-300s and reservation must be positive")
    if execute and (not isinstance(paid_authorization, str)
                    or not re.fullmatch(r"[A-Za-z0-9_.:/-]{1,120}", paid_authorization)
                    or paid_authorization.startswith("sk-") or budget_usd <= 0):
        raise ValueError("Execute requires a nonsecret authorization record ID and positive budget")


def save_images(root, run_id, raw, scene_ids):
    # run_id is generated internally, never an input file path.
    folder = root / run_id
    folder.mkdir()
    raw_path = folder / "raw.png"
    raw_path.write_bytes(raw)
    with Image.open(BytesIO(raw)) as image:
        image.load()
        with image.convert("RGB") as rgb:
            with rgb.resize((1023, 1023), Image.Resampling.LANCZOS) as normalized:
                output = BytesIO()
                normalized.save(output, format="PNG")
                normalized_bytes = output.getvalue()
                normalized_path = folder / "normalized.png"
                normalized_path.write_bytes(normalized_bytes)
    panels = []
    for index, step_id in enumerate(scene_ids):
        x, y = index % 3 * 341, index // 3 * 341
        panels.append({"index": index, "step_id": step_id, "box": [x, y, x + 341, y + 341]})
    return normalized_path.as_posix(), panels, {
        "raw_path": raw_path.as_posix(), "raw_sha256": bytes_digest(raw),
        "normalized_path": normalized_path.as_posix(), "normalized_sha256": bytes_digest(normalized_bytes),
        "raw_bytes": len(raw), "normalized_bytes": len(normalized_bytes)}


async def run_pilot(*, protocol_dir, output_dir, execute=False, max_calls=6,
                    timeout_seconds=120, budget_usd=0, reservation_usd=1,
                    paid_authorization=None, client=None, clock=time.monotonic):
    validate_options(max_calls=max_calls, timeout_seconds=timeout_seconds, budget_usd=budget_usd,
                     reservation_usd=reservation_usd, execute=execute, paid_authorization=paid_authorization)
    cases, schema = load_protocol(protocol_dir)
    rows = plan(cases)
    validator = jsonschema.Draft202012Validator(schema)
    for row in rows:
        validator.validate(row)
    # Dry-run deliberately does not read the environment or construct a client.
    if execute and client is None:
        client = SandboxImageClient(api_key=os.environ.get("OPENAI_API_KEY", ""), timeout_seconds=timeout_seconds)
    root = Path(output_dir).resolve()
    root.mkdir(parents=True, exist_ok=False)  # Never overwrite or mix distinct sessions.
    artifacts = {"schema_version": "visual-artifacts-1", "evaluation_protocol_commit": PROTOCOL_COMMIT,
                 "protocol_cases_sha256": CASES_DIGEST, "protocol_schema_sha256": SCHEMA_DIGEST,
                 "endpoint": "https://api.openai.com/v1/images/generations",
                 "request_settings": {"size": "1024x1024", "quality": "low", "n": 1,
                                      "output_format": "png", "background": "opaque", "moderation": "auto"},
                 "measurement_source": ("offline" if client is not None and client._transport is not None else "live") if execute else "not_run",
                 "timeout_seconds": timeout_seconds, "max_calls": max_calls,
                 "budget_reservation_cap_usd": budget_usd, "reservation_per_call_usd": reservation_usd,
                 "cost_policy": "reservation is a planning cap, not actual billing; no cost estimate summed with tokens",
                 "attempted_calls": 0, "reserved_usd": 0, "runs": {}}
    source = artifacts["measurement_source"]
    results_path = root / "results.jsonl"
    stop_reason = None
    with results_path.open("w", encoding="utf-8") as stream:
        for row in rows:
            sidecar = {"prompt_utf8_sha256": bytes_digest(row["prompt"].encode("utf-8")),
                       "settings_sha256": digest(row["settings"]), "attempted": False}
            if execute:
                if stop_reason is None and artifacts["attempted_calls"] >= max_calls:
                    stop_reason = "call_limit"
                if stop_reason is None and artifacts["reserved_usd"] + reservation_usd > budget_usd + 1e-9:
                    stop_reason = "reservation_budget_limit"
                if stop_reason:
                    row.update(status="cancelled", error_code=stop_reason)
                else:
                    artifacts["attempted_calls"] += 1
                    artifacts["reserved_usd"] = round(artifacts["reserved_usd"] + reservation_usd, 8)
                    sidecar["attempted"] = True
                    row.update(paid_authorization=paid_authorization, measurement_source=source, provider="openai",
                               started_at_utc=datetime.now(timezone.utc).isoformat())
                    started = clock()
                    try:
                        result = await client.generate(model=row["model"], prompt=row["prompt"])
                        # Latency includes provider request/response/decode; disk work is excluded.
                        row["latency_ms"] = max(0, (clock() - started) * 1000)
                        row.update(request_id=result["request_id"], http_status=result["http_status"], usage=result["usage"])
                        path, panels, saved = save_images(root, row["run_id"], result["raw_png"], row["scene_step_ids"])
                        sidecar.update(saved)
                        row.update(status="success", output_path=path, panels=panels,
                                   raw_dimensions=[1024, 1024], normalized_dimensions=[1023, 1023],
                                   normalization="whole-image Lanczos resize 1024x1024 to 1023x1023; no crop")
                    except BenchmarkFailure as exc:
                        row.update(status="timeout" if exc.code == "timeout" else "api_error",
                                   error_code=exc.code, request_id=exc.request_id, http_status=exc.http_status,
                                   usage=exc.usage,
                                   latency_ms=max(0, (clock() - started) * 1000))
                    except asyncio.CancelledError:
                        row.update(status="cancelled", error_code="cancelled",
                                   latency_ms=max(0, (clock() - started) * 1000))
                        stop_reason = "cancelled"
                    except OSError:
                        row.update(status="api_error", error_code="artifact_write_error",
                                   latency_ms=row["latency_ms"] if row["latency_ms"] is not None else max(0, (clock() - started) * 1000))
                        stop_reason = "artifact_write_error"
            validator.validate(row)
            artifacts["runs"][row["run_id"]] = sidecar
            stream.write(json.dumps(row, ensure_ascii=False) + "\n")
            stream.flush()
            (root / "artifacts.json").write_text(json.dumps(artifacts, ensure_ascii=False, indent=2), encoding="utf-8")
    return rows, artifacts


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--protocol-dir", required=True)
    parser.add_argument("--output-dir", required=True, help="New session directory; must not exist")
    parser.add_argument("--execute", action="store_true", help="Paid Backend-owner sandbox only, after authorization")
    parser.add_argument("--max-calls", type=int, default=6)
    parser.add_argument("--timeout-seconds", type=float, default=120)
    parser.add_argument("--budget-usd", type=float, default=0)
    parser.add_argument("--reservation-usd-per-call", type=float, default=1)
    parser.add_argument("--paid-authorization", help="Nonsecret Coordinator authorization record ID")
    args = parser.parse_args(argv)
    try:
        rows, artifacts = asyncio.run(run_pilot(protocol_dir=args.protocol_dir, output_dir=args.output_dir,
            execute=args.execute, max_calls=args.max_calls, timeout_seconds=args.timeout_seconds,
            budget_usd=args.budget_usd, reservation_usd=args.reservation_usd_per_call,
            paid_authorization=args.paid_authorization))
    except (ValueError, OSError, jsonschema.exceptions.ValidationError, jsonschema.exceptions.SchemaError):
        parser.exit(2, "Benchmark configuration/artifact error; no secret details logged.\n")
    print(json.dumps({"records": len(rows), "attempted_calls": artifacts["attempted_calls"],
                      "measurement_source": artifacts["measurement_source"], "actual_cost_usd": None}))
    return 1 if args.execute and any(row["status"] != "success" for row in rows) else 0


if __name__ == "__main__":
    raise SystemExit(main())
