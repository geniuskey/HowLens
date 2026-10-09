# W5 Second Device QA Report

## Result

**Status: PENDING APK delivery.** The authorized QA phone is attached and identified, but the supplied baseline App SHA currently has no APK on this host. I recorded read-only device inventory and display settings; no app has been installed and no app UI scenarios have run.

## Host and device inventory

- Date: 2026-10-09 (Asia/Seoul)
- Worktree branch / source commit: `device-qa` / `63ead54`
- Java: OpenJDK runtime 18.0.1.1 available (`JAVA_HOME` set)
- Gradle: unavailable in PATH; no full Android SDK installed
- ADB source: official standalone Android SDK Platform-Tools for Windows, extracted to `%TEMP%\HowLens-W5-device-qa`; no PATH or global SDK changes
- Platform Tools: `adb` 37.0.1; ZIP SHA-256 `45F4D63113E895EBDE0C90F194099A4676B6AC653BD28D54314A9E022BBC1A99`
- Authorized attached device: serial `R3CY70W172M`, model `SM-S938N`, Android 16 / API 36 (`samsung/pa3qksx/pa3q:16/BP4A.251205.006/S938NKSSCCZH2_OKRCCZH2:user/release-keys`); `adb devices -l` state was `device`
- ADB display baseline, read before app testing: font scale `1.15`; accelerometer rotation `1`; user rotation `0`; physical display `1080x2340`, density `450`
- Network connectivity probe: passed (TCP connectivity probe to 8.8.8.8:53)
- ADB server: stopped after each read-only inventory pass; no device settings changed
- Personal gallery, camera, and paid AI: not accessed

## App verification

- Baseline source App SHA supplied by coordinator: `0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1`
- APK URL and hash: pending from coordinator's delivery helper; package/build SHA is not yet observed
- Install and launch: PENDING
- Permissions / denial / return from settings: PENDING
- Rotation / keyboard / large-font behavior: PENDING
- Background and recovery: PENDING
- Synthetic 1x1 PNG selection, mock/non-guide, back/cancel, readable UI: PENDING
- New blue/two-tab app: NOT TESTED; coordinator says to repeat only the delta when its SHA is available
- Screenshots: none

No test result is claimed until the relevant app build is installed and the scenario is run. If app settings are changed, restore font scale to `1.15`, accelerometer rotation to `1`, and user rotation to `0` before cleanup.

## Next dependency

Await the baseline APK URL/hash from the coordinator helper, then install only that immutable build and run the assigned phone scenarios. Later, repeat only the new blue/two-tab delta against the exact supplied SHA. Do not access the excluded AppPC phone.
