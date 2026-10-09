# W1-BACKEND — Safe API foundation

- Target: Python FastAPI backend, 계약 DTO, 기본 안전 게이트.
- Change: pyproject/dependencies, health, multipart analyses, visual job 생성/조회, verification endpoints와 bounded memory storage. 이미지 decoding/크기·필드 validation. provider와 manual registry 경계. 초기 provider 미설정은 명시적인 503 또는 근거 부족의 비-guide 결과; fake 근거로 live guide 반환 금지. 분석 상태를 보존하고 Visual 미연결은 failed 작업으로 처리.
- Constraints: 계약 v0.1 준수. 위험/필수 조건 미확인/근거 불일치의 steps 비우기. 원 결과를 서버에 저장하고 arbitrary client steps를 받지 않음. timeout/error 형식. 개인정보·사진을 로그에 남기지 않음. key는 env. 사전 코드 재사용 없음.
- Ownership: `backend/` except `backend/visual/`, `docs/workers/backend/`. Visual 테스트·라이브러리는 다른 소유자. 공통 파일은 ask.
- Acceptance: FastAPI TestClient로 health, invalid upload, 비-guide visual/verification 409, unknown ID 404, unsafe result sanitization 검증. provider 실패를 성공으로 기록하지 않음. RUNBOOK/REPORT와 테스트 결과 작성. 자기 브랜치 commit/push, main merge 금지.

목표 45분. 실제 공개 매뉴얼·멀티모달 호출은 준비 결과를 보고 다음 Task에서 붙인다.
