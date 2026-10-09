# App data hardening report

Date: 2026-10-09 KST  
Branch: `geniuskey/app-data-hardening`  
Implementation commit: `2b01cb41f3b1676d54e3c3a93b2c685ab7165920`

## Changes

- Visual PNG assets still have the existing 2 MiB response bound and now require valid Android `BitmapFactory` bounds, no more than 20 MP source dimensions, and a successful sampled decode capped at 4 MP before the repository returns success.
- Share previews reject HTTPS source URLs containing credential parameter keys after repeated percent decoding in either the query or fragment. Public noncredential query and anchor links remain shareable.
- Added synthetic signature-only and valid PNG instrumentation fixtures, and synthetic encoded-query, fragment-credential, and public-link unit fixtures.

## Verification

- `:app:testDebugUnitTest --tests kr.howlens.app.data.ShareRegressionTest :app:assembleDebug` — passed after final code changes.
- `:app:connectedDebugAndroidTest -Pandroid.injected.device.serial=emulator-5554 -Pandroid.testInstrumentationRunnerArguments.class=kr.howlens.app.data.RepositoryRegressionTest` — both decoder tests passed on the Pixel 3a API 34 emulator. Despite the serial property, Gradle also ran those tests on the attached SM-S948N physical phone; this was unintended and reported to the coordinator. No phone UI was operated, and no further device command will be run.
- Full `:app:testDebugUnitTest :app:assembleDebug` — failed at `FoundationTest.liveGuideVisualRequestLoadsNineOrderedPanelsAndSameServerAssets` with `TimeoutCancellationException`; the focused share tests pass.
- `git diff --check` — passed.

## Remaining

The full JVM unit suite still has the timeout above. The Android instrumentation run's unintended execution on the attached physical phone is disclosed for coordinator review. The implementation commit above is the App cherry-pick target.
