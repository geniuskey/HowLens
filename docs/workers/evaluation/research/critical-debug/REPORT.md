# 긴급 진단 — Fold4 APK → Backend

2026-10-09 13:02 KST · 읽기 전용 진단 · 제품/기기/서버 상태 변경0, 유료 호출0.

**전송 병목은 해소된 증거가 있다.** QA `ctx_15c1e6bdf26a` 최신 exact transcript에서 표준 reverse 복구와 stdin 유지 후 Fold4 → Backend `/health`가 **HTTP200, {status:ok,mode:live},2091ms**로 확인됐다(응답 Date03:58:57UTC). 이 결과는 담당 QA의 실제 출력이며 본 연구자가 폰에서 실행한 결과가 아니다. **APK의 실제 POST/응답 렌더는 아직 미검증**이다. 이제 반복 LAN/서버 재시작 대신 demo OFF + `http://127.0.0.1:18000/` + 허가된 합성 사진으로 APK 분석 한 번에 집중한다.

## 원인/장애 순위 — 최대3개

| 순위 | 증거/판정 | 정확한 조치 |
|---|---|---|
| 1 | **진단 probe의 EOF 취급과 일시적인 reverse 매핑 소실.** 기존 nc 즉시 빈 stdout/exit0, `reverse --remove tcp:18000` 뒤 `tcp:127.0.0.1:8000`은 Invalid destination port로 실패. 이후 `tcp:18000 tcp:8000` 복구 + printf 후 sleep2로 HTTP200. 둘을 함께 수정했으므로 최초 빈 응답의 단일 내부 원인을 패킷 없이 더 단정하지 않음 | 현재 정상 mapping 유지. 필요할 때만 아래 재현 명령. TCP 연결/exit0/매핑 목록만으로 HTTP PASS 기록 금지 |
| 2 | **폰의 직접 LAN은 timeout이지만 Backend는 살아 있음.** 연구 PC에서 `10.102.72.28:8000/health` HTTP200, QA host에서도127.0.0.1과LAN HTTP200. 폰LAN3s timeout의 정확한 원인(AP isolation/라우팅/방화벽 등)은 미확정 | 검증된 USB reverse 사용. LAN 원인 조사는 deadline 경로에서 제외. 서버 restart, bind 변경, 방화벽 변경 불필요 |
| 3 | **APK 모드·baseURL·입력/운영 인계가 남은 관문.** 57405bb 기본 offline=true,baseUrl10.0.2.2; demo일 때 주소 칸 숨김. QA 사용자가 demoOFF 후 주소 입력칸 표시를 실제 응답으로 확인함. 주소 설정·최종POST 성공 증거는 아직 없음 | offline OFF, 정확한 루트127.0.0.1:18000, syntheticPNG/질문 입력 후 한 번 분석.10.0.2.2는 에뮬레이터용이며 /health나 /analyses를 baseURL에 넣지 않음 |

초기 중첩 transcript 표시만 보고 CRLF double-escape를 의심했으나 **철회**했다. 도구 입력 JSON과 Python literal까지 해독한 payload에는 실제 `0d0a`4개, literal backslash0개였다. CRLF 문제를 확정 rootcause로 기록하면 안 된다.

## APK 확인 범위

코드 기준 `57405bb4811f67bb49ea779d883fd2937c98fd7d`:
- main manifest INTERNET 있음; debug manifest `usesCleartextTraffic=true`; 별도 networkSecurityConfig는 읽은 manifest에 없음. release나 다른 APK에 확대 적용하지 않는다.
- QA는 해당 debug APK15034933bytes/SHA`2b39251176178975fd0a9228da55a88903d879e7d2cc9fd4dcdb695d128abb25`를 검증했다고 기록. 연구자는 설치 APK manifest를 직접 추출하지 않았다.
- `HttpAnalysisRepository`는 인증정보/query/fragment 없는 루트 path `/`만 허용. analyze는 `/analyses`에 multipart device_id/question/photo를 보낸다. `health()` 함수는 있어도 현재 ViewModel.analyze가 사전 health를 자동 호출하지는 않는다.
- connect10s/read45s/write30s/call60s, 자동 연결 재시도 없음. generic network 문구는 IOException을 가려 cleartext/connection refusal을 구별할 수 없으므로 실패 시 owner가 제한된 앱 예외만 확인한다.
- `AnalysisViewModel` 상태는 메모리 기반. settings edit가 진행 요청을 cancel/invalidate하므로 요청 중 모드/주소/입력 편집 금지. 회전과 process death를 동일시하지 말고 이 1회검증 동안 앱 재설치/강제종료로 기본mock에 돌아가지 않게 한다.

## 담당 QA가 실행할 정확한 최소 검증

이미03:58:57UTC health가 성공했으므로 **현 mapping이 유지되면 probe를 다시 돌리지 않는다.** USB 재연결 등 mapping이 사라진 경우에만 해당 폰의 기존 소유자인 QA가 실행한다. 이 연구자는 실행하지 않았다.

```sh
/Users/jymbook/Library/Android/sdk/platform-tools/adb -s R3CT80CZ2MW reverse --list
```

목표는 `tcp:18000 tcp:8000`. 없는 경우에만 다음 명령으로 추가한다. 이미 다른 mapping이면 덮어쓰지 말고 소유 상태부터 확인한다.

```sh
/Users/jymbook/Library/Android/sdk/platform-tools/adb -s R3CT80CZ2MW reverse --no-rebind tcp:18000 tcp:8000
/Users/jymbook/Library/Android/sdk/platform-tools/adb -s R3CT80CZ2MW shell "(printf 'GET /health HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n'; sleep 2) | toybox nc -w 3 -W 3 127.0.0.1 18000"
```

adb reverse 목적지8000은 **ADB host의 localhost8000**이다. 이번 Fold4는 Backend host에 붙어 있어 host local health도 이미 확인됨. Galaxy가 다른 PC에 붙어 있으면 같은 명령을 복사해도 그 다른 host8000으로 가므로 잘못된 경로다. 기존 성공 relay가 필요한 것은 그 경우뿐이며 이 Fold4에는 새 relay 불필요. 서버를 다른 PC로 옮기지 않는다.

**실제 APK 한 번 검증 (QA 소유, 추가 curl POST 없음):**

1. 기기R3CT80CZ2MW/앱57405bb/대상process를 기록; settings demoOFF, baseURL `http://127.0.0.1:18000/` 확인 후 settings 닫기.
2. 이미 허가·준비된 TEST-only synthetic PNG를 선택하고 preview 확인. 개인 사진·실장비 작업 사진·카메라 새 촬영으로 대체 금지. 질문은 합성 시험임과 모델 확인을 요청하는 짧은 문구, device_id=server.
3. **사진 분석 버튼 한 번만** 누르고 최대60초 기다린다. QA의 기존 유료 호출 권한·budget 확인 범위에서만 수행; 이 문서는 새로운 유료 권한을 부여하지 않는다. timeout이면 자동 반복/다른 worker POST 병렬 시도 금지.
4. PASS에는 실제 APK loading→result, 서버 발급analysis_id,device_id=server,mode=live,decision과 steps를 기록한다. 예상은 needs_more_information 또는 stop 및 steps=[]; 이를 실패라고 보고 guide를 강제하지 않는다. response/UI의 문서근거·오류 분기 일치도 확인한다.
5. HTTP503/504/422를 받으면 전송은 도달했으나 해당 provider/validation 단계 실패로 분리한다. HTTP200만인데 UI가 decode error이면 App DTO 단계다. 단순 빈nc/localhost mapping/화면탭 확인은 APK POST PASS가 아니다.

서버는 `--no-access-log`로 기동되어 access line 부재가 미도달 증거는 아니다. 키·전체 질문/사진·전체 로그를 수집하지 않는다. 에러 발생 시 owner가 시간/상태코드/예외 클래스/phase와 합성 요청 ID만 연결한다.

## App test worker의 기다림 진단

`ctx_e7170acb2e7a` latest worker-read는 **failed/completed report** 상태: docs/test asset commit3569db5의 push가 normal+HTTP1.1에서 실패하여 worker_done failed를 보냈다. 현재 blocked ask가 계속 도는 상태라고 볼 수 없다. 이전 `delivery_651b56bee040` 반복은 ack 없는 delivery replay로 보이며 Coordinator가 원 worker를 재개할 경우 새 지시 처리 후 자기 delivery를 ack하도록 하면 된다. 연구자가 남의 inbox를 consume/ack하거나 Coordinator를 사칭하지 않았다.

Fold4 QA human reply에는 demoOFF 후 주소 칸 표시가 실제 있다. Galaxy camera deny/settings-return은 아직 human 확인 pending으로 기록되어 있고, 권한 허용 지시와 사용자가 실제로 탭한 증거는 다르다. 직접 사용자 override는 해당 지시에서 우선하되 미응답 행동을 PASS로 만들지 않는다.

## 공식 근거와 검증 한계

조회2026-10-09, 실제 열람:
- [AOSP toybox netcat.c](https://android.googlesource.com/platform/external/toybox/+/refs/heads/main/toys/net/netcat.c), blob557380a4aa8e5affbce083bab47bdf99e86d6c77: -w 연결시간,-W idle시간,-q stdin EOF 후 종료시간. 기기 탑재 binary가 이 HEAD와 같은 버전이라는 주장 아님.
- [AOSP adb manpage](https://android.googlesource.com/platform/packages/modules/adb/+/refs/heads/main/docs/user/adb.1.md): serial선택/reverse와TCP port mapping. 실제 이 adb에서 hostname 형식 거절 출력 확인.
- [Android application cleartext](https://developer.android.com/guide/topics/manifest/application-element#usesCleartextTraffic): manifest cleartext 정책. debug 소스 확인 범위만 주장.

본 연구 실행: host HTTP health1회, git fetch/show read-only, authorized worker-read, 공식 문서 열람, 문서 정적검사. 기기명령·서버재시작·APKPOST·paid API·personal data 조작 없음. transport성공과 독립 human Approval/guide 차단은 서로 다른 문제이며 guide 승인 공백은 남는다.

## App takeover에 바로 쓸 확인된 경로

- App workspace: `/Users/edwin/orca/workspaces/HowLens/feat-ui-foundation`; Gradle root는 그 아래 `android/`.
- App adb: `/Users/edwin/Library/Android/sdk/platform-tools/adb`; AppGalaxy serial은 `R5KL20H60TN`(Fold4와 다름).
- source57405bb: wrapper Gradle8.9, AGP8.7.3, Kotlin/Compose compiler2.0.21, Compose BOM2024.09.00, Java target17, compile/target34,min26, Espresso3.7.0.
- `./gradlew --version`으로 실행 JDK를 확인한 후 기존 환경을 재사용한다. 실제 JAVA_HOME 절대경로는 이번 로그에 없어 추측하지 않는다. 네트워크 진단 해결을 위해 toolchain upgrade나 APK rebuild가 필요한 증거는 없다.
- Coordinator가 새 App Astra takeover를 준비한다고 통보했으므로 구 Luna worker 재가동/중복 editor 지시를 하지 않는다. 문서 push 실패와 APK 전송 검증은 별도 추적한다.
