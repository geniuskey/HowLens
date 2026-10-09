# Backend W1 runbook

Validated 2026-10-09 on macOS / Python 3.14.6. Run from repository root:

```sh
python3 -m venv backend/.venv
backend/.venv/bin/pip install -e 'backend[test]'
backend/.venv/bin/python -m pytest backend/tests -q
backend/.venv/bin/pip check
backend/.venv/bin/pip wheel --no-deps --wheel-dir /tmp/howlens-backend-w1-wheel ./backend
backend/.venv/bin/uvicorn howlens.main:app --host 127.0.0.1 --port 8000 --no-access-log
```

The venv was created in the preserved checkpoint; editable install, wheel build, pip check and tests were executed successfully in the current Dispatch. From `backend/`, `.venv/bin/python -m pytest -q` also passed: 38 passed, 1 warning in 0.37s. The warning is Starlette 1.7.0 deprecating httpx TestClient support; it is not suppressed. An actual subprocess Uvicorn smoke test used a dynamically selected localhost port, fetched `/health`, asserted `{"status":"ok","mode":"live"}`, and terminated the subprocess; PASS. Port 8000 in the command above is the normal launch example, not the smoke-test port.

Default runtime is live mode with no provider/manual registry/reviewer; valid analysis returns retryable 503 `provider_unconfigured`. Tests are synthetic offline fixtures and never runtime fallback. No real public PDF, multimodal provider or visual package has been integrated. `create_app(provider=..., registry=..., reviewer=...)` is the explicit server-only dependency boundary; adapters must be async, impose upstream response limits before materializing output, keep secrets in environment variables, treat photo/manual text as data and avoid guaranteeing safety/normal operation from photos. Manual registration requires independently checked device/version/PDF page/exact quote/source/rights and supported-action mapping; model assertions cannot register evidence. Independent reviewer approval requires provenance, equipment confirmation, sufficient action evidence, hazard assessment and observed/confirmed required preconditions.

Limits: 10 MiB JPEG/PNG file, 20MP decoded pixels; MIME must match decoded format. Multipart total is bounded to 10 MiB + 64 KiB before parsing; receive idle timeout 15s and total receive deadline 30s. Read/decode has 15s response timeout and two decode slots; Pillow runs in a thread and Python cannot forcibly terminate that thread on timeout. Production hard CPU cancellation/process isolation and deployment-wide concurrency limits remain follow-up work. Question is trimmed then constrained to 1–2000 characters; optional confirmation max 2000. Provider/review timeout defaults to 30s including semaphore queueing, with two upstream slots. DTOs forbid extra fields, revalidate instances, limit each string to 4000 characters, arrays to 128 entries, and serialized response to 128 KiB. Provider failures/malformed/oversized output return generic retryable 503; timeout returns 504 without private upstream details.

Storage: FIFO maximum 16 analyses / 64 MiB original-photo plus serialized-analysis bytes. Jobs are independently bounded to two per retained analysis, with metadata/object overhead beyond that byte accounting. Original bytes are private, preserved exactly, and have no serving endpoint; multipart temporary upload files are closed. Associated jobs are evicted with the analysis. Restart loses IDs. Non-guide or non-live analysis cannot enter visual/verification. Missing Visual integration returns failed/202; one explicit user retry, then the same second failure is returned. No image generation occurs and approved text remains stored. Successful visual asset serving and semantic panel review are a later integration task.

Verification uses only stored original photo and approved steps, accepts no client procedures, requires matching identity/live mode and evidence IDs contained in the original analysis, and always adds a visual-only limitation. The provider implementation remains responsible for conservative observation language; no live adapter is configured here.

Official implementation references checked:
- https://fastapi.tiangolo.com/tutorial/request-files/ — multipart UploadFile and python-multipart.
- https://pillow.readthedocs.io/en/stable/handbook/security.html — JPEG/PNG allowlist and decompression protections.

## W2 real Responses adapter configuration

The configured entry point supersedes the W1 `howlens.main:app` launch for env-backed provider work:

```sh
cd /Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation
backend/.venv/bin/pip install -e 'backend[test]'
backend/.venv/bin/python -m howlens.config
backend/.venv/bin/uvicorn howlens.config:configured_app --factory --host 0.0.0.0 --port 8000 --no-access-log
```

Human key-entry location (opened in Orca): `/Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation/backend/.env`. It is ignored, owner-readable/writable only (0600), and existing content is preserved. `backend/.env.example` is also ignored and contains blank fields. If setting up a new checkout, create these blank fields in `backend/.env` with mode 0600:

```dotenv
OPENAI_API_KEY=
OPENAI_MODEL=
HOWLENS_PAID_CALLS_ENABLED=
```

Enter the key locally in the editor; never paste it into the terminal, chat, Git, or another PC. Choose an explicit OpenAI API model supporting vision/Responses/strict structured outputs; there is no default model and no claim of account/model access. The dotenv loader reads only the absolute backend `.env` path and preserves process environment overrides. Restart the server after changing configuration. Keep `HOWLENS_PAID_CALLS_ENABLED` blank until the Coordinator explicitly confirms API budget and key readiness; only then may the human/coordinator set it to `true`. Codex subscription/credits are not evidence of API budget. A key alone cannot trigger a provider request.

Current local network address observed by `ipconfig getifaddr en0`: `10.102.72.28`. After launch, health is `http://127.0.0.1:8000/health` locally or `http://10.102.72.28:8000/health` from the same reachable LAN. Address may change; firewall/VPN/device reachability is not tested. Android emulator on this same host can use host address `10.0.2.2` where supported. This is a local development serve instruction, not a public deployment. The factory was actually started on `0.0.0.0` with an ephemeral port, health fetched on localhost, and stopped; no persistent server is left running.

`python -m howlens.config` prints only booleans/counts, never key/model values. Last observed readiness: key_present=false, model_configured=false, paid_calls_enabled=false, manual_entries=0, independent_physical_review_ready=false, visual_integrated=false. `/health` still means API process healthy/live mode, not provider/key/manual/guide readiness.

Adapter uses the official `POST https://api.openai.com/v1/responses` REST boundary, `input_image` base64 JPEG/PNG data URLs, strict `text.format` JSON schema from the DTOs, `store:false`, explicit model, 3000 output-token cap, 25s total timeout, no automatic retries or tools, no redirects/proxy environment routing, streamed response cap 1 MiB and existing typed DTO limits. Refusal/incomplete/malformed/foreign-mode responses fail; HTTP timeout maps to 504 and other upstream failures to generic 503. Offline MockTransport tests validate request shape; no real request/schema/model compatibility was exercised.

Manual catalog is intentionally empty. `APPROVED_SOURCE_URLS` is an exact server-owned original-PDF URL allowlist; trusted `ManualEntry` registration requires device/version/page/exact excerpt, source URL, PDF SHA-256, independent reviewer, usage terms and 1–9 supported actions. The digest/metadata are registration prerequisites, not proof that a file has been acquired or reviewed; actual review must happen before inserting production entries. Existing deterministic registry matches complete excerpt/page/source/version/device and supported action text. Photo/manual/question/confirmation text stays untrusted. Live adapter converts every required prerequisite to unknown, and conservative reviewer issues no physical approval: model claims never promote guide. Grounded observations need approved manual excerpts; a guide additionally needs an independent observation/confirmation channel and hazard/action review.

Visual public functions were read from published `ed04a43f87d542622527efcbf383151c702d796e` using `git show`, without merging/copying/editing Visual-owned files. `howlens.visual_boundary.generate_reviewed_assets` is prepared against `visual.service.generate_storyboard(analysis: dict) -> bytes` and `visual.splitter.split_storyboard(bytes) -> list[bytes]`, requires stored independently approved live analysis, budget and async semantic reviewer before invocation, validates exactly nine bounded panels, and derives row-major repeated step IDs using the published scene mapping. HTTP job activation/asset storage remain disabled until an authorized integration task. The current visual route still returns explicit failed jobs preserving text, even if the library later becomes importable.

W2 validation: 54 passed, 1 Starlette httpx deprecation warning in 0.51s; editable install succeeded with python-dotenv 1.2.4; `pip check` clean; W2 wheel built; configured env-loader Uvicorn health smoke PASS. No paid calls, actual images/model outputs, manufacturer PDFs or semantic storyboard validation were performed.

Official docs fetched before implementation:
- https://developers.openai.com/api/docs/guides/images-vision — Responses input_image/data URL example.
- https://developers.openai.com/api/docs/guides/structured-outputs?api-mode=responses — strict text.format JSON schema, required properties, refusals.
- https://developers.openai.com/api/docs/guides/migrate-to-responses — store:false and Responses migration.
