# Existing App APK delivery follow-up

## Artifact provenance

- Source commit: immutable `57405bb4811f67bb49ea779d883fd2937c98fd7d`
  (`geniuskey/feat-ui-foundation`).
- Source APK: `/Users/edwin/orca/workspaces/HowLens/feat-ui-foundation/android/app/build/outputs/apk/debug/app-debug.apk`.
- Method: copied the explicitly provided, pre-existing APK read-only; no rebuild or
  access to other App outputs.
- Staged filename: `howlens-app-57405bb-debug.apk`.
- Size: `15,034,933` bytes.
- SHA-256: `2b39251176178975fd0a9228da55a88903d879e7d2cc9fd4dcdb695d128abb25`.
- The private stage contains only that APK and `manifest.txt`.

## Private LAN delivery

- APK: `http://10.102.72.225:8765/howlens-app-57405bb-debug.apk`
- Manifest: `http://10.102.72.225:8765/manifest.txt`
- Stage: `/var/folders/nq/_sblkr657y90dnzcs4c0w8qh0000gn/T/howlens-app57405bb-delivery-mmnf_fga`.
- Dedicated server PID: `22008`, bound only to private address `10.102.72.225`,
  port `8765`; exec session `87763`.
- Launch command: `python3 /tmp/howlens_delivery_server_57405bb.py
  /var/folders/nq/_sblkr657y90dnzcs4c0w8qh0000gn/T/howlens-app57405bb-delivery-mmnf_fga
  10.102.72.225 8765 1200`.
- The service permits only the named APK and manifest, shuts down after two
  complete APK GET responses, and has a 20 minute timeout. Cleanup owner: this
  worker, which will stop only PID `22008` after the second receipt or timeout.
- Receipt 1/2 completed at `2026-10-09T03:49:06Z`: HTTP 200; all `15,034,933` APK bytes sent.
- Receipt 2/2 completed at `2026-10-09T03:51:24Z`: HTTP 200; all `15,034,933` APK bytes sent.
- Server printed `SERVER_STOPPED receipts=2`; process PID `22008` exited and port `8765` is confirmed closed. No recipient-side hash confirmations were provided.

## Scope and QA

- No physical device was controlled. Three-device QA remains separate and is
  still pending the local test workers.
- No public upload, credential access, product edits, or rebuild was performed.
