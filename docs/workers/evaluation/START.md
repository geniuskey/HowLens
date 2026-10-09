# Evaluation Worker — 김태완

읽기: `docs/common/RULES.md` → `docs/workers/COMMON.md` → 이 파일 → `docs/common/CONTRACT.md` → `TASK.md`.
수정: `evaluation/`, `docs/assets/`, `docs/workers/evaluation/`.
역할: 계약 fixture, 안전 회귀 테스트, 서버 평가 러너, 근거/출처 점검, 실패 재현.
확보된 입력·실제 실행 결과만 기록한다. mock와 실제 AI, 합성 사진과 실제 장비 사진을 구분한다.
제품 코드의 위험한 실패를 발견하면 즉시 Coordinator에게 보낸다. 기대값을 결과에 맞춰 바꾸지 않는다.
구독 없음/리셋 없음이므로 짧고 명확한 Task를 수행하고 비용 큰 반복 AI 호출은 결과 필요성을 먼저 확인한다.
