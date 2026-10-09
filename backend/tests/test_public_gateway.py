"""Offline fake upstream. Never reads the deployed token or performs paid calls."""
import asyncio
import httpx
from fastapi.testclient import TestClient
from dev.public_gateway import Gateway, MAX_BODY, demo_token


def test_health_public_and_paid_routes_require_auth():
    calls = []
    def upstream(request):
        calls.append(request)
        assert request.url.host == '127.0.0.1' and request.url.port == 8000
        assert 'authorization' not in request.headers
        return httpx.Response(200, json={'status': 'ok', 'mode': 'live'})
    with TestClient(Gateway(b'synthetic-token', transport=httpx.MockTransport(upstream))) as client:
        assert client.get('/health').status_code == 200
        assert client.post('/analyses', content=b'private').status_code == 401
        assert client.get('/visual-jobs/id').status_code == 401
        assert client.get('/visual-assets/id.png').status_code == 401
        assert len(calls) == 1


def test_fixed_routes_status_body_and_size_preserved():
    calls = []
    def upstream(request):
        calls.append(request)
        return httpx.Response(422, json={'detail': {'code': 'field', 'message': 'invalid', 'retryable': False}})
    with TestClient(Gateway(b'synthetic-token', transport=httpx.MockTransport(upstream))) as client:
        auth = {'Authorization': 'Bearer synthetic-token'}
        response = client.post('/analyses', headers=auth, content=b'form')
        assert response.status_code == 422 and response.json()['detail']['code'] == 'field'
        assert calls[0].content == b'form'
        assert client.post('/analyses', headers=auth, content=b'x'*(MAX_BODY+1)).status_code == 413
        for path in ['/docs', '/openapi.json', '/admin', '/http://other.com', '/analyses/id/../../admin', '/health?url=http://other.com']:
            assert client.get(path, headers=auth).status_code == 404
        assert len(calls) == 1


def test_token_file_private_never_changes_on_read(tmp_path):
    path = tmp_path / 'token'
    token = demo_token(path)
    assert path.stat().st_mode & 0o777 == 0o600
    assert demo_token(path) == token


def test_duplicate_or_wrong_auth_and_encoded_paths_never_forward():
    gateway = Gateway(b'synthetic-token', transport=httpx.MockTransport(
                      lambda _: (_ for _ in ()).throw(AssertionError('Unexpected upstream'))))
    with TestClient(gateway) as client:
        assert client.post('/analyses', headers={'Authorization': 'Bearer wrong'}).status_code == 401
        assert client.post('/analyses', headers=[('Authorization','Bearer synthetic-token'),
                                                ('Authorization','Bearer synthetic-token')]).status_code == 401
        assert client.get('/visual-jobs/%61', headers={'Authorization':'Bearer synthetic-token'}).status_code == 404


def test_disconnect_cancels_transport_and_releases_slot():
    async def run():
        started, cancelled = asyncio.Event(), asyncio.Event()
        async def upstream(request):
            started.set()
            try:
                await asyncio.Event().wait()
            finally:
                cancelled.set()
        gateway = Gateway(b'synthetic-token', transport=httpx.MockTransport(upstream))
        scope = {'type':'http','method':'POST','path':'/analyses','raw_path':b'/analyses',
                 'query_string':b'','headers':[(b'authorization',b'Bearer synthetic-token')]}
        uploaded = False
        async def receive():
            nonlocal uploaded
            if not uploaded:
                uploaded = True
                return {'type':'http.request','body':b'form','more_body':False}
            await started.wait()
            return {'type':'http.disconnect'}
        async def send(message):
            pass
        await asyncio.wait_for(gateway(scope,receive,send),2)
        assert cancelled.is_set() and gateway.slots._value == 4
    asyncio.run(run())
