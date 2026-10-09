# Public tunnel process handoff

Live URL: https://premium-proceed-hartford-published.trycloudflare.com

Preserve the three running processes recorded in [REPORT.md](REPORT.md).
Coordinator owns service lifecycle and App coordination. The demo token resides
only at `/Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation/backend/.demo-token`
(ignored,0600). Its owner may open that local file privately to enter the App token
setting; do not print/copy it into a chat, terminal transcript, command argument or
Orca message. The OpenAI key remains in the existing owner's environment/file.

## Launch commands (already executed)

Gateway, dedicated plain Orca terminal:

```sh
cd /Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation/backend
exec .venv/bin/python -m uvicorn dev.public_gateway:create_gateway --factory \
  --host 127.0.0.1 --port 18080 --no-access-log --log-level warning
```

Tunnel, separate dedicated plain Orca terminal:

```sh
exec /opt/homebrew/bin/cloudflared tunnel --no-autoupdate --protocol http2 \
  --url http://127.0.0.1:18080 --loglevel info
```

Do not point a tunnel at unguarded8000. No email login, permanent account tunnel,
automatic system startup or public artifact publishing is involved.

## Health and safe validation

Public health requires no token and incurs no provider call:

```sh
curl --max-time 15 https://premium-proceed-hartford-published.trycloudflare.com/health
```

Expected HTTP200 with status=ok/mode=live; mode is process health, not guide approval.
Unauthenticated `POST /analyses` should return401 and never reach the provider.
Coordinator may load the token from the local file into a request header inside a
local Python process without printing it; empty multipart/unknown-ID validations
must fail before any paid model invocation. Actual photos require the coordinated
bounded live-test cap. App sends bearer headers to analysis, visual jobs and assets.

## Stop / resume

When Coordinator requests cleanup, first stop only tunnel terminal
`term_d4770fd0-03f6-415d-9238-71daae4ed799` with Ctrl+C, then gateway terminal
`term_7e05c37c-9fdf-4313-8c5b-f1af20abeb73`. If using PIDs, verify each process still
matches before sending SIGTERM to46758 and46220. Do not signal old Backend74518
as part of tunnel cleanup. No cleanup command was executed by this worker.

Resume through new/dedicated plain Orca terminals using the commands above and the
existing private token file. A tunnel restart creates a new hostname; report and
update App base URL before testing. A gateway restart retains the token; do not
delete/regenerate it implicitly. Token rotation is a separate coordinated change
requiring gateway restart and owner App setting update.

Backend discovery code is pushed but not loaded by PID74518. Only a coordinated
upstream restart should activate it; do not infer running code from Git HEAD.
