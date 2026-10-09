"""Additive discovery DTOs. Discovery never carries executable guide steps."""
from datetime import datetime
from typing import Annotated, Literal
from pydantic import BaseModel, ConfigDict, Field, StringConstraints, field_validator

Name = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=200)]
Text = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=1000)]


class StrictModel(BaseModel):
    model_config = ConfigDict(extra='forbid', strict=True)


class DiscoverySource(StrictModel):
    title: Name
    url: Annotated[str, StringConstraints(min_length=1, max_length=2048)]
    retrieved_at: datetime

    @field_validator('url')
    @classmethod
    def public_url(cls, value):
        from .discovery import validate_public_url
        return validate_public_url(value)

    @field_validator('retrieved_at')
    @classmethod
    def utc_time(cls, value):
        if value.tzinfo is None or value.utcoffset() is None:
            raise ValueError('Source retrieval timestamp must include timezone')
        return value


class ProductCandidate(StrictModel):
    manufacturer: Name
    model: Name
    summary: Text
    sources: list[DiscoverySource] = Field(min_length=1, max_length=3)
    match_notes: list[Text] = Field(min_length=1, max_length=4)


class ProductDiscovery(StrictModel):
    discovery_id: Name
    status: Literal['candidate', 'needs_more_information', 'not_found']
    candidates: list[ProductCandidate] = Field(max_length=3)
    missing_information: list[Text] = Field(max_length=5)
    mode: Literal['live', 'mock']

    @field_validator('candidates')
    @classmethod
    def candidates_match_status(cls, value, info):
        if info.data.get('status') == 'candidate' and not value:
            raise ValueError('Candidate status requires a sourced product')
        if info.data.get('status') == 'not_found' and value:
            raise ValueError('Not-found status cannot claim products')
        return value
