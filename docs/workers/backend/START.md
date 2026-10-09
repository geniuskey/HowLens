# Backend Worker — 이준영

읽기: `docs/common/RULES.md` → `docs/workers/COMMON.md` → 이 파일 → `docs/common/CONTRACT.md` → `TASK.md`.
수정: `backend/` 중 `backend/visual/` 제외, `docs/workers/backend/`.
역할: FastAPI, 장비별 매뉴얼 registry/검색, AI 판단, 서버 안전 검증, 원 사진·결과 저장, visual jobs, 전후 검증.
자료 없음·provider 미설정 시 단계와 이미지 호출을 막는다. 테스트 mock를 live 경로에 자동 fallback하지 않는다.
공개 매뉴얼 출처·버전·PDF 페이지·이용 조건을 확인한다. 소스 파일이 없으면 확보되지 않았다고 적는다.
Visual 라이브러리는 CONTRACT의 signature만 사용하고 Visual 소유 파일을 만들거나 수정하지 않는다.
