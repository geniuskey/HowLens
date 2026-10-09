# App data hardening runbook

Use the Android Studio bundled JDK and installed SDK for this workspace:

```sh
ANDROID_HOME='/Users/edwin/Library/Android/sdk' \
PATH='/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin':$PATH \
./gradlew :app:testDebugUnitTest --tests kr.howlens.app.data.ShareRegressionTest :app:assembleDebug
```

`RepositoryRegressionTest` is an Android instrumentation test because it must exercise the platform `BitmapFactory`; its PNG bytes are generated locally by the test. Run it only in an isolated emulator environment. Gradle's connected test task may run on every attached device, even when `-Pandroid.injected.device.serial` is set.
