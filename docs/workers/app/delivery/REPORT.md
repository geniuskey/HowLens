# Baseline APK delivery recovery

## Artifact

- Source: immutable App commit `0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1`
  (`geniuskey/feat-ui-foundation`), extracted with `git archive` outside both
  active worktrees.
- Build: `./gradlew --no-daemon :app:assembleDebug` — **BUILD SUCCESSFUL**.
- Size: `10,341,344` bytes.
- SHA-256: `88eb7eac202b15a32baf1ee61490083c42c8fd776fb85dd3ff2de8d4410122e`.
- Delivery stage contains only the APK and `manifest.txt`.

## Private LAN service

- APK: `http://10.102.72.225:8765/howlens-baseline-0e2aa94-debug.apk`
- Manifest: `http://10.102.72.225:8765/manifest.txt`
- Dedicated server PID: `98131` (exec session `87904`), bound only to private address `10.102.72.225`,
  port `8765`.
- Launch command: `python3 /tmp/howlens_delivery_server_task90.py
  /var/folders/nq/_sblkr657y90dnzcs4c0w8qh0000gn/T/howlens-baseline-delivery-zio6jmlx
  10.102.72.225 8765 1200`.
- The server serves only the staged APK and manifest, stops accepting requests
  after two complete APK responses, and has a 20 minute hard timeout. Cleanup owner: this worker;
  if required before auto-exit, stop only PID `98131`.
- Receipt `1/2` completed at `2026-10-09T03:34:58Z`: HTTP 200 and all
  `10,341,344` APK bytes sent; awaiting receipt `2/2` before service shutdown.
- Receipt `2/2`: HTTP 200 and all `10,341,344` APK bytes sent. The server
  printed `SERVER_STOPPED receipts=2`, then this worker sent `SIGTERM` to PID
  `98131` to clear the still-pending 20 minute timer; process and listener are
  confirmed stopped.
- Backend independently confirmed receipt `1/2` had the expected SHA-256. No
  hash confirmation was received for the second client.

No physical device was controlled, no public upload was made, and no active
App worktree outputs or credentials were accessed. The private server is now
stopped after two complete downloads.
