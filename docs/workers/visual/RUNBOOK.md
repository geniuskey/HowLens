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
