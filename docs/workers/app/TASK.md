# W1-APP — Android foundation

- Target: `android/`와 역할별 문서.
- Change: 독립 Gradle Android 프로젝트(Kotlin/Compose). 장비 선택, JPEG/PNG 갤러리 또는 카메라 입력, 질문, 분석 상태, 근거·경고·추가 정보 표시. 계약 DTO와 multipart 서버 client, base URL 설정, timeout·retry UI. ViewModel/Repository 경계. CameraX가 과도하면 초기 카메라 intent로 연결하고 차이를 기록한다.
- Constraints: 제품 OpenAI 키 없음. mock를 UI에 명시. 비-guide 단계/이미지 차단. 서버 미완료 시 fake repository로 needs_more_information/stop 화면 확인. 실제 AI 성공으로 보고하지 않는다. Gradle wrapper 바이너리는 공식 출처에서 받는다.
- Ownership: `android/`, `docs/workers/app/`만. 루트 설정이나 계약 변경은 ask.
- Acceptance: `assembleDebug` 실행 결과(실패/도구 누락 포함), 입력 검증과 guide gate 확인. 안전한 mock를 통한 화면 동작, 빌드/실기기 각각 구분. RUNBOOK/REPORT 작성. 작은 커밋으로 자기 브랜치 push; main merge 금지.

목표 작업 시간 45분. 서버 실제 연동과 Visual 패널은 다음 Task에서 통합한다.
