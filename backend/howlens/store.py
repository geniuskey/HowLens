from collections import OrderedDict
from dataclasses import dataclass
from .models import Analysis, VisualJob
from .safety import Approval


@dataclass
class StoredAnalysis:
    analysis: Analysis
    photo: bytes
    approval: Approval


class MemoryStore:
    def __init__(self, max_analyses=16, max_bytes=64*1024*1024):
        if max_analyses < 1 or max_bytes < 10*1024*1024:
            raise ValueError('Invalid memory bounds')
        self.max_analyses, self.max_bytes = max_analyses, max_bytes
        self.analyses = OrderedDict()
        self.jobs: dict[str, VisualJob] = {}
        self.attempts: dict[str, list[str]] = {}

    def put(self, stored):
        while self.analyses and (len(self.analyses) >= self.max_analyses or
                                 sum(len(a.photo) for a in self.analyses.values()) + len(stored.photo) > self.max_bytes):
            key, _ = self.analyses.popitem(last=False)
            for job in self.attempts.pop(key, []):
                self.jobs.pop(job, None)
        self.analyses[stored.analysis.analysis_id] = stored
