# Camera preview worker report

## Result

Implemented `CameraCapturePane` using CameraX `PreviewView`, `ImageCapture`,
runtime camera permission, an explicit still-photo button, and a gallery-picker
callback. The component writes captures directly to app cache, filters camera
resolutions to at most 20 MP, uses JPEG quality 85, rejects output above 10
MiB, ignores stale callbacks, and unbinds its use cases when inactive or
disposed. Permission state is reactive and refreshes on permission results and
`ON_RESUME`; permanent denial offers a direct app Settings action. Visible copy
is concise Korean and does not expose platform exception details. Capture
generation invalidation clears pending state during disposal and prevents stale
callbacks from mutating a capture after re-entry.

## Changed files

- `android/app/src/main/java/kr/howlens/app/camera/CameraCapturePane.kt`
- `android/app/src/main/java/kr/howlens/app/camera/PhotoLimits.kt`
- `android/app/src/test/java/kr/howlens/app/camera/PhotoLimitsTest.kt`
- `docs/workers/app/camera/INTEGRATION.md`
- `docs/workers/app/camera/RUNBOOK.md`
- `docs/workers/app/camera/REPORT.md`

## Verification

- In a disposable copy outside the worktree, added only CameraX 1.4.2
  dependencies and `android.permission.CAMERA` for compile verification.
- `:app:testDebugUnitTest :app:assembleDebug` — **PASS**, including two camera
  photo-limit tests, two permission/capture-state regression tests, and the five
  existing foundation tests.
- `git diff --check` — **PASS**.
- No APK was installed or launched. The Galaxy SM_S948N was occupied by the App
  Worker, as required by the active device slot; device capture behavior, actual
  Settings navigation, and system permission dialog flows remain unverified on
  hardware.

## Integration requirements and limits

The coordinator received the exact public API and dependency/manifest snippets
early in the task. The module does not edit Gradle or manifest files. Its URI
is `Uri.fromFile` for the captured JPEG under app cache; app integration must
keep this file available until the downstream reader/upload finishes and must
retain its existing image decoder validation. Captures larger than 10 MiB are
discarded; the 20 MP CameraX resolution filter constrains camera output sizes.
Permission and capture callbacks have focused pure-state unit coverage, while
real runtime permission/Settings behavior remains unverified because device
control is reserved. There is no video capture, network call, upload, or camera
success claim.

## Source and branch

- Branch: `geniuskey/camera-preview`
- Base: `f4f0ab65797a345d9af4565df9bf035ba22e8681`
