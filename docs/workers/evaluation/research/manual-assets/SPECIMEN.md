# 원본 연결 표본: Dell Figure 212

상태: **원본 동일성 검증 완료 / 작업 승인 없음 / 외부 공개용 이미지 아님**.

- 원본: `/Users/runixs/working/ai/dsdn-hub/HowLens-prework/prework_assets/02_manuals/server/poweredge-r750_Owners-Manual_en-us.pdf`
- 제조사 URL: https://dl.dell.com/topicspdf/poweredge-r750_Owners-Manual_en-us.pdf
- 동일 바이트를 이전 연구에서 제조사 다운로드로 검증한 URL: https://dl.dell.com/content/manual15837466-dell-emc-poweredge-r750-installation-and-service-manual.pdf
- 문서: Dell PowerEdge R750 Installation and Service Manual, December 2024 Rev A11, 255쪽.
- PDF SHA-256: `1bdf0df910860207f11a27d9fb7ce6effcb70c0a324ed06e3579d1da6fe6b879`.
- PDF211 / 인쇄211, caption: “Figure 212. Removing a power supply unit”. 이번 작업에서 페이지를 렌더하고 직접 보았다.
- 해당 그림은 제거 동작을 보인다. 같은 페이지 아래의 설치 텍스트와 결합해 설치 그림이라고 잘못 붙이면 안 된다. 절차 전제와 시작 단계는 앞 페이지 등을 함께 검토해야 한다.
- 원본 embedded JPEG: 1650×960, xref35712, SHA-256 `4ab2abc1205ad3f4e91c55b56e4bc810c404077cd20f1fd71383d8c23d21ceda`.
- 페이지 크기 points: `[0,0,595.275634765625,841.8897705078125]`, rotation0.
- 그림 배치 bbox points: `[99.63780975341797,66.09684753417969,495.6378173828125,296.4968566894531]`; PyMuPDF unrotated top-left 좌표.
- 원본 픽셀 crop: `[0,0,1650,960]`. embedded JPEG 전체를 추출했으며 재인코딩·왜곡·생성 없음.
- prework `experiments/image_prompting/source/psu_figure212_original.jpg`와 재추출 byte equality **True**.
- private 표본: `docs/assets/manual-asset-design/private/figure212-original.jpg`; local viewer는 `docs/assets/manual-asset-design/SPECIMEN.html`.

이 표본은 제조사 그림→페이지→hash→figure ID 연결을 입증한다. prework 인포그래픽에서 연결된 절차 문구가 모두 올바르다거나 PSU 교체가 현재 현장에서 승인되었다는 뜻은 아니다. 원본 공개/재배포 권리가 확인되지 않아 PDF와 그림은 git에 넣지 않았다. 권리 확인 전에는 이 local viewer를 외부 artifact로 publish하지 않는다.

오늘 최소 식별 시나리오에는 이 제거 그림을 사용하지 않는다. 별도로 Dell 전면 패널 p11/p245의 읽기 전용 식별 근거를 검토한다. 원본 보존성과 작업 적합성은 다른 검사다.
