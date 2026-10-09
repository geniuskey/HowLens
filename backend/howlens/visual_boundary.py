"""Prepared public Visual boundary; route activation/assets await authorized integration."""
import asyncio
from dataclasses import dataclass
from importlib import import_module
from .models import Analysis


@dataclass(frozen=True)
class PreparedVisual:
    grid_png: bytes
    panels: tuple[bytes, ...]
    step_ids: tuple[str, ...]


async def generate_reviewed_assets(saved, *, calls_authorized=False, quality_review=None,
                                   service=None, splitter=None, timeout_seconds=30):
    analysis = Analysis.model_validate(saved.analysis)
    approval = saved.approval
    if (analysis.decision != 'guide' or analysis.mode != 'live' or not approval.provenance
            or not approval.device_confirmed or not approval.sufficient_evidence
            or not approval.hazard_free or analysis.warnings or not 1 <= len(analysis.steps) <= 9
            or any(s.step_id not in approval.approved_steps for s in analysis.steps)
            or any(p.required and (p.status != 'satisfied' or p.precondition_id not in
                                   approval.confirmed_preconditions) for p in analysis.preconditions)):
        raise ValueError('Stored independently approved live guide required')
    if not calls_authorized or quality_review is None:
        raise RuntimeError('Visual budget and semantic review are not configured')
    try:
        generate = service or import_module('visual.service').generate_storyboard
        split = splitter or import_module('visual.splitter').split_storyboard
    except ImportError:
        raise RuntimeError('Visual library has not been integrated') from None
    async with asyncio.timeout(timeout_seconds):
        grid = await generate(analysis.model_dump())
        if not isinstance(grid, bytes) or not 0 < len(grid) <= 10*1024*1024:
            raise ValueError('Invalid visual grid output')
        panels = await asyncio.to_thread(split, grid)
        if len(panels) != 9 or any(not isinstance(p, bytes) or not 0 < len(p) <= 10*1024*1024 for p in panels):
            raise ValueError('Expected nine bounded PNG panels')
        step_ids = tuple(analysis.steps[min(index*len(analysis.steps)//9,
                                           len(analysis.steps)-1)].step_id for index in range(9))
        if await quality_review(analysis.model_copy(deep=True), tuple(panels), step_ids) is not True:
            raise ValueError('Visual semantic quality review failed')
        return PreparedVisual(grid, tuple(panels), step_ids)
