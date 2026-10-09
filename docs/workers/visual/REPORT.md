# W1-VISUAL report

[DONE] Implemented the async guarded storyboard library, injectable provider boundary,
approved-step prompt and uniform row-major PNG splitter. Eight standalone synthetic
tests passed; no live provider or semantic image QA was run. Backend wiring and a
documented live provider adapter remain follow-up work.

- Branch: `feat-visual-foundation`; clean starting HEAD/base: `75c8eac`.
- Changes: `backend/visual/{__init__.py,service.py,splitter.py,requirements.txt,.gitignore,tests/test_visual.py}`
  and `docs/workers/visual/{RUNBOOK.md,REPORT.md}` only.
- Actual validation: Windows PowerShell, Python 3.9, Pillow 11.3.0;
  `python -m unittest discover -s backend/visual/tests -v` with backend PYTHONPATH:
  **8 tests passed** (0.255 seconds on initial run).
- Verified nine synthetic color panels, every pixel's color, row-major order,
  PNG format, and uniform 40x32 dimensions from a rectangular 120x96 grid.
- Rejected corruption, truncation, tiny/non-divisible grids, JPEG, non-bytes,
  empty and oversized payloads. Invalid provider bytes fail explicitly.
- Forbidden decision/mode combinations and unsafe steps/preconditions make zero
  provider calls. Unconfigured live provider raises an explicit error; provider
  timeout propagates. Valid offline double returns PNG, without input mutation;
  one approved step repeats in nine cells and visual hints are excluded.
- `git diff --check` passed. No parent backend files, Android, shared contracts,
  or other owners' code changed; no main merge.
- Environment issue resolved locally: `python` resolves to a Windows Store alias;
  used `py -3` and an ignored visual-local virtual environment. Pillow was absent
  initially and installed only into that environment.
- Limitations: no network image generation, no live provider options selected,
  no prompt effectiveness/geometry/action semantic validation, no divider detection.
  Provider readiness is **unconfigured**, never reported as live success. Backend
  still owns source validation, approved stored input, job state, timeouts/HTTP
  mapping, text preservation and final semantic scene-to-step acceptance.

## Delivery checkpoint

Implementation and test acceptance are complete. The initial commit/push delivery was blocked.
`git commit` failed with `Author identity unknown`; `git push` failed with
`Cannot prompt because user interactivity has been disabled` and
`unable to get password from user`. No identity was fabricated, no account was
borrowed, and no global configuration was changed. Coordinator instructed preserving
staged work and reporting failed delivery (not test failure) for a follow-up dispatch.

Worktree: `C:\Users\luj01\orca\workspaces\HowLens\feat-visual-foundation`.
At that checkpoint, branch was `feat-visual-foundation` with unchanged HEAD SHA:
`75c8eacf79e22b0f89a611b9df848661a9c9a81b`; no new commit or successful push.
All eight listed owned files were staged; virtual environment and caches are ignored.
Final rerun: eight tests passed in 0.118 seconds; `git diff --cached --check` passed.
Current permissions already are danger-full-access/never; no permission restart was
needed for implementation.

## User-authorized delivery resumed

The user supplied their author identity and completed GitHub authentication, then
directly requested continuation of the preserved work. Repository-local author
configuration and remote read/push dry-run succeeded; the prior identity/authentication
blockers are resolved. No settled Task/Dispatch lifecycle IDs are reused for this
user-owned continuation. All eight tests passed again in 0.313 seconds on 2026-10-09.
Delivery is on `feat-visual-foundation`; use the commit containing this report
(`git log -1 --format=%H -- docs/workers/visual/REPORT.md`) for its exact SHA.

## W2-VISUAL — offline provider delivery (2026-10-09)

[DONE] Added the official Images API adapter, deterministic scene labels and a
timestamped lightweight synthetic panel presentation. Actual offline validation:
**17 tests passed** in 0.365 seconds on Python 3.9/Pillow 11.3.0/httpx 0.28.1;
`git diff --check` passed. Live generation and semantic image QA remain **pending**.

- Baseline/branch preserved: `ed04a43`, `feat-visual-foundation`; no main merge.
- Changed files: `backend/visual/openai_provider.py`, `service.py`,
  `requirements.txt`, `tests/test_openai_provider.py`;
  `docs/workers/visual/RUNBOOK.md`, `REPORT.md`, `mockups/README.md`,
  `mockups/make_panel_mockup.py`,
  `mockups/20261009_1145_panel_step_source_labels.png`.
- Public helper: `scene_step_ids(analysis: dict) -> list[str]`, nine intended
  row-major IDs using `steps[index * count // 9]`; contract signature immediately
  sent to Coordinator. Tests compare helper and prompt for every count 1-9.
- Public adapter: `OpenAIStoryboardProvider.from_env()` then
  `configure_provider(adapter)` at Backend startup. Uses server-only
  `OPENAI_API_KEY`; default model `gpt-image-1.5`, size `1024x1024`, quality `low`,
  timeout 120s. Full env allowlists/signatures/exception mapping in RUNBOOK.
- Opened official current OpenAI Create image API reference and image-generation
  guide before implementation (links in RUNBOOK). Fixed HTTPS Images endpoint,
  PNG/base64/opaque/auto moderation/n=1; legacy `response_format` omitted.
- Bounded timeout 1-300s, streamed HTTP body 15 MiB, PNG 10 MiB, 20MP and exact
  requested source dimensions. Whole-grid resize to divisible-by-three dimensions
  is documented (1024 -> 1023); source geometry validation does not establish
  drawn cell boundaries or semantic acceptance. No automatic retries or URL fetches.
- HTTP doubles verify exact request contract, source validation, nine 341x341
  panels, forbidden-input zero HTTP calls, sanitized HTTP/network/timeout errors,
  no retry, total deadline cancellation, invalid response/base64/PNG/dimensions,
  response limit and missing/invalid settings. Existing W1 tests remain passing.
- Synthetic mockup shows intended panel/step/source/document labels, visually
  inspected for clipping and clear synthetic status. No invented equipment photo,
  actual manufacturer evidence, or paid call is represented.
- No key was copied to this PC or printed; no external generation was invoked.
  Backend live integration needs Coordinator budget/key-ready authorization and
  real semantic acceptance review; validated text must survive visual failure.
- Lifecycle recovery: initial task preamble absent, execution-host dispatch-show
  returned Task not found, then official preamble arrived with literal `worker`
  sender which returned stable_pane_required. Coordinator explicitly corrected
  sender to this live terminal; heartbeat receipt `950f1c13-a8af-42ef-bfd1-5401f5fb5789`
  was accepted for the exact new task/dispatch. No duplicate editor/task was created.

This report is delivered in the W2 commit; resolve its exact SHA with
`git log -1 --format=%H -- docs/workers/visual/REPORT.md`. Coordinator receives the
literal final branch/SHA, push receipt, changed paths and integration evidence.

## W3-VISUAL-BENCHMARK-RUNNER (2026-10-09)

[DONE] Added a product-independent opt-in sandbox runner, bounded direct HTTP client,
schema-compatible results/sidecar, exact protocol preparation commands and a quick
timestamped offline reviewer mockup. **Actual paid/AI calls on this PC: 0**.

- Baseline `63dd0f8`, branch `feat-visual-foundation`; owned paths only, no main merge.
- Read only the explicitly permitted Evaluation protocol/cases/schema/README at
  `0234159c2a5b4cc9443e79de30e90ef5586661d2`, after Coordinator clarified
  `Runixs/eval-foundation` is a branch of this repository, not another repository.
  No Evaluation files or contracts were edited. Canonical protocol hashes are pinned.
- CLI/schema were coordinated early; schema v1 disallows extra hash/budget fields,
  so `results.jsonl` stays exact and deterministic `artifacts.json` carries them.
- Models fixed to `gpt-image-1.5`, `gpt-image-1-mini`; identical three synthetic
  prompts interleaved, 1024 square/low, concurrency 1/retry 0. Default dry-run
  reads no key and performs no requests. Execute requires explicit flag, real
  nonsecret approval ID, positive reservation budget and integer max_calls 1-6.
- Raw provider bytes/hashes and separate 1023 normalized PNG/hashes are saved.
  Monotonic latency, UTC start, usage, request/status/error/timeout/cancel metadata
  retained; snapshot unknown null, actual cost/billing receipt null. No token+output
  proxy double counting or unsupported price estimate; reservations are not billing.
- Actual tests: Python 3.9, Pillow 11.3.0, httpx 0.28.1, jsonschema 4.25.1;
  full suite **26 tests passed in 1.506s**, including the 17 W1/W2 regressions.
  New fakeHTTP tests cover exact settings/raw/hash, zero-call/no-env dryrun, original
  protocol.plan equality, JSON Schema validity, original protocol.score pending human
  review, summary excluding offline, timeout/cancel/error/invalid outputs/no retries,
  budget/call limits, arbitrary model rejection, changed protocol and overwrite guards.
- Initial new test run: 24 passed/one error, caused by Windows cp949 default when
  reading UTF-8 results in a test. Corrected the test to explicit UTF-8; final run
  all passed. No product-provider test behavior changed.
- Final rerun after preserving numeric usage on invalid-output failures:
  **26 tests passed in 1.507s**; Windows protocol preparation script also executed
  successfully and `git diff --check` passed.
- Actual CLI: `python -m visual.benchmark --protocol-dir backend/visual/.venv/eval-protocol
  --output-dir docs/workers/visual/benchmark-preview/w3-dry-run` with backend PYTHONPATH.
  Output: six records, attempted_calls 0, source not_run, actual_cost null; committed
  dry-run evidence is not measured latency or generated equipment imagery.
- Mockup: `mockups/20261009_1222_image_model_reviewer_sheet.png`, visually inspected.
  Synthetic nine-cell placeholders, cost unknown/latency NOT RUN, topology/step/grid/
  arrows-labels/no-new-action and independent reviewer fields; no winner or score.
- Changed files: `backend/visual/benchmark.py`, `benchmark_http.py`,
  `requirements.txt`, `tests/test_benchmark.py`; Visual `RUNBOOK.md`, `REPORT.md`,
  `prepare_benchmark_protocol.ps1`, `benchmark-preview/w3-dry-run/{results.jsonl,artifacts.json}`,
  `mockups/README.md`, `mockups/make_benchmark_mockup.py`, timestamped PNG above.
- Remaining: Backend owner runs only after explicit budget/key-ready authorization,
  then Evaluation reviews actual images/usage/billing, semantics/topology and fit.
  No credentials were requested/copied, no accounts switched, no paid execution here.

Windows dry-run and future Backend-owner commands are in RUNBOOK. Final commit SHA
is sent literally in the completion receipt and can be resolved from this report's
commit (`git log -1 --format=%H -- docs/workers/visual/REPORT.md`).

## W3 review fixes — budget precision and cancellation metadata

[DONE] Addressed both independent-review P2 findings against immutable `5dcf6f2`.
Budget/reservation values below supported eight-decimal precision now fail before
environment access or HTTP; supported amounts are converted exactly to integer
1e-8 USD units. Removed the absolute comparison epsilon and cumulative round8
accounting, eliminating the subprecision six-attempt/zero-reservation bypass.
Cancellation remains a cancellation, now carrying already-observed sanitized
request ID/status/usage from the sandbox client into the attempted result.

- Actual validation: **30 tests passed in 2.191s**, comprising all 26 previous
  tests and four targeted regressions. Subprecision 1e-10 cases reject with zero
  calls; minimum supported budget/reservation 1e-8 allows exactly one; 0.3/0.1
  allows exactly three. Existing 1.5/1 budget regression still allows one call.
- HTTP-double stream yields a partial body after HTTP 200 and `req_cancelled`,
  then raises CancelledError: one attempt only, cancelled result retains both
  known header fields, no private exception message saved, remaining rows have
  null headers; every result remains schema-valid. No automatic retries.
- Actual fresh CLI dry-run saved six schema-valid not_run records to
  `benchmark-preview/w3-review-fixes-dry-run/`, attempted_calls 0, actual cost null.
  No paid/network AI calls or environment-key reads were performed in this task.
- Main product provider/service and their behavior/tests are unchanged.
  Existing synthetic comparison mockup retained: reviewer-visible information
  did not change, so no redundant image rendering or heavy generation occurred.
- Changed owned files: `backend/visual/benchmark.py`, `benchmark_http.py`,
  `tests/test_benchmark.py`; Visual `RUNBOOK.md`, `REPORT.md`, plus the two fresh
  dry-run evidence files. `git diff --check` passed.
- Coordinator's additional mkdir-only collection instruction completed:
  `C:/2026_DSDN/HowLens/docs/assets/manuals/visual/` and active-worktree
  `C:/Users/luj01/orca/workspaces/HowLens/feat-visual-foundation/docs/assets/manuals/visual/`.
  No files overwritten, no checkout/merge/reset; collected PDFs/source.txt are
  not automatically approved evidence. These empty directories add no tracked edits.
- Branch preserved: `feat-visual-foundation`; completion receipt records exact
  committed/pushed SHA. Real generation, billing and semantic review remain pending.
