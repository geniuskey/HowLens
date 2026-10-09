# [DONE] Additive product discovery implementation

Implemented `POST /product-discoveries` with a printed-label-first provider, bounded
web search, actual source metadata, hash-only response caching and cancellation.
The full backend suite passes 118 offline tests; no paid discovery or real-device
identification test has been run. Coordinator integration, a capped live provider
check and serial human review remain outside this worker's completed implementation.

## Scope and references

- Task `task_e6aa78618e38`, dispatch `ctx_34b6d97bd407`.
- Branch `ljyonefineday/feat-backend-foundation`; starting SHA `d977170`.
- Contract accepted through coordinator relay `relay_e13074c9281b`; exact request,
  response, error, privacy and budget details: [API_PROPOSAL.md](API_PROPOSAL.md).
- Integration/testing instructions: [RUNBOOK.md](RUNBOOK.md).
- No shared contracts, app code, visual-owned code or device-QA documentation edited.
- Live server PID 74518 was not restarted, signalled or reconfigured.
- Paid check deferred explicitly through coordinator reply on 2026-10-09; no paid
  discovery attempt was made, and no live PASS is claimed.

## Modified files

- `backend/howlens/discovery_models.py`: bounded strict public DTOs, source URL/time
  validation, required candidate/source fields and no guide extensions.
- `backend/howlens/discovery.py`: at most two calls, literal printed identity gates,
  required single web search, manufacturer/model source matching, actual citations,
  uncertain/empty-search statuses, timeout/concurrency/cache limits.
- `backend/howlens/discovery_router.py`: additive multipart route, v0.1 upload reuse,
  standard redacted errors and downstream/upstream cancellation.
- `backend/howlens/main.py`: additive route hook and injectable discovery service.
- `backend/howlens/config.py`: discovery uses the existing configured adapter object.
- `backend/howlens/openai_provider.py`: optional bounded web-search request and
  source-envelope return; shares existing key, authorization, ledger and attempt cap;
  does not present the old text-only cost estimate as covering search fees.
- `backend/tests/test_discovery.py`, `backend/tests/test_discovery_api.py`: synthetic
  photo/fake HTTP behavior tests; no paid or physical-device assertions.
- This discovery documentation directory.

## Verification actually run

2026-10-09, backend `.venv` (Python 3.14.6):

```text
.venv/bin/python -m pytest -q
118 passed, 1 warning in 0.82s
git diff --check
exit 0, no output
```

The warning is the existing Starlette TestClient/httpx deprecation; no dependency
change was needed. The suite includes existing backend API/provider/safety/visual-boundary tests and
60 discovery tests. Synthetic examples cover exact labels, unknown/ambiguous labels,
model variants, manufacturer collisions, empty results, absent/weak/invented sources,
private/credentialed URLs, prompt injection/procedural/confidence summaries, unapproved/exhausted
budget, persisted attempt accounting, failed HTTP with no retry, upstream/total timeout,
task/client-disconnect cancellation, cache coalescing/TTL/LRU/context/config keys,
upload/media/pixel/field bounds, redacted errors and unchanged device enum.

The first endpoint test failed with 404 before implementation and passed afterward.
The manufacturer-collision test also failed before tightening source matching and
passed afterward. The HTTP endpoint integration test exposed an AnyIO cancelled-scope
polling hang against `BoundedRequest`'s `asyncio.wait_for` child receive; a diagnostic
probe without polling passed, and switching to cancellable blocking receive fixed
both the integration test and the ASGI disconnect test. Hung offline pytest processes
were terminated; the live service was untouched.

Wheel packaging also passed using `uv build --wheel --out-dir
/tmp/howlens-discovery-wheel`; an isolated `python -I` ZIP import probe confirmed
the three discovery modules, additive endpoint, required response fields and
unchanged inline v0.1 device enum. No paid calls occurred. Two initial offline build
attempts failed because setuptools was absent from the test venv/UV cache; normal
isolated build resolved the existing declared build requirement without changing
project dependencies. The first schema probe incorrectly expected a named `Device`
schema; the corrected probe checked the actual inline multipart enum and passed.

## Evidence limits and remaining integration work

Official OpenAI docs fetched during this dispatch establish Responses image input,
`gpt-4.1-mini` image/structured-output/web-search support, required web tool choice,
consulted-source include and the tool-call bound. No local OpenAI SDK is installed;
the existing REST/httpx approach is reused. The account's actual model/tool/schema
compatibility, search latency, quality and billed cost still need the coordinator's
bounded initial check (<=2 REST attempts/<=1 search) after integration.

This conservative first version requires complete visible printed manufacturer and
model, and can reject legitimate products whose source metadata does not spell out
the complete identity. Source title/path/hostname matching is a plausibility gate,
not independent verification of the webpage's semantic claims. It prioritizes official
sources in the prompt, never registers search results as reviewed manual evidence,
and never creates repair instructions, guide approval or automatic safety decisions.

URLs undergo lexical public-address validation; this module does not DNS-resolve or
fetch them. A source response timestamp is not a guarantee of the webpage's update
date. The existing ledger must be used by a single configured app/provider instance;
integration must avoid concurrent independent live processes/helpers sharing it.

Coordinator granted exclusive staging/commit/push through `relay_0786756b8d6a` after
QA acknowledged no Git mutation in progress. Only the eleven discovery-owned files
were staged and committed; QA's untracked documentation was preserved.
Implementation SHA: `75de75bb4476e9ca5b8862c98dbbbf7bc911283b`.
The subsequent report-metadata commit and verified pushed HEAD are delivered through
the lifecycle completion, without rewriting either commit.

## Coordinator follow-up: PDF intake

On 2026-10-09, after coordinator relay `relay_6792bb3b4a21`, read-only checks of
`/Users/jymbook/HowLens/docs/assets/manuals/backend` and this worktree's matching
`docs/assets/manuals/backend` confirmed both directories exist and are empty.
An ignore-disabled PDF search also returned no files. Filename/size/SHA256/page
count/product/version cannot be reported without an original; the coordinator was
notified and asked to relay the actual upload path if different. No PDF original was
committed or transmitted. No user product photo has arrived in this dispatch, so
the synthetic offline suite is not represented as user-photo evaluation.

Coordinator relay `relay_3c7d967ae30d` then authorized path-only PDF inventory
across the two complete repositories. That ignore-disabled search found five files
under `/Users/jymbook/HowLens/docs/assets/manuals/evaluation/`:

- `um1724-stm32-nucleo64-boards-mb1136-stmicroelectronics.pdf`
- `WHP-Robot-Application-Manual.pdf`
- `9b6e38.pdf`
- `be94f9.pdf`
- `slvub62b.pdf`

It also found the worktree's three existing `backend/.manual-cache/` PDFs:
`ur5e-710-965-00-10.5.152.pdf`, `SPD_UM_SU-990-6411_EN.pdf`, and
`dell-r750-ism-a11.pdf`. These locations were reported to the coordinator. The
expanded check read file paths only; evaluation-owned contents were not inspected,
and no newly discovered PDF was indexed, copied, committed or transmitted.
Owner-upload classification and the still-unreceived actual photo remain with the
coordinator; names do not establish printed product/version metadata.
