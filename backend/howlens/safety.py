"""Registry is populated only from independently checked manuals, never model text."""
from dataclasses import dataclass, field
from .models import Analysis, Evidence


@dataclass(frozen=True)
class Approval:
    device_confirmed: bool = False
    sufficient_evidence: bool = False
    hazard_free: bool = False
    confirmed_preconditions: set[str] = field(default_factory=set)
    approved_steps: set[str] = field(default_factory=set)
    provenance: str = ''


class ManualRegistry:
    def __init__(self):
        self._entries = {}

    def register(self, device_id: str, evidence: dict, supported_actions: set[str]):
        """Trusted bootstrap API: quote, page, source/version and action mapping checked offline.

        No HTTP registration route. Public source alone is not approval; caller must
        verify actual PDF, rights, page text and action correspondence before registration.
        """
        e = Evidence.model_validate(evidence)
        self._entries[(device_id, e.document_id, e.document_version, e.pdf_page, e.section, e.quote,
                       e.source_url, e.printed_page)] = frozenset(supported_actions)

    def actions(self, device_id, e):
        return self._entries.get((device_id, e.document_id, e.document_version, e.pdf_page, e.section,
                                  e.quote, e.source_url, e.printed_page))


def sanitize(a: Analysis, registry: ManualRegistry, approval: Approval) -> Analysis:
    a = a.model_copy(deep=True)
    original_evidence = a.evidence
    a.evidence = [e for e in a.evidence if registry.actions(a.device_id, e) is not None]
    ids = {e.evidence_id for e in a.evidence}
    unique = (len(ids) == len(original_evidence)
              and len({s.step_id for s in a.steps}) == len(a.steps)
              and len({p.precondition_id for p in a.preconditions}) == len(a.preconditions))
    refs_valid = all(set(p.evidence_ids) <= ids for p in a.preconditions)
    step_support = all(s.evidence_ids and set(s.evidence_ids) <= ids and
                       all(s.description in registry.actions(a.device_id, e)
                           for e in a.evidence if e.evidence_id in s.evidence_ids) for s in a.steps)
    safe = (a.mode == 'live' and approval.provenance and approval.device_confirmed
            and approval.sufficient_evidence and approval.hazard_free and not a.warnings
            and bool(a.evidence) and unique and refs_valid and step_support and 1 <= len(a.steps) <= 9
            and all(s.step_id in approval.approved_steps for s in a.steps)
            and all(not p.required or (p.status == 'satisfied' and p.precondition_id in
                                      approval.confirmed_preconditions) for p in a.preconditions))
    # Never preserve dangling/unvalidated precondition references.
    for p in a.preconditions:
        p.evidence_ids = [ref for ref in p.evidence_ids if ref in ids]
    if a.decision != 'guide' or not safe:
        if a.decision == 'guide':
            a.decision = 'stop' if a.warnings or not approval.hazard_free and approval.provenance else 'needs_more_information'
            a.missing_information.append('Independent equipment, manual, action and precondition validation is required.')
        a.steps = []
    return a
