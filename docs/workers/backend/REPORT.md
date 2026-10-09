# W1-BACKEND checkpoint

[BLOCKER] Coordinator requested permission-mode restart at the first implementation/test checkpoint; this session remains workspace-write/on-request. Implementation is preserved and must resume in a fresh Dispatch; this is not final task completion.

Implemented FastAPI v0.1 health/analyses/visual-jobs/verification endpoints, strict DTOs, JPEG/PNG decoding and 10 MiB/20MP limits, server-generated analysis IDs, private original-photo bounded memory store, provider timeout/error mapping, independent manual registry/reviewer approval boundaries and unsafe step sanitization. Initial offline TestClient suite passes 21 tests; missing provider is 503, missing visual integration is an explicit failed job with one user retry, and no real guide/evidence is fabricated. Remaining work is package/install validation, additional edge-case coverage and implementation review, docs finalization, commit/push verification and final completion reporting under a new Dispatch.

## Branch / base

- Branch: `ljyonefineday/feat-backend-foundation`.
- Base confirmed: `75c8eac docs: define role-scoped orchestration and API foundation`.
- Checkpoint commit SHA: use `git log -1 --format=%H` after checkpoint commit (this report is included in that commit).
- No main merge and no edits outside backend ownership.

## Actual commands and results

1. `git status --short --branch`: clean worker branch.
2. `git fetch origin main`: initial sandbox error opening shared FETCH_HEAD; escalated retry succeeded.
3. `git merge --ff-only origin/main`: Already up to date.
4. `git merge-base --is-ancestor 75c8eac HEAD`: exit 0; HEAD was 75c8eac.
5. `python3 -m venv backend/.venv`: succeeded.
6. `backend/.venv/bin/pip install fastapi uvicorn python-multipart pillow httpx pytest`: sandbox attempt had DNS failure; escalated retry installed dependencies successfully.
7. First `backend/.venv/bin/python -m pytest backend/tests -q`: collection failed because `howlens` package did not exist yet, confirming missing implementation (not a completed behavioral red run).
8. `PYTHONPATH=backend backend/.venv/bin/python -m pytest backend/tests -q`: **21 passed, 1 warning in 1.13s**.
9. Orca inbox/heartbeat commands succeeded after runtime_access_denied required escalation; inbox delivered coordinator restart instruction, which triggered this checkpoint.

All provider/manual fixtures are synthetic offline test doubles; no live multimodal call or manufacturer PDF was tested. No real public manual was acquired, registered or claimed complete. Test coverage includes health, unconfigured/error/timeout provider, invalid bytes/MIME/file size/pixel size, question/device validation, untrusted/fabricated/unsafe guide suppression, non-guide 409, unknown ID 404, absent visual failure/retry limit, original photo retention, visual-only verification limitation and store eviction.

## Changed files

- `backend/.gitignore`, `backend/pyproject.toml`
- `backend/howlens/__init__.py`, `models.py`, `provider.py`, `safety.py`, `store.py`, `main.py`
- `backend/tests/test_api.py`
- `docs/workers/backend/RUNBOOK.md`, `docs/workers/backend/REPORT.md`

## Resume-critical review

- No `backend/visual/` files exist or were edited. Visual endpoint deliberately remains failed even if library later appears, pending integration/semantic review; follow-up should use contract signature only.
- Need test streaming multipart limit and timeout; exceptions raised inside ASGI receive may be transformed by parser. Verify proper 413/504 responses and request body/storage bounds.
- Need tests for verification errors, foreign evidence IDs, confirmation length and provider malformed DTOs.
- DTO list/string output bounds and memory accounting beyond photo bytes should be reviewed; current provider output sizes are not bounded.
- `read_photo` uses thread decoding; provider work has timeout and two concurrent slots. Review decode timeout/concurrency handling and safe cancellation.
- Registry entries must be independently acquired/verified, including exact quote/version/page, rights, source URL and action correspondence. Empty default registry and reviewer deny guides.
- Verification currently appends visual-only limitation but trusts provider observations; review safety/normal-operation guarantee wording before live adapter integration.
- Runtime dependency versions are pinned; lockfile, editable build and uvicorn launch are not yet verified. Starlette emits httpx TestClient deprecation warning.
- Push has not occurred; coordinator explicitly requested checkpoint before full task completion.
