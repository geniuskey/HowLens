#!/usr/bin/env python3
"""Offline fixture validation plus opt-in, read-only HTTP health smoke."""
import argparse
import json
import math
from pathlib import Path
import sys
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit
from urllib.request import Request, build_opener, HTTPRedirectHandler

try:
    from . import validator
except ImportError:
    import validator

ROOT = Path(__file__).resolve().parent


class NoRedirect(HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def health_smoke(base_url, timeout):
    url = urlsplit(base_url)
    validator.require(url.scheme in ('http', 'https') and bool(url.hostname)
                      and not url.username and not url.password and not url.query and not url.fragment,
                      'base-url: expected http(s) URL without credentials, query, or fragment')
    validator.require(math.isfinite(timeout) and timeout > 0, 'timeout must be finite and positive')
    request = Request(base_url.rstrip('/') + '/health', headers={'Accept': 'application/json'})
    with build_opener(NoRedirect).open(request, timeout=timeout) as response:
        validator.require(response.status == 200, 'health: expected HTTP 200')
        validator.require(response.headers.get_content_type() == 'application/json', 'health: expected JSON content type')
        raw = response.read(65537)
        validator.require(len(raw) <= 65536, 'health response exceeds 64 KiB')
        data = json.loads(raw)
    validator.fields(data, 'status mode', 'health')
    validator.string(data['status'], 'health status')
    validator.enum(data['mode'], 'live mock', 'health mode')
    return {'status': 'passed', 'endpoint': 'GET /health', 'reported_status': data['status'],
            'reported_mode': data['mode'], 'scope': 'HTTP/JSON contract only; no analysis, visual, or verification calls'}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--fixtures', type=Path, default=ROOT / 'fixtures/contract.json')
    parser.add_argument('--cases', type=Path, default=ROOT / 'fixtures/safety_cases.json')
    parser.add_argument('--base-url', help='Opt-in GET /health only; never submits TEST guide/evidence or invokes models')
    parser.add_argument('--timeout', type=float, default=5.0)
    args = parser.parse_args(argv)
    report = {'fixture_validation': 'not_run', 'safety_case_validation': 'not_run',
              'live_api_smoke': {'status': 'not_run'}, 'live_safety_evaluations': 'not_run',
              'notice': 'Structural fixture checks are not product safety or real equipment approval.'}
    try:
        data = json.loads(args.fixtures.read_text())
        report['fixture_count'] = validator.bundle(data)
        report['fixture_validation'] = 'passed'
        cases = json.loads(args.cases.read_text())
        report['safety_case_count'] = validator.safety_cases(cases)
        report['safety_case_validation'] = 'passed'
        report['pending_cases'] = sum(case['status'] == 'pending' for case in cases['cases'])
        if args.base_url:
            report['live_api_smoke'] = {'status': 'failed'}
            report['live_api_smoke'] = health_smoke(args.base_url, args.timeout)
    except (validator.Invalid, OSError, URLError, ValueError) as error:
        if isinstance(error, HTTPError):
            error.close()
        # Avoid dumping remote bodies, URLs with credentials, or response data.
        report['error'] = str(error) if isinstance(error, validator.Invalid) else type(error).__name__
        report['status'] = 'failed'
        print(json.dumps(report, indent=2))
        return 1
    report['status'] = 'passed'
    print(json.dumps(report, indent=2))
    return 0


if __name__ == '__main__':
    sys.exit(main())
