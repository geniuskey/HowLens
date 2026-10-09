# [DONE] Bounded image quality comparison

Task `task_96c1d2506226`, dispatch `ctx_047d57e579b7`, Visual owner, 2026-10-09. Branch `feat-visual-foundation`; implementation commit `b8b01e892352ad4ad0e7be01f85dfed8aca3680e`, based on `890abd4`. This report is delivered in a following documentation commit; its exact SHA and remote push outcome are included in the coordinator completion receipt.

Extended the isolated image benchmark with current official model controls, configurable six-call presets, durable reservation receipts and automatic blank human comparison sheets. Thirty-nine offline tests pass and the committed selected-preset preview made zero provider calls, while returned metadata, billing receipts and semantic quality remain unmeasured. Backend owner execution and human review are the remaining dependencies; no winner, p95, reference-fidelity score or production approval is claimed.

## Changes

- `backend/visual/benchmark_config.py`: eleven documented exact model/alias/snapshot strings, model-specific quality validation, unsupported effort null/reason, six bounded presets and 1–6-row explicit comparison config, list-price estimates without double counting. Moving alias rates remain unknown.
- `backend/visual/benchmark.py`: preserves pinned Evaluation v1 schema and legacy parity; CLI defaults to selected paired Sunburst dry-run. Exact USD accounting now also caps the configured envelope at USD6. New comparison execution requires at least USD1 reservation per attempt. Reservation snapshots and append-only receipt events reach disk before HTTP; failures/timeouts consume attempts, cancellation stops the remainder.
- `backend/visual/benchmark_http.py`: explicit fixed quality requests, no retry, allowlisted returned model/quality/settings, numeric usage and sanitized request IDs. Omitted model/quality are never inferred from the request. Existing observed cancellation headers remain available.
- `backend/visual/benchmark_review.py`: raw-image HTML comparison and CSV for identical low/medium pairs; reviewer, preference and quality fields are blank until manual review. Reference-input hashes are empty with a reason because the pinned fixtures contain no image references.
- `backend/visual/tests/test_benchmark_quality.py`: nine added offline tests for all presets/control combinations, malformed/unsupported config before credentials, USD6/call bounds, reservation durability and sequential calls, secret suppression, dry-run sheets, cancellation and cost separation.
- `docs/workers/visual/image-quality/{OFFICIAL-CONTROLS.md,RUNBOOK.md,REPORT.md,preview/*}`: dated official sources, exact Backend-owner commands, evidence boundaries and fresh zero-call preview artifacts.

## Executed validation

```powershell
$env:PYTHONPATH = 'backend'
backend/visual/.venv/Scripts/python.exe -m unittest discover -s backend/visual/tests -v
```

Final implementation run: **39 tests passed**, exit 0, including all original 30 tests. HTTP is fake in the tests and images are deterministic synthetic PNGs; this is not live model performance evidence. Existing tests also verify non-guide/mock/unsafe inputs cannot call the production provider, read-only Evaluation schema parity, response bounds, timeout and cancellation metadata. No Backend route/model or Android file changed.

```powershell
$env:PYTHONPATH = 'backend'
backend/visual/.venv/Scripts/python.exe -m visual.benchmark --protocol-dir backend/visual/.venv/eval-protocol --output-dir docs/workers/visual/image-quality/preview --preset paired-sunburst --max-calls 6 --timeout-seconds 120 --budget-usd 6 --reservation-usd-per-call 1
```

Actual CLI output: `records=6`, `attempted_calls=0`, `measurement_source=not_run`, `actual_cost_usd=null`, exit 0. The plan alternates low/medium for `TEST-server`, `TEST-cobot`, `TEST-ups` using `gpt-image-2.5-sunburst-2026-09-08`; each pair has matching prompt and reference identities and distinct run IDs. CSV inspection confirmed three synthetic pairs with all reviewer/scoring fields empty. `git diff --check` passed before the implementation commit.

## Remaining measured work and limits

The coordinator explicitly selected paired Sunburst low/medium on the same three fixtures. [RUNBOOK.md](RUNBOOK.md) contains the exact six-attempt Backend-owner command with nonsecret task authorization ID, concurrency1/retry0, total timeout120 and USD6 reservation envelope. No paid generation, credential lookup or cross-PC key access happened on this Visual PC. Account access and successful calls are not established by documentation or mocks.

Every row records requested settings and measured timing/usage/errors when available; sidecar receipts add model/quality returned, hashes and estimated versus actual cost fields. 2.5 pre-run output cost remains null because fetched calculator consumption was not reliably established for its selected state. Complete returned usage yields an uncached standard list-rate estimate; actual cost stays null until genuine account billing evidence is available. USD6 bounds local reservations rather than provider billing. Three cases cannot establish a winner or p95, and absence of references cannot establish reference fidelity.

Real user appliance photos and the separately collected web-photo corpus remain in the input recognition/manual-matching evaluation lane. The two earlier ImageGen illustrations are clearly synthetic and are not counted as benchmark measurements. Production guide approval remains gated; manual figures must be retained by deterministic composition, and supplementary generation is only for missing figures after evidence validation. This task documents that production constraint without changing the production adapter or claiming a composition implementation.
