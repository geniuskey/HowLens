from typing import Literal
from pydantic import BaseModel, ConfigDict, Field, model_validator

MAX_DTO_BYTES = 128 * 1024

Mode = Literal['live', 'mock']
Device = Literal['server', 'cobot', 'ups']


class DTO(BaseModel):
    model_config = ConfigDict(extra='forbid', strict=True, revalidate_instances='always')

    @model_validator(mode='after')
    def bounded_output(self):
        def check(value):
            if isinstance(value, str) and len(value) > 4000:
                raise ValueError('Output string exceeds limit')
            if isinstance(value, list):
                if len(value) > 128:
                    raise ValueError('Output array exceeds limit')
                for item in value:
                    check(item)
            if isinstance(value, dict):
                for item in value.values():
                    check(item)
        check(self.model_dump())
        if len(self.model_dump_json().encode('utf-8')) > MAX_DTO_BYTES:
            raise ValueError('Output exceeds byte limit')
        return self


class Evidence(DTO):
    evidence_id: str
    document_id: str
    document_version: str
    pdf_page: int = Field(ge=1)
    printed_page: str | None
    section: str
    quote: str
    source_url: str


class Precondition(DTO):
    precondition_id: str
    description: str
    status: Literal['satisfied', 'unsatisfied', 'unknown']
    required: bool
    evidence_ids: list[str]


class Step(DTO):
    step_id: str
    description: str
    evidence_ids: list[str] = Field(min_length=1)
    visual_hint: str


class Analysis(DTO):
    analysis_id: str
    device_id: Device
    decision: Literal['guide', 'needs_more_information', 'stop']
    observations: list[str]
    evidence: list[Evidence]
    preconditions: list[Precondition]
    steps: list[Step]
    warnings: list[str]
    missing_information: list[str]
    mode: Mode


class Panel(DTO):
    index: int = Field(ge=0, le=8)
    step_id: str
    image_url: str


class VisualJob(DTO):
    job_id: str
    analysis_id: str
    status: Literal['queued', 'running', 'completed', 'failed']
    image_url: str | None
    panels: list[Panel]
    error: str | None
    mode: Mode


class Verification(DTO):
    analysis_id: str
    result: Literal['observed_change', 'issue_remaining', 'inconclusive']
    observations: list[str]
    evidence_ids: list[str]
    missing_information: list[str]
    limitations: list[str]
    mode: Mode
