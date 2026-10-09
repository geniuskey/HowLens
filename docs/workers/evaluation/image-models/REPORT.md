# W4-IMAGE-EVAL-PREP 결과 — 2026-10-09

[DONE] 이미지 모델 shortlist/가격·접근 matrix, 3개 합성 관찰-only benchmark, provider-neutral run/result schema, 비용 estimator와 자동 geometry/사람 의미 검토를 분리한 scorer 및 dry-run CLI를 준비했다. 오프라인 테스트 8개와 JSON Schema로 dry-run 6개 검증이 통과했으며 실제 모델 호출·장비 이미지 생성·live latency 측정·계정 접근 확인은 모두 미실행이다. 팀장 실제 기준과 접근 가능한 모델 2개·pilot cap을 확정한 후 Backend PC owner가 별도 sandbox 실행을 승인받는 단계가 남았으며 승자는 선정하지 않았다.

## 근거·소유권

- 평가 시작점 `8e69072bf7674ea3b935621d1d95af0639c22ca6`, `Runixs/eval-foundation` 유지.
- public Visual read-only SHA `63dd0f80155b120fcd9bd07f7fb3bf6cf75773dc`: service/openai_provider/splitter 3개만 확인. 공통 CONTRACT v0.1 재확인. 제품 변경 및 다른 연구 문서 접근 없음.
- OpenAI Docs 스킬의 공식 문서 조회 흐름 사용. 실제 열람한 2026-10-09 공식 문서와 정확한 가격·종료 일정 링크는 [SHORTLIST](SHORTLIST.md)에 항목별 기록.
- 허용된 세 경로에만 산출물 작성. live executor/key reader가 없고 계정/키를 수집·복사·전환하지 않았다. 유료 호출 **0회**. 사용자가 말한 $50+인당 $50는 실제 잔액이나 실행 승인으로 해석하지 않았다.

## 전달물

- [README](README.md): owner 실행 명령, 결과 기록 규칙, 자동/사람 checklist, latency/error/retry/usage/cost 산식 및 승인 대기 조건.
- [SHORTLIST](SHORTLIST.md): 현재 adapter baseline 1.5/mini, 현재 세대 2.5 Sunburst, 대안 Google 3.1 Flash Image. 대안은 1 provider만 채택; BFL 공식 페이지 확인으로 확정 가격을 얻지 못해 추정 숫자나 추가 비교군을 만들지 않음.
- `evaluation/image_models/protocol.py`, `test_protocol.py`, `requirements.txt`: dry-run/score/summary/estimate, 8개 회귀, Pillow·jsonschema 의존성.
- `cases.jsonl`, `run-result.schema.json`, 4개 `*.rate.json`, `usage.example.json`: 합성 명세/계획·결과 구조/가격 snapshot/가정 token 양.
- `docs/assets/image-model-eval/dry-run.jsonl`, `offline-validation.txt`: 실제 실행한 dry-run 6건과 원시 오프라인 검증 결과. 생성된 모델 이미지와 live 성능 데이터는 없음.

## 실제 검증

별도 임시 venv `/var/folders/69/r13269yn5wn5ydgqj5c6r0qh0000gn/T/howlens-w4-78pq1wng/venv`, Python 3.14.2, Pillow 12.3.0, jsonschema 4.26.0. 설치만 패키지 다운로드 사용; 실행은 로컬 파일/합성 white PNG fixture를 사용했다. 기본 Python에서 Pillow 미설치로 최초 1개 test error가 났고, 요구 의존성을 별도 venv에 설치한 뒤 기대값 변경 없이 해결했다.

실제 명령 (각 python은 위 venv interpreter, dry-run은 stdlib 기본 python에서도 확인):

```sh
python -m unittest evaluation.image_models.test_protocol -v
python -m evaluation.image_models.protocol dry-run > docs/assets/image-model-eval/dry-run.jsonl
python -m evaluation.image_models.protocol summary --results docs/assets/image-model-eval/dry-run.jsonl
python -m evaluation.image_models.protocol estimate --rate evaluation/image_models/gpt-image-1.5.rate.json --usage evaluation/image_models/usage.example.json
```

- **8 tests passed, 0.036s**, exit 0: row-major/크기/step mismatch 거부, automatic geometry≠semantic pass, no human→pending, unsafe human rating→reject, 원문 변경→reject, mock/dry latency 분리, n<20 percentile 미표시, retry/API error/unknown billing, output proxy와 output tokens 중복 청구 방지.
- JSON Schema Draft 2020-12 schema self-check 및 6 record validation exit 0. 정확한 Python 호출은 `offline-validation.txt`에 보존.
- Summary: `live_groups={}`, `dry_run_count=6`. 통계 테스트의 숫자는 unit fixture이며 모델 live latency가 아니다.
- Estimator: 1.5 low 1024² + **가정** text 500 tokens → estimated $0.0115, actual null. 이 결과로 실제 청구나 $50 내 실행 횟수를 보증하지 않음.

## 주의점·남은 입력

1. 팀장 평가 기준/가중치/통과선은 미수신. 현재 rating은 draft이며 가중 총점/모델 순위 없음. 모든 human rating=2인 candidate_pass도 product_safety_approval=false.
2. production prompt는 글자/번호를 금지하므로 화살표·한영 라벨 케이스를 sandbox track으로 분리했다. model+settings+track+criteria별 집계하며 개별 표본 수·실패를 보존한다.
3. 자동 검사는 PNG와 crop manifest의 기하학만 확인한다. 그림에 진짜 균등 9장면이 있는지·부품 연결이 맞는지·위험 행동이 추가됐는지는 blind human review 필요. 원문 hash 확인은 실제 App 텍스트 보존 시험을 대체하지 않음.
4. 기존 adapter는 reference edit 입력/usage·비용·request ID 반환을 노출하지 않는다. 후속 승인된 sandbox executor 및 메타데이터 기록 필요; 본 W4에서 제품 소스를 바꾸지 않음. alias allowlist가 모델 사용 가능성을 보증하지 않는다.
5. 공식 문서상 1.5/mini는 2026-12-01 종료 예정. 현재 key 접근/최신 모델 파라미터 compatibility는 owner 확인 전 pending. 비교 pilot은 **같은 3 prompts×2 available models, concurrency1, retry0**부터 시작하며 실측 전 승자를 정하지 않는다.
6. paid calls, 실제 image outputs, live API errors/retry rate/usage/cost, latency p50/p95, 실제 장비 fidelity 모두 **미실행/미측정**. 팀장 기준·모델 접근·비용 cap 결정 후 새 승인된 실행 작업으로 넘긴다.

본 보고서와 코드/데이터를 동일 브랜치에 commit/push하며 최종 SHA는 worker_done에 기록한다.
