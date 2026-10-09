# W3 독립 통합 감사 — 2026-10-09

**판정: 지정 backend 테스트 54개 통과, 취소 시 디코더 동시 작업 제한 우회 1건 재현.** 기본 guide 안전 gate는 통과했으나 실제 이미지 생성/실기기/유료 AI 성공은 확인하지 않았다. Coordinator가 재현 문제를 Backend 담당자에게 전달했으며 제품 수정은 하지 않았다.

- 감사 대상: `origin/ljyonefineday/feat-backend-foundation` = **1081de70191cdb35bbaeca5c7291edf1b60da02d** (fetch 후 일치 확인).
- 평가 시작점: `Runixs/eval-foundation` = **d263f77fc81b676cb6f60ba9fb9da92a0c51cb8e**, 기존 커밋 보존, main merge 없음.
- 기준: 이 평가 브랜치의 `docs/common/CONTRACT.md` v0.1.
- 허용 파일만 `git archive`로 별도 임시 디렉터리에 추출. 추가 import인 `store.py`, `__init__.py`는 Coordinator 승인 후 같은 SHA에서 추출. product worktree/소스 수정 및 실제 `.env`/비밀 읽기 없음. config 테스트가 임시 생성한 **synthetic** dotenv만 사용.

## 재현 결과와 조치

| 구분 | 증거 / 영향 | 담당 조치 |
| --- | --- | --- |
| **재현 실패 / 우선 수정** | `main.py:63–77`: `Semaphore(2)` 안에서 `asyncio.to_thread(decode, data)`를 기다리다가 요청이 취소되면 슬롯은 반환되지만 기존 스레드는 계속 실행. 느린 decode 주입 + HTTP ASGI 요청 2개 시작→취소→새 2개 요청으로 **peak=4** 재현. 새 repro CLI는 이를 exit **1**로 표시 | Backend: 실제 worker 종료까지 슬롯을 보유하거나 별도 bounded worker/process 설계; 취소·timeout 회귀 추가. Coordinator가 전달 완료. 실사진 악용/네트워크 disconnect 자동 취소까지 입증한 것은 아니며 **주입한 느린 디코더에서 task 취소를 재현**한 결과 |
| **재현 호환 차이 / 제품 결함 아님** | Visual 1·2·3회 POST가 모두 202이고 3회는 2회 job_id 재사용. 계약의 총 2회 시도 제한은 지킴. W2 더블은 3회에 409 반환하여 동일하지 않음 | App: retry 제한을 409에만 의존하지 말 것. Evaluation: W2 더블의 세 번째 409는 계약 요구가 아닌 로컬 profile임을 전달; 본 감사에서 기대값을 조용히 바꾸지 않음 |
| **정적 확인 / 데모 의존성** | `main.py:115–116` health는 provider 미설정이어도 `ok/live`. 실제 설정 미완료 요청은 503. W2 default smoke는 live를 보면 GET 2건 후 POST를 pending으로 생략 | App/Coordinator: health ok/live를 모델·guide·이미지 준비 완료로 표시하지 말 것. 실제 URL smoke에서 pending은 정상 비용 차단이며 실패를 숨긴 PASS가 아님 |
| **정적 확인 / 통합 미완료** | `main.py:140–155` Visual 라우트는 승인 guide에도 명시적 failed만 반환. 이미지 asset 라우트/실제 생성 연결 없음. `visual_boundary.py:40–43`은 1–9 step을 9칸에 비례 배정 후 별도 semantic reviewer 승인 요구 | Backend/Visual: 실제 활성화 전에 panel 의미와 step 매핑을 함께 검증. 현재는 텍스트 보존+failed만 데모 가능; 실제 9패널 생성 성공 주장 금지 |
| **정적 확인 / 미래 활성화 점검** | `models.py:71–84`의 Visual DTO 자체는 9개 순서/참조/relative URL 교차 검증 없음. boundary는 bytes 길이와 개수만 검사하고 PNG decode 자체는 하지 않음 | Backend/Visual: completed 라우트 연결 시 evaluation invariant 및 asset 검증 적용. 현재 미활성 경로이므로 공개 API에서 잘못된 completed를 재현한 것은 아님 |

## 안전 gate 및 계약 대조

- **테스트/추가 HTTP 확인:** W2의 합성 1×1 PNG를 실제 backend decoder가 200으로 수락하고 미승인 guide 후보를 `needs_more_information`, `steps=[]`로 반환. evaluation Analysis validator 통과. TEST provider/reviewer는 격리 테스트만의 주입이며 실장비 승인 아님.
- **테스트:** 독립 registry/reviewer가 없거나 warning, unknown/unsatisfied 필수조건, 위조 quote, 다른 action, mock mode, 중복 evidence, non-guide일 때 실행 steps가 차단된다. 등록된 장비·문서·버전·페이지·quote·section·URL·printed page와 action, 승인된 step 및 prerequisite ID를 대조한다 (`safety.py:41–68`).
- **추가 HTTP 확인:** mock 후보는 non-guide로 바뀌고 Visual와 Verification 모두 **409**. 제품 live-only gate를 해제하거나 fake fixture를 제품 live 승인으로 사용하지 않았다.
- **정적+테스트:** OpenAI adapter는 required preconditions를 unknown으로 되돌린다 (`openai_provider.py:98–102`); 기본 conservative reviewer는 빈 Approval, catalog는 비어 있음. 모델 confidence/자기 주장만으로 guide가 나오지 않는다. 기본 구성에서 실제 승인 guide가 나올 준비도 아직 없다.
- **테스트+validator:** 합성 trusted test seam의 Analysis, failed VisualJob, Verification이 평가 스키마를 통과. verification enum/원래 evidence ID/analysis ID/live mode 검증 및 안전 보증 불가 limitations 확인. source 질문/사진 속 injection은 데이터로 전달되고 보수적 reviewer로 차단됨. 실제 모델의 injection 저항성을 시험한 것은 아님.
- **테스트:** JPEG/PNG 디코딩/MIME 불일치, 10MiB 경계·초과, 20MP 초과, streaming request 초과, 공백 제거 뒤 질문 길이, 404/409/422, 업로드 timeout, provider timeout 504 및 generic 503, 출력 4,000자/128항목/128KiB 제한, bounded store eviction 검증 통과.
- **정적 확인:** upload 15초 processing/receive 및 30초 receive deadline, provider timeout/semaphore, provider HTTP response 1MiB 상한, redirect off, trust_env off가 있다. 비동기 timeout은 thread를 강제 종료하지 않으므로 위 재현 finding은 별도 해결 필요. Visual splitter도 `to_thread`를 사용하지만 미활성 경로의 취소/자원 제한은 **미시험**.
- Android wire 계약: 지정 필수 snake_case 필드/enum/nullable 형태와 detail 객체·422 배열이 일치. 실제 Kotlin 모델/화면은 허용 범위 밖이므로 읽거나 실행하지 않았다. 회전/백그라운드/취소 UX/갤러리/카메라/실제 LAN 연결 모두 PENDING.

## 실행 증거·환경

별도 snapshot: `/var/folders/69/r13269yn5wn5ydgqj5c6r0qh0000gn/T/howlens-w3-3hfsmgn7`.

| 항목 | 실제 버전 |
| --- | --- |
| Python | 3.14.2 |
| FastAPI / Starlette | 0.143.0 / 1.7.0 |
| Pydantic / pydantic-core | 2.14.0 / 2.50.0 |
| Uvicorn / python-multipart | 0.54.0 / 0.0.32 |
| Pillow / httpx | 12.3.0 / 0.28.1 |
| python-dotenv / pytest / anyio | 1.2.4 / 9.1.1 / 4.15.1 |

설치: 정확한 `backend/pyproject.toml`의 `[test]` 의존성을 snapshot의 `venv314`에 editable 설치. API 환경을 상속하지 않는 subprocess env allowlist(`PATH`, 임시 `HOME`, `PYTHONDONTWRITEBYTECODE`, 필요 시 snapshot `PYTHONPATH`) 사용. 테스트 실행 동안 `socket.socket.connect`의 AF_INET/AF_INET6 연결을 차단하여 MockTransport/ASGI/TestClient 외부 TCP 호출 불가. **외부 AI/유료 호출 0회**; 패키지 다운로드만 네트워크 사용.

실제 pytest 실행은 위 TCP 차단 wrapper에서 `pytest.main(['-q', 'tests/test_api.py', 'tests/test_openai_provider.py'])`: **54 passed, 1 warning, 1.84s, exit 0**. warning은 Starlette의 httpx TestClient deprecation이며 테스트 실패가 아니다. 첫 환경 생성 시 PATH 최소화로 시스템 Python/pip21이 선택되어 editable install 실패가 있었고, Python 3.14의 절대 interpreter로 **새 venv314**를 생성하여 해결했다; 제품 의존성이나 기대값은 변경하지 않았다.

- 원시 결과: `docs/assets/test-prep/W3-backend-tests.txt`.
- 추가 HTTP/취소 probe 원시 결과: `docs/assets/test-prep/W3-probe-results.txt` (**exit 1**, `decode_cancel_peak=4`).
- 재현 스크립트: `evaluation/w3_audit_probe.py`; Coordinator 요청에 따라 소유 경로에 보존. 공개 HTTP 응답 검사 외에 cancellation 원인 분리를 위해 **decode 함수만 테스트 내에서 지연 주입**한다. 제품 수정/실제 모델 호출 없음. 고정 snapshot test helpers를 쓰므로 범용 테스트 suite로 수집하지 않는다.

### 정확한 재현 명령

평가 저장소 루트에서, 아래는 이미 추출·설치한 감사 snapshot을 재사용한다:

```sh
W3_SNAPSHOT=/var/folders/69/r13269yn5wn5ydgqj5c6r0qh0000gn/T/howlens-w3-3hfsmgn7
env -i PATH=/usr/bin:/bin HOME="$W3_SNAPSHOT" PYTHONDONTWRITEBYTECODE=1 \
  "$W3_SNAPSHOT/venv314/bin/python" evaluation/w3_audit_probe.py "$W3_SNAPSHOT"
```

다른 PC에서는 pinned SHA에서 `backend/howlens/{main,models,safety,provider,openai_provider,manual_catalog,visual_boundary,config,store,__init__}.py`, `backend/tests/{test_api,test_openai_provider}.py`, `backend/pyproject.toml` **만** `git archive`로 별도 디렉터리에 추출하고 Python 3.11+ venv 및 `python -m pip install -e "$W3_SNAPSHOT/backend[test]"` 설치 후 같은 명령을 실행한다. 실제 backend `.env`나 다른 worktree를 복사하지 않는다. 재현 스크립트는 TCP를 차단하며 응답의 live 표기는 원래 backend test seam의 mode일 뿐 실제 AI 호출이 아니다.

## 남은 일 / 일정

Backend의 cancellation 수정은 Coordinator를 통해 담당 Worker에게 전달 완료; **수정 SHA는 본 감사 범위에서 아직 미시험**. 해당 SHA를 받으면 바뀐 관심사만 재검증한다. Galaxy baseline은 App PC에서 지금 진행하며, feature freeze 15:30 / packaging 16:30 / 제출 17:00 KST를 유지한다. 물리 장비를 기다리거나 이 평가 작업에서 실제 guide·이미지 생성·유료 호출 범위를 확장하지 않는다. 이 보고서와 재현 자료를 소유 브랜치에 commit/push하고 최종 SHA를 lifecycle 완료 메시지에 기록한다.
