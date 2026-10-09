# App data hardening report

Date: 2026-10-09 KST  
Branch: `geniuskey/app-data-hardening`  
Previous implementation commit: `2b01cb41f3b1676d54e3c3a93b2c685ab7165920`

## Confirmed cause of the JVM timeout

The focused reproduction was run from `android/`:

```sh
ANDROID_HOME='/Users/edwin/Library/Android/sdk' \
PATH='/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin':$PATH \
./gradlew :app:testDebugUnitTest --tests 'kr.howlens.app.FoundationTest.liveGuideVisualRequestLoadsNineOrderedPanelsAndSameServerAssets' --info
```

It failed after 5 seconds in `FoundationTest.awaitState` at line 144 while waiting for nine visual images. An isolated JVM probe against the installed Android SDK jar showed that constructing `android.graphics.BitmapFactory.Options` throws `java.lang.RuntimeException: Stub!` from `BitmapFactory.java:51`; this is the mockable `android.jar` behavior in local JVM tests, before the image fixture can be decoded.

The test's existing Base64 fixture is also malformed independently of that JVM stub. A standard-library PNG chunk/CRC check found a 1×1 image with a valid IHDR and IEND, but its IDAT CRC is stored as `efbf9747` while the calculated CRC is `efa2a75b`. The compressed IDAT payload itself decompresses to three bytes. Thus the test needs both a valid PNG fixture and an injected JVM decoder; changing only one would not make the Android `BitmapFactory` path valid in this JVM test.

## Changes in this follow-up

- Added an internal `PngAssetDecoder` dependency scoped to each `HttpAnalysisRepository` instance. The public repository constructor still defaults to `PngAssetValidator`, which performs the real bounded Android `BitmapFactory` decode; the seam is not global or mutable.
- Added a JVM MockWebServer regression for the repository asset path using an injected decoder. The real platform decoder regression remains under `androidTest`.
- No `FoundationTest.kt`, UI/ViewModel, Gradle, or device files were changed.

## FoundationTest integration patch for the App owner

The current `AnalysisViewModel` has no repository factory injection point, so the existing visual integration test cannot use the new per-instance decoder seam. The App owner can integrate the seam while editing that file for the separate ViewModel regressions. Minimal ViewModel factory wiring:

```diff
-class AnalysisViewModel : ViewModel() {
+class AnalysisViewModel(
+    private val repositoryFactory: (String) -> HttpAnalysisRepository = { url -> HttpAnalysisRepository(url) }
+) : ViewModel() {
...
-                else HttpAnalysisRepository(input.baseUrl.trim())
+                else repositoryFactory(input.baseUrl.trim())
...
-                val repo = HttpAnalysisRepository(input.baseUrl.trim())
+                val repo = repositoryFactory(input.baseUrl.trim())
...
-                val result = HttpAnalysisRepository(input.baseUrl.trim()).verify(analysis, photo, input.confirmation)
+                val result = repositoryFactory(input.baseUrl.trim()).verify(analysis, photo, input.confirmation)
```

Then update only the fixture and ViewModel construction in `FoundationTest.liveGuideVisualRequestLoadsNineOrderedPanelsAndSameServerAssets`:

```diff
-            val png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/l0cAAAAASUVORK5CYII=")
+            val png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR4nGP4DwQACfsD/fteaysAAAAASUVORK5CYII=")
...
-            val vm = AnalysisViewModel()
+            val vm = AnalysisViewModel(repositoryFactory = { url ->
+                HttpAnalysisRepository(url, PngAssetDecoder { bytes -> assertArrayEquals(png, bytes) })
+            })
```

`FoundationTest` already imports `org.junit.Assert.*`, so the proposed assertion needs no import change. The App owner must apply and test this integration; no edits to `FoundationTest` were authorized in this Dispatch.

## Verification

- `:app:testDebugUnitTest --tests kr.howlens.app.data.RepositoryRegressionTest --tests kr.howlens.app.data.ShareRegressionTest :app:assembleDebug` — passed after the final code/test changes; this covers both focused JVM classes and APK assembly.
- Full `:app:testDebugUnitTest` — 15 tests completed, with the same `FoundationTest.liveGuideVisualRequestLoadsNineOrderedPanelsAndSameServerAssets` five-second `TimeoutCancellationException`. The patch above requires App-owner ViewModel/test integration before a full-suite pass is possible.
- `git diff --check` — passed after the final code/test changes.
- No connected test, `adb`, emulator, or device command was run in this follow-up.

## Prior connected-test command and device effects (from the previous Dispatch)

Working directory: `android/`. Exact command:

```sh
ANDROID_HOME='/Users/edwin/Library/Android/sdk' \
PATH='/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin':$PATH \
./gradlew :app:connectedDebugAndroidTest -Pandroid.injected.device.serial=emulator-5554 -Pandroid.testInstrumentationRunnerArguments.class=kr.howlens.app.data.RepositoryRegressionTest
```

Gradle output was:

```text
Starting 2 tests on Pixel_3a_API_34_extension_level_7_arm64-v8a(AVD) - 14
Starting 2 tests on SM-S948N - 17
Finished 2 tests on SM-S948N - 17
Finished 2 tests on Pixel_3a_API_34_extension_level_7_arm64-v8a(AVD) - 14
BUILD SUCCESSFUL in 9s
```

The two `RepositoryRegressionTest` instrumentation methods ran on both devices; both passed on both. Gradle's connected test task installed/launched its test target on the attached SM-S948N as well as the emulator despite the serial property. App/test APK cleanup state was not inspected, and this follow-up ran no device commands. The coordinator was notified by escalation in the previous Dispatch.

## Remaining

The JVM decoder seam and focused repository tests are committed in this follow-up. Full JVM acceptance remains blocked on App-owner integration of `AnalysisViewModel.repositoryFactory` and the corrected `FoundationTest` fixture, then rerunning `:app:testDebugUnitTest`.
