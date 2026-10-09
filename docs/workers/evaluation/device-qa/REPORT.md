# W5 Second Device QA Report

## Result

**Status: PARTIAL; baseline-only.** The authorized Android 16 phone ran the immutable baseline APK. Core install/launch, Settings return, keyboard, font scaling, and background recovery passed; rotation, gallery selection, and example controls have issues or remain unverified. This is the old W2 single-screen UI, so no new blue/two-tab feature coverage is claimed.

## Build and device

- Date: 2026-10-09 (Asia/Seoul)
- Worktree branch / report commit: `device-qa` / see branch history
- APK package/version: `kr.howlens.app`, version `0.1` (`versionCode=1`, `targetSdk=34`)
- Source App SHA: `0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1`
- APK SHA-256: `88eb7eac202b15a32baf1ee61490083c42c8fd776fb85dd3ff2de8d4410122e` (10,341,344 bytes)
- Device: authorized serial `R3CY70W172M`, Samsung `SM-S938N`, Android 16 / API 36 (`samsung/pa3qksx/pa3q:16/BP4A.251205.006/S938NKSSCCZH2_OKRCCZH2:user/release-keys`)
- Java: OpenJDK 18.0.1.1; Gradle and full Android SDK unavailable
- ADB: standalone Platform Tools `37.0.1` from the [official Android release page](https://developer.android.com/tools/releases/platform-tools), extracted in `%TEMP%\HowLens-W5-device-qa`; ZIP SHA-256 `45F4D63113E895EBDE0C90F194099A4676B6AC653BD28D54314A9E022BBC1A99`

## Results

| Scenario | Result | Notes |
|---|---|---|
| Install and launch | PASS | APK hash verified before install; `MainActivity` launched. UI is the old W2 single-screen form. |
| Permission denial / app permissions | NOT APPLICABLE | Android App Info reports “Requested permissions: none.” HowLens has no runtime permission prompt to deny. |
| Camera route | PASS with limitation | Camera button launched the external Samsung Camera; backed out without shutter or save. App camera app-op remained `ignore`. |
| Settings return | PASS | Opened HowLens App Info and returned with Back to `MainActivity`. |
| Keyboard | PASS | Entered synthetic `qa_test`; Back dismissed keyboard and retained text. No personal text was entered. |
| Large font | PASS | At temporary font scale `1.4`, text wrapped and form remained vertically scrollable/readable. Screenshot retained below. |
| Rotation | FAIL / unsupported in this run | With auto-rotate off and `user_rotation=1`, app remained portrait (`uiautomator rotation=0`). |
| Background and recovery | PASS | Home/background then relaunch returned to `MainActivity`; force-stop and cold relaunch also succeeded. |
| Gallery with synthetic PNG | FAIL / incomplete | Added only `qa_synthetic_1x1.png` (68-byte synthetic image) to Downloads. DocumentsUI opened; selecting its exact entry presented Android `ResolverActivity` instead of returning the image to HowLens. Back canceled to `MainActivity`; no photo was uploaded. The picker also exposed a recent-items list; no other item was opened or selected. Synthetic file was removed afterward. |
| Offline mock / non-guide | PARTIAL | Offline MOCK was visibly enabled. Empty submit showed “question must be 1–2,000 characters”; with synthetic question and no attached photo it showed “Select a JPEG or PNG photo.” No API call occurred. The “추가 정보 예시” and “중단 예시” controls showed no visible state change when tapped, so guide/non-guide result views remain unverified. |
| New blue/two-tab app | NOT TESTED | New build SHA not supplied in this dispatch. |

## Screenshots

- [Baseline W2 home](../../../assets/device-qa/baseline-w2-home.png)
- [Baseline at font scale 1.4](../../../assets/device-qa/baseline-font-scale-1.4.png)

## Cleanup and remaining work

- Restored device settings to their recorded baseline: font scale `1.15`, accelerometer rotation `1`, user rotation `0`.
- Removed `/sdcard/Download/qa_synthetic_1x1.png`; no gallery photo or camera capture was uploaded or retained.
- Stopped the temporary ADB server after device checks. No global PATH/SDK changes were made. Baseline APK remains installed for coordinator-directed same-device follow-up.
- ADB temp files and verified APK remain under `%TEMP%\HowLens-W5-device-qa`.
- Re-run only the delta against the exact new blue/two-tab APK SHA if supplied; current results cover baseline UI only.