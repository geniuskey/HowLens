"""No live adapter is configured in W1. Secrets belong in environment variables only.

Adapters must implement async methods, bounded network I/O, strict DTO parsing,
and treat photos/manuals as untrusted data. Never install a test fallback here.
"""
from typing import Protocol
from .models import Analysis, Verification


class Provider(Protocol):
    async def analyze(self, device_id: str, question: str, photo: bytes) -> Analysis: ...
    async def verify(self, analysis: Analysis, original_photo: bytes, photo: bytes,
                     confirmation: str | None) -> Verification: ...
