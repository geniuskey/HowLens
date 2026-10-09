# Analysis summary sheet

## API

```kotlin
@Composable
fun AnalysisSummarySheet(
    analysis: Analysis,
    thumbnail: ImageBitmap? = null,
    onDismissRequest: () -> Unit,
    onOpenGuide: () -> Unit,
    onRequestMoreInformation: () -> Unit,
)
```

`Analysis` is the immutable model from App commit `57405bb`.
`onOpenGuide` is selected only when `analysis.canShowSteps` is true; otherwise
the primary action calls `onRequestMoreInformation` and displays
`사진·질문 수정`. The parent owns visibility and callback navigation.

## Behavior

- Uses a Material 3 modal bottom sheet, HowLens theme blue, readable Korean
  body text, scrollable content, and touch targets at least 48 dp high.
- Optionally displays a supplied `ImageBitmap` with a screen-reader label.
- Displays up to two nonblank observations and omits empty sections.
- Prioritizes provided warnings, missing information, and required/unmet
  preconditions before observations. It shows model text as provided and does
  not add causes, steps, or diagnosis claims.
- Labels mock content as `테스트 예시`.

## Verification

The source was compiled in a disposable archive of immutable App commit
`57405bb4811f67bb49ea779d883fd2937c98fd7d`; only this component and its scoped
unit test were copied into that archive. With the local Android SDK/JDK and
Gradle cache, `:app:testDebugUnitTest :app:assembleDebug` completed
`BUILD SUCCESSFUL`; the summary sheet regression class ran 2 tests with 0
failures, and the baseline suite ran 16 more with 0 failures. One existing
baseline `FoundationTest` emitted a `CoroutinesInternalError` stack trace to
test stderr although Gradle reported the test successful; it is outside this
component. The debug APK remains in the disposable harness and was not copied
into the repository. No device or emulator interaction was performed.
