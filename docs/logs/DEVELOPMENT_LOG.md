# Development log

## 2026-10-09 — Documentation and Wave 1 preparation

- 사용자 요청: Coordinator Medium, docs 중심 문서, PC 역할과 역할별 읽기 경로, 사람 선택 전 외부 선례 조사.
- Codex 작업: 공식 선례를 열람하고 문서 트리·공통 규칙·API 계약·각 역할 START/TASK를 작성.
- 팀 구성: App 김의윤 / Backend 이준영 / Visual 이윤재 / Evaluation 김태완. 각 계정 지급 2,500크레딧은 사용자 제공 정보.
- 사전 자료: prework의 Orca 운영 문서만 참고. 실험 코드는 복사하지 않음. 공개 PDF·합성 사진의 제품 사용은 아직 없음.
- 검증: 22개 문서 링크 검사(missing 0) 및 Git diff check 통과. 제품 코드·실기기·AI 성공은 미검증. 사람 리뷰 완료로 기록하지 않음.

## 2026-10-09 11:00 KST — Wave 1 dispatch

- 사용자 설정 변경 후 같은 Coordinator 세션에서 재개; 새 Coordinator를 중복 실행하지 않음.
- 문서 commit `75c8eac`를 main에 push하고 Run `run_6351cb7363de` 생성.
- App/Backend/Visual을 각 PC 별도 worktree에 배포. 원격 빈 refs는 git fetch로 복구.
- Evaluation은 폴더 신뢰 gate로 시작 실패; 신뢰 승인 요청. 완료나 테스트 통과로 기록하지 않음.
- 사람 제품 코드 리뷰·통합은 아직 없음. 결과는 역할 REPORT와 Coordinator 리뷰 자료에 기록 예정.
