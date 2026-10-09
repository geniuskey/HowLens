"""Only the real public startup/routing boundary is exercised; provider is synthetic."""
import asyncio
import copy
from importlib import import_module
from io import BytesIO
import json
import os
from pathlib import Path
import socket
import sys
from unittest.mock import patch

snapshot=Path(sys.argv[1]).resolve()
launch=sys.argv[2]
sys.path.insert(0,str(snapshot if launch=='root' else snapshot/'backend'))
os.chdir(snapshot if launch=='root' else snapshot/'backend')
original=socket.socket.connect

def deny_tcp(self,address):
    if self.family in (socket.AF_INET,socket.AF_INET6):
        raise AssertionError('Release audit prohibits TCP calls')
    return original(self,address)
socket.socket.connect=deny_tcp
prefix='backend.howlens' if launch=='root' else 'howlens'
boundary=import_module(prefix+'.visual_boundary')
models=import_module(prefix+'.models')
safety=import_module(prefix+'.safety')
store=import_module(prefix+'.store')
config=import_module(prefix+'.config')
from fastapi.testclient import TestClient
from PIL import Image

with TestClient(config.configured_app()) as client:
    assert client.get('/health').json()=={'status':'ok','mode':'live'}
assert config.readiness()['visual_integrated'] is False
service=boundary.public_module('service')
adapter=boundary.public_module('openai_provider')

class Fake:
    def __init__(self):self.prompts=[]
    async def generate_png(self,*,prompt):
        self.prompts.append(prompt)
        image=Image.new('RGB',(96,96))
        for i in range(9):image.paste((i*20,0,0),(i%3*32,i//3*32,i%3*32+32,i//3*32+32))
        output=BytesIO();image.save(output,format='PNG');image.close()
        return output.getvalue()

fake=Fake()
try:
    boundary.configure_same_process_visual()
    raise AssertionError('startup must require authorization')
except RuntimeError:pass
with patch.object(adapter.OpenAIStoryboardProvider,'from_env',return_value=fake) as factory:
    boundary.configure_same_process_visual(integration_authorized=True)
    factory.assert_called_once_with()

value={'analysis_id':'TEST-integration','device_id':'server','decision':'guide','mode':'live',
 'observations':[],'evidence':[{'evidence_id':'TEST-e','document_id':'TEST-doc','document_version':'TEST-v1','pdf_page':1,'printed_page':None,'section':'TEST','quote':'TEST ONLY fictional observation','source_url':'https://example.invalid/TEST'}],
 'preconditions':[],'steps':[{'step_id':f'TEST-s{i}','description':'TEST ONLY observe fictional object','evidence_ids':['TEST-e'],'visual_hint':''} for i in range(3)],'warnings':[],'missing_information':[]}
a=models.Analysis.model_validate(value)
approval=safety.Approval(device_confirmed=True,sufficient_evidence=True,hazard_free=True,
 approved_steps={s.step_id for s in a.steps},provenance='SYNTHETIC TEST ONLY')
saved=store.StoredAnalysis(a,b'TEST-only',approval)
before=copy.deepcopy(value)
reviews=[]
async def review(analysis,panels,ids):
    reviews.append(ids)
    assert len(panels)==9
    for i,panel in enumerate(panels):
        with Image.open(BytesIO(panel)) as im:assert im.getpixel((0,0))==(i*20,0,0)
    return True  # Test acceptance only, never real semantic approval.

async def run():
    with patch.object(service,'scene_step_ids',wraps=service.scene_step_ids) as mapping:
        result=await boundary.generate_reviewed_assets(saved,calls_authorized=True,quality_review=review)
        mapping.assert_called_once()
    expected=tuple(f'TEST-s{i//3}' for i in range(9))
    assert result.step_ids==expected==reviews[0]
    prompt_ids=tuple(x['step_id'] for x in json.loads(fake.prompts[0].split('\n',1)[1])['scenes'])
    assert prompt_ids==expected
    for decision,mode in [('stop','live'),('needs_more_information','live'),('guide','mock')]:
        changed=a.model_copy(deep=True);changed.decision=decision;changed.mode=mode
        try:
            await boundary.generate_reviewed_assets(store.StoredAnalysis(changed,b'TEST',approval),calls_authorized=True,quality_review=review)
            raise AssertionError('unsafe route accepted')
        except ValueError:pass
    assert len(fake.prompts)==1
    async def reject(*args):return False
    try:
        await boundary.generate_reviewed_assets(saved,calls_authorized=True,quality_review=reject)
        raise AssertionError('quality rejection ignored')
    except ValueError:pass
    assert saved.analysis.model_dump()==before
    print(json.dumps({'launch':launch,'service_module':service.__name__,'startup':'passed_fake_factory',
      'public_mapper_and_prompt_ids':list(result.step_ids),'panels':len(result.panels),
      'guards':'passed_zero_additional_provider_calls','rejected_visual_text_preserved':True,
      'provider_calls':len(fake.prompts),'real_calls':0,'semantic_review':'synthetic_only'},indent=2))
try:asyncio.run(run())
finally:service.configure_provider(None)
