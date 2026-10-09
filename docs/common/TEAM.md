# PC and role assignment

2026-10-09 사용자 제공 계정 정보. 아래 크레딧은 지급액이며 실시간 잔액 조회 결과가 아니다.

| PC / Orca 환경 | 등록된 HowLens clone | 역할 | 플랜 | 리셋권 | 지급 크레딧 |
|---|---|---|---|---|---|
| 김의윤 / local | `/Users/edwin/HowLens` | App Worker | Plus | 3 | 2,500 |
| 이준영 / `이준영` | `/Users/jymbook/HowLens` | Backend / AI / Safety Worker | Plus | 3 | 2,500 |
| 이윤재 / `이윤재` | `C:/2026_DSDN/HowLens` | Visual Worker | Plus | 3 | 2,500 |
| 김태완 / `김태완` | `/Users/runixs/HowLens` | Coordinator + 별도 Evaluation / Regression Worker | Pro x20 | 미확인 | 2,500 |

환경 이름은 영문 가칭이 아닌 실제 등록된 한국어 이름을 사용한다. 이준영 PC의 다른 SSH 저장소는 대상이 아니다.

## 소유권

| 역할 | 수정 허용 범위 | 다른 담당과의 경계 |
|---|---|---|
| Coordinator | `AGENTS.md`, 루트 문서 포인터, `docs/common/`, `docs/orchestrator/`, `docs/plans/`, `docs/decisions/`, `docs/reviews/`, `docs/logs/`, 초기 `.gitignore` | 제품 코드를 작성하지 않는다 |
| App | `android/`, `docs/workers/app/` | `android/.../visual/`은 Visual에 넘기기 전 별도 조율 |
| Backend | `backend/` 중 `backend/visual/` 제외, `docs/workers/backend/` | visual 패키지 파일·테스트는 Visual 소유 |
| Visual | `backend/visual/`, `docs/workers/visual/` | Backend 라우터는 Backend 담당; Android visual 변경은 Task별 명시 |
| Evaluation | `evaluation/`, `docs/assets/`, `docs/workers/evaluation/` | 제품 코드 수정 대신 실패를 담당 Worker에게 전달 |

## 모델과 예산

Coordinator 요청 설정: GPT-6 Astra, Medium, Standard. 이 문서는 실제 세션 설정 변경의 증거가 아니다.
Worker 모델·effort는 각 PC 설정을 유지하고 launch receipt로 실제 구성을 확인한다. 별도 지시 없이 일괄 변경하지 않는다.
김태완 계정의 Coordinator와 Evaluation Worker는 같은 한도·크레딧을 공유한다. 팀 크레딧을 한 계정으로 모아 계산하지 않는다.
각 계정에서 500크레딧을 최종 수정용으로 남기는 운영 목표를 둔다. 자동 과금 상한이 아니다.
리셋권은 사용 한도 복구와 크레딧 잔액을 구분해 관리한다. 제품 OpenAI API 예산은 별도 확인한다.

## 실행 권한 (사용자 승인, 2026-10-09)

Coordinator 현재 세션은 danger-full-access / approval never로 변경됨.
원격 Worker 3대 모두 YOLO 적용을 사용자가 명시적으로 승인했다.
실행 명령에 `codex --dangerously-bypass-approvals-and-sandbox`를 명시하고 현재 세션 권한이 원격으로 상속된다고 가정하지 않는다.
전환 시 진행 중인 파일을 보존하고 checkpoint/settlement 뒤 같은 worktree에서 새 Dispatch로 이어간다.
계정·전역 설정을 공유하거나 변경하지 않는다. YOLO여도 역할 소유권·안전·사람 리뷰 규칙은 그대로 적용된다.

## Coordinator handoff

사용자 지시로 김태완 PC의 main clone `/Users/runixs/HowLens`에 Coordinator를 이전한다. 별도 Coordinator worktree를 만들지 않는다. 기존 Evaluation worktree는 그대로 보존한다. Pro x20은 사용자 제공 플랜 정보이며 현재 잔액/리셋권은 미확인이다.
