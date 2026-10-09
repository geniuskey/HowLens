"""Offline corpus preparation. Original photos remain byte-exact; crops explicitly derived."""
from pathlib import Path
import hashlib,json,shutil
from PIL import Image
import pymupdf as fitz
A=Path('docs/assets/real-photo-corpus'); D=Path('docs/workers/evaluation/research/real-photo-corpus'); P=A/'private'
for n in ['heldout','reference','quarantine']: (P/n).mkdir(exist_ok=True)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
photos=[
('t01','r750-front.jpg','https://www.storagereview.com/wp-content/uploads/2021/03/dell-emc-r750-label.jpg','https://www.storagereview.com/review/dell-emc-poweredge-r750-hands-on','StorageReview / Brian Beeler','R750 label readable; exact hardware revision not visible; review describes preproduction unit','Dell PowerEdge R750',None,'storagereview-r750-unit'),
('t02','r750-rear.jpg','https://www.storagereview.com/wp-content/uploads/2021/03/r750-back.jpg','https://www.storagereview.com/review/dell-emc-poweredge-r750-hands-on','StorageReview / Brian Beeler','Rear ports visible; model and hardware revision not independently legible in this view','candidate Dell PowerEdge R750',None,'storagereview-r750-unit'),
('t03','ax55-top-tg.jpg','https://cdn.mos.cms.futurecdn.net/4d5NcMD9FPKkk7emM335nB.jpg','https://www.tomsguide.com/computing/routers/tp-link-archer-ax55-review',"Tom’s Guide (photo credit); Brian Nadel review",'TP-Link logo and four antennas; no model/revision label; ask for existing label image or typed model/version','candidate TP-Link Archer AX55',None,'tomsguide-ax55-unit'),
('t04','ax55-rear-tg.jpg','https://cdn.mos.cms.futurecdn.net/a6WNGFsNKTUC9sGbgivybB.jpg','https://www.tomsguide.com/computing/routers/tp-link-archer-ax55-review',"Tom’s Guide (photo credit); Brian Nadel review",'WAN and LAN1-LAN4 labels visible; hardware revision/region unknown; do not infer v1','candidate TP-Link Archer AX55',None,'tomsguide-ax55-unit'),
('t05','ax55-label-rtings.jpg','https://www.rtings.com/assets/products/zmZPFAUB/tp-link-archer-ax55/label-small.jpg?format=auto','https://www.rtings.com/router/reviews/tp-link/archer-ax55','RTINGS.com','Archer AX55(CA) Ver:1.0 directly legible; CANADA, not confirmed US; other unit photos cannot inherit identity','TP-Link Archer AX55(CA)','1.0','rtings-ax55-unit')]
items=[]
for ident,name,url,page,credit,expected,model,rev,group in photos:
 src=P/name
 with Image.open(src) as im:
  dims=list(im.size)
  if ident=='t05':
   # Keep only model/revision row; omit QR, password, serial and MAC.
   rect=[186,71,372,85]; im.crop(rect).save(P/'heldout/t05.png'); dst=P/'heldout/t05.png'; transform={'operation':'crop','xyxy':rect,'resize':False}
  else:
   dst=P/'heldout'/f'{ident}.jpg';shutil.copyfile(src,dst);transform=None
 items.append(dict(photo_id=ident,original_path=str(src),original_sha256=sha(src),bytes=src.stat().st_size,dimensions=dims,asset_url=url,source_page=page,credit=credit,source_type='firsthand reviewer product photograph',synthetic=False,classification_basis='review attribution + visual inspection of photographed physical object',rights='copyright retained; no open redistribution permission established; private local evaluation only; no external upload authorized by this manifest',heldout_path=str(dst),heldout_sha256=sha(dst),transform=transform,expected_visible_model=model,hardware_revision_from_photo=rev,manual_applicability='pending region/revision/configuration check',expected_behavior=expected,split='heldout_only_not_prompt_reference',correlation_group=group,manually_reviewed=True))
src=P/'r750-rear.jpg';dst=P/'heldout/t06.png';rect=[290,285,540,375]
with Image.open(src) as im: im.crop(rect).save(dst)
items.append(dict(photo_id='t06',original_photo_id='t02',original_sha256=sha(src),heldout_path=str(dst),heldout_sha256=sha(dst),transform={'operation':'crop','xyxy':rect,'resize':False},synthetic=False,source_type='derived crop of real photograph, not independent sample',rights=items[1]['rights'],expected_visible_model=None,hardware_revision_from_photo=None,expected_behavior='Partial rear connectors only; ask for existing uncropped/model-label image or typed model, no exact R750 claim',split='heldout_only_not_prompt_reference',correlation_group='storagereview-r750-unit',manually_reviewed=True))
# Reuse independently hash-verified manufacturer original, not a screenshot.
src=Path('/Users/runixs/working/ai/dsdn-hub/HowLens-prework/prework_assets/02_manuals/server/poweredge-r750_Owners-Manual_en-us.pdf'); dst=P/'reference/dell-r750-a11.pdf';shutil.copyfile(src,dst)
manual_defs=[('dell-r750-a11',dst,'Dell','PowerEdge R750','December 2024 Rev A11',None,'https://dl.dell.com/topicspdf/poweredge-r750_Owners-Manual_en-us.pdf',None,[(13,'13','Rear view of the system; Figure 8',[("VGA port",'vga')])]),('tplink-ax55-us-v1-current',P/'ax55-v1-current-support.pdf','TP-Link','Archer AX55','1910013020 REV1.0.0','US V1 support linkage; device match still required','https://static.tp-link.com/upload/manual/2022/202203/20220321/1910013020_Archer%C2%A0AX55_UG_REV1.0.0.pdf','https://www.tp-link.com/us/document/79681/',[(9,'5','1.2.2 Back Panel',[("Back Panel",'rear')]),(10,'6','Button and Port Explanation',[("WAN Port",'wan'),('LAN Port (1-4)','lan')])]),('tplink-ax55-seed-202112',P/'ax55-v1.pdf','TP-Link','Archer AX55','1910013020 REV1.0.0',None,'https://static.tp-link.com/upload/manual/2021/202112/20211230/1910013020_Archer%20AX55_UG_REV1.0.0.pdf',None,[])]
manuals=[];index=[]
for ident,path,mfr,model,ver,region,url,support,pages in manual_defs:
 assert path.read_bytes().startswith(b'%PDF-');doc=fitz.open(path);h=sha(path); ev=[]
 for number,printed,section,quotes in pages:
  page=doc[number-1];text=page.get_text();(P/'reference'/f'{ident}-p{number}.txt').write_text(text)
  page.get_pixmap(matrix=fitz.Matrix(1.5,1.5)).save(P/'reference'/f'{ident}-p{number}.png')
  figures=[]
  for info in page.get_images(full=True):
   xref=info[0];fig=doc.extract_image(xref);fpath=P/'reference'/f'{ident}-p{number}-xref{xref}.{fig["ext"]}';fpath.write_bytes(fig['image'])
   figures.append(dict(xref=xref,path=str(fpath),sha256=sha(fpath),dimensions=[fig['width'],fig['height']],bbox_points=[list(r) for r in page.get_image_rects(xref)],coordinate_system='unrotated PyMuPDF top-left x0,y0,x1,y1 points',source_type='manufacturer diagram/render, NOT real photo'))
  entry=dict(document_id=ident,document_sha256=h,pdf_page=number,printed_page=printed,section=section,page_rotation=page.rotation,page_size_points=list(page.rect),figures=figures)
  for quote,key in quotes:
   assert quote in text
   e=dict(evidence_id=ident+'-'+key,document_id=ident,document_version=ver,pdf_page=number,printed_page=printed,section=section,quote=quote,source_url=url,document_sha256=h,supported_actions=[],review_status='observation-only candidate; not runtime approved')
   index.append(e)
  ev.append(entry)
 manuals.append(dict(document_id=ident,path=str(path),bytes=path.stat().st_size,sha256=h,pdf_signature=True,page_count=len(doc),manufacturer=mfr,model=model,document_version=ver,hardware_region_scope=region,source_url=url,support_document_url=support,support_page='https://www.tp-link.com/us/support/download/archer-ax55/v1/' if mfr=='TP-Link' else None,rights='manufacturer copyright; no full redistribution',pages=ev,ingestion_stages={'downloaded_or_archived':True,'pdf_parsed':True,'selected_pages_extracted':bool(pages),'local_index_ready':bool(pages),'backend_registered_by_this_task':False,'runtime_ingestion_verified':False,'guide_approved':False}))
manifest=dict(schema_version=1,checked_date='2026-10-09',scope='5 downloaded real photographs + 1 dependent crop; no runtime/paid calls',renderer_version=fitz.VersionBind,photos=items,manuals=manuals,split_policy='heldout photographs and all their crops never enter reference index or prompts; review groups are correlated, not 6 independent devices')
(D/'MANIFEST.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n');(D/'OBSERVATION_INDEX.json').write_text(json.dumps(index,ensure_ascii=False,indent=2)+'\n')
print('prepared',len(items),'cases',len(manuals),'manual artifacts',len(index),'short evidence candidates')
