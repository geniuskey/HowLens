# Third Android phone QA runbook

Assigned serial: `R3CT80CZ2MW`; exact existing adb binary: `/Users/jymbook/Library/Android/sdk/platform-tools/adb` (37.0.1). Inventory can use `devices -l`; all device operations must explicitly use `-s R3CT80CZ2MW`. Do not target another connected phone or kill adb servers.

Current gate: unauthorized; user must unlock and accept the phone's USB debugging RSA prompt for this PC. No bypass attempted. Once state=device, query only necessary properties:

```sh
/Users/jymbook/Library/Android/sdk/platform-tools/adb -s R3CT80CZ2MW shell getprop ro.product.manufacturer
/Users/jymbook/Library/Android/sdk/platform-tools/adb -s R3CT80CZ2MW shell getprop ro.product.model
/Users/jymbook/Library/Android/sdk/platform-tools/adb -s R3CT80CZ2MW shell getprop ro.build.version.release
/Users/jymbook/Library/Android/sdk/platform-tools/adb -s R3CT80CZ2MW shell getprop ro.build.version.sdk
```

Baseline artifact already downloaded ONCE and preserved in ignored `artifacts/howlens-baseline-0e2aa94-debug.apk`. Source0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1, expected10341344bytes, SHA25688eb7eac202b15a32baf1ee61490083c42c8fd776fb85dd3ff2de8d4410122e2; checked actual values match. Package kr.howlens.app, launch kr.howlens.app.MainActivity, version0.1/code1, minAPI26/targetAPI34; manifest allows HTTP cleartext. User device compatibility remains pending.

Only after authorization and hash verification:

```sh
/Users/jymbook/Library/Android/sdk/platform-tools/adb -s R3CT80CZ2MW install -r docs/workers/backend/device-qa/artifacts/howlens-baseline-0e2aa94-debug.apk
/Users/jymbook/Library/Android/sdk/platform-tools/adb -s R3CT80CZ2MW shell am start -n kr.howlens.app/.MainActivity
```

Never uninstall or clear existing app/user data. Signature mismatch is a blocker, not permission to uninstall. Preserve existing settings before any test change. UI/network checks must use only the test app; no personal gallery photos or unrelated app content. Capture screenshots only after controlled app/test-only screens, and redact personal information before durable delivery.

Synthetic fixture: Coordinator authorized exact `d263f77:docs/assets/test-prep/TEST-only-upload.png`, staged in ignored artifacts. Actual68bytes, 1x1PNG, SHA256431ced6916a2a21a156e38701afe55bbd7f88969fbbfc56d7fe099d47f265460, Pillow verify/load PASS. Push only that file to a test-specific Download directory after device authorization and select the exact filename; no embedded fixture or personal gallery upload is assumed.

API endpoint: http://10.102.72.28:8000. Existing server PID74518 / Orca term_bde18df8-bc41-4a7f-bd14-2742207763a7 must stay running untouched. Host health is200/live; this does not prove device reachability. Test app health/URL errors/recovery before exactly one synthetic analysis. Health is free; one actual analysis is within existing20attempt integration cap, with no automatic paid retry or fake guide claims. Record elapsed time, mode/decision/error and source/hash/OS.

If LAN fails and adb reverse is required, first list mappings with explicit serial. Add only an unused QA-specific port mapping, record it, use that loopback port in app settings, and remove only the exact owned mapping after tests. Do not remove-all, change unrelated mappings, disable global radios or kill other services. Offline recovery can use controlled endpoint loss rather than interrupting personal networking.

User manual collection directories were created by separate exact Coordinator follow-up (mkdir only; no file overwrite):
- /Users/jymbook/HowLens/docs/assets/manuals/backend/
- /Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation/docs/assets/manuals/backend/

These are PDF/source.txt collection paths only, not automatic evidence approval. New blue/two-tab APK is a later source/hash and gets only relevant delta retesting after delivery; baseline must not be labeled as that UI. Do not settle on APK wait without Coordinator checkpoint.
