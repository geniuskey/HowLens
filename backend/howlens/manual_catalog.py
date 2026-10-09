"""Server-owned allowlist; empty until original PDFs/excerpts are independently reviewed."""
from dataclasses import dataclass
from urllib.parse import urlsplit
import re
from .models import Evidence
from .safety import ManualRegistry

# Exact original PDF URLs, never URLs supplied by model/client. No acquired PDFs yet.
APPROVED_SOURCE_URLS: frozenset[str] = frozenset()


@dataclass(frozen=True)
class ManualEntry:
    device_id: str
    evidence: Evidence
    pdf_sha256: str
    reviewed_by: str
    usage_terms: str
    supported_actions: frozenset[str]


def build_registry(entries=(), *, allowed_sources=APPROVED_SOURCE_URLS):
    registry = ManualRegistry()
    if len(entries) > 64:
        raise ValueError('Manual catalog exceeds limit')
    for entry in entries:
        e = Evidence.model_validate(entry.evidence)
        parsed = urlsplit(e.source_url)
        if (entry.device_id not in {'server','cobot','ups'} or e.source_url not in allowed_sources
                or parsed.scheme != 'https' or not parsed.hostname or parsed.username or parsed.password
                or not re.fullmatch(r'[a-f0-9]{64}', entry.pdf_sha256)
                or not entry.reviewed_by.strip() or not entry.usage_terms.strip()
                or not e.document_version.strip() or not e.quote.strip() or not e.section.strip()
                or not 1 <= len(entry.supported_actions) <= 9
                or any(not action.strip() or len(action) > 4000 for action in entry.supported_actions)):
            raise ValueError('Unreviewed/non-allowlisted manual entry')
        registry.register(entry.device_id, e.model_dump(), set(entry.supported_actions))
    return registry
