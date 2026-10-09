# 완료 보고 — HowLens prework 매뉴얼 자산화 설계

2026-10-09 KST · Evaluation/Architect · branch `Runixs/hackathon-research` · 기존 기준 `979a109` 이후 소유 문서만 변경.

**오늘 권고는 파인튜닝 없이 검토된 catalog + 원본 figure + 결정적 합성이다.** 미등록 제품 공식 웹 탐색은 별도 candidate discovery로 제공하며 근거·권리·적용 모델·실제 전제 검토 전 guide로 승격하지 않는다. 실행 가이드 통합은 Backend의 독립 Approval 채널이 아직 없어 보류 조건이 남는다.

실제 원본 `/Users/runixs/working/ai/dsdn-hub/HowLens-prework`에서6PDF를 확인했다. 선택109개 자산의 경로·SHA·치수를 기록하고 모두 private 로컬 사본의 hash를 재대조했다. Dell Figure212는 PDF211에서 다시 추출한 JPEG가 prework 추출본과 byte-identical이며 bbox·페이지·버전을 기록했다. 인포그래픽은3×3=9패널, 81작업 근거 없음. UR5e5.23와 APC3534H는 현행 catalog5.17/6411A와 달라 별도 revision 등록이 필요하다.

- [ARCHITECTURE.md](ARCHITECTURE.md): 비교·선택, Mermaid asset graph, 검색/안전/시각 fidelity, 확정 discovery DTO 반영, 담당자별 최소 변경·acceptance·rollback.
- [INVENTORY.json](INVENTORY.json):109개 선택 파일 및 정밀 figure 좌표/원본 hash.
- [MANUAL_PROVENANCE.json](MANUAL_PROVENANCE.json):6PDF URL/version/page identity/review/rights.
- [SPECIMEN.md](SPECIMEN.md):Dell 원본 연결 표본, PSU 제거 작업 승인 아님.
- [SOURCES.md](SOURCES.md):공식 자료 실제 열람과 실패·검증 한계.
- [RUNBOOK.md](RUNBOOK.md):재현·인계. 연구 scripts와 local HTML viewer 포함.

실행 검증 PASS:109개 원본/보관본 SHA 일치,6PDF parse/hash/페이지수, Figure212 byte equality, 문서 상대 링크. PyMuPDF1.28.2와 Pillow를 소유 경로의 ignored venv에서 사용했다. PDF 표본 렌더와 Dell211/표지,UR version244,APC version20 및 server 인포그래픽을 육안 확인했다. 모든 페이지·모든 그림·모든 절차 검토 완료라는 뜻은 아니다.

미실행:제품 코드 수정/제품 테스트, 유료 모델/API, 실제 장비 조작, 신규 이미지 생성, 작업 안전 승인. 전체 PDF와 파생 원본 그림은 커밋하지 않는다. 로컬 private 보관본은 push되지 않으므로 타 PC 원본 전달은 별도 권리·접근 경로로 해결해야 한다. 사실상 병목은 이미지 생성 속도보다 실장비 식별/전제 증거, action↔figure 검토와 사용권이다. 14:30 freeze 전에 충족되지 않으면 자산 패키지와 conservative 실패 경로를 유지한다.
