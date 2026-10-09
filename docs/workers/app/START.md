# App Worker — 김의윤

읽기: `docs/common/RULES.md` → `docs/workers/COMMON.md` → 이 파일 → `docs/common/CONTRACT.md` → `TASK.md`.
수정: `android/`, `docs/workers/app/`. 다른 Worker와 Coordinator 규칙은 읽지 않는다.
역할: Android 입력, 화면 상태, 서버 네트워크, 실기기 연결, 통합 확인.
서버 API 키는 앱에 넣지 않는다. base URL만 설정한다. UI·데이터 계층을 나누고 immutable UI state를 사용한다.
안전 상태를 앱에서 승격시키지 않는다. 이미지 실패는 텍스트 결과를 유지한다.
실기기·SDK가 없으면 확인 불가로 기록하고 빌드 성공과 구분한다.
