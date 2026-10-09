"""Offline endpoint checks; no paid calls or physical device assertions."""
from io import BytesIO
from PIL import Image
from fastapi.testclient import TestClient
from howlens.main import create_app
from howlens.discovery import DiscoveryService
from howlens.discovery_models import ProductDiscovery
import asyncio
import pytest


def photo():
    output = BytesIO()
    Image.new('RGB', (8, 8)).save(output, format='PNG')
    return output.getvalue()


def test_discovery_unconfigured_has_explicit_service_error():
    with TestClient(create_app()) as client:
        response = client.post('/product-discoveries',
                               files={'photo': ('label.png', photo(), 'image/png')})
    assert response.status_code == 503
    assert response.json()['detail']['code'] == 'discovery_unconfigured'


class FakeDiscovery:
    """Explicit offline test double; production never instantiates this."""
    def __init__(self):
        self.calls = []

    async def discover(self, image, question, model_hint):
        self.calls.append((image, question, model_hint))
        return ProductDiscovery(discovery_id='synthetic-test-only', status='needs_more_information',
                 candidates=[], missing_information=['Synthetic fixture: supply complete label.'], mode='mock')


def test_optional_fields_trimmed_mock_honest_and_analysis_store_untouched():
    provider = FakeDiscovery()
    app = create_app(discovery_service=DiscoveryService(provider))
    with TestClient(app) as client:
        response = client.post('/product-discoveries', data={'question': '  identify  ', 'model_hint': '  R750  '},
                               files={'photo': ('label.png', photo(), 'image/png')})
        assert response.status_code == 200 and response.json()['mode'] == 'mock'
        assert set(response.json()) == {'discovery_id', 'status', 'candidates', 'missing_information', 'mode', 'research'}
        assert response.json()['research'] is None
        assert provider.calls[0][1:] == ('identify', 'R750')
        assert app.state.store.analyses == {}
        existing = client.post('/analyses', data={'device_id': 'invented', 'question': 'identify'},
                               files={'photo': ('label.png', photo(), 'image/png')})
        assert existing.status_code == 422


@pytest.mark.parametrize('data,filename,content,mime,expected', [
    ({'question': 'x'*2001}, 'label.png', photo(), 'image/png', 422),
    ({'model_hint': 'x'*201}, 'label.png', photo(), 'image/png', 422),
    ({}, 'label.gif', photo(), 'image/gif', 415),
    ({}, 'label.png', b'not an image', 'image/png', 415),
    ({}, 'label.jpg', photo(), 'image/jpeg', 415),
    ({}, 'label.png', b'x'*(10*1024*1024+1), 'image/png', 413),
])
def test_discovery_upload_and_field_limits_use_existing_errors(data, filename, content, mime, expected):
    provider = FakeDiscovery()
    with TestClient(create_app(discovery_service=DiscoveryService(provider))) as client:
        response = client.post('/product-discoveries', data=data, files={'photo': (filename, content, mime)})
    assert response.status_code == expected and not provider.calls
    assert set(response.json()['detail']) == {'code', 'message', 'retryable'}


def test_discovery_twenty_megapixel_limit():
    output = BytesIO()
    Image.new('L', (5000, 4001)).save(output, format='PNG')
    with TestClient(create_app()) as client:
        response = client.post('/product-discoveries', files={'photo': ('big.png', output.getvalue(), 'image/png')})
    assert response.status_code == 413 and response.json()['detail']['code'] == 'too_many_pixels'


def test_optional_boundaries_and_missing_photo_validation():
    provider = FakeDiscovery()
    with TestClient(create_app(discovery_service=DiscoveryService(provider))) as client:
        response = client.post('/product-discoveries', data={'question': 'x'*2000, 'model_hint': 'x'*200},
                               files={'photo': ('label.png', photo(), 'image/png')})
        assert response.status_code == 200
        assert client.post('/product-discoveries', data={}).status_code == 422


@pytest.mark.parametrize('failure,code,status', [
    (TimeoutError(), 'discovery_timeout', 504),
    (RuntimeError('Sensitive upstream content must never appear'), 'discovery_provider_error', 503),
])
def test_upstream_errors_redacted_and_standard(failure, code, status):
    class Failing:
        async def discover(self, *args):
            raise failure
    with TestClient(create_app(discovery_service=DiscoveryService(Failing()))) as client:
        response = client.post('/product-discoveries', files={'photo': ('label.png', photo(), 'image/png')})
    assert response.status_code == status and response.json()['detail']['code'] == code
    assert 'Sensitive' not in response.text and response.json()['detail']['retryable'] is True


def test_asgi_client_disconnect_cancels_discovery_http_task():
    async def run():
        started, cancelled = asyncio.Event(), asyncio.Event()
        class Slow:
            async def discover(self, *args):
                started.set()
                try:
                    await asyncio.Event().wait()
                finally:
                    cancelled.set()
        app = create_app(discovery_service=DiscoveryService(Slow()))
        boundary = 'offline-test'
        body = (f'--{boundary}\r\nContent-Disposition: form-data; name="photo"; filename="label.png"\r\n'
                'Content-Type: image/png\r\n\r\n').encode() + photo() + f'\r\n--{boundary}--\r\n'.encode()
        scope = {'type': 'http', 'asgi': {'version': '3.0'}, 'http_version': '1.1', 'method': 'POST',
                 'scheme': 'http', 'path': '/product-discoveries', 'raw_path': b'/product-discoveries',
                 'query_string': b'', 'root_path': '', 'server': ('offline', 80), 'client': ('127.0.0.1', 1),
                 'headers': [(b'content-type', f'multipart/form-data; boundary={boundary}'.encode())]}
        uploaded = False
        async def receive():
            nonlocal uploaded
            if not uploaded:
                uploaded = True
                return {'type': 'http.request', 'body': body, 'more_body': False}
            await started.wait()
            return {'type': 'http.disconnect'}
        responses = []
        async def send(message):
            responses.append(message)
        async with app.router.lifespan_context(app):
            await asyncio.wait_for(app(scope, receive, send), 2)
            assert cancelled.is_set()
            assert responses[0]['status'] == 499
    asyncio.run(run())
