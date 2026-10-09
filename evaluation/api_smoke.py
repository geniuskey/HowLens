"""Black-box contract smoke. Default POSTs only to a server reporting mode=mock."""
import argparse
import base64
import json
import math
from urllib.error import HTTPError
from urllib.parse import urlsplit
from urllib.request import Request, build_opener
from uuid import uuid4
from evaluation import validator as v
from evaluation.run import NoRedirect

PNG = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=')
LIMIT = 10 * 1024 * 1024


def multipart(photo=PNG, device='server', mime='image/png'):
    boundary = 'TEST-' + uuid4().hex
    chunks = []
    for name, value in [('device_id', device), ('question', 'TEST ONLY synthetic input; no equipment procedure requested.')]:
        chunks.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{name}"\r\n\r\n{value}\r\n'.encode())
    chunks += [f'--{boundary}\r\nContent-Disposition: form-data; name="photo"; filename="TEST.png"\r\nContent-Type: {mime}\r\n\r\n'.encode(), photo, f'\r\n--{boundary}--\r\n'.encode()]
    return b''.join(chunks), 'multipart/form-data; boundary=' + boundary


def request(base, path, timeout, body=None, content_type=None):
    req = Request(base.rstrip('/') + path, data=body, headers={'Accept': 'application/json'})
    if content_type:
        req.add_header('Content-Type', content_type)
    try:
        response = build_opener(NoRedirect).open(req, timeout=timeout)
    except HTTPError as error:
        response = error
    with response:
        code = response.code
        raw = response.read(1048577)
        v.require(len(raw) <= 1048576, 'response too large')
        v.require(response.headers.get_content_type() == 'application/json', 'expected JSON response')
        return code, json.loads(raw)


def error_shape(data, status):
    v.fields(data, 'detail', 'error')
    detail = data['detail']
    if status == 422 and isinstance(detail, list):
        v.require(bool(detail) and all(isinstance(x, dict) for x in detail), 'invalid validation error array')
        return
    v.fields(detail, 'code message retryable', 'detail')
    v.string(detail['code'], 'error code')
    v.string(detail['message'], 'error message')
    v.require(type(detail['retryable']) is bool, 'retryable must be boolean')


def smoke(base, timeout=5, allow_paid_analysis=False):
    url = urlsplit(base)
    v.require(url.scheme in ('http', 'https') and url.hostname and not url.username
              and not url.password and not url.query and not url.fragment, 'invalid base URL')
    v.require(math.isfinite(timeout) and timeout > 0, 'invalid timeout')
    results = []
    def check(name, path, expected, body=None, mime=None):
        code, data = request(base, path, timeout, body, mime)
        v.require(code == expected, f'{name}: expected {expected}, got {code}')
        if code >= 400:
            error_shape(data, code)
        results.append({'case': name, 'status': 'passed', 'http': code})
        return data
    health = check('health', '/health', 200)
    v.fields(health, 'status mode', 'health')
    v.string(health['status'], 'health status')
    v.enum(health['mode'], 'live mock', 'mode')
    unknown = 'TEST-missing-' + uuid4().hex
    check('unknown_visual_job', '/visual-jobs/' + unknown, 404)
    if health['mode'] != 'mock' and not allow_paid_analysis:
        results.append({'case': 'all_post_cases', 'status': 'pending', 'reason': 'live server: paid analysis opt-in is OFF; no POST sent'})
        return results
    for name, photo, device, mime, status in [
        ('invalid_format', b'TEST not an image', 'server', 'text/plain', 415),
        ('invalid_decode', b'TEST corrupt PNG', 'server', 'image/png', 415),
        ('oversize', PNG + b'X' * (LIMIT + 1 - len(PNG)), 'server', 'image/png', 413),
        ('invalid_device', PNG, 'TEST-unknown-device', 'image/png', 422),
    ]:
        body, content = multipart(photo, device, mime)
        check(name, '/analyses', status, body, content)
    body, content = multipart()
    check('unknown_analysis_visual', '/analyses/' + unknown + '/visual', 404, b'')
    check('unknown_analysis_verification', '/analyses/' + unknown + '/verification', 404, body, content)
    original = check('synthetic_analysis_upload', '/analyses', 200, body, content)
    v.analysis(original)
    v.require(original['mode'] == health['mode'], 'analysis mode differs from health')
    if original['decision'] == 'guide':
        results.append({'case': 'non_guide_409', 'status': 'pending', 'reason': 'synthetic input returned guide; no visual/verification POST sent and no live approval inferred'})
    else:
        from urllib.parse import quote
        path = '/analyses/' + quote(original['analysis_id'], safe='')
        check('non_guide_visual', path + '/visual', 409, b'')
        check('non_guide_verification', path + '/verification', 409, body, content)
    return results


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--base-url', required=True)
    parser.add_argument('--timeout', type=float, default=5)
    parser.add_argument('--allow-paid-analysis', action='store_true', help='OFF by default; permits POSTs to live mode, potentially paid. Not used in W2.')
    args = parser.parse_args(argv)
    report = {'scope': 'synthetic black-box contract smoke, not real equipment evaluation', 'paid_analysis_opt_in': args.allow_paid_analysis}
    try:
        report['cases'] = smoke(args.base_url, args.timeout, args.allow_paid_analysis)
        report['status'] = 'pending' if any(c['status'] == 'pending' for c in report['cases']) else 'passed'
        code = 2 if report['status'] == 'pending' else 0
    except (OSError, ValueError) as error:
        report.update(status='failed', error=str(error) if isinstance(error, v.Invalid) else type(error).__name__)
        code = 1
    print(json.dumps(report, indent=2, ensure_ascii=False))
    return code


if __name__ == '__main__':
    raise SystemExit(main())
