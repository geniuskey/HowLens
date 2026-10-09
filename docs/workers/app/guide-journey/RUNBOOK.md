# Guide journey integration and validation

## Public hookup

`kr.howlens.app.ui.GuideJourneyPane` is a standalone scrollable screen inside
`HowLensTheme`. Place it in a bounded screen container, rather than inside another
vertically scrolling result column. Call the saveable helper above screen branching:

```kotlin
val journey = rememberGuideJourneyState()
// In the parent's guide screen branch:
GuideJourneyPane(
    analysis = analysis,
    state = journey.value,
    onStateChange = { journey.value = it },
    onBack = { /* parent navigates to the retained analysis result */ },
    onConfirmed = { /* parent handles the explicit local user mark */ },
    panels = state.visualPanels,
    images = state.visualImages,
    visualLoading = state.visualLoading,
    visualError = state.visualError,
    onRequestImages = if (!state.offline && analysis.canRequestVisual &&
        state.visualAttempts < 2) vm::requestVisual else null,
)
```

Supply real navigation actions in place of the comments. Supply `onAskQuestion` only
when an actual parent question flow exists; its button is absent by default.
`onConfirmed` is a local user acknowledgment, never a verification API request or
an AI success/safety decision. The parent may show a completion screen or stay here.
The final primary button requires every explicit checkbox and the last step.
Unchecking a step revokes the local mark. Movement alone never checks a step.

`GuideJourneyState.forAnalysis` resets on a new analysis ID; checked IDs and selected
step ID survive previous/next and back/revisit when the parent keeps the helper alive.
Saved state contains only IDs and a boolean. Photo bytes and analysis text are not saved.
The pane displays only gated `visibleSteps`. Scene panels are filtered by current
`stepId`, so nine scenes remain decorations for the actual 1–9 procedure steps.
The parent must pass panels/images belonging to the same analysis; existing ViewModel
clears them on a new analysis. Failed/missing image bytes leave verified text/evidence visible.

## Reproduce the scoped validation

This task used a disposable archive of immutable App `57405bb4811f67bb49ea779d883fd2937c98fd7d`
with only the three owned Kotlin additions copied in. Do not merge, rebase, or cherry-pick
the entire App branch into the data-hardening branch.

```sh
git archive 57405bb android | tar -x -C /path/to/disposable-snapshot
# Copy GuideJourneyPane.kt, GuideJourneyState.kt and GuideJourneyStateTest.kt into matching paths.
cd /path/to/disposable-snapshot/android
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
ANDROID_HOME='/Users/edwin/Library/Android/sdk' \
./gradlew :app:testDebugUnitTest --tests kr.howlens.app.ui.GuideJourneyStateTest \
  :app:assembleDebug --console=plain
```

Expected: eight tests, zero failures/errors, and `BUILD SUCCESSFUL`.
The known unrelated full JVM fixture failure is outside this component; full-suite
green status is not claimed. Device rendering/interaction is a separate App integration task.

## Demo photo selection — primary App/device owner

The user has no real device photos or camera-capable test setup. Use ImageGen synthetic
photo assets from `demo-photos/`, not a request to photograph real hardware:

1. Download `synthetic-server-rear.png` and `synthetic-ups-exterior.png` to the mobile
   test environment's Downloads/Pictures through its supported file-download/import route.
2. Open HowLens's photo selection action and select one downloaded PNG through the gallery picker.
3. Check preview, photo selection, analysis input/error handling, and the explicit synthetic/demo context.
4. Treat blocked or insufficient-evidence responses as valid; generic synthetic equipment
   does not prove a registered model or live procedure eligibility.
5. Exercise guide navigation with the approved synthetic UI fixture separately; check
   320dp width, enlarged fonts, warning readability, all touch areas, previous/next,
   back/revisit, image decode failure, and explicit local completion.

Both generated PNGs are 1448×1086 (1,572,528 pixels), approximately 2.1 MB each,
within the app's 10 MiB/20MP limits. `PROMPTS.md` records exact prompts and provenance.
This worker has not imported them into a mobile environment and ran no device commands.
