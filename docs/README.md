# HowLens documentation

기준일: 2026-10-09. 제품 코드와 평가 결과는 생성·검증된 이후에만 완료로 기록한다.

## 역할별 읽기 경로

모두 `../AGENTS.md` → `common/RULES.md`로 시작한다. 이후 자신에게 배정된 행만 읽는다.

| 역할 | PC 담당 | 시작 문서 | 추가 공통 문서 |
|---|---|---|---|
| Orchestrator | 김의윤 Hub, 현재 대화 | [orchestrator/START.md](orchestrator/START.md) | 계약, 팀 구성, 작업 상태 |
| App Worker | 김의윤 Hub의 별도 Codex | [workers/app/START.md](workers/app/START.md) | 계약, Worker 규칙 |
| Backend Worker | 이준영 PC | [workers/backend/START.md](workers/backend/START.md) | 계약, Worker 규칙 |
| Visual Worker | 이윤재 PC | [workers/visual/START.md](workers/visual/START.md) | 계약, Worker 규칙 |
| Evaluation Worker | 김태완 PC | [workers/evaluation/START.md](workers/evaluation/START.md) | 계약, Worker 규칙 |

이는 역할별 문서 로딩·수정 범위 규칙이다. Git 저장소 자체의 파일 접근 권한을 분리하는 ACL은 아니다.

## 문서 트리

```text
docs/
  README.md
  common/                 모든 역할이 필요한 최소 공통 기준
    RULES.md
    CONTRACT.md
    TEAM.md
  orchestrator/           Coordinator만 읽는 운영 지침
    START.md
    RULES.md
    REVIEW_PROTOCOL.md
    STATUS.md
  workers/
    COMMON.md             Worker만 읽는 실행·보고 규칙
    app/                  START.md, TASK.md, RUNBOOK.md, REPORT.md
    backend/              START.md, TASK.md, RUNBOOK.md, REPORT.md
    visual/               START.md, TASK.md, RUNBOOK.md, REPORT.md
    evaluation/           START.md, TASK.md, RUNBOOK.md, REPORT.md
  plans/IMPLEMENTATION_PLAN.md
  decisions/              외부 근거와 결정 기록
    README.md
    0001-foundation.md
  reviews/                사람이 선택할 검토 자료
    README.md
  logs/DEVELOPMENT_LOG.md
```

Worker의 RUNBOOK과 REPORT는 해당 Worker가 실제 실행 결과로 작성한다. 디렉터리 전체를 자동으로 읽지 않는다.
제품 범위는 [구현 계획](plans/IMPLEMENTATION_PLAN.md), 네트워크 계약은 [CONTRACT](common/CONTRACT.md)를 따른다.
충돌이 있으면 사용자 지시 → 승인된 결정 → 계약 → 역할 Task 순서로 해결하고 Coordinator에게 알린다.
