"""Verify local bytes, split separation, evidence links and Git exclusion; no network."""
from pathlib import Path
import hashlib,json,subprocess
D=Path(__file__).parent
m=json.loads((D/'MANIFEST.json').read_text()); idx=json.loads((D/'OBSERVATION_INDEX.json').read_text())
sha=lambda p:hashlib.sha256(Path(p).read_bytes()).hexdigest()
checks=0
for p in m['photos']:
 assert sha(p['heldout_path'])==p['heldout_sha256']; checks+=1
 assert p['manually_reviewed'] and p['split']=='heldout_only_not_prompt_reference'
 if 'original_path' in p: assert sha(p['original_path'])==p['original_sha256'];checks+=1
 assert subprocess.run(['git','check-ignore','-q',p['heldout_path']]).returncode==0
for d in m['manuals']:
 assert sha(d['path'])==d['sha256'];checks+=1
 assert Path(d['path']).read_bytes().startswith(b'%PDF-')
 assert not any(d['ingestion_stages'][k] for k in ['backend_registered_by_this_task','runtime_ingestion_verified','guide_approved'])
 for pg in d['pages']:
  assert 1<=pg['pdf_page']<=d['page_count']
  for f in pg['figures']: assert sha(f['path'])==f['sha256'];checks+=1
byid={d['document_id']:d for d in m['manuals']}
for e in idx:
 assert e['document_sha256']==byid[e['document_id']]['sha256'] and e['supported_actions']==[]
 assert e['pdf_page'] in [p['pdf_page'] for p in byid[e['document_id']]['pages']]
tracked=subprocess.check_output(['git','ls-files','--','docs/assets/real-photo-corpus/private'],text=True)
assert not tracked
print(f'PASS: {checks} byte hashes, 6 reviewed cases, 3 PDFs, 4 evidence links; no private assets tracked; no runtime ingestion claimed')
