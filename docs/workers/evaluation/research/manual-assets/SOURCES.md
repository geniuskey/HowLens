# 조사 출처와 검증 한계

조회일 2026-10-09 KST. 외부 조사에는 제조사·공식 문서만 사용했다. 아래 자료의 요약은 아키텍처 판단 근거이며 제품 안전 승인이나 사용권 부여가 아니다.

| 공식 출처 | 실제 확인 | 채택 범위 |
|---|---|---|
| [Microsoft Document Intelligence RAG](https://learn.microsoft.com/en-us/azure/ai-services/document-intelligence/concept/retrieval-augmented-generation?view=doc-intel-4.0.0) | 본문 열람; v4.0 GA, Layout2024-11-30; 페이지 갱신2026-08-15 | 외부 검색+기존 모델, 구조/문단 단위 chunk. Azure 서비스 도입/호출은 하지 않음 |
| [Azure AI Search RAG overview](https://learn.microsoft.com/en-us/azure/search/retrieval-augmented-generation-overview) | 본문 열람 | retrieval 계층을 분리하는 설계 참고 |
| [PyMuPDF images](https://pymupdf.readthedocs.io/en/latest/recipes-images.html) | 본문 열람; 실행 renderer 버전은 INVENTORY에 기록 | embedded image 추출과 page clip을 구분 |
| [W3C Web Annotation](https://www.w3.org/TR/annotation-model/#selectors) | Recommendation 본문 열람 | body/target/selector, 부분영역 출처 표현; 전체 표준 구현 주장 아님 |
| [UR5e SW5.23 제조사 페이지](https://www.universal-robots.com/download/manuals-e-seriesur-series/user/ur5e/523/user-manual-ur5e-e-series-sw-523-english-international-en/) | 열람; Last modified Oct13 2025; 공식 S3 다운로드 링크 확인 | local PDF244의5.23/10.13.387과 대조; 5.17 catalog와 혼합 금지 |
| [APC Installation Guide](https://download.schneider-electric.com/files?p_Doc_Ref=SPD_SCON-7QHM74_EN) | 공식 PDF8쪽 열람; EN990-3535H-001/05-2022 | local version 일치; 모델/지역 차이 그대로 유지 |

제조사 매뉴얼별 URL은 [MANUAL_PROVENANCE.json](MANUAL_PROVENANCE.json). Dell topicspdf, APC Operation/RBC/Addendum 웹 도구 open은 접근 오류였으므로 이번 원격 재다운로드 성공으로 기록하지 않는다. 해당 파일은 실제 로컬 PDF를 열고 hash·문서 표기·일부 페이지를 검사했다. Dell은 이전 연구 `SOURCE_MANIFEST.json`의 제조사 다운로드 SHA와도 동일하다. 다른 5문서에 대해서는 이번에 원격 bytes equality까지 검증한 것은 아니다.

원본 문서의 모든 작업 절차·경고를 완독/승인하지 않았다. 정밀 specimen은 Dell PDF211만 연결했다. UR p194/p244, 각 PDF 표지/끝 페이지 등을 렌더했지만 모든 렌더를 육안 승인한 것으로 기록하지 않는다. page crop 좌표는 실제 Dell figure에 한해 검증했고 나머지 prework 코드의 좌표는 등록 후보다.

읽은 prework 요구: `prework_assets/experiments/image_prompting/PROMPTS.md`, `RESULTS.md`, `experiments/panel_split/RESULT.md`, `06_dayof/repo_seed/docs/prompts/storyboard.md` v2, `05_visuals/scenes/SCENES.md`, `05_visuals/infographics/build/compose_infographics.py`, `02_manuals/MANUALS.md`. 다른 원본 문서 도착은 기다리지 않는다.
