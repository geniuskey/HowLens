# 0001 — 착수 구조와 역할별 문서

날짜: 2026-10-09. 상태: 사용자 지시 반영(역할 재배분·docs 기준·Coordinator Medium). 세부 계약은 착수 v0.1.

## 결정

Android+FastAPI 구조를 유지하고 docs를 common / orchestrator / workers/<role>로 나눈다.
공통 자료는 RULES와 CONTRACT만 최소로 공유하고 Worker마다 START, TASK, RUNBOOK, REPORT를 둔다.
Coordinator는 인간 리뷰를 취합하고 외부 근거를 먼저 조사한다. 기존 Kotlin core 카드·BuildConfig API 키·무조건 9단계는 적용하지 않는다.
역할: 김의윤 App/Hub, 이준영 Backend/AI/Safety, 이윤재 Visual, 김태완 Evaluation.

## 외부 선례와 적용

- [FastAPI Forms and Files](https://fastapi.tiangolo.com/tutorial/request-forms-and-files/): 사진과 질문을 File/Form으로 받는 multipart 패턴과 python-multipart 의존성을 확인. JSON body와 파일을 같은 요청에서 혼용하지 않는 계약으로 적용.
- [Android architecture recommendations](https://developer.android.com/topic/architecture/recommendations): UI/data layer와 repository, ViewModel/state 흐름의 공식 권장. 소규모 MVP에 필요한 경계만 적용하며 과도한 모듈을 먼저 만들지 않는다.
- [GitHub code owners](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners): 경로별 리뷰 담당의 공식 패턴. TEAM에 소유 범위를 기록한다. CODEOWNERS와 브랜치 보호가 설정되었다고 주장하지 않는다. GitHub 사용자가 모두 확인되고 팀이 필요로 하면 후속으로 설정한다.
- Orca 1.4.223의 `orca skills get orchestration`: Run/Task/Dispatch, bounded summary, accepted worker_done 후 cleanup 패턴을 확인. 연결 loss를 종료로 취급하지 않는다.
- [OpenAI pricing](https://learn.chatgpt.com/docs/pricing)와 [GPT-6.1 Sol](https://developers.openai.com/api/docs/models/gpt-6.1-sol): Coordinator 요청을 Sol/Medium/Standard로 기록. 실제 launch/현재 세션 설정은 따로 확인한다.

위 출처는 2026-10-09 실제 열람한 공식 문서다. 외부에서 지원되는 방식이며 우리 제품에서의 E2E 검증은 아직 미실행이다.
문서 읽기 경로는 프롬프트 규칙이며 파일 ACL이 아니다. 소유권 위반은 변경 파일 검사로 확인한다.

## 검증과 후속

문서 링크·역할/계약 일관성을 검사한 뒤 공유한다. 초기 4개 Worker는 독립 골격과 테스트를 구현한다.
완료 브랜치의 실제 diff/테스트를 취합해 사람 리뷰 자료를 만든다. 실제 매뉴얼·키·실기기 연결은 후속 준비 확인이 필요하다.
