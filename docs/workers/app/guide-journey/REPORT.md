# [DONE] Isolated guide journey

Implemented a callback-driven Compose guide journey with actual current/total steps,
progress, previous/next, user checkboxes, and explicit last-step `확인했어요`.
Warnings and document/version/page/quote evidence remain visible without expansion;
optional scenes map by `stepId` and missing/failed images do not remove text.
One filled primary action, 48dp minimum controls, wrapping labels, and a scrollable
screen support small screens and large fonts; device layout QA remains unverified.

## Scope and delivery

- Branch: `geniuskey/guide-journey`, based on `e0f0d2087786c2b442b5d7ba1a1d34bacd900be5`.
- Implementation commit: `017160a9f883b3b65c5a45741f1ad66018992da4`.
- New product files only: `GuideJourneyPane.kt`, `GuideJourneyState.kt`.
- New scoped tests: `android/app/src/test/java/kr/howlens/app/ui/GuideJourneyStateTest.kt`.
- Owned documentation/assets: this directory, `RUNBOOK.md`, `demo-photos/PROMPTS.md`,
  `demo-photos/synthetic-server-rear.png`, `demo-photos/synthetic-ups-exterior.png`.
- No MainActivity, ViewModel, shared models, Repository, Gradle, manifest, FoundationTest,
  shared contract, or other owner's code edits. No merge/rebase of App's branch.
- Public signature sent to coordinator before UI implementation; integration example in RUNBOOK.

## Executed validation

2026-10-09: disposable immutable App snapshot
`57405bb4811f67bb49ea779d883fd2937c98fd7d`, plus the three owned Kotlin files.
Source requirements: designated UI_REQUIREMENTS D/E from
`051ffdc33bb6a55c66d968467deaf6e134d62451`; public models and blue theme from App snapshot.

- `:app:testDebugUnitTest --tests kr.howlens.app.ui.GuideJourneyStateTest`: **8 passed**, 0 skipped,
  0 failures, 0 errors. Tests are explicitly synthetic, not hardware evidence.
- Coverage: two-step navigation without automatic checks, checks retained on back/revisit,
  new analysis reset with identical step IDs, non-guide/mock/malformed gate blocking,
  nine scenes mapped to two steps, missing/failed scene metadata retaining approved text,
  uncheck revoking completion, step reordering/removal preserving/pruning identity.
- `:app:assembleDebug`: **PASS**; includes Kotlin/Compose compilation.
- `git diff --cached --check`: **PASS** for the implementation commit.
- PNG dimensions/byte counts inspected: both 1448×1086; 2,094,098 and 2,106,646 bytes.
- Generated photo subjects and `SYNTHETIC DEMO` placards visually inspected: **PASS**.
- Full JVM suite was not run; coordinator's known unrelated fixture failure was not changed.
- No connected tests, ADB, physical-device or emulator commands; no device rendering claims.

## User steering and remaining integration

The user explicitly requires ImageGen-created test photos because there are no real
equipment photos and no camera-capable test environment; available internet PDFs
are demo material only. Generated two clearly marked generic synthetic equipment
exteriors with the built-in ImageGen tool, saved in this owned directory with exact prompts.
They support gallery/upload UI demos and do not establish registered hardware identity,
procedure eligibility, repair success, or safety. The coordinator was notified through
Orca orchestration, with mobile download/import and photo-selection validation routed
to the primary App/device owner.

Remaining work belongs to primary integration/QA: hook up the public component, retain
parent saveable state across screen changes, download/import demo PNGs to the mobile
test environment, validate gallery selection, and inspect small-screen/large-font UI.
The image-failure test proves text projection independence; it does not execute Android
BitmapFactory or assert on a rendered Compose screen. Local completion never changes
the server decision or supplies an AI repair/safety guarantee.
