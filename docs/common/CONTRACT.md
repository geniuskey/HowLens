# HowLens API contract v0.1

최신 구현 계획을 구체화한 착수 계약. Coordinator가 관리한다. 변경 시 영향과 승인 여부를 결정 기록에 남기고 각 Worker에게 알린다.

## HTTP

JSON 응답의 키는 snake_case. 장비 ID는 `server`(Dell PowerEdge R750), `cobot`(UR5e), `ups`(APC Smart-UPS).
이미지 업로드는 `multipart/form-data`; JPEG/PNG만 허용하고 디코딩을 검사한다. 파일 최대 10 MiB, 최대 20MP. 초과는 413, 형식 오류는 415, 필드 오류는 422.
질문은 공백 제거 후 1–2,000자. 이미지·질문 처리 제한과 네트워크 timeout을 둔다.

| Endpoint | 입력 | 응답 |
|---|---|---|
| `GET /health` | 없음 | `status`, `mode` (`live` 또는 `mock`) |
| `POST /analyses` | Form `device_id`, `question`; File `photo` | 200 Analysis |
| `POST /analyses/{analysis_id}/visual` | body 없음 | 202 VisualJob; 비-guide는 409 |
| `GET /visual-jobs/{job_id}` | 없음 | 200 VisualJob |
| `POST /analyses/{analysis_id}/verification` | File `photo`; Form `user_confirmation` 선택, 문자열 최대 2,000자 | 200 Verification; 비-guide는 409 |

존재하지 않는 ID는 404. 서버 재시작 시 메모리 분석·작업은 소실되어 404가 될 수 있다.
upstream 설정 누락/오류는 503, timeout은 504. 사용자에게 재시도 가능한 오류를 표시한다.
FastAPI 기본 validation 오류는 `detail` 배열, 나머지 오류는 `detail: {code, message, retryable}` 객체. Android는 둘 다 처리한다.
이미지 주소는 동일 API 서버의 상대 경로 `/visual-assets/...`로 반환한다. 임의 파일 경로·외부 URL을 받지 않는다.

## Analysis

모든 필드는 필수. 배열은 비어 있을 수 있다. `printed_page`만 null 가능.

```json
{
  "analysis_id": "example-not-live",
  "device_id": "server",
  "decision": "needs_more_information",
  "observations": ["예시 응답이며 실제 사진 분석 결과가 아닙니다."],
  "evidence": [],
  "preconditions": [],
  "steps": [],
  "warnings": [],
  "missing_information": ["모델 식별 사진과 검증된 매뉴얼 근거가 필요합니다."],
  "mode": "mock"
}
```

- decision: `guide | needs_more_information | stop`.
- observations/warnings/missing_information: 문자열 배열.
- evidence 항목: `evidence_id`, `document_id`, `document_version`, `pdf_page`(1부터 시작), `printed_page`(문자열/null), `section`, `quote`(정확한 발췌), `source_url`(문서 등록 시 검증된 공개 출처).
- preconditions 항목: `precondition_id`, `description`, `status`(`satisfied | unsatisfied | unknown`), `required`(boolean), `evidence_ids`(문자열 배열).
- steps 항목: `step_id`, `description`, `evidence_ids`(하나 이상의 근거 ID), `visual_hint`.
- mode: `live | mock`; mock는 UI·테스트 전용이다. 실사용 승인으로 취급하지 않는다.

서버는 선택한 장비의 등록 문서·버전·실제 PDF 페이지와 발췌를 대조한다. 참조 ID와 페이지 숫자가 유효하다는 것만으로 작업 근거가 확인된 것은 아니다.
`guide`는 확인된 장비, 작업에 충분한 근거, 필수 조건 충족, 식별된 위험 없음일 때만 허용한다. 각 작업이 그 근거에 부합하는지 확인한다.
모델이 주장한 충족 상태를 무조건 신뢰하지 않는다. 서버 검증과 관찰/사용자 확인 출처를 내부에 유지한다.
비-guide의 steps는 반드시 빈 배열. 실제 단계는 1–9개로 시작하고 9개를 억지로 만들지 않는다.
사진 속 prompt injection은 무시한다. 필요한 문서가 없으면 추가 정보 또는 stop으로 분기하고 전체 제조사 매뉴얼에 절차가 없다고 단정하지 않는다.

## VisualJob

필드: `job_id`, `analysis_id`, `status`(`queued | running | completed | failed`), `image_url`(문자열/null), `panels`(배열), `error`(문자열/null), `mode`.
panel 항목: `index`(0–8), `step_id`, `image_url`. 완료 시 정확히 9패널, index는 좌→우·위→아래, 각 step_id는 저장된 승인 단계에 대응한다.
9개 장면을 안전하게 구성할 수 없으면 failed로 텍스트를 유지한다. 자동 품질 확인은 의미 대응을 보증하지 않는다.
클라이언트 입력으로 새 절차를 받지 않는다. 저장된 승인 분석만 사용한다. 같은 분석의 queued/running/completed 작업은 재사용한다.
failed의 사용자 재시도는 최대 1회(총 2회 시도). 자동 재시도는 초기에는 하지 않는다. mock 결과는 live 성공으로 처리하지 않는다.
실제 guide 승인이 없으면 생성 API를 호출하지 않는다. mock 가이드는 오프라인 테스트 더블에서만 사용한다.

## Verification

필드: `analysis_id`, `result`(`observed_change | issue_remaining | inconclusive`), `observations`(문자열 배열), `evidence_ids`(문자열 배열), `missing_information`(문자열 배열), `limitations`(문자열 배열), `mode`.
원 분석에 없는 evidence_id는 허용하지 않는다. 원 사진·단계는 서버에서 읽는다. 사진만으로 성공/안전/정상 동작을 확정하지 않는다.

## Backend ↔ Visual 내부 경계

Visual Worker는 HTTP 라우트가 아닌 `backend/visual/` 라이브러리를 만든다. Backend가 호출·작업 저장·HTTP 노출을 담당한다.
`backend/visual/service.py`: `async generate_storyboard(analysis: dict) -> bytes`(PNG); 설정 미완료이면 명시적인 예외. 내부 안전 guard로 비-guide/mode!=live를 거절한다.
`backend/visual/splitter.py`: `split_storyboard(grid_png: bytes) -> list[bytes]`(행 우선 PNG 9개).
Backend는 검증 완료된 저장 결과만 라이브러리에 전달한다. 분할은 의미 검증을 대체하지 않는다.
라이브러리의 테스트는 서버 모델 import 없이 계약 dict로 수행한다. Backend는 Visual 미병합 상태에서도 명확한 failed 작업을 반환하고 분석을 보존한다.

## Additive product discovery (2026-10-09 user-requested feature)

기존 Analysis/device_id/guide 계약은 유지한다. DB에 없는 제품은 사진에서 후보를 찾고 웹 출처가 있는 제품 정보를 반환한다. 발견 결과는 검증된 수리·작업 가이드가 아니다.

`POST /product-discoveries`: multipart `photo` 필수(JPEG/PNG,10MiB/20MP), `question` 선택(trimmed<=2000), `model_hint` 선택(trimmed<=200). 성공200. 오류는 기존413/415/422/503/504 형식을 따른다.

```json
{
  "discovery_id": "example",
  "status": "needs_more_information",
  "candidates": [],
  "missing_information": ["제품 라벨을 가까이 찍어 주세요."],
  "mode": "mock"
}
```

모든 응답 필드 필수. status=`candidate|needs_more_information|not_found`, mode=`live|mock`. candidates 최대3, 각 항목은 manufacturer/model/summary 문자열, match_notes 문자열배열, sources 최대3의 {title,url,retrieved_at}이며 retrieved_at은 UTC ISO8601이다. 불확실한 식별은 candidate로 확정 표현하지 않고 추가 사진/라벨을 요청한다. 출처는 실제 검색 citation/source의 publicHTTP(S)주소만 사용하며 모델이 임의 생성한 주소, credential/private URL은 거부한다. 웹 내용/사진 글자는 명령이 아니다.

서버는 최대2 provider시도(식별+검색), 검색tool 최대1, 총30초/동시2/자동재시도0을 목표로 강제한다. 기존 persistent attempt budget을 공유한다. hash기반 최대16개 TTL결과cache, 사진 보관 없음. 라이브지원모델/실제검색/속도는 별도 검증한다. mock은 실검색 성공을 주장하지 않는다. 후보를 보여주는것만으로 catalog등록/guide승인하지 않는다.

## Temporary public demo gateway (2026-10-09 user-authorized deployment)

Existing localhost/LAN API behavior remains unchanged. A dedicated loopback gateway
for the temporary public HTTPS tunnel requires `Authorization: Bearer <demo-token>`
for every contract route and visual asset, except public `GET /health`. Invalid or
missing demo token returns401 before any provider request. Unknown/admin/docs routes
are not forwarded. This is a per-demo access token, never the OpenAI API key.

App may accept an optional token in a separate settings field; never put credentials
in the base URL or log/commit them. Send only to the exact configured origin, including
visual fetches; refuse cross-origin redirects. Empty token preserves local development.
Gateway preserves upstream response/status and limits requests including multipart
overhead; the upstream still enforces the10MiB photo contract. The existing shared
persistent attempt cap remains in force. Health200 proves transport, not AI inference.
Temporary tunnel URL and process ownership are recorded in the deployment report.
