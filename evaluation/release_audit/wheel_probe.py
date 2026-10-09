"""Check the built distribution outside source-tree imports; no network calls."""
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import zipfile

wheel=Path(sys.argv[1]).resolve()
with tempfile.TemporaryDirectory(prefix='howlens-wheel-audit-') as directory:
    with zipfile.ZipFile(wheel) as archive:
        names=archive.namelist()
        archive.extractall(directory)
    code='''import sys,socket,json
sys.path.insert(0,sys.argv[1])
original=socket.socket.connect
def blocked(self,address):
    if self.family in (socket.AF_INET,socket.AF_INET6):raise AssertionError('TCP blocked')
    return original(self,address)
socket.socket.connect=blocked
import howlens
from howlens.visual_boundary import public_module
result={'howlens_path':howlens.__file__}
try:
    result['visual_module']=public_module('service').__name__
except ModuleNotFoundError as error:
    result['missing_module']=error.name
print(json.dumps(result))
raise SystemExit(1 if 'missing_module' in result else 0)
'''
    env={'PATH':os.defpath,'HOME':directory,'PYTHONDONTWRITEBYTECODE':'1'}
    run=subprocess.run([sys.executable,'-I','-c',code,directory],cwd=directory,env=env,capture_output=True,text=True)
    print(json.dumps({'wheel':wheel.name,'manual_catalog_packaged':'howlens/manual_sources.json' in names,
      'visual_members':[n for n in names if n.startswith(('visual/','backend/visual/'))],
      'probe_stdout':run.stdout.strip(),'probe_stderr':run.stderr.strip(),'import_exit':run.returncode},indent=2))
    raise SystemExit(run.returncode)
