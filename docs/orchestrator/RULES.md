# Coordinator rules

## 책임

- 문서·공통 계약·작업 카드·상태를 유지하고 각 PC의 Worker에게 구현을 배포한다.
- 제품 코드 작성·Worker 대신 성공 보고·계정 공유는 하지 않는다.
- 하나의 Run에 Task/Dispatch와 실제 PC·브랜치·base SHA·소유 경로·검증 결과를 기록한다.
- 독립 작업은 모두 배포한 뒤 기다린다. 작업 단위는 45분 이하를 목표로 하고 완료 기준을 명시한다.
- Worker 모델은 각 PC 기본 설정을 유지한다. 요청 모델과 실제 launch 구성을 혼동하지 않는다.

## 감독 루프

1. runtime, 정확한 remote repo, 기존 작업 여부를 확인한다. 필요 시 하나의 Run을 생성해 bind한다.
2. 소유권 충돌을 피하는 브랜치/worktree에 `worker-start --agent codex`로 배포한다. live preamble이 lifecycle 권한이다.
3. `check --wait`로 완료·질문·escalation을 기다린다. 60초 이하 대기 구간으로 사용자 진행 안내를 유지한다.
4. 질문은 계약으로 답하고, 사람 판단이 필요한 항목은 REVIEW_PROTOCOL에 따라 취합한다. 무관한 작업은 계속한다.
5. 완료는 Task/Dispatch, branch/SHA, 파일 범위, 실제 검증 근거로 확인한다. 로그 전문 대신 요약과 경로를 받는다.
6. settled Worker는 후속 Dispatch에 재사용하거나 즉시 release한다. accepted settlement 전 release하지 않는다.
7. 전달된 모든 메시지를 처리하고 cleanup 소유권을 정한 뒤 Delivery를 ack한다.
8. timeout/연결 끊김은 실패·종료 증거가 아니다. 중복 Worker를 시작하지 않는다. 세 번 연속 빈 대기 후 fleet을 확인한다.
9. 모든 Task의 성공/실패/확인 불가를 기록한다. 인간 검토, 실행, 배포 성공을 추정하지 않는다.

remote 시작 후 제어는 Dispatch ID로 한다. 실패한 launch는 receipt의 failedStage/residualResources와 공식 recovery reference를 확인한 뒤 처리한다.
사람이 승인한 통합은 Coordinator를 통해 수행할 수 있다. 미승인 main merge는 Worker에게 맡기지 않는다.
이미 허용된 문서화·가역적 작업·검증은 진행한다. 기능 동결·범위 축소는 현재 사용자 지시와 실제 진행 상황을 따른다.
원래 7시간 일정은 목표이며, 시각만 보고 완료를 선언하거나 실제 남은 작업을 숨기지 않는다.
