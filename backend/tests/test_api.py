"""Synthetic offline fixtures; no real manual or provider approval."""
import asyncio
from copy import deepcopy
from io import BytesIO
import pytest
from PIL import Image
from fastapi.testclient import TestClient
from howlens.main import create_app
from howlens.models import Analysis, Verification
from howlens.safety import ManualRegistry, Approval


def photo(size=(2, 2)):
    buf = BytesIO()
    Image.new('RGB', size).save(buf, format='PNG')
    return buf.getvalue()


def candidate():
    return dict(analysis_id='provider-id', device_id='server', decision='guide', observations=[],
                evidence=[dict(evidence_id='e1', document_id='synthetic', document_version='test',
                               pdf_page=1, printed_page=None, section='test', quote='Inspect indicator.',
                               source_url='https://example.org/synthetic')],
                preconditions=[dict(precondition_id='p1', description='No hazard', status='satisfied',
                                    required=True, evidence_ids=['e1'])],
                steps=[dict(step_id='s1', description='Inspect indicator.', evidence_ids=['e1'], visual_hint='indicator')],
                warnings=[], missing_information=[], mode='live')


class Provider:
    def __init__(self, value=None, error=None, delay=0):
        self.value = value or candidate()
        self.error, self.delay = error, delay
    async def analyze(self, device_id, question, photo):
        await asyncio.sleep(self.delay)
        if self.error:
            raise self.error
        return Analysis.model_validate(deepcopy(self.value))
    async def verify(self, analysis, original_photo, photo, confirmation):
        return Verification(analysis_id=analysis.analysis_id, result='observed_change', observations=['Indicator differs'],
                            evidence_ids=['e1'], missing_information=[], limitations=[], mode='live')


def registry():
    r = ManualRegistry()
    r.register('server', candidate()['evidence'][0], {'Inspect indicator.'})
    return r


async def approve(analysis, photo, question):
    return Approval(device_confirmed=True, sufficient_evidence=True, hazard_free=True,
                    confirmed_preconditions={'p1'}, approved_steps={'s1'},
                    provenance='synthetic independent reviewer; tests only')


def client(provider=None, trusted=False, **kwargs):
    return TestClient(create_app(provider=provider, registry=registry() if trusted else None,
                                reviewer=approve if trusted else None, **kwargs))


def upload(c, **kwargs):
    return c.post('/analyses', data={'device_id':'server', 'question':' Inspect indicator '},
                  files={'photo':('private.png', kwargs.get('data', photo()), kwargs.get('mime','image/png'))})


def test_default_health_and_unconfigured():
    c = client()
    assert c.get('/health').json() == {'status':'ok', 'mode':'live'}
    assert upload(c).status_code == 503


@pytest.mark.parametrize('data,mime,status', [(b'not png','image/png',415), (photo(),'image/jpeg',415),
                                              (b'x'*(10*1024*1024+1),'image/png',413),
                                              (photo((5000,4001)),'image/png',413)])
def test_invalid_upload(data,mime,status):
    assert upload(client(), data=data, mime=mime).status_code == status


@pytest.mark.parametrize('fields', [{'device_id':'bad','question':'ok'}, {'device_id':'server','question':'   '},
                                    {'device_id':'server','question':'x'*2001}])
def test_invalid_fields(fields):
    assert client().post('/analyses',data=fields,files={'photo':('p.png',photo(),'image/png')}).status_code == 422


def test_untrusted_guide_is_sanitized_and_blocked():
    c = client(Provider())
    a = upload(c).json()
    assert a['decision'] == 'needs_more_information' and a['steps'] == []
    assert a['evidence'] == []
    assert c.post(f"/analyses/{a['analysis_id']}/visual").status_code == 409
    assert c.post(f"/analyses/{a['analysis_id']}/verification", files={'photo':('p.png',photo(),'image/png')}).status_code == 409


@pytest.mark.parametrize('mutation', ['warning','unknown','quote','step','mode','duplicate','non-guide'])
def test_unsafe_output_cannot_expose_steps(mutation):
    a = candidate()
    if mutation == 'warning': a['warnings']=['Hazard identified']
    if mutation == 'unknown': a['preconditions'][0]['status']='unknown'
    if mutation == 'quote': a['evidence'][0]['quote']='Fabricated quote'
    if mutation == 'step': a['steps'][0]['description']='Touch wiring'
    if mutation == 'mode': a['mode']='mock'
    if mutation == 'duplicate': a['evidence'].append(deepcopy(a['evidence'][0]))
    if mutation == 'non-guide': a['decision']='stop'
    result = upload(client(Provider(a), trusted=True)).json()
    assert result['decision'] != 'guide' and result['steps'] == []


def test_unknown_ids():
    c=client()
    assert c.post('/analyses/missing/visual').status_code == 404
    assert c.get('/visual-jobs/missing').status_code == 404
    assert c.post('/analyses/missing/verification', files={'photo':('p.png',photo(),'image/png')}).status_code == 404


@pytest.mark.parametrize('provider,status', [(Provider(error=RuntimeError('secret upstream detail')),503),
                                            (Provider(delay=.1),504)])
def test_provider_failure(provider,status):
    r=upload(client(provider,timeout_seconds=.01))
    assert r.status_code == status and r.json()['detail']['retryable']
    assert 'secret' not in r.text


def test_approved_text_survives_missing_visual_and_retry_limit():
    c=client(Provider(),trusted=True)
    a=upload(c).json()
    assert a['decision']=='guide'
    path=f"/analyses/{a['analysis_id']}/visual"
    first=c.post(path)
    assert first.status_code==202 and first.json()['status']=='failed'
    assert first.json()['image_url'] is None and first.json()['panels']==[]
    second=c.post(path).json()
    assert second['job_id'] != first.json()['job_id']
    assert c.post(path).json()['job_id']==second['job_id']
    assert c.get('/visual-jobs/'+first.json()['job_id']).json()['status']=='failed'
    v=c.post(f"/analyses/{a['analysis_id']}/verification",files={'photo':('p.png',photo(),'image/png')})
    assert v.status_code==200 and v.json()['limitations']
    assert c.app.state.store.analyses[a['analysis_id']].analysis.steps
    assert c.app.state.store.analyses[a['analysis_id']].photo==photo()


def test_bounded_store_evicts_analysis_and_related_jobs():
    c=client(Provider(),trusted=True,max_analyses=1)
    a=upload(c).json()['analysis_id']
    job=c.post(f'/analyses/{a}/visual').json()['job_id']
    upload(c)
    assert c.post(f'/analyses/{a}/visual').status_code==404
    assert c.get(f'/visual-jobs/{job}').status_code==404


@pytest.mark.parametrize('mutation', ['string', 'array', 'aggregate', 'malformed'])
def test_provider_output_bounds(mutation):
    a = candidate()
    if mutation == 'string': a['observations'] = ['x' * 4001]
    if mutation == 'array': a['observations'] = ['x'] * 129
    if mutation == 'aggregate': a['observations'] = ['한' * 4000] * 12
    if mutation == 'malformed': del a['decision']
    c = client(Provider(a))
    r = upload(c)
    assert r.status_code == 503
    assert not c.app.state.store.analyses


@pytest.mark.parametrize('mutation,status', [('error',503), ('timeout',504), ('foreign',503),
                                             ('identity',503), ('mode',503), ('oversized',503)])
def test_verification_provider_failures(mutation, status):
    class BadVerification(Provider):
        async def verify(self, analysis, original_photo, after, confirmation):
            if mutation == 'error': raise RuntimeError('private upstream failure')
            if mutation == 'timeout': await asyncio.sleep(.1)
            result = (await super().verify(analysis, original_photo, after, confirmation)).model_dump()
            if mutation == 'foreign': result['evidence_ids'] = ['foreign']
            if mutation == 'identity': result['analysis_id'] = 'foreign'
            if mutation == 'mode': result['mode'] = 'mock'
            if mutation == 'oversized': result['observations'] = ['x' * 4001]
            return result
    c = client(BadVerification(), trusted=True, timeout_seconds=.01)
    a = upload(c).json()['analysis_id']
    r = c.post(f'/analyses/{a}/verification', files={'photo':('p.png',photo(),'image/png')})
    assert r.status_code == status and r.json()['detail']['retryable']
    assert 'private upstream' not in r.text
    assert c.app.state.store.analyses[a].analysis.decision == 'guide'


def test_verification_confirmation_bounds():
    c = client(Provider(), trusted=True)
    a = upload(c).json()['analysis_id']
    assert c.post(f'/analyses/{a}/verification', data={'user_confirmation':'x'*2001},
                  files={'photo':('p.png',photo(),'image/png')}).status_code == 422


def test_streaming_request_overflow():
    from howlens.main import MAX_REQUEST
    r = client().post('/analyses', content=iter([b'x'*1024] * (MAX_REQUEST//1024+1)),
                      headers={'Content-Type':'multipart/form-data; boundary=test'})
    assert r.status_code == 413 and r.json()['detail']['code'] == 'request_too_large'


def test_upload_receive_timeout(monkeypatch):
    import howlens.main as main
    real_wait = asyncio.wait_for
    async def immediate_timeout(awaitable, timeout):
        return await real_wait(awaitable, timeout=.001)
    monkeypatch.setattr(main.asyncio, 'wait_for', immediate_timeout)
    async def run():
        messages = []
        async def receive():
            await asyncio.sleep(.05)
            return {'type':'http.request', 'body':b'', 'more_body':False}
        async def send(message): messages.append(message)
        await create_app()({'type':'http', 'asgi':{'version':'3.0'}, 'method':'POST',
                            'path':'/analyses', 'raw_path':b'/analyses', 'query_string':b'',
                            'headers':[(b'content-type',b'multipart/form-data; boundary=test')],
                            'scheme':'http', 'server':('test',80), 'client':('test',1)}, receive, send)
        assert messages[0]['status'] == 504
    asyncio.run(run())


def test_question_limit_applies_after_trimming():
    c = client()
    r = c.post('/analyses', data={'device_id':'server', 'question':' '*2001+'x'+' '*2001},
               files={'photo':('p.png',photo(),'image/png')})
    assert r.status_code == 503 and r.json()['detail']['code'] == 'provider_unconfigured'


def test_max_file_is_accepted_and_store_accounts_for_output():
    from howlens.main import MAX_FILE
    data = photo() + b'\0' * (MAX_FILE-len(photo()))
    assert upload(client(), data=data).json()['detail']['code'] == 'provider_unconfigured'
    c = client(Provider(), trusted=True, max_bytes=MAX_FILE)
    assert upload(c, data=data).status_code == 503
    assert not c.app.state.store.analyses


def test_mutated_provider_dto_is_revalidated():
    class Mutated(Provider):
        async def analyze(self, *args):
            a = Analysis.model_validate(candidate())
            a.observations = ['x' * 4001]
            return a
    assert upload(client(Mutated())).status_code == 503


def test_required_unsatisfied_and_unreviewed_step_are_blocked():
    a = candidate()
    a['preconditions'][0]['status'] = 'unsatisfied'
    assert upload(client(Provider(a), trusted=True)).json()['steps'] == []
    a = candidate()
    a['steps'][0]['step_id'] = 'unreviewed'
    assert upload(client(Provider(a), trusted=True)).json()['steps'] == []


def test_cancelled_requests_do_not_release_running_decode_capacity(monkeypatch):
    import threading
    import howlens.main as main
    release=threading.Event()
    lock=threading.Lock()
    active=0
    peak=0
    def slow_decode(data):
        nonlocal active,peak
        with lock:
            active+=1
            peak=max(peak,active)
        try:
            assert release.wait(3)
            return 'PNG'
        finally:
            with lock: active-=1
    monkeypatch.setattr(main,'decode',slow_decode)
    class Upload:
        content_type='image/png'
        async def read(self,n): return photo()
        async def close(self): pass
    async def run():
        gate=main.DecodePool() if hasattr(main,'DecodePool') else asyncio.Semaphore(2)
        try:
            first=[asyncio.create_task(main.read_photo(Upload(),gate)) for _ in range(2)]
            for _ in range(100):
                with lock: started=active==2
                if started: break
                await asyncio.sleep(.005)
            assert started
            for task in first: task.cancel()
            await asyncio.gather(*first,return_exceptions=True)
            second=[asyncio.create_task(main.read_photo(Upload(),gate)) for _ in range(2)]
            await asyncio.sleep(.05)
            for task in second: task.cancel()
            await asyncio.gather(*second,return_exceptions=True)
            assert peak<=2
        finally:
            release.set()
            if hasattr(gate,'close'): await asyncio.to_thread(gate.close)
    asyncio.run(run())
