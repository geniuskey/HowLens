# Paired quality benchmark — Backend account owner

Selected by Coordinator for task `task_96c1d2506226`: pinned Sunburst low/medium on the same three unchanged synthetic Evaluation fixtures. Six requests maximum, concurrency 1, retry 0, one USD1 reservation for every attempted call, USD6 reservation envelope. The CLI defaults to this preset and to zero calls. The Python `run_pilot` default remains `legacy-low` for compatibility with existing Evaluation parity tests; always pass the selected preset explicitly.

## Preparation

Run from the repository root on the Backend owner's PC/account after accepting the Visual commit. Use only that account's already authorized local `OPENAI_API_KEY`; no key is printed, shared, copied or loaded from another PC. These are the existing public Evaluation dependencies at `0234159c2a5b4cc9443e79de30e90ef5586661d2`: `evaluation/image_models/{cases.jsonl,run-result.schema.json,protocol.py}`. The existing preparation script exports only those pinned files into an ignored directory and the runner verifies their content hashes.

```powershell
if (-not (Test-Path backend/visual/.venv/Scripts/python.exe)) { python -m venv backend/visual/.venv }
backend/visual/.venv/Scripts/python.exe -m pip install -r backend/visual/requirements.txt
& ./docs/workers/visual/prepare_benchmark_protocol.ps1
$env:PYTHONPATH = 'backend'
backend/visual/.venv/Scripts/python.exe -m unittest discover -s backend/visual/tests -v
```

## Exact zero-call preview command

```powershell
$env:PYTHONPATH = 'backend'
$qualityPreview = 'backend/visual/.venv/sunburst-preview-' + [guid]::NewGuid().ToString('N')
backend/visual/.venv/Scripts/python.exe -m visual.benchmark --protocol-dir backend/visual/.venv/eval-protocol --output-dir $qualityPreview --preset paired-sunburst --max-calls 6 --timeout-seconds 120 --budget-usd 6 --reservation-usd-per-call 1
```

Expected summary: six records, zero attempted calls, measurement_source not_run, actual cost null. No environment credential read occurs in dry-run. Committed `preview/` was produced this way and contains no generated image.

## Exact measured command — Backend owner only

Coordinator selected this task and will arrange the measured run. Execute once on the Backend account with its local authorized key, after the zero-call preview is checked:

```powershell
$env:PYTHONPATH = 'backend'
$qualitySession = 'backend/visual/.venv/sunburst-measured-' + [guid]::NewGuid().ToString('N')
backend/visual/.venv/Scripts/python.exe -m visual.benchmark --protocol-dir backend/visual/.venv/eval-protocol --output-dir $qualitySession --preset paired-sunburst --execute --max-calls 6 --timeout-seconds 120 --budget-usd 6 --reservation-usd-per-call 1 --paid-authorization task_96c1d2506226
```

`task_96c1d2506226` is the nonsecret authorization record for this selected run, not a key. If local paid authorization or model access is absent, report that state; do not borrow credentials. Max six attempts counts errors/timeouts too. Reservations are exact integer USD units, durably persisted and journaled before HTTP. They are never refunded based on an uncertain timeout/cancellation. USD6 is a conservative local reservation limit, not an enforceable provider billing cap; actual receipt cost remains unknown until account reconciliation. Every new invocation is a new envelope, so do not repeat the run automatically after failures.

## Output and review

- `results.jsonl`: unchanged Evaluation v1 schema, requested model/settings, input protocol hashes, timing, usage, failures and product_safety_approval false.
- `artifacts.json`: requested/returned model and quality, safe returned settings, supported controls, null effort plus reason, exact UTF-8 prompt hash, empty reference hashes plus no-reference reason, raw/normalized output hashes, reservations and list-price estimates versus null actual receipt costs.
- `receipts.jsonl`: flushed reservation-before-call events, finished attempts and explicit unattempted cancellation rows. A process kill may leave a reservation without a finished event: do not interpret that as no charge. No automatic resume/retry exists.
- `<run_id>/raw.png`: retained exact response image, recommended for human comparison. `normalized.png`: resized to 1023 square for the existing nine-panel coordinate contract; resizing/splitting does not verify panel meaning.
- `human-comparison.html` / `human-comparison.csv`: automatically grouped low/medium pairs with identical prompt/reference identities. Human reviewer, preference and all quality fields start blank. No reference input exists, so leave fidelity blank and document not applicable. Fill instruction/step adherence, geometry, legibility and notes manually; preserve the original raw receipts.

Model or quality omitted by the provider stays null with an omission reason; requested IDs are not proof of returned snapshots. A returned-quality mismatch is recorded for reviewer attention. No raw provider text, authorization header or credential is written. Failed disk reservation prevents HTTP; failed image writes stop subsequent attempts. Exit 0 means successful dry plan/all executed requests; 1 means measured run has a failure/cancelled record; 2 means configuration/artifact failure with sanitized stderr. A completed request is not a quality pass or guide approval.

## Alternative configurations

Only one bounded preset is selected per invocation; do not execute all presets under this task's total budget. `paired-flare` and `paired-mini` also give three low/medium pairs. `quality-screen` compares Sunburst/mini low/medium/high on one identical case (six requests); `current-model-screen` compares Sunburst/Flare at those qualities. `legacy-low` retains the prior low-only plan.

For explicitly authorized comparisons, `--comparison-config path.json` accepts only this shape, with 1–6 unique case/model/quality rows and optional null effort. All requests remain square PNG n1. Unknown parameters, non-null effort, unknown fixtures, retired models, unsupported quality and duplicate runs are rejected before credentials/network. Example:

```json
{"runs":[{"case_id":"TEST-server","model":"gpt-image-2.5-sunburst-2026-09-08","quality":"high","reasoning_effort":null},{"case_id":"TEST-server","model":"gpt-image-2.5-flare-2026-09-08","quality":"high","reasoning_effort":null}]}
```

Use [official controls](OFFICIAL-CONTROLS.md) for current sources and evidence boundaries. Public/photo-reference edit comparisons need a separately approved fixture protocol; the pinned synthetic protocol deliberately rejects reference assets. Preserve manual figures through deterministic composition and keep the existing production guide gate; this runner imports neither production service nor Backend routes.
