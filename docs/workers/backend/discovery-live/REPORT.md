# Deployment and bounded live measurements

Current upstream: **c462a290d45e084b9b29a260d57891a71d022050**, immutable source
`/tmp/howlens-discovery-runtime-c462a29`, PID **89580**, Orca terminal
`term_4f01bd3a-800a-48c9-a774-91830d385164`. Current gateway **99195** and tunnel46758
remain running with the unchanged public URL:
https://premium-proceed-hartford-published.trycloudflare.com

Both original d00384a activation and c462a29 patch activation used Coordinator's
explicit phone-idle grants. Canonical `.env`, private DEMO token, persisted20-attempt
ledger and active checkout were preserved. Old upstream74518, then67997, were
stopped gracefully; only upstream8000 was replaced. Current local/public health200,
discovery schema presence and authenticated empty-request422 were observed.
See [deployment.json](deployment.json) and [patched_deployment.json](patched_deployment.json).

## Actual recognition result: unresolved, not functional PASS

All photos were unchanged JPEG originals matching the supplied manifest. Label
4015b4013bb71d9f is1.212MP/806584bytes and whole98ca76178e51d1f5 is12MP/7025339bytes;
both pass20MP/10MiB. Same generic question and empty model_hint were used throughout;
the supplied evaluation answer was never placed in a model prompt/hint.

| Version / authorized case | HTTP / status | Time | Attempts / searches | Input / output tokens | Result |
|---|---|---:|---:|---:|---|
| d00384a label | 200 / needs_more_information | 7.336s | 2 /1 | 11106 /451 | No accepted model or citation |
| d00384a whole | 200 / needs_more_information | 8.630s | 1 /0 | 2575 /22 | Label-close-up request |
| c462a29 single label replay | 200 / needs_more_information | 8.490s | 2 /1 | 11106 /463 | Fixed reason `source_provenance` |

The patched reason means no candidate-requested URL survived extraction/membership
against actual consulted/citation metadata. It does not expose the internal model
or establish the historical precise cause. The original adapter discarded stage
DTO/envelopes; they cannot be reconstructed from the completed requests. Actual
public results, numeric usage and honest stage limits are in [results.json](results.json),
[stage_diagnostics.json](stage_diagnostics.json), and [patched_replay.json](patched_replay.json).

Shared ledger attempts1→4→6; **14/20 remained** at the final measured replay.
No automatic retry or cap increase occurred. Original two-call conservative estimate
wasUSD0.0194292; the separately authorized replay estimate wasUSD0.0183832. These use
uncached model list rates plus a conservative reserved-search fee/input allowance,
not account billing receipts. Actual billed cost remains unverified. Further Backend
paid replays are HOLD. Coordinator released all three phones immediately after the
replay; human feedback and synchronized installs belong to the Coordinator.

## One image envelope, completed

Visual2021958a10421584d1b935191d3de5b045be0e72 and Evaluation0234159 were extracted
immutably under `/tmp/howlens-visual-paired-20261009`. First-time pinned-source
offline preflight passed39 tests; zero-call preview had6 planned records and0 attempts.
Exactly one authorized paired-sunburst invocation then made6 distinct requests,
concurrency1/retry0/120s timeout, withinUSD6 local reservation.

All six returned HTTP200 and valid1024-square PNGs. Requested model was
`gpt-image-2.5-sunburst-2026-09-08`; returned model was omitted/null, while returned
low/medium controls matched requests. Latency range12.990–19.526s. Six finished
receipt pairs and twelve raw/normalized hashes were verified. Raw images remain
private at `/tmp/howlens-visual-paired-20261009/measured`; only metadata/receipts were
copied into [visual-benchmark](visual-benchmark/measurement_metadata.json).
Actual billedUSD, human quality, reference fidelity, winner and p95 remain null.
This synthetic guide-output benchmark is separate from real photo recognition and
does not grant product/repair/safety approval. No second invocation was run.

## Current demo access: explicit no-auth override

Latest explicit user override `relay_a1f11148a1c0` disabled only temporary DEMO
bearer friction. Gateway46220 was replaced by99195 in terminal
`term_48011114-e653-4ca3-900e-c4b9bc78b8ad`, with explicit runtime configuration
`HOWLENS_DEMO_AUTH_DISABLED=true`. Upstream89580/tunnel46758 and hostname were unchanged.
Actual local/public unauthenticated emptyPOST `/analyses` and `/product-discoveries`
returned422 instead of401; health200 and excluded `/docs`404 passed. Ledger remained
6/20, with no paid request in these checks. See [noauth_gateway.json](noauth_gateway.json).
Route allowlist, body/concurrency/time limits, fixed upstream, transport cancellation,
provider ledger and all physical safety/guide gates remain in force. No OpenAI key
is exposed. Clients now need only the base URL, no DEMO or account credential.

## Prior provisioning, now discontinued

The DEMO bearer is separate from OpenAI/account credentials. Its ignored0600 local
file remains on the owner PC. Verified Galaxy/Windows recipient public keys received
only RSA-OAEP-SHA256/MGF1-SHA256 ciphertexts; private keys remained on recipient PCs,
and no plaintext value was printed, put in tool arguments, logged or committed.
Root independently performed the same provisioning; its earlier delivered envelopes
are authoritative, so the crossed duplicate ciphertext was not needed.

Local Fold4 token setup was discontinued by the latest user instruction before input.
No phone input, competing taps, camera automation or repeated baseline suite occurred.
The owner-only token file was opened privately in TextEdit; there was no GUI capture.
After setup, all phone interaction belongs to humans/Coordinator.

Production Visual provider is not activated by the deployed configured_app factory;
it remains distinct from the successful six-call standalone Image API benchmark.
No production guide or image-generation approval was added in this Dispatch.

Runtime-only launch/rollback instructions are in [RUNBOOK.md](RUNBOOK.md). No product
source was changed in this Dispatch. The observed identification blocker is assigned
to Coordinator's separate discovery-fix worker; HTTP200 is not identification success.
