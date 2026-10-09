# 이미지 모델 비교 후보 — 2026-10-09 확인

**승자 미선정. 실제 접근 권한·결과·속도·비용은 아직 측정하지 않았다.** 동일 프롬프트 3개 × 접근 가능한 모델 2개만 먼저 시험하고 팀장 기준을 받기 전에는 유료 실행하지 않는다. 가격은 USD Standard/API 기준이며 9개 패널을 한 이미지로 생성할 때의 비용이다. 패널별 9회 생성 비용과 혼동하지 않는다.

| 후보 / 현재 역할 | 출력 비용과 별도 입력 요금 | 접근·기능·비교 주의점 |
| --- | --- | --- |
| OpenAI `gpt-image-1.5` / 기존 adapter baseline | 1024² 출력 low $0.009, medium $0.034, high $0.133; text input $5/M, image input $8/M, cached 각각 $1.25/$2, image output $32/M | 기존 adapter 기본 모델. PNG·low/medium/high의 같은 설정으로 baseline 비교 가능. **2026-12-01 종료 예정**, 현재 owner 프로젝트 접근은 미확인 |
| OpenAI `gpt-image-1-mini` / 가격 baseline | 1024² 출력 low $0.005, medium $0.011, high $0.036; text input $2/M, image input $2.50/M, cached $0.20/$0.25, image output $8/M | 기존 allowlist에 있음. 비용이 낮다고 9장면·설비 형상이 더 적합하다는 뜻은 아님. **2026-12-01 종료 예정** |
| OpenAI `gpt-image-2.5-sunburst` / 현재 세대 비교 후보 | text input $5/M, image input $8/M, cached $1.25/$2, image output $30/M. **고정 장당 가격 미가정**; 실제 출력 토큰 보정 필요 | 공식 모델은 생성·편집 및 low/medium/high/xhigh/max/auto 지원. 기존 adapter는 low/medium/high만 노출. alias 또는 공식 snapshot `gpt-image-2.5-sunburst-2026-09-08` 접근 확인 필요; 현 adapter allowlist는 alias만 지원 |
| Google `gemini-3.1-flash-image` / 대안 1개 | text/image input $0.50/M; text/thinking output $3/M; image output $60/M. 1K=1120 output tokens→**$0.0672**(공식 표시 약 $0.067), 2K $0.101, 4K $0.151 | 별도 Google API billing/access 필요. 기존 OpenAI key/크레딧을 쓸 수 있다고 가정하지 않음. 1:1·1K 조건으로 비교 준비; 반환 MIME/정확 크기 기록 및 PNG 정규화 필요. 동일한 quality 이름으로 동등하다고 간주하지 않음 |

근거: [GPT Image 1.5 가격](https://developers.openai.com/api/docs/models/gpt-image-1.5), [GPT Image mini 가격](https://developers.openai.com/api/docs/models/gpt-image-1-mini), [OpenAI 종료 일정](https://developers.openai.com/api/docs/deprecations), [GPT Image 2.5 Sunburst 기능·가격](https://developers.openai.com/api/docs/models/gpt-image-2.5-sunburst), [Gemini API Standard 가격](https://ai.google.dev/gemini-api/docs/pricing), [Google 이미지 생성 API](https://ai.google.dev/gemini-api/docs/image-generation). 모두 2026-10-09 실제 열람. OpenAI API 조직 확인이 필요할 수 있으며 key 보유가 모델 접근이나 실행 승인 증거는 아니다. [OpenAI 이미지 가이드](https://developers.openai.com/api/docs/guides/image-generation)

공식 BFL 개요/가격 페이지도 확인했으나 이번 열람에서 확정 가능한 세부 가격 표를 얻지 못해 숫자를 추정하거나 후보를 늘리지 않았다. 대안은 Google 한 개만 shortlist에 포함했다. Imagen/이전 Gemini 모델로 현재 후보를 대체하지 않는다. 공급자의 “fast”, “best” 표현은 latency/fit 보증으로 쓰지 않는다.

## 현재 Visual 구현과의 차이

read-only 확인 SHA **63dd0f80155b120fcd9bd07f7fb3bf6cf75773dc**의 `backend/visual/{service,openai_provider,splitter}.py`:

- `POST /v1/images/generations`, `n=1`, opaque PNG, 1024²/1536×1024/1024×1536, low/medium/high; timeout 기본 120초(설정 1–300초), 자동 retry 없음.
- 원본 요청 크기를 검증하고 각 변을 3의 배수로 resize한다. 1024²→1023²→패널 341². 원본과 정규화 결과를 각각 저장·평가해야 글자/화살표 손상을 구분할 수 있다. grid를 자를 수 있다는 사실은 실제 9장면 경계가 정확하다는 증거가 아니다.
- prompt는 기존 1–9 step을 `index * step_count // 9`로 반복 배정하고 **텍스트·라벨·숫자를 금지**한다. 따라서 벤치마크의 `sandbox_labels_arrows`는 제품 동작 변경 제안이 아니라 별도 연구 조건이다. App overlay 글자 선명도와 모델 생성 글자도 분리한다.
- adapter는 **텍스트 생성 요청만** 제공하며 참조 이미지 편집 입력을 전달하지 않는다. topology 비교는 합성 텍스트 명세 조건부터 시작한다. image-reference 트랙은 권한/해시·별도 edit adapter 준비가 필요하고 text-only 점수와 합치지 않는다.
- 반환은 PNG bytes뿐: API usage/cost/request ID/end-to-end latency를 노출하지 않는다. 후속 sandbox runner는 이 메타데이터를 별도 기록해야 실제 비용 비교가 가능하다. 제품 소스는 수정하지 않았다.
- benchmark는 `generate_storyboard`에 fake live guide를 주입하지 않으며 제품 라우트·store·승인 gate와 분리한다. owner가 나중에 승인한 sandbox 호출 결과조차 제품 안전 승인이 아니다.

## 예산과 보정

`C = uncached_text*rate + cached_text*rate + uncached_image*rate + cached_image*rate + text_output*rate + image_output*rate`, 토큰 요금은 모두 `/1e6`. 출력 토큰을 알면 장당 출력 proxy를 더하지 않는다. 출력 토큰을 모를 때만 고정 설정의 proxy 사용. 입력 이미지 비용은 해상도만 보고 0으로 추정하지 않으며 tokenizer/usage로 보정한다. 실패/timeout도 청구 여부를 확인할 때까지 비용 null; 무료로 기록하지 않는다.

예: 1.5 low + mini low, 각 동일 3 prompts, 프롬프트당 **가정** 500 uncached text tokens·참조 0·retry 0 → 1.5 $0.0115/건 + mini $0.006/건 → 6건 **추정 $0.0525**. 이는 실측도 비용 상한 보장도 아니다. 2.5는 출력 토큰을 보정하기 전 총액 미정. Gemini에 thinking/text output이 있으면 이미지 가격에 추가한다. 실비에는 실패/사용자 retry/세금·환산(해당 시)을 별도 기록한다.

사용자 설명의 서버 $50 및 추가 인당 $50는 **잔액 조회 결과가 아니며 자동 공동예산이 아니다**. 계정 전환·키 복제 없이 Backend PC owner가 자기 프로젝트의 접근/잔액을 확인한다. 팀장 기준·모델 2개·settings·작은 pilot cap(제안 $1, token-only 모델은 calibration 후 결정)·수동 retry 정책을 확정한 다음에만 유료 실행을 별도로 승인받는다. 처음부터 모든 모델×해상도×quality 조합을 펼치지 않는다.
