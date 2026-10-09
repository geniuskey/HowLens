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

## Updated device inventory

- ADB source: official Android SDK Platform-Tools for Windows, downloaded to `%TEMP%\HowLens-W5-device-qa` (standalone; no PATH or global SDK changes)
- Platform Tools: `adb` 37.0.1; downloaded ZIP SHA-256 `45F4D63113E895EBDE0C90F194099A4676B6AC653BD28D54314A9E022BBC1A99`
- Authorized connected device: serial `R3CY70W172M`, model `SM-S938N`, Android 16 / API 36 (`samsung/pa3qksx/pa3q:16/BP4A.251205.006/S938NKSSCCZH2_OKRCCZH2:user/release-keys`)
- Device access: `adb devices -l` reported state `device`; model/OS were read using read-only `getprop` queries. The temporary ADB server was stopped after inventory.
- Baseline source App SHA supplied by coordinator: `0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1`; APK delivery is pending, so package/build SHA is not yet observed and install/launch/UI checks remain pending.
