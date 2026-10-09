# 인계와 재검증

제품 코드가 아닌 read-only 연구 스크립트다. 원본 루트는 `inspect_assets.py`의 ROOT로 고정되어 있다. 다른 PC에서는 Coordinator가 제공하는 실제 경로로 바꾼 뒤 실행한다. 원본 경로가 없을 때 임의 파일을 대신 쓰지 않는다.

```sh
python3 -m venv docs/assets/manual-asset-design/.venv
docs/assets/manual-asset-design/.venv/bin/pip install pymupdf==1.28.2 pillow
docs/assets/manual-asset-design/.venv/bin/python docs/workers/evaluation/research/manual-assets/inspect_assets.py
python3 docs/workers/evaluation/research/manual-assets/enrich_provenance.py
```

재현 환경 버전은 INVENTORY.renderer_version 및 로컬 venv 패키지 버전을 확인해 고정한다. 스크립트는 PDF/이미지를 읽어 SHA와 치수를 수집하고, selected source를 private/originals에 바이트 복사하며, 일부 PDF 페이지를 render하고 Figure212를 추출한다. 생성 모델·network/API·원본 수정은 없다. requirements 설치만 네트워크를 사용한다.

- `INVENTORY.json`: 109개 선택 파일의 hash/path/치수,6PDF 및 실제 figure 위치.
- `MANUAL_PROVENANCE.json`:6문서의 URL/버전/페이지 identity. 출처 항목은 reviewed catalog로 자동 로드하지 않는다.
- `SPECIMEN.md`:원본과 제거 그림의 관계; 업무 승인 아님.
- `docs/assets/manual-asset-design/SPECIMEN.html`:로컬 전용 source viewer; private 이미지 파일이 있어야 표시됨.
- `ARCHITECTURE.md`:Backend/Visual/App별 최소 구현 카드와 실패/rollback 기준.

연구 검증 명령은 제품 acceptance tests가 아니다. App/Backend/Visual 담당자는 각자 제품 코드에서 negative case와 기존 계약 회귀를 실행해야 한다. proof: source bytes 일치, figure ID와 정확한 action mapping, 실장비 identity/precondition provenance, no-guide 접근 차단, text fallback, 권리 검토. private 원본/전체 매뉴얼을 git add -f 하거나 artifact로 publish하지 않는다.
