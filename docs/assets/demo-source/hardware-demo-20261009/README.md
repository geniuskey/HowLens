# H/W 데모 예제 · 2026-10-09

우선순위: **MagnaTran → LOGOSOL → STM32**.

| 예제 | 매뉴얼·원문 기반 스토리보드 | imagegen 작업 장면 |
|---|---|---|
| MagnaTran 손목 밴드 측정·조정 | [자료](manual-storyboards/01-magnatran/DEMO.md) | [생성 이미지](generated-actions/01-magnatran-actions.png) |
| LOGOSOL 진공 센서 감도 조정 | [자료](manual-storyboards/02-logosol/DEMO.md) | [생성 이미지](generated-actions/02-logosol-actions.png) |
| STM32 CN2 점퍼 설정 | [자료](manual-storyboards/03-stm32/DEMO.md) | [생성 이미지](generated-actions/03-stm32-actions.png) |

각 manual-storyboards 예제 폴더에는 전체 선택 섹션을 담은 complete-section-excerpt.pdf, 카드형 storyboard.pdf/PNG, DEMO.md, 출처 JSON 및 원문 이미지가 있다. LOGOSOL은 섹션 시작 26쪽을 포함해 26–28쪽 전체를 발췌했다. MagnaTran 작업은 515–516쪽 전체, STM32는 18–19쪽의 §7.4.4 단위 작업이다.

전체 원본 PDF는 `docs/assets/manuals/evaluation/`에 이미 저장되어 있다. 발췌 페이지 번호 대응은 각 storyboard-source.json의 page_map에 있다.

## orchestrator 전달 사항

- 이 브랜치는 PDF·이미지·근거·데모 소스 자료만 포함한다. 제품 코드/API/앱 통합은 변경하지 않았다.
- generated-actions는 원문 figure/photo를 참조한 imagegen 결과로, 원문 자체나 실물 작업 사진이 아니다. 도구 동작과 LOGOSOL A/B LED 상태는 설명을 위해 합성했다.
- 원문 기반 기계적 세부 일치가 완전히 검증된 상태는 아니다. 특히 MagnaTran 도구 접촉면과 STM32 CN2 핀 세부는 review.json의 후속 검토 항목이다.
- 매뉴얼 기반 스토리보드 원문 crop는 픽셀 일치를 확인했으며 generated-actions에 그 검증 결과를 적용하면 안 된다.
- MagnaTran 7·8 카드는 조건별 분기다. LOGOSOL은 dual-arm 분기다. STM32는 6카드이며 기존 9패널 계약에 임의로 맞추지 않는다.
- 새 장비를 기존 server/cobot/ups 장비 ID에 임의로 연결하지 않는다. 등록/source_url/안전 조건/실물 평가/실제 구현·통합 범위는 orchestrator가 결정한다.

생성 이미지 검토: [review.json](generated-actions/review.json)
원문 자료 검증: [validation.json](manual-storyboards/validation.json)

실물 작업·live API·앱 통합은 미실행이다. 이전 위치 식별 전용 v1/v2 및 임시 ZIP은 이 브랜치에 넣지 않았다.
