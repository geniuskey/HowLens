# W2 Evaluation 준비 완료 보고 — 2026-10-09

[DONE] 한국어 Galaxy 10분 체크리스트, 담당별 즉시 준비 항목, 공식 Android 문서 근거, 비용 없는 black-box smoke, 별도 포트의 합성 Visual/Verification 더블을 구현했다. 기존 13개와 신규 7개를 합친 **20개 테스트 통과**, 로컬 더블 상대 **11개 HTTP smoke 통과**를 확인했고 실제 Galaxy/backend/장비 평가는 실행하지 않았다. 현재 계정의 저장소 권한이 복구되어 W1/W2를 보존한 `Runixs/eval-foundation` push가 성공했으며, App PC의 기본 점검은 지금 시작하고 15:30–16:30 최종 회귀 및 16:30–17:00 제출 준비가 남아 있다.

## 전달물

- `docs/workers/evaluation/W2-HUMAN-TEST-KO.md`: 짧은 10분 실행표, P/F/PENDING 기록, 담당별 액션, USB 데이터 케이블/RSA/잠금 해제/ADB/APK 설치, 동일 PC reverse와 별도 backend PC LAN·방화벽 구분. 공식 Android 페이지 링크와 확인일 2026-10-09 포함.
- `docs/workers/evaluation/W2-RUNBOOK.md`: CLI 사용법, 서버 mode gate, mock guide의 제품 gate 유지, 별도 App test 주입, 제한 및 pending 기준.
- `evaluation/api_smoke.py`: health/잘못된 MIME·PNG/10MiB 초과/잘못된 장비/404/수락된 non-guide 409 검증. paid opt-in 기본 OFF. live 서버이면 POST 전부 생략; fixture가 guide이면 해당 분석에 visual/verification을 요청하지 않음.
- `evaluation/demo_double.py`: 기본 127.0.0.1:8765, 제품 import/upstream/모델 호출 없음. TEST mock guide 또는 non-guide/stop, 4개 Visual 상태, Verification 세 enum, 실제 숫자 0–8 PNG와 상대 URL, 완료 작업 재사용과 실패 사용자 재시도 1회. 서버를 재시작하면 TEST ID 소실.
- `evaluation/test_w2.py`: 위 runner/더블 회귀 및 요청 기록 기반 guide/live 호출 차단 테스트.
- `docs/assets/test-prep/TEST-only-upload.png`: 1×1 합성 입력 fixture, 장비 사진 아님.
- `docs/assets/test-prep/W2-local-double-smoke.json`: 실제 실행한 로컬 더블 상대 smoke 결과. 제품 서버 평가 결과가 아님.

기본 branch HEAD `856b2a6`에서 작업했다. 타 owner 코드/소스 또는 evaluation/research 경로를 읽거나 수정하지 않았고 main merge도 하지 않았다. W1 기대값과 과거 push 실패 기록은 그대로 보존했다.

## 실제 실행

```sh
PYTHONDONTWRITEBYTECODE=1 python3 -m evaluation.demo_double --port 8765
PYTHONDONTWRITEBYTECODE=1 python3 -m evaluation.api_smoke --base-url http://127.0.0.1:8765 > docs/assets/test-prep/W2-local-double-smoke.json
PYTHONDONTWRITEBYTECODE=1 python3 -W error::ResourceWarning -m unittest discover -s evaluation -p 'test_*.py' -v
```

결과: smoke exit 0, 11건 모두 passed. HTTP 순서: **200, 404, 415, 415, 413, 422, 404, 404, 200, 409, 409**. `paid_analysis_opt_in=false`. 전체 20개 unittest는 3.789초, OK. 로컬 서버는 완료 전 SIGINT로 종료했으며 장기 실행 프로세스를 남기지 않았다.

후속 Coordinator 지시에 따라 mock guide의 분석 ID로 visual/verification 요청을 전혀 보내지 않는 것을 요청 spy로 명시적으로 확인하도록 테스트를 강화했다:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 -W error::ResourceWarning -m unittest evaluation.test_w2 -v
```

7개 W2 테스트 3.714초, OK. live health 응답 기본값에서 GET 2건만 발생하는 것도 별도 spy로 확인했다. **제품 App의 버튼/HTTP gate는 이 PC에서 검사할 수 없어 PENDING**이며 사람 체크리스트 T06에 명시했다. 제품 guard를 끄거나 fixture mode를 live로 바꾸지 않았다.

실제 backend의 오류를 발견한 것은 없다. 이 결과는 평가 runner와 합성 더블 사이의 테스트이고, 제품의 형식 검증·시각 실패 처리·재시도 제한을 입증하지 않는다. 더블은 알려진 PNG 원본만 허용하므로 앱 재인코딩/JPEG/실사진/20MP 검증은 제공하지 않는다. 원본 fixture 주입 경로가 없으면 UI fixture 테스트를 pending으로 남긴다.

## Commit / push 결과

첫 W2 구현·체크리스트 commit: `3aa4c32ae9b676053e184828453b4c56277e967a`.

```sh
git push -u origin Runixs/eval-foundation
```

exit 0, 실제 receipt:

```text
To https://github.com/geniuskey/HowLens
 * [new branch]      Runixs/eval-foundation -> Runixs/eval-foundation
branch 'Runixs/eval-foundation' set up to track 'origin/Runixs/eval-foundation'.
```

현재 Runixs 계정을 그대로 사용했다. W1 403 delivery blocker는 이 push로 해소되었다. 추가 guard assertion과 본 보고서는 후속 commit에 포함하며 최종 branch SHA와 push 결과를 worker_done에 전달한다. Coordinator에 체크포인트와 즉시 사용 가능한 체크리스트/성공한 push를 중간 보고했다.

## 미실행 및 다음 담당

- **김의윤:** 지금 Galaxy 설치/카메라/갤러리 기본 점검, APK·OS·URL·mode 기록. 제품 mock guide에서 live-only Visual/verification gate가 유지되는지 관찰. test 전용 주입 경로가 있으면 시각 실패/9패널/검증 화면을 따로 실행.
- **이준영:** 실제 base URL 및 mock에서 model 호출 없음 확인, 별도 PC LAN·포트·방화벽 도달 확인. 제공 주소가 없어 제품 API smoke는 미실행.
- **김태완/Coordinator:** 실제/합성별 P/F/PENDING 기록 취합. 15:30–16:30 최종 회귀, 16:30–17:00 제출.
- **모두 PENDING:** 실제 Galaxy 접근, APK 설치, 권한 거부, 카메라·갤러리, 잘못된 장비·흐림·매뉴얼 없음·악성 문자열·위험의 실제 판단, 네트워크 단절·복귀/회전/취소/재시도/텍스트 보존/9개 순서 UI, 실제 전후 검증, 실제 매뉴얼 근거 확인. 기존 21개 안전 케이스는 계속 pending.
- **유료 호출 0회.** mock 결과는 실제 guide 승인이나 장비 성공 증거가 아니다.
