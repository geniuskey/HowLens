# 고정 레퍼런스 수용 기준

Authority: /Users/runixs/HowLens/content.png, UI_REQUIREMENTS.md, docs/orchestrator/REFERENCE_UI_BRIEF.md (2026-10-09). 이미지 보드의 다른 탭 변형보다 brief의 홈/가이드/중앙카메라/AI도움/내정보 순서가 우선한다.

| 위치 | 필수 구성·동작 | 확인 방식 |
|---|---|---|
| Home 상단 | HowLens + 검색/알림/프로필 아이콘; 짧은 복사 | 320/390 캡처, 버튼/접근가능 이름 |
| Hero | 선명한 파랑 배경, 실제 하드웨어 사진, 짧은 제목, 흰 CTA | 레퍼런스 왼쪽 홈과 비교; CTA 클릭 즉시 camera |
| 카테고리 | 조립하기/부품교체/문제해결/업그레이드 원형 아이콘 4개 | 순서·줄바꿈·각 목적지 확인 |
| 추천 가이드 | 실제 사진 썸네일 카드, 제목·짧은 정보 | 2열/좁은화면 잘림, 카드 클릭 |
| 하단 5칸 | 고정 bar, 중앙 큰 파란 camera direct action | scroll/320/390/desktop, 홈·가이드·AI도움·내정보 |
| 카메라 | explicit click에서만 요청, 어두운 전체화면, 파란 corner, shutter/gallery | denied fallback, capture/retake/back; 장식 감지박스 금지 |
| 분석 | 사진 미리보기, loading, 취소/복구; 모의임을 명시 | 타이머 결과를 실제 확정으로 제시하지 않음 |
| summary/guide | 후보 표현, source 또는 demo 태그, 이전/다음 진행 | 비-guide native guard 유지; mock 승인 금지 |
| 완료 | 사용자 명시 확인, 시각 변화만, 성공 보증 없음 | checklist/check/완료 버튼 |
| AI도움/내정보 | 정직한 local/demo 또는 연결 상태, 입력/전송·설정 동작 | keyboard/Enter/back/history |
| 모든 화면 | 최소48 target, 긴 한글/확대, no horizontal overflow, keyboard focus | DOM geometry + 실제 캡처·키 입력 |

초기 Android 소스(수정 전): MainActivity.kt enum HOME/PHOTO/CAMERA, bottomBar homeVisible 제외, HomeEntryPane 호출에 heroImage 없음. HomeEntryPane의 카메라 그림 fallback·세로 설명/버튼 구성은 pinned reference와 다름(P1). 소스의 onOpenCamera 직접 CAMERA 전이와 canShowSteps gate는 보존할 긍정 요소. 이는 실제 새 APK 실패 판정이 아니며 새 빌드 캡처는 별도 필요.

미검증 항목에는 PASS를 주지 않는다. source SHA와 캡처 시각을 기록하고 변동 중인 HTML/Android 파일의 판정을 혼합하지 않는다.
