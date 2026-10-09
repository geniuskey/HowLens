# W1-VISUAL — Visual library foundation

- Target: `backend/visual/service.py`, `splitter.py`, 그 아래 tests.
- Change: CONTRACT 함수 구현. generate_storyboard는 비-guide 또는 mock를 호출 전 거절하고, live provider 미설정이면 명확한 예외. 이미지 provider protocol과 approved steps 기반 프롬프트 구성; 실제 호출 옵션은 공식 문서 확인 후 준비 상태로 기록. Pillow 균등 3×3 분할, 작은 이미지·잘못된 PNG 거절, 행 우선 순서 유지. 근거 없는 9개 단계 생성 없음.
- Constraints: Backend 모델/라우터 import와 수정 없음. 부모 package init/pyproject는 Backend 담당. library 단독 테스트 가능. 생성 성공을 조작하지 않음. API 키는 서버 env. 합성 grid는 테스트 자료라고 명시.
- Ownership: `backend/visual/`, `docs/workers/visual/`만.
- Acceptance: 합성 9색 grid에서 패널 수/순서/크기 검증, corrupted input 거절, 비-guide/mock에서 provider 호출 0회 검증. 이미지 의미 품질은 미평가로 기록. RUNBOOK/REPORT, 자기 브랜치 commit/push; main merge 금지.

목표 45분. 구분선 검출·실제 이미지 생성·Android visual은 후속 Task.
