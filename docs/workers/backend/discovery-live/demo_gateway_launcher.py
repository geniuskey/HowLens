"""Explicit temporary demo-auth override; all original proxy bounds stay in force."""
import os
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[4] / 'backend'))
from dev.public_gateway import Gateway, create_gateway


class NoAuthDemoGateway(Gateway):
    async def __call__(self, scope, receive, send):
        if scope['type'] == 'http':
            scope = dict(scope)
            scope['headers'] = [(key, value) for key, value in scope.get('headers', [])
                                if key.lower() != b'authorization']
            # Internal nonsensitive sentinel, never forwarded by the original proxy.
            scope['headers'].append((b'authorization', b'Bearer ' + self.token))
        return await super().__call__(scope, receive, send)


if __name__ == '__main__':
    if os.environ.get('HOWLENS_DEMO_AUTH_DISABLED') == 'true':
        app = NoAuthDemoGateway(b'authorized-temporary-demo-bypass')
    else:
        app = create_gateway()
    import uvicorn
    uvicorn.run(app, host='127.0.0.1', port=18080, access_log=False, log_level='warning')
