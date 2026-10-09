# W3 Backend release fix

[DONE] Fixed setuptools discovery to include the public Visual package and pytest defaults to collect both suites using importlib mode. Disposable combined source now collects/passes75tests with normal documented pytest, and a freshly installed non-editable wheel imports Visual outside the source tree and passes the public startup/mapper/split boundary probe. Dedicated live server/key/env were untouched, no paid calls or main merge occurred, and guide/HTTP Visual readiness limits remain unchanged.

## Input / ownership / commit

- Worker branch: `ljyonefineday/feat-backend-foundation`, base `aba3e9e0fc81597228da3349511a16688a6de3ae`.
- Fix code SHA: **76fa5970a0b1609bb05c6530d7199bb87b4f3031**; final docs/pushed SHA sent in worker_done.
- Independent report read from `Runixs/eval-foundation` **e558424**, exact path docs/workers/evaluation/W4-RELEASE-AUDIT.md after explicit fetch.
- Visual input **63dd0f80155b120fcd9bd07f7fb3bf6cf75773dc**, read-only public package extracted to disposable external snapshot.
- Lifecycle task `task_4f2c7aa6aa8c`, dispatch `ctx_a78d1be91800`.
- Only backend/pyproject.toml, backend/tests/installed_wheel_probe.py and own RUNBOOK/REPORT changed; no backend/visual modifications or root/common/Evaluation/App changes.

## Changes

Setuptools includes `howlens*` and `visual*` while excluding `visual.tests*`; existing Pillow12.3.0/httpx0.28.1 pins already satisfy the published Visual dependency ranges, confirmed by fresh installed-wheel pip check. Manual catalog remains package data. A combined source release thus bundles the actual Visual code; standalone Backend checkout remains usable and packages only what is present, with absent Visual still failing closed.

Pytest testpaths are tests + visual/tests, configured importlib mode prevents duplicate test_openai_provider module names, and helper paths support existing Backend fixture imports. From backend cwd, normal `python -m pytest -q` collects combined75 without CLI import-mode/test-path workarounds; standalone still58. No other-owner tests were renamed or edited.

New Backend-owned installed_wheel_probe.py runs under fresh installed python -I from external cwd, asserts all package imports resolve inside that venv, checks four Visual wheel modules/catalog and excludes Visual tests/env files, then tests real published startup/mapper/splitter through the Backend boundary with synthetic provider/reviewer. TCP is blocked and no credentials inherited; this proves packaging/public geometry wiring, not live generation or semantic safety approval.

## Actual evidence

Disposable snapshot `/var/folders/zb/_bbtpp4s4zb5_760tsn1y9dh0000gn/T/howlens-w3-release-fix-oqoswz4p`: Backendaba3e9e public howlens/tests/config plus candidate pyproject (identical configuration committed76fa597), Visual63dd0f8 package. Git archive selected tracked public paths only; no key/env copy. Subprocess env excluded OPENAI_API_KEY/paid flags; pytest TCP was blocked through external sitecustomize. Existing server PID74518 and terminal term_bde18df8-bc41-4a7f-bd14-2742207763a7 were not stopped/restarted/modified, and backend/.env was not accessed or changed in this Task.

- Normal snapshot `python -m pytest --collect-only -q`, cwd snapshot/backend: **75 collected in0.26s**, exit0.
- Normal snapshot `python -m pytest -q`, same cwd/config: **75 passed,14 subtests passed,10 warnings in0.62s**, exit0. Starlette TestClient1 + Pillow getdata9 deprecation warnings unsuppressed.
- Standalone project-directory `.venv/bin/python -m pytest -q`: **58 passed,1 warning in0.54s**. Also explicit Backend paths passed58 in0.56s.
- `python -m pip wheel --no-deps <snapshot>/backend -w <snapshot>/wheel`: built howlens_backend-0.1.0-py3-none-any.whl successfully, SHA256 **40d8b06b359d39ffade2627bea18930b95d162a2f1d7db4d6bf2edb0289bfcc4**.
- Created separate snapshot/installed venv and actual `pip install <wheel>` (non-editable) succeeded. From snapshot/home, isolated `installed/bin/python -I <Backend owned installed_wheel_probe.py> <installed venv> <wheel>`: PASS wheel members, out-of-source installed imports, manual catalog, public startup/mapper/split boundary;9panels; paid_calls0.
- Actual wheel members: visual/__init__.py, openai_provider.py, service.py, splitter.py; no visual/tests members. Fresh installed `pip check`: no broken requirements.
- `git diff --check`: clean.
- Initial test orchestration mistakenly resolved the venv interpreter symlink to base Python and failed No module named pytest; corrected to preserve the venv executable path before the successful runs above. That failed attempt is not PASS.

Raw local transcripts: snapshot/{collection,tests,wheel-build,wheel-install,installed-probe}.txt. Reproduction and probe commands in RUNBOOK. Probe source is durable and Backend-owned; complete PDFs/private images/keys were never copied into the release snapshot.

## Remaining limits

Visual package must actually be supplied in the authorized combined source release: this worker branch intentionally does not merge/add/edit other-owner Visual files. Packaging fixes inclusion, not code acquisition or main merge approval. Existing runtime still returns failed HTTP Visual jobs and has no completed asset serving; guide/physical review remains unreachable without the previously proposed/approved session contract and actual equipment inputs. Wrapper30s vs image adapter120s policy and splitter worker/cancellation bounds await HTTP activation work; this Task does not certify them. No paid AI/real guide/image semantics/Android/public deployment tests were performed. Existing dedicated controlled LAN service is left running unchanged under prior Backend/Coordinator ownership.

---

# W2 Backend delivery

[DONE] Implemented real server-side Responses analysis/verification, conservative physical/evidence gates, independently checked descriptive manual registry, prepared public Visual integration, controlled paid-call accounting and cancellation-safe bounded decoding. All58 offline tests pass, the independent cancellation probe passes, one authorized live synthetic analysis returned non-guide/steps0, and the controlled LAN server is running for App integration. Genuine guide/verification/Visual success remains pending actual equipment/operator approval and scoped contract decision; no fake physical approval or live image success is claimed.

## Branch and commits

- Branch `ljyonefineday/feat-backend-foundation`; W1 base `d035a285861d96025db4be728dc5255a344e44eb` preserved.
- Adapter code `28b1e387a47c7055db5704da51e46d9e6b60ae63`.
- Verified descriptive catalog/one-smoke tooling `f6809ea77fe6444600bd6e419f59e495f1be2cbf`.
- Final validated code, decode cancellation fix and persistent paid cap: **`ac7e618b8cf6c0af63f5689beecb70524e2ac17c`**.
- Task `task_69c26e09a2dc`, dispatch `ctx_4fc700a02b03`; final documentation/remote SHA reported in worker_done.
- Coordinator later verified own-account write access restored and authorized push. First W1/W2 push succeeded at `1081de70191cdb35bbaeca5c7291edf1b60da02d`, without account/credential change; final push follows this report. No main merge, ff bootstrap, reset/rebase/force push, other-owner edit or public deployment.

## Actual implementation/readiness

Real bounded async REST `POST /v1/responses`, env OPENAI_API_KEY, explicit model, JPEG/PNG base64 image inputs, strict DTO JSON schemas, store:false, no tools/redirects/autoretries/private exception logging. Refusal/incomplete/malformed/oversized/device-mode mismatches fail; timeout504/other upstream503. Required model prerequisites always become unknown; independent conservative reviewer issues no approval. Verification compares stored original and new image with stored guide, restricts evidence IDs and states observational limitations; actual live verification has not been run because no genuine guide exists.

Key/model now present and configured locally, sanitized booleans only; model gpt-4.1-mini. Coordinator explicitly confirmed user USD50 API budget, first one paid smoke, then at most20 subsequent analysis/verification attempts for controlled integration. Persistent cap is reserved before network, failures consume attempts, no retries; ignored owner-only ledger stores only numeric usage/status/model. Health remains free. No billing balance was verified. Other PCs call this backend rather than receiving/copying the key.

Current server: Orca terminal `term_bde18df8-bc41-4a7f-bd14-2742207763a7`, PID74518, 0.0.0.0:8000. Actual local and own-LAN-interface health checks PASS at `127.0.0.1:8000` and `10.102.72.28:8000`. Remote Android/LAN reachability remains untested. Backend/Coordinator owns stop/restart; exact command and Ctrl+C/PID instruction in RUNBOOK. Server intentionally remains running for the authorized App integration; dispatched terminal is distinct.

Guide is explicitly **unreachable in current live configuration**: the registered facts have no approved procedural action, model physical conditions stay unknown and reviewer never asserts them satisfied. Proposal sent but not implemented: optional observation_session_id referencing server-owned reviewed TTL field conditions plus independently checked no-contact R750 action supported by A11 PDF245; actual equipment/operator confirmation and source-aware prerequisite application needed. No silent contract additions or blanket approval via question/checkbox/env booleans.

## Actual validation

- Editable `pip install -e 'backend[test]'`: succeeded; runtime httpx0.28.1/python-dotenv1.2.4.
- Original38 tests retained; initial W2 suite54 passed in0.51s; manual/usage tests56; paid-ledger regression57.
- Cancellation regression before fix: **FAIL peak4<=2**, reproduced exactly. After bounded actual-worker completion fix, full suite **58 passed,1 warning in0.58s** (Starlette httpx TestClient deprecation, unsuppressed).
- Same unmodified published W3 probe8e69072, loaded into an external temporary snapshot with its minimal imports, against current backend: exit0, decode_cancel_peak2, verification schema passed, mock visual/verification409, three visual responses202 with retry reuse. First snapshot execution lacked evaluation.run import and failed before tests; corrected by including that dependency, no PASS claimed for first attempt. Probe disables external network; fixtures synthetic.
- `pip check`: no broken requirements; `git diff --check` clean.
- Final wheel built with catalog package data: howlens_backend-0.1.0, SHA256 `5aa44554dcbd526fb748f7f0a27244d224e3c5232be7b4ebf773c8fe02b1ca30`.
- Env-loader factory HTTP health smoke succeeded on ephemeral all-interface port and stopped; dedicated port8000 server then started, both own-host addresses verified200.
- Published Visual63dd0f80155b120fcd9bd07f7fb3bf6cf75773dc public API tested in external temporary snapshot: startup from_env configuration with synthetic key, synthetic provider generation, actual public scene_step_ids/splitter and backend wrapper gave exactly9panels/IDs. No real image API call or semantic quality validation.

## One paid live smoke

After explicit Coordinator budget/key-ready authorization, executed exactly once:
`backend/.venv/bin/python -m howlens.live_smoke --authorized-one-call --model gpt-4.1-mini`.

Synthetic64x64 gray image; upstream HTTP200, backend200, mode live, decision needs_more_information, exact registered evidence count2, steps0. Provider usage input963/output337/total1300; published gpt-4.1-mini input0.40/output1.60 USD per1M tokens yields conservative estimate **USD0.0009244** without cached discount. [Official model/pricing page](https://developers.openai.com/api/docs/models/gpt-4.1-mini) opened before model selection. This is actual API/schema/account compatibility and fail-closed response evidence, not real equipment-photo identification/relevance or safety/guide success. No repeated paid retry or paid image generation by this Worker. Server's subsequent20-attempt integration allowance is separate and may be consumed by App calls after this checkpoint.

## Independently checked manuals

Authorized research source `Runixs/hackathon-research`5c0b830; read only specified SOURCE_MANIFEST.json and REPORT plus original public PDFs. Direct bounded HTTPS downloads matched original/resolved allowlisted URL, byte size and complete SHA-256 for all3 PDFs: Dell55,547,654bytes, UR22,934,094bytes, UPS614,727bytes. macOS PDFKit independently confirmed page counts255/362/20 and five exact excerpts after whitespace normalization: Dell PDF11/245, UR329, UPS10 twice (printed8). Versions/hash/URL/excerpt/page/review metadata preserved in `backend/howlens/manual_sources.json`.

These are **5 descriptive excerpts, 0 approved actions**, exact tuple/evidence validation; no claims that document acquisition establishes real equipment/SKU/firmware match or task safety. Full PDFs ignored locally, not committed/redistributed. Manufacturer copyright retained, brief attributed quotations only; no reuse license claimed. Missing registry file returns empty non-guide path. API/model text cannot register sources.

## Files/limits remaining

Changed own backend/.gitignore/pyproject, main/safety/config/openai_provider/manual_catalog/manual_sources/live_smoke/visual_boundary, test_api/test_openai_provider, own RUNBOOK/REPORT. Local key and blank .env.example ignored; key file0600, editor opened and existing key content preserved. No backend/visual/app/evaluation/common-contract edits. Public Visual code only read from exact published refs and copied to external test snapshots with authorization.

Prepared Visual startup/mapping boundary is not HTTP activation; actual library import awaits authorized integration branch, genuinely approved stored guide, image budget, semantic review and bounded assets. Error/absence still yields failed jobs preserving text. Thread decoding cannot be forcibly killed, but cancellation no longer bypasses max2 actual workers; global deployment isolation remains future work. Provider prompts constrain observation wording but real field-image/semantic quality and verification are not yet evaluated. App/remote reachability pending; current live non-guide analysis ready within cap. Billing/account balance never represented as independently verified.

W1 detailed checkpoint history remains in commit d035a285 and prior report revisions; W2 server/manual/budget status above supersedes that historical no-key/no-manual/push403 state.
