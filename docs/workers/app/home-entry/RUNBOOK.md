# Home entry integration

Use product-only commit `bd0d642f3c6a019f335289f4f7dcf003949514ca` from
`origin/Runixs/home-entry-ui`. Cherry-pick that commit rather than merging this
branch, which preserves pre-existing evaluation commits.

```kotlin
@Composable
fun HomeEntryPane(
    onOpenCamera: () -> Unit,
    onChoosePhoto: () -> Unit,
    modifier: Modifier = Modifier,
    onDiscoverProducts: (() -> Unit)? = null,
    onResumeLastResult: (() -> Unit)? = null,
    lastResultTitle: String? = null,
    heroImage: Painter? = null,
    heroImageDescription: String? = null,
    enabled: Boolean = true,
)
```

The primary App owner mounts the pane in the existing Scaffold content insets.
The pane scrolls its own content; do not wrap it in another vertical scroller.
The host retains photo/camera tabs, navigation, permission handling and errors.
Connect camera to the camera destination and photo selection to the existing
gallery launcher. Set `enabled = false` while the host cannot accept new input.

Only provide `onDiscoverProducts` for a working supported-equipment destination.
Only provide `onResumeLastResult` and a short, nonblank `lastResultTitle` when an
actual retained result can be opened. Missing callbacks hide their sections.
No result, guide catalog or history is created by this component.

`heroImage` accepts an already available Painter with an optional description;
the pane does not decode files or load network images. Omitting it displays the
native camera illustration. All colors and text styles derive from the existing
HowLens MaterialTheme, with no new dependencies.

## Isolated verification

Export immutable `57405bb4811f67bb49ea779d883fd2937c98fd7d`'s `android/`
directory to a disposable directory, copy only `HomeEntryPane.kt` into its UI
package, and put the local SDK path in the disposable `local.properties`.
Do not change the active primary checkout or its Gradle files.

On this host the usable JDK is
`/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`; the dispatch's
original Android Studio JBR path does not exist. From the disposable `android/`:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/Users/runixs/Library/Android/sdk \
./gradlew :app:compileDebugKotlin -Pandroid.builder.sdkDownload=false --console=plain
```

SDK installation is coordinated by the primary App worker; this helper does not
install platforms. Standard `platforms;android-34` is needed by the unchanged
baseline. `android-34-ext11` alone does not satisfy it.

Android Studio Preview entries are `Home 320dp`, `Home 412dp`, and
`Home 320dp large text` (font scale 1.5). Rendering and TalkBack interaction need
separate verification; annotation declarations do not prove screenshots or
runtime behavior. No device/emulator commands are needed to inspect Preview.
