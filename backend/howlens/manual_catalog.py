"""Server-owned allowlist; empty until original PDFs/excerpts are independently reviewed."""
from dataclasses import dataclass
import json
from pathlib import Path
from urllib.parse import urlsplit
import re
from .models import Evidence
from .safety import ManualRegistry

# Exact original PDFs independently downloaded/hash/page checked on 2026-10-09.
APPROVED_SOURCE_URLS = frozenset({
    'https://dl.dell.com/content/manual15837466-dell-emc-poweredge-r750-installation-and-service-manual.pdf',
    'https://s3-eu-west-1.amazonaws.com/ur-support-site/225353/710-965-00_UR5e_User_Manual_en_Global.pdf',
    'https://download.schneider-electric.com/files?p_Doc_Ref=SPD_UM_SU-990-6411_EN&p_enDocType=User+guide&p_File_Name=SU_UM_990-6411A_EN.pdf',
})


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
                or not 0 <= len(entry.supported_actions) <= 9
                or any(not action.strip() or len(action) > 4000 for action in entry.supported_actions)):
            raise ValueError('Unreviewed/non-allowlisted manual entry')
        registry.register(entry.device_id, e.model_dump(), set(entry.supported_actions))
    return registry


def load_registry():
    path = Path(__file__).with_name('manual_sources.json')
    if not path.exists():
        return build_registry()  # Missing catalog never fabricates evidence.
    data = path.read_bytes()
    if len(data) > 128*1024:
        raise ValueError('Manual source catalog exceeds limit')
    rows = json.loads(data)
    if not isinstance(rows, list) or len(rows) > 64:
        raise ValueError('Invalid manual catalog')
    entries = [ManualEntry(device_id=row['device_id'], evidence=Evidence.model_validate(row['evidence']),
                          pdf_sha256=row['pdf_sha256'], reviewed_by=row['reviewed_by'],
                          usage_terms=row['usage_terms'], supported_actions=frozenset(row['supported_actions']))
               for row in rows]
    return build_registry(entries)
