# Discovery integration runbook

The additive contract and bounds are in [API_PROPOSAL.md](API_PROPOSAL.md).

Offline checks:

```sh
cd backend
.venv/bin/python -m pytest -q
```

The configured factory automatically wires discovery using the same adapter as
analysis; no new environment names or key copy are needed. Provider authorization
and the persisted 20-attempt maximum remain in force. Production never uses the
offline `FakeDiscovery` fixture.

Coordinator owns integration, server restart and serial human QA. Existing live
server PID 74518 must remain running until that integration decision. This worker
does not control phones, ask humans directly or start a second live server.

Initial paid validation must have coordinator-provided per-check attempt/search
caps and an authorized photo. Do not run broad paid loops. One complete-label
discovery costs at most two reserved Responses attempts and one search call; an
unreadable-label response consumes at most one attempt. A retry is a new explicit
attempt, never automatic. Ledger is shared by one configured app/provider; do not
run parallel server processes or test helpers against the same account ledger.

For a coordinator-run human check, assign one person one action at a time: submit
one photo with a readable complete label. Expected result is a sourced `candidate`
matching that exact label, or an honest close-up/source request. Confirm that no
repair steps or safety approval appear and open the returned source independently.
Use another serial action for a blurry label: expected close-up request without a
web search. Report actual results and request IDs, not assumed identity correctness.

Multipart example for a coordinator-authorized integration server:

```sh
curl -X POST http://127.0.0.1:8000/product-discoveries \
  -F 'photo=@/absolute/path/to/authorized-label.png;type=image/png' \
  -F 'question=Identify this product' \
  -F 'model_hint='
```

This command is illustrative and has not been run against the live server. The
path must be a real authorized image, not a placeholder copied into execution.
