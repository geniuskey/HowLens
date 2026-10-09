# [DONE] Authorized HTTPS demo deployment

Public demo URL: **https://premium-proceed-hartford-published.trycloudflare.com**

The user explicitly authorized deployment through coordinator relay
`relay_fb6c7d4e6eb6`, then directly prioritized that relay over PDF intake.
Authenticated loopback gateway and Cloudflare Quick Tunnel are running in dedicated
durable Orca terminals. Existing Backend PID 74518 was preserved; this worker made
no paid request, copied no API key and printed no demo token.

## Actual runtime

| Service | Address / PID | Orca terminal | Local start time |
|---|---|---|---|
| Existing Backend | `127.0.0.1:8000`, PID74518 (existing all-interface listener) | `term_bde18df8-bc41-4a7f-bd14-2742207763a7` | 2026-10-09 11:50:10 KST |
| Auth gateway | `127.0.0.1:18080`, PID46220 | `term_7e05c37c-9fdf-4313-8c5b-f1af20abeb73` | 2026-10-09 13:16:00 KST |
| cloudflared 2026.10.0 | HTTP/2 tunnel, PID46758 | `term_d4770fd0-03f6-415d-9238-71daae4ed799` | 2026-10-09 13:16:29 KST |

Gateway source: `backend/dev/public_gateway.py`, SHA256
`f954fd848ebdc0b1c54b7a9b79ceb50dfd85a7a31c60c393a5b0caa30e5b6e99`.
This is the file content launched, not a claim that uncommitted code was already in
the preceding repository HEAD. Discovery implementation is pushed separately at
`75de75bb4476e9ca5b8862c98dbbbf7bc911283b`; pre-gateway/report HEAD was
`eb838e3876b05419c23d9b8a73849260f1399b78`.

The existing Backend does not expose `/product-discoveries`: actual local OpenAPI
confirmed this. Canonically sorted OpenAPI JSON SHA256 at this checkpoint:
`246c9ca9d0becbf96b21cf81ad5971224e232458eddb1098200efd62338daa99`.
Exact loaded upstream Git SHA cannot be proven from the current checkout: PID74518
predates later commits and was deliberately not restarted. Startup time and observed
schema are recorded instead of claiming current HEAD is served. Coordinator owns
the later combined restart/discovery activation.

## Verification actually observed

- Backend host: local gateway `/health` HTTP200, body status=ok/mode=live.
- Backend host: actual HTTPS `/health` HTTP200 with the same body.
- Actual HTTPS unauthenticated `POST /analyses` HTTP401.
- Local unauthenticated visual job/assets HTTP401; `/docs` HTTP404.
- Authenticated local lookup of an unknown visual job HTTP404 with upstream
  `job_not_found`, proving allowed authenticated passthrough without a paid call.
- Token file is ignored by Git and has Unix mode0600. Token value never appears
  in command arguments, tool output, repository, chat or Orca messages.
- Provider usage ledger snapshot: attempts=1/completed=1; these pre-existing counts
  were only read as numeric diagnostics, not reset or expanded.
- Coordinator independent Root-PC HTTPS test passed: `/health`200 in0.5085s and
  unauthenticated `/analyses`401 in0.3393s (`relay_482ce98b7293`).
- Coordinator authenticated empty public POST also returned422 without a provider
  call; confirmation arrived through the serialized-commit reply and slot grant.
- `.venv/bin/python -m pytest -q`: **123 passed, one existing Starlette TestClient
  deprecation warning, 0.89s**. Five gateway offline tests cover public health/auth,
  private token creation, fixed upstream/status/body preservation, size and route
  exclusions, duplicate/wrong bearer headers, encoded paths and disconnect cancellation.

The first fake-upstream body test exposed use of raw iteration on a pre-read mock
response; decoded byte iteration with content-type-only response forwarding fixed
it and also avoids forwarding stale compressed content length. This was corrected
before gateway startup. HTTP status and error JSON are preserved.

## Bounds and ownership

- Public `GET /health`; other known contract routes require constant-time comparison
  against the per-demo bearer token loaded from ignored `backend/.demo-token`.
- Fixed origin `http://127.0.0.1:8000`; no client URL/host/query forwarding, redirects,
  path traversal, encoded-route bypass, docs/admin routes, auth/cookie forwarding or
  request body/header logs. Visual job/assets routes use the same bearer gate.
- Four admission slots; 50ms acquisition limit, 30s bounded body stream, total
  multipart ceiling10MiB+64KiB. Complete bounded body is collected before forwarding;
  the existing Backend still checks photo<=10MiB/20MP.
- Upstream phase65s, decoded response ceiling32MiB, no proxy retry. Disconnect or
  gateway task cancellation closes its HTTP transport and releases capacity.
  Old upstream analysis cancellation semantics are unchanged; closing the transport
  does not prove cancellation of an already-started model call in that old service.
- Existing configured paid-attempt cap and account authorization remain with the
  preserved Backend. Gateway creates no key/provider/paid ledger of its own.
- cloudflared uses HTTP/2 and info logs (no debug header logging); access logging is
  disabled on gateway Uvicorn. Homebrew installed official cloudflared2026.10.0;
  no Cloudflare account/email login or boot service was configured.

## Remaining work

Coordinator/App owns phone token entry and same-origin authenticated API/asset
requests. No real photo or paid analysis was submitted by this worker. Runtime
discovery activation and any capped live check remain with coordinator integration.
PDF manifest was preserved at the explicit P0 deployment interruption. After TLS
verification, `discovery/manual-intake/observation_inventory.json` was completed
with five short exact caption quotes, page/figure locations and photo-only questions;
all original hashes were rechecked unchanged. No manual was runtime-registered or
guide-approved, and public-source provenance remains unverified.

## Primary reference

[Cloudflare Quick Tunnels](https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/)
was fetched on2026-10-09; it documents temporary HTTPS without an account/domain,
changing hostnames after restart, and lack of interactive-email support for native
noninteractive clients. CLI help was checked before launching. The running URL is
temporary and stops when the tunnel process stops.
