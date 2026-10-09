# Reference UI 독립 감사 — 최초 HTML 판정

후속 수정본 판정: [c503985 한정 재검증](recheck-c503985/REPORT.md). 아래는 최초 검사본의 역사적 결과이며 최신 결론으로 혼용하지 않는다.

2026-10-09 KST. 사용자 승인된 시각 방향을 유지한다. 제품 수정 없음; 아래 결과는 Chrome headless 실제 렌더/DOM·클릭·키 입력 검사다. Android 신규 APK 판정은 **미검증**이며 HTML의 성공을 전용하지 않는다. 현재 확정 P0 없음, 기능/접근성 **P1 4건 미해결**. 전체 PASS 아님. 수정은 builder 한 번의 batch로 인계했으며 native 작업을 기다리게 할 이유는 없다.

## 검증 대상과 증거

권위: `/Users/runixs/HowLens/content.png` 실제 열람, `UI_REQUIREMENTS.md`, `docs/orchestrator/REFERENCE_UI_BRIEF.md`. 이전 디자인 제안 대신 레퍼런스 왼쪽 홈 구성을 기준으로 삼았다. 마지막 Coordinator 지시: 사용자 현재 HTML 구성 승인, 새 미학 탐색 금지.

읽기 전용 대상 `/Users/runixs/HowLens/docs/mockups/reference-ui/`:

| 파일 | 검사본 SHA-256 |
|---|---|
| index.html | a4d7a150831b351665e51c28caec607c94037432304f04c263a3e16179662aa7 |
| app.js | be7d3b91b70ae75a6c90ba731014de5dfb2eb29abd405ec1ce29393f3da395f8 |
| style.css | 37028c0f05b5475e6fcc76d8238f21ed96ae0e5de98526c19fe1457e919ddf09 |

실제 로컬 file:// URL, viewport 320×844 / 390×844 / 1280×900, deviceScaleFactor1. 주요 캡처: [320 홈](evidence/home-320.png), [390 홈](evidence/home-390.png), [desktop 홈](evidence/home-1280.png), [카메라 오류 대안](evidence/camera-fallback-final.png), [분석 요약](evidence/summary.png), [완료](evidence/confirmation-done.png). 전체 사진/화면을 fake UI로 붙이지 않았고 실제 DOM을 캡처했다. CDP/자체 프로필 사용; Aside agent·유료 API·기기 제어 없음. source/capture 해시 목록은 EVIDENCE.json.

## P1 — 수정 우선순위

1. **손상된 이미지가 정상 preview/분석 대상으로 통과** — app.js `#upload.onchange` (마지막 파일입력 handler). `new File(['not an image'],'broken.png',{type:'image/png'})`를 단일 입력으로 넣으면 `#preview`로 이동하고 naturalWidth=0인데 데모 분석 시작 버튼이 활성화된다. [캡처](evidence/invalid-image.png), [DOM 로그](evidence/edge-cases.json). MIME/크기만 확인하고 decode 실패/FileReader 오류를 처리하지 않는다. **수용 기준:** image.decode 또는 createImageBitmap 성공 후에만 photo 교체/preview; 실패 시 기존 입력 보존, 오류 설명과 재선택; 잘못된 데이터로 다음 진행 차단. 외부 전송·실제 분석은 수행하지 않았다.
2. **새 작업에 이전 완료 상태가 남음** — app.js 전역 `complete`, `data-guide`, `#sample`, `#capture`, `#finish`. GPU 가이드 끝에서 체크/완료 → 가이드 목록 → SSD 가이드 끝으로 이동하면 첫 방문부터 체크됨+“확인을 기록했어요”+완료 활성 상태다. [캡처](evidence/second-guide-confirmation.png), edge-cases.json secondGuide.checked=true/disabled=false. **수용 기준:** photo/guide 작업 identity별 상태 또는 새 작업 시작 시 complete=false; 같은 작업 내 이전/다음 보존은 유지. 사용자의 새 완료 확인 없이 완료 처리 금지.
3. **전체화면 카메라 뒤 숨겨진 nav가 키보드 focus 대상** — app.js camera branch, style.css `.camera-page` z-index10 / nav z-index5. camera에서 실제 Tab12회: 취소→사진선택→예시→화면 뒤 홈/가이드/촬영하기/AI도움/내정보로 순환한다. [실제 focus 순서](evidence/interactions.json) cameraKeyboard. **수용 기준:** camera 활성 중 배경 nav hidden/inert, 카메라 첫 동작으로 focus 이동, 나갈 때 진입 버튼으로 복귀. 시각적으로 없는 버튼을 키보드로 실행하지 않게 한다.
4. **상단 아이콘 48px 수용 기준 미달** — style.css `.tools button`, max-width340 media override. 320 홈 검색/알림/내정보 실제 폭34px, 기본 폭38px; 높이48만 충족한다. [측정](evidence/home-dom.json), home-320.png. **수용 기준:** 320에서도 최소48×48 hit box, 로고/아이콘 충돌 없음. 사용자 브리프의 명시 기준에 따른 판정이며 별도 WCAG 위반 판정을 추정하지 않는다.

모두 보고 시점 미해결; builder 수정 소스나 재캡처 없이 resolved로 표시하지 않는다.

## 추가 기능·수용 간극 (P2 / 승인 방향 유지)

- **직접 URL/새로고침:** app.js 초기 `route='home'` 때문에 `#guides`에서 reload하면 주소는 guides지만 화면은 홈이다. [직접 reload 캡처](evidence/direct-hash-reload.png), final-checks.json. 같은 문서 hashchange는 동작하므로 cold load와 구분한다. 초기 route를 허용 route 목록으로 읽고 잘못된 hash는 안전한 홈으로 처리할 것.
- **도움말 내역 소실:** 질문 전송 후 홈 왕복 시 `render()`가 messages 초기 문구로 재생성한다. [질문 전송 화면](evidence/help-sent.png), edge-cases.json chatReturn. 요구사항의 불필요한 상태 초기화 방지와 다름; 탭 왕복에 대화 배열 보존 권장.
- **Bottom sheet 구성:** summary는 닫기 가능한 overlay가 아니라 일반 page `.sheet` 카드다. [화면](evidence/summary.png). 가이드로 이어지는 기능은 있으나 원문의 sheet/닫기 수용 항목은 미충족. 현재 사용자 승인된 홈 방향을 바꿀 요구는 아님.
- **카메라 corner:** [카메라](evidence/camera-fallback-final.png)는 연속 둥근 사각 테두리. 브리프의 파란 corner와 다름. 검출 성공 표시는 없고 전체 사진을 자르지는 않는다. 초기 status의 P1 후보를 최종 P2로 낮춤: 기능 blocker가 아니므로 native 출시보다 우선하지 않음.
- **사진 의미 대응:** 홈 SSD/메모리 카드가 같은 범용 PCB 사진을 쓰며 SSD/메모리 부품 식별이 분명하지 않다. 레퍼런스의 해당 부품 사진과는 차이지만 현재 홈 구성 사용자 승인됨. 실제 작업 guide 근거 사진으로 오해시키지 않도록 예시 태그 유지.
- **desktop nav:** 본 감사 1280×900에서는 5칸 bar 표시 확인. Coordinator가 별도 viewport에서 phonecard/nav 간격 문제를 보고했으나 해당 캡처는 읽기 범위 밖이라 재확인하지 않았음; 별도 owner 수정 항목으로 유지.

## 실제 확인한 동작

- header 검색/알림/프로필, 홈 복귀, 네 카테고리의 각각 맞는 필터 결과, 없는 단어 검색의 빈 상태.
- hero CTA와 중앙 camera 모두 중간 폼 없이 camera route 진입. headless에서 카메라를 사용할 수 없다는 문구/파일 대안 확인. 실제 카메라 영상·shutter capture·OS permission prompt는 미검증.
- 예시사진 → 전체 사진 preview → 명시적 데모 분석 → 예시 결과 → 3단계 이전/다음 → 체크 후 완료. 자동 timer 성공 확정 없음. 최초 완료 버튼은 체크 전 disabled이며 최초 작업에 대해서는 정상.
- 저장 가이드가 내 정보에 나타나고 저장지우기로 제거됨. 검색/filter/저장/내정보 경로는 실제 수행. 계정/서버 저장으로 표시하지 않는다.
- 도움말 질문 입력/submit 후 사용자와 규칙응답이 표시됨; live AI가 아니라 로컬임을 명시함. 예시 안내는 실제 guide 승인으로 표시하지 않는다.
- 320/390/1280 기본 홈 및 390 진행 화면에서 document horizontal overflow=false. 320 문자 1.5배 **주입 스트레스** 캡처는 [여기](evidence/large-text-320.png): 줄바꿈이 촘촘하지만 페이지 수평 overflow 없음. OS 글꼴 크기 테스트 아님.
- 390×420 짧은 viewport에서 입력을 focus/scrollIntoView했을 때 입력·전송 버튼이 bar 위에 보임([캡처](evidence/help-short-viewport.png)). 실제 Android IME·TalkBack·브라우저 zoom·회전 검증으로 대체하지 않는다.

DOM `.click()` 경로 검사는 터치/포인터 hit-test 전체 보장을 하지 않는다. 파일 picker 실제 OS 선택, 유효 사용자 이미지 upload, hardware flash/zoom/switch, 모든 selector/button의 전수 검증, runtime console error 전체 수집은 미실행이다. 따라서 “모든 버튼 PASS”나 접근성 인증/수치 점수를 만들지 않는다.

## Native 인계 기준

수정 전 MainActivity enum HOME/PHOTO/CAMERA, 홈에서는 bottomBar 숨김; HomeEntryPane 호출에서 heroImage 미전달로 카메라 그림 fallback. 4카테고리/사진카드/5칸 nav 없음. 이는 새 구현 전 소스 관찰이며 신규 APK 결함이라고 단정하지 않는다. 기존 onOpenCamera는 직접 CAMERA 전이하고 결과 canShowSteps 조건은 유지해야 한다.

새 APK 최소 수용: 320dp 홈 캡처에 하드웨어 hero+흰CTA+4카테고리+사진카드+5칸 nav, 두 camera 진입 실제 preview, 허가거절 gallery, capture/retake/cancel+photo보존, 실제 분석 request loading/error, needs-info/stop에 steps 없음, guide 조건에서만 단계 노출, 새 작업 완료상태 분리, keyboard/insets/48dp 확인. 사용자 승인 HTML 방향대로 구현하고 모의 예시와 실제 서버 결과를 분리한다. 현재 본 reviewer의 native APK 실측 없음.

## 결론·후속

시각 구성은 사용자 승인 및 실제 캡처로 확인되었고 기본 데모 경로는 연결되어 있다. 우선 P1 네 건을 한 번에 수정하고 관련 캡처/DOM만 한 번 재검증하면 된다. 현재 보고는 definitive **현재 검사본** 결과이며 이후 수정본의 PASS를 선약하지 않는다. 전체 성능·테마·접근성을 측정하지 않아 20점 환산은 생략한다. 제품 파일은 수정하지 않았다.
