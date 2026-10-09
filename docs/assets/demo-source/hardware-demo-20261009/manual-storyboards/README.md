# HowLens 원문 이미지 기반 단계별 데모 예제

첨부된 UPS 카드형 예시의 구성(제목, 번호, 원문 그림, 짧은 설명, 페이지 출처)을 참고했다. 우선순위는 MagnaTran → LOGOSOL → STM32다.

| 예제 | 카드 | 파일 | 섹션 완결성 |
|---|---|---|---|
| 01-magnatran | 9 | storyboard.png / storyboard.pdf | Wrist Band Adjustment 전체 515–516쪽 |
| 02-logosol | 9 | storyboard.png / storyboard.pdf | §3.2 전체 26–28쪽 및 안전 문맥 포함 |
| 03-stm32 | 6 | storyboard.png / storyboard.pdf | §7.4.4 + Figure 7 전체 18–19쪽; 점퍼 설정 단위 작업 |

각 폴더에는 complete-section-excerpt.pdf, DEMO.md, storyboard-source.json, 원문 이미지 assets가 있다. 그림은 매뉴얼의 figure/photo를 그대로 발췌했으며 AI 생성 이미지를 사용하지 않았다. 형상 및 LED 상태를 변경하지 않고 같은 그림을 단계 설명에 재사용했다. 이 그림을 실제 작업 전후 촬영 이미지로 표시하지 않는다.

LOGOSOL은 기존 v2 발췌본에서 누락된 섹션 시작 26쪽을 보완했다. MagnaTran 7·8 카드는 측정값에 따른 분기이며 동시에 연속 수행하는 단계가 아니다. STM32는 불필요한 단계를 추가하지 않고 6카드로 구성했다.

## orchestrator 전달용 초안

이 카드형 예제를 기준으로 데모 구현 및 통합을 검토해 주세요. 각 카드 그림과 절차의 원문 페이지는 storyboard-source.json에 분리되어 있습니다. 실제 장비 등록/source_url 검증/안전 조건/실물 전후 검증은 별도 진행해야 합니다. MagnaTran·LOGOSOL은 기존 장비 ID에 임의 매핑하지 마세요. STM32 6카드는 기존 9패널 API에 직접 호환되지 않으므로 적용 방식을 결정해 주세요. 이미지 생성이 필요하다면 제공된 원문 이미지의 부품 형상·배선·배치를 유지하는 참조 기반 확장만 허용하고 zero-base 생성을 하지 마세요.

이 문구는 전달용 초안이며 구현 요청을 직접 보내지 않았습니다. 제품 코드 수정, live API 호출, 실제 작업, 앱 통합, Git commit/push는 수행하지 않았습니다.
