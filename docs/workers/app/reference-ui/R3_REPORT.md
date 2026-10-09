# R3 equipment-first Home source freeze

Frozen 2026-10-09 14:18 KST for the coordinator's single build and synchronized rollout. Working-tree HEAD at report time: `670dd106ecc7f777af5c0e8cbbf9344e2b8fdc1e`; R2 live app source remains `319eda7` until that rollout.

## Delivered behavior

- Home shows the existing `InputRules.devices` catalog: Dell PowerEdge R750 (`server`), UR5e (`cobot`), APC Smart-UPS (`ups`). These replace recommended guide cards and make no new manual or support claims.
- Selecting a device stores its canonical ID and immediately opens CameraX capture. The camera displays the chosen equipment name; preview/submission use the existing selected-device state. Returning Home preserves that selection.
- HowLens is the clickable Home target; the redundant top Home text button is removed. Home's PC-only hero wording becomes 장비. The prominent central camera remains.
- Existing analysis/result safety gates, server request behavior, build-time URL configuration, and timeouts are unchanged. This change adds zero provider calls and no client research/model routing.
- The corresponding [interactive HTML mockup](../../../mockups/reference-ui/index.html) follows the same equipment selection and identifies analysis as a demo.

## Focused verification

`node docs/workers/app/reference-ui/check-equipment-r3.cjs` passed: exact three catalog choices; each immediately enters camera; equipment identity survives preview, demo analysis, and logo Home navigation; 320/390 widths have no horizontal overflow or browser errors. Screenshots [320 px](r3-home-320.png) and [390 px](r3-home-390.png) were visually inspected.

`git diff --check` passed. Added native instrumentation regression `equipmentSelectionEntersCameraAndLogoKeepsChosenDevice`; it has **not been compiled or run** because the coordinator exclusively owns Gradle and device actions for this rollout. No baseline suite was repeated. No APK was built here, so R3 APK hash/size and device validation remain with the coordinator.

## Exact changed files

Product sources and regression test:

- `android/app/src/main/java/kr/howlens/app/MainActivity.kt`
- `android/app/src/main/java/kr/howlens/app/ui/HomeEntryPane.kt`
- `android/app/src/androidTest/java/kr/howlens/app/OfflineScreenTest.kt`
- `docs/mockups/reference-ui/app.js`
- `docs/mockups/reference-ui/index.html`
- `docs/mockups/reference-ui/style.css`

Evidence:

- `docs/workers/app/reference-ui/check-equipment-r3.cjs`
- `docs/workers/app/reference-ui/r3-home-320.png`
- `docs/workers/app/reference-ui/r3-home-390.png`
- `docs/workers/app/reference-ui/R3_REPORT.md`

Android source SHA-256 at freeze:

| File | SHA-256 |
| --- | --- |
| MainActivity.kt | `5e426909d149dc8f350e8847ff6511254cc85865e74f46f218608134224c1b2e` |
| HomeEntryPane.kt | `357c9e7b866a45baa2be3d4e291225cb37ded7b3e918c2573c9348c3dd3c9159` |
| OfflineScreenTest.kt | `1287e5ac4287e79ddddd70ab5b8c054368999084524dd1a2c8f42234028c7781` |

No Git staging/commit/push, backend edits, device actions, or Gradle processes were performed by this worker. Existing AI dashboard files and user photos were preserved.
