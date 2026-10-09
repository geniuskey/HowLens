# Worker rules

공통 제품 규칙에 더해 모든 Worker가 이 파일을 읽는다. Coordinator의 내부 운영 문서는 읽지 않는다.

1. root AGENTS → 공통 RULES → 이 파일 → 자기 START → CONTRACT → 자기 TASK 순서로 읽는다.
2. 자기 소유 경로만 읽고 수정한다. 계약 또는 Coordinator가 정확히 지정한 의존 경로는 추가로 읽을 수 있다.
3. 새 브랜치/worktree에서 작업한다. main merge, force push, 다른 계정 실행은 하지 않는다.
4. 새 worktree의 기반 문서 commit을 확인한다. 오래된 clone이면 새 브랜치에서 원격 문서 기준으로 fast-forward만 하고 충돌·기존 수정은 보존한다.
5. Task 하나의 검증 가능한 결과를 완성한다. mock fixture는 합성·테스트 전용이라고 명시한다.
6. 사람 질문·계약 변경은 live preamble의 `ask`로 Coordinator에게 보낸다. 동료 사람에게 직접 승인·선택을 요청하지 않는다.
7. 제안에는 문제, 최소 변경안, 영향, 검증 근거를 붙인다. 선례 조사 요청을 Coordinator에게 전달할 수 있다.
8. 새 파일 시작 전, 테스트 후, 완료 직전에 자기 Dispatch inbox를 확인한다. heartbeat cadence는 live preamble을 따른다.
9. 완료 시 자기 `REPORT.md`에 요약·변경 파일·실제 검증·실패·브랜치/SHA·남은 의존성을 기록한다. 실행 방법은 `RUNBOOK.md`에 적는다.
10. live preamble의 명령으로 worker_done을 정확히 한 번 전송한다. Task/Dispatch ID와 explicit succeeded/failed를 포함한다.
11. worker_done 후 turn을 끝내고 idle한다. 스스로 다음 Task를 시작하거나 계속 poll하지 않는다.
12. 막히면 원인을 숨기지 말고 `[BLOCKER]` 한 줄과 최소 해결책을 보낸다. 같은 실패를 무한 반복하지 않는다.

보고 머리말: `[DONE]`, `[BLOCKER]`, `[QUESTION]`, `[CONTRACT]`, `[FYI]`.
출력은 3문장 요약, 수정 파일 경로, 검증 결과를 기본으로 한다. 토큰·키·이메일·로그 전문을 붙이지 않는다.
