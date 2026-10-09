window.UR5E_DEMO = {
  "question": "어떻게 인스톨해?",
  "device": "UR5e",
  "reply": "알겠어요. 사용자님이 알려주신 **UR5e 기준**으로 설명할게요. 사진상 로봇과 그리퍼가 이미 장착되어 있으므로, 지금 필요한 것은 처음부터 조립하는 작업보다는 **설치 상태 확인과 운용 설정**으로 판단됩니다.\n\n설치 담당자가 베이스 고정·제어기 연결 상태를 확인하고, 장착된 그리퍼의 TCP(도구 기준점)·무게·무게중심 및 안전 설정을 검증하는 단계가 남았는지 확인하면 됩니다. 첨부 매뉴얼 그림은 부품 위치 설명이지 설치·분해 순서가 아니므로, 아래 그림도 위치와 확인 범위를 설명하며 로봇 이동이나 배선 작업은 안내하지 않습니다.",
  "sources": [
    {
      "title": "UR5e 공식 매뉴얼",
      "url": "https://s3-eu-west-1.amazonaws.com/ur-support-site/225353/710-965-00_UR5e_User_Manual_en_Global.pdf"
    }
  ],
  "steps": [
    {
      "step_id": "4158993c-50a0-4cde-bd2c-45f9bd9d336d",
      "index": 0,
      "title": "현재 구성",
      "description": "로봇과 그리퍼가 장착된 현재 모습이며 설치 완료 여부는 확인되지 않았습니다.",
      "image_url": "assets/ur5e-scene-1.png",
      "task_step_index": 0,
      "task_step_title": "현재 장착 상태 이해",
      "source_refs": [],
      "evidence_basis": "photo"
    },
    {
      "step_id": "51cfcb9d-7875-4d8d-841d-e9cd21c28203",
      "index": 1,
      "title": "베이스 위치",
      "description": "사진 아래쪽의 베이스와 받침대가 만나는 영역을 보여줍니다.",
      "image_url": "assets/ur5e-scene-2.png",
      "task_step_index": 0,
      "task_step_title": "현재 장착 상태 이해",
      "source_refs": [],
      "evidence_basis": "photo"
    },
    {
      "step_id": "6abfb198-e700-4e88-ada6-9e46ecf86f64",
      "index": 2,
      "title": "장착된 그리퍼",
      "description": "팔 끝의 그리퍼와 연결 케이블은 보이지만 그리퍼 모델은 확인되지 않았습니다.",
      "image_url": "assets/ur5e-scene-3.png",
      "task_step_index": 0,
      "task_step_title": "현재 장착 상태 이해",
      "source_refs": [],
      "evidence_basis": "photo"
    },
    {
      "step_id": "91060a7a-c22c-4d30-98d3-30d8f9616d1b",
      "index": 3,
      "title": "매뉴얼 구조 그림",
      "description": "매뉴얼 12쪽의 그림은 베이스·관절·도구 플랜지의 위치를 설명합니다.",
      "image_url": "assets/ur5e-scene-4.png",
      "task_step_index": 1,
      "task_step_title": "매뉴얼의 부품 위치 이해",
      "source_refs": [
        {
          "figure_id": "ur5e-p12-figure2-1-joints",
          "document_id": "ur5e-710-965-00-10.5.152",
          "pdf_page": 12,
          "printed_page": "12",
          "source_url": "https://s3-eu-west-1.amazonaws.com/ur-support-site/225353/710-965-00_UR5e_User_Manual_en_Global.pdf",
          "caption": "Figure 2.1. The joints, the base and the tool flange of the Robot Arm."
        }
      ],
      "evidence_basis": "manual"
    },
    {
      "step_id": "7e5d6d6b-953d-4866-8a02-cadcee10a05d",
      "index": 4,
      "title": "베이스와 어깨 영역",
      "description": "원본 그림의 왼쪽 아래에 있는 베이스·베이스 관절·어깨 관절을 구분합니다.",
      "image_url": "assets/ur5e-scene-5.png",
      "task_step_index": 1,
      "task_step_title": "매뉴얼의 부품 위치 이해",
      "source_refs": [
        {
          "figure_id": "ur5e-p12-figure2-1-joints",
          "document_id": "ur5e-710-965-00-10.5.152",
          "pdf_page": 12,
          "printed_page": "12",
          "source_url": "https://s3-eu-west-1.amazonaws.com/ur-support-site/225353/710-965-00_UR5e_User_Manual_en_Global.pdf",
          "caption": "Figure 2.1. The joints, the base and the tool flange of the Robot Arm."
        }
      ],
      "evidence_basis": "manual"
    },
    {
      "step_id": "65457e7e-2bbc-48a0-9b37-2f4a2be3825a",
      "index": 5,
      "title": "손목과 도구 플랜지",
      "description": "원본 그림 오른쪽 위의 손목 관절과 도구 플랜지는 도구 연결 위치를 설명합니다.",
      "image_url": "assets/ur5e-scene-6.png",
      "task_step_index": 1,
      "task_step_title": "매뉴얼의 부품 위치 이해",
      "source_refs": [
        {
          "figure_id": "ur5e-p12-figure2-1-joints",
          "document_id": "ur5e-710-965-00-10.5.152",
          "pdf_page": 12,
          "printed_page": "12",
          "source_url": "https://s3-eu-west-1.amazonaws.com/ur-support-site/225353/710-965-00_UR5e_User_Manual_en_Global.pdf",
          "caption": "Figure 2.1. The joints, the base and the tool flange of the Robot Arm."
        }
      ],
      "evidence_basis": "manual"
    },
    {
      "step_id": "4053c072-15d0-49fa-98d5-d33a140dfd4a",
      "index": 6,
      "title": "고정 상태 확인 범위",
      "description": "설치 담당자가 베이스와 받침대의 고정 적합성을 확인할 영역입니다.",
      "image_url": "assets/ur5e-scene-7.png",
      "task_step_index": 2,
      "task_step_title": "설치 담당자의 확인 범위",
      "source_refs": [],
      "evidence_basis": "general"
    },
    {
      "step_id": "7516a79e-38b7-4ecc-bfde-3c4b4d5026e5",
      "index": 7,
      "title": "도구 설정 확인 범위",
      "description": "장착된 도구의 TCP·무게·무게중심은 실제 도구 자료에 맞춰 확인해야 합니다.",
      "image_url": "assets/ur5e-scene-8.png",
      "task_step_index": 2,
      "task_step_title": "설치 담당자의 확인 범위",
      "source_refs": [],
      "evidence_basis": "general"
    },
    {
      "step_id": "f8f050b2-c251-466f-9a60-2e8abd4c8644",
      "index": 8,
      "title": "화면과 검증의 구분",
      "description": "켜진 화면은 관찰 사실이며 안전 설정과 운전 준비의 검증을 대신하지 않습니다.",
      "image_url": "assets/ur5e-scene-9.png",
      "task_step_index": 2,
      "task_step_title": "설치 담당자의 확인 범위",
      "source_refs": [],
      "evidence_basis": "general"
    }
  ],
  "guide_plan": {
    "task_title": "UR5e의 현재 구성과 설치 확인 범위 설명",
    "scope": "사용자 사진의 장착 상태와 매뉴얼 12쪽 Figure 2.1의 부품 위치 설명. 실제 설치·분해·배선·운전 절차는 포함하지 않음.",
    "section_completeness": "partial",
    "missing_references": [
      "기계 설치 절차 본문",
      "장착 그리퍼 자료",
      "현장 고정 및 안전 검증 기록"
    ],
    "sequence_basis": "explanatory"
  }
};
