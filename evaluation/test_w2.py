import contextlib
import io
import json
import threading
import unittest
from unittest.mock import patch
from urllib.request import urlopen
from evaluation import api_smoke as smoke, demo_double, validator


@contextlib.contextmanager
def running(**kwargs):
    server = demo_double.make_server(port=0, **kwargs)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    try:
        yield f'http://127.0.0.1:{server.server_port}'
    finally:
        server.shutdown()
        server.server_close()
        thread.join()


class W2Tests(unittest.TestCase):
    def test_complete_non_guide_smoke(self):
        with running() as base:
            report = smoke.smoke(base)
        self.assertEqual(len(report), 11)
        self.assertTrue(all(x['status'] == 'passed' for x in report))
        self.assertEqual([x['http'] for x in report], [200, 404, 415, 415, 413, 422, 404, 404, 200, 409, 409])

    def test_live_default_never_posts(self):
        with patch.object(smoke, 'request', side_effect=[(200, {'status': 'ok', 'mode': 'live'}), (404, {'detail': {'code': 'missing', 'message': 'missing', 'retryable': False}})]) as request:
            result = smoke.smoke('http://example.invalid')
        self.assertEqual(request.call_count, 2)
        self.assertTrue(all(call.args[3] is None for call in request.call_args_list))
        self.assertEqual(result[-1]['status'], 'pending')

    def test_guide_is_not_used_by_smoke(self):
        with running(scenario='guide') as base, patch.object(smoke, 'request', wraps=smoke.request) as request:
            result = smoke.smoke(base)
        paths = [call.args[1] for call in request.call_args_list]
        self.assertFalse(any(path.startswith('/analyses/TEST-analysis-') for path in paths))
        self.assertEqual(result[-1]['status'], 'pending')
        self.assertEqual(result[-1]['case'], 'non_guide_409')

    def test_complete_visual_and_all_verification_enums(self):
        for result in ('observed_change', 'issue_remaining', 'inconclusive'):
            with self.subTest(result=result), running(scenario='guide', visual='completed', verification=result) as base:
                body, mime = smoke.multipart()
                _, analysis = smoke.request(base, '/analyses', 2, body, mime)
                path = '/analyses/' + analysis['analysis_id']
                code, visual = smoke.request(base, path + '/visual', 2, b'')
                self.assertEqual(code, 202)
                validator.visual(visual, analysis)
                _, reused = smoke.request(base, path + '/visual', 2, b'')
                self.assertEqual(reused, visual)
                _, polled = smoke.request(base, '/visual-jobs/' + visual['job_id'], 2)
                self.assertEqual(polled, visual)
                images = []
                for panel in visual['panels']:
                    with urlopen(base + panel['image_url'], timeout=2) as response:
                        images.append(response.read())
                self.assertEqual(len(set(images)), 9)
                self.assertTrue(all(png.startswith(b'\x89PNG\r\n\x1a\n') for png in images))
                _, verification = smoke.request(base, path + '/verification', 2, body, mime)
                validator.verification(verification, analysis)
                self.assertEqual(verification['result'], result)

    def test_visual_failed_retry_limit(self):
        with running(scenario='guide', visual='failed') as base:
            body, mime = smoke.multipart()
            _, analysis = smoke.request(base, '/analyses', 2, body, mime)
            path = '/analyses/' + analysis['analysis_id'] + '/visual'
            for _ in range(2):
                code, job = smoke.request(base, path, 2, b'')
                self.assertEqual(code, 202)
                validator.visual(job, analysis)
            code, _ = smoke.request(base, path, 2, b'')
            self.assertEqual(code, 409)
            self.assertTrue(analysis['steps'])

    def test_wrong_expectations_fail_and_cli_reports(self):
        with patch.object(smoke, 'request', return_value=(201, {})), contextlib.redirect_stdout(io.StringIO()) as output:
            code = smoke.main(['--base-url', 'http://example.invalid'])
        self.assertEqual(code, 1)
        self.assertEqual(json.loads(output.getvalue())['status'], 'failed')

    def test_error_shapes(self):
        smoke.error_shape({'detail': [{'loc': ['body'], 'msg': 'missing', 'type': 'missing'}]}, 422)
        for data in ({'detail': []}, {'detail': {'code': 'x', 'message': 'x', 'retryable': 'yes'}}):
            with self.assertRaises(validator.Invalid):
                smoke.error_shape(data, 422)


if __name__ == '__main__':
    unittest.main()
