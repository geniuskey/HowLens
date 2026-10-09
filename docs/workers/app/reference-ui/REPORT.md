# Reference UI implementation report — 2026-10-09

[DONE] Replaced the rejected home with the supplied reference composition in a clickable HTML demo and native Android, with a blue hardware-photo hero, four category actions, photo guide cards and five bottom destinations with a large central camera. HTML flow and native build/JVM/lint checks pass; live API/auth/discovery/approved-guide gates remain intact, while example guides and local AI help are explicitly labeled. Device installation, native visual/font-scale checks, camera permission/capture and live end-to-end verification remain with Galaxy QA because this Mac has no attached Android device.

## Deliverables

- HTML: `docs/mockups/reference-ui/index.html`, `style.css`, `app.js`, `assets/{pc,hardware}.jpg`.
- Screenshots: `home-320.png`, `home-390.png`, `home-desktop.png`, `confirmation-390.png` in this directory.
- APK: `/Users/runixs/HowLens/android/app/build/outputs/apk/reference-ui/howlens-reference-ui-20261009.apk`.
- SHA-256: `3e8e61c0949c753b9d751c69b94d45549876093307ccd340c337c43b29958f4a`.
- Size: **12,942,834 bytes**.
- Source: main checkout, base `4feba8ec15836b66a38a000c1d23de62352c903c` plus the explicit Android file changes below. Coordinator owns commit/push; worker performed no git mutation.

## Native changes

- `android/app/src/main/java/kr/howlens/app/MainActivity.kt`: five destination shell, immediate fullscreen CameraX entry, guide/help/profile/notification destinations and draft handoff. Existing photo review, real loading, cancel, retake, summary, approved guide journey, verification, settings and discovery preserved.
- `android/app/src/main/java/kr/howlens/app/ui/HomeEntryPane.kt`: reference home, bundled photograph, round category actions, guide thumbnails, 48dp header targets and central camera bottom bar. Removes prior canvas camera illustration.
- `android/app/src/main/java/kr/howlens/app/ui/ReferenceDestinations.kt`: searchable/filterable learning examples, persistent local favorites, local help and question handoff to real analysis, recent results, settings and product discovery access. Filters wrap for narrow/large-font displays.
- `android/app/src/main/java/kr/howlens/app/camera/CameraCapturePane.kt`: dark fullscreen preview, four blue framing corners and circular shutter; original CameraX lifecycle, permission recovery, capture limits and full-frame FIT_CENTER retained.
- `android/app/src/main/res/drawable-nodpi/reference_pc.jpg` and `reference_board.jpg`: real hardware photographs, attribution in ASSETS.md.
- `android/app/src/androidTest/java/kr/howlens/app/OfflineScreenTest.kt`: meaningful regression coverage for five destinations, home imagery, guide safety boundary, help draft handoff and immediate camera entry. Existing tests retained.

## Actual validation

- `assembleDebug`, `testDebugUnitTest`, `lintDebug`, `assembleDebugAndroidTest`: **PASS**. Final combined run: 10 seconds. Subsequent instrumentation fixture correction compiled and linted: **PASS**, 2 seconds; production APK unchanged.
- JVM: **36 tests, 0 failures, 0 errors**.
- Lint: **0 errors, 12 warnings** (11 existing dependency update notices and a ModifierParameter style warning).
- Browser: 320×780, 390×844 and 1360×1000 screenshots inspected; no horizontal overflow. Full capture-example → preview → explicitly simulated analysis → summary → 3 learning steps → checkbox-gated confirmation passes. Real JPEG import, guide search/save and local help response pass; no page errors.
- Reviewer correction batch: header targets expanded to 48px, frame replaced with four corners, desktop navigation anchored to app shell, camera navigation hidden and route focus moved, corrupt uploads decoded/rejected before preview, 20MP cap applied, completion reset on a new guide/photo and help history retained.
- `adb devices`: no devices. Native UI tests were **compiled only**, not reported as device passes. No live AI, photo capture or native screenshot claim is made.

## Limits

There is no live chat endpoint, so AI 도움 is functional local help with a real question handoff to photo analysis. Guide browsing contains preparatory learning examples and cannot approve operational instructions. Only the existing contract-approved analysis flow reveals real work steps; confirmation remains a user observation and does not certify safe/normal operation. The homepage stock photograph is illustrative and the SSD thumbnail identifies its board-reference nature. User files `content.png`, `UI_REQUIREMENTS.md` and untracked photos were preserved.
