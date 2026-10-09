"""Offline HTTP doubles only; no credential or paid generation is needed."""
import asyncio
import base64
import json
import os
import unittest
from io import BytesIO
from unittest.mock import patch

import httpx
from PIL import Image
from visual import service
from visual.openai_provider import (OpenAIStoryboardProvider, ImageProviderError,
                                    ImageProviderTimeout, MAX_RESPONSE_BYTES)
from visual.splitter import InvalidStoryboardError, split_storyboard
from test_visual import analysis


def encoded_png(size=(1024, 1024)):
    with Image.new("RGB", size, "#A0B0C0") as image:
        output = BytesIO()
        image.save(output, format="PNG")
    return base64.b64encode(output.getvalue()).decode("ascii")


class OpenAIProviderTests(unittest.IsolatedAsyncioTestCase):
    def tearDown(self):
        service.configure_provider(None)

    def provider(self, handler):
        return OpenAIStoryboardProvider(api_key="offline-test-key",
                                       transport=httpx.MockTransport(handler))

    async def test_http_contract_normalization_and_split(self):
        requests = []
        def handler(request):
            requests.append(request)
            self.assertEqual(str(request.url), "https://api.openai.com/v1/images/generations")
            self.assertEqual(request.method, "POST")
            body = json.loads(request.content)
            self.assertEqual({k: v for k, v in body.items() if k != "prompt"},
                             {"model": "gpt-image-1.5", "n": 1, "size": "1024x1024",
                              "quality": "low", "output_format": "png",
                              "background": "opaque", "moderation": "auto"})
            self.assertNotIn("response_format", body)
            return httpx.Response(200, json={"data": [{"b64_json": encoded_png()}]})
        service.configure_provider(self.provider(handler))
        grid = await service.generate_storyboard(analysis())
        with Image.open(BytesIO(grid)) as image:
            self.assertEqual(image.size, (1023, 1023))
        panels = split_storyboard(grid)
        self.assertEqual(len(panels), 9)
        with Image.open(BytesIO(panels[0])) as image:
            self.assertEqual(image.size, (341, 341))
        self.assertEqual(len(requests), 1)

    async def test_forbidden_inputs_zero_http_calls(self):
        calls = []
        def handler(request):
            calls.append(request)
            return httpx.Response(500)
        service.configure_provider(self.provider(handler))
        for decision, mode in [("stop", "live"), ("needs_more_information", "live"),
                               ("guide", "mock"), ("guide", "invalid")]:
            value = analysis()
            value.update(decision=decision, mode=mode)
            with self.assertRaises(service.VisualApprovalError):
                await service.generate_storyboard(value)
        self.assertEqual(calls, [])

    async def test_errors_sanitized_and_no_retry(self):
        calls = []
        def handler(request):
            calls.append(request)
            return httpx.Response(401, text="private provider details offline-test-key")
        with self.assertRaises(ImageProviderError) as caught:
            await self.provider(handler).generate_png(prompt="Approved scene")
        self.assertEqual(str(caught.exception), "Image provider HTTP failure (401)")
        self.assertIsNone(caught.exception.__cause__)
        self.assertEqual(len(calls), 1)

    async def test_network_and_timeout_sanitized(self):
        for error, expected in [(httpx.ReadTimeout("private"), ImageProviderTimeout),
                                (httpx.ConnectError("private"), ImageProviderError)]:
            def handler(request):
                raise error
            with self.assertRaises(expected) as caught:
                await self.provider(handler).generate_png(prompt="Approved scene")
            self.assertNotIn("private", str(caught.exception))

    async def test_total_timeout_cancels(self):
        async def handler(request):
            await asyncio.sleep(5)
            return httpx.Response(200)
        provider = self.provider(handler)
        provider.timeout_seconds = 0.02  # Shorten validated default for offline test.
        with self.assertRaises(ImageProviderTimeout):
            await provider.generate_png(prompt="Approved scene")

    async def test_invalid_response_and_output(self):
        payloads = [{}, {"data": []}, {"data": [{"url": "https://invalid.example"}]},
                    {"data": [{"b64_json": "!!!"}]},
                    {"data": [{"b64_json": base64.b64encode(b"broken").decode()}]},
                    {"data": [{"b64_json": encoded_png((96, 96))}]}]
        for payload in payloads:
            with self.subTest(payload_type=list(payload)):
                with self.assertRaises((ImageProviderError, InvalidStoryboardError)):
                    await self.provider(lambda req: httpx.Response(200, json=payload)).generate_png(prompt="Approved scene")

    async def test_response_limit(self):
        with self.assertRaises(ImageProviderError):
            await self.provider(lambda req: httpx.Response(200, content=b"x" * (MAX_RESPONSE_BYTES + 1))).generate_png(prompt="Approved scene")

    def test_env_missing_invalid_and_repr_does_not_expose_key(self):
        with patch.dict(os.environ, {}, clear=True):
            with self.assertRaises(service.ProviderNotConfiguredError):
                OpenAIStoryboardProvider.from_env()
        for settings in [{"timeout_seconds": "bad"}, {"timeout_seconds": "nan"},
                         {"timeout_seconds": 301}, {"size": "96x96"},
                         {"quality": "max"}, {"model": "unknown"}]:
            with self.assertRaises(service.ProviderNotConfiguredError):
                OpenAIStoryboardProvider(api_key="offline-test-key", **settings)
        self.assertNotIn("offline-test-key", repr(self.provider(lambda r: httpx.Response(500))))

    def test_mapping_matches_prompt_for_one_to_nine_steps(self):
        for count in range(1, 10):
            value = analysis()
            value["steps"] = [{"step_id": "s" + str(i), "description": "Approved action",
                               "evidence_ids": ["e1"]} for i in range(count)]
            expected = ["s" + str(i * count // 9) for i in range(9)]
            self.assertEqual(service.scene_step_ids(value), expected)
            scenes = json.loads(service.build_storyboard_prompt(value).split("\n", 1)[1])["scenes"]
            self.assertEqual([s["step_id"] for s in scenes], expected)
