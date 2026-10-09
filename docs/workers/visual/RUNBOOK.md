# Visual library runbook

Run from the repository root in PowerShell (Python 3.9+):

```powershell
py -3 -m venv backend/visual/.venv
backend/visual/.venv/Scripts/python.exe -m pip install -r backend/visual/requirements.txt
$env:PYTHONPATH = (Resolve-Path backend).Path
backend/visual/.venv/Scripts/python.exe -m unittest discover -s backend/visual/tests -v
```

The tests import `visual` directly without backend models, routes, or parent init.
Backend integration can import `backend.visual.service` and `backend.visual.splitter`.
Call `configure_provider(adapter)` once at server startup. The adapter implements
`async generate_png(*, prompt: str) -> bytes`, owns server environment credentials
and bounded network timeouts, and propagates failure instead of returning placeholders.
No live adapter, provider model, SDK options or credentials are configured in W1.
Actual provider options require official provider documentation review in the follow-up task.
Missing configuration raises `ProviderNotConfiguredError`; non-guide/non-live inputs
raise `VisualApprovalError` before provider calls.

Only backend-validated stored analyses may be passed to this library. Local guards
check decision/mode, steps, evidence references, and required preconditions; they do
not independently verify manufacturer evidence or safety. The prompt includes only
step IDs/descriptions. For fewer than nine steps it repeats the same approved scenes
in order, without generating additional operations. Nine scenes use
`steps[index * len(steps) // 9]`; downstream mappings must use this same sequence.
If safe scene composition or semantic correspondence cannot be established, Backend
must mark the job failed and preserve approved text. Decoding alone is not approval.

PNG grids must be single-frame, at most 10 MiB/20MP, have sides divisible by three,
and have at least 32 pixels per panel side (96 per grid side). This is a technical
minimum, not a readability or semantic quality threshold. Non-divisible dimensions
are rejected without cropping away pixels. The splitter returns nine PNG byte strings
left to right, top to bottom. Transparency is retained.

Implementation reference: [official Pillow Image documentation](https://pillow.readthedocs.io/en/stable/reference/Image.html)
(`open` is lazy; `verify` checks integrity and reopening plus `load` decodes pixels;
`crop` uses pixel bounds). Tests use synthetic colors and offline provider doubles.
No real generation or semantic image QA has occurred.

## W2 OpenAI provider integration

Install visual `requirements.txt` into the Backend server environment (parent
dependency files remain Backend-owned). The adapter uses `httpx` directly, with
no OpenAI SDK requirement. At startup, after the Coordinator confirms API key and
budget readiness on the Backend PC:

```python
from backend.visual.openai_provider import OpenAIStoryboardProvider
from backend.visual.service import configure_provider, generate_storyboard, scene_step_ids
from backend.visual.splitter import split_storyboard

configure_provider(OpenAIStoryboardProvider.from_env())  # No HTTP call at startup.
# analysis must be the backend-validated stored live guide, never new client data.
grid_png = await generate_storyboard(analysis)
panels_png = split_storyboard(grid_png)
step_ids = scene_step_ids(analysis)
# zip(range(9), step_ids, panels_png) for intended panel mappings.
# Semantic acceptance must be decided separately before marking a job completed.
```

Public additive boundary: `scene_step_ids(analysis: dict) -> list[str]` returns
exactly nine row-major IDs, matching the prompt for all 1-9 approved-step counts.
It checks the same guide/live eligibility; mapping alone does not verify imagery.

Server environment:

| Variable | Default / validation |
| --- | --- |
| `OPENAI_API_KEY` | Required, server only; missing/blank raises `ProviderNotConfiguredError` |
| `HOWLENS_IMAGE_MODEL` | `gpt-image-1.5`; allowlist also includes `gpt-image-1-mini`, `gpt-image-2`, `gpt-image-2.5-sunburst`, `gpt-image-2.5-flare` |
| `HOWLENS_IMAGE_SIZE` | `1024x1024`; also `1536x1024`, `1024x1536` |
| `HOWLENS_IMAGE_QUALITY` | `low`; also `medium`, `high` |
| `HOWLENS_IMAGE_TIMEOUT_SECONDS` | `120`; finite 1-300 seconds |

Configuration is independent of readiness/budget authorization: constructing the
adapter makes no request, but generating does. Backend must keep live generation
disabled until explicitly authorized. No key was copied to the Visual PC and no
paid generation ran during W2. `gpt-image-1.5` is a supported compatibility default,
not a claim that it is the newest model. Model access remains account-dependent.

Request: fixed HTTPS `POST https://api.openai.com/v1/images/generations`, `n=1`,
`output_format=png`, `background=opaque`, `moderation=auto`. GPT Image returns
`data[0].b64_json`; the unsupported legacy `response_format` is omitted. Only
base64 is accepted; external URLs are never fetched. No retries or redirects;
environment proxy discovery is disabled. Overall async request and network phases
are bounded by the timeout; cancellation is propagated. Timeout does not prove
the upstream request was not charged, so Backend must not auto-retry it.

Response cap: 15 MiB streamed decoded HTTP body; decoded PNG cap: 10 MiB; image
cap: 20MP, single-frame PNG, exact requested source dimensions. To satisfy equal
3x3 panel geometry, the adapter resizes the **whole** validated PNG down to the
nearest divisible-by-three sides using Lanczos (1024x1024 -> 1023x1023, panels
341x341); it does not crop off an edge. This slight resampling is explicit and
does not establish panel boundary placement or semantic content correctness.

`ImageProviderTimeout` subclasses `TimeoutError`; `ImageProviderError` is a
sanitized upstream/network/response error. `InvalidStoryboardError` rejects
invalid PNG/dimensions. Backend maps these into failed jobs while preserving
text; never expose provider response bodies, secrets, or raw network exceptions.

Official documentation opened on 2026-10-09:
[Create image API reference](https://developers.openai.com/api/reference/resources/images/methods/generate),
[Image generation guide](https://developers.openai.com/api/docs/guides/image-generation).
Live output and semantic image acceptance remain pending.

Quick synthetic presentation preview (no live provider):

```powershell
$env:PYTHONPATH = (Resolve-Path backend).Path
backend/visual/.venv/Scripts/python.exe docs/workers/visual/mockups/make_panel_mockup.py
```

## W3 isolated benchmark pilot — default dry-run

The sandbox modules `visual.benchmark` and `visual.benchmark_http` do not import
product service, providers, routes, models, stores or approval logic. They never
fabricate a live guide. The pinned Evaluation cases/schema at
`0234159c2a5b4cc9443e79de30e90ef5586661d2` are read-only dependencies, not modified
or copied into tracked product fixtures. Canonical hashes reject changed cases
or schema before key access or requests. The public runner parameters were sent
to Coordinator early. Models are fixed to `gpt-image-1.5` and `gpt-image-1-mini`;
no arbitrary-model flag exists. Three unchanged synthetic prompts run interleaved,
each with both models, `1024x1024`, `low`, PNG/opaque/auto moderation, `n=1`.

Windows PowerShell from repository root (safe on Visual PC, no AI requests):

```powershell
backend/visual/.venv/Scripts/python.exe -m pip install -r backend/visual/requirements.txt
& ./docs/workers/visual/prepare_benchmark_protocol.ps1
$env:PYTHONPATH = (Resolve-Path backend).Path
$env:HOWLENS_BENCHMARK_PROTOCOL_DIR = (Resolve-Path backend/visual/.venv/eval-protocol).Path
backend/visual/.venv/Scripts/python.exe -m unittest discover -s backend/visual/tests -v
$benchmarkSession = 'backend/visual/.venv/benchmark-' + (Get-Date -Format 'yyyyMMdd_HHmmss')
backend/visual/.venv/Scripts/python.exe -m visual.benchmark --protocol-dir backend/visual/.venv/eval-protocol --output-dir $benchmarkSession
```

For a fresh environment, first create the visual venv using the W1 instructions.
Protocol preparation requires ordinary repository read access, not an API key.
Tests load those exact public protocol files; the optional environment override
locates them on another host. Test comparisons use the original Evaluation
planner/scorer/summary in addition to JSON Schema validation.

Default/dry-run does not read credentials or construct a network client.
It writes six `dry_run/not_run` records with latency, cost and usage null.
The committed zero-call sample is `benchmark-preview/w3-dry-run/`.
Every output directory must be new; there is no overwrite or resume execution.

**Future Backend PC owner only**, after explicit Coordinator paid authorization
and existing server environment readiness (do not run this command on Visual PC):

```powershell
$env:PYTHONPATH = (Resolve-Path backend).Path
$benchmarkSession = 'backend/visual/.venv/paid-pilot-' + (Get-Date -Format 'yyyyMMdd_HHmmss')
# Assign the actual nonsecret Coordinator approval record ID before running.
backend/visual/.venv/Scripts/python.exe -m visual.benchmark --protocol-dir backend/visual/.venv/eval-protocol --output-dir $benchmarkSession --execute --max-calls 6 --timeout-seconds 120 --budget-usd 6 --reservation-usd-per-call 1 --paid-authorization $coordinatorApprovalRecord
```

`$coordinatorApprovalRecord` must be the real approval ID, never a key/token.
`OPENAI_API_KEY` is read only on execute and only from the existing server env.
No account switching, key copying, token CLI argument or secret logging exists.
Budget values above are a **conservative reservation example requiring approval**,
not model prices or an invoice cap. Before each call, reserve the configured
amount; cap 1-6 total attempted calls, concurrency 1, retry 0. Failed/timed-out
calls consume their reservation. If the reservation cap or call cap is reached,
unattempted rows become `cancelled/not_run` with a stop reason. A timeout can
still incur provider charges; there is no automatic retry. Reservation accounting
cannot guarantee actual provider billing; independently enforce provider budgets.

Timeout range 1-300 seconds covers the total async request plus per-phase network
timeouts. Input files cap 1 MiB each; prompt cap 32,000 characters; response cap
15 MiB; raw PNG cap 10 MiB; source must be single-frame PNG of exactly 1024x1024.
No redirects/proxies from env, external URLs, retries or response-body logs.
Fixed official API parameters were rechecked in the linked Create image reference
on 2026-10-09. Product provider configuration/behavior remains untouched.

Artifacts:

- `results.jsonl`: exact Evaluation schema v1; same canonical JSON prompt/case/text
  digests and plan run IDs. `prompt_sha256` follows Evaluation's JSON-string digest,
  not raw UTF-8 hashing. Attempted live rows are `live`; HTTP-double rows are
  `offline`; dry-run/unattempted rows are `not_run`. Failure/timeout/cancellation,
  monotonic elapsed latency, UTC start, sanitized HTTP status/request ID and known
  numeric usage are retained. Unknown model snapshot stays null, requested model
  alias is recorded; seed unsupported/not sent. Actual billing and billing evidence
  remain null without a real receipt; there is no invented cost estimate.
- `artifacts.json`: deterministic conversion sidecar keyed by `run_id` for fields
  excluded by Evaluation's `additionalProperties=false`: exact endpoint/request
  options, protocol/settings and prompt UTF-8 hashes, timeout/cap/reservations,
  attempted flags, raw/normalized paths+byte counts+SHA-256. No provider body is
  saved. Usage remains provider token counters; there is no addition of an image
  output proxy to output-token charges.
- `RUN_ID/raw.png`: original decoded provider PNG bytes, **no resampling**.
  `RUN_ID/normalized.png`: separately saved whole-grid Lanczos resize 1024→1023,
  with nine intended boxes in schema. Normalization and hashing never establish
  that drawn scenes, topology, text, arrows or actions are correct.

Evaluation's scorer can read `results.jsonl` records one at a time with the
normalized PNG path; fake-success integration test remains `pending_human_review`.
Summary excludes offline outputs and dry runs from live comparisons. Human reviewer
criteria are draft; no speed/cost/quality winner is inferred from three cases.
All benchmark plans/artifacts are sandbox-only and product safety approval is false.

Quick reviewer-sheet mockup (synthetic only):

```powershell
backend/visual/.venv/Scripts/python.exe docs/workers/visual/mockups/make_benchmark_mockup.py
```
