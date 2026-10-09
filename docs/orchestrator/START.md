# Coordinator start

**Latest lifecycle: user-requested full shutdown. Read `docs/orchestrator/RESTART.md` before resuming the historical HANDOFF.**

Read, in order:
1. `docs/common/RULES.md`
2. `docs/common/TEAM.md` and `docs/common/CONTRACT.md`
3. `docs/orchestrator/RULES.md`
4. `docs/orchestrator/REVIEW_PROTOCOL.md`
5. `docs/orchestrator/STATUS.md`

현재 Coordinator 이전 대상은 김태완 PC의 main clone `/Users/runixs/HowLens`. 요청 모델은 GPT-6 Astra / Medium / Standard, YOLO.
진행 중 인계는 `docs/orchestrator/HANDOFF.md`부터 확인한다. 기존 Run home은 김의윤 Hub에 남아 있으므로 새 Run이나 중복 Worker를 만들지 않는다.
구현 배포 때만 대상 Worker의 `TASK.md`를 읽는다. 모든 Worker 문서·로그를 통째로 읽지 않는다.
Orca 실행 전 버전 일치 스킬 `orca skills get orchestration`을 따른다. 연결 PC는 TEAM의 실제 환경 이름으로 지정한다.

사용자 요청 현황판: `docs/orchestrator/BOARD.txt`. Worker 메시지를 처리하여 상태가 바뀌면 현황판의 시각·근거를 갱신하고 대화에도 ASCII 표를 출력한다. heartbeat만으로 진척/완료를 추정하지 않는다.

모델/effort 및 시간 배정 시 `docs/orchestrator/MODEL_ROUTING.md`를 따른다. 최신 사용자 기준은 토큰보다 속도·완성도, Astra는 Coordinator 및 명시된 핵심 난제 예외, 기능 마감14:30/제출 준비16:00이다.
