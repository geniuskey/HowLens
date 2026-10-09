# W1 App / Visual integration review

Status: pending; 2026-10-09. No main product integration approved or performed.

## Concrete branches and evidence

| Item | Delivery | Coordinator verification |
|---|---|---|
| App | `geniuskey/feat-ui-foundation` / `f4f0ab65797a345d9af4565df9bf035ba22e8681` | fetched; diff from `75c8eac`: 23 files, 1289 additions; only android and own docs; diff --check clean |
| Visual | `feat-visual-foundation` / `ed04a43f87d542622527efcbf383151c702d796e` | fetched; service/splitter read; 8 tests rerun successfully in isolated archive snapshot |

App's implementation commit is `9cdd98298194d6dcff678cc81ebd2fabed363874`.
Visual's implementation commit is `bb92b7518a94e02c9bf64fa222b3d76306cb1977`;
the delivery HEAD adds the timestamped synthetic split mockup.

Coordinator read original App build XML directly on the App PC:

- JVM `FoundationTest`: 5 tests, failures/errors/skipped 0; timestamp 2026-10-09T02:06:13.
- API 34 emulator `OfflineScreenTest`: 4 tests, failures/errors/skipped 0;
  timestamp 2026-10-09T02:06:27.
- lint XML: 9 Warning issues, no Error issues.
- assembleDebug and APK cold launch remain Worker-reported; not independently rerun.

Visual independent rerun: Python 3.14.2, Pillow 12.3.0, 8 tests PASS in 0.098s.
One Pillow getdata deprecation warning. Initial system Python attempt failed because
Pillow was absent; the passing rerun used a temporary venv with declared dependencies.
No repository product code was changed by this verification.

## Behavior and remaining limits

App implements input, decoding limits, source citations, explicit offline synthetic
results, HTTP DTO/client, and defensive live-guide step gating. Offline mode does not
perform photo analysis. Visual/verification clients exist but their UI flows are not
wired. Real camera JPEG/rotation/process-death and real backend/AI integration are untested.

The camera implementation uses ActivityResultContracts.TakePicture and an external
camera app. App TASK.md explicitly permits an initial camera intent when CameraX is
excessive; this is an authorized implementation choice. Physical-device validation
remains necessary, but no separate camera-choice approval is required.

App cancel currently cancels the coroutine Job but the blocking OkHttp execute call
has no explicit Call.cancel bridge. UI cancellation tests exercise the fake repository;
network abort behavior is not proven. Unbounded response.body.string and unvalidated
VisualJob panel semantics should be reviewed before live integration. These observations
are static review findings, not executed failure reproductions.

Visual guards non-guide/mock inputs and validates PNG size/decoding/grid geometry.
Its provider is unconfigured, and successful splitting does not establish scene-to-step
semantic correctness. No real image generation was performed. The user-requested
timestamped mockup is synthetic color-grid evidence only.

## Official precedents checked before a decision

Accessed 2026-10-09:

- [Android TakePicture contract](https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.TakePicture)
  documents the Activity Result camera contract. This supports the mechanism, not
  proof on our devices; the project Task independently permits this mechanism.
- [Android photo capture guide](https://developer.android.com/media/camera/camera-deprecated/photobasics)
  documents delegation to an external camera application and FileProvider URIs.
  The page is in deprecated-camera guidance and recommends CameraX/Camera2 for camera
  APIs. Our target is SDK 34/minSdk 26; no device execution was added by this research.

Do not request a blanket main merge yet: Backend/Evaluation are still active and
the remaining physical-device and live integration gaps must be included in the consolidated
human review. No approval is inferred from Worker success or these documents.
