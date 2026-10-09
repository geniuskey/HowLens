# Backend W1 runbook

Checkpoint: 2026-10-09, Python 3.14.6 on macOS. Runtime defaults to live mode with no provider/manual registry configured; valid analysis upload returns explicit retryable 503. No real provider, public PDF, or visual package has been integrated.

From repository root:

```sh
python3 -m venv backend/.venv
backend/.venv/bin/pip install -e 'backend[test]'
cd backend
.venv/bin/python -m pytest -q
.venv/bin/uvicorn howlens.main:app --host 127.0.0.1 --port 8000
```

The editable installation command and uvicorn launch above are intended next-run commands, not yet verified at this checkpoint. The command actually executed successfully was:

```sh
backend/.venv/bin/pip install fastapi uvicorn python-multipart pillow httpx pytest
PYTHONPATH=backend backend/.venv/bin/python -m pytest backend/tests -q
```

Result: 21 passed, 1 warning in 1.13s. Warning: Starlette 1.7.0 deprecates httpx TestClient support in favor of httpx2. No test suppression was added.

Configuration is via `create_app` dependency injection for now. Only async provider adapters are permitted; independent trusted review, verified manual excerpts/action mappings, and provenance must be supplied separately. Never configure tests as a runtime fallback. Future external API secrets belong only in environment variables; no secret files or photo logs are present.

Storage: FIFO maximum 16 analyses / 64 MiB original photo bytes, 2 failed visual attempts per analysis; associated jobs are evicted with analysis. Original upload bytes stay private, with no photo-serving route. Restart loses all IDs. The visual endpoint intentionally returns a failed job until the visual library, asset storage and semantic review are connected; text survives.

Reference checks used for implementation:
- https://fastapi.tiangolo.com/tutorial/request-files/ — multipart UploadFile and python-multipart.
- https://pillow.readthedocs.io/en/stable/handbook/security.html — format allowlist/decompression limits.

Remaining verification: editable package install/build, streaming multipart overflow/timeout behavior, verification provider error/foreign evidence tests, DTO/output size bounds and memory accounting, full suite from backend project directory, and integration review before push.
