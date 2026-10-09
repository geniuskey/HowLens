"""OFFLINE SYNTHETIC CLIENT DOUBLE. No models, equipment approval, or product imports."""
import argparse
import copy
from email.parser import BytesParser
from email.policy import default
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
from pathlib import Path
import struct
import zlib
from evaluation.api_smoke import LIMIT, PNG

FIXTURES = json.loads((Path(__file__).parent / 'fixtures/contract.json').read_text())
DIGITS = ['111101101101111', '010110010010111', '111001111100111', '111001111001111', '101101111001001', '111100111001111', '111100111101111', '111001001001001', '111101111101111']


def panel_png(index=None):
    size = 288 if index is None else 96
    rows = []
    for y in range(size):
        row = bytearray()
        for x in range(size):
            number = (y // 96) * 3 + x // 96 if index is None else index
            px, py = x % 96, y % 96
            gx, gy = (px - 30) // 12, (py - 18) // 12
            ink = 0 <= gx < 3 and 0 <= gy < 5 and DIGITS[number][gy * 3 + gx] == '1'
            row.extend((20, 20, 20) if ink else (230, 240 - number * 8, 250 - number * 12))
        rows.append(b'\0' + row)
    def chunk(kind, data):
        return struct.pack('!I', len(data)) + kind + data + struct.pack('!I', zlib.crc32(kind + data))
    return b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('!2I5B', size, size, 8, 2, 0, 0, 0)) + chunk(b'IDAT', zlib.compress(b''.join(rows))) + chunk(b'IEND', b'')


def make_server(host='127.0.0.1', port=8765, scenario='non-guide', visual='failed', verification='inconclusive'):
    analyses, jobs, attempts = {}, {}, {}
    class Handler(BaseHTTPRequestHandler):
        def log_message(self, *args):
            pass

        def send(self, status, obj):
            payload = json.dumps(obj).encode()
            self.send_response(status)
            self.send_header('Content-Type', 'application/json')
            self.send_header('X-HowLens-Test-Only', 'synthetic-mock')
            self.send_header('Content-Length', str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)

        def error(self, code):
            self.send(code, {'detail': {'code': f'TEST_{code}', 'message': 'SYNTHETIC test double response only', 'retryable': code >= 500}})

        def do_GET(self):
            if self.path == '/health':
                return self.send(200, {'status': 'TEST ONLY synthetic client double', 'mode': 'mock'})
            if self.path.startswith('/visual-jobs/'):
                job = jobs.get(self.path.rsplit('/', 1)[-1])
                return self.send(200, job) if job else self.error(404)
            assets = {'/visual-assets/TEST-grid.png': None, **{f'/visual-assets/TEST-panel-{i}.png': i for i in range(9)}}
            if self.path in assets:
                data = panel_png(assets[self.path])
                self.send_response(200)
                self.send_header('Content-Type', 'image/png')
                self.send_header('Content-Length', str(len(data)))
                self.end_headers()
                return self.wfile.write(data)
            self.error(404)

        def do_POST(self):
            length = int(self.headers.get('Content-Length', '0'))
            if length > LIMIT + 65536:
                self.close_connection = True
                return self.error(413)
            raw = self.rfile.read(length)
            parts = self.path.strip('/').split('/')
            if self.path == '/analyses' or (len(parts) == 3 and parts[2] == 'verification'):
                msg = BytesParser(policy=default).parsebytes(('Content-Type: ' + self.headers.get('Content-Type', '') + '\r\n\r\n').encode() + raw)
                form = {p.get_param('name', header='content-disposition'): p for p in msg.iter_parts()}
                photo = form.get('photo')
                if photo is None:
                    return self.error(422)
                payload = photo.get_payload(decode=True)
                if len(payload) > LIMIT:
                    return self.error(413)
                # Deliberately accepts only the exact known PNG, not a real image decoder.
                if photo.get_content_type() != 'image/png' or payload != PNG:
                    return self.error(415)
                if self.path == '/analyses':
                    device = form.get('device_id')
                    device = device.get_content() if device else ''
                    if device not in ('server', 'cobot', 'ups'):
                        return self.error(422)
                    obj = copy.deepcopy(FIXTURES['analyses'][0 if scenario == 'guide' else 2 if scenario == 'stop' else 1])
                    obj.update(analysis_id=f'TEST-analysis-{len(analyses)}', device_id=device)
                    analyses[obj['analysis_id']] = obj
                    return self.send(200, obj)
            if len(parts) != 3 or parts[0] != 'analyses' or parts[2] not in ('visual', 'verification'):
                return self.error(404)
            obj = analyses.get(parts[1])
            if obj is None:
                return self.error(404)
            if obj['decision'] != 'guide':
                return self.error(409)
            if parts[2] == 'verification':
                item = copy.deepcopy(next(x for x in FIXTURES['verifications'] if x['result'] == verification))
                item['analysis_id'] = obj['analysis_id']
                return self.send(200, item)
            count = attempts.get(parts[1], 0)
            existing = next((j for j in jobs.values() if j['analysis_id'] == parts[1] and j['status'] != 'failed'), None)
            if existing:
                return self.send(202, existing)
            if count >= 2:
                return self.error(409)
            attempts[parts[1]] = count + 1
            item = copy.deepcopy(next(x for x in FIXTURES['visual_jobs'] if x['status'] == visual))
            item.update(analysis_id=obj['analysis_id'], job_id=f'TEST-job-{len(jobs)}')
            jobs[item['job_id']] = item
            self.send(202, item)
    return ThreadingHTTPServer((host, port), Handler)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--host', default='127.0.0.1')
    parser.add_argument('--port', type=int, default=8765)
    parser.add_argument('--scenario', choices=['non-guide', 'stop', 'guide'], default='non-guide')
    parser.add_argument('--visual', choices=['queued', 'running', 'completed', 'failed'], default='failed')
    parser.add_argument('--verification', choices=['observed_change', 'issue_remaining', 'inconclusive'], default='inconclusive')
    args = parser.parse_args()
    if args.port == 8000:
        parser.error('8000 reserved for product examples; use test-only port 8765')
    server = make_server(**vars(args))
    print(f'TEST ONLY SYNTHETIC/MOCK on {args.host}:{args.port}; no real guide approval', flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()


if __name__ == '__main__':
    main()
