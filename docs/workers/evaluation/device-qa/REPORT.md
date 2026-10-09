# W5 Second Device QA Report

## Result

**Status: PARTIAL; baseline plus W3 camera-permission and photo-selection delta.** The assigned Android 16 phone ran the immutable baseline and new blue/two-tab APKs. Baseline install/launch, Settings return, keyboard, font scaling, and background recovery passed; baseline rotation and gallery selection remain unresolved, and example controls showed no visible state change. On W3, the user confirmed camera permission denial and Settings return, then selected a valid synthetic PNG and saw it in the app; analysis was not run.

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
| New blue/two-tab app | PARTIAL | W3 SHA verified and installed; initial screen confirmed. User confirmed camera permission prompt, denial return, rationale, and Settings return. A valid synthetic PNG appeared selected in HowLens; analysis was not run. |

## W3 delta (new blue/two-tab APK)

- APK SHA-256: `2b39251176178975fd0a9228da55a88903d879e7d2cc9fd4dcdb695d128abb25` (15,034,933 bytes); downloaded once and hash verified.
- Installed with `adb install -r` on the assigned device; package remains `kr.howlens.app` version `0.1`.
- First screen displays the blue HowLens header and `사진 분석` / `카메라` tabs. The `사진 분석` tab is initially selected; screen also shows Demo and settings controls.
- Screenshot: [W3 initial screen](../../../assets/device-qa/w3-57405bb-home.png).
- The user denied CAMERA permission, saw the denial explanation and Settings button, opened permission settings, and returned to HowLens. Device verification found `android.permission.CAMERA: granted=false` and app-op mode `ignore`. No camera preview or capture was performed.
- Photo selection: the first test fixture was a malformed PNG (invalid IDAT CRC), which produced “손상된 사진입니다”. It was replaced with a valid 70-byte 1x1 RGBA PNG under the same synthetic filename; the user selected it and confirmed it appeared in HowLens. Analysis was not run.

## Screenshots

- [Baseline W2 home](../../../assets/device-qa/baseline-w2-home.png)
- [Baseline at font scale 1.4](../../../assets/device-qa/baseline-font-scale-1.4.png)
- [W3 blue/two-tab initial screen](../../../assets/device-qa/w3-57405bb-home.png)

## Cleanup and remaining work

- Restored device settings to their recorded baseline: font scale `1.15`, accelerometer rotation `1`, user rotation `0`.
- Removed `/sdcard/Download/qa_synthetic_1x1.png`; no gallery photo or camera capture was uploaded or retained.
- Stopped the temporary ADB server after device checks. No global PATH/SDK changes were made. W3 APK remains installed for coordinator-directed same-device follow-up; baseline and W3 APK files are retained in the task temp directory.
- ADB temp files and verified APK remain under `%TEMP%\HowLens-W5-device-qa`.
- W3 camera-permission/settings and synthetic photo-selection results above are the new-build delta tested. Analysis remains pending.
