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
