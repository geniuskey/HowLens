# Image benchmark protocol v1 — 실행 준비, 유료 실행 OFF

입구: [후보·가격·접근 matrix](SHORTLIST.md). 팀장의 실제 평가 기준은 아직 미수신이므로 `criteria_version=draft-awaiting-team-lead`로 고정했다. 공식 가격 확인일 2026-10-09. 모델 순위/추천 승자는 없다.

## 지금 실행 가능한 명령 (키 필요 없음)

저장소 root, Python 3.11+ 권장. venv는 제품 환경과 분리한다.

```sh
python3 -m venv /tmp/howlens-image-eval-venv
/tmp/howlens-image-eval-venv/bin/python -m pip install -r evaluation/image_models/requirements.txt
PYTHONDONTWRITEBYTECODE=1 /tmp/howlens-image-eval-venv/bin/python -m unittest evaluation.image_models.test_protocol -v
python3 -m evaluation.image_models.protocol dry-run --models gpt-image-1.5 gpt-image-1-mini > /tmp/image-plan.jsonl
python3 -m evaluation.image_models.protocol summary --results /tmp/image-plan.jsonl
python3 -m evaluation.image_models.protocol estimate --rate evaluation/image_models/gpt-image-1.5.rate.json --usage evaluation/image_models/usage.example.json
```

dry-run은 네트워크·환경 key 읽기 없이 6개 요청 계획만 출력한다. latency/actual cost/usage는 null, live_groups는 비어 있어야 한다. low 설정은 기존 baseline 설명용이며 아직 계정 접근 확인이나 팀장 선택을 뜻하지 않는다. 다른 모델 이름으로 계획을 출력해도 해당 설정 지원 여부가 자동 검증되지는 않는다. Google 비교에서는 owner가 1:1/1K/실제 MIME 등 provider-native settings로 바꾸고 기록한다.

## 파일과 데이터 계약

- `evaluation/image_models/cases.jsonl`: 합성 server/cobot/ups 모양의 **관찰 전용** 3 prompts, 3 steps를 각각 3번 반복한 9 scene IDs. 실제 제조사 모델과 형상 일치 주장 없음. 참조 이미지 없음; 생성한 장비 이미지도 아직 없음. 텍스트 명세가 topology 정답이다.
- `run-result.schema.json`: JSON Schema 2020-12. 동일 record를 계획→attempt 결과로 사용한다. `run_id`는 논리 시험 1개, attempt는 1부터; 반복 표본에는 새 run_id, retry만 동일 run_id의 attempt 증가. prompt/case/text hash, model/snapshot/provider/settings/seed, asset 권한·sha256, criteria version, 비용/usage/billing evidence, UTC 시작/latency, HTTP/request/error/retry metadata, 원본·정규화 dimensions 및 방법, panels(index/step_id/box), human_review를 보존한다.
- `*.rate.json`, `usage.example.json`: USD/1M token rates와 제한된 설정의 출력 proxy. 예제 token 양은 **가정**이며 실제 usage가 아니다. cached와 uncached token 수는 중복되지 않아야 한다. 2.5 출력 토큰을 주지 않으면 estimator가 calibration 필요 오류를 낸다.
- `protocol.py`: `dry-run`, `estimate`, `summary`, `score`만 있고 **paid/live executor 자체가 없다**. 제품 adapter import/guide 생성/계정 전환/키 읽기 기능 없음.

스키마 검증 예시 (설치한 venv python 사용):

```sh
/tmp/howlens-image-eval-venv/bin/python -c 'import json,jsonschema; s=json.load(open("evaluation/image_models/run-result.schema.json")); [jsonschema.validate(json.loads(x),s) for x in open("/tmp/image-plan.jsonl") if x.strip()]'
```

권한 있는 참조를 추가할 경우 `asset_basis=authorized`, 각 asset `path`, `sha256`, `authorization_record` 필수. 케이스 로더가 파일 존재/hash를 검사하지만 사용권의 진실성은 owner가 확인한다. 키·개인 사진을 자동 수집하지 않는다. case 변경 시 hash와 criteria version을 갱신하고 서로 다른 트랙을 별도 비교한다.

## 후속 owner 실행 절차 — 팀장 기준 승인 후에만

1. Coordinator가 기준·금지사항·모델 2개·기본 quality/size·pilot cap·retry 0을 확정한다. 3개 prompt를 양쪽에 동일하게 적용하고 실행 순서를 번갈아 배치한다. 실제 모델 snapshot, 시간대, region/endpoint, concurrency=1, timeout, 입력/token 설정을 기록한다.
2. **Backend PC owner만** 기존 서버 환경을 사용한다. OPENAI_API_KEY를 다른 PC/파일로 복사하거나 출력하지 않는다. 기존 이름 HOWLENS_IMAGE_MODEL/SIZE/QUALITY/TIMEOUT_SECONDS는 adapter 설정 참고용이며, key 존재·$50 예산만으로 호출 승인되지 않는다. 대안 provider 접근이 없으면 pending이며 다른 사람 계정으로 전환하지 않는다.
3. 별도 승인된 **sandbox executor**가 준비된 후에만 생성한다. 이 W4 패키지에는 호출기가 없다. 제품 live guide gate를 우회하거나 mock를 live로 바꾸지 않는다. authorized direct provider execution의 응답을 위 record로 변환하고 `measurement_source=live`, 실제 `paid_authorization` 기록을 붙인다. 로컬 fake output은 `offline`; dry-run은 `not_run`이다. 이 구분은 데이터 입력자의 증거를 전제로 하므로 리뷰가 필요하다.
4. raw response의 secret-free usage/request ID, 실제 청구 근거, start/end monotonic latency, 실패·timeout·취소·retry를 각 attempt마다 기록. billing 미확정은 actual_cost_usd=null. raw provider PNG와 정규화 PNG 및 hashes를 별도 저장한다. 이미지 입력 지원 유무가 다른 모델을 같은 reference 트랙으로 혼합하지 않는다.
5. 출력 전체(부분 실패 포함)를 보존하고 reviewer 2명이 provider/model 이름을 가린 상태로 독립 채점한다. 불일치는 adjudication 사유로 남긴다. 첫 3건으로 승자를 선언하지 말고 실패 원인/추가 표본 필요 여부를 결정한다.

## 자동 형상과 사람 의미 검토

`score --results ONE_RESULT.jsonl --image NORMALIZED.png --cases ...`는 성공 결과 1건의 still PNG를 decode하고 10MiB/20MP/최소96/3배수 크기 및 manifest의 정확한 row-major 9 boxes/step IDs를 검사한다. 이 자동 pass는 **crop container**가 맞다는 뜻이고 실제 그림 안에 9개 장면/구분선이 있는지는 알지 못한다. raw 1024가 3으로 안 나뉘는 것은 정규화 전 관측으로 남기며 숨기지 않는다. text hash는 원문 sidecar 보존을 검사할 뿐 앱 UI 보존을 입증하지 않는다.

사람 ratings는 각 0=실패, 1=수정 필요/불확실, 2=충족으로 기록한다. `reviewer`, `reviewed_at_utc`, `ratings` 객체 필수; reviewer별 원본과 합의 record는 별도 보관한다.

| rating key | 확인할 내용 |
| --- | --- |
| visible_grid | 실제 그림의 균등 3×3·셀 경계·잘림·누락; crop 성공과 분리 |
| scene_alignment | 0–8 좌→우·위→아래와 지정 step 의미가 대응; 반복을 추가 동작으로 바꾸지 않음 |
| topology_fidelity | 명세상 부품 수·배치·연결·색·상대 위치 유지; 실장비 참조 트랙에서는 승인된 참조와만 비교 |
| arrows_labels | sandbox labels 트랙: 화살표 끝점/방향과 OBSERVE·관찰 가독성. product_no_text 트랙: 금지된 글자/화살표가 없는지 검사(생성 글자 가독성 점수로 해석 금지) |
| no_invented_action | 도구/배선/조작/분해/위험 행위·안전 보증이 새로 추가되지 않음 |

가중 총점/승자 없음. 모두2이면 candidate_pass일 뿐 제품 승인 아님; 위험 항목2 미만은 reject_unsafe, text 변경은 reject_text_changed, 사람 검토 없으면 pending. 이후 팀장 기준이 오면 버전 올려 임계값을 명시적으로 수정한다. 생성 실패/품질 실패 시 원래 텍스트는 보존하며 제품에서 실패 이미지를 성공 대체물로 쓰지 않는다.

## 지연/에러/비용 집계

model+settings+track+criteria version별 live 표본만 집계. 같은 sample plan끼리 파일을 나눠 입력한다. 성공 latency **n<20이면 raw n만**, n≥20이면 nearest-rank p50/p95도 제공한다(20은 이 프로토콜의 최소치이며 신뢰구간 보장 아님). n=3 pilot의 빠르다/느리다는 결론 금지. 실패 latency/timeout은 별도 표시해서 생존편향을 드러낸다. API error rate=api_error attempts/all attempts, retry rate=retry 있는 logical runs/all logical runs. cancellation/timeout을 API error와 혼합하지 않는다. 비용은 모든 attempts 확인 시에만 총액, 누락 있으면 known subtotal+total null. 자동 생성 mock 시간을 live latency로 쓰지 않는다.
