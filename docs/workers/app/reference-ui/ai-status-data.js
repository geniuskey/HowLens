// Public operational inventory only. No credentials, user photos or raw prompts.
window.HOWLENS_AI_STATUS = {
  "updated": "2026-10-09 14:30 KST",
  "evidenceTime": "2026-10-09 14:28:42 KST",
  "runtime": {
    "model": "gpt-6.1-sol / gpt-6-luna",
    "source": "dd7e4ae",
    "requests": 22,
    "limit": 40,
    "searches": 1,
    "lastSeconds": 21.064,
    "lastOutcome": "분석 HTTP200 · CUCKOO AC-35U20FWS 관찰 · 장비 불일치로 guide 차단",
    "evidence": "docs/orchestrator/receipts/routed-server-670dd10.json; 서버 stage 로그 확인",
    "inputTokens": 2419,
    "outputTokens": 782,
    "reportedEstimate": null,
    "actualBill": null
  },
  "rollout": "최신 모델 서버·R3 앱 3대 배포 완료 · 실사진 분석 확인 · 일부 모델의 정확한 출처 매칭 미완료",
  "deployment": {
    "appSource": "9a2bfa9",
    "apkSha256": "7edb4e6cc669ac1121a0d2a654ebe735699f393d5d952fd21dada3f8b9ba5f3b",
    "apkBytes": 13193414,
    "install": "R3 장비 선택 Home 동일 APK 3대 설치·실행 확인",
    "gateway": "데모 토큰 인증 해제 완료 · 공개 API 빈 요청 422 확인",
    "modelAvailability": "Sol/Luna 실제 반환 모델 확인 · Astra 조건부 경로는 호출 전"
  },
  "features": [
    {
      "id": "identify",
      "title": "제품·모델 식별",
      "owner": "서버",
      "model": "gpt-6-luna",
      "kind": "운영 설정 확인",
      "state": "실제 응답 확인",
      "input": "장비 사진 → 제조사·전체 모델명·라벨 관찰 JSON",
      "method": "Responses API의 이미지 입력. 명확한 라벨을 읽고 제조사·모델 문자열을 교차 확인합니다.",
      "limit": "Luna 실제 분류 응답 5.419초/7.358초 확인. 테더링 DNS 복구 및 런타임 분류 제한 20초 적용.",
      "target": "GPT-6 Luna → 필요 시 GPT-6.1 Sol",
      "reason": "빠른 단서 추출은 Luna, 후보가 충돌하거나 라벨이 흐리면 Sol로 재관찰. 추정 후보를 확정 모델로 승격하지 않습니다.",
      "file": "backend/howlens/discovery.py",
      "symbol": "DiscoveryProvider.discover / LabelObservation"
    },
    {
      "id": "analysis",
      "title": "사진·증상 분석",
      "owner": "서버",
      "model": "gpt-6.1-sol",
      "kind": "운영 설정 확인",
      "state": "실제 응답 확인",
      "input": "사진 + 사용자 설명 + 등록 문서 발췌 → Analysis",
      "method": "서버 역할 라우팅: Sol medium 초기 분석, 필요 시 Luna 분류와 Sol 검색, 검증된 일치 모델만 Astra 재분석.",
      "limit": "실제 사진 분석 성공. 선택 장비와 사진 장비가 다르면 가이드를 승인하지 않습니다.",
      "target": "GPT-6.1 Sol",
      "reason": "일반 사진 분석과 여러 단서 결합의 기본 모델. 서버가 난도와 남은 응답 시간을 보고 추론량을 조절합니다.",
      "file": "backend/howlens/openai_provider.py",
      "symbol": "OpenAIResponsesProvider.analyze / _request"
    },
    {
      "id": "research",
      "title": "제품·공식 문서 검색",
      "owner": "서버",
      "model": "gpt-6.1-sol",
      "kind": "운영 호출 확인",
      "state": "정확 모델 근거 미완료",
      "input": "인식한 제조사·모델 → 실제 검색 출처 + 제품 설명",
      "method": "라벨 판독 뒤 검색을 1회 요청합니다. 검색 출처와 제조사·전체 모델명 일치를 검증하며 임의 URL은 채택하지 않습니다.",
      "limit": "완료 검색 + 진행 중 항목 호환성 수정 배포. 실제 응답 14.984초, 출처 URL13개가 검증에 들어왔으나 CUCKOO 정확 모델 근거는 source_identity로 미승인.",
      "target": "GPT-6.1 Sol + web_search",
      "reason": "검색어 재구성 → 제조사 문서 탐색 → 후보 대조를 서버에서 수행. 결과가 충돌할 때만 Astra로 올리는 정책을 요청했습니다.",
      "file": "backend/howlens/discovery.py",
      "symbol": "SearchResult / actual_sources / supports_identity"
    },
    {
      "id": "retrieval",
      "title": "매뉴얼 근거 연결",
      "owner": "서버",
      "model": "모델 없음 · 등록 문서 조회",
      "kind": "코드 확인",
      "state": "제한 있음",
      "input": "장비 ID → 문서 버전·페이지·발췌·지원 작업",
      "method": "등록된 매뉴얼 레지스트리를 조회합니다. 현재 코드에 별도 임베딩 모델이나 벡터 검색은 없습니다.",
      "limit": "웹에서 찾은 새 문서가 곧바로 등록·검증된 작업 근거가 되지는 않습니다. 문서 내용과 페이지를 검증하는 연결이 필요합니다.",
      "target": "문서 조회 도구 + GPT-6.1 Sol",
      "reason": "문서 수집·페이지 추출은 도구가 처리하고, 모델은 증상과 발췌의 관련성을 판단합니다.",
      "file": "backend/howlens/manual_catalog.py",
      "symbol": "load_registry / ManualEntry"
    },
    {
      "id": "guide",
      "title": "단계별 가이드·검증",
      "owner": "서버",
      "model": "gpt-6.1-sol",
      "kind": "코드·운영 설정 확인",
      "state": "검증 조건 있음",
      "input": "문서 근거 + 조건 확인 → guide / 확인 항목 / stop",
      "method": "모델 출력 뒤 서버가 근거 ID, 문서 버전·페이지 및 필수 조건을 검사합니다. 앱은 통과한 단계만 표시합니다.",
      "limit": "물리적 안전 조건을 확인하는 독립 채널은 현재 운영 설정에서 미연결입니다. 검색 성공이나 고성능 모델만으로 작업 승인을 만들지 않습니다.",
      "target": "GPT-6.1 Sol · 복잡한 충돌은 GPT-6 Astra",
      "reason": "일반 근거 대조는 Sol, 여러 매뉴얼의 충돌·복잡한 추론만 Astra로 배정. 최종 조건 검증은 서버 규칙으로 유지합니다.",
      "file": "backend/howlens/safety.py",
      "symbol": "sanitize / Approval / conservative_review"
    },
    {
      "id": "visual",
      "title": "단계별 안내 이미지",
      "owner": "서버",
      "model": "gpt-image-1.5",
      "kind": "코드 기본값 · 운영 미확인",
      "state": "미검증",
      "input": "서버 승인 단계 → 3×3 설명용 이미지 → 패널 9개",
      "method": "HOWLENS_IMAGE_MODEL 미설정 시 gpt-image-1.5. low · 1024×1024 · timeout 120s가 코드 기본값입니다.",
      "limit": "운영 가이드 이미지의 실제 성공은 입증되지 않았습니다. 별도 이미지 벤치마크는 아래에 구분했습니다.",
      "target": "최신 이미지 전용 모델 · 품질/지연별 선택",
      "reason": "텍스트 추론 모델과 분리. 같은 장면의 low/medium 결과를 검토한 뒤 운영 모델과 품질을 고정합니다.",
      "file": "backend/visual/openai_provider.py",
      "symbol": "OpenAIStoryboardProvider.from_env"
    },
    {
      "id": "verify",
      "title": "전후 사진 비교",
      "owner": "서버",
      "model": "gpt-6.1-sol",
      "kind": "운영 설정·코드 확인",
      "state": "실기능 증거 미확인",
      "input": "승인된 원 분석 + 전 사진 + 후 사진 → 관찰 변화",
      "method": "같은 Responses 모델에 두 이미지를 보내 observed_change / issue_remaining / inconclusive를 구조화합니다.",
      "limit": "이 현황판의 운영 증거에는 전후 비교 성공 호출이 포함되지 않았습니다. 사진 비교는 정상 동작이나 안전을 보증하지 않습니다.",
      "target": "GPT-6.1 Sol",
      "reason": "두 사진을 비교하고 기존 근거와 연결. 가려짐·상충 관찰이 큰 경우 서버 정책에 따라 고난도 검토로 올립니다.",
      "file": "backend/howlens/openai_provider.py",
      "symbol": "OpenAIResponsesProvider.verify"
    },
    {
      "id": "help",
      "title": "AI 도움 화면",
      "owner": "앱",
      "model": "AI 호출 없음 · 로컬 도움말",
      "kind": "앱 코드 확인",
      "state": "로컬 기능",
      "input": "추천 질문 / 입력 문장 → 정해진 도움말",
      "method": "앱의 localHelp 함수가 답하며 질문을 사진 분석으로 넘길 수 있습니다. 실시간 모델 채팅은 아닙니다.",
      "limit": "백엔드 채팅 엔드포인트는 현재 확인된 계약에 없습니다.",
      "target": "서버 질문 처리 · GPT-6 Luna / GPT-6.1 Sol",
      "reason": "단순 사용법은 빠른 경로, 사진·문서 맥락이 필요한 질문은 서버 분석 경로에 연결합니다.",
      "file": "android/app/src/main/java/kr/howlens/app/ui/ReferenceDestinations.kt",
      "symbol": "localHelp / onUseQuestion"
    }
  ],
  "benchmark": {
    "requestedModel": "gpt-image-2.5-sunburst-2026-09-08",
    "cases": 6,
    "quality": "low / medium",
    "http": "6건 HTTP 200",
    "latency": "12.990–19.526초",
    "returnedModel": "응답 모델 필드 null",
    "review": "이미지 품질 사람 검토 전",
    "billing": "실제 청구 금액 미확인",
    "provenance": "Backend PC visual-benchmark · Coordinator 전달"
  },
  "routing": [
    {
      "name": "빠른 관찰·분류",
      "model": "GPT-6 Luna",
      "effort": "low",
      "trigger": "명확한 라벨, 짧은 추출, 사용법",
      "next": "후보가 둘 이상이거나 단서가 충돌하면 Sol"
    },
    {
      "name": "일반 분석·검색",
      "model": "GPT-6.1 Sol",
      "effort": "medium",
      "trigger": "사진 + 증상 + 제조사 문서를 결합",
      "next": "여러 근거의 충돌·복잡한 추론은 Astra"
    },
    {
      "name": "고난도 재검토",
      "model": "GPT-6 Astra",
      "effort": "고난도 경로 · 호출 한도 적용",
      "trigger": "Sol의 재탐색 후에도 해결되지 않은 핵심 쟁점",
      "next": "새 검색 단서·검증 결과를 활용해 답변 완성"
    },
    {
      "name": "설명용 이미지",
      "model": "이미지 전용 모델",
      "effort": "품질 / 해상도로 조절",
      "trigger": "근거가 검증된 작업 단계의 시각 자료 요청",
      "next": "이미지 실패 시 검증된 텍스트 안내 유지"
    }
  ]
};
