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
