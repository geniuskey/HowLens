# App 담당 실행 절차 — 연구자는 실행하지 않음

목표: 동일 앱 코드에서 Espresso 후보가 초기화 실패를 제거하는지 한 번 확인. 장비 slot은 App/Coordinator 소유다. 기존 테스트 데이터/개인 사진/계정을 삭제하지 않는다. 모든 명령은 **실제 Android checkout의 android/ 디렉터리**에서 실행하며, 이 research worktree에는 Android 모듈을 checkout하지 않는다.

## 1. 증거 확보 (2분)

App 로컬 SHA, Gradle/플러그인/SDK 설정, assertion 진입 전 최초 예외 클래스와 cause chain의 framework/Espresso 부분만 기록한다. stack 전체에 경로/기기 ID가 있으면 제거하고 필요한 frame만 공유한다. 본 연구가 받은 것은 멤버명뿐이다.

```sh
# 소유자가 할당받은 단말 serial을 로컬 환경변수에 설정한 상태.
# 명령 결과에 serial 자체를 보고서에 넣지 않는다.
: "${ANDROID_SERIAL:?Set the assigned device locally before device commands}"
adb -s "$ANDROID_SERIAL" shell getprop ro.build.version.release
adb -s "$ANDROID_SERIAL" shell getprop ro.build.version.sdk
adb -s "$ANDROID_SERIAL" shell getprop ro.build.version.codename

./gradlew :app:dependencyInsight --configuration debugAndroidTestRuntimeClasspath --dependency espresso-core
./gradlew :app:dependencyInsight --configuration debugAndroidTestRuntimeClasspath --dependency androidx.test
```

예상 구버전과 다른 resolved graph가 나오면 그것이 기준이다. 테스트 XML/logcat을 무차별 업로드하지 않는다. SDK_INT37/프리뷰 여부를 확인하되 플랫폼 display name만으로 root cause를 확정하지 않는다.

## 2. 한 줄 후보·빌드 gate (최대 5분, 1회)

App 담당이 본인 소유 브랜치에서 REPORT의 espresso-core3.7.0만 추가한다. 같은 그래프를 다시 출력하고 core/idling/runner/monitor가 무엇으로 해결되었는지 before/after를 기록한다.

```sh
./gradlew :app:dependencyInsight --configuration debugAndroidTestRuntimeClasspath --dependency espresso-core
./gradlew :app:dependencyInsight --configuration debugAndroidTestRuntimeClasspath --dependency androidx.test
./gradlew :app:checkDebugAndroidTestAarMetadata :app:assembleDebug :app:assembleDebugAndroidTest
```

build gate 실패 시 최초 compatibility 오류를 확인한다. 특정 AAR minCompileSdk 요구, Kotlin metadata, duplicate classes가 나오면 멈추고 담당자에게 전달한다. 이 후보 때문에 AGP/Gradle/Compose/target를 연쇄 변경하지 않는다. debug runtime과 test runtime의 Kotlin/coroutines도 필요할 때 dependencyInsight로 비교한다. 이 단계는 네트워크 다운로드 시간이 더 걸릴 수 있으므로 시간 제한은 운영상 중단 기준이다.

## 3. Galaxy 단일 smoke (최대 3분, 1회)

먼저 대상과 테스트 APK가 둘 다 새로 빌드된 것인지 확인한다. App이 장비 slot을 확보한 후 설치/실행한다. 다른 연결 기기는 이 명령의 대상이 아니다.

```sh
adb -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s "$ANDROID_SERIAL" shell am instrument -w -r \
  -e class 'kr.howlens.app.OfflineScreenTest#emptyInputShowsValidation' \
  kr.howlens.app.test/androidx.test.runner.AndroidJUnitRunner
```

이 class/method는 공개 f4f0ab6에 존재한다. 최신 App에서 이름이 바뀌었으면 동일 역할의 **현재 존재하는 테스트**로 선택자를 교체한다. 0 tests/잘못된 필터를 PASS로 기록하지 않는다. 디자인 변경에 따른 label assertion 실패는 framework 초기화 실패와 별개다. 목표는 InputManager lookup을 통과하고 실제 assertion에 도달하는지이며, assertion이 틀리면 테스트 기대값을 무조건 바꾸지 말고 현재 제품 요구와 비교한다.

## 4. 단일 smoke 성공한 경우만 전체 (최대 3분)

```sh
adb -s "$ANDROID_SERIAL" shell am instrument -w -r \
  -e class kr.howlens.app.OfflineScreenTest \
  kr.howlens.app.test/androidx.test.runner.AndroidJUnitRunner
```

실제 테스트 수/통과/실패/skip을 기록한다. 동일 APK/후보로 API34 회귀 시험도 App slot에서 1회 실행한다. 이전 API34 PASS는 새 후보의 회귀 PASS가 아니다. assertion 진입이 해결되어도 카메라/IME/권한 등 물리 검증은 아래 체크리스트에서 따로 수행한다.

## 중단과 전달

10–15분 또는 동일 framework 초기화 실패 재발이면 추가 버전 추측·alpha 승격·compat toggle 우회를 중단한다. 결과를 `Android17 instrumentation BLOCKED; API34 automation App-reported PASS (기존 SHA); physical UX pending/실행값`처럼 나눠 쓴다. API34 emulator와 Galaxy 수동 시험은 서로 보완하지만 Android17 자동 시험을 대체했다고 쓰지 않는다. OS 숨김 API 제한 해제/기기 초기화/자동 권한 강제 허용/queue toggle 변경은 이 runbook에 포함하지 않는다.
