# [DONE] Server research and role routing — 2026-10-09

Task `task_dc71a3804022`, dispatch `ctx_0621c812c305`; base `c462a29`, shared main checkout, root owns all Git mutations and deployment. Scope expanded by coordinator relays to server-only autonomous analysis and role routing; additive `research` response explicitly approved. No Android, gateway, live process, environment/key, paid call or Git mutation was performed.

## Actual evidence versus reproduced defects

The routed owner replay reports HTTP 200 / 8.49 seconds / two attempts / one search / `source_provenance` / no candidate. This establishes only that requested candidate URLs did not survive membership validation. Minimal candidate/citation/consulted URL metadata was requested through the coordinator but was not available at handoff; **the actual failure predicate is still unconfirmed**.

Official [Responses web-search documentation](https://developers.openai.com/api/docs/guides/tools-web-search) specifies flat `output[].content[].annotations[]` URL citations and `web_search_call.action.sources`; the latter may supply URL without title. The official [Responses reference example](https://developers.openai.com/api/reference/typescript/resources/responses/methods/create) includes the exact attribution parameter `utm_source=chatgpt.com` on citation URLs. No undocumented recursive/nested Chat Completions shape is accepted.

Offline before/after against the actual `c462a29` module: a synthetic candidate URL without that attribution parameter, with the same exact product URL in actual citation metadata carrying it, returned **needs_more_information → candidate**. The returned URL is still the unchanged actual metadata URL. Other query values/product IDs, paths, variants, domains, fragments and private/credentialed URLs remain non-equivalent. Unsafe title text is replaced with its actual hostname rather than erasing valid URL provenance; this does not invent model evidence. Malformed entries are skipped. Source logs now separate numeric entry/invalid-URL/title-redaction/accepted counts and correlate them with discovery ID, without URLs or model text.

## Behavior

- Discovery still uses at most two provider calls and one search. If no complete label is readable but a broad visual category is discernible, it searches descriptive category sources and returns `status=needs_more_information`, empty `candidates`, and optional `research={category,observations,summary,sources}`. The server writes the uncertainty summary; no exact identity, guide or safety assertion is inferred from shape. Unsupported or injected hypotheses remain research=null.
- `/analyses` now has an optional generic question default. Configured server analysis can do initial analysis → label/category → search → registered-manual reanalysis, at most four provider attempts and one search. Research descriptions/sources fit the existing observations strings; the Analysis response schema is unchanged.
- STOP or warnings terminate enrichment. Reanalysis requires an exact sourced identity matching the selected reviewed catalog model and existing registered manual excerpts. Smart-UPS is only a family and cannot qualify for exact-model reanalysis. Unknown models and confusable variants do not trigger it. Web research never registers manuals or grants physical approval; existing independent review and sanitization remain authoritative.
- With routing enabled, the configured route timeout is 40 seconds and optional enrichment stops at 37 seconds measured from initial provider analysis; without routing the defaults remain 30/27 seconds. Optional work uses a deep copy and revalidates DTO bounds, preserving the validated original on timeout/failure/overflow. Analysis HTTP disconnect cancels the pending pipeline. Completed upload monitoring does not reuse the upload deadline. There is no retry loop.

## Role configuration for the next reviewed deployment

`HOWLENS_ROLE_ROUTING_ENABLED=true` enables these defaults; unset preserves the configured single-model transport. Official model capabilities/IDs were checked on [Luna](https://developers.openai.com/api/docs/models/gpt-6-luna), [Sol](https://developers.openai.com/api/docs/models/gpt-6.1-sol), and [Astra](https://developers.openai.com/api/docs/models/gpt-6-astra) pages. Coordinator separately reports authenticated model-list availability; neither that nor these offline tests proves live inference success.

| Stage | Actual request model | Effort | Max seconds | Max output tokens |
|---|---|---|---:|---:|
| Initial analysis | gpt-6.1-sol | medium | 20 | 2500 |
| Label/category triage | gpt-6-luna | low | 8 | 1500 |
| Descriptive research | gpt-6.1-sol | medium | 20 | 2500 |
| Qualified unresolved reanalysis | gpt-6-astra | low | 15 | 3000 |
| Existing post-guide verification | gpt-6.1-sol | medium | 20 | 2500 |

These are the coordinator's final requested budgets, **not** measured latency. Override IDs with `HOWLENS_ANALYSIS_MODEL`, `HOWLENS_TRIAGE_MODEL`, `HOWLENS_RESEARCH_MODEL`, `HOWLENS_ESCALATION_MODEL`; corresponding `HOWLENS_<ROLE>_REASONING_EFFORT` selects effort. Sol/Astra reject none; Luna permits it. Per-stage deadlines use `HOWLENS_INITIAL_TIMEOUT_SECONDS`, `HOWLENS_TRIAGE_TIMEOUT_SECONDS`, `HOWLENS_RESEARCH_TIMEOUT_SECONDS`, `HOWLENS_ESCALATION_TIMEOUT_SECONDS`, each at most 25 seconds. `HOWLENS_ANALYSIS_TIMEOUT_SECONDS` is at most 40; enrichment receives that value minus three. Per-stage maxima do not extend that shared deadline, so optional work may stop before using its full cap. Image generation is outside this worker's ownership and is unchanged.

Keep the existing `OPENAI_MODEL` ledger anchor when enabling routing: changing it would trigger the existing persisted-ledger model mismatch protection. Actual request IDs come from the stage policies, including initial Sol analysis. The original aggregate attempts/cap are never reset; `model_attempts` adds per-model accounting, and unsupported mixed-model/search price estimates stay null. INFO logger `howlens.openai_provider` records stage, requested/validated-returned model, elapsed_ms, HTTP status and numeric usage. It does not log questions, photos, keys, URLs or upstream bodies. `HOWLENS_AUTONOMOUS_RESEARCH` defaults true in configured_app and can explicitly disable enrichment.

## Verification and remaining work

New/changed-path tests cover attribution normalization, provenance mismatches, malformed metadata, redacted diagnostics, uncertain visual research, injection rejection, exact-model/manual gates, four-call and shared persisted budget caps, STOP preservation, partial results, schema-required fields, HTTP/task cancellation, explicit role routing, model mismatch and stage deadlines. The earlier 192-test baseline was intentionally not rerun. Final targeted run with AF_INET/AF_INET6 connect/connect_ex disabled: **40 passed in 0.69 seconds**, one existing Starlette deprecation warning; owned-path `git diff --check` clean.

Final selection: `tests/test_discovery_followup.py`, the changed API response-shape test, and consulted-source, duplicate-metadata-order, and gate-redaction tests from `test_discovery.py`. All provider traffic used MockTransport; this is not an inference acceptance run.

Root must review/commit and coordinate deployment after the immutable R2 human test. A subsequent controlled live request must record exact deployed SHA, effective requested/returned stage models, usage/latency and source counts; source_provenance still requires minimal sanitized candidate/source metadata to establish its real cause. Do not claim that a known product identification or a live latency target now passes solely from these offline results.
