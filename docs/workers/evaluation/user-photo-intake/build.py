"""Offline metadata/evidence preparation; originals are read only, private artifacts ignored."""
from pathlib import Path
import json,hashlib
import pymupdf as fitz
D=Path(__file__).parent;P=Path('docs/assets/user-real-photos/private')
sha=lambda p:hashlib.sha256(Path(p).read_bytes()).hexdigest()
rows=json.loads((D/'INVENTORY.json').read_text())
obs=[('label','Samsung','RF60A91C3AP','모델 및 615L 표기가 읽힘; Rev.05는 라벨 인쇄 표기이며 매뉴얼 판본/HW revision으로 전용하지 않음','refrigerator'),('label','SK magic','WPU-B600F','모델 표기가 읽힘; 인증 문자열 WPU-B610F와 모델명 필드를 혼동하지 않음','water-purifier'),('whole','Nespresso',None,'Nespresso 브랜드·복수 추출구·컵과 버튼이 보임; 정확 모델 라벨 없음','coffee'),('whole',None,None,'4도어 냉장고 외관; 전체 사진만으로 정확 모델 확정하지 않음','refrigerator'),('label','CUCKOO','AC-35U20FWS','모델 AC-35U20FWS 및 공기청정기 표기가 읽힘','air-cleaner'),('label-context','Samsung','RF60A91C3AP','넓은 라벨 문맥 사진; 전체 외관 시험 입력으로 쓰면 정답 누출','refrigerator'),('whole','CUCKOO',None,'CUCKOO 로고·원통형 공기청정기·청색 계열 조명; 공기질/필터 상태 확정 불가','air-cleaner'),('label','CUCKOO','AC-35U20FWS','모델명 읽힘; 전자파 등록 모델 AC-34U20을 제품명과 혼동하지 않음','air-cleaner'),('whole',None,None,'스탠드형 정수기·두 출수부·표시등; 사진만으로 온도/수질/정상 여부 확인 불가','water-purifier')]
for r,(kind,brand,model,note,group) in zip(rows,obs):
 r.update(input_kind=kind,visible_brand=brand,visible_exact_model=model,manual_observation=note,provisional_batch_group=group,pairing_proof='same-batch attribution only; not independently established',manually_reviewed=True,model_test_executed=False,rights='user-supplied; private evaluation only; no public redistribution',exif_policy='orientation only; no GPS/time/device identifiers copied',within_10MiB_20MP=r['bytes']<=10*1024*1024 and r['raw_dimensions'][0]*r['raw_dimensions'][1]<=20000000)
(D/'INVENTORY.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
urls=json.loads((P/'download-urls.json').read_text());manuals=[]
for name,version,pages,scope in [('samsung-kr','support version 4.0; 2023-03-20; filename KO_230310',[1,19,92],'Exact KR RF60A91C3AP support association; family cover RF60/85A*, RF84C*; options conditional'),('cuckoo-fwgh-family','10383-0016P0 Rev.1 (PDF36 visual); official listing 1777',[1,35,36],'REJECT for AC-35U20FWS: PDF identifies AC-35U20FWGH')]:
 path=P/(name+'.pdf');doc=fitz.open(path);e=[]
 for n in pages:
  pg=doc[n-1];pg.get_pixmap(matrix=fitz.Matrix(1,1)).save(P/f'{name}-p{n}.png')
  figs=[]
  for im in pg.get_images(full=True):
   xref=im[0];x=doc.extract_image(xref);fp=P/f'{name}-p{n}-xref{xref}.{x["ext"]}';fp.write_bytes(x['image']);figs.append(dict(path=str(fp),sha256=sha(fp),xref=xref,bbox_points=[list(b) for b in pg.get_image_rects(xref)]))
  e.append(dict(pdf_page=n,printed_page=str(n) if n in [19,35] else None,page_size_points=list(pg.rect),rotation=pg.rotation,embedded_figures=figs,coordinate_system='PyMuPDF top-left points; original PDF retains vector figures',reviewed=n in ([1,19,92] if name=='samsung-kr' else [1,35,36])))
 manuals.append(dict(document_id=name,path=str(path),sha256=sha(path),bytes=path.stat().st_size,pdf_signature=path.read_bytes().startswith(b'%PDF-'),page_count=len(doc),version=version,source_url=urls[name+'.pdf'],applicability=scope,pages=e,downloaded=True,backend_registered=False,guide_approved=False))
# HTML is HTML: no fabricated PDF page or document revision.
manuals.append(dict(document_id='sk-b600-html',path=str(P/'skmanual.html'),sha256=sha(P/'skmanual.html'),source_url='https://qr.skmagic.com/2019/model/WPU/WPUB610FREWH/Manual.htm',format='HTML',version=None,pdf_page=None,section='contents-4-1 / WPU-B600F',exact_model_evidence='Official data.js maps WPUB600FREWH; Manual.htm has separate WPU-B600F heading and img_04_01_02.png',figure=dict(path=str(P/'sk-b600-exterior.png'),sha256=sha(P/'sk-b600-exterior.png'),source_url=urls['sk-b600-exterior.png'],crop=None,original_preserved=True),backend_registered=False,guide_approved=False))
(D/'MANUALS.json').write_text(json.dumps(dict(checked_date='2026-10-09',renderer_version=fitz.VersionBind,manuals=manuals),ensure_ascii=False,indent=2)+'\n')
tests=[]
for r in rows:
 tests.append(dict(test_id=r['id']+'-independent',input_ids=[r['id']],prompt='사진에서 직접 확인되는 제품 종류와 브랜드 또는 모델명을 알려 주세요. 정확한 모델을 확인할 수 없으면 필요한 추가 정보를 알려 주세요. 조작 방법은 안내하지 마세요.',other_images_allowed=False,prior_label_context_allowed=False,expected_exact_model=r['visible_exact_model'],expected_behavior=r['manual_observation'],execution_status='not_run',accuracy=None))
(D/'TEST_MATRIX.json').write_text(json.dumps(dict(tests=tests,whole_first_order=['p03','p04','p07','p09'],label_separate_sessions=['p01','p02','p05','p06','p08'],prompt_leak_guard='No inventory, model-specific manual, answer labels, previous conversation or group mappings in whole-object identification inputs',pair_tests='Optional after independent scoring; pairings provisional, report separately, never independent sample count'),ensure_ascii=False,indent=2)+'\n')
print('Prepared 9 independent input cases, 2 PDFs (1 rejected variant), 1 HTML manual; no model execution')
