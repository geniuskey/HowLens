# 연구 결과 재검증·앱 인계

## 공유 기능 최소 구현 명세 (App 담당, 20–30분 추정)

1. 현재 화면의 저장된 Analysis만 입력으로 받는다. 새 분석/AI/서버 호출을 하지 않는다. 표시 순서는 모델명(서버의 고정 매핑), decision, mode, 필터를 통과한 관찰/부족 정보, 문서 ID·버전·PDF 페이지·인쇄 페이지·등록된 공식 URL, 한계 문구다. 기본 공유에 steps/자유 질문/user_confirmation/raw 응답은 포함하지 않는다. 부족 정보로 차단된 상태와 mock 표시는 항상 유지한다.
2. 허용 목록 기반으로 본문을 만든다. 사진·이미지 URI·analysis_id·Service Tag·시리얼·MAC/IP·장비 호스트명·로그·계정·토큰·비밀번호는 제외한다. 문서 ID는 공개 문서 식별자이며 장비 식별자와 구별한다. URL은 서버 등록 제조사 HTTPS 주소만 사용하고, 임의 사용자/사진 URL과 인증·서명 쿼리 URL은 제외한다. 매뉴얼 다운로드에 필요한 공개 문서 선택 쿼리만 허용한다.
3. observations/missing_information/quote는 자유 텍스트라서 개인정보가 없다고 가정하지 않는다. 가장 빠른 안전한 기본값은 공유 시 자유 텍스트를 생략하고 decision/mode/문서 출처/고정 한계 문구만 출력하는 것이다. 자유 텍스트를 넣으려면 기존 검증된 필터가 있는 경우에만 적용하고, 필터가 없으면 생략한다. 정규식 하나로 비밀 제거를 보증하지 않는다. 화면 미리보기에서 정확히 어떤 내용이 공유되는지 확인하게 한다.
4. `Intent(ACTION_SEND)` + `type="text/plain"` + `EXTRA_TEXT` + `Intent.createChooser`를 사용한다. EXTRA_STREAM/ClipData/이미지 썸네일/파일 권한은 필요 없다. 화면에서 누른 사용자가 대상 앱을 고르고 직접 전송한다. 자동 전송이나 특정 수신자 기본 지정은 하지 않는다. 앱 전환 불가 시 오류 표시와 기존 결과를 보존한다.
5. 검증: 세 decision, 빈 evidence, mock/live, 한글과 긴 URL, raw serial/IP/token이 들어간 자유 텍스트, 취소, 수신 앱 없음 케이스. 실제 share intent payload에 비밀·사진 URI·원문 질문·단계가 없는지 확인한다. 테스트용 수신 Activity 또는 미리보기에서 확인하고 타인에게 시험 전송하지 않는다. 이 연구에서는 앱 테스트를 실행하지 않았다.

## 출처 재검증

- `SOURCE_MANIFEST.json`의 source_url로 각 제조사 PDF를 저장소 밖 임시 디렉터리에 다운로드한다. UR S3 주소는 공식 landing_url의 링크에서 따라간 것이다.
- `shasum -a 256 <다운로드 파일>`로 manifest와 비교한다. 다르면 새 판본으로 보고 페이지/발췌를 다시 검증한다. 현재 문서 전체는 커밋하지 않는다.
- macOS PDFKit으로 PDFDocument.pageCount와 각 PDFPage.string을 추출했다. 페이지 번호는 index+1이고 인쇄 번호는 footer 렌더에서 따로 확인했다. 시스템에 Poppler/pypdf가 없어서 설치 없이 PDFKit을 사용했다.
- PDFPage.thumbnail을 PNG로 렌더하고 Dell 1/11/245, UR5e 329/362, APC 10/20쪽을 직접 열었다. UR5e 마지막 페이지의 10.5.152는 텍스트 추출에 누락되어 이미지로 확인했다.
- 발췌 검사는 줄바꿈을 포함한 실제 텍스트와 대조한다. 첫 UR 문장 검사는 줄바꿈 때문에 실패하여, 최종 manifest에는 실제 연속 문자열인 짧은 절 제목만 채택했다. 최종 검사에서는 모든 발췌 일치와 문서당 25단어 이하를 확인했다.
- `git diff --check`와 `python3 -m json.tool docs/workers/evaluation/research/SOURCE_MANIFEST.json`으로 문서/JSON 기본 검사를 수행한다. 실제 장비/앱/API 검증과 별개다.

## 전달

기반 717514d, 소유 경로 docs/workers/evaluation/research/만 커밋한다. 커밋 확인: `git log -1 -- docs/workers/evaluation/research`. 기존 계정으로 현재 브랜치를 origin에 push하고, 성공 여부와 SHA를 Orca worker_done 3문장에 기록한다. 자격 증명을 변경하지 않는다.
