"""W3 pinned-snapshot reproduction; synthetic providers and injected slow decode, not a live test.
Usage: isolated-python evaluation/w3_audit_probe.py SNAPSHOT_ROOT
No product source edits. Not a production or general regression test suite.
"""
import sys, socket, asyncio, threading, json
from pathlib import Path
snapshot = Path(sys.argv[1]).resolve()
sys.path.insert(0, str(snapshot/'backend'))
sys.path.insert(0, str(snapshot/'backend/tests'))
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from test_api import client, Provider, candidate, upload, photo
from evaluation.api_smoke import PNG
from evaluation import validator
from howlens import main
import httpx
from unittest.mock import patch
original_connect=socket.socket.connect
def no_network(self,address):
    if self.family in (socket.AF_INET,socket.AF_INET6): raise AssertionError('external network forbidden')
    return original_connect(self,address)
socket.socket.connect=no_network
results={}
c=client(Provider())
r=upload(c,data=PNG)
results['W2_synthetic_png']={'status':r.status_code,'body':r.json()}
if r.status_code==200: validator.analysis(r.json())
c=client(Provider(),trusted=True)
a=upload(c).json()
validator.analysis(a)
for i in range(3):
    r=c.post('/analyses/'+a['analysis_id']+'/visual')
    validator.visual(r.json(),a)
    results['visual_attempt_'+str(i+1)]={'status':r.status_code,'job':r.json()['job_id']}
r=c.post('/analyses/'+a['analysis_id']+'/verification',files={'photo':('TEST.png',photo(),'image/png')})
validator.verification(r.json(),a)
results['verification']='schema passed'
value=candidate();value['mode']='mock'
c=client(Provider(value),trusted=True);a=upload(c).json()
results['mock_gate']={'decision':a['decision'],'steps':a['steps'],'visual':c.post('/analyses/'+a['analysis_id']+'/visual').status_code,'verification':c.post('/analyses/'+a['analysis_id']+'/verification',files={'photo':('TEST.png',photo(),'image/png')}).status_code}
# Simulate a slow decoder behind real HTTP route; cancel requests, not the worker threads.
async def cancel_probe():
    release=threading.Event();lock=threading.Lock();state={'active':0,'peak':0}
    def slow_decode(data):
        with lock:
            state['active']+=1;state['peak']=max(state['peak'],state['active'])
        release.wait(3)
        with lock: state['active']-=1
        return 'PNG'
    async def wait_active(n, required=True):
        for _ in range(200):
            if state['active']>=n:return
            await asyncio.sleep(.005)
        if required: raise AssertionError('decoder did not start')
    tasks=[]
    try:
        with patch.object(main,'decode',slow_decode):
            async with httpx.AsyncClient(transport=httpx.ASGITransport(app=main.create_app()),base_url='http://offline') as c:
                def start():return asyncio.create_task(c.post('/analyses',data={'device_id':'server','question':'TEST'},files={'photo':('TEST.png',photo(),'image/png')}))
                tasks=[start(),start()];await wait_active(2)
                for task in tasks:task.cancel()
                await asyncio.gather(*tasks,return_exceptions=True)
                tasks=[start(),start()];await wait_active(4, required=False)
                results['decode_cancel_peak']=state['peak']
                release.set();await asyncio.gather(*tasks)
    finally:
        release.set()
        for task in tasks:
            if not task.done():task.cancel()
asyncio.run(cancel_probe())
print(json.dumps(results,indent=2))

raise SystemExit(1 if results["decode_cancel_peak"] > 2 else 0)
