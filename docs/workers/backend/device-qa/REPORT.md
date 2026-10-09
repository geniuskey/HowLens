# Third Android device network/API QA — active checkpoint

[BLOCKER] Exactly one USB Android device detected, serial `R3CT80CZ2MW`, ADB state `unauthorized`. Model/API and device-side network/UI cannot be queried until the user approves the phone's USB debugging RSA prompt. Baseline APK source `0e2aa94` artifact URL/hash also pending; no install or paid analysis attempted.

Lifecycle: task `task_49156d49810e`, dispatch `ctx_15c1e6bdf26a`. Ownership: this third device slot and docs/workers/backend/device-qa only; other PC phones are separately tested by their owners.

## Actual commands/results

- `command -v adb`: not on PATH; common-path check found existing `/Users/jymbook/Library/Android/sdk/platform-tools/adb`.
- Exact binary `adb version`: 1.0.41, Platform Tools37.0.1-15733141, Darwin27.0.0 arm64.
- `adb devices -l`: exactly `R3CT80CZ2MW unauthorized usb:1048576X transport_id:1`. Rechecked once: unchanged.
- No daemon initially; adb started tcp5037 normally. No adb server kill/restart/global tool install/full SDK/admin action occurred.
- Host urllib GET `http://127.0.0.1:8000/health`:200 in12ms, status=ok/mode=live.
- Host urllib GET `http://10.102.72.28:8000/health`:200 in2ms, status=ok/mode=live.
- These are host checks, **not Android reachability PASS**. Existing dedicated backend server remained untouched.
- Escalation and blocking `ask` sent to Coordinator for user USB authorization and APK URL/SHA256. No user key/env read or sharing.

## Pending sequence

1. Query authorized serial-specific model/release/API; if multiple devices are present, retain explicit serial and never infer another target.
2. Receive baseline artifact source0e2aa94 and verify provided SHA256 before `adb -s R3CT80CZ2MW install -r`; preserve app/user data, no uninstall/clear.
3. Test phone health/server connection, settings/invalid connection/offline recovery; use exact adb reverse mapping only if needed, remove only that mapping afterward.
4. One synthetic-only photo analysis through actual installed APK within existing20attempt cap; record timing/live mode/decision, no fake guide or personal gallery.
5. Retest only relevant delta for delivered blue/two-tab APK when instructed; scrub personal screen content and record exact source/hash/OS.

No personal screen captures or gallery access have occurred. No product code edits, new guides, paid API requests or server changes in this Task. Do not settle solely waiting for APK; Coordinator checkpoint required.

## APK receipt — 12:34 KST

Coordinator supplied artifact `http://10.102.72.225:8765/howlens-baseline-0e2aa94-debug.apk`, source `0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1`. Downloaded exactly once in9902ms; actual10341344bytes and SHA256 `88eb7eac202b15a32baf1ee61490083c42c8fd776fb85dd3ff2de8d4410122e2` match. Artifact preserved under ignored device-qa/artifacts; receipt sent immediately. This is the baseline, not new blue/camera UI. USB remains unauthorized at the latest check, so installation and phone/network tests are not performed yet.

## Synthetic input prepared

Coordinator authorized exact Evaluation `d263f77:docs/assets/test-prep/TEST-only-upload.png`. Archived only that file to ignored local artifacts; actual68bytes, 1x1PNG, SHA256 `431ced6916a2a21a156e38701afe55bbd7f88969fbbfc56d7fe099d47f265460`; Pillow verify + load PASS. After RSA approval, only this file may be pushed to a test-specific Download folder and selected by exact filename. No embedded fixture was claimed based on APK metadata. APK manifest confirms package kr.howlens.app/version0.1/code1, minAPI26/targetAPI34 and INTERNET permission; actual device compatibility remains unverified until model/API query.

## Additional prepared metadata

Existing aapt36.0.0 read-only badging/XML checks show HTTP cleartext enabled. No tool download/full SDK/admin install was necessary. Coordinator also explicitly requested mkdir-only PDF collection locations: `/Users/jymbook/HowLens/docs/assets/manuals/backend/` and active-worktree equivalent; directories ensured without file writes/overwrite/merge, exact paths reported. They do not register or approve evidence. Latest device authorization remains pending; RUNBOOK contains explicit-serial, data-preserving install and synthetic-only/owned-mapping constraints for continuation.
