# [DONE] W3 디자인 인계

2026-10-09 KST. 브랜치 Runixs/hackathon-research, 연구 기준 커밋 5c0b830 이후 디자인 추가. Android 제품 코드 변경 없음. 모델 변경·툴 업데이트·하위 에이전트 없음.

**사용자 팀장 레퍼런스의 블루 HowLens를 채택하고, 사진 분석 / 카메라 두 탭 + 적은 문구 + 한 주행동으로 정리했다.** 입력은 사진 선택 또는 로컬 preview/촬영에서 시작하고, 결과는 고정 사진·서버 판정/단계·펼치는 근거를 보여준다. 태그·설명문·개발 설정이 첫 작업을 밀어내지 않는다.

## App 담당이 읽을 순서

1. [DESIGN.md](DESIGN.md): 사용자 레퍼런스·방향 비교·색상/dp/sp/대비.
2. [SCREENS.md](SCREENS.md): 두 탭, 사진/질문, loading, needs-info/stop, guide·근거, 9패널, verification/share, 설정·오류 전체.
3. [CHECKLIST.md](CHECKLIST.md): 접근성·입력 보존·계약·오류 검증. 모든 APK 항목 미실행.
4. [PRODUCT.md](PRODUCT.md), [REFERENCES.md](REFERENCES.md): 사실·최신 사용자 범위·공식 근거.

## 콘셉트

- [입력 / 두 탭](../../../../assets/design/howlens-input-concept.png)
- [결과 / 단계와 근거](../../../../assets/design/howlens-result-concept.png)
- [팀장 원본](../../../../assets/design/reference/team-lead-howlens-original.png)
- [생성·복사 출처와 해시](../../../../assets/design/PROVENANCE.json)

두 장은 각각 생성한 독립 세로 Android 콘셉트이며 APK나 실제 장비 분석 결과가 아니다. 외부에 CONCEPT·데모·실제 작업 안내 아님을 표시했고 결과 안에도 mock 경고를 넣었다. 실제 제품에는 CONCEPT 문구를 넣지 않는다. 생성한 서버 그림의 단자/표시등/모델 로고는 제조사 근거로 사용할 수 없다.

시각 검토에서 확인한 구현 보정: 입력 raster의 사진 자리는 4:3보다 세로로 길고 장비 썸네일이 남아 있으므로 실제 앱은 SCREENS의 4:3/최대 화면 36%와 중립 장비 아이콘을 따른다. raster의 큰 wordmark·미세 음영·demo pill도 구현 강제 사항이 아니다; 앱바 20sp, 평면색, 작은 mode text가 명세다. 결과에는 데모 한정 안내 단계가 그려졌고 서버 mock-guide 테스트 fixture에서만 시연할 수 있다. guide 없는 실제 분석에는 이 화면을 보여주지 않는다.

## 검증한 것

공식 Android 접근성/CameraX/insets, Google Lens, Microsoft Dynamics 문서와 공식 제품 UI 이미지 2장을 열었다. 현재 원격 앱 f4f0ab6의 MainActivity와 ViewModel을 읽어 설정 선행·긴 단일 스크롤·결과 위치 문제를 확인했다. 팀장 원본을 직접 보고 무변경 복사한 바이트 일치를 검사했다. 최종 콘셉트 2장을 직접 열어 한국어·브랜드·모드·CTA·근거와 synthetic 표시를 확인했다.

색 대비 직접 계산: blue/white 5.75:1, 본문/쿨그레이 16.12:1, 보조 6.87:1. 청록/white 2.49:1이므로 본문·유일 상태 표시에 금지했다. PNG 해시/크기, 상대 파일 링크와 git diff 공백 검사를 수행했다. UI 코드를 생성하지 않아 web detector/native build/finish-build review는 적용하지 않았다.

## 남은 일

App의 현재 실행 스크린샷 전달·APK 구현·TalkBack/글꼴/IME/실물 카메라 검증은 pending. 현재 화면 평가는 코드 기반이며 렌더를 봤다고 주장하지 않는다. CameraX는 사용자 추가 범위이며 45분 prototype gate는 coordinator/App 담당 소유; 설계는 사진 fallback과 같은 POST 계약을 보존한다. 실제 장비·사진·guide 승인은 계속 pending이다.

초기 청록안은 이후 사용자 원본에 따라 폐기했다. 두 번째 블루 입력은 최신 두 탭 요청으로 한 번 수정했으며 최종 에셋은 입력/결과 2장만 인계한다. 전 기능 명세가 이미지보다 우선하며 추가 이미지 반복 생성으로 구현 시간을 소비하지 않는다.

커밋 SHA/push 결과는 현재 Dispatch worker_done으로 전달하며 `git log -1 -- docs/workers/evaluation/research/design`으로 확인할 수 있다.
