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
