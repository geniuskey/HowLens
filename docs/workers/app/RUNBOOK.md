# W1-APP 실행 방법

프로젝트 루트는 `android/`이며 저장소 루트 Gradle 설정에 의존하지 않는다. Android SDK 34/build-tools 34.0.0, JDK 17, Gradle 8.9, AGP 8.7.3, Kotlin/Compose compiler 2.0.21을 사용한다. 이 작업 PC에서는 시스템 Java가 없으므로 Android Studio 내장 JBR 17을 사용했다.

```sh
cd android
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME='/Users/edwin/Library/Android/sdk'
./gradlew assembleDebug testDebugUnitTest lintDebug --console=plain
```

다른 PC에서는 JDK/SDK 경로를 조정한다. Android Studio에서 `android/`를 프로젝트로 열 수도 있다. `local.properties`, build 결과, Gradle 캐시는 Git에 넣지 않는다. APK는 `android/app/build/outputs/apk/debug/app-debug.apk`이다.

## 화면 확인

1. 앱 실행 시 기본값은 **오프라인 MOCK · 합성 UI 테스트**이다. 네트워크/AI 호출 없이 합성 결과만 반환하며 실제 사진·질문은 분석하지 않는다.
2. 장비를 선택하고 갤러리에서 JPEG/PNG를 선택하거나 카메라 앱으로 촬영한다. Android Activity Result `OpenDocument`/`TakePicture`와 FileProvider를 사용한다. CameraX 미리보기·카메라 제어는 이번 구현에 포함하지 않았다.
3. 공백 제거 후 1–2,000자 질문을 입력한다. 파일은 10 MiB 이하/20MP 이하이며 MIME 헤더와 샘플 디코딩을 확인한다. 손상·형식·크기 오류는 입력 단계에서 차단된다.
4. **추가 정보 예시** 또는 **중단 예시**를 선택하고 **MOCK 화면 확인**을 누른다. 로딩 후 관찰/경고/필수 조건/추가 정보가 나타나며 실행 단계와 이미지가 차단된다.
5. 서버 연결을 시험하려면 오프라인 스위치를 끄고 API 서버 루트 주소를 입력한다. 에뮬레이터 개발 기본값은 `http://10.0.2.2:8000/`이다. 실기기 주소는 네트워크에 맞게 설정한다. HTTP는 debug manifest에서만 허용하며 release는 Android 기본 HTTPS 정책을 유지한다. API 주소에 인증정보/쿼리/경로를 넣지 않는다.
6. API 오류는 구조화 detail 객체와 FastAPI detail 배열을 모두 표시한다. 재시도 가능한 오류에는 **다시 시도** 버튼을 제공한다. 자동 재시도는 없다. 취소 또는 입력 변경은 이전 분석을 버린다.

live guide만 실행 단계 후보가 될 수 있다. 비-guide/mock 결과, 충족되지 않은 필수 조건, 비어 있거나 10개 이상인 단계, 잘못된 근거 참조는 단계 표시 및 이미지 요청을 차단한다. 서버 결정을 승격하지 않는다. 근거 카드에는 ID/문서/버전/PDF 페이지/인쇄 페이지/절/발췌/출처가 표시된다.

## 자동 검증

```sh
# 연결된 Android 에뮬레이터 또는 기기가 필요하다.
./gradlew connectedDebugAndroidTest --console=plain
```

이 작업에서 사용한 에뮬레이터는 `Pixel_3a_API_34_extension_level_7_arm64-v8a`이다. JVM 결과는 `android/app/build/reports/tests/testDebugUnitTest/`, 계측 결과는 `android/app/build/reports/androidTests/connected/`, lint 결과는 `android/app/build/reports/lint-results-debug.html`에서 확인한다. 생성된 보고서는 Git 제외이며 실행 결과 요약은 역할 REPORT에 기록한다.

JVM 테스트는 악성 비-guide 단계/잘못된 근거/미충족 조건, 입력 경계, 두 오류 형식, MockWebServer multipart 전송, synthetic ViewModel 상태/취소를 확인한다. 계측 테스트는 실제 Compose 렌더링에서 두 MOCK 상태/차단 문구/입력 오류/출처 표시와 Android PNG 디코딩/손상 입력 차단을 확인한다. MockWebServer 응답 및 문서·이미지는 합성 테스트 자료이며 실제 분석·안전 검증 증거가 아니다.

## 네트워크 및 다음 통합

`AnalysisRepository`/immutable `AnalysisUiState`/ViewModel/Compose 경계를 사용한다. OkHttp는 연결 10초/읽기 45초/쓰기 30초/총 요청 60초 제한 및 리다이렉트/자동 재시도 비활성화를 적용한다. 질문은 trim 후 `device_id`, `question`, `photo` multipart로 전송한다. OpenAI SDK·키·앱 토큰 주입은 없다.

Health/Analysis/VisualJob/Verification의 snake_case DTO와 API client 메서드가 있다. Visual/Verification은 승인 live guide gate를 거쳐야 호출 가능하다. W1 화면은 이미지 생성·폴링·패널/전후 검증 API를 호출하지 않는다. 다음 통합에서 visual 실패/재시도 1회/텍스트 유지 및 서버 재시작 404 흐름을 실제 backend와 확인해야 한다.

사진은 메모리에 유지하고 카메라 임시 JPEG 한 개는 앱 cache에 저장한다. 사진 로딩은 ViewModel에서 수행하여 화면 회전 중에도 이어진다. 프로세스 종료 후 입력/사진 영속 복구는 구현하지 않았다. 사진만으로 정상 동작/안전을 보증하지 않는다.

## 도구 선택 출처

- [AGP 8.7 공식 호환성 표](https://developer.android.com/build/releases/agp-8-7-0-release-notes): Gradle 8.9/JDK 17/build-tools 34.0.0.
- [Compose compiler Gradle plugin 설정](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler): Kotlin 2의 Compose plugin 구성.
- [Activity Result API](https://developer.android.com/training/basics/intents/result), [TakePicture 계약](https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.TakePicture): 초기 카메라 intent 연결 근거.
- Wrapper JAR/스크립트: 공식 [Gradle v8.9.0 저장소](https://github.com/gradle/gradle/tree/v8.9.0/gradle/wrapper). JAR SHA-256 `498495120a03b9a6ab5d155f5de3c8f0d986a449153702fb80fc80e134484f17`를 [공식 checksum](https://services.gradle.org/distributions/gradle-8.9-wrapper.jar.sha256)과 대조했다. 배포 ZIP도 wrapper properties의 공식 SHA-256으로 검증한다.

## W2 live guide, visual panels, verification, and evidence-only sharing

The analysis endpoint still accepts one selected JPEG/PNG photo per request (`POST /analyses` multipart). The camera button launches Android's camera app with `TakePicture`; this build does not stream video or contain CameraX. The visual flow is available only after a validated LIVE guide: the app creates one visual job, polls while queued/running, validates the returned analysis ID/mode, exact nine indices (0–8), and every panel's approved step reference, then fetches each `/visual-assets/...` path from the same API server. Failed/malformed visual responses retain the original text and allow one deliberate user retry (two total requests). Cancel stops the OkHttp call.

Verification keeps the original selected photo as the before photo and opens a separate gallery action for the after photo. It sends only the after photo and optional user observation to the server. The UI reports observed change / issue remaining / inconclusive plus limitations and missing information; it never upgrades this to a safety, repair-success, or normal-operation guarantee.

The optional share preview is assembled from an allowlist: fixed device label, decision, mode, public evidence document ID/version/PDF+printed page, public HTTPS source URL without credentials or secret query keys, and fixed limitations. It excludes free-form observations/questions/quotes/steps, photos, analysis IDs, and device identifiers. The user reviews the preview and then chooses a recipient in Android Sharesheet; tests inspect the text only and never send it. Android's official guidance uses `ACTION_SEND`, `text/plain`, `EXTRA_TEXT`, and `Intent.createChooser`: https://developer.android.com/develop/ui/compose/sharing/send

## W3 blue redesign and CameraX input

The home screen has two tabs: **사진 분석** for selecting a still image and **카메라** for a live local preview. CameraX 1.4.2 is mounted only while the camera tab needs it. **사진 촬영** creates one JPEG in app cache; the app reads that file through the existing `PhotoLoader` and deletes the cache file after the read completes. It shows a frozen still with a question field and explicit **확인하기** action; switching tabs, canceling the picker, or retaking does not send an analysis request. There is no video stream or automatic upload.

The CameraX component requests camera permission when opened, releases its provider when it leaves composition, and reports permission/initialization failure in the camera tab with a path back to photo selection. Existing callers must not open a file until `PhotoLoader` finishes. The media picker is `OpenDocument`; physical Galaxy manual QA opened and canceled it without opening personal items. The physical camera permission was already granted on the test device, so first-grant and denied-permission/settings-return states were not physically exercised in W3.

The HowLens theme uses the approved light palette on dark-system devices too: primary `#0052FF`, white surfaces, and cool-gray backgrounds. The two tabs and sticky primary action were visually checked on API 34 emulator at 320dp and 412dp widths. Other-device large-font/landscape coverage is tracked separately by the coordinator's parallel QA lane.

The feature APK for W3 is `android/app/build/outputs/apk/debug/app-debug.apk`. Build and test command:

```sh
cd android
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
ANDROID_HOME='/Users/edwin/Library/Android/sdk' \
./gradlew assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug --console=plain
```

The W3 Compose fixtures and JVM doubles use synthetic photos/results only. The physical Galaxy capture was a local UI test and was not submitted to the backend. Before sharing device screenshots, avoid retaining any user-entered question or photo; the committed W3 Galaxy screenshots show only the empty input and live preview.

### W2 build and API doubles

```sh
cd android
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
ANDROID_HOME='/Users/edwin/Library/Android/sdk' \
./gradlew assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug --console=plain
```

The debug APK is `android/app/build/outputs/apk/debug/app-debug.apk`. JVM tests use local MockWebServer only and synthetic fixtures; they exercise nine-panel success/same-server assets, visual failure and one retry, cancellation, malformed panels, verification multipart/evidence guards, response contracts, and share allowlisting. Compose instrumentation tests use synthetic-only fixtures and cover non-guide/mock step blocking, panel order/step refs, share preview allowlist, and Android PNG loading. No AI API or real machine photo is used by these tests.

### W2 physical Galaxy evidence

On 2026-10-09 the connected Galaxy reported serial `R5KL20H60TN`, model `SM_S948N` (ADB product `m3qksx`). Final APK `adb install -r` succeeded; `MainActivity` was observed foregrounded. Gallery picker opened and selected only the public synthetic 68-byte asset at `/sdcard/Pictures/HowLens-TEST-only-upload.png`, which the app showed as 1×1; no personal image was opened and the asset was not submitted. Camera launched as `com.sec.android.app.camera/.Camera` and was closed without taking a photo. Back navigation from both external activities returned through the app/task. The offline MOCK label was visible on the actual phone screen. The app remained responsive after physical rotation to `ROTATION_270`.

The API PC's GET `/health` returned HTTP 200, `{"status":"ok","mode":"live"}`. Direct Galaxy 5G access failed with Chrome `ERR_NETWORK_CHANGED`; no device network setting was changed. A temporary Python TCP relay owned by this task listened only on `127.0.0.1:18000` and forwarded to `10.102.72.28:8000`; `adb reverse tcp:18000 tcp:18000` let Chrome on the phone display the same health JSON. The relay process was stopped, `adb reverse --remove tcp:18000` was run, the reverse list was empty, and port 18000 was confirmed free. Relay PID was not retained in the task log. No analysis POST/paid API request was made; paid attempts: 0.

The test runner on this Galaxy's Android 17 image fails before UI assertions because Espresso looks up missing `android.hardware.input.InputManager.getInstance`. This is recorded as a device test harness failure, not a passing physical UI suite. The API 34 Pixel 3a emulator ran all six Compose tests successfully. Its rotation was toggled to landscape with emulator-only `cmd window user-rotation lock 1`, app orientation was observed at ROTATION_90, and emulator rotation was released with `user-rotation free`. Separately, physical Galaxy rotation to ROTATION_270 was observed while the app remained responsive.

Screenshots are in `docs/workers/app/screenshots/`: `w2-galaxy-input.png` is the physical phone input screen; `w2-galaxy-camera-launch.png` shows the external camera app launch; `w2-api34-synthetic-non-guide.png` is an API 34 Compose screenshot from synthetic offline `needs_more_information` state, showing MOCK labeling and blocked steps. The latter is test output, not a server or AI result.


## W4 local integration host

The active implementation host uses JDK 21 to execute Gradle while compilation targets
Java/Kotlin 17. The installed Android Studio contains an older JRE and is not used.
The first Gradle run installed the standard Android 34 platform under the already
accepted SDK license; the previous `android-34-ext11` installation is preserved.

```sh
cd android
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/Users/runixs/Library/Android/sdk \
./gradlew assembleDebug testDebugUnitTest lintDebug --console=plain
```

In Settings, switch out of offline demo, enter the server root first, then manually
enter the separate demo token. The field is masked. Editing the server root clears
that token; process death also clears it. It is not an OpenAI key and must not be
placed in a URL, report, screenshot, or source file. Leave it empty for the existing
local development path. Requests and panel downloads refuse HTTP redirects.

Analysis results open a summary sheet. The guide CTA appears only for a model-gated
live guide; other results offer photo/question correction. The guide keeps explicit
local step checks across back/revisit, and confirmation is not server verification.


Final integrated validation also builds instrumentation without running a device:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/Users/runixs/Library/Android/sdk \
./gradlew assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest --console=plain
```

The Home screen offers camera capture, photo selection, product discovery, and the
retained last result. Discovery needs one photo; question and model hint are optional.
It never submits a catalog `device_id` or enables the analysis guide gate. Open a
candidate source only with the visible citation button. Offline discovery makes no
network call and explicitly requests a live server for actual search.

For delegated QA, install the final app and the matching `howlens-tests-ab20cc3.apk`
using the assigned serial only, then run the existing AndroidJUnitRunner. The updated
UI tests dismiss the new summary sheet before exercising underlying result controls.
Instrumentation assembly is not an execution pass. The first auth APK remains in
`app/build/deliveries/howlens-auth-55a8583.apk`; final Home/discovery APK is
`app/build/deliveries/howlens-integrated-ab20cc3.apk`. Compare SHA-256 with the manifest
before private delivery. Do not expose the token in logs, URLs, screenshots, or reports.
