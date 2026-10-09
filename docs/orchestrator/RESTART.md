# 재시작 인계 — 2026-10-09 14:34 KST

사용자가 모든 HowLens 세션을 종료하고 다시 시작하도록 명시 지시했다. 새 작업을 배포하지 않는다. 이 문서는 이전 HANDOFF보다 최신이다. 종료 receipt는 `receipts/session-shutdown-20261009.json`을 확인한다.

## 시작 지점

- 김태완 PC `/Users/runixs/HowLens`, main. 새 Coordinator worktree 불필요. RULES → START → 이 문서 → BOARD/DEVICE_ROUND 순으로 읽는다.
- 제품 코드 main push 완료. 앱 R3 source `9a2bfa9`, 서버 source `dd7e4ae`, 종료 전 문서 commit `4e0e734`.
- 기존 Run `run_6351cb7363de`는 김의윤 Hub runtime `6f189372-0b29-4065-a2f2-cc27d192b26d`에 보존. reset/새 Run/legacy takeover 금지. 62개 Dispatch 조회 시 active0. 모두 완료/실패/abandon 처리돼 중복 재개하지 않는다.
- 기존 Coordinator와 consumer terminal은 종료 대상으로 재사용 불가. 새 live terminal에서 version-matched Orca guide를 읽고 새 consumer binding을 공식 CLI로 확인할 것. 이전 `term_3d2273ec-18ec-4f05-a363-47a0d768c9e2`는 Hub plain transport였으며 계정 공유가 아니었다.

## 앱과 실기기

- R3 동일 APK 세 폰 설치-r/실행 Status:ok 확인. 장비 중심 Home, 추천가이드 대신 등록 장비3종, 선택 즉시 camera, HowLens 로고 Home, 상단 별도 Home 제거. 선택 장비 유지.
- SHA256 `7edb4e6cc669ac1121a0d2a654ebe735699f393d5d952fd21dada3f8b9ba5f3b`, 13,193,414 bytes.
- 영구 로컬 보관: 김태완 `/Users/runixs/HowLens-session-artifacts/20261009-1433/howlens-r3-9a2bfa9.apk` + manifest.json. `/tmp`에도 있지만 재부팅 보관용으로 의존하지 말 것.
- Galaxy 김의윤: R5KL20H60TN, adb `/Users/edwin/Library/Android/sdk/platform-tools/adb`.
- Fold4 이준영: R3CT80CZ2MW, adb `/Users/jymbook/Library/Android/sdk/platform-tools/adb`.
- S25 이윤재: R3CY70W172M, adb `%TEMP%/HowLens-W5-device-qa/platform-tools/adb.exe`.
- 앱을 삭제하거나 데이터를 초기화하지 않았다. 설치된 앱은 서버 재가동 전 분석할 수 없다.
- Build: Java21 `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`, SDK `/Users/runixs/Library/Android/sdk`, android에서 `./gradlew --no-daemon :app:assembleDebug`, URL만 `HOWLENS_API_BASE_URL`로 주입. OpenAI key는 절대 앱에 넣지 않는다.

## 서버 재가동

- 이준영 PC `jymbook@10.102.72.28` (테더링 IP 재확인). backend worktree `/Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation`.
- canonical key/env와 누적 ledger는 각각 `backend/.env`, `backend/.provider-usage.json`; 값 출력/공유 금지, ledger reset 금지. 종료 직전23 attempts/21completed, cap40, 실제 다음 시작 시 파일 재확인.
- 검증된 immutable runtime/launcher를 `/Users/jymbook/HowLens-session-artifacts/20261009-1433/howlens-routing-runtime-dd7e4ae/`에 보존. 해당 `launch.py`는 같은 owner의 canonical .env를 참조하며 key 복사본을 포함하지 않는다. 실행 Python은 위 backend/.venv/bin/python. 원 `/tmp/howlens-routing-runtime-dd7e4ae`도 유지.
- 라우팅: 초기분석 Sol6.1 medium20s, 분류 Luna low20s(runtime override), 검색 Sol6.1 medium20s, 조건충족 재분석만 Astra low15s. 총분석40s/선택적조사37s, 최대4provider attempts. Discovery총30s/최대2attempts. `OPENAI_MODEL=gpt-4.1-mini`는 기존ledger anchor이므로 바꾸지 않는다. 실제 요청은 role routing ID.
- Gateway script `docs/workers/backend/discovery-live/demo_gateway_launcher.py`, `HOWLENS_DEMO_AUTH_DISABLED=true`; upstream8000→gateway127.0.0.1:18080. 사용자 승인으로 임시 no-auth. 물리적 안전/검증 guide gate는 유지.
- Tunnel은 cloudflared HTTP2 quick tunnel. **종료된 https://premium-proceed-hartford-published.trycloudflare.com 주소의 재사용은 보장되지 않는다.** 새 URL이면 앱 BuildConfig를 다시 주입해 동일 APK를 세 폰에 동기 설치.
- 원 PID21375(API),99195(gateway),46758(tunnel)를 종료. 재사용/숫자만 보고 kill 금지, 다음 실행은 새 PID/terminal receipt를 기록.
- 테더링 DNS 장애: owner Wi-Fi DHCP DNS10.102.72.240에서 api.openai.com lookup 실패, 공개health는 성공. DNS를1.1.1.1/8.8.8.8로 바꿔 TLS4010.402s 복구. 현재 설정 유지. 이전은 수동 DNS 없음; 필요할 때 `networksetup -setdnsservers Wi-Fi Empty`로 되돌린다.

## 증명된 것과 남은 일

- 실제 Sol 분석 HTTP20021.064s, CUCKOO AC-35U20FWS 라벨 관찰. 다른 실제 분석도 Sol17–19s 응답. 선택 server와 사진 공기청정기 불일치이므로 guide 차단은 유지.
- Luna 분류3.878–7.358s, Sol 검색10.1s, 마지막Discovery20014.984s. 반환모델 확인됨. Astra는 설정만, 실제 호출 미검증.
- 검색 응답에 completed1+searching1이 함께 온 문제는 dd7e4ae에서 수정. source13 URLs가 validation까지 진입했지만 **source_identity로 exact candidate 미승인**. 후보/URL/title 최소 metadata를 다음 디버깅에서 안전하게 남겨 원인을 규명해야 한다. 검색/매뉴얼기반guide 완성으로 과장하지 말 것.
- 이미지 production 통합은 아직 미완료. fallbackgpt-image1.5와 별도 benchmark를 혼동하지 않는다. Sunburst2.5 datedmodel 6회low/medium HTTP20012.990–19.526s, 실제bill/품질winner 미확정. 소유자 PC 영구보관 `/Users/jymbook/HowLens-session-artifacts/20261009-1433/measured`에 이미지/측정자료. 추가 생성 전 품질 검토부터.
- 모든제품에 대한 PDF ingestion/정확매뉴얼등록 완료는 미검증. 등록catalog는 server/cobot/ups3종. 사용자 자료9JPG, content.png, UI_REQUIREMENTS.md는 main 로컬 untracked로 보존했고 임의 Git업로드하지 않았다.
- 최신 검증: R3build12s PASS, HTML3장비흐름320/390 PASS, 새native instrumentation 작성됐으나 미실행. 서버변경40 focused, timeout3, searchshape11 PASS. 이전baseline 반복실행하지 말 것.

## 운영 지시 유지

- 김의윤 PC/계정 Luna6 테스트용만. Astra는 김태완 계정. 제품 수정 Workers, root main Git/배포 mutex.
- 사용자 피드백은 Coordinator 한 곳. 한 번 빌드한 APK를3대 동기설치 후 사람이 테스트. 사람이 테스트하는 동안 자동tap 금지.
- App은 요청1회, 서버만 연구/재분석/모델선택. 토큰입력 UI 없음, 앱키 없음. 현재 camera는 preview+단일사진분석, continuousvideo AI 아님.
- 디자인/모델 대시보드 `docs/workers/app/reference-ui/ai-status.html`; 목업 `docs/mockups/reference-ui/index.html`. 공식영수증 `docs/orchestrator/receipts/`, 상태 `BOARD.txt`/`DEVICE_ROUND.json`.
- 제출17:00, 원featurefreeze14:30/검증15:30/package16:00 목표. 현재 남은 시간을 다시 계산한다.
