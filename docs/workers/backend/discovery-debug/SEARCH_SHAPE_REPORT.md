# [DONE] Actual search envelope compatibility fix — 2026-10-09

Task `task_3c63f4373e1e`, dispatch `ctx_96043f5a0db9`; code base `670dd10`. No code was edited until coordinator relay `relay_6aa61ba953d1` supplied actual sanitized runtime shape:

```text
output[0]: web_search_call, completed, action.type=search,
           action keys queries/query/sources/type, 12 sources
output[1]: web_search_call, searching, action.type=search,
           action keys queries/query/type, 0 sources
output[2]: message, completed
```

The diagnostic replay took 18.598 seconds. The provider request had max_tool_calls=1 but included these two search-state entries. The precise `search_incomplete` cause was the old gate requiring exactly one total web_search_call item, rather than one completed search; it returned before validating the available completed-source evidence. This does not establish the cause of older source_provenance failures.

The [official Responses schema](https://developers.openai.com/api/reference/resources/responses/methods/create) documents completed, searching, in_progress, failed and incomplete web-search statuses. The [official web-search guide](https://developers.openai.com/api/docs/guides/tools-web-search) distinguishes search, open_page and find_in_page actions. Our narrow compatibility rule accepts exactly one completed search and at most one searching/in_progress search entry, as observed; it does not interpret an unfinished entry as completed work. Failed/incomplete calls, two completed searches, pending-only output, more than one pending entry, missing action and explicit non-search actions remain rejected.

Only completed search metadata supplies consulted sources; pending sources are discarded by the existing extractor. Candidate source membership, public URLs, exact manufacturer/model including variants, descriptive-only output, and guide safety are unchanged. The request still sends max_tool_calls=1, uses at most two discovery provider attempts, and retains the four-attempt analysis pipeline bound. Logs now distinguish completed/pending/total tool item counts; the attempt ledger remains separate from tool status counts.

Against the actual committed 670dd10 gate, the reconstructed observed envelope shape changed **False → True**. Synthetic full-pipeline evidence then produced a sourced candidate in two attempts, while pending-only provenance, confusable model variants and injected summaries remained rejected. Product URLs and identities in tests are explicitly synthetic; live candidate acceptance still requires root's deployment/replay.

Coordinator additionally authorized the provider constructor's configurable cumulative attempt ceiling to increase from 20 to 50, with root initially deploying max_calls=40. The focused ledger test loads 18 existing attempts, performs one synthetic attempt, reloads 19, and refuses a call when cap=19; no ledger reset occurs. Runtime/environment values were not changed by this worker.

Changed paths: `backend/howlens/discovery.py`, `backend/howlens/openai_provider.py`, `backend/tests/test_discovery_search_shape.py`, this report. Focused command: `/tmp/howlens-main-validation-20261009/bin/python -m pytest tests/test_discovery_search_shape.py -q` from backend. No baseline suite, paid calls, key/env/process changes, Android changes or Git mutations. Final focused test count is in the handoff message.
