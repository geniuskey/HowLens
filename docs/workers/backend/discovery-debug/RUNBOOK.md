# Discovery regression and diagnostic runbook

Run offline from `backend/` using the prepared validation environment:

```sh
HOWLENS_BENCHMARK_PROTOCOL_DIR=/Users/runixs/HowLens/evaluation/image_models /tmp/howlens-main-validation-20261009/bin/python -m pytest tests/test_discovery.py tests/test_discovery_api.py -q
```

The combined suite was run through `pytest.main(['-q'])` after replacing `socket.socket.connect` and `connect_ex` with wrappers rejecting AF_INET/AF_INET6, permitting only local Unix sockets. This disables real TCP while retaining the MockTransport and TestClient paths. Expected count at handoff: 192 passed, 14 subtests passed.

For a root-authorized replay on the existing Backend owner's account/process lifecycle, enable only logger `howlens.discovery` at INFO with a configured handler. Do not enable HTTP debug logging or dump inputs. Correlate the returned discovery_id with `discovery_gate id=... reason=...`; a cache hit reuses its original ID and does not create a new gate event.

Gate codes: `label_identity`, `search_incomplete`, `search_empty`, `candidate_manufacturer`, `candidate_model`, `candidate_description`, `source_provenance`, `source_identity`, `accepted`. Multiple rejected candidate codes are sorted and comma-separated. `source_provenance` means no requested candidate URL survived actual-source extraction; `source_identity` means at least one survived but none supported the complete identity. These codes are not a raw provider trace and cannot reconstruct historical calls.

Use the clear original label first, no model hint, and one controlled request under the existing shared ledger cap. Record exact deployed SHA, public status/model/citations, gate code, elapsed time and numeric usage deltas. Stop after that request; no automatic retries or cap increase. A whole-photo needs_more_information result is expected when the model is not visible. HTTP 200 alone is not product identification success.
