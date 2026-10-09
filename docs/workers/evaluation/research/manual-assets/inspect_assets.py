"""Read-only prework inventory; private archive never committed. No network/model calls."""
from pathlib import Path
import hashlib,json,shutil
import pymupdf as fitz
from PIL import Image
ROOT=Path('/Users/runixs/working/ai/dsdn-hub/HowLens-prework')
OUT=Path('docs/assets/manual-asset-design/private'); OUT.mkdir(parents=True,exist_ok=True)
sha=lambda b:hashlib.sha256(b).hexdigest()
files=[]
for group in ['02_manuals','05_visuals/scenes','05_visuals/storyboards','05_visuals/infographics','experiments/image_prompting','experiments/panel_split','06_dayof/repo_seed/docs/prompts']:
 for p in sorted((ROOT/'prework_assets'/group).rglob('*')):
  if not p.is_file() or p.suffix.lower() not in {'.pdf','.png','.jpg','.md','.py'}: continue
  rel=p.relative_to(ROOT); data=p.read_bytes(); h=sha(data)
  item={'path':str(p),'relative_path':str(rel),'bytes':len(data),'sha256':h}
  dest=OUT/'originals'/h/p.name; dest.parent.mkdir(parents=True,exist_ok=True); shutil.copyfile(p,dest)
  assert sha(dest.read_bytes())==h
  item['archive_path']=str(dest)
  if p.suffix=='.pdf':
   dest=OUT/'originals'/h/p.name; dest.parent.mkdir(parents=True,exist_ok=True); shutil.copyfile(p,dest)
   assert sha(dest.read_bytes())==h
   doc=fitz.open(p); item.update(pages=len(doc),metadata=doc.metadata,archive_path=str(dest),rights='restricted; no public redistribution',review_status='inventory_only')
  elif p.suffix.lower() in {'.png','.jpg'}:
   with Image.open(p) as im: item['dimensions']=list(im.size)
   item['evidence_class']='synthetic_or_derived_unreviewed' if '/05_visuals/' in str(p) or '/experiments/' in str(p) else 'reference_photo_not_field_identity'
  files.append(item)
manuals=[i for i in files if i['path'].endswith('.pdf')]
# Minimal rendered inspection set; private only.
for item in manuals:
 doc=fitz.open(item['path']); name=Path(item['path']).stem
 pages=[1,len(doc)]
 if '/server/' in item['path']: pages += [11,211,248]
 if '/cobot/' in item['path']: pages += [194,244]
 for n in sorted(set(pages)):
  page=doc[n-1]; page.get_pixmap(matrix=fitz.Matrix(1,1)).save(OUT/f'{name}-p{n}.png')
  (OUT/f'{name}-p{n}.txt').write_text(page.get_text())
# Source-backed specimen: existing embedded Dell Figure 212, identify placement, not an operational approval.
dell=next(x for x in manuals if '/server/' in x['path']); doc=fitz.open(dell['path']); page=doc[210]
existing=ROOT/'prework_assets/experiments/image_prompting/source/psu_figure212_original.jpg'
figs=[]
for im in page.get_images(full=True):
 xref=im[0]; data=doc.extract_image(xref)
 if (data['width'],data['height'])==(1650,960):
  raw=data['image']; dest=OUT/'figure212-original.jpg'; dest.write_bytes(raw)
  figs.append({'figure_id':'dell-a11-figure212','manual_sha256':dell['sha256'],'pdf_page':211,'printed_page':'211','figure_number':212,'xref':xref,'page_size_points':list(page.rect),'coordinate_system':'PyMuPDF unrotated page points, top-left origin; x0,y0,x1,y1','page_rotation':page.rotation,'page_rects_points':[list(r) for r in page.get_image_rects(xref)],'pixel_crop_xyxy':[0,0,1650,960],'dimensions':[1650,960],'sha256':sha(raw),'private_path':str(dest),'prework_extracted_path':str(existing),'prework_extracted_sha256':sha(existing.read_bytes()),'byte_matches_prework_extract':raw==existing.read_bytes(),'rights':'restricted; private inspection only','review_status':'source_identity_verified; procedure_not_approved'})
manifest={'checked_date_kst':'2026-10-09','source_root':str(ROOT),'local_git_head':'86e87345a11c3386ea7a8fe7d26b1001c63dd48e','note':'working files independently hashed; git HEAD does not prove trackedness','files':files,'figures':figs,'renderer_version':fitz.VersionBind}
Path('docs/workers/evaluation/research/manual-assets/INVENTORY.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
print('manuals',len(manuals),'files',len(files),'figures',figs)
for i in manuals: print(Path(i['path']).name,i['pages'],i['sha256'])
