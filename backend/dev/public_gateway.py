"""Temporary loopback gateway. Fixed upstream, explicit routes, no sensitive logs."""
import asyncio
import json
import os
from pathlib import Path
import re
import secrets
import stat
import httpx

MAX_BODY = 10 * 1024 * 1024 + 64 * 1024
MAX_RESPONSE = 32 * 1024 * 1024
ID = r'[A-Za-z0-9_-]{1,128}'
ROUTES = {
    'GET': [r'/health', rf'/visual-jobs/{ID}', rf'/visual-assets/{ID}(?:/{ID})?(?:\.png)?'],
    'POST': [r'/analyses', r'/product-discoveries', rf'/analyses/{ID}/(?:visual|verification)'],
}


def demo_token(path):
    path = Path(path)
    try:
        fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    except FileExistsError:
        pass
    else:
        with os.fdopen(fd, 'w') as output:
            output.write(secrets.token_urlsafe(32) + '\n')
    fd = os.open(path, os.O_RDONLY | os.O_NOFOLLOW)
    with os.fdopen(fd, 'rb') as source:
        info = os.fstat(source.fileno())
        if not stat.S_ISREG(info.st_mode) or stat.S_IMODE(info.st_mode) != 0o600:
            raise RuntimeError('Demo token file requires mode 0600')
        token = source.read(257).strip()
    if not 40 <= len(token) <= 256 or not re.fullmatch(rb'[A-Za-z0-9_-]+', token):
        raise RuntimeError('Invalid demo token file')
    return token


class Gateway:
    def __init__(self, token, *, transport=None):
        self.token, self.transport = token, transport
        self.slots = asyncio.Semaphore(4)

    async def reply(self, send, status, code, message):
        body = json.dumps({'detail': {'code': code, 'message': message,
                                     'retryable': status >= 500}}).encode()
        await send({'type': 'http.response.start', 'status': status,
                    'headers': [(b'content-type', b'application/json'), (b'cache-control', b'no-store')]})
        await send({'type': 'http.response.body', 'body': body})

    async def __call__(self, scope, receive, send):
        if scope['type'] == 'lifespan':
            while True:
                message = await receive()
                if message['type'] == 'lifespan.startup':
                    await send({'type': 'lifespan.startup.complete'})
                elif message['type'] == 'lifespan.shutdown':
                    await send({'type': 'lifespan.shutdown.complete'})
                    return
        if scope['type'] != 'http':
            return
        path, method = scope['path'], scope['method']
        raw = scope.get('raw_path', path.encode())
        if (scope.get('query_string') or raw != path.encode() or '%' in path or '..' in path
                or not any(re.fullmatch(rule, path) for rule in ROUTES.get(method, []))):
            return await self.reply(send, 404, 'route_not_allowed', 'Route is not exposed.')
        public = method == 'GET' and path == '/health'
        auth = [value for key, value in scope.get('headers', []) if key.lower() == b'authorization']
        if not public and (len(auth) != 1 or len(auth[0]) > 512
                           or not secrets.compare_digest(auth[0], b'Bearer ' + self.token)):
            return await self.reply(send, 401, 'authentication_required', 'Demo bearer token is required.')
        acquired = False
        started = False
        tasks = []
        try:
            async with asyncio.timeout(0.05):
                await self.slots.acquire()
                acquired = True
        except TimeoutError:
            return await self.reply(send, 503, 'gateway_busy', 'Gateway is busy; retry later.')
        try:
            data = bytearray()
            async with asyncio.timeout(30):
                while True:
                    message = await receive()
                    if message['type'] == 'http.disconnect':
                        return
                    if message['type'] != 'http.request':
                        continue
                    if len(data) + len(message.get('body', b'')) > MAX_BODY:
                        return await self.reply(send, 413, 'request_too_large', 'Upload exceeds request limit.')
                    data.extend(message.get('body', b''))
                    if not message.get('more_body', False):
                        break
            headers = {key.decode('ascii'): value.decode('latin1') for key, value in scope.get('headers', [])
                       if key.lower() in {b'content-type', b'accept'}}

            async def forward():
                nonlocal started
                async with asyncio.timeout(65):
                    async with httpx.AsyncClient(timeout=65, trust_env=False, follow_redirects=False,
                                                  transport=self.transport) as client:
                        async with client.stream(method, 'http://127.0.0.1:8000' + path,
                                                 headers=headers, content=bytes(data)) as response:
                            response_headers = [(key.encode(), value.encode()) for key, value in response.headers.items()
                                                if key in {'content-type'}]
                            response_headers.append((b'cache-control', b'no-store'))
                            await send({'type': 'http.response.start', 'status': response.status_code,
                                        'headers': response_headers})
                            started = True
                            size = 0
                            async for chunk in response.aiter_bytes():
                                size += len(chunk)
                                if size > MAX_RESPONSE:
                                    raise RuntimeError('Response exceeds limit')
                                await send({'type': 'http.response.body', 'body': chunk, 'more_body': True})
                            await send({'type': 'http.response.body', 'body': b'', 'more_body': False})

            async def disconnected():
                while (await receive()).get('type') != 'http.disconnect':
                    pass

            tasks = [asyncio.create_task(forward()), asyncio.create_task(disconnected())]
            done, _ = await asyncio.wait(tasks, return_when=asyncio.FIRST_COMPLETED)
            if tasks[1] not in done:
                await tasks[0]
        except (TimeoutError, httpx.TimeoutException):
            if not started:
                await self.reply(send, 504, 'gateway_timeout', 'Upstream timed out.')
        except Exception:
            if not started:
                await self.reply(send, 503, 'gateway_upstream_error', 'Upstream is unavailable.')
        finally:
            for task in tasks:
                if not task.done():
                    task.cancel()
            await asyncio.gather(*tasks, return_exceptions=True)
            if acquired:
                self.slots.release()


def create_gateway():
    return Gateway(demo_token(Path(__file__).resolve().parents[1] / '.demo-token'))
