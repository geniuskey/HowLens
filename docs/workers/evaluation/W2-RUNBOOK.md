# W2 비용 없는 계약 스모크 및 앱 더블

2026-10-09. Python 3.10+ 표준 라이브러리만 사용. 저장소 루트에서 실행한다. W1 문서·기대값은 보존하며 새 실행기는 `python3 -m evaluation.api_smoke`다. 실제 Galaxy는 김의윤 PC에서 [사람 점검표](W2-HUMAN-TEST-KO.md)에 따라 별도 실행한다.

## 기본 실행 — 유료 호출 없음

터미널 A:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 -m evaluation.demo_double --port 8765
```

터미널 B:

```sh
PYTHONDONTWRITEBYTECODE=1 python3 -m evaluation.api_smoke --base-url http://127.0.0.1:8765
PYTHONDONTWRITEBYTECODE=1 python3 -W error::ResourceWarning -m unittest discover -s evaluation -p 'test_*.py' -v
```

`api_smoke`의 기대 순서는 health 200, 존재하지 않는 visual job 404, 잘못된 MIME 415, 손상 PNG 415, **파일 10 MiB+1 byte** 413, 잘못된 장비 ID 422, 존재하지 않는 분석 visual/verification 404 각각, 합성 PNG 분석 200, 받은 분석이 non-guide일 때 visual/verification 409 각각이다. 모든 11건이 실행되어 통과하면 exit 0. 기대값 불일치는 exit 1. 안전하게 미실행한 요청이 있으면 exit 2와 `pending`이다. 기대값을 응답에 맞춰 바꾸지 않는다.

실제 제공된 base URL에도 같은 명령을 사용하되 Backend 담당자가 **mock가 실제로 upstream을 호출하지 않음**을 먼저 확인해야 한다. health가 live를 보고하면 기본값에서는 GET 두 건만 수행하고 **모든 POST를 건너뛴다**. 명시적 `--allow-paid-analysis` 옵션이 있지만 W2에서는 절대 사용하지 않았다/사용하지 않는다. 서버가 잘못 mock라고 보고하는 내부 구현까지 black-box가 보증하지는 못한다. 유료 호출 차단을 서버 담당자와 함께 확인한다.

mock 분석이 synthetic 입력에 guide를 반환하면 runner는 guide를 승인으로 간주하지 않고 visual/verification 호출을 생략하고 pending을 기록한다. 이때 409 확인을 위해 서버 기대값을 임의로 non-guide로 바꾸지 않는다. API 검증 오류는 계약의 detail 객체 또는 422의 detail 배열을 확인한다. 응답 최대 1 MiB, 기본 네트워크 timeout 5초, redirect 금지. 실패 응답 전문/사진/키는 출력하지 않는다. 실패 시 첫 실패에서 중단하므로 이후 항목은 실행되지 않았다.

## 앱 UI용 합성 서버 — 제품 포트·제품 코드와 분리

같은 PC에서 제품은 예시 8000, 더블은 **8765**. `8000`을 더블 포트로 지정하면 거부한다. 기본 loopback 전용이며 필요한 LAN 테스트에서만 `--host 0.0.0.0`를 사용한다. 사용 후 Ctrl-C로 종료한다. 더블은 모든 분석/시각화/검증을 mock로 반환하고 제품 코드를 import하지 않으며 모델/네트워크 upstream을 호출하지 않는다.

아래 각 명령은 앞 서버를 종료한 뒤 하나씩 실행한다. 재시작하면 저장된 TEST 분석·작업 ID가 사라지므로 앱에서 새 분석이 필요하다.

```sh
# 추가 정보 / steps 비어 있음
python3 -m evaluation.demo_double --port 8765 --scenario non-guide
# 위험 stop 화면
python3 -m evaluation.demo_double --port 8765 --scenario stop
# TEST guide + 시각 실패, 사용자 재시도 1회만(세 번째 요청은 409)
python3 -m evaluation.demo_double --port 8765 --scenario guide --visual failed
# TEST guide + 숫자 0~8의 실제 PNG 9개; 원래 단계는 TEST-s1 하나
python3 -m evaluation.demo_double --port 8765 --scenario guide --visual completed --verification inconclusive
# 나머지 검증 화면: --verification observed_change 또는 issue_remaining
# 대기/진행 표시: --visual queued 또는 running (자동 진행하지 않음)
```

완료 결과는 같은 분석에 재사용되고 GET /visual-jobs/{id}로 조회 가능하다. 이미지 `/visual-assets/TEST-grid.png`는 3×3 숫자 0–8; 각 `/visual-assets/TEST-panel-{0..8}.png`는 해당 숫자다. 이 PNG는 일반 코드로 만든 **순서 검사 도형**이며 장비 사진도 AI 생성 이미지도 아니다. 의미·안전성 검증 자료가 아니다. 실패 후 텍스트는 앱이 보존하는지 사람이 확인한다. 더블의 테스트 통과는 앱 통과를 뜻하지 않는다.

앱에 넣을 입력은 `docs/assets/test-prep/TEST-only-upload.png` (1×1 PNG, 합성)이다. 서버는 의도적으로 **이 파일의 원본 바이트만** 받는다. 파일/문서 선택기를 통해 원본을 선택한다. 갤러리/앱이 이미지를 다시 인코딩하면 415가 나올 수 있으며, 이는 더블 범위 제한이지 제품 오류로 판정할 근거가 아니다. App 담당자가 테스트 클라이언트에 원본 fixture를 공급할 수 없으면 해당 UI 검증은 pending으로 남기고 HTTP 검증만 사용한다. 임의 실제 사진을 넣어 guide를 받아 장비 승인처럼 쓰지 않는다.

김의윤 PC에서 폰으로 파일 복사 예시 (SERIAL은 실제 값):

```sh
adb -s SERIAL push docs/assets/test-prep/TEST-only-upload.png /sdcard/Download/TEST-only-upload.png
adb -s SERIAL reverse tcp:8765 tcp:8765
```

폰의 파일 선택기에서 Download 파일을 선택한다. Photo Picker에서 파일이 안 보이면 자동으로 보인다고 가정하지 말고 App 담당자가 지원하는 파일 선택 경로를 확인한다. 앱 테스트 URL은 `http://127.0.0.1:8765`. 앱이 mock guide에서 시각/검증 버튼을 차단하면 제품 안전 gate를 해제하지 않는다. App 담당자의 기존 test-only repository/HTTP fixture 주입 경로가 있을 때만 해당 화면을 검사하고, 없으면 pending으로 기록한다.

## 범위와 제한

더블은 정적 UI fixture 제공 도구이며 backend 구현의 정답 판별기가 아니다. 정확히 알려진 PNG만 허용하므로 JPEG/범용 디코딩/20MP 경계/전체 폼 필드 검증을 재현하지 않는다. HTTP smoke는 실제 backend로 실행하기 전까지 backend 통과 증거가 아니다. 취소 HTTP endpoint는 계약에 없으며 취소는 앱 로컬 동작으로 검사한다. retry 제한/enum/9패널 데이터는 더블에서 검사했지만 실제 앱/제품의 동작은 사람 점검 결과가 필요하다. 잘못된 장비·흐림·근거 없음·악성 문자열·위험·전후 모호함의 실제 분석은 기존 21개 케이스에서 계속 pending이다.
