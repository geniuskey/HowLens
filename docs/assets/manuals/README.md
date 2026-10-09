# 샘플 PDF 설명서

네 담당자가 직접 확보한 설명서 PDF를 넣는 공통 경로입니다. 저장소 루트 기준 `docs/assets/manuals/`를 사용합니다.

| 담당 | PDF를 넣을 경로 |
|---|---|
| App · 김의윤 | `docs/assets/manuals/app/` |
| Backend · 이준영 | `docs/assets/manuals/backend/` |
| Visual · 이윤재 | `docs/assets/manuals/visual/` |
| Evaluation · 김태완 | `docs/assets/manuals/evaluation/` |

파일명은 `제조사_모델_문서종류_버전_언어.pdf` 형식을 권장합니다. 예: `Dell_PowerEdge-R750_ServiceManual_RevA01_en.pdf`.

같은 이름의 기존 파일을 덮어쓰지 말고 버전을 구분해 넣어 주세요. PDF와 함께 같은 이름의 `.source.txt`에 원문 URL, 문서 버전, 확보 날짜를 적으면 근거 등록에 사용할 수 있습니다.

이 경로는 설명서 수집용입니다. 파일을 넣는 것만으로 서버에 자동 등록되거나 검증된 작업 근거가 되지는 않습니다. 문서 등록과 페이지·발췌 검증은 Backend/Evaluation 작업에서 별도로 진행합니다.
