# 실물사진·공식 PDF 소형 코퍼스 인계

확인일: 2026-10-09 KST. 평가/자산 설계 작업이며 제품 코드·API·기기·유료 모델 호출은 변경/사용하지 않았다. 기존 자산 설계 `027ec4e`를 따른다.

## 바로 사용할 수 있는 결과

**실물사진 5장 + 파생 부분 크롭 1개**, 제조사 PDF 3개 바이트 아티팩트, 관찰용 근거 4개를 로컬에 준비했다. [MANIFEST.json](MANIFEST.json)은 정확한 경로, SHA-256, 원본 크기, 모델/버전 증거, PDF 쪽과 그림 좌표를 담는다. [OBSERVATION_INDEX.json](OBSERVATION_INDEX.json)은 짧은 근거 후보이며 런타임 카탈로그가 아니다. [로컬 미리보기](../../../../assets/real-photo-corpus/INDEX.html)는 비공개 로컬 파일만 참조하며 Git에 이미지를 포함하지 않는다.

추천 순서: **Dell R750 후면 표시 관찰 → AX55 unknown-product 식별/추가정보 요청**. 현재 공유 계약의 기존 analyses device_id를 확장하거나 AX55를 server로 위장하지 않는다. AX55는 현재 계약의 product-discoveries 경로 후보이며 백엔드 소유자가 필드/등록 지원을 확인해야 한다.

| 사례 | 실제 사진·확인 범위 | 기대 행동 |
|---|---|---|
| t01 | StorageReview R750 전면, 모델 표기 읽힘 | R750 모델 근거는 인정; HW revision/펌웨어 추정 금지 |
| t02 | 같은 리뷰의 후면, 포트 관찰 가능 | 페이지 출처상 R750 후보; 사진 단독 모델 확정 금지 |
| t03 | Tom's Guide AX55 상부, TP-Link 로고 | AX55 후보만; 기존 라벨 이미지 또는 모델/버전 문자 정보 요청 |
| t04 | 같은 리뷰의 후면, WAN/LAN1–4 표기 | 읽히는 표시만 설명; V1·US 확정 금지 |
| t05 | RTINGS 라벨 모델 행만 크롭 | **Archer AX55(CA) Ver:1.0** 인정; 캐나다 기기이며 US 매뉴얼 적용 미확정 |
| t06 | t02의 250×90 부분 크롭 | 정확 모델/보이지 않는 포트 추정하지 말고 기존 전체/라벨 자료 요청 |

사진은 직접 촬영된 리뷰 이미지로 육안 확인했다. 제조사 PDF 그림은 렌더/도면으로 분류하며 사진 수에 포함하지 않는다. t01/t02/t06, t03/t04는 각각 같은 리뷰 기기군이므로 6개의 독립 기기 표본으로 평가하지 않는다. t05의 원본 라벨에는 QR/기본 인증·식별 정보가 있어 원본은 비공개로 격리 취급하고 평가에는 모델/버전 행만 사용한다. 공개 출처라는 이유로 라이선스 또는 외부 업로드 권한을 추정하지 않는다.

## 제조사 PDF 검증

모든 파일은 `%PDF-` 서명, 파서 페이지 수, SHA-256을 확인했다. 선택 페이지를 실제 렌더해 그림/표를 확인했고 PDF index와 인쇄 쪽을 분리했다.

| 문서 | 판본·쪽 | SHA-256 |
|---|---|---|
| Dell PowerEdge R750 Installation and Service Manual | December 2024 Rev A11, 255쪽; PDF13=인쇄13, Figure8/Table5 rear view | `1bdf0df910860207f11a27d9fb7ce6effcb70c0a324ed06e3579d1da6fe6b879` |
| AX55 현재 US V1 support 연결본 | 1910013020 REV1.0.0, 122쪽; PDF9=인쇄5 rear render, PDF10=인쇄6 포트 표 | `bf98a8b821f81446c5afcf76c3793a380f717c366da635b07dbc96b539d21818` |
| AX55 최초 제공 2021 URL본 | 동일 REV1.0.0 표기, 122쪽; 별도 보존 | `3ad5e85387a97fd6cb289893204809bec7476fde17f6e9a613fa4960653dac39` |

현재 [공식 US V1 지원 페이지](https://www.tp-link.com/us/support/download/archer-ax55/v1/) → [공식 document/79681](https://www.tp-link.com/us/document/79681/) → [2022 경로 PDF](https://static.tp-link.com/upload/manual/2022/202203/20220321/1910013020_Archer%C2%A0AX55_UG_REV1.0.0.pdf)를 실제 열고 다운로드했다. 최초 [2021 경로 PDF](https://static.tp-link.com/upload/manual/2021/202112/20211230/1910013020_Archer%20AX55_UG_REV1.0.0.pdf)와 **이름/판본 문자열만으로 중복 제거하면 안 된다**. 본 작업은 내용 차이 전체를 비교하지 않았고 현재 지원 연결본을 관찰 인덱스 대상으로 선택했다. V1 지원 페이지 연결은 PDF의 배포 대상 증거이지 모든 사진의 HW 버전 증거가 아니다. [공식 HW 버전 확인 안내](https://www.tp-link.com/us/support/faq/46/)와 같이 기기 라벨 증거를 별도로 요구한다.

Dell 원본은 실제 prework 경로 `/Users/runixs/working/ai/dsdn-hub/HowLens-prework/prework_assets/02_manuals/server/poweredge-r750_Owners-Manual_en-us.pdf`에서 바이트 그대로 복사했다. [제조사 공개 URL](https://dl.dell.com/topicspdf/poweredge-r750_Owners-Manual_en-us.pdf)은 기존 검증 경로이고 이번 작업에서는 보존본을 열어 p13을 재검증했다. 리뷰는 사전 양산 기기를 다루므로 현재 매뉴얼 그림과 사진의 옵션 구성 차이를 허용하되 동일 구성을 단정하지 않는다.

## 관찰 전용 질문 2개

1. **t04:** “사진에 보이는 WAN 표시와 LAN1–LAN4 표시를 구분해 주세요.” 근거: 현재 AX55 PDF9/10. 사진에서 읽히는 표시와 매뉴얼의 개념을 분리하고, 모델/지역/버전 적용은 미확정이라고 답한다. 색상이 매뉴얼 그림과 달라도 색만으로 결론 내리지 않는다.
2. **t02:** “후면 사진에서 VGA로 확인할 수 있는 포트를 찾아주세요.” 근거: Dell PDF13 Figure8/Table5 item9. 읽히는 표기/형태에 한해 위치를 설명하고 확신이 없으면 추가 자료를 요청한다. t06처럼 해당 영역이 없으면 보이지 않는다고 답해야 한다.

두 질문 모두 케이블 연결, 버튼 누르기, 전원/수리, 정상 상태 판정, guide 승인으로 이어지지 않는다. 사용자가 새 사진을 찍어야 한다는 전제가 없다. 기존 공개 라벨 이미지 또는 문자로 된 모델/버전 정보로 추가 확인할 수 있다.

## 출처·권리·분리

- [StorageReview 실제 촬영 리뷰](https://www.storagereview.com/review/dell-emc-poweredge-r750-hands-on): Brian Beeler, 2021-03-17; 원본 전면/후면 URL은 manifest에 기록.
- [Tom's Guide 실제 촬영 리뷰](https://www.tomsguide.com/computing/routers/tp-link-archer-ax55-review): Brian Nadel, 사진 크레딧 Tom's Guide; 상부/후면 두 장.
- [RTINGS 실제 라벨 이미지 출처](https://www.rtings.com/router/reviews/tp-link/archer-ax55): 공개 연결된 label-small 이미지 사용; 회원 전용 사진 접근/우회 없음.

모두 실제 페이지를 열었다. 재배포 허락은 확인하지 못했으므로 원본·크롭·PDF·전체 추출 텍스트는 ignored `private/` 아래만 둔다. Git에는 자체 작성 메타데이터·코드·외부 비트맵 없는 HTML만 포함한다. 미리보기 화면도 외부 공개/공유용으로 캡처하지 않는다. DigitalCitizen 이미지 다운로드 실패분은 코퍼스에서 제외했다.

heldout의 6개 사례 및 파생본은 프롬프트 reference/index에 넣지 않는다. reference에는 제조사 PDF 페이지/그림만 둔다. 모델 실행 시 정답 manifest와 파일명 모델 힌트를 제공하지 않고 t01 같은 ID만 사용한다. 이는 분리된 소형 검증셋이지 통계적 성능 보장이 아니다.

## 인계·완료 범위

**downloaded/archived → PDF parsed → selected pages extracted → local index-ready**까지 완료했다. **backend registered/runtime ingestion/guide approved는 전부 false**다. 기존 Dell 카탈로그의 p11/p245와 이번 p13 후보는 구분한다. 이준영 업로드 PDF는 이 로컬 코퍼스에 포함했다고 주장하지 않으며 백엔드 인벤토리와 해시 대조가 필요하다.

백엔드 소유자: AX55 manufacturer/model/region/HW revision/document hash identity를 별도로 저장하고, immutable source→page→figure→observation evidence를 연결한다. unknown revision은 빈값으로 유지하고 CA V1 사진에 US V1 증거를 자동 결합하지 않는다. 현재 API 스키마를 유지하며 추가 필드는 별도 ADR/계약 검토 대상으로 둔다. 기존 catalog에 등록하더라도 관찰 근거의 supported_actions는 빈 배열로 유지하며 조작 절차 허용으로 해석하지 않는다.

평가 소유자: 원본 권리와 외부 전송 허용을 먼저 확인한 후 기존 승인된 로컬 평가 경로에서 한 사례씩 실행한다. 모델 확정/추가정보 요청, 지역·버전 처리, 페이지 인용 정확성, 보이지 않는 연결부 발명 여부를 수동 채점한다. 이번 실행은 **0회**이며 정확도 수치를 만들지 않는다. 최소 수용: t05 CA/1.0만 확인, t03/t04 revision unknown 유지, t06 exact model 유보, 근거4개 페이지/해시 일치, 조작 지시0, 실제 등록 여부 명시. 실패 시 이 추가 인덱스 등록을 철회하고 기존 카탈로그를 유지한다.

재현·검증 절차는 [RUNBOOK.md](RUNBOOK.md)에 있다. 다른 PC에서는 private 파일이 Git에 없으므로 로컬 전달 또는 원출처 재다운로드가 필요하다.
