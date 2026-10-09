# [DONE] Discovery identity debugging — 2026-10-09

Implemented bounded identity matching and source metadata preservation, with fail-closed model-variant tests and fixed-code diagnostics. Actual sanitized results prove the clear label failed the post-search candidate/source gate, but cannot prove which predicate failed because the old provider discarded its internal responses. The patch is ready for the root-owned commit; a controlled replay by the live Backend owner remains necessary to establish real product discovery success.

## Evidence and uncertainty

- Read-only user label `docs/4015b4013bb71d9f.jpg`: manufacturer `쿠쿠홈시스(주)`, model `AC-35U20FWS`; the radio certification contains a different model-like string `AC-34U20`, which must not replace the product model.
- Read-only whole photo `docs/98ca76178e51d1f5.jpg`: CUCKOO branding is visible, but no complete model label. Requesting a label is appropriate; shape alone cannot identify the version.
- Coordinator-routed actual artifacts: `/tmp/howlens-discovery-diagnostics/results.json` and `stage_diagnostics.json`. Clear label: HTTP 200, needs_more_information, 7.336 seconds, two completed attempts, one search. Whole photo: HTTP 200, needs_more_information, 8.63 seconds, one attempt, no search.
- No live LabelObservation, SearchResult or citation envelope exists in those artifacts. Neither the exact rejected predicate nor improved live accuracy/latency can be claimed from offline tests.

## Independently reproduced bugs and changes

Against the committed discovery module, a synthetic actual-source-shaped fixture with the printed Korean legal name and a `CUCKOO AC35U20FWS` citation returned false; the patched matcher returns true. An actual citation title followed by a hostname-only consulted entry was overwritten; either metadata order now preserves the supporting title. The old model containment also incorrectly accepted `AC-35U20FWS(S)` as support for `AC-35U20FWS`; the patched matcher rejects this.

Normalization is limited to explicit CUCKOO/Homesys legal/brand aliases, Unicode compatibility typography, common dash forms, case, and model spacing/hyphens. Digits and suffixes remain significant, including `FCG`, different numeric models, `(S)`, `/S`, and `-2`. The original printed manufacturer/model remain in the public result. Unknown manufacturer spellings are not guessed or translated.

Alias evidence checked on the official [CUCKOO company page](https://www.cuckoo.co.kr/company/cuckooGuideCuckooIntroduce): CUCKOO/Homesys and the same address as the label. An official [CUCKOO manual](https://www.cuckoo.co.kr/upload_cuckoo/_bo_rep/manual/file_6936b21d-27c4-41ba-88f9-0e489f223bc6.pdf) pairs CUCKOO with 쿠쿠홈시스㈜. These establish brand aliases, not authorization for this product's procedures or equivalence of model variants.

The official [product page](https://www.cuckoo.co.kr/mall/productView?categoryCd=3&productNo=5241) is indexed with model `AC-35U20FWS(S)`, while its page title is only `CUCKOO` and URL uses a numeric productNo. A provider returning just that title/URL still does not supply complete-model metadata. The patch deliberately does not infer identity from query echoes, generated summaries, numeric IDs, or assume `(S)` is interchangeable; this remains a possible real failure mode needing a sanitized replay.

Unchanged: actual provider citation/consulted-URL membership, public URL/credential/private-address guards, no source URL fetch, descriptive-only output guard, no guide steps, shared persistent attempt budget, at most two calls/one search, timeout/cache/concurrency bounds. Korean missing-information messages now explain the failure without internal details. INFO events contain only generated discovery ID and fixed gate codes; no photos, OCR text, questions, hints, URLs or keys.

## Validation

- Targeted discovery/API suite: **99 passed**, one existing Starlette deprecation warning.
- Combined backend + visual suite with AF_INET/AF_INET6 connect/connect_ex disabled: **192 passed, 14 subtests passed**, 10 existing Starlette/Pillow warnings, 2.34 seconds.
- Added 39 parametrized cases cover brand/legal-name and model typography equivalence; candidate/title/path/label variants; query-only and generic metadata; lookalike domains; duplicate metadata order; diagnostic redaction.
- `git diff --check` on owned code/test paths: clean.
- All provider requests in tests were synthetic MockTransport. No paid call, API key access, live PC/process mutation, or Git mutation performed.

## Handoff

Changed files: `backend/howlens/discovery.py`, `backend/tests/test_discovery.py`, this report, and `RUNBOOK.md` beside it. `discovery_models.py` and `test_discovery_api.py` did not need changes. Checkout is shared `main`; observed HEAD during validation was `4feba8ec15836b66a38a000c1d23de62352c903c`. Root alone owns add/commit/push and deployment; concurrent Android/coordinator edits were not touched.

Remaining: owner-controlled replay with diagnostic INFO enabled, preserving the existing paid cap and exact code version. If the reason is source_identity, inspect only the minimally sanitized public citation/source title, URL and exact candidate/label identity in memory under owner control; do not enable raw request/response/photo logging. Richer page-body evidence would require a separately reviewed provenance design, not an extra arbitrary fetch or a relaxed matching predicate.
