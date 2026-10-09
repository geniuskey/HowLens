# W5 Second Device QA Report

## Result

**Status: PENDING / BLOCKED by host inventory.** No Android device session was started. `adb` and `fastboot` are unavailable in PATH and the standard Android SDK locations checked on this host; `ANDROID_HOME` and `ANDROID_SDK_ROOT` are unset. The user reported an Android device, but this host cannot verify device attachment, model, or OS, so no device state is claimed.

## Inventory

- Date: 2026-10-09 (Asia/Seoul)
- Worktree branch / source commit: `device-qa` / `63ead54`
- Java: OpenJDK runtime 18.0.1.1 available (`JAVA_HOME` set)
- Android SDK / adb: unavailable; standard `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe` and `C:\Android\platform-tools\adb.exe` paths absent
- Gradle: unavailable in PATH
- Network connectivity probe: passed (TCP connectivity probe to 8.8.8.8:53)
- Device model / OS / authorization: unobservable because `adb` is unavailable
- App package, version, APK/build SHA: not installed or observed
- Screenshots: none
- Settings changed / ADB cleanup: none; no device session was opened
- Personal gallery, camera, and paid AI: not accessed

## Checks

| Check | Result | Evidence |
|---|---|---|
| Host device/toolchain inventory | PASS | PowerShell command discovery and standard SDK path checks on this host |
| Android device identification and authorization | PENDING | Cannot run `adb devices -l`; device attachment/model/OS remain unknown |
| Baseline W2 install and launch | PENDING | No device or APK available on this host |
| Synthetic 1x1 PNG selection, mock/non-guide, back/cancel, readable UI | PENDING | No device session |
| New two-tab App W3 validation | NOT TESTED | New build SHA was not supplied or observed; do not infer coverage |

## Next dependency

Coordinator was asked to provide an APK delivery path or restore `adb`/device access and share the App SHA if the baseline has finished. No Android SDK setup was attempted, in keeping with the task's 10-minute setup limit.
