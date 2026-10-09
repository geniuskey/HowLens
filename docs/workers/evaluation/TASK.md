# W1-EVALUATION — Contract fixtures and runner

- Target: `evaluation/`, `docs/assets/`, 자기 역할 문서.
- Change: 계약 v0.1에 맞는 명시적으로 synthetic/mock인 guide/needs_more_information/stop/verification/visual 실패 fixture. server/cobot/ups, 흐림·장비 불일치·근거 없음·prompt injection·위험·전후 불명확 평가 케이스 정의. 구조 검사와 비-guide steps 금지 검사, --base-url 선택 live API smoke runner. 실제 호출 없이도 fixture 검증 실행 가능.
- Constraints: mock guide 근거는 TEST 문서라고 표시하며 real guide 승인이 아님. 사진 미확보는 pending 처리. backend/app source 읽기·수정 금지. 실제 API와 사진 평가를 실행하지 않았으면 미실행으로 기록. 결과를 기대값에 맞춰 조작하지 않음.
- Ownership: `evaluation/`, `docs/assets/`, `docs/workers/evaluation/`.
- Acceptance: runner 실행으로 fixture 검증, non-guide steps가 있는 손상 fixture 거절, 참조 ID·visual 순서·verification enum 검증. live runner 미실행 구분. RUNBOOK/REPORT 작성, 자기 브랜치 commit/push. main merge 금지.

목표 45분. 실제 사진 세트와 제품 평가 round는 후속 Task.
