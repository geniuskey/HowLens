# 공식 출처·시각 근거

실제 열람일 2026-10-09 KST. 아래는 디자인 판단 근거이며 현장 안전 인증이나 사용자 시험 결과가 아니다. 타사 스크린샷은 원본 링크로만 인계하고 제품 이미지로 재배포하지 않는다.

| 출처 | 확인한 내용 / HowLens에 적용 | 한계·버전 |
|---|---|---|
| [Android Make apps more accessible](https://developer.android.com/guide/topics/ui/accessibility/apps) | 48×48dp 최소 touch target, 작은 글자 4.5:1, 큰 글자 3:1, 목적 중심 contentDescription. 16sp/48dp 및 TalkBack 기준 채택 | 페이지 2026-09-22 갱신. 최신 Compose 예제와 현재 앱 의존성 호환은 App 담당 확인 |
| [Android mobile accessibility](https://developer.android.com/design/ui/mobile/guides/foundations/accessibility) | sp 사용과 사용자의 폰트 크기 존중. 이 프로젝트는 가독성을 위해 본문 16sp를 별도 결정 | 가이드의 최소 본문 수치를 그대로 목표로 삼지 않음 |
| [CameraX preview](https://developer.android.com/media/camera/camerax/preview) | PreviewView는 crop/scale/rotation과 lifecycle binding을 가짐. FIT는 전체 프레임, 기본 FILL_CENTER는 잘릴 수 있음 | 페이지 2025-03-04 갱신. 현재 앱은 TakePicture intent; 문서는 선택적 in-app preview 확장의 근거이지 구현 증거 아님 |
| [Google Lens 공식](https://search.google/ways-to-search/lens/) (lens.google 리디렉트) | 사진·카메라·스크린샷을 먼저 선택하고 질문을 결합하는 상호작용. 말로 설명하기 전 눈앞의 대상을 쓰는 구조를 채택 | Lens 생성·영상·쇼핑·상시 분석 기능은 HowLens 범위 아님; 마케팅 성능 약속 복제 안 함 |
| [Dynamics 365 refreshed mobile experience](https://learn.microsoft.com/en-us/dynamics365/field-service/mobile/do-work-newux) | 공식 options와 settings 이미지를 다운로드해서 직접 봄. 세부 행동을 bottom sheet로, 설정을 별도 화면으로 분리하는 점 채택 | 페이지 2026-07-17 갱신. 공식 이미지는 iOS 프레임이므로 Android chrome 복제 근거가 아님. Agenda의 많은 필드는 작은 HowLens 화면의 반면교사 |
| [Dynamics inspections](https://learn.microsoft.com/en-us/dynamics365/field-service/inspections) | 질문 아래 답변을 두는 Comfortable density, 필수 항목과 technician preview를 분리. 조건 상세에 적용 | 문서의 Dynamics 최소 버전 9.1.0000.15015+는 HowLens와 무관. 업무 주문/조직 역할/동기화는 범위 밖 |

Dynamics 실제 확인 이미지: [작업별 옵션 시트](https://learn.microsoft.com/en-us/dynamics365/field-service/media/mobile-newux-options.png), [설정 화면](https://learn.microsoft.com/en-us/dynamics365/field-service/media/user-settings-newux.png). 공식 설명과 이미지 2장을 열어 실제 UI임을 확인했다. Learn 페이지에는 authorization banner도 있었지만 본문·공개 이미지 열람은 성공했다.

접근 기록: IBM `/products/maximo/mobile`는 일반 Maximo 페이지로 리디렉트되어 모바일 화면 비교 근거로 채택하지 않았다. Material `/foundations/accessible-design/accessibility-basics`는 404였으므로 해당 경로를 검증 근거로 쓰지 않았다. Android 공식 접근성 문서의 Material 연결과 Compose 지침을 구현 기준으로 사용한다.

## 사용자 시각 권위

[팀장 레퍼런스 원본](../../../../assets/design/reference/team-lead-howlens-original.png)은 사용자 제공 이미지, coordinator가 11:52 KST에 정확한 로컬 경로를 전달했다. 원본을 그대로 복사했으며 외부 서비스에서 찾은 디자인으로 표시하지 않는다. 원본 해시는 asset provenance 파일에 기록한다. 전체 보드의 scanner logo/blue wordmark/쿨그레이/사진+단계/결과 시트 구조를 직접 확인했다. 과밀 라벨, 5탭, 채팅, 내부 PC 조작, 사진으로 완료 단정 등은 계약·최신 사용자 요청과 충돌해 제외했다.

## 현재 코드

Fetched ref `origin/geniuskey/feat-ui-foundation` / `f4f0ab65797a345d9af4565df9bf035ba22e8681`. MainActivity.kt는 coordinator가 명시적으로 read-only 허용한 뒤 열었고, ui/AnalysisViewModel.kt와 함께 확인했다. 현재 제시된 화면의 레이아웃 분석은 코드 기반; 실제 앱 screenshot과 emulator 검증은 별도 체크리스트 항목이다.

추가 열람: [Compose window insets](https://developer.android.com/develop/ui/compose/system/insets-ui), 2026-10-01 갱신, safeDrawing/IME inset 소비 설명. Material accessible-design/overview는 열었으나 JavaScript 안내만 반환하여 본문을 읽었다고 주장하지 않는다.
