# [DONE] 17시 데모를 위한 저비용 기능·장비 근거 조사

확인일: 2026-10-09 KST, 11:29부터 조사. 담당: Evaluation Research Worker. 기준: `717514d`, 계약 v0.1, 브랜치 `Runixs/hackathon-research`. 제품 코드는 열거나 변경하지 않았다. 시간은 구현 경험에 따른 추정이며 실제 구현·기기 테스트 결과가 아니다.

**권고: Dell PowerEdge R750 전면 비접촉 식별·상태 관찰 한 경로 + 출처가 붙은 텍스트 공유만 먼저 완성한다.** 제조사 PDF 3개를 다운로드하고 아래 실제 페이지를 확인했지만, 실물·사진·현장 조건은 모두 pending이다. 문서 확보는 `guide` 승인이나 설비 정상 동작의 증명이 아니다.

## 1. 추가 기능: 데모·사용자 가치 순위

구체적인 공유 payload·개인정보 제외·검증 명세는 [RUNBOOK.md](RUNBOOK.md)에 있다. 자유 텍스트 필터가 준비되지 않았으면 관찰/부족 정보 원문을 생략하고 상태·출처·고정 한계 문구만 공유한다. 기존 분석 화면과 v0.1 응답 모델이 이미 연결되어 있다는 조건이다. 새 API/생성 모델 없이 Android 담당자가 구현할 후보이며 연구 Worker는 구현하지 않는다.

| 순위 / 예상 | 최소 기능·가치 | 의존성 / 비용 없는 방식 | 검증 방법·범위 제한 |
|---|---|---|---|
| 1 / 20–30분 | 근거 링크 포함 공유 보고서. 판단, 관찰, 부족 정보, 문서명·판본·PDF/인쇄 페이지·공식 URL을 전달해 심사자가 근거를 재확인 | 기존 Analysis/evidence를 `ACTION_SEND`, `text/plain`, `EXTRA_TEXT`, `createChooser`로 직렬화. 서버 변경·PDF 생성·추가 AI 호출 없음 | guide/stop/needs_more_information 각 공유 미리보기 비교, 한글·빈 evidence·긴 URL·mock 표기 유지. non-guide에 단계가 섞이지 않는지 확인. 실제 전송 대상은 사용자가 선택. 사진/태그/비밀번호는 기본 제외 |
| 2 / 25–40분 | 전후 사진 나란히 보기. 사용자가 변화와 촬영 차이를 직접 확인 | 이미 보유한 두 로컬 이미지 URI와 Compose Image 재사용, `ContentScale.Fit`으로 전체 보존. 비교 표시 자체는 API 0회 | 같은 사진·다른 각도·흐림·회전·누락·다른 장비 케이스. 기존 verification 결과가 있으면 limitations와 함께 표시; 없으면 자동 판정하지 않음. URI 보존이 안 되어 있으면 40분 내 불확실 |
| 3 / 30–45분 | 승인된 현재 단계만 기기 내 TTS로 읽기. 화면을 계속 보지 않아도 되는 접근성 | `TextToSpeech` 초기화 성공, 설치된 한국어 Voice 중 `isNetworkConnectionRequired=false` 확인. `guide`의 저장된 단계만 읽고 자동 다음 단계 없음 | 비행기 모드 한국어 재생, 음성 미설치/초기화 실패, stop·화면 이탈·다른 분석 시 재생 중지. `shutdown` 처리. TTS 실패 시 텍스트 유지; mock는 테스트 표시 |

근거: [Android Sharesheet](https://developer.android.com/develop/ui/compose/sharing/send)는 텍스트 공유의 공식 Kotlin 예제를 제공한다. [Compose 이미지](https://developer.android.com/develop/ui/compose/graphics/images/customize)는 Fit의 종횡비 보존을 설명한다. [TTS](https://developer.android.com/reference/android/speech/tts/TextToSpeech)와 [Voice](https://developer.android.com/reference/android/speech/tts/Voice)는 초기화·자원 해제·네트워크 필요 여부를 명시한다. 모두 실제 열람했다. 기능 순위/시간은 이 근거와 현재 계약을 바탕으로 한 연구자의 판단이다.

버전 차이: Sharesheet 고급 미리보기는 Android 10/API 29 이상이므로 기본 텍스트만 우선한다. Voice 선택 API는 21 이상, Android 11 타깃 TTS에는 manifest queries 선언이 필요하다. 프로젝트 minSdk/targetSdk/Compose BOM 및 음성 설치 상태는 소유 범위 밖이므로 미확인이다. 공식 최신 예제를 그대로 옮기기 전 앱 담당자가 기존 버전과 대조해야 한다.

## 2. 실제 제조사 문서와 장비 후보

해시·다운로드 URL·짧은 정확 발췌는 [SOURCE_MANIFEST.json](SOURCE_MANIFEST.json)에 기록했다. PDF 페이지는 **1부터 시작**한다. 원본 PDF·전문 추출·페이지 이미지는 저장소에 넣지 않았다.

| 장비 / 정확한 문서 | 실제 확인 페이지와 판본 | 낮은 위험의 데모 후보 / 물리적 필수 조건 |
|---|---|---|
| **Dell PowerEdge R750**, 규제 모델 E70S / E70S001. [공식 Installation and Service Manual PDF](https://dl.dell.com/content/manual15837466-dell-emc-poweredge-r750-installation-and-service-manual.pdf) | 255쪽, **December 2024 Rev A11**. PDF/인쇄 11쪽 전면 왼쪽 패널 도해, 245쪽 표 87 상태·ID 표시. 표지와 두 페이지 렌더 확인 | **선정**: 이미 접근 가능한 전면의 모델 확인과 표시 위치/색상 관찰. 현장 관리자의 접근·촬영 허용, 정확한 R750 식별, 안정 설치, 전면 표시가 가려지지 않음, 통로·현장 위험 없음, 충분한 조명 필요. 랙 이동·베젤 분리·버튼·전원·케이블 조작 없이 촬영 가능한 조건만 |
| **UR5e e-Series**, [공식 SW 5.17 다운로드](https://www.universal-robots.com/download/manuals-e-seriesur-series/user/ur5e/517/user-manual-ur5e-e-series-sw-517-english-international-en/) → 제조사 연결 S3 PDF | 362쪽. PolyScope **5.17**, 문서 **10.5.152**, 번호 **710-965-00**, 국제 영어. PDF/인쇄 329쪽 About, PDF 362쪽 판본 렌더 확인. 다운로드 페이지 수정일 2024-07-04 | 대안: 현장 담당자가 이미 표시한 About 화면의 버전·식별 정보를 사진으로 읽기. 운영자 관리 아래 정지 상태·예기치 않은 재시작 방지 및 접근 안전 확인, 이미 켜진 pendant, 실제 SW 일치가 전제. 앱은 시동·Freedrive·프로그램 실행·설정 변경을 안내하지 않음. 현장 위험평가 미확인으로 이번 우선 경로에서 제외 |
| **APC Smart-UPS SMT1500I**, 1500VA 타워 230V 후보. [정확 SKU 공식 제품 페이지](https://www.se.com/sa/en/product/SMT1500I/apc-smartups-line-interactive-1500va-tower-230v-8x-iec-c13-outlets-smartslot-avr-lcd/), [SMT 타워 공식 매뉴얼](https://www.se.com/us/en/download/document/SPD_UM_SU-990-6411_EN/) | 문서 SPD_UM_SU-990-6411_EN, **EN 990-6411A / Rev A**, 20쪽. PDF 10 = 인쇄 8쪽 Display Panel, PDF 20 판권면 **04/2022**; 웹 등록일은 **2022-09-17**로 구분 | 대안: 이미 켜진 전면 LCD/표시등을 접촉 없이 식별·기록. 정확 SKU/전압/타워 여부와 패널 일치, 이미 설치된 장비, 접근 허용, 노출 배선·배터리 이상 등 현장 위험 없음이 필요. SMT1500I는 조사 후보일 뿐 실제 장비 모델은 pending. 메뉴/자가시험/출력·배터리 조작 제외 |

판본 주의: Dell A11의 페이지 숫자를 다른 판본에 이식하지 않는다. UR 문서의 `Document Version` 값은 텍스트 추출에서 빠졌으나 마지막 페이지 이미지에서 **10.5.152**를 확인했다. UR5/CB-series·다른 PolyScope 버전에 자동 적용하지 않는다. APC의 Smart-UPS라는 명칭은 SMT/SMC/SMX/SMTL, 랙/타워, 지역 전압을 구분하지 못한다. SMT1500I 제품 사양과 SMT 타워 매뉴얼의 제품군은 부합하지만 실제 SKU에 대한 최종 문서 매칭은 현장 라벨·제조사 연결 문서 확인 후 확정한다.

### 선정한 R750 데모의 승인 경계

- 입력: 정확 모델을 확인할 수 있는 자료와 비밀정보를 제외한 전면 사진. 정보 태그에는 iDRAC 기본 비밀번호가 있을 수 있음(11쪽); 태그를 꺼내거나 전체를 업로드하는 흐름을 요구하지 않는다.
- 확인 전에는 `needs_more_information`, `steps=[]`. 모델·필수 조건·사진 품질·해당 작업 근거를 서버가 확인한 후에만 비접촉 관찰 안내의 `guide` 후보가 된다. 현재는 모든 실물 조건 pending이다.
- 왼쪽 상태/ID 표시의 위치와 보이는 색만 서술한다. **정지 사진 한 장으로 점멸/고정을 판정할 수 없다.** 245쪽은 파란색 고정과 점멸을 다른 상태로 구분하므로 모호하면 추가 관찰 확인을 요청한다. 현재 `/analyses` 입력에는 사용자 확인 필드가 없으므로 이를 이미 지원한다고 가정하지 않는다; 오늘은 점멸 해석을 생략하고 부족 정보로 남겨 계약 확장을 피한다.
- 매뉴얼의 상태 설명을 화면의 관찰과 분리한다. 경고색/이상 징후에는 수리 단계를 생성하지 않고 담당자 확인으로 종료한다. 상태등이 정상 의미라도 실제 시스템 안전·정상 동작을 보증하지 않는다. 전후 촬영은 동일 부위를 더 선명하게 찍는 예시만 가능하며 수리 성공으로 부르지 않는다.
- 9장 그림을 위해 동작을 추가하지 않는다. 관찰 몇 단계로 9패널 의미 대응이 불가능하면 visual failed와 텍스트 보존이 계약상 맞다. 비용 없는 데모 경로는 실제 AI 실행 검증과 별개로 명시한다.

## 3. 오늘 17:00 KST 가능성·병목

11:40을 계획 기준으로 잡으면 약 5시간 20분이다. **12:00 착수 → 16:00 구현 동결 → 17:00 시험 종료**가 4시간 구현 + 1시간 검증의 마지막 시작점이다. 공유 기능 1개 + 장비 1개 + 기존 분석 흐름이 준비되어 있다면 조건부 가능하며, 세 기능 모두와 세 장비 live 시연을 약속할 근거는 없다.

- 12:00–13:00: backend 담당이 R750 판본·페이지·짧은 근거 등록과 모델/조건 차단을 확정; app 담당은 공유 기능 구현. 13:00까지 실물/사진 확보 실패 시 실제 장비 live demo는 pending으로 고정하고, 명시적 mock UI와 실문서 근거 열람 시연으로 범위를 줄인다.
- 13:00–15:00: 단일 경로 연결·오류 처리·근거 불일치 검증. 15:00 이후 추가 기능 착수 중지; 15:00–16:00 통합 문제 수정과 재현 데이터 준비.
- 16:00–17:00: 비-guide 단계 0개, 장비/판본 불일치, 흐린 사진·점멸 불확실, 위험 요청, visual 실패 시 텍스트 유지, 공유의 mock/부족정보 보존, 동일 전후 사진의 불확실 결과를 확인. 이는 **계획이며 미실행**이다.

가장 큰 병목은 실물·정확 모델·현장 접근 조건, 서버 근거 검증과 앱 통합의 현재 준비도이다. 9패널 생성 의미 검증과 유료 호출은 이번 조사로 검증되지 않았고, 추가 기능을 무료로 만들더라도 기존 live AI 비용까지 0원이 되는 것은 아니다. GitHub 권한은 조사 도중 coordinator가 복구되었다고 알려 기존 계정 그대로 push한다.

## 4. 실제 검증과 인계

- 완료: 공식 HTML 자료 열람, 제조사 원본 PDF 3개 다운로드, SHA-256 계산, PDFKit 전체 페이지 텍스트 추출로 페이지 수 확인, 선택 페이지 7장 렌더·시각 대조, manifest 발췌/페이지/해시 검사, git diff 공백 검사.
- 접근 제한: web 도구는 Dell PDF와 UR 대용량 PDF를 처리하지 못했으나 Python HTTPS 직접 다운로드는 성공했다. Schneider 미국 SKU URL은 제품 목록으로 이동하여 사우디 공식 SKU 페이지에서 확인했다. 검색 결과/비공식 미러는 최종 근거로 사용하지 않았다.
- 미실행: Android 빌드·TTS·공유·사진 비교, API 호출, 실제 장비 관찰, 실제 AI 평가. 안전 승인 없음. 유료 호출·하위 에이전트 없음.
- 변경 파일: 이 보고서, `SOURCE_MANIFEST.json`, `RUNBOOK.md` (모두 본 research 폴더). 커밋 SHA와 push 결과는 최종 Orca worker_done에 기록; 이 보고서의 커밋은 `git log -1 -- docs/workers/evaluation/research`로 재현한다.
