# W1-APP 결과 — 2026-10-09

[DONE] 독립 Android Kotlin/Compose 프로젝트, 장비/사진/질문 입력, immutable ViewModel 상태, 근거 표시, multipart API client 및 안전한 offline fake를 구현했다. assembleDebug, JVM 5개 테스트, API 34 에뮬레이터 4개 계측 테스트가 모두 통과했고 비-guide/mock 단계·이미지 차단 및 실제 Compose 근거 표시를 확인했다. 실기기 카메라/갤러리, 실제 backend/AI, Visual 패널 및 전후 검증 통합은 다음 Task에서 확인해야 한다.

## 브랜치와 기반

- Worker branch: `geniuskey/feat-ui-foundation`.
- 구현 commit: `9cdd98298194d6dcff678cc81ebd2fabed363874` (`feat(android): implement safe Compose analysis foundation`).
- RUNBOOK/REPORT는 후속 문서 commit으로 전달하며 최종 delivery HEAD는 live worker_done에 포함한다.
- 초기 status는 clean이며 branch reflog의 `Created from refs/remotes/origin/main`을 확인했다. `git fetch origin main`, `git merge --ff-only origin/main`은 성공/Already up to date였다. 문서 commit `75c8eac`의 존재를 확인했다.
- 소유 범위 `android/`, `docs/workers/app/`만 변경했다. main merge/이력 재작성/다른 Worker 코드 변경은 하지 않았다.

## 구현

- SDK 34/minSdk 26, JDK 17, Gradle wrapper 8.9, AGP 8.7.3, Kotlin/Compose compiler 2.0.21의 독립 프로젝트.
- 서버/협동로봇/UPS 장비 선택, JPEG/PNG 갤러리 및 외부 카메라 intent 입력, 샘플 사진 미리보기, Unicode 문자 기준 질문 제한.
- 실제 디코딩/파일 크기/픽셀 제한, 로딩·취소·결과·오류·사용자 재시도, 입력 변경 시 이전 결과 제거.
- 기본 오프라인 fake는 `needs_more_information`/`stop`만 반환하며 화면과 fixture에 합성/mock임을 명시한다. 사진이나 질문을 실제 분석하지 않는다.
- live guide 이외의 단계/이미지 요청은 차단한다. 필수 조건 미충족, 단계 범위 오류, 누락 근거 참조도 방어적으로 차단한다. 서버 결정을 guide로 승격하지 않는다.
- 문서 ID/버전/PDF 페이지/인쇄 페이지/절/정확한 발췌/출처 URL을 표시한다. 모델 confidence나 사진만으로 안전/정상 동작을 판단하지 않는다.
- 계약의 snake_case Health/Analysis/VisualJob/Verification DTO, `device_id`/trim된 `question`/`photo` multipart client, 상대 visual asset URL guard, live guide 전용 Visual/Verification client guard.
- 두 FastAPI 오류 형식, 10/45/30/60초 연결/읽기/쓰기/총 요청 제한, 자동 재시도 및 리다이렉트 비활성화. OpenAI SDK/키/앱 키 주입 없음.
- CameraX 대신 Activity Result TakePicture를 사용했다. API 31+ cloud/device transfer 제외 규칙과 이전 Android full backup 비활성화, cache 기반 FileProvider를 설정했다.

## 실제 검증

| 확인 | 결과 | 근거/한계 |
|---|---|---|
| 최초 `assembleDebug testDebugUnitTest` | PASS | 2분 56초; JVM 5개, failure/error 0 |
| 최초 `connectedDebugAndroidTest lintDebug` | PASS | API 34 arm64 에뮬레이터 3개, failure/error 0; lint 0 errors |
| 최종 `assembleDebug testDebugUnitTest connectedDebugAndroidTest lintDebug` | PASS | 18초; JVM 5개/계측 4개, failure/error/skipped 0 |
| backup manifest 보완 후 `assembleDebug lintDebug` | PASS | 3초; Kotlin/test 코드는 최종 통합 검증 이후 변경 없음 |
| 입력/안전 gate | PASS (테스트 자료) | 빈 질문/사진 없음/알 수 없는 장비/GIF/크기/픽셀 경계/Unicode 2,000자, 악성 비-guide 단계, mock guide, 미충족 조건, 잘못된 근거, 10개 단계 차단 |
| HTTP 계약 | PASS (MockWebServer) | POST /analyses multipart field 이름, trim, filename/MIME 및 snake_case/null page 역직렬화; 비-guide/mock visual gate는 요청 전에 거절 |
| offline 실제 Compose 화면 | PASS (에뮬레이터) | 추가 정보·중단·mock 라벨·단계/이미지 차단, 빈 입력 오류, synthetic 문서 버전/페이지/발췌/출처 표시 |
| Android photo loader | PASS (합성 PNG) | 실제 Android PNG 디코딩 및 손상 데이터 차단; 실제 카메라 JPEG 아님 |
| APK cold launch | PASS (에뮬레이터) | 계측 runner가 APK를 정리한 뒤 최초 launch는 Activity 미설치 오류였음; `adb install -r` 성공 후 MainActivity COLD/Status ok, 기본 입력 화면 스크린샷 확인 |
| Wrapper 출처 | PASS | 공식 Gradle v8.9.0 JAR/스크립트 다운로드; JAR checksum을 공식 checksum과 대조, 배포 ZIP SHA-256 pin |
| `git diff --check` | PASS | 공백 오류 없음 |
| 실제 Android 기기 | 미실행 | `adb devices`에 실기기 없음; 에뮬레이터와 구분 |
| 실제 backend/AI/Visual/Verification | 미실행 | 이번 결과는 실제 AI 성공이나 실사용 안전 승인이 아님 |

Lint는 0 errors/9 warnings이다. 8개는 설치된 SDK 34와 호환되는 고정 AndroidX 버전에 대한 최신 버전 안내이며, 1개는 backup metadata 안내이다. `allowBackup=false`, `fullBackupContent=false`, API 31+ 명시적 cloud/device transfer 제외 규칙을 적용했지만 lint advisory는 남아 있어 이를 숨기거나 PASS 무경고로 보고하지 않는다. 최초 APK 패키징의 `libandroidx.graphics.path.so` strip 경고는 해당 라이브러리를 그대로 패키징했으며 빌드는 성공했다.

재현 명령/공식 선택 근거/생성 보고서 위치는 [RUNBOOK](RUNBOOK.md)에 있다. 실행된 XML 결과는 각각 `android/app/build/test-results/testDebugUnitTest/TEST-kr.howlens.app.FoundationTest.xml`, `android/app/build/outputs/androidTest-results/connected/debug/TEST-*.xml`이며 build 산출물은 Git 제외다.

## 수정 파일

- `android/.gitignore`
- `android/settings.gradle.kts`
- `android/build.gradle.kts`
- `android/gradle.properties`
- `android/gradlew`
- `android/gradlew.bat`
- `android/gradle/wrapper/gradle-wrapper.jar`
- `android/gradle/wrapper/gradle-wrapper.properties`
- `android/app/build.gradle.kts`
- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/debug/AndroidManifest.xml`
- `android/app/src/main/res/xml/photo_paths.xml`
- `android/app/src/main/res/xml/data_extraction_rules.xml`
- `android/app/src/main/res/drawable/ic_howlens.xml`
- `android/app/src/main/java/kr/howlens/app/MainActivity.kt`
- `android/app/src/main/java/kr/howlens/app/data/Models.kt`
- `android/app/src/main/java/kr/howlens/app/data/Repository.kt`
- `android/app/src/main/java/kr/howlens/app/data/PhotoLoader.kt`
- `android/app/src/main/java/kr/howlens/app/ui/AnalysisViewModel.kt`
- `android/app/src/test/java/kr/howlens/app/FoundationTest.kt`
- `android/app/src/androidTest/java/kr/howlens/app/OfflineScreenTest.kt`
- `docs/workers/app/RUNBOOK.md`
- `docs/workers/app/REPORT.md`

## 다음 의존성/제한

1. 실제 backend의 live/mock 응답, 등록 매뉴얼 근거, 413/415/422/503/504/재시작 404와 실제 네트워크 timeout을 통합 확인해야 한다. 현재 HTTP 전송 검증은 local MockWebServer다.
2. 실기기 갤러리 URI·외부 카메라 앱·JPEG·회전/프로세스 종료를 확인해야 한다. ViewModel은 사진 로딩/입력을 회전에 유지하지만 프로세스 종료 후 영속 복구는 없다.
3. Visual client/DTO는 있으나 W1 화면에서 생성·폴링·패널 렌더링을 호출하지 않는다. 실패 시 텍스트 유지/총 2회 사용자 시도/동일 서버 asset 검증을 다음 통합에서 확인해야 한다.
4. Verification client/DTO는 있으나 전후 비교 UI는 다음 Task다. 관찰 변화만 판단해야 하며 안전/정상 동작 보증은 금지한다.

Task `task_667825f4524d` / Dispatch `ctx_6459221af0a9`의 W1 구현 범위를 완료했다. 외부 채널에는 게시하지 않고 live Orca worker_done으로 보고한다.

# W2-APP live guide integration — 2026-10-09

[DONE] Wired the LIVE guide-only visual request/poll/cancel UI, nine ordered panel and approved-step validation, same-server image loading, explicit one-user-retry limit, separate after-photo verification, and observation-only limitations. Replaced blocking OkHttp `execute()` with a cancellation-aware `enqueue` bridge and capped JSON/image response bodies at 2 MiB with actionable too-large/malformed/network errors; added the evidence-only user-reviewed Sharesheet preview. No real analysis POST or paid API call was made by this App task.

## Physical / emulator / double evidence

- Physical Galaxy: serial `R5KL20H60TN`, model `SM_S948N` / product `m3qksx`; final APK `adb install -r` succeeded, and `MainActivity` was observed foregrounded. System Gallery picker and camera app both opened; only the public 68-byte synthetic PNG pushed to `/sdcard/Pictures/HowLens-TEST-only-upload.png` was selected and previewed (1×1), no personal image was opened, and no camera capture was saved. Back returned from camera/picker paths. The physical screen showed the visible offline MOCK label. The app remained responsive after rotation to `ROTATION_270`.
- Physical backend health path: App PC GET `http://10.102.72.28:8000/health` was HTTP 200, mode live. Galaxy direct 5G browser access showed `ERR_NETWORK_CHANGED`. A task-owned temporary relay bound only to `127.0.0.1:18000`, forwarded to the backend PC, and with `adb reverse tcp:18000 tcp:18000` the Galaxy browser displayed `{"status":"ok","mode":"live"}`. Relay was stopped and reverse removed; listener and reverse checks were empty. No analysis POST was made; paid attempts: 0. Relay PID was not retained in the task log.
- Physical instrumentation: `connectedDebugAndroidTest` on Galaxy Android 17 failed before UI assertions because Espresso's `InputManager.getInstance` lookup is unavailable. Do not report this as a pass.
- Emulator: API 34 Pixel 3a ran all six Compose instrumentation tests successfully via `adb -s emulator-5554 shell am instrument -w kr.howlens.app.test/androidx.test.runner.AndroidJUnitRunner`. Tests include MOCK/non-guide blocking, nine panel display/order/ref, share allowlist preview, photo loader, and input validation. Emulator-only landscape rotation was observed and released; not physical evidence.
- JVM/API doubles: `assembleDebug`, `testDebugUnitTest` (11 tests, synthetic/local MockWebServer only), `assembleDebugAndroidTest`, and `lintDebug` passed. Visual API doubles verified initial request, completed nine-panel response, relative same-server assets, failed status, one retry maximum, cancel, and malformed panel rejection while preserving the guide text. Verification and share allowlist doubles passed. All guide/document/photo fixtures are synthetic and never treated as real work authorization.
- Screenshot artifacts: [Galaxy input](screenshots/w2-galaxy-input.png), [Galaxy camera launch](screenshots/w2-galaxy-camera-launch.png), [API 34 synthetic non-guide result](screenshots/w2-api34-synthetic-non-guide.png).

## Changed files

- `android/app/src/main/java/kr/howlens/app/MainActivity.kt`
- `android/app/src/main/java/kr/howlens/app/data/Models.kt`
- `android/app/src/main/java/kr/howlens/app/data/Repository.kt`
- `android/app/src/main/java/kr/howlens/app/ui/AnalysisViewModel.kt`
- `android/app/src/test/java/kr/howlens/app/FoundationTest.kt`
- `android/app/src/androidTest/java/kr/howlens/app/OfflineScreenTest.kt`
- `docs/workers/app/RUNBOOK.md`
- `docs/workers/app/REPORT.md`
- `docs/workers/app/screenshots/w2-galaxy-input.png`
- `docs/workers/app/screenshots/w2-galaxy-camera-launch.png`
- `docs/workers/app/screenshots/w2-api34-synthetic-non-guide.png`

## Build output and remaining checks

- APK: `android/app/build/outputs/apk/debug/app-debug.apk`; version `0.1`, package `kr.howlens.app`; SHA-256 `327ca37a04afc8cecf0b3fb0c12a6f63c5797311882b353da77d8491f99327da`.
- Task branch is `geniuskey/feat-ui-foundation`, based on preserved W1 HEAD `f4f0ab6`. No main merge or other-role product code changes.
- Actual backend analysis/visual/verification endpoints remain uncalled by this app task. The synthetic test PNG was selected in the Gallery picker and shown by the app but was not submitted. Physical camera capture was not verified; Galaxy instrumentation is blocked by the Android 17 Espresso reflection incompatibility. Mock results are synthetic and not evidence of actual guide generation.
- Current camera path remains system camera intent; CameraX Preview + still ImageCapture feasibility was reported separately as 3–5 hours with no video streaming/API change. No `android/app/src/main/java/kr/howlens/app/camera/` path exists in this checkout.

# W3-APP design integration — 2026-10-09

Implemented the approved Runixs/Team Lead blue photo-first direction across the input and result surfaces, integrated the reviewed CameraX still-capture module, and kept W2 guide/panel/verification/share behavior. The W3 screen uses two input tabs, settings, a single blue primary action, frozen review before analysis, same-server visual assets, ordered contract steps, nine separate visual panels, and observation-only before/after photo comparison. Photo/camera input and draft state remain local until the user presses **확인하기**; no live API or paid request was made in this task.

## Design and integration source

- Read design handoff `Runixs/hackathon-research` commit `ab527d2` (`PRODUCT.md`, `DESIGN.md`, `SCREENS.md`, `CHECKLIST.md`) and viewed both HowLens input/result concepts plus `docs/assets/design/reference/team-lead-howlens-original.png`. Used the original team-lead reference as the visual authority: blue `#0052FF`, white/cool-gray surfaces, camera/photo first, and compact Korean labels.
- Integrated camera worker reviewed upstream commit `449cd552d5bc936565f7db12d24560f7473affe5` (includes `26837d6`) as local commits `11c25db` and `42a2357`. CameraX 1.4.2 preview/capture is local only, permission-denial/failure has a photo fallback, and a captured cache file is removed after `PhotoLoader` reads it.
- Protected W5-owned `data/Repository.kt`, `data/Models.kt`, `RepositoryRegressionTest.kt`, and `ShareRegressionTest.kt` from edits.

## Verification evidence

| Check | Result | Evidence and limits |
|---|---|---|
| `assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug` | PASS | Build successful; lint completed. JVM: `FoundationTest` 12/12 and `PhotoLimitsTest` 4/4, total 16 passing, 0 failures/errors. |
| API 34 Compose instrumentation | PASS | `OfflineScreenTest`: 7/7. Covers input/settings draft preservation, empty input, mock/non-guide action blocking, evidence details, all nine panel refs, evidence-only share preview, and synthetic PNG decode/corrupt rejection. |
| Galaxy Android 17 instrumentation | PASS | Serial `R5KL20H60TN`, model `SM_S948N`, API 37: one smoke test `emptyInputShowsValidation`, 1/1. Espresso 3.7.0 resolved and removed the earlier reflection failure on this check. |
| Galaxy manual camera and permission prompt | PARTIAL | CameraX preview opened; explicit still capture showed frozen review; **다시 촬영** returned to live preview; a synthetic draft and photo remained after switching photo/camera tabs. Revoking only HowLens CAMERA permission exposed Android's first-grant prompt; permission later reported granted and preview resumed, but the human action was not confirmed. A denial actionrequest was sent to the coordinator; denial and settings-return remain unverified. |
| 320dp and 412dp viewports | PASS | API 34 emulator screenshots saved below; no clipping in the empty input screen. Other-device large-font/lifecycle QA remains with the coordinator's parallel lane. |
| Physical gallery item selection | PASS, local synthetic input | Added only `HowLens-SYNTHETIC-NETWORK-APPLIANCE.png` (640×480, 10,691 bytes) to the public Pictures folder, searched by its HowLens filename, and selected it into the W3 review screen. Screenshot: [Galaxy synthetic device review](screenshots/w3-galaxy-synthetic-device-gallery.png). No personal image was opened, no analysis request was sent, and this illustration is synthetic rather than a photograph of real equipment. |
| Live backend/paid API | NOT RUN | All result/API fixtures are synthetic; no mock-to-live bypass was exercised. |

### Device and screenshot details

- Galaxy ADB serial `R5KL20H60TN`, model `SM_S948N`, API 37; connected as direct USB transport. Camera permission currently reports granted. The only permission mutation was revoke for the app's CAMERA permission to expose the prompt; no device account/system setting was changed. No USB relay was started, so there was no relay process or reverse mapping to clean up.
- Actual W3 screenshots: [Galaxy input](screenshots/w3-galaxy-input.png), [Galaxy CameraX preview](screenshots/w3-galaxy-camera-preview.png), [Galaxy synthetic gallery review](screenshots/w3-galaxy-synthetic-device-gallery.png), [API 34 emulator 320dp](screenshots/w3-emulator-320-input.png), and [API 34 emulator 412dp](screenshots/w3-emulator-412-input.png). Permission-prompt captures are local evidence; the first-grant screenshot shows preview after permission was granted, not the prompt itself.
- The emulator's temporary size/font/rotation overrides were restored: size override reset, font scale 1.0, user rotation free. A first retake tap sequence left the app at the launcher; reinstalling the APK and repeating capture/retake succeeded. This was not repeatable in the final manual pass.

## APK, ownership, and pending work

- APK: `android/app/build/outputs/apk/debug/app-debug.apk`; package `kr.howlens.app`, version `0.1`; 15,034,933 bytes; SHA-256 `2b39251176178975fd0a9228da55a88903d879e7d2cc9fd4dcdb695d128abb25`.
- W3 branch: `geniuskey/feat-ui-foundation`, preserving W1 and W2 commits; no main merge, no backend/common-contract edits. Camera files are within current App ownership.
- Pending: separate QA lane for large font, landscape/IME, and backend/network errors; physical denial/settings-return action (coordinator request remains unconfirmed); real backend integration. The W2 Galaxy Android 17 framework failure is historical; the new one-test smoke passed after Espresso 3.7.0. Do not treat mocked panels or mock results as generated/approved instructions.

### Follow-up checkpoint (2026-10-09)

- The exact W3 APK remains installed on Galaxy serial `R5KL20H60TN` and API 34 emulator `emulator-5554`; APK SHA-256 is `2b39251176178975fd0a9228da55a88903d879e7d2cc9fd4dcdb695d128abb25`. The 3-device delivery helper has the same accepted artifact hash; this worker directly observed the Galaxy and emulator, not the third device.
- Added a local 640×480 synthetic network appliance illustration at `test-assets/synthetic-network-appliance.png` and selected its copy from the Galaxy public Pictures folder into HowLens review. The fixture is labeled in `test-assets/README.md`; it must only be used for local UI/decoder testing, never presented as a real product photo or sent to an API.
- The Gallery search result was filtered to HowLens test filenames before selection. No personal photo was opened; no analysis/live API or paid request was made.
- A prior W3 picker pass also selected the 68-byte synthetic decoder fixture; its review screen is retained at `screenshots/w3-galaxy-synthetic-gallery.png`.
- Physical permission: CAMERA was granted before testing. App-level revoke exposed the first-grant Android prompt; afterward permission reported granted and CameraX preview was visible, but no human action confirmation was received. Denial/settings-return remain pending; the coordinator was asked to have Kim Euiyun tap **허용 안함** once on the Galaxy, with the expected result of CAMERA remaining denied and the app showing its photo fallback. This is not claimed as a human PASS.

### W4 local follow-up (2026-10-09)

- Integrated the data helper's instance-scoped PNG decoder seam through `AnalysisViewModel.repositoryFactory` and repaired only the owned `FoundationTest` fixture with a valid PNG plus injected decoder. Repository and model sources were not changed in this follow-up.
- Verification on the current branch: `:app:testDebugUnitTest` passed 22/22; `:app:assembleDebug :app:lintDebug` completed successfully. The pre-fix run reproduced one five-second timeout in `liveGuideVisualRequestLoadsNineOrderedPanelsAndSameServerAssets`; the failure was the JVM Android `BitmapFactory` stub combined with the invalid IDAT CRC fixture documented in `data-hardening/REPORT.md`.
- Built APK: `android/app/build/outputs/apk/debug/app-debug.apk`, 15,034,933 bytes, SHA-256 `e3bbedc6e1fe226259d3ce5541087c6e58ccb6f91e4bddb97d40f36cc480e6fb`. `adb install -r` succeeded on Galaxy serial `R5KL20H60TN`; a read-only pull of installed `base.apk` matched the same SHA-256. The device was locked immediately after install, so the new build's post-install UI launch was not verified and no unlock action was attempted.
- Galaxy connected Compose suite attempt: 0 tests ran. Android's test installer stopped before assertions with `INSTALL_FAILED_UPDATE_INCOMPATIBLE` because the existing target package certificate did not match the APK the test plugin attempted to install. I did not uninstall or clear HowLens data to work around the signing conflict.
- Before this rebuild, the user-provided MacBook Pro and Logitech MX Anywhere 2S camera photos were selected separately through the exact-name DocumentsUI search and displayed in HowLens frozen review. The MacBook image includes a device serial, so neither image was copied into the repository, retained on this workstation, uploaded, analyzed, or included in screenshots. No backend request was made.
- Remaining: unlock-dependent launch check for the new APK; gallery selection on the new build; three-device delivery confirmation; physical permission denial/settings-return action remains unconfirmed.

### W3 changed paths

- `android/app/build.gradle.kts`
- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/main/java/kr/howlens/app/MainActivity.kt`
- `android/app/src/main/java/kr/howlens/app/camera/CameraCapturePane.kt`
- `android/app/src/main/java/kr/howlens/app/ui/AnalysisViewModel.kt`
- `android/app/src/main/java/kr/howlens/app/ui/HowLensTheme.kt`
- `android/app/src/test/java/kr/howlens/app/FoundationTest.kt`
- `android/app/src/androidTest/java/kr/howlens/app/OfflineScreenTest.kt`
- `docs/workers/app/RUNBOOK.md`
- `docs/workers/app/REPORT.md`
- `docs/workers/app/screenshots/w3-emulator-320-input.png`
- `docs/workers/app/screenshots/w3-emulator-412-input.png`
- `docs/workers/app/screenshots/w3-galaxy-camera-preview.png`
- `docs/workers/app/screenshots/w3-galaxy-input.png`
- `docs/workers/app/screenshots/w3-galaxy-synthetic-device-gallery.png`
- `docs/workers/app/screenshots/w3-galaxy-synthetic-gallery.png`
- `docs/workers/app/screenshots/w3-galaxy-permission-first-grant.png`
- `docs/workers/app/screenshots/w3-galaxy-permission-deny-prompt.png`
- `docs/workers/app/test-assets/README.md`
- `docs/workers/app/test-assets/synthetic-network-appliance.png`
