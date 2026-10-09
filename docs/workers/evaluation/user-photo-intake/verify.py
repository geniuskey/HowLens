"""Read-only hashes, privacy exclusions and test split verification."""
from pathlib import Path
import json,hashlib,subprocess
D=Path(__file__).parent;sha=lambda p:hashlib.sha256(Path(p).read_bytes()).hexdigest()
photos=json.loads((D/'INVENTORY.json').read_text());m=json.loads((D/'MANUALS.json').read_text());t=json.loads((D/'TEST_MATRIX.json').read_text())
coordinator=json.loads(Path('/Users/runixs/HowLens/docs/orchestrator/references/user-photo-intake-20261009.json').read_text());known={x['file']:x['sha256'] for x in coordinator['files']}
n=0
for p in photos:
 assert sha(p['path'])==p['sha256']==known[Path(p['path']).name];n+=1
 assert p['manually_reviewed'] and not p['model_test_executed'] and p['within_10MiB_20MP']
 assert subprocess.run(['git','check-ignore','-q',p['preview']]).returncode==0
for d in m['manuals']:
 assert sha(d['path'])==d['sha256'];n+=1
 assert not d['backend_registered'] and not d['guide_approved']
 for page in d.get('pages',[]):
  for im in page['embedded_figures']:assert sha(im['path'])==im['sha256'];n+=1
 if 'figure' in d:assert sha(d['figure']['path'])==d['figure']['sha256'];n+=1
for case in t['tests']:
 assert len(case['input_ids'])==1 and case['execution_status']=='not_run' and case['accuracy'] is None
 assert not case['other_images_allowed'] and not case['prior_label_context_allowed']
assert not set(t['whole_first_order']) & set(t['label_separate_sessions'])
assert not subprocess.check_output(['git','ls-files','--','docs/assets/user-real-photos/private'],text=True)
print(f'PASS {n} byte hashes; 9 originals unchanged and coordinator-matched; single-input split validated; no model execution claimed; private assets untracked')
