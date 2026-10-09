# 로컬 재현 및 백엔드 인계

저장소 루트에서 실행한다. Python/PyMuPDF/Pillow는 기존 로컬 venv를 사용했으며 설치/유료 호출은 없었다.

```sh
docs/assets/manual-asset-design/.venv/bin/python docs/workers/evaluation/research/real-photo-corpus/prepare.py
open docs/assets/real-photo-corpus/INDEX.html
```

전제: MANIFEST의 original_path와 세 PDF가 private에 존재하고 Dell prework 원본이 명시 경로에 있어야 한다. 스크립트는 원본을 변경하지 않고 파생본과 JSON만 재생성한다. PDF 렌더 버전은 manifest에 기록한다. 원본 재다운로드 시 바이트 해시가 다르면 덮어쓰지 말고 새 source revision으로 격리한다.

새 PC에서 파일 준비는 manifest의 asset_url/source_url을 각 path로 다운로드하는 방식이다. 예:

```sh
mkdir -p docs/assets/real-photo-corpus/private
curl --fail --location --max-time 90 'https://www.tp-link.com/us/document/79681/' --output docs/assets/real-photo-corpus/private/ax55-v1-current-support.pdf
shasum -a 256 docs/assets/real-photo-corpus/private/ax55-v1-current-support.pdf
```

현재 기대 해시는 REPORT 표 참조. 해시 불일치/HTML 반환이면 준비를 중단하고 출처·판본을 재확인한다. 다운로드는 로컬 보존만 승인된 것이며 퍼블릭 Git 또는 외부 모델 업로드 허락이 아니다.

검증:

```sh
python3 docs/workers/evaluation/research/real-photo-corpus/verify.py
git check-ignore docs/assets/real-photo-corpus/private/heldout/t01.jpg
git diff --cached --name-only
```

preview는 상대 경로 이미지라 저장소+private 로컬 파일이 있어야 보인다. GitHub에서 이미지가 안 보이는 것이 의도된 동작이다. t05 원본 전체 라벨은 preview에 표시하지 않는다.

백엔드 전달 최소 단위: MANIFEST.json + OBSERVATION_INDEX.json + 허용된 비공개 reference 파일 + 검증 로그. heldout 사진/정답은 백엔드 검색 인덱스에 복사하지 않는다. API 필드/endpoint/실제 서비스 접근은 소유자가 현행 계약을 확인한 뒤 수행하며 본 runbook은 자동 등록·POST를 실행하지 않는다.
