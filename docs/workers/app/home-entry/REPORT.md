# Home entry delivery

- Task: `task_993f9b1994fc`, dispatch: `ctx_6e3ae7370112`.
- Live placement correction explicitly authorized App component work in the
  current clean `eval-foundation` checkout, preserving its evaluation commits.
- Starting HEAD: `3e52de985040005c8e230122605e2d15d0a0c8c5`.
- Product-only commit: `bd0d642f3c6a019f335289f4f7dcf003949514ca`.
- Pushed to: `origin/Runixs/home-entry-ui`.
- Reference: exact original `docs/orchestrator/references/UI_REQUIREMENTS.md`
  read from fetched main `b4889339e6eb5a81f799834a833756c88da6d0ae`.
- Native style baseline: `57405bb4811f67bb49ea779d883fd2937c98fd7d`.

## Implemented

Added only `android/app/src/main/java/kr/howlens/app/ui/HomeEntryPane.kt` to
product code. The camera banner uses the existing blue token, a recognizable
camera illustration and the short Korean CTA `지금 촬영하기`. The separate
`사진 선택` gallery action has its own image icon and accessible description.
Optional supported-equipment discovery and last-result resume only render with
real callbacks; resume also requires a nonblank title.

The pane uses existing MaterialTheme colors and typography (14sp supporting text,
22sp main heading). Primary and photo actions have a minimum 56dp height;
optional destination actions have a minimum 48dp height. Text can wrap and content
scrolls. Camera and photo have distinct action descriptions; the heading carries
heading semantics. Preview annotations cover 320dp, 412dp and 320dp at 1.5 font
scale. These annotations are authored source, not rendered visual evidence.

No MainActivity, ViewModel, navigation, data, Gradle, other UI, shared contract,
keys or network code was edited. No fake recent history/catalog/community/chat
actions were added. Product and documentation commits are separate.

## Verification

- Starting checkout was clean; source whitespace check passed.
- Disposable directory:
  `/var/folders/69/r13269yn5wn5ydgqj5c6r0qh0000gn/T/howlens-home-entry-24x2nzfg`.
- Exported only immutable baseline `android/`, then copied the new component;
  no active primary code was included.
- Initial offline `:app:compileDebugKotlin` failed during project configuration
  because required Gradle artifacts were not cached. No Kotlin compilation ran.
- Online retry resolved configuration dependencies but failed before Kotlin
  compilation because the SDK has only `android-34-ext11`, while the baseline
  requests standard `android-34`. SDK auto-download was disabled to preserve the
  sole primary installer boundary. Coordinator received an explicit blocker.
- After the primary installed standard android-34, the unchanged isolated
  `:app:compileDebugKotlin` passed: **BUILD SUCCESSFUL in 27s**, exit code 0,
  14 executed tasks. The Kotlin daemon logged a startup termination on its first
  attempt, then the build completed successfully without source diagnostics.
  The final build output is preserved in `compile.log` next to this report.
- No device/emulator commands, screenshots, instrumentation tests or live camera
  interaction were performed. Runtime accessibility remains unverified.
- No unit tests were added: a callback-only UI has no new business logic, and
  meaningful interaction coverage requires Compose rendering/instrumentation.

## Remaining integration

Primary App owns mounting the pane, camera/gallery routes, callback availability
and retaining a real result. Isolated compilation is complete; device/runtime
and rendered Preview checks remain separate from source compilation and must
not be reported as passed.
