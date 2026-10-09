"""Adversarial contract regression tests; HTTP tests use a local synthetic double."""
import contextlib
import copy
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import io
import json
from pathlib import Path
import tempfile
import threading
import unittest

from evaluation import run, validator as v

FIXTURES = json.loads((run.ROOT / 'fixtures/contract.json').read_text())
CASES = json.loads((run.ROOT / 'fixtures/safety_cases.json').read_text())


def set_path(obj, path, value):
    for key in path[:-1]:
        obj = obj[key]
    obj[path[-1]] = value


class ContractTests(unittest.TestCase):
    def test_offline_fixtures_and_pending_cases(self):
        self.assertEqual(v.bundle(FIXTURES), 11)
        self.assertEqual(v.safety_cases(CASES), 21)
        self.assertTrue(all(c['status'] == 'pending' for c in CASES['cases']))

    def test_analysis_rejections(self):
        changes = [
            (['decision'], 'success', 'decision'),
            (['device_id'], 'unknown', 'device_id'),
            (['mode'], 'production', 'mode'),
            (['observations'], 'text', 'observations'),
            (['evidence'], None, 'evidence'),
            (['evidence', 0, 'pdf_page'], 0, 'pdf_page'),
            (['evidence', 0, 'pdf_page'], True, 'pdf_page'),
            (['evidence', 0, 'printed_page'], 1, 'printed_page'),
            (['evidence', 0, 'source_url'], '/manual.pdf', 'source_url'),
            (['steps', 0, 'evidence_ids'], [], 'requires evidence'),
            (['steps', 0, 'evidence_ids'], ['missing'], 'unknown reference'),
            (['steps', 0, 'evidence_ids'], [None], 'nonempty string'),
            (['preconditions', 0, 'evidence_ids'], ['missing'], 'unknown reference'),
            (['preconditions', 0, 'required'], 'true', 'boolean'),
            (['preconditions', 0, 'status'], 'unknown', 'not satisfied'),
            (['preconditions', 0, 'status'], 'unsatisfied', 'not satisfied'),
            (['preconditions', 0, 'status'], 'verified', 'invalid enum'),
            (['steps'], [], '1–9'),
        ]
        for path, value, message in changes:
            with self.subTest(path=path, value=value):
                obj = copy.deepcopy(FIXTURES['analyses'][0])
                set_path(obj, path, value)
                with self.assertRaisesRegex(v.Invalid, message):
                    v.analysis(obj)

    def test_non_guide_steps_rejected_for_both_decisions(self):
        for decision in ('needs_more_information', 'stop'):
            with self.subTest(decision=decision):
                obj = copy.deepcopy(FIXTURES['analyses'][0])
                obj['decision'] = decision
                with self.assertRaisesRegex(v.Invalid, 'non-guide steps must be empty'):
                    v.analysis(obj)

    def test_missing_fields_nulls_and_nonobjects(self):
        guide = FIXTURES['analyses'][0]
        for key in guide:
            for remove in (True, False):
                with self.subTest(key=key, remove=remove):
                    obj = copy.deepcopy(guide)
                    if remove:
                        del obj[key]
                    else:
                        obj[key] = None
                    with self.assertRaises(v.Invalid):
                        v.analysis(obj)
        for value in (None, [], 'guide', 3):
            with self.assertRaises(v.Invalid):
                v.analysis(value)

    def test_unique_ids_and_step_limit(self):
        for key in ('evidence', 'steps', 'preconditions'):
            obj = copy.deepcopy(FIXTURES['analyses'][0])
            obj[key].append(copy.deepcopy(obj[key][0]))
            with self.assertRaisesRegex(v.Invalid, 'duplicate ID'):
                v.analysis(obj)
        obj = copy.deepcopy(FIXTURES['analyses'][0])
        obj['steps'] = [dict(obj['steps'][0], step_id=f'TEST-{i}') for i in range(10)]
        with self.assertRaisesRegex(v.Invalid, '1–9'):
            v.analysis(obj)

    def test_fake_evidence_cannot_be_live_fixture(self):
        for path, value in [(['mode'], 'live'), (['evidence', 0, 'document_id'], 'REAL'),
                            (['evidence', 0, 'quote'], 'Claimed real excerpt')]:
            obj = copy.deepcopy(FIXTURES['analyses'][0])
            set_path(obj, path, value)
            with self.assertRaises(v.Invalid):
                v.analysis(obj, fixture=True)

    def test_visual_rejections(self):
        guide = FIXTURES['analyses'][0]
        good = FIXTURES['visual_jobs'][2]
        changes = [
            (['analysis_id'], 'other', 'mismatch'),
            (['mode'], 'live', 'mode mismatch'),
            (['status'], 'success', 'invalid enum'),
            (['panels'], good['panels'][:8], 'exactly 9'),
            (['panels'], list(reversed(good['panels'])), 'row-major'),
            (['panels', 1, 'index'], 0, 'row-major'),
            (['panels', 0, 'index'], True, 'out of range'),
            (['panels', 0, 'index'], 9, 'out of range'),
            (['panels', 0, 'step_id'], 'invented', 'unknown step_id'),
            (['image_url'], None, 'requires image'),
            (['image_url'], 'https://outside.example/grid.png', 'relative'),
            (['panels', 0, 'image_url'], '/visual-assets/../secret', 'relative'),
            (['panels', 0, 'image_url'], '/visual-assets/%2e%2e/secret', 'relative'),
            (['error'], 'error on completed job', 'no error'),
        ]
        for path, value, message in changes:
            with self.subTest(path=path, value=value):
                obj = copy.deepcopy(good)
                set_path(obj, path, value)
                with self.assertRaisesRegex(v.Invalid, message):
                    v.visual(obj, guide)
        obj = copy.deepcopy(FIXTURES['visual_jobs'][3]); obj['error'] = None
        with self.assertRaisesRegex(v.Invalid, 'failed visual error'):
            v.visual(obj, guide)

    def test_visual_failure_preserves_original_text(self):
        guide = copy.deepcopy(FIXTURES['analyses'][0])
        before = copy.deepcopy(guide)
        for job in FIXTURES['visual_jobs'][3:]:
            v.visual(job, guide)
            self.assertEqual(guide, before)
            self.assertTrue(guide['steps'])

    def test_verification_rejections(self):
        for path, value, message in [(['result'], 'safe', 'invalid enum'),
                                    (['result'], 'success', 'invalid enum'),
                                    (['evidence_ids'], ['missing'], 'unknown reference'),
                                    (['analysis_id'], 'other', 'mismatch'),
                                    (['limitations'], None, 'array'),
                                    (['mode'], 'live', 'mode mismatch')]:
            with self.subTest(path=path, value=value):
                obj = copy.deepcopy(FIXTURES['verifications'][0])
                set_path(obj, path, value)
                with self.assertRaisesRegex(v.Invalid, message):
                    v.verification(obj, FIXTURES['analyses'][0])
        for validate, obj in [(v.visual, FIXTURES['visual_jobs'][0]),
                              (v.verification, FIXTURES['verifications'][0])]:
            with self.assertRaisesRegex(v.Invalid, 'requires guide|mismatch'):
                validate(obj, FIXTURES['analyses'][1])

    def test_missing_inputs_cannot_be_ready(self):
        obj = copy.deepcopy(CASES)
        obj['cases'][0]['status'] = 'ready'
        with self.assertRaisesRegex(v.Invalid, 'must remain pending'):
            v.safety_cases(obj)

    def test_cli_nonzero_for_corrupted_fixture(self):
        obj = copy.deepcopy(FIXTURES)
        obj['analyses'][1]['steps'] = obj['analyses'][0]['steps']
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'broken.json'
            path.write_text(json.dumps(obj))
            output = io.StringIO()
            with contextlib.redirect_stdout(output):
                result = run.main(['--fixtures', str(path)])
            self.assertEqual(result, 1)
            self.assertIn('non-guide steps must be empty', output.getvalue())


class HTTPDoubleTests(unittest.TestCase):
    def test_health_only_and_failure_reporting(self):
        state = {'body': {'status': 'ok', 'mode': 'mock'}, 'code': 200, 'paths': []}

        class Handler(BaseHTTPRequestHandler):
            def do_GET(self):
                state['paths'].append(self.path)
                self.send_response(state['code'])
                self.send_header('Content-Type', 'application/json')
                if state['code'] == 302:
                    self.send_header('Location', '/unexpected-redirect')
                self.end_headers()
                self.wfile.write(json.dumps(state['body']).encode())

            def log_message(self, *args):
                pass

        server = ThreadingHTTPServer(('127.0.0.1', 0), Handler)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        try:
            base = f'http://127.0.0.1:{server.server_port}'
            for code, body, expected in [(200, {'status': 'ok', 'mode': 'mock'}, 0),
                                         (200, {'status': 'ok', 'mode': 'live'}, 0),
                                         (200, {'status': 'ok', 'mode': 'invalid'}, 1),
                                         (200, {'mode': 'mock'}, 1), (503, {}, 1), (302, {}, 1)]:
                with self.subTest(code=code, body=body):
                    state.update(code=code, body=body)
                    output = io.StringIO()
                    with contextlib.redirect_stdout(output):
                        result = run.main(['--base-url', base, '--timeout', '1'])
                    self.assertEqual(result, expected)
                    report = json.loads(output.getvalue())
                    self.assertEqual(report['live_safety_evaluations'], 'not_run')
                    self.assertEqual(report['live_api_smoke']['status'], 'passed' if expected == 0 else 'failed')
            self.assertEqual(state['paths'], ['/health'] * 6)
        finally:
            server.shutdown()
            server.server_close()
            thread.join()

    def test_invalid_url_and_timeout_without_network(self):
        for base, timeout in [('file:///tmp/x', 1), ('http://user:secret@localhost', 1),
                              ('http://localhost', 0), ('http://localhost', float('nan'))]:
            with self.assertRaises(v.Invalid):
                run.health_smoke(base, timeout)


if __name__ == '__main__':
    unittest.main()
