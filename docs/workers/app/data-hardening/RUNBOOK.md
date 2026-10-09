# App data hardening runbook

Use the Android Studio bundled JDK and installed SDK for this workspace:

```sh
ANDROID_HOME='/Users/edwin/Library/Android/sdk' \
PATH='/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin':$PATH \
./gradlew :app:testDebugUnitTest --tests kr.howlens.app.data.ShareRegressionTest :app:assembleDebug
```

The JVM `RepositoryRegressionTest` injects a per-instance decoder for the MockWebServer asset path; it does not replace the repository's Android production default. The Android instrumentation `RepositoryRegressionTest` under `src/androidTest` exercises the real platform `BitmapFactory` using a generated PNG fixture.

Run instrumentation only in an isolated emulator environment. Gradle's connected test task may run on every attached device, even when `-Pandroid.injected.device.serial` is set. The earlier unintended run and its exact command/device effects are preserved in `REPORT.md`.
