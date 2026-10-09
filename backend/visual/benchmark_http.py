"""Direct sandbox transport; no product service, guide, route or store imports."""
from __future__ import annotations

import asyncio
import base64
import json
import math
import re
from io import BytesIO

import httpx
from PIL import Image

PILOT_MODELS = ("gpt-image-1.5", "gpt-image-1-mini")
ENDPOINT = "https://api.openai.com/v1/images/generations"
MAX_HTTP_BYTES = 15 * 1024 * 1024
MAX_PNG_BYTES = 10 * 1024 * 1024


class BenchmarkFailure(RuntimeError):
    def __init__(self, code, *, request_id=None, http_status=None, usage=None):
        super().__init__(code)
        self.code, self.request_id, self.http_status = code, request_id, http_status
        self.usage = usage


def safe_request_id(value):
    return value if isinstance(value, str) and re.fullmatch(r"[A-Za-z0-9_-]{1,200}", value) else None


def numeric_usage(value):
    """Retain only known numeric token counters; never arbitrary provider text."""
    if not isinstance(value, dict):
        return None
    result = {}
    for key in ("input_tokens", "output_tokens", "total_tokens"):
        number = value.get(key)
        if isinstance(number, int) and not isinstance(number, bool) and number >= 0:
            result[key] = number
    for key in ("input_tokens_details", "output_tokens_details"):
        if isinstance(value.get(key), dict):
            details = {k: n for k, n in value[key].items()
                       if k in {"text_tokens", "image_tokens", "cached_tokens"}
                       and isinstance(n, int) and not isinstance(n, bool) and n >= 0}
            if details:
                result[key] = details
    return result or None


def validate_raw_png(raw):
    if not isinstance(raw, bytes) or not raw or len(raw) > MAX_PNG_BYTES:
        raise BenchmarkFailure("invalid_output")
    try:
        with Image.open(BytesIO(raw), formats=["PNG"]) as image:
            if image.size != (1024, 1024) or getattr(image, "n_frames", 1) != 1:
                raise ValueError()
            image.verify()
        with Image.open(BytesIO(raw), formats=["PNG"]) as image:
            image.load()
    except (OSError, ValueError, SyntaxError, Image.DecompressionBombError):
        raise BenchmarkFailure("invalid_output") from None


class SandboxImageClient:
    """One request per call, no retries; credentials remain in memory only."""
    def __init__(self, *, api_key, timeout_seconds=120, transport=None):
        if not isinstance(api_key, str) or not api_key.strip():
            raise ValueError("Server OPENAI_API_KEY is required for execute")
        if not isinstance(timeout_seconds, (float, int)) or not math.isfinite(timeout_seconds) or not 1 <= timeout_seconds <= 300:
            raise ValueError("Timeout must be 1-300 seconds")
        self._api_key = api_key.strip()
        self.timeout_seconds, self._transport = timeout_seconds, transport

    async def generate(self, *, model, prompt):
        if model not in PILOT_MODELS or not isinstance(prompt, str) or not 1 <= len(prompt) <= 32000:
            raise ValueError("Only bounded pilot model/prompts are supported")
        metadata = {"request_id": None, "http_status": None, "usage": None}
        try:
            return await asyncio.wait_for(self._request(model, prompt, metadata), self.timeout_seconds)
        except (asyncio.TimeoutError, httpx.TimeoutException):
            raise BenchmarkFailure("timeout", **metadata) from None
        except httpx.HTTPError:
            raise BenchmarkFailure("network_error", **metadata) from None
        except BenchmarkFailure as exc:
            exc.request_id, exc.http_status = metadata["request_id"], metadata["http_status"]
            exc.usage = metadata["usage"]
            raise exc from None

    async def _request(self, model, prompt, metadata):
        body = {"model": model, "prompt": prompt, "n": 1, "size": "1024x1024",
                "quality": "low", "output_format": "png", "background": "opaque", "moderation": "auto"}
        async with httpx.AsyncClient(timeout=self.timeout_seconds, transport=self._transport,
                                     trust_env=False, follow_redirects=False) as client:
            async with client.stream("POST", ENDPOINT,
                    headers={"Authorization": "Bearer " + self._api_key}, json=body) as response:
                metadata.update(request_id=safe_request_id(response.headers.get("x-request-id")),
                                http_status=response.status_code)
                if response.status_code != 200:
                    raise BenchmarkFailure("http_error")
                chunks, count = [], 0
                async for chunk in response.aiter_bytes():
                    count += len(chunk)
                    if count > MAX_HTTP_BYTES:
                        raise BenchmarkFailure("response_limit")
                    chunks.append(chunk)
        try:
            payload = json.loads(b"".join(chunks))
            metadata["usage"] = numeric_usage(payload.get("usage")) if isinstance(payload, dict) else None
            data = payload["data"]
            if not isinstance(data, list) or len(data) != 1:
                raise ValueError()
            encoded = data[0]["b64_json"]
            if not isinstance(encoded, str) or len(encoded) > 4 * ((MAX_PNG_BYTES + 2) // 3):
                raise ValueError()
            raw = base64.b64decode(encoded, validate=True)
        except (ValueError, KeyError, TypeError, IndexError):
            raise BenchmarkFailure("invalid_response") from None
        validate_raw_png(raw)
        return {"raw_png": raw, "request_id": metadata["request_id"],
                "http_status": metadata["http_status"], "usage": metadata["usage"]}
