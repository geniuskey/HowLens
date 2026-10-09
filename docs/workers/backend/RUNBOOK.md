# Backend W2 runbook

Current implementation preserves W1 `d035a285` and adds env-backed real OpenAI Responses, independently checked descriptive manual facts, controlled paid integration limits, and cancellation-safe bounded decoding. Runtime needs Python 3.11+; validated macOS/Python 3.14.6.

## Current local server

Backend API is running in Orca terminal `term_bde18df8-bc41-4a7f-bd14-2742207763a7`, title `Backend API W2 (20-call cap)`, Uvicorn PID **74518**. Backend/Coordinator owns this process. Actual health checks passed from this host at both `http://127.0.0.1:8000/health` and `http://10.102.72.28:8000/health`; remote Android/LAN reachability remains untested. IP was obtained with `ipconfig getifaddr en0` and may change. Stop with Ctrl+C in that dedicated terminal, or `kill -TERM 74518` after confirming that PID still identifies this server. Do not close the dispatched worker terminal to stop it.

Launch/restart from repository root:

```sh
cd /Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation
backend/.venv/bin/pip install -e 'backend[test]'
backend/.venv/bin/python -m howlens.config
backend/.venv/bin/uvicorn howlens.config:configured_app --factory --host 0.0.0.0 --port 8000 --no-access-log
```

`howlens.main:app` remains the unconfigured W1 entry point; use the configured factory above for live integration. Binding all interfaces is local development only; no public product deployment was performed. No CORS/API contract changes were introduced. `/health` returns status=ok/mode=live, which reports process health rather than approval of equipment or procedures.

## Key and controlled budget

Local human key location: `/Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation/backend/.env`, ignored and chmod0600. Orca editor was opened; user saved the key there. Never print/read the key into tool output or share it with another PC. `.env.example` is intentionally ignored and blank, as requested. For a fresh checkout create these variables locally:

```dotenv
OPENAI_API_KEY=
OPENAI_MODEL=
HOWLENS_PAID_CALLS_ENABLED=
HOWLENS_MAX_PROVIDER_CALLS=
```

Loader reads the exact absolute backend `.env` path and does not override process environment. Current authorized local settings are model `gpt-4.1-mini`, paid flag `true`, request limit `20`; actual key is never recorded here. Restart after configuration changes. Factory defaults to zero paid attempts unless an explicit request limit is configured, and a key alone never authorizes a call.

Coordinator explicitly relayed user authorization of **USD50 API budget for this server account**, distinct from Codex credits, then authorized one smoke followed by **up to20 bounded analysis/verification attempts** for the next integration checkpoint. Budget/balance in the billing UI was not independently verified. Health and rejected non-guide verification/visual do not consume paid calls. No automatic retries. Image generation remains disabled regardless of analysis budget.

Each attempted Responses request is reserved before network I/O and counts even if timeout/network/HTTP failure occurs. Maximum20 for the running integration server is persisted in ignored owner-only `backend/.provider-usage.json` across restart; do not delete/reset it or increase the cap without a new Coordinator-approved checkpoint. Ledger contains only model, numeric attempts/completions/tokens/status/cost estimate, never key/prompt/photo/response text. Two upstream slots, 25s adapter timeout, 30s endpoint timeout, max3000 output tokens, 1MiB streamed upstream JSON and strict DTO bounds remain enforced. The one prior smoke used a separate one-call process, max1500 tokens; the server cap is for the subsequent20 attempts.

Sanitized readiness command prints only booleans/counts. Last observed after configuration: key_present=true, model_configured=true, paid_calls_enabled=true, descriptive manual_entries=5; independent physical review=false, Visual integrated=false. Ready for live **non-guide observation** integration. Genuine guide is currently unreachable: zero approved action mappings, all required model prerequisites forced unknown and reviewer issues no physical approval.

## Validation and one paid smoke

```sh
backend/.venv/bin/python -m pytest backend/tests -q
backend/.venv/bin/pip check
backend/.venv/bin/pip wheel --no-deps --wheel-dir /tmp/howlens-backend-w2-wheel ./backend
```

Latest suite: **58 passed, 1 warning in0.58s**; all original38 remain passing. Warning is Starlette1.7.0's httpx TestClient deprecation; not suppressed. Editable install (python-dotenv1.2.4), wheel and pip check succeeded. Env-backed factory was also started on an ephemeral all-interface port, HTTP health checked, then stopped before the dedicated server was launched.

Exactly one paid command executed by this Worker, after budget/key-ready authorization:

```sh
backend/.venv/bin/python -m howlens.live_smoke --authorized-one-call --model gpt-4.1-mini
```

This script creates a synthetic64x64 solid gray PNG, makes one analysis request through the real adapter and prints only sanitized status/usage. Result: upstream200/backend200, live needs_more_information, registered evidence2, steps0, input963/output337/total1300 tokens. Estimated upper cost **USD0.0009244**, using published standard input0.40/output1.60 per1M tokens and ignoring possible cached discounts. Not verified invoice/balance. This proves live wire/schema compatibility on that account, not real equipment identity, photo relevance, safety or guide success. Do not rerun this paid smoke automatically; Worker did no paid retry or image generation.

## Evidence and guide gates

The configured factory loads `howlens/manual_sources.json`: three original manufacturer PDFs independently downloaded over HTTPS and checked against research5c0b830 SHA-256/byte counts, PDFKit physical page counts and five exact short excerpts after whitespace normalization. Full PDFs are only in ignored local `.manual-cache/`; not redistributed/committed. References:

- Dell R750 Installation/Service Manual, December2024 RevA11, SHA256 `1bdf0df910860207f11a27d9fb7ce6effcb70c0a324ed06e3579d1da6fe6b879`, 255pages, PDF11/245.
- UR5e PolyScope5.17, DocumentVersion10.5.152/710-965-00, SHA256 `2bff4708278fa9928f03505ba245c19268818f57dfe6f806680156f9c9e28cae`, 362pages, PDF329.
- APC SMT tower EN990-6411A/RevA, PDF04/2022, SHA256 `681778248bc8eae8883d00ad626719512e969a54d5600043b691ec91c11194bf`, 20pages, PDF10=printed8, two short captions.

Exact original URLs/version/physical and printed pages/quotes/hash/review metadata are in the catalog. Manufacturer copyright retained, short attributed excerpts only, no reuse license claimed. Each source is exact-URL allowlisted; full device/document/version/page/section/quote/source/printed-page tuple is compared deterministically. Missing catalog returns an empty registry, never fake evidence. The five **descriptive facts have zero approved actions**: they can contextualize observations but cannot validate arbitrary procedural steps. Equipment/SKU/firmware match and field permission/conditions remain unknown.

Guide proposal sent to Coordinator, not implemented: optional Form `observation_session_id` referencing a server-owned operator-reviewed TTL record of exact R750 identity/access/photo permission/stable placement/visible panel/no-contact hazard assessment; source-aware independent application of confirmed prerequisites; independently approved no-contact observation action tied to DellA11 PDF245. A question/checkbox/model assertion is insufficient. No new contract field was added; genuine guide/verification/Visual remain blocked pending the Coordinator decision and actual equipment/operator inputs.

## Decode cancellation and Visual preparation

A regression reproduced peak4 concurrent decode threads with the old async-semaphore/to_thread cancellation path. Fixed via dedicated max2 ThreadPoolExecutor, shielded await and permit release only in actual concurrent-future completion; canceled requests return without freeing live decode capacity, with exception consumption and lifespan executor cleanup. Original Pillow work cannot be forcibly killed, but actual workers stay bounded. The same published independent W3 probe8e69072 now exits0 with decode_cancel_peak2; non-guide/mock gates and verification/visual schemas pass. The local cancellation test also passes.

Read published Visual63dd0f8 public service/openai-provider/splitter plus its RUNBOOK; did not merge/copy/edit owned product Visual files. Temporary external snapshot offline integration passed nine-panel row-major mapping/configuration, using a synthetic image provider and reviewer. `configure_same_process_visual(integration_authorized=True)` prepares `OpenAIStoryboardProvider.from_env()` startup configuration without a call; it is not activated in HTTP runtime. `generate_reviewed_assets` requires stored independent guide approval, separate image budget and semantic reviewer before generation; uses published `scene_step_ids` and nine split panels. Route/assets stay failed preserving text until authorized integration/semantic acceptance; no paid image call.

## Official docs opened

- https://developers.openai.com/api/docs/guides/images-vision
- https://developers.openai.com/api/docs/guides/structured-outputs?api-mode=responses
- https://developers.openai.com/api/docs/guides/migrate-to-responses
- https://developers.openai.com/api/docs/models/gpt-4.1-mini — image input, Responses, strict outputs, input/output rates.
