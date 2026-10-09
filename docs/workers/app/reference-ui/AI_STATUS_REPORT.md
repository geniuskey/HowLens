# AI feature inventory and server model routing

2026-10-09. Direct user-owned continuation after the settled reference UI dispatch; no old Task/Dispatch lifecycle messages were reused.

## Delivered

- `ai-status.html`: interactive Korean inventory with eight feature rows, searchable by feature/model, expandable code evidence, and a separate server routing tab.
- `ai-status-data.js`: public snapshot, explicitly separating verified runtime configuration, code defaults, independent benchmarks and requested routing.
- `ai-status-desktop.png`, `ai-status-mobile.png`: 1440px and 390px captures.
- `check-ai-status.cjs`: new surface checks for eight features, search, details, routing tab, horizontal overflow and page errors. PASS; no old app test suites rerun.

## Verified current state (Coordinator-relayed evidence)

Backend source c462a29, runtime PID89580: OPENAI_MODEL=gpt-4.1-mini. At 14:00:15, the recorded product-discovery replay returned HTTP200 in8.49s but ended needs_more_information/source_provenance. That is a transport/provider response, not a completed guide. Usage snapshot:6/20 calls,11106input/463output tokens,1search; reported estimate$0.0183832 is not actual billing and search-fee inclusion was not verified here.

Image production setting is unset, so its code fallback is gpt-image-1.5, low,1024×1024,120s. Production live-guide image success is not established. A separate six-case benchmark requested gpt-image-2.5-sunburst-2026-09-08 in low/medium, returned HTTP200 in12.990–19.526s; returned model field null, quality not reviewed, actual billing unknown.

Canonical remote evidence supplied by Coordinator: Backend PC `docs/workers/backend/discovery-live/patched_replay.json` and visual-benchmark. These were relayed facts, not this worker's own remote execution.

## User-directed server routing

The user corrected both client-side orchestration and a single-model Astra upgrade. Server ownership is now explicit: role/complexity/latency select the model; unresolved work triggers tool use and bounded escalation, while the app makes one analysis request and displays progress/results.

| Server role | Requested route | Trigger |
|---|---|---|
| Fast extraction/triage | gpt-6-luna, low | Printed labels and straightforward short extraction |
| Main multimodal analysis/research | gpt-6.1-sol, medium | Combine photo, symptom, candidate identity and official sources |
| Difficult reassessment | gpt-6-astra, bounded | Conflicting evidence or unresolved complex reasoning |
| Guide imagery | Dedicated image model | Choose by reviewed quality/latency, not a text-model alias |

Coordinator confirmed nonpaid GET/v1/models200 lists gpt-6-luna, gpt-6.1-sol and gpt-6-astra (and image sunburst/flare models). This proves account model access, not inference success. Backend Worker ctx0621 is implementing the server router/research/document-validation changes; this dashboard does not claim deployment or model inference has passed.

Official documentation opened on2026-10-09: [GPT-6 Luna](https://developers.openai.com/api/docs/models/gpt-6-luna), [GPT-6.1 Sol](https://developers.openai.com/api/docs/models/gpt-6.1-sol), [GPT-6 Astra](https://developers.openai.com/api/docs/models/gpt-6-astra), [current family guide](https://developers.openai.com/api/docs/guides/latest-model), [web search](https://developers.openai.com/api/docs/guides/tools-web-search). The mapping above is an application design decision; the model names, vision/tool availability and reasoning choices were checked against those sources.

## Android source freeze and completed R2 rollout

The unpublished client research chain was removed after the user clarified server ownership. The remaining Android change accepts an optional question with a useful default prompt, displays constructive result labels, embeds only HOWLENS_API_BASE_URL at build time, defaults the configured app to live mode and removes demo token input. Extra automatic client/provider attempts: **zero**. Existing API token transport compatibility, cancellation and approved-guide gates were not broadened.

Root committed source319eda7, built R2 successfully in21s and installed/started the same artifact on all three phones. Root-reported SHA256: `3a34683ced34174d576f38d7137c8d15d641cb2b14de3d5cdfd7ed7ec5f1cbd1`;13,189,265bytes. Gateway PID99195 now accepts unauthenticated demo traffic; empty public POST /analyses and /product-discoveries returned422. Human testing is open, no automated taps performed by this worker. R1 reference artifact was not replaced by this worker.

Root owns Git, backend deployment, APK builds and synchronized device rollout. This worker made no paid provider requests and did not touch DEVICE_ROUND, BOARD or STATUS.

## Updating the inventory

Update only public facts in ai-status-data.js after a new verified runtime/deployment receipt. Preserve the distinction among requested model, configured model, provider-returned model and benchmark model. Never put API keys, demo credentials, user photos or raw prompts in this document or the dashboard.
