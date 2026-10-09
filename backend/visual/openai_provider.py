"""Bounded server-only adapter for POST /v1/images/generations.

No automatic retries, external asset URL fetches, credentials logging or fallback.
"""
from __future__ import annotations

import asyncio
import base64
import binascii
import json
import math
import os
from io import BytesIO

import httpx
from PIL import Image

from .service import ProviderNotConfiguredError
from .splitter import MAX_BYTES, MAX_PIXELS, InvalidStoryboardError, _decode

ENDPOINT = "https://api.openai.com/v1/images/generations"
MAX_RESPONSE_BYTES = 15 * 1024 * 1024
SIZES = {"1024x1024", "1536x1024", "1024x1536"}
MODELS = {"gpt-image-1.5", "gpt-image-1-mini", "gpt-image-2",
          "gpt-image-2.5-sunburst", "gpt-image-2.5-flare"}


class ImageProviderError(RuntimeError):
    """Sanitized upstream failure; response bodies and credentials are omitted."""


class ImageProviderTimeout(TimeoutError):
    """The provider did not finish within the configured time budget."""


def _normalize_png(raw: bytes, expected_size: str) -> bytes:
    if not raw or len(raw) > MAX_BYTES:
        raise InvalidStoryboardError("Provider PNG exceeds byte limit or is empty")
    try:
        with Image.open(BytesIO(raw), formats=["PNG"]) as image:
            expected = tuple(map(int, expected_size.split("x")))
            if image.size != expected or image.width * image.height > MAX_PIXELS:
                raise InvalidStoryboardError("Provider PNG dimensions do not match request")
            if getattr(image, "n_frames", 1) != 1:
                raise InvalidStoryboardError("Animated provider output rejected")
            image.verify()
        with Image.open(BytesIO(raw), formats=["PNG"]) as image:
            image.load()
            image = image.convert("RGB")  # Request explicitly uses opaque background.
            target = (image.width - image.width % 3, image.height - image.height % 3)
            with image:
                with image.resize(target, Image.Resampling.LANCZOS) as resized:
                    output = BytesIO()
                    resized.save(output, format="PNG")
                    result = output.getvalue()
        with _decode(result):
            pass
        return result
    except (OSError, SyntaxError, ValueError, Image.DecompressionBombError):
        raise InvalidStoryboardError("Invalid provider PNG") from None


class OpenAIStoryboardProvider:
    def __init__(self, *, api_key: str, model: str = "gpt-image-1.5",
                 size: str = "1024x1024", quality: str = "low",
                 timeout_seconds: float = 120, transport=None):
        if not isinstance(api_key, str) or not api_key.strip():
            raise ProviderNotConfiguredError("OPENAI_API_KEY is required on the server")
        if model not in MODELS or size not in SIZES or quality not in {"low", "medium", "high"}:
            raise ProviderNotConfiguredError("Unsupported image model, size or quality setting")
        try:
            timeout_seconds = float(timeout_seconds)
        except (TypeError, ValueError):
            raise ProviderNotConfiguredError("Image timeout must be 1-300 seconds") from None
        if not math.isfinite(timeout_seconds) or not 1 <= timeout_seconds <= 300:
            raise ProviderNotConfiguredError("Image timeout must be 1-300 seconds")
        self._api_key = api_key.strip()
        self.model, self.size, self.quality = model, size, quality
        self.timeout_seconds = timeout_seconds
        self._transport = transport  # Offline contract tests use httpx.MockTransport.

    @classmethod
    def from_env(cls) -> OpenAIStoryboardProvider:
        """Read credentials/settings at server startup; does not make a request."""
        return cls(api_key=os.environ.get("OPENAI_API_KEY", ""),
                   model=os.environ.get("HOWLENS_IMAGE_MODEL", "gpt-image-1.5"),
                   size=os.environ.get("HOWLENS_IMAGE_SIZE", "1024x1024"),
                   quality=os.environ.get("HOWLENS_IMAGE_QUALITY", "low"),
                   timeout_seconds=os.environ.get("HOWLENS_IMAGE_TIMEOUT_SECONDS", "120"))

    async def generate_png(self, *, prompt: str) -> bytes:
        if not isinstance(prompt, str) or not prompt.strip() or len(prompt) > 32000:
            raise ImageProviderError("Invalid image prompt length")
        try:
            return await asyncio.wait_for(self._request(prompt), timeout=self.timeout_seconds)
        except (asyncio.TimeoutError, httpx.TimeoutException):
            raise ImageProviderTimeout("Image generation timed out") from None
        except httpx.HTTPError:
            raise ImageProviderError("Image provider network failure") from None

    async def _request(self, prompt: str) -> bytes:
        body = {"model": self.model, "prompt": prompt, "n": 1,
                "size": self.size, "quality": self.quality,
                "output_format": "png", "background": "opaque", "moderation": "auto"}
        # Fixed HTTPS endpoint, no redirect or proxy/env credential forwarding.
        async with httpx.AsyncClient(timeout=self.timeout_seconds, transport=self._transport,
                                     follow_redirects=False, trust_env=False) as client:
            async with client.stream("POST", ENDPOINT,
                    headers={"Authorization": "Bearer " + self._api_key}, json=body) as response:
                if response.status_code != 200:
                    raise ImageProviderError("Image provider HTTP failure ({})".format(response.status_code))
                chunks, count = [], 0
                async for chunk in response.aiter_bytes():
                    count += len(chunk)
                    if count > MAX_RESPONSE_BYTES:
                        raise ImageProviderError("Image provider response exceeds limit")
                    chunks.append(chunk)
        try:
            payload = json.loads(b"".join(chunks))
            data = payload["data"]
            if not isinstance(data, list) or len(data) != 1:
                raise ValueError()
            encoded = data[0]["b64_json"]
            if not isinstance(encoded, str) or len(encoded) > 4 * ((MAX_BYTES + 2) // 3):
                raise ValueError()
            raw = base64.b64decode(encoded, validate=True)
        except (ValueError, TypeError, KeyError, IndexError, binascii.Error):
            raise ImageProviderError("Invalid image provider response") from None
        return _normalize_png(raw, self.size)
