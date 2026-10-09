"""Prepared public Visual boundary; route activation/assets await authorized integration."""
import asyncio
from dataclasses import dataclass
from importlib import import_module
from .models import Analysis


def public_module(name):
    # Repository-root launch uses namespace backend.visual; backend-directory launch uses visual.
    try:
        return import_module(f'backend.visual.{name}')
    except ModuleNotFoundError as error:
        if error.name not in {'backend', 'backend.visual', f'backend.visual.{name}'}:
            raise
        return import_module(f'visual.{name}')


@dataclass(frozen=True)
class PreparedVisual:
    grid_png: bytes
    panels: tuple[bytes, ...]
    step_ids: tuple[str, ...]


async def generate_reviewed_assets(saved, *, calls_authorized=False, quality_review=None,
                                   service=None, splitter=None, scene_mapper=None, timeout_seconds=30):
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
        if service is None:
            public_service = public_module('service')
            generate = public_service.generate_storyboard
            scene_mapper = public_service.scene_step_ids
        else:
            generate = service
        split = splitter or public_module('splitter').split_storyboard
    except (ImportError, AttributeError):
        raise RuntimeError('Visual library has not been integrated') from None
    # Published W2 mapper is authoritative. Local mapping is for injected W1 test doubles only.
    step_ids = tuple(scene_mapper(analysis.model_dump())) if scene_mapper else tuple(
        analysis.steps[min(index*len(analysis.steps)//9, len(analysis.steps)-1)].step_id for index in range(9))
    if len(step_ids) != 9 or any(ref not in {step.step_id for step in analysis.steps} for ref in step_ids):
        raise ValueError('Invalid Visual scene mapping')
    async with asyncio.timeout(timeout_seconds):
        grid = await generate(analysis.model_dump())
        if not isinstance(grid, bytes) or not 0 < len(grid) <= 10*1024*1024:
            raise ValueError('Invalid visual grid output')
        panels = await asyncio.to_thread(split, grid)
        if len(panels) != 9 or any(not isinstance(p, bytes) or not 0 < len(p) <= 10*1024*1024 for p in panels):
            raise ValueError('Expected nine bounded PNG panels')
        if await quality_review(analysis.model_copy(deep=True), tuple(panels), step_ids) is not True:
            raise ValueError('Visual semantic quality review failed')
        return PreparedVisual(grid, tuple(panels), step_ids)


def configure_same_process_visual(*, integration_authorized=False):
    """Preparation only: call once at startup after the Visual W2 integration is authorized."""
    if not integration_authorized:
        raise RuntimeError('Visual integration is not authorized')
    try:
        service = public_module('service')
        adapter = public_module('openai_provider').OpenAIStoryboardProvider.from_env()
        service.configure_provider(adapter)
    except (ImportError, AttributeError):
        raise RuntimeError('Visual provider library has not been integrated') from None
