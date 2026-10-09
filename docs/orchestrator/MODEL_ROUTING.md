# HowLens 모델 라우팅 — 속도와 완성도 우선

2026-10-09 12:18 KST. 사용자 최신 지시가 이전 기본 모델/500크레딧 보존 목표보다 우선한다. 토큰 절약을 작업 축소 이유로 쓰지 않는다. Astra는 Coordinator 기본이며, 복잡한 핵심 구현·디버깅·사용성 난제에만 예외 투입한다. 모든 작업을 Sol로 보내지 않는다.

## 조사 근거와 적용 한계

- [OpenAI Models](https://learn.chatgpt.com/docs/models): Astra는 가장 어려운 다단계 작업, GPT-6.1 Sol은 복잡한 지속 작업, Luna는 명확하고 반복 가능한 작업에 적합하다는 공식 구분. 실제 접근은 계정·클라이언트·rollout에 따라 달라진다.
- [공식 Model selection](https://learn.chatgpt.com/docs/model-selection): Sol Medium은 복잡한 기술 작업, Sol Extra High는 연결된 시각 시스템과 정교한 산출물, Astra Medium은 넓은 맥락과 안정적인 상호작용을 요구하는 작업에 제시된다. 높은 effort는 무조건 빠른 해결을 보장하지 않는다.
- [GPT-6 가이드](https://developers.openai.com/api/docs/guides/latest-model): Luna는 집중된 작업의 빠른 처리, Sol은 복잡한 코딩, Astra는 가장 어려운 추론에 대응한다. 적절한 검증 뒤 불필요한 반복 검사를 늘리지 말라는 지침도 적용한다.

위 공식 페이지를 실제 열람했다. 아래 세부 역할·시간 제한은 HowLens용 Coordinator 운영 판단이며, 모델별 동일 작업 속도/정확도를 실측한 벤치마크 결과가 아니다. Codex CLI 로컬 버전은0.162.0. API 모델 표/속도 옵션을 ChatGPT 로그인 계정의 사용 권한 증거로 대체하지 않는다.

## 작업 분류와 기본 배정

| 분류 | 예시 / 산출물 | 기본 모델·effort | 상향 조건 |
|---|---|---|---|
| ORCH | 우선순위, 의존성, 계약 충돌, 릴리스 판단 | GPT-6 Astra Medium | 여러 근거 충돌 시 High, 결정 후 Medium |
| EXEC | 명령 실행, 로그 추출, SHA 확인, 제한된 전송 복구 | GPT-6 Luna Low/Medium | 원인 불명 복합 실패는 Sol High |
| FOCUSED-CODE | API가 정해진 독립 모듈·작은 버그 | GPT-6 Luna High | 파일/상태 경계가 넓어지면 Sol Medium/High |
| INTEGRATION | Android 상태/API 연결, 백엔드 패키징, provider 경계 | GPT-6.1 Sol Medium/High | 핵심 흐름 차단·재현 난제이면 Astra High 예외 |
| UX-CRITICAL | 카메라→검토→분석→단계 안내, 오류 복구, 큰 글꼴 | GPT-6.1 Sol High, 시각 설계는 필요 시 xhigh | 구현 난도 높고 실사용 흐름에 직접 영향이면 Astra Medium/High 예외 |
| DEBUG | race, 취소/회전, lifecycle, 기기/프레임워크 충돌 | GPT-6.1 Sol High | 근거 있는 두 가설 실패 또는10분 무진척 + 핵심 경로이면 Astra High |
| VERIFY | 정해진 테스트/스크린샷/스키마·비용 계산 | GPT-6 Luna Medium/High | 테스트 설계·경계 판단은 Sol High, 복잡한 핵심 결함 감사만 Astra 예외 |
| RESEARCH | 공식 문서·버전·가격 추출, 출처 정리 | GPT-6 Luna Medium | 상충 근거/호환성 다중 제약은 Sol Medium/High; 일반 조사는 Astra 사용 안 함 |
| DEMO | 시연 순서, 체크리스트, 제출물 정리 | GPT-6 Luna Medium | 핵심 UX의 설명 구조/정확성 판단은 Sol High |

속도 모드는 해당 계정에서 확인되는 Fast를 우선한다. Standard가 더 빨리 착수 가능한 이미 실행 중 작업은 모델 변경 때문에 끊지 않고 checkpoint에서 전환한다. Ultrafast는 현재 계정 권한을 확인하기 전 사용 가능하다고 기록하지 않는다. Max/Ultra effort 일괄 사용 금지: 벽시계 시간 개선 근거가 있을 때만 제한된 난제에 적용한다.

## 상향·대체 규칙

1. Task마다 category, model_requested, model_observed, effort, owner, paths, base_SHA, first_checkpoint, acceptance를 기록한다. 요청값과 실제 화면/receipt를 분리한다.
2. 10분 안에 첫 증거(빌드, 재현, diff, 화면)를 만들고, 작업 단위는15–30분 기본/최대45분 checkpoint. 침묵·PTY ready는 진척 증거가 아니다.
3. 동일 실패를 무한 반복하지 않는다. 원인/시도/정확한 오류를 모아 분할 또는 상향한다. 이미 통과한 검증은 변경이나 새 결함 없이 반복하지 않는다.
4. Astra 예외는 Coordinator가 사용자 허용 조건에 맞는 이유를 Task에 기록하고 바로 실행한다. 같은 승인을 다시 묻지 않는다. 일반 문서/명령 작업으로 확대하지 않는다.
5. 계정이6.1 Sol을 거절하면 동일 모델 재시도 루프 금지. 실제 picker/launch에서 확인된6 Sol 또는 Luna를 사용한다. 핵심 난제는 사용 가능한 계정에서 read-only Astra 진단을 병렬화하고 제품 수정은 소유 Worker가 수행한다. 계정/키 공유 금지.
6. 모델 전환은 현재 파일/커밋 보존과 settlement 또는 명확한 idle checkpoint 뒤 공식 CLI/세션 선택으로 한다. active editor를 중복 생성하거나 현재 세션이 바뀌었다고 추정하지 않는다.
7. 개발용 모델 라우팅과 제품 이미지 생성 모델 평가/유료 API 예산은 별개다. 토큰을 아끼지 말라는 지시는 계정 접근 가능성이나 무제한 유료 API 지출 증거가 아니다.

## 현재 적용과 예외

| 현재 작업 | 관측 모델 | 적용 |
|---|---|---|
| Coordinator | 사용자 인계 Astra Medium Fast | ORCH 기준 유지 |
| App 디자인+두 탭 | Luna High Fast | 앱 PC6.1 Sol 거절 이력. 현재 구현 checkpoint 보존; 새 선택 전 실제 사용 가능 모델 확인. Coordinator가 핵심 UX 리뷰 |
| Backend 패키징 수정 |6.1 Sol Low, Fast off | 이미 실행 중, 다음 경계 통합 작업은 Medium/High 및 가능한Fast로 선택 |
| Visual benchmark runner |6.1 Sol default | 독립 구현, 실제effort 미확인. 다음 실행에Medium 요청/확인 |
| 앱 취소·상태·계약 감사 | Astra Medium Fast | 현재 핵심 상호작용 복합 결함 진단 예외. 단순 회귀 실행에 계속 사용하지 않음 |
| Android17 blocker 조사 | Astra Medium Fast | 이전 배정 완료 직전. 한정된 기기 테스트 blocker만 마무리, 후속 일반 조사는Luna/Sol |
| 전송 복구 | Luna High Fast, 완료 | 정확한0e2aa94 push/원격SHA 확인; 추가 반복 없음 |

## 남은 시간과 임팩트 우선순위

제출17:00. 최신 사용자 요청으로 이전15:30 feature freeze를 아래 일정으로 대체한다.

| KST | 목표 |
|---|---|
| 지금–13:00 | 두 탭/블루UI 첫 실제 APK, 서버·Visual 패키징 결함 수정, 평가 실행 준비 |
|13:00–14:00| 실제 흐름 연결, 이미지 후보 소규모 비교, 사람 검토 자료 완성 |
|14:00–14:30| 승인된 통합/핵심 결함 수정, 기능 동결 |
|14:30–15:30| 실기기 회귀·실패 복구·시연 리허설; 신규 기능 안 넣음 |
|15:30–16:00| APK/설치 설명/근거와 한계/시연 백업/제출 파일 확정 |
|16:00–17:00| 최소1시간 제출·환경 장애 대응 여유 |

임팩트의 중심은 '사진 또는 카메라에서 바로 시작 → 읽을 문장 최소화 → 근거가 있는 다음 행동 → 설명 이미지 → 전후 변화 확인'의 연결 완성도다. 장식 기능이나 미지원 메뉴보다 이 흐름의 속도·반응·복구를 우선한다. 실제guide 승인/실장비 근거가 없으면 완료로 꾸미지 않는다. 사진 탭과 카메라 탭은 모두 단일사진 요청이며 연속 영상 분석은 범위 밖이다.

독립 소유 경로/테스트 슬롯을 가진 준비된 작업은 가능한 만큼 병렬 배포한다. 동시 워커 수를 늘리기 위한 중복 구현·중복 조사·기기 동시 조작은 하지 않는다. 완료한 워커는 즉시 재사용하거나release하고 [BOARD.txt](BOARD.txt)에 상태를 갱신한다.
