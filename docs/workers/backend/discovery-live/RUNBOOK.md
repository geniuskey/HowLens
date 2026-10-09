# Owner runtime handoff

Keep current gateway99195/tunnel46758 and upstream89580 running. Public URL is
https://premium-proceed-hartford-published.trycloudflare.com . The current upstream
was launched from immutable c462a29 source by:

```sh
cd /Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation
exec backend/.venv/bin/python docs/workers/backend/discovery-live/patched_launcher.py
```

The launcher points config to the canonical owner `backend/.env` in memory. It does
not copy/read out credentials, change the20-attempt cap or reset the ledger.
Only `howlens.discovery` INFO messages go to private
`/tmp/howlens-discovery-c462a29-gates.log`: discovery ID and fixed gate reason only.
Do not enable HTTP debug/access logging or dump provider/photo inputs.

Current gateway is an explicit temporary demo-auth override, started by:

```sh
HOWLENS_DEMO_AUTH_DISABLED=true exec backend/.venv/bin/python \
  docs/workers/backend/discovery-live/demo_gateway_launcher.py
```

This runtime-only wrapper keeps the original proxy route/body/admission/time/cancellation
guards and changes only its bearer prerequisite. It reads no OpenAI key and requires
no phone credential. Removing the explicit true flag before a coordinated gateway
restart restores the original bearer mode. Do not restart the tunnel/hostname.

Coordinator owns future lifecycle. To replace upstream, obtain a short API-idle
window, verify the current PID, stop only that upstream, and launch the approved
immutable snapshot. Do not restart gateway/tunnel or change the public hostname.
Current source snapshot and launch helper must remain available for resume.

Prepared rollback is known pre-discovery source d977170 at
`/tmp/howlens-discovery-rollback-d977170`. On an explicit rollback decision, after
stopping candidate upstream, run:

```sh
exec backend/.venv/bin/python docs/workers/backend/discovery-live/rollback_launcher.py
```

It uses the same canonical key/config/usage paths, never an old ledger copy. Rollback
does not preserve in-memory analysis/job IDs; restart may yield404 for old IDs, as
the existing v0.1 contract states. No rollback was executed.

Original live checks and one patched replay are already consumed: do not rerun them
without a fresh bounded authorization. The image measured directory is also a
completed single envelope, not resumable/repeatable authorization. Raw generated
images stay private; request IDs/usage/returned settings and hashes are retained
for billing/human review. Actual receipt costs and human scores are not inferred.

Token provisioning is discontinued under the latest explicit no-auth instruction.
No OpenAI/account credential belongs on a phone or in an APK. No independent phone
interaction or automated test is allowed during the human trial window; Coordinator
owns future synchronized builds, installs and feedback.
