# Reference UI runbook

Open `docs/mockups/reference-ui/index.html` directly or serve its directory over localhost. All UI scripts, styles, and hardware photographs are bundled; no network is needed for the demo. Camera permission is requested only after opening the camera screen. If unavailable, use photo import or the explicitly named example photo. Analysis, local help, guide and confirmation screens are marked as examples; photos are not transmitted.

## Local browser verification

The checked-in scripts use the Playwright installation and Google Chrome available on this Mac; change their `require`/`executablePath` for another host.

```sh
node docs/workers/app/reference-ui/check-html.cjs
node docs/workers/app/reference-ui/check-regressions.cjs
```

The first script creates the 320/390/desktop screenshots and validates the full flow, confirmation gate, saved guides, search, help and actual JPEG file import. The second covers reviewer regressions: 48px header targets, fullscreen keyboard isolation, corrupt PNG rejection and per-guide completion reset.

## Android verification and QA

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/Users/runixs/Library/Android/sdk \
android/gradlew -p android assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
```

Install `android/app/build/outputs/apk/reference-ui/howlens-reference-ui-20261009.apk` on the QA phone. Verify Home at 320dp and increased font scale; tap both hero and center-camera actions; check immediate CameraX preview, permission denial/settings recovery, gallery fallback, capture, retake, question entry and real analysis. Verify guides/help/profile navigation and existing auth/discovery/stop/needs-more-information/approved-guide gates. Live calls require the coordinator's configured endpoint and token, which are not included in this artifact.

The instrumentation APK is `android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`. Run connected tests only with the intended device attached. This host had no attached device; instrumentation tests were compiled, not run, and native screenshots must be collected on the Galaxy QA host.
