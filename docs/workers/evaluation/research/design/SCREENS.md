# 구현용 화면·상태 명세

Mode: Operate. 디자인 콘셉트이며 실행한 APK가 아니다. 문서 규칙이 AI 콘셉트 이미지의 미세한 차이보다 우선한다. 화면 폭 360dp 기준, 320/412dp 및 200% 글꼴 대응. 기존 API와 모든 기능을 보존하며 새 서버 상태를 만들지 않는다.

## 0. 현재 화면에서 바꾸는 것

검토 기준 `origin/geniuskey/feat-ui-foundation` SHA `f4f0ab65797a345d9af4565df9bf035ba22e8681`, MainActivity.kt와 ui/AnalysisViewModel.kt read-only. 현재는 20dp padded 긴 Column 안에 제목→설명→mock 스위치/경고/시나리오 또는 baseURL→장비 3칩→앨범/카메라→파일 수치→160dp 사진→3줄 질문→버튼→결과가 놓인다. MaterialTheme 기본값이며 별도 Kotlin theme 파일이 이 tree에 보이지 않는다. guide에는 visual 통합 예고만 존재하고 검증/공유의 현재 구현은 이 스냅샷에서 확인하지 못했다. 아래 전체 흐름은 계약을 구현할 담당자에게 주는 명세이며 이미 구현됐다고 주장하지 않는다.

설정·디버그를 별도 화면으로 옮기고 기존 콜백과 fake scenario 선택을 보존한다. 입력과 결과를 분리하되 back/cancel에는 사진·장비·질문을 보존한다. 분석 응답 ID는 상세 내부 진단용이며 기본 화면/공유에는 노출하지 않는다.

## 1. 두 입력 탭: 사진 분석 / 카메라 (최신 확정)

앱바(HowLens, 작은 데모/실제 분석, 설정) → 상단 2탭(minHeight 48dp, 각 50% 폭, blue underline + selected semantics) → 56dp selected-device row. 탭은 입력 수단이며 별도 제품/백엔드가 아니다. 분석/결과 동안 전환은 잠금 또는 명시적 입력 복귀 이후 허용한다.

**사진 분석 탭**: 4:3 사진 자리(이미지 icon + `장비 사진`만) → sticky `사진 선택` 56dp. 갤러리 OpenDocument JPEG/PNG를 연다. 사진이 없으면 질문 필드를 숨겨 불필요한 읽기를 줄이고, 선택 후 사진+editable 질문+`확인하기`로 바뀐다. 이전 시스템 TakePicture fallback은 사진 바꾸기 sheet에 보존할 수 있다. 첫 화면에 장비 사용 설명·AI 마케팅 문단·추가 배지는 없다.

**카메라 탭**: 같은 프레임 위치에 로컬 CameraX PreviewView + sticky `촬영`(카메라 icon). 초기 권한 요청은 이 탭 진입 때만. 촬영 후 프레임을 고정해 사진 검토+질문+`확인하기`로 전환한다. 촬영과 서버 전송은 별도 의도적 행동이며 영상/상시 분석·자동 업로드 표시 없음. 사진 검토에서 `다시 촬영`은 보조, `확인하기`는 주행동. 실제 프레임 취득 전 분석 불가.

- 권한 거부: `카메라 권한이 필요해요` + `사진으로 확인` primary(사진 분석 탭으로 이동), 영구 거부 시 `설정 열기` 보조. 카메라 unavailable/startup failure도 같은 사진 탭 fallback. 빈 검은 화면을 무한히 두지 않는다.
- 모드: `데모` / `실제 분석` 1개만. Demo는 offline fixture, live는 서버 분석일 뿐 안전 승인 아님. 모드와 카메라 preview 여부는 독립이다.
- 장비 row→bottom sheet의 3개 64dp+ radio rows: `서버 / Dell PowerEdge R750`, `협동로봇 / UR5e`, `UPS / APC Smart-UPS`. 내용이 길면 높이 자동 증가. 선택 변경 시 기존 분석 무효화 규칙 유지.
- 프리뷰는 안전 상태/장비 식별 완료 표시가 아니다. AI bounding box/점멸 분석/임의 zoom 버튼을 추가하지 않는다. lifecycle에서 탭 이탈/백그라운드 시 camera release; 회전과 crop/저장 사진 일치 검증. 입력 도중 탭 전환은 기존 질문/선택 사진 초안을 보존하며 분석 대상은 선택된 고정 사진으로 명확히 한다.
- 촬영 취소/실패/앨범 취소에서 기존 입력 보존. 파일 제한은 평소 숨기고 오류 시 `10 MiB 이하 사진을 선택해 주세요` / `20MP 이하 사진을 선택해 주세요` / `JPEG 또는 PNG를 선택해 주세요`처럼 해당 문제만 노출한다.

## 2. 사진 선택 후 / 질문

사진을 Fit로 표시하고 row 하단에 48dp `사진 바꾸기`(카메라/앨범 sheet). 바로 아래 진짜 editable OutlinedTextField `무엇을 확인할까요?`, 본문 16sp, minLines 2/maxLines 4 뒤 내부 스크롤. **chevron·읽기 전용·필수 아닌 것처럼 표시 금지.** 촬영 전에는 질문 필드를 생략해도 초안은 보존한다. 선택 후 primary는 `확인하기`(mock는 `예시 확인`).

Trim 1–2,000자 검증 유지. 빈 질문으로 CTA 누르면 `확인할 내용을 입력해 주세요`를 field supporting text로 표시하고 focus+IME 이동; 2,000자 초과 시 오류와 현재 글자 수를 표시. 오류 전 숫자 카운터/장문 설명 없음. 전송 중 사진/장비/질문 변경 잠금; 취소는 가능. 사진은 업로드/모델 식별 승인과 동일하지 않다.

## 3. 분석 중

사진 유지 + indeterminate progress + `사진을 확인하고 있어요`(mock `예시를 불러오고 있어요`). 주행동 위치는 취소(tonal)로 변경. 서버가 제공하지 않는 단계/퍼센트/남은 시간·문서 검증 완료를 만들지 않는다. TalkBack은 진입 때 한 번 공지; 매 프레임 읽지 않는다. Back은 취소와 동일, 나중에 도착한 이전 결과가 새 입력을 덮지 않도록 기존 Job cancellation 유지.

## 4. 추가 정보 필요

사진 → warning icon + `사진이 더 필요해요`는 missing_information이 실제 사진 문제인 경우만; 일반 fallback은 `추가 확인이 필요해요`. 서버의 주요 missing_information을 그대로 짧게 보여주며 길면 줄바꿈. 나머지는 `확인할 내용 N개` 펼침(48dp). 단계/visual/verification 버튼 없음.

주행동: 사진 문제 `다시 찍기`, 모델/질문 문제 `입력 수정`. 상태 문자열만 보고 항상 재촬영을 강요하지 않는다. 모델 근거 부족 등 사용자가 사진으로 해결 못하는 경우 `입력으로 돌아가기`; 임의 해결 절차 생성 금지. `문서 근거` row는 evidence가 있을 때만, 없으면 상세에서 `연결된 근거 없음`. mock라면 `실제 작업에 사용하지 마세요` 한 번. 출처 공유는 보조 text action이며 검증된 허용 필드만.

## 5. stop

상단 위험 icon + `작업을 멈춰 주세요`, 서버의 핵심 경고는 접지 않고 먼저. 사진은 축소해 경고가 먼저 읽히도록 한다. primary `입력으로 돌아가기`. `이해했습니다`를 눌러 guide로 통과시키지 않는다. 모든 warnings는 아래 목록, 문서 근거는 펼침. 공유는 가능하나 stop 상태 유지; mock는 테스트 표시. 담당자 자동 연락/비상 동작은 기능으로 만들지 않는다.

## 6. guide + 단계별 근거

`문서 근거 안내` 제목(안전 승인/정상 판정 금지) → 중요 warnings/필수 conditions → 현재 단계 `1 / N` + 원문 description → 48dp `근거 보기` → 이전(보조)/`다음 단계`(주행동). N은 실제 1–9개. 로컬 단계 이동은 수행 완료나 장비 상태 변화의 서버 기록으로 취급하지 않는다. 전 단계 목록은 `전체 단계` 펼침; 현 단계의 모든 경고를 유지한다.

`근거 보기`는 ModalBottomSheet(초기 최대 가용 높이 80%, 큰 글꼴 전체 높이 가능): 해당 step evidence_ids에 대응한 document_id·version·PDF page·printed page·section·정확 quote·공식 source URL·`원문 보기`. 긴 URL 줄바꿈, null printed_page는 `인쇄 쪽수 없음`, 스크린리더 heading. 외부 앱 실패는 snackbar `문서를 열 수 없어요`와 재시도; 진입 전의 현재 단계를 잃지 않는다. 필수 conditions는 satisfied/unsatisfied/unknown을 `확인됨/미충족/미확인`으로 표시하고 앱에서 임의 체크하여 승인을 바꾸지 않는다.

마지막 단계 primary `전후 사진 확인`, 보조 `설명 이미지`/`출처 공유`. 실제 단계와 이미지를 동시에 강한 버튼으로 경쟁시키지 않는다. mock guide는 로컬 테스트만, 실제 visual API 호출 금지.

## 7. 설명 이미지 9패널

Guide 안의 선택 기능, 새 절차 아님. 기존 `POST visual`/polling 계약 사용. queued/running은 사진·텍스트 유지 + 작은 진행 표시. 성공 시 3×3 순서 미리보기(index 0–8, 화면 label 1–9), 탭하면 panel full-width와 연결 step 원문. 작은 썸네일 안에 본문을 그려 넣지 않는다. 각 썸네일은 ≥48dp와 TalkBack `설명 이미지 2 / 9, 단계 1`처럼 실제 매핑 사용.

320dp 또는 큰 글꼴에서는 1열 panel 목록으로 전환해 **서버 순서와 9개 전체** 보존; 9단계로 재명명 금지. image failure: `이미지를 불러오지 못했어요` / `텍스트로 계속` primary. failed 서버 생성 재시도는 1회만 보조 버튼; 자동 재생성 금지. 기존 completed job은 재사용. 깨진 이미지도 텍스트 단계는 남긴다.

## 8. 전후 확인

Guide에서만: 원 사진 `이전` + 새 사진 `이후`, Fit. 두 사진은 최소 136dp 폭 확보, 320dp/큰 글꼴이면 세로 배치. after 없으면 primary `사진 찍기`, 있으면 `변화 확인`. 갤러리도 유지. user_confirmation은 `직접 확인한 내용` 접힌 optional field(최대 2,000자), 서버 승인 체크박스로 표현하지 않는다.

결과 원문 enum 매핑: observed_change=`보이는 변화가 있어요`, issue_remaining=`문제가 남아 보여요`, inconclusive=`사진만으로 판단하기 어려워요`. 항상 limitations를 가까이 표시하고 `안전·정상 동작 확인이 아니에요` 문구를 유지. 증거 ID는 원 분석 범위. primary는 inconclusive면 `다시 찍기`, 그 외 `출처 공유`; `수리 완료`, 100%, 정상/안전 배지 금지.

## 9. 공유 / 설정 / 공통 복구

공유는 ../RUNBOOK.md 그대로: `공유할 내용` 미리보기→native Sharesheet. 문서 메타데이터·판정·mode·고정 한계만 기본; 자유 질문/원문 관찰/사진/시리얼/토큰/분석 ID 제외. 사용자가 대상 선택·전송, 취소하면 결과 유지. CONCEPT는 산출물 표시이며 실제 제품 문자열이 아니다.

설정: 상단 gear 48dp→별도 화면. 기존 demo/live switch, mock scenario, server baseURL, 연결 설명을 모두 여기 보존. 임의 health 성공/연결됨을 표시하지 않는다. 적용하면 기존 ViewModel의 결과 초기화 의미를 유지하고 입력은 보존. Back은 원 작업으로. 실행 중 설정 변경 금지.

오류: 네트워크/503/504는 원 입력+`연결을 확인해 주세요`, retryable일 때만 `다시 시도`. 413/415/422는 사진 또는 질문 옆 오류와 `입력 수정`. 404 분석 소실은 `결과가 만료됐어요`+`다시 분석`; 자동 재과금 재분석 없음. 409는 `이 결과에서는 사용할 수 없어요`+기존 결과. 알 수 없는 오류에도 결과/사진 손실 없이 원 상태로 복귀. FastAPI detail 배열과 객체 모두 화면 오류에 연결.

## 접근성·반응형 계약

Scaffold content padding과 safeDrawing/navigationBars/IME inset 각각 한 번 소비. 최소 버튼 높이를 고정 높이로 쓰지 않는다. 상태 heading에 접근성 focus 이동, 수정 오류는 해당 필드로 이동. 의미가 있는 사진 description, icon-only 설정/닫기/뒤로 설명, 중복 Text contentDescription 금지. system Back/predictive Back 존중, 시트 닫기→원 화면 순. 1.0/1.3/2.0 글꼴, TalkBack 순서, landscape와 키보드 노출을 체크한다. 태블릿 대상이면 최대 600dp 단일 작업 열 또는 사진/내용 2열, 하단 CTA는 내용 열에 위치. 숨은 기능을 제스처에만 의존하지 않는다.

## CameraX 구현 경계

두 탭은 사용자 확정 범위다. 카메라 구현의 45분 prototype gate·별도 모듈 소유는 coordinator/App 담당이 결정한다. 레이아웃은 PreviewView와 fallback 모두 수용하되 구현되지 않은 preview를 성공처럼 표시하지 않는다. 실제 local capture time을 기록한 경우에만 상세에 표시하고 서버 응답과 같은 frozen photo를 유지한다. 정지 사진 API 계약과 합성/실제 구분은 바뀌지 않는다.
