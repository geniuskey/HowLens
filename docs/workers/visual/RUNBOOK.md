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
