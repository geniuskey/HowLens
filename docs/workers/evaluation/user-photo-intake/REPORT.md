# 사용자 실물사진 9장 독립 평가·공식 매뉴얼 인계

확인일 2026-10-09 KST. 이전 코퍼스 `6614596` 보존. 제품 코드·원본 사진 변경 없음, 모델/API 평가 0회. **준비 완료와 인식 성공을 구분한다.**

## 결과

[INVENTORY.json](INVENTORY.json): 원본 절대경로, SHA-256, 바이트, 원본/표시 크기, EXIF orientation, 육안 관찰. 9/9 Coordinator 인벤토리와 해시 일치. 사진은 모두 10MiB/20MP 제한 이내이며 최대 4000×3000(12MP), 최대 7,025,339바이트다. p01/p05/p08은 orientation=1, 나머지는 EXIF orientation 없음(null)으로 기록했다. null을 촬영 회전 0도의 증거로 해석하지 않는다. EXIF 위치/시간/단말·일련번호는 문서에 복제하지 않았다.

| ID | 파일 | 입력 구분 | 직접 확인한 것 / 정확 모델 기대값 |
|---|---|---|---|
| p01 | 137681a773828da5.jpg | 삼성 근접 라벨 | RF60A91C3AP, 615L; 라벨 Rev.05를 매뉴얼 판본으로 오인 금지 |
| p02 | 16197495f93c0545.jpg | SK 라벨 | 모델 WPU-B600F; 인증 표기의 B610F와 혼동 금지 |
| p03 | 2edbff3ad97392f2.jpg | 커피머신 전체 | Nespresso 브랜드, 정확 모델 UNKNOWN |
| p04 | 37771b794b19699e.jpg | 냉장고 전체 | 4도어 외관, 정확 모델 단정 금지 |
| p05 | 4015b4013bb71d9f.jpg | 쿠쿠 근접 라벨 | AC-35U20FWS |
| p06 | 933cd59f8afc7f43.jpg | 삼성 넓은 라벨 | RF60A91C3AP; 전체 사진 테스트에 넣으면 라벨 누출 |
| p07 | 98ca76178e51d1f5.jpg | 공기청정기 전체 | CUCKOO 브랜드·원통형 외관; 정확 모델 불명 |
| p08 | c9a7bd3a11f4b154.jpg | 쿠쿠 라벨 | AC-35U20FWS; 인증 AC-34U20과 모델명 구별 |
| p09 | ed979618bc94654a.jpg | 정수기 전체 | 두 출수부와 표시등; 정확 모델/실제 온도·수질 불명 |

사진 속 인물 반사·일련번호·QR 내용은 공개 보고서와 정답에 불필요하여 제외했다. 원본은 `/Users/runixs/HowLens/docs/*.jpg`에 그대로 있고 미리보기 및 제조사 원본은 이 worktree의 ignored `docs/assets/user-real-photos/private/`에만 있다. 이 자료를 공개 Git·외부 서비스에 업로드하지 않았다.

## 공식 매뉴얼 실제 확보

### 삼성: 정확 KR 지원 연결의 한국어 공용 PDF 확보

[삼성 KR RF60A91C3AP 지원](https://www.samsung.com/sec/support/model/RF60A91C3AP/)에서 615L 모델을 확인했다. [삼성전자서비스 해당 모델 페이지](https://www.samsungsvc.co.kr/download/view?code=RF60A91C3AP)의 렌더 텍스트는 다운로드 없음으로 보였으나 **같은 응답의 공개 embedded data**에는 CTTFileID=9091568, UM, version=4.0, 2023-03-20, 한국어 파일 경로가 있었다. 그 경로로 [공식 다운로드센터 PDF](https://downloadcenter.samsung.com/content/UM/202303/20230320135004710/RF9000A_AC_2020_UM_RK0001_KO_230310.pdf)를 받아 서명·크기·92페이지를 검증했다. API 키/로그인/우회는 사용하지 않았다.

- 원본: `docs/assets/user-real-photos/private/samsung-kr.pdf`, 15,281,768바이트.
- SHA-256: `be4221520ef42364dc251805cb0efa95244f50384e87ba18a778d783d2b90041`.
- 표지 RF60/85A*, RF84C* 공용. PDF19=인쇄19: 외부 명칭 도면, 01 냉장실 문, 05 냉동실 문, 06 맞춤 보관실 문. PDF1/19/92 실제 렌더 확인.
- 지원 페이지의 정확 모델 연결은 확인했지만 **그림의 모든 선택 사양이 사진의 제품에 있다는 뜻은 아니다**. 마지막 쪽은 공용 그림 차이 및 국내 전용임을 명시한다. AE/523L 문서 대체 없음.

### SK매직: 정확 모델의 공식 HTML 근거 확보, PDF는 미확보

[공식 QR 검색](https://qr.skmagic.com/Search.htm)의 [공개 data.js](https://qr.skmagic.com/2019/js/data.js)는 WPUB600FREWH를 WPUB610FREWH 공용 경로에 명시적으로 연결한다. [해당 공식 매뉴얼](https://qr.skmagic.com/2019/model/WPU/WPUB610FREWH/Manual.htm#contents-4-1)은 외부 명칭에서 **WPU-B600F 별도 제목과 별도 그림**을 제공한다. [B600F 원본 그림](https://qr.skmagic.com/2019/model/WPU/WPUB610FREWH/images/img_04_01_02.png)을 다운로드·육안 확인했고 같은 section의 번호별 표를 대조했다. 07은 물받이다.

HTML 저장본 SHA: `6911787890f7141a45e086fa0321eb0095bc4c96266a5eafc8e97ab0bd5063fd`. 원본 PNG의 해시는 [MANUALS.json](MANUALS.json)에 있다. HTML이므로 PDF 페이지·인쇄 페이지·문서 판본은 null이며 `2019` URL을 발행/개정 연도로 단정하지 않는다. 현재 페이지형 evidence 계약에 넣기 위해 가짜 PDF 쪽을 만들지 않는다.

### 쿠쿠: FWS 정확 PDF 미확보, 다른 모델 PDF 명시 거부

[공식 제품 페이지](https://www.cuckoo.co.kr/mall/productView?categoryCd=3&productNo=5241)는 AC-35U20FWS(S) 제품을 설명하지만 해당 상품 데이터 manualNo=0이다. [공식 설명서 검색](https://www.cuckoo.co.kr/customer/customerSvCProdMualDnloadModelSch)의 공개 JS가 사용하는 `/rest/customer/productManualSearch`에 `AC-35U20`으로 조회했을 때 manualNo1777 **AC-35U20FWGH** 한 건을 반환했다. [그 PDF](https://cdn.cuckoo.co.kr/upload_cuckoo/_bo_rep/manual/file_718aeeb2-2a7c-4d79-a1af-b55fd9604915.pdf)는 36쪽, 표지와 PDF35의 모델도 FWGH이며 PDF36은 `10383-0016P0 Rev.1`이다. 따라서 FWS 작업 근거로 쓰지 않는다.

비교·거부 증거용 원본 `private/cuckoo-fwgh-family.pdf`, SHA `4c062c0de40b7e8d43116afbfd9d425ddd5c7b8e128f85f91c6c11f2974edb24`. 외관이 비슷하거나 규격이 같아도 모델 적용을 승인하지 않는다. 한국어 폰트 텍스트 추출이 불완전하여 표지·규격·뒷표지 이미지를 직접 확인했다.

### Nespresso

브랜드 이외 모델 라벨이 없으므로 특정 모델 매뉴얼을 연결하지 않는다. 추가 기존 라벨/구매명 정보가 필요하다. 커피 추출·세척·열수·전기 작업 추천 없음.

## 독립 테스트 설계

[TEST_MATRIX.json](TEST_MATRIX.json)은 **아직 실행하지 않은** 9개 단일 이미지 사례다. whole p03/p04/p07/p09를 먼저 새 세션에서 실행하며 라벨 사진·모델명 정답·특정 모델 매뉴얼·이 보고서·이전 대화는 입력하지 않는다. 라벨 사례 p01/p02/p05/p06/p08은 각각 별도의 새 세션에서 실행한다. 실제 평가자는 이 설계를 읽은 현재 대화 세션을 평가 세션으로 재사용하지 않는다.

whole에서 exact model 정답을 강제하지 않고 보이는 브랜드/종류, 불확실성, 필요한 정보 요청을 채점한다. label에서만 읽히는 모델 문자열·필드 구별을 평가한다. 근접/넓은 라벨과 whole의 연결은 동일 배치 추정이며 독립적인 같은 기기 증거는 아니다. 후속 paired 테스트는 별도 결과로 기록하며 9개 독립 기기라고 집계하지 않는다.

기록 항목: case ID·입력 SHA·실제 prompt·모델/버전·응답·브랜드/모델 추출·추가정보 요청·환각·운영 지시 여부·인용 일치·실행 시각. 모든 실행 상태는 `not_run`, accuracy=null이다. 원본과 전처리 이미지의 성능을 섞지 않으며 변환 시 파생 해시를 별도로 기록한다.

## 근거 확인 후 추천하는 최소 데모

1. **삼성 PDF19 + 라벨 확인 후 외부 문 명칭 관찰**: “사진에서 위쪽 문과 아래쪽 문 위치를 매뉴얼의 외부 명칭 그림과 비교해 알려 주세요. 문을 열거나 설정을 바꾸지 않습니다.” 모델 일치와 whole/label pairing 확인 후에만 해당 명칭을 적용한다. 선택 사양 및 내부 구성은 보이지 않으므로 설명하지 않는다.
2. **SK 공식 외부 명칭 그림의 물받이 위치 관찰**: WPU-B600F 연결 확인 후 “출수구 아래 물받이가 사진에서 어디에 보이는지 표시해 주세요.” 분리·청소·출수·버튼 조작 없음. HTML 근거를 현행 PDF-page evidence로 위장하지 않고 observation-only 검토 자료로 전달한다.

이는 매뉴얼 기반 관찰 제안이며 guide 승인·안전/정상 동작 보증이 아니다. 쿠쿠와 커피머신은 정확 매뉴얼/모델 미확보 상태로 식별 또는 추가정보 요청 데모까지만 한다.

## 인계 상태와 남은 작업

사진 inventory/육안 검토 완료, 삼성 공식 PDF 검증 및 선택 페이지 추출 완료, SK HTML/원본 도면 보존 완료, 쿠쿠 다른 변형 PDF 격리 완료. **백엔드 등록·런타임 ingestion·모델 평가·guide 승인 모두 미실행**이다. 파일이 있다는 사실을 제품 ingestion 성공으로 보고하지 않는다.

Backend: 삼성 exact-model 지원 연결 + 공용판본의 적용 범위를 함께 보존하고, 문서 hash→PDF19→그림/표 명칭→관찰 질문을 연결한다. 원본 PDF는 byte-exact이며 페이지의 벡터 도면을 포함한다. MANUALS의 embedded image xref/bbox는 페이지 내 래스터 객체에 한하며 벡터 도면 전체를 뜻하지 않는다. HTML 근거 지원은 별도 계약 검토 대상이다. 쿠쿠 FWS 공식 설명서 및 Nespresso 모델 확인, whole-label pairing 확인이 남아 있다.

재현: 기존 venv에서 `docs/assets/manual-asset-design/.venv/bin/python docs/workers/evaluation/user-photo-intake/build.py`, 원본/근거 검증은 `python3 docs/workers/evaluation/user-photo-intake/verify.py`. 다운로드 원본 및 공개 HTML 응답은 ignored private에 있어 다른 PC에는 Git만으로 전달되지 않는다. 정확 URL은 MANUALS와 위 출처, 원본 사진은 INVENTORY 절대경로를 따른다. 제조사 PDF 전체·사용자 사진을 공개 커밋하지 않는다.
