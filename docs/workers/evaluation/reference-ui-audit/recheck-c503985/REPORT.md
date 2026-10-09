# c503985 요청본 — 한 차례 한정 재검증

2026-10-09 13:51 KST, 이전 감사 da0b25b의 P1 4건 + hash/help 회귀만 검사했다. **기존 P1 재현 4/4 해소, help PASS, cold hash FAIL**. 제품 파일·main Git·native 기기 변경 없음. 사용자 승인된 구성에 대한 새 디자인 라운드는 하지 않았다. native APK PASS를 뜻하지 않는다.

## 실행 증거

읽기 대상 `/Users/runixs/HowLens/docs/mockups/reference-ui`, Chrome headless 로컬 file://, 320×844와 390×844, 각 viewport에서 동일 6항목을 한 번씩 실행했다. source SHA 및 실제 DOM/focus 결과는 [results.json](results.json), 재현 스크립트는 [check.mjs](check.mjs). c503985는 Dispatch가 지정한 기준이며 main Git을 조작/검사하지 않고 검사 직전 실제 바이트 해시를 기록했다.

- index.html: `a4d7a150831b351665e51c28caec607c94037432304f04c263a3e16179662aa7`
- app.js: `bc4a6f6e85de8b283f56c90b2551ea9ee0dd4140391c7d3ec41bd96f85fb4816`
- style.css: `f3df6ca9bb2c4740c110ea236c48f6aa299a458d6fd3d5395205b4b287976303`

| 이전 결함 | 320 | 390 | 실제 결과 |
|---|---|---|---|
| P1 손상 PNG가 분석에 진입 | PASS: 진행 차단 | PASS: 진행 차단+오류 표시 | `File(['not an image'], 'broken.png', image/png)` 후 camera 유지, analysisButton=false |
| P1 다른 가이드에 완료 상태 누출 | PASS | PASS | 첫 가이드 완료 후 SSD 끝에서 checked=false, finish.disabled=true |
| P1 camera 뒤 nav로 keyboard focus 진입 | PASS | PASS | nav.hidden=true/display=none, 실제 Tab12회 중 inNav=0 |
| P1 header target48 미달 | PASS | PASS | 검색/알림/내정보 모두48×49px, 홈 document horizontal overflow=false |
| P2 도움말 탭 왕복 시 소실 | PASS | PASS | 입력 “보존 검증 사진”과 답변이 홈→도움말 복귀 뒤 유지 |
| P2 직접 hash reload | **FAIL** | **FAIL** | `#guides`로 이동 후 Page.reload: URL guides이지만 홈 렌더, search 없음 |

[320 홈](home-320.png), [390 홈](home-390.png), [손상 업로드390](corrupt-upload-390.png), [새 작업 완료초기화390](second-guide-390.png), [도움말 복귀390](help-return-390.png), [hash 실패320](hash-reload-320.png). 모든 PNG는 실제 렌더 캡처다. 전체 mockup 품질이나 모든 버튼 PASS로 확대 해석하지 않는다.

## 남은 기능 문제

**P2 cold hash 미해결** — app.js 초기 `route='home'`, 맨 마지막 `hydrate();render()`에서 location.hash를 초기 상태로 읽지 않는다. hashchange 이벤트만 처리하므로 새로고침 시 주소와 화면이 어긋난다. 기존 보고와 동일 결함이다. 최소 수정: 허용 route 목록으로 초기 hash를 해석하고 photo가 필요한 경로의 선행조건을 확인한 뒤 최초 render; invalid hash는 안전하게 홈으로 처리. 본 작업에서는 수정하지 않았다.

**P2 오류 문구 경합 관찰** — 320에서 손상 업로드 직후 카메라 비동기 실패가 뒤늦게 도착해 상태 문구가 “카메라를 사용할 수 없어요…”로 표시됐다([캡처](corrupt-upload-320.png)). 390에서는 “사진을 열 수 없어요. 20MP 이하…”가 유지됐다. 코드상 camera catch와 decode catch가 같은 `#camera-status`를 덮어쓰는 경로와 일치하나 내부 callback 타임라인은 별도 계측하지 않았다. 차단 자체는 양쪽에서 유지되므로 이전 P1 진행 차단 결함과 구분한다. 오류 영역 분리 또는 최신 요청/상태 우선순위로 개선 가능.

## 범위와 결론

기존 broken-image 재현에 대해 decode guard가 작동하고, 기존 다른-guide 재현에 대해 complete=false가 작동한다. 모든 비동기 오류·실제 OS filepicker·실제 촬영·모든 guide/URL 조합을 검증했다는 뜻은 아니다. 기존 키보드 결함 판정은 배경 nav 제거에 한하며 일반적인 focus-return 전체는 미검증이다. 실제 Android IME/카메라/3대 기기의 native 검증은 Coordinator/기기 소유자의 별도 결과를 따른다.

남은 cold hash와 문구 경합을 즉시 Coordinator에게 보고했다. 추가 디자인 변경 요청 없음, 이번 timebox에서 브라우저 한정 재검증을 종료한다.
