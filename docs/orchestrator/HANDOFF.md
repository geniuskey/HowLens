# Coordinator ownership handoff

사용자 승인: 김태완 Pro x20, GPT-6 Astra, 기존 Medium, YOLO. Coordinator는 `/Users/runixs/HowLens` main clone에서 작업한다. 기존 김의윤 Coordinator는 인계 프롬프트 accepted 뒤 구현/감독을 중단한다. 제품 코드는 Workers만 수정한다.

## Authority

기존 Run home은 김의윤 Hub, runtime `6f189372-0b29-4065-a2f2-cc27d192b26d`이며 Run은 `run_6351cb7363de`. 원 Coordinator handle은 `term_be75fa04-a11b-405a-afce-de3b5623829a`. 이 Run을 삭제/reset/복제하지 않는다. 새 PC가 원 Hub에 접근 가능한 연결을 `orca environment list`로 확인한다. 기존 Run을 원 home에서 bind/check하고 검증한다. 현재 Run이면 legacy takeover 플래그를 추정해 사용하지 않는다. 권한 이동이 지원되지 않으면 공식 guide/help와 실제 오류를 기록하고 owner와 연결 복구만 해결한다. 원 Hub Orca를 종료하지 않는다.

## Required next actions

1. common RULES, Coordinator START의 계약/규칙/STATUS와 이 인계만 읽는다. 다른 역할 문서는 Task에 필요할 때 지정 경로만 읽는다.
2. `orca skills get orchestration`, 필요 reference, `orca skills get orca-cli`를 읽고 기존 Run 연결/소비 권한을 확인한다. 새 Run/Task 중복 시작 금지.
3. Evaluation `ctx_acf34f2187a5`는 active. 동일 작업을 다시 시작하거나 터미널 종료하지 않는다.
4. Backend Task `task_2f454c0c4e2c`의 실패 Dispatch `ctx_ae22fd3172c6` 이후 작업을 재개한다. 기존 user_takeover 터미널/수동 작업 상태를 먼저 확인한다. 보존 checkpoint와 `docs/workers/backend/REPORT.md`를 읽고 package validation, upload/output bounds, 최종 테스트, branch push를 마무리한다. 필요한 fresh launch는 명시 YOLO와 `--retry-of`로 동일 worktree 배치한다.
5. Visual Task `task_c2a9fed3559c` / failed Dispatch `ctx_0f017aa04711`. 이미 danger-full-access/never였다는 Worker 보고가 있음. 권한 문제와 author/auth 문제를 혼동하지 않는다. 8 staged 파일을 보존하고 이윤재의 실제 Git author name/email 및 GitHub login 사용자 응답을 기다린다. 계정 도용/author 발명/전역 설정 변경 금지. 미응답 질문을 반복하지 않는다.
6. App 완료 branch를 fetch/read-only review하고 REPORT의 검증 한계와 diff를 포함해 concrete 사람 review 자료를 만든다. 승인 전 main 제품 merge 금지. 독립 Task는 계속 진행한다.
7. docs 상태/로그를 최신화하고 자체 권한으로 인계 수신 확인을 보낸 뒤 감독을 이어간다.

## Exact placements

| Role | Environment / Repo ID | Worktree | Task / Dispatch |
|---|---|---|---|
| App | 김의윤 local / `5c14b88b-9f3d-4104-ab16-4b650c3ba039` | `/Users/edwin/orca/workspaces/HowLens/feat-ui-foundation` | `task_667825f4524d` / `ctx_6459221af0a9` succeeded released |
| Backend | 이준영 / `1619fb71-419f-4426-b78a-ca54590917b4` | `/Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation` | `task_2f454c0c4e2c` / `ctx_ae22fd3172c6` failed |
| Visual | 이윤재 / `9663dbbf-dee2-4b8e-b09c-c854640f0f0f` | `C:/Users/luj01/orca/workspaces/HowLens/feat-visual-foundation` | `task_c2a9fed3559c` / `ctx_0f017aa04711` failed |
| Evaluation | 김태완 / `8b42bcb9-c339-4f12-9eab-f231c1d7f1bc` | `/Users/runixs/orca/workspaces/HowLens/eval-foundation` | `task_328754f0630e` / `ctx_acf34f2187a5` active |

전체 worktree selector는 `id:<repo-id>::<absolute-path>`. Remote worker 제어는 Dispatch ID 사용. Evaluation YOLO terminal은 `term_a1ea80c5-38b7-4de5-9240-623696027112`이며 Worker 전용이다. Coordinator로 재사용하지 않는다.

## Settlements and user questions

`delivery_b2c5dc5008d8`에는 Backend heartbeat, Visual FYI (already YOLO), Visual failed worker_done, Backend failed worker_done, App succeeded worker_done가 있었다. 모두 처리했고 ack했다. App release closed_agent_terminal + transcript archive captured. Backend/Visual release는 user_takeover retained; close를 우회하지 않는다. 새 inbox는 다시 확인한다.

원 사용자에게 이윤재 author name/email(or GitHub noreply) + Windows GitHub login 질문을 올렸으며 아직 응답 미확인. 이 정보가 들어오면 새 Coordinator에 전달할 것. YOLO 원격 Worker 3대 적용은 이미 명시 승인되었으므로 재승인 질문 금지.

모든 테스트는 Worker 보고이며 Coordinator diff 검토와 통합 검증은 별도다. 지급 크레딧 2500은 잔액이 아니다. 실제 AI 키/제품 API 예산과 실물 E2E는 미확인이다.
