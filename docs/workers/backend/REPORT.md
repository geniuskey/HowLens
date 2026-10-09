# W2 backend delivery

[DONE] Implemented a real gated OpenAI Responses multimodal REST adapter, exact allowlisted manual-registration boundary, conservative prerequisite review, observation-only before/after comparison, explicit local dotenv configuration and prepared public Visual integration boundary. All existing 38 tests remain passing, with 54 total offline tests plus configured-server health smoke, editable installation, wheel build and pip dependency checks passing. No paid call or live model/manual/visual success is claimed; first grounded analysis remains blocked by key/model/API-budget readiness and independently acquired/reviewed manual excerpts, and guides require independent physical approval.

- Preserved W1 base: `d035a285861d96025db4be728dc5255a344e44eb`.
- W2 code SHA: `28b1e387a47c7055db5704da51e46d9e6b60ae63`.
- Branch: `ljyonefineday/feat-backend-foundation`; no main merge/ff bootstrap/credential change.
- Current lifecycle: task `task_69c26e09a2dc`, dispatch `ctx_4fc700a02b03`.
- Final docs/remote SHA is reported in the current worker_done; code SHA above identifies validated implementation.

## W2 actual evidence

`git ls-remote --heads origin` identified Visual `feat-visual-foundation` at `ed04a43f87d542622527efcbf383151c702d796e`; `git fetch origin feat-visual-foundation` succeeded. Read only the specifically authorized published `backend/visual/service.py` and `splitter.py` with `git show`; did not merge, copy or edit Visual files. Initial `git show ed04a43` failed because object was not local, then fetch resolved it.

Official OpenAI vision/structured-output/migration docs were browsed/opened before API implementation. The very large general Responses reference could not be fetched (web content-size limit); the fetched guides establish the actual wire format used. Runtime uses bounded async httpx REST, no SDK/tool/autoretry fallback, explicit configurable model, store:false and strict typed response schema; unsupported model/schema/auth/rate failures remain honest 503 until a budget-authorized live test is run.

`backend/.venv/bin/pip install -e 'backend[test]'` succeeded, adding python-dotenv 1.2.4; `backend/.venv/bin/python -m pytest backend/tests -q`: **54 passed, 1 warning in 0.51s**. `backend/.venv/bin/pip check`: no broken requirements; `git diff --check`: clean. `backend/.venv/bin/pip wheel --no-deps --wheel-dir /tmp/howlens-backend-w2-wheel ./backend` built howlens_backend-0.1.0 successfully. Python subprocess launched `uvicorn howlens.config:configured_app --factory --host 0.0.0.0 --port <ephemeral port> --no-access-log`, with paid flag explicitly blank; actual localhost health JSON matched status=ok/mode=live, PASS, process stopped.

New tests use only synthetic offline HTTP/transport doubles: strict request shape/image input/untrusted-context placement, key-without-budget zero calls, incomplete/refusal/HTTP/malformed/oversized response rejection, timeout mapping, device/mode mismatch, physical prerequisite unknown conversion and blocked guide, original/after comparison and stored step/evidence restriction, exact allowlisted source/quote validation, local dotenv secret-free diagnostics, and Visual budget/approval/semantic-review gates. These tests do not prove model resistance to all prompt injection or live semantic correctness; independent server gates prevent procedure approval even if the model ignores its prompt.

Local `backend/.env` prepared with blank fields only if absent, ignored and chmod 0600; existing content preserved. `backend/.env.example` blank template is intentionally ignored per Task. Orca editor opened backend/.env without printing its content. Latest sanitized configuration check: key_present=false, model_configured=false, paid_calls_enabled=false, manuals=0, independent physical reviewer=false, visual integration=false. No secret is in diagnostics/logs/commits, and no key is shared across PCs. Configuration and exact LAN serve instructions were sent to Coordinator and are in RUNBOOK; observed address 10.102.72.28, external LAN reachability untested.

## W2 blockers and next integration

1. Human must enter local API key and explicit compatible model; Coordinator must confirm API balance/budget/key readiness before paid switch is enabled. Codex credits were not treated as API credits; no paid request made.
2. No actual original manufacturer PDF has been acquired/reviewed. Registry defaults empty; claimed evidence is rejected unless exact device/document/version/page/quote/source/actions match trusted catalog. The catalog allows independently reviewed registration, never model/client URL registration.
3. Required physical conditions are forced unknown by the live adapter, and independent reviewer returns no approval. Thus live guide cannot be promoted solely by model satisfaction claims; independent device/conditions/hazard/action review needs a later trusted source.
4. Visual library not merged. Prepared public wrapper checks stored approval and budget plus semantic reviewer; HTTP completion/assets remain intentionally unavailable, with failed jobs preserving analysis text. Semantic review cannot be replaced by splitting/decoding.
5. W1 known thread-decode cancellation/global concurrency and test-client deprecation limits still apply. Observational provider language is constrained by policy and typed DTO/limitations, but live language/semantic quality remains untested.

## W2 changed files

`backend/.gitignore`, `backend/pyproject.toml`, `backend/howlens/safety.py`, new `backend/howlens/openai_provider.py`, `manual_catalog.py`, `config.py`, `visual_boundary.py`, new `backend/tests/test_openai_provider.py`, and own RUNBOOK/REPORT. Ignored local `.env` and `.env.example` are not staged. No app/evaluation/common contract or backend/visual edits.

Coordinator follow-up verified owner-account write access restored and explicitly authorized pushing preserved W1/W2 at the clean checkpoint. No repeated 403 retry or alternate credentials will be used; remote outcome is recorded after the authorized push.

---

# W1-BACKEND completion

[BLOCKER] Required branch push failed with owner-account HTTP 403; validated implementation is complete locally. Preserved the existing API checkpoint and completed upload/provider-output bounds, storage accounting and verification edge validation. All 38 synthetic offline TestClient tests pass, editable package installation and wheel build succeed, and an actual Uvicorn HTTP health smoke test passes. Live provider/manual acquisition and Visual integration remain subsequent tasks; the unconfigured runtime returns 503 and never fabricates a guide or evidence.

## Branch and provenance

- Branch: `ljyonefineday/feat-backend-foundation`.
- Base documentation `75c8eac` verified as ancestor (exit 0).
- Preserved checkpoint: `b784455`; completed code: `07d8e8712f67a7cd5d32db4d8025694fc18098e8`.
- Final documentation commit is a descendant of that code SHA; exact final local SHA is included in worker_done and obtainable with `git rev-parse HEAD`.
- Current Dispatch: task `task_2f454c0c4e2c`, dispatch `ctx_eaec09bca2f2`.
- `git fetch origin main` succeeded and fetched origin/main `717514d`; `git merge --ff-only origin/main` failed because the checkpoint branch diverged. Working tree was clean, existing commits were preserved, and Coordinator explicitly approved continuation without merge/reset/rebase/history rewrite through the live `ask` reply. No main merge occurred.

## Implementation

FastAPI v0.1 health, multipart analyses, visual job create/query and verification endpoints; strict contract DTOs; private original-photo storage; bounded uploads/JPEG/PNG decoding; bounded memory records; timeout and generic retryable upstream errors; trusted manual registry and independent reviewer boundaries; fail-closed unsafe-step sanitization. Guide admission requires trusted matching device/manual/action evidence, unique IDs, supported steps, independent hazard and precondition approval, live mode and 1–9 steps. Non-guide steps are always empty, invalid evidence is removed, unknown IDs return 404 and non-guide visual/verification return 409.

Continuation adds DTO string/list/UTF-8 byte limits and instance revalidation, counts serialized analysis bytes in storage capacity, validates output again after sanitization/verification limitations, bounds decoding response time, imposes upload receive deadline, and applies question length after whitespace trimming. Missing Visual remains an explicit failed job with a maximum of two attempts and stored text survives; no backend/visual files were read or edited.

## Actual commands/results

- `backend/.venv/bin/pip install -e 'backend[test]'`: succeeded, package built and installed.
- Initial preserved suite: `backend/.venv/bin/python -m pytest backend/tests -q`: 21 passed, 1 warning in 0.34s.
- New negative tests before output-bound fix: 4 failed, 30 passed in 0.46s; oversized string/array/aggregate analysis and verification responses incorrectly returned 200. Fixed and reran: 34 passed in 0.40s.
- Final root suite after boundary tests: `backend/.venv/bin/python -m pytest backend/tests -q`: 38 passed, 1 warning in 0.41s; after upload deadline change, root `python -m pytest -q`: 38 passed in 0.39s.
- Final project-directory `.venv/bin/python -m pytest -q`: 38 passed, 1 warning in 0.37s.
- `backend/.venv/bin/pip check`: No broken requirements found.
- `backend/.venv/bin/pip wheel --no-deps --wheel-dir /tmp/howlens-backend-w1-wheel ./backend`: built howlens_backend-0.1.0 wheel successfully. An earlier command incorrectly used bare `backend` and built an unrelated PyPI package; that result is excluded from validation and no unrelated package was installed into the runtime.
- Python subprocess smoke launching `backend/.venv/bin/uvicorn howlens.main:app --host 127.0.0.1 --port <dynamically selected port> --no-access-log`, fetching `/health` with urllib and terminating server: PASS, exact status/mode asserted.
- `git diff --check`: passed. Dependency and official framework reference checks completed.

Coverage: health, unconfigured/error/timeout providers, invalid image/MIME/file/pixel limits, maximum file accepted, trimmed question and confirmation bounds, malformed/oversized/mutated provider outputs, private upstream error suppression, unknown IDs, fabricated/unreviewed evidence/actions/preconditions, unsafe/non-live/non-guide suppression, verification foreign evidence/identity/mode/error/timeout, absent Visual/retry/text retention, private exact original bytes, FIFO eviction/jobs and serialized-output storage capacity, streaming body overflow and receive timeout. All provider/manual fixtures are synthetic; no real multimodal inference, manual verification, equipment safety or storyboard was tested.

## Files changed

Checkpoint introduced `backend/.gitignore`, `backend/pyproject.toml`, `backend/howlens/{__init__,models,provider,safety,store,main}.py`, `backend/tests/test_api.py`, and the backend RUNBOOK/REPORT. This Dispatch modified `backend/howlens/main.py`, `models.py`, `store.py`, `backend/tests/test_api.py`, `docs/workers/backend/RUNBOOK.md` and this report. No app/evaluation/visual/shared-contract edits.

## Remaining dependencies and limits

No public manufacturer PDF/source was acquired or registered; no live adapter/reviewer configured; actual equipment/task evidence must be verified before guides. Visual library/semantic panel review/asset storage need their own integration task and use only the public signatures. Threaded Pillow cancellation is cooperative: response timeout cannot terminate an active decode thread; production hard isolation and global concurrency controls are not established. Memory limit counts photo + serialized analysis, with bounded job/object overhead separately. Verification adds explicit limitations but a future adapter still needs conservative observation-language review. Starlette's httpx TestClient deprecation warning remains; no tests are skipped or suppressed. No token/key/private image content was logged or committed.

## Push blocker

`git push -u origin ljyonefineday/feat-backend-foundation` at documentation SHA `98419e36cffbf65aca017662d669b80d7baa4974` failed with HTTP 403: `Permission to geniuskey/HowLens.git denied to ljyonefineday`. No push success is claimed; all validated commits remain local with a clean working tree before this report update. Escalation and a blocking question were sent through the current Orca Dispatch requesting owner-account access or an authorized fork remote; no alternate account or token was used.

Coordinator instructed settlement as failed delivery because repository write permission is unavailable across owners; no further push retry or credential/account change is authorized. Local implementation and validation are complete, and the final local HEAD is reported in worker_done.
