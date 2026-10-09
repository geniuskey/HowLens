# Camera capture integration contract

## Public Compose API

```kotlin
@Composable
fun CameraCapturePane(
    isActive: Boolean,
    onPhotoCaptured: (Uri) -> Unit,
    onError: (String) -> Unit,
    onChooseFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Render the pane in the camera-preview tab and set `isActive` to `false` when
that tab is hidden. The camera is lifecycle-bound while active and its owned
Preview/ImageCapture use cases are unbound when inactive or disposed. Rotation
recreates/rebinds the view through Compose and the current `LifecycleOwner`.

The capture callback receives a `file://` `Uri` to a JPEG saved in the app's
cache directory. The caller can read it with `ContentResolver.openInputStream`
or convert it to the application's upload representation; the camera module
does not upload, transcode, or retain user gallery files. A stale capture that
finishes after the pane is inactive is discarded and its temporary cache file
is deleted. Captures are written directly to disk to avoid a full-image memory
buffer. The existing image validation path must still enforce JPEG, 10 MiB,
and 20 MP limits before upload.

## App-level prerequisites

Add these dependencies to `android/app/build.gradle.kts`:

```kotlin
implementation("androidx.camera:camera-camera2:1.4.2")
implementation("androidx.camera:camera-lifecycle:1.4.2")
implementation("androidx.camera:camera-view:1.4.2")
```

CameraX 1.4.2 is the latest stable 1.4 patch line selected for this app's
`compileSdk = 34`; CameraX 1.5.3 is also stable but its release notes record a
compile SDK 35 requirement. These artifacts support min SDK 26. Ensure the
Google Maven repository is configured (the current project already uses it).

Add the runtime permission to `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

No storage permission or FileProvider path is needed for this module's
app-cache output. Keep camera hardware optional for devices without a camera.
The caller supplies the gallery-picker action; a system Photo Picker can be
used without media-storage permission.

## Official references

- [CameraX release notes](https://developer.android.com/jetpack/androidx/releases/camera)
- [CameraX architecture and lifecycle binding](https://developer.android.com/media/camera/camerax/architecture)
- [CameraX still image capture](https://developer.android.com/media/camera/camerax/take-photo)
- [Request app permissions](https://developer.android.com/training/permissions/requesting)
