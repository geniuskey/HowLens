# W4 combined release audit — 2026-10-09

**결론: 지정 소스 트리 조합은 75개 테스트 통과. 기본 test discovery와 wheel-only Visual 배포에는 명시적 제한이 있다.** 제품 main 통합/배포 승인은 Coordinator 판단이며, 이 감사는 실장비·실제 guide·이미지 의미 검증을 승인하지 않는다.

## 정확한 입력 / 격리

- Backend **aba3e9e0fc81597228da3349511a16688a6de3ae**의 `backend/`.
- Visual **63dd0f80155b120fcd9bd07f7fb3bf6cf75773dc**의 `backend/visual/`만 합성.
- Evaluation 시작점 **0234159c2a5b4cc9443e79de30e90ef5586661d2**, branch `Runixs/eval-foundation`; 과거 image-model 준비 파일 그대로 보존.
- 실제 snapshot `/var/folders/69/r13269yn5wn5ydgqj5c6r0qh0000gn/T/howlens-release-pmaay3q4`와 그 안의 전용 `venv`. 두 SHA의 `git ls-tree` 목록으로 범위를 확인하고 `git archive` 추출; `.env` 및 `.env.*` 제외. 실제 dotenv/key 파일 읽기·복사 없음.
- 설치 subprocess/test subprocess 환경은 `PATH`, 임시 `HOME`, `PYTHONDONTWRITEBYTECODE`만 전달. API key·paid enable·proxy 환경을 상속하지 않았다. 테스트 helper의 synthetic dotenv만 임시 생성됨. 테스트/경계/wheel import probe는 Python TCP connect를 차단하며 HTTP는 MockTransport/ASGI/TestClient만 사용. 패키지 다운로드 외 외부 요청 없음, 실제 AI/유료 호출 **0회**.

## 실제 수집·실행 결과

| 실행 | 결과 | 의미 |
| --- | --- | --- |
| 기본 `pytest --collect-only -q` | exit0, **58 collected** | pyproject `testpaths=[tests]`로 Visual17 누락 |
| `pytest --collect-only -q tests visual/tests` | exit2, **66 collected + 1 collection error** | 양쪽 `test_openai_provider.py`가 동일 module 이름으로 충돌; 캐시 삭제로 해결한 것이 아님 |
| `pytest -q --import-mode=importlib tests visual/tests` + helper paths | exit0, **75 passed = Backend58 + Visual17**, **14 subtests passed**, 10 warnings, **1.02s** | 양쪽 코드가 실제로 함께 수집·실행됨; importlib와 명시 경로가 이번 후보 검증 recipe |
| root source launch public boundary probe | exit0 | `backend.visual.service`의 startup/mapper/split/quality gate 연동 |
| backend-directory source launch public boundary probe | exit0 | `visual.service` fallback의 동일 연동 |
| wheel build | exit0 | `howlens_backend-0.1.0-py3-none-any.whl` 생성 |
| 소스 트리 밖 wheel-stage import | **exit1**, `ModuleNotFoundError: visual` | manual catalog는 포함되나 Visual package 없음 |

warnings는 Starlette TestClient/httpx deprecation 1회, Pillow `getdata` deprecation 9회다. supplied suite 안의 cancellation regression은 전체58에 포함되며, Coordinator가 이미 peak2를 확인한 **W3 별도 cancellation probe는 반복하지 않았다**.

실제 의존성: Python **3.14.2**, FastAPI **0.143.0**, Starlette **1.7.0**, Pydantic **2.14.0** / core **2.50.0**, Uvicorn **0.54.0**, python-multipart **0.0.32**, Pillow **12.3.0**, httpx **0.28.1**, python-dotenv **1.2.4**, pytest **9.1.1**, anyio **4.15.1**. Backend exact pins와 Visual의 Pillow>=11,<13 / httpx>=0.27,<1을 동시에 설치하여 호환됨.

## 새로 확인한 실제 통합 경계

`evaluation/release_audit/boundary_probe.py`는 기존 내부 알고리즘을 복사한 테스트가 아니라, 이전 양쪽 suite가 별도 주입으로 지나가던 **public startup→public scene mapper→real Visual service→real splitter→quality callback** 경계를 연결한다.

- `configured_app()` startup/health 통과, readiness의 `visual_integrated=false` 유지. `configure_same_process_visual()`은 승인 flag 없이 거절. 허용 호출에서는 **from_env factory만 fake provider로 대체**, 실환경 키를 읽지 않음.
- root와 backend cwd를 별도 process로 실행. 각 `public_module`이 선택한 service와 adapter가 같은 namespace를 사용해 provider 상태가 실제 생성 경계로 전달됨. 서로 다른 namespace를 임의 혼합한 startup까지 보증하지 않음.
- 3개 TEST step의 mapping은 **s0,s0,s0,s1,s1,s1,s2,s2,s2**. 공개 `scene_step_ids` 호출을 spy로 확인하고 실제 prompt의 scenes, 반환 IDs, reviewer 수신 IDs가 동일함. 서로 다른 색의 synthetic PNG 9 cells를 실제 splitter로 나눠 row-major 색/위치를 확인.
- stop / needs_more_information / mock guide는 provider 추가 호출 없이 거절. 별도 reviewer reject 시 실패하고 저장된 analysis/text는 변경되지 않음. provider는 성공1 + reviewer rejection1 = **fake call2**이며 live semantic approval이 아님.

## 발견 사항 / 담당 액션

1. **지속 CI 설정 gap, 후보 검증 workaround 있음.** 기본 pytest는 Visual을 누락하고 단순 양쪽 경로 지정은 파일명 충돌. Coordinator가 이번 후보에 명시적 importlib recipe 사용을 수용했으며, 후속 Backend/Visual CI 담당자는 공통 수집 설정 또는 package/test 명명 방식을 결정한다. 평가 Worker가 product config를 수정하지 않았다.
2. **wheel-only 배포에서 Visual 누락 재현.** `backend/pyproject.toml` setuptools include가 `howlens*`뿐이며 Visual 별도 distribution 설정은 없다. built wheel의 `visual/`와 `backend/visual/` member가 모두 빈 배열; 외부 cwd에서 wheel contents를 sys.path 우선으로 두면 howlens는 wheel-stage에서 import되지만 public Visual resolver는 실패한다. Backend/Visual 담당자가 wheel 포함/별도 패키지 또는 source checkout 배포를 명시해야 한다. **현재 source-tree candidate 통과와 wheel delivery blocker를 구분**하며 Coordinator에 전달 완료.
3. **실제 HTTP Visual은 여전히 fail-closed stub.** `main.py`는 verified stored live guide만 허용하고, Visual 요청은 202 failed/null URL/empty panels. 실제 generate_reviewed_assets 및 startup helper는 준비된 public boundary지만 configured_app/HTTP route에서 자동 호출되지 않는다. 두 코드를 합치는 것만으로 이미지 생성 기능이 활성화되지는 않는다.

## 9패널 / retry / 원문 / URL 위험 검토

- public mapper가 이제 우선이며 Backend 자체 fallback은 injected test double용. Visual은 1–9 steps를 동일 공식으로 9 scenes에 배정한다. Backend의 mapper 반환값은 개수9·저장 step ID 존재를 검사하고 quality reviewer가 최종 의미 승인을 맡는다. 이 감사는 색상 fixture 기반 geometry만 확인했다.
- 현재 HTTP Visual 최초 실패와 사용자 재시도1회 이후 세 번째 요청은 기존 두 번째 job을 **202로 재사용**한다. 추가 생성 시도는 없음; 409 응답에만 의존하는 클라이언트는 안 된다. Backend58에 관련 stored text/photo preservation·retry·eviction 테스트 포함.
- Visual provider는 JSON URL 다운로드 fallback 없이 base64 PNG만 받아 byte/dimension/decode 검사 후 1024²→1023² 정규화한다. splitter는 최소96, 3배수, 20MP, 10MiB, still PNG를 제한한다. prompt는 승인된 step description만 쓰고 raw hint/evidence를 명령으로 사용하지 않는다.
- HTTP completed/asset serving은 아직 없어서 **실제 `/visual-assets/...` URL 경계·접근·cache·출처와 Android 다운로드를 시험할 대상이 없다**. DTO 문자열만으로 relative URL 안전을 보증하지 않으므로 완료 라우트 연결 때 same-server relative path 및 panel order/step references 확인이 필요하다. 현재 failed 응답은 image_url=null, panels=[]여서 외부 URL 노출은 재현되지 않음.
- `generate_reviewed_assets` timeout 기본30초, Visual adapter 기본120초: 연결 시 timeout 정책을 owner가 맞춰야 한다. splitter의 `to_thread` 실제 worker 취소/자원 상한은 이 변경 관심사에서 시험하지 않음. W3 decode 수정 효과를 새 이미지 splitter 경로까지 확대 주장하지 않는다.

## 재현 명령 / helper 경로

평가 repository root에서 아래 source recipe를 실행한다. 지정 SHA에는 실제 env 파일이 없음을 tree metadata로 확인했으며, 복사 대상은 다음 public 코드/테스트뿐이다:

```sh
RELEASE_SNAPSHOT=$(mktemp -d /tmp/howlens-release-XXXXXX)
git archive aba3e9e0fc81597228da3349511a16688a6de3ae backend/howlens backend/tests backend/pyproject.toml | tar -x -C "$RELEASE_SNAPSHOT"
git archive 63dd0f80155b120fcd9bd07f7fb3bf6cf75773dc backend/visual | tar -x -C "$RELEASE_SNAPSHOT"
python3 -m venv "$RELEASE_SNAPSHOT/venv"
env -i PATH=/usr/bin:/bin HOME="$RELEASE_SNAPSHOT" "$RELEASE_SNAPSHOT/venv/bin/python" -m pip install -e "$RELEASE_SNAPSHOT/backend[test]" -r "$RELEASE_SNAPSHOT/backend/visual/requirements.txt"
env -i PATH=/usr/bin:/bin HOME="$RELEASE_SNAPSHOT" PYTHONDONTWRITEBYTECODE=1 "$RELEASE_SNAPSHOT/venv/bin/python" evaluation/release_audit/offline_pytest.py "$RELEASE_SNAPSHOT" -q --import-mode=importlib tests visual/tests
env -i PATH=/usr/bin:/bin HOME="$RELEASE_SNAPSHOT" PYTHONDONTWRITEBYTECODE=1 "$RELEASE_SNAPSHOT/venv/bin/python" evaluation/release_audit/boundary_probe.py "$RELEASE_SNAPSHOT" root
env -i PATH=/usr/bin:/bin HOME="$RELEASE_SNAPSHOT" PYTHONDONTWRITEBYTECODE=1 "$RELEASE_SNAPSHOT/venv/bin/python" evaluation/release_audit/boundary_probe.py "$RELEASE_SNAPSHOT" backend
```

`offline_pytest.py`가 cwd를 snapshot/backend로, import paths를 **snapshot/backend**, **snapshot/backend/tests**, **snapshot/backend/visual/tests**로 설정하고 TCP 차단 후 pytest를 호출한다. 기본 수집 문제 재현은 같은 helper의 pytest 인자를 `--collect-only -q` 또는 `--collect-only -q tests visual/tests`로 교체한다.

```sh
env -i PATH=/usr/bin:/bin HOME="$RELEASE_SNAPSHOT" "$RELEASE_SNAPSHOT/venv/bin/python" -m pip wheel --no-deps "$RELEASE_SNAPSHOT/backend" -w "$RELEASE_SNAPSHOT/wheel"
env -i PATH=/usr/bin:/bin HOME="$RELEASE_SNAPSHOT" PYTHONDONTWRITEBYTECODE=1 "$RELEASE_SNAPSHOT/venv/bin/python" evaluation/release_audit/wheel_probe.py "$RELEASE_SNAPSHOT/wheel/howlens_backend-0.1.0-py3-none-any.whl"
```

wheel probe는 별도 임시 stage에 distribution을 풀고 source cwd 없이 `python -I`로 실행한다. 해당 import 오류를 exit1로 내며 제품 수정은 하지 않는다.

원시 증거: `docs/assets/release-audit/{default-discovery,combined-default-import,combined,boundary-root,boundary-backend,wheel,versions}.txt`. 세 helper와 본 보고서만 새 평가 코드/문서이며 main/worker branch merge 없음. 실제 guide/모델 생성·semantic review·실물 장비/폰 제어·LAN/API 실서비스 테스트는 **미실행**. source 후보 통합 판단과 packaging follow-up은 Coordinator에게 넘긴다.
