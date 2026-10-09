# [DONE] Android 17 Espresso 실패: 최소 테스트 의존성 후보

확인일 2026-10-09 KST. W4 연구, 브랜치 Runixs/hackathon-research. **App 담당의 첫 후보는 `androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")` 추가다.** 제품 Compose BOM·Kotlin·AGP·compile/target SDK를 함께 올리지 않는다. 이 후보는 공식 수정과 누락 멤버가 일치하지만 Galaxy에서 아직 재검증하지 않았다.

## 관측과 원인 추정의 경계

Coordinator가 App 보고를 전달했다: Galaxy SM_S948N Android 17에서 **`InputManager.getInstance` Espresso lookup 실패, assertion 0/4 진입**, API 34 Compose 4/4 및 JVM 11/11 PASS. 이 연구자는 장비·XML·전체 예외 스택을 직접 보지 않았다. 따라서 해당 수치는 **App 보고**, 후보 적용 후 결과는 **pending**이다. 예외 클래스/최초 발생 프레임/실제 SDK_INT·OS 빌드/해결된 dependency graph는 여전히 필요하다.

[공식 AndroidX Test 릴리스](https://developer.android.com/jetpack/androidx/releases/test#espresso-3.7.0)는 Espresso **3.7.0, 2025-07-30**에 InputManager의 반사 getInstance 호출을 system service 조회로 바꾸고, MessageQueue 처리를 개선했다고 명시한다. 해당 InputManager 수정은 **3.7.0-alpha04, 2025-06-13**에도 기록되어 있다. alpha 대신 stable 3.7.0을 후보로 선택한다. minSdk는 21이므로 앱 min26과 충돌하지 않는다.

Google Maven의 [3.7.0 sources JAR](https://dl.google.com/dl/android/maven2/androidx/test/espresso/espresso-core/3.7.0/espresso-core-3.7.0-sources.jar)을 직접 열어 `InputManagerEventInjectionStrategy#getInputManager`가 API 23 미만에서만 이전 반사 호출을 하고 API 23 이상에서는 system service를 사용함을 확인했다. 이는 보고된 누락 멤버에 직접 대응하는 **강한 수정 후보**이며, 실제 사용 버전·OEM runtime을 확인하기 전 확정 원인/수정 PASS로 기록하지 않는다.

## 공개 빌드 기준과 영향

읽기 기준: `geniuskey/feat-ui-foundation` 공개 SHA **f4f0ab65797a345d9af4565df9bf035ba22e8681**의 app/build.gradle.kts, root build.gradle.kts, wrapper properties, OfflineScreenTest.kt. 버전 카탈로그는 해당 tree에 없다. W2 로컬 0e2aa94는 이 연구에서 열람하지 못했으므로 실제 담당 브랜치와 비교해야 한다.

| 항목 | 확인된 현재값 | 후보 적용 시 |
|---|---|---|
| compileSdk / targetSdk / minSdk | 34 / 34 / 26 | 유지 |
| AGP / Gradle / Java target | 8.7.3 / 8.9 / 17 | 유지 |
| Kotlin / Compose compiler plugin | 2.0.21 / 2.0.21 | 유지 |
| Compose BOM | implementation 및 androidTest 모두 2024.09.00 | 유지 |
| Compose UI·ui-test·ui-test-junit4·manifest | BOM POM에서 **1.7.0** 매핑 확인 | 같이 유지; test만 최신 Compose로 올리지 않음 |
| AndroidX Test 직접 선언 | runner 1.6.2, ext.junit 1.2.1 | Espresso 전이에 의해 runner 1.7.0 선택 예상; graph 확인 |
| Espresso | 직접 선언 없음; ui-test-android 1.7.0 POM은 core/idling **3.5.0** 요청 | core 3.7.0 직접 추가→idling 3.7.0 전이 예상 |
| coroutines-android / test | 1.9.0 / 1.9.0 | 유지 |

POM은 요청 버전이며 **실제 resolved 버전이 아니다**. App 로컬 변경/constraints/lockfile에 따라 결과가 달라지므로 dependencyInsight가 최종 판단이다. 공식 Compose UI 1.7.0 릴리스는 **2024-09-04**이고, [현재 Compose 릴리스 페이지](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.7.0)의 최신 목록이 더 높더라도 이 장애에 대한 전체 제품 업그레이드 근거가 되지 않는다.

[Espresso 3.7.0 POM](https://dl.google.com/dl/android/maven2/androidx/test/espresso/espresso-core/3.7.0/espresso-core-3.7.0.pom)을 열어 core 1.7.0 / monitor 1.8.0 / runner 1.7.0 / idling 3.7.0 / kotlin-stdlib 1.9.21 요청을 확인했다. [core 1.7.0 POM](https://dl.google.com/dl/android/maven2/androidx/test/core/1.7.0/core-1.7.0.pom)은 coroutine-core-jvm 1.8.1을 요청한다. 공개 프로젝트 Kotlin 2.0.21·coroutines 1.9.0보다 높은 언어/코루틴 전면 변경 요구는 이 POM에서 발견하지 못했다. 다만 Android test APK의 classpath/패키징·AAR metadata 검사를 통과해야 호환 후보가 된다. Espresso AAR에 aar-metadata 항목이 없었던 사실만으로 전체 graph의 compileSdk 요구를 보증하지 않는다.

### 적용 후보 (App 담당 전용, 여기서는 미적용)

```kotlin
// 기존 Compose BOM, runner/ext.junit 선언은 우선 그대로 둔다.
androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
```

의존성 하나 추가여도 전이 Test 라이브러리가 바뀐다. `force`/exclude로 구버전 monitor·runner를 묶어 혼합하지 않는다. 명시적 정렬이 팀 정책상 필요하면 **기존 runner를 1.7.0, ext.junit을 1.3.0으로 수정**하는 두 번째 후보까지 가능하나, 이것을 첫 재현 전에 묶어 원인 구분을 잃지 않는다. core-ktx/espresso-contrib/monitor alpha/새 Compose BOM 등 사용하지 않는 의존성은 추가하지 않는다. monitor 1.9.0-alpha01의 공식 수정 항목은 이 InputManager 멤버 문제와 직접 일치하지 않아 우선 후보에서 제외한다.

## Android 17과 target34를 혼동하지 않기

[Android 17 MessageQueue 공식 안내](https://developer.android.com/about/versions/17/changes/messagequeue), 갱신 **2026-10-01**: 새 lock-free 구현은 target API37 이상에 기본 적용되며 구버전 target도 compat toggle로 시험할 수 있다. 공식 권고는 Espresso **3.7.0 이상**이다. mMessages는 삭제됐다고 단정할 수 없고 문서는 새 구현에서도 필드가 남아 null이라고 설명한다. **이번 보고의 누락 멤버는 InputManager.getInstance이므로 MessageQueue를 원인으로 바꿔 적지 않는다.** target34이고 toggle 상태도 모르므로 새 queue가 켜졌다는 가정도 하지 않는다.

[Android 17 migration](https://developer.android.com/about/versions/17/migration), 갱신 **2026-10-01**은 기존 target/compile을 유지한 호환 시험과 새 API37 대상으로 이동하는 작업을 구분한다. 이 연구는 전자다. compileSdk34를 37로 바꾸는 것만으로 이미 실행 중인 Espresso의 반사 코드가 수정되지는 않는다. 전체 스택이 다른 멤버/다른 class를 가리키거나 graph가 이미 3.7.0이면 추가 업그레이드를 중단하고 그 증거부터 조사한다.

## 다음 실행·마감 판단

[RUNBOOK.md](RUNBOOK.md)에 **장비 소유자만 실행하는** 한 번의 build/단일 smoke/후속 전체 시험을 정리했다. 후보 빌드 또는 단일 Galaxy smoke가 같은 오류로 실패하면 10–15분 내 탐색을 중지하고, 현재 확인된 API34 자동 시험 + Galaxy 수동 UX 시험을 별도 결과로 인계한다. 이 분리는 Android17 자동 시험 PASS를 의미하지 않으며 안전·모델 품질 검증도 대체하지 않는다.

[PHYSICAL_CHECKLIST.md](PHYSICAL_CHECKLIST.md)는 두 탭의 권한·설정 복귀·회전·background·사진 보존·IME·큰 글꼴을 위한 합성 입력 기반 실행표다. 연구자는 장비를 조작하거나 APK를 설치하지 않았다. 자료 열람/POM 분석/문서 기본 검사만 완료했고, 수정 적용·runtime 검증·제품 테스트는 App 담당 pending이다.

정확한 공식 POM/AAR/source URL 열람 목록은 [SOURCES.json](SOURCES.json)에 있다. 종료 전 coordinator는 0e2aa94 push 복구를 알렸지만 추가 제품 열람 없이 지정 f4f0ab6 기준으로 이 제한된 조사를 종료한다. 커밋/push SHA는 worker_done에 전달한다.
