"""Run with fresh installed-venv/python -I, outside the source tree; offline only.

Usage: python -I installed_wheel_probe.py INSTALLED_VENV WHEEL_PATH
Tests the installed public Backend/Visual boundary, not live semantic approval.
"""
import asyncio
from io import BytesIO
import json
import os
from pathlib import Path
import socket
import sys
import zipfile


def main():
    prefix = Path(sys.argv[1]).resolve()
    wheel = Path(sys.argv[2])
    assert not os.environ.get('OPENAI_API_KEY'), 'Release probe must not inherit credentials'
    original_connect = socket.socket.connect
    def offline(self, address):
        if self.family in (socket.AF_INET, socket.AF_INET6):
            raise AssertionError('Installed-wheel probe prohibits TCP')
        return original_connect(self, address)
    socket.socket.connect = offline
    with zipfile.ZipFile(wheel) as archive:
        names = set(archive.namelist())
        required = {'visual/__init__.py','visual/service.py','visual/splitter.py',
                    'visual/openai_provider.py','howlens/manual_sources.json'}
        assert required <= names
        assert not any(name.startswith('visual/tests/') or '/.env' in name for name in names)
    import howlens
    import visual.service as service
    import visual.splitter as splitter
    import visual.openai_provider as adapter
    from howlens.manual_catalog import load_registry
    from howlens.models import Analysis
    from howlens.safety import Approval
    from howlens.store import StoredAnalysis
    from howlens.visual_boundary import configure_same_process_visual, generate_reviewed_assets
    from PIL import Image
    for module in (howlens, service, splitter, adapter):
        assert Path(module.__file__).resolve().is_relative_to(prefix), module.__name__
    assert len(load_registry().excerpts('server')) == 2
    class Synthetic:
        async def generate_png(self, *, prompt):
            buffer = BytesIO()
            Image.new('RGB',(96,96),'gray').save(buffer,format='PNG')
            return buffer.getvalue()
    original_factory = adapter.OpenAIStoryboardProvider.from_env
    adapter.OpenAIStoryboardProvider.from_env = classmethod(lambda cls: Synthetic())
    try:
        configure_same_process_visual(integration_authorized=True)
    finally:
        adapter.OpenAIStoryboardProvider.from_env = original_factory
    analysis = Analysis.model_validate(dict(analysis_id='offline-only',device_id='server',decision='guide',
        observations=[],evidence=[dict(evidence_id='e1',document_id='synthetic',document_version='test',
        pdf_page=1,printed_page=None,section='test',quote='Synthetic inspection only.',
        source_url='https://example.org/synthetic')],preconditions=[],steps=[dict(step_id=f's{i}',
        description=f'Synthetic scene {i}',evidence_ids=['e1'],visual_hint='') for i in range(3)],
        warnings=[],missing_information=[],mode='live'))
    saved = StoredAnalysis(analysis,b'',Approval(True,True,True,set(),{'s0','s1','s2'},'offline synthetic review'))
    async def review(a, panels, ids):
        assert ids == ('s0','s0','s0','s1','s1','s1','s2','s2','s2')
        assert len(panels) == 9
        return True  # Synthetic boundary fixture, not real semantic review.
    result = asyncio.run(generate_reviewed_assets(saved,calls_authorized=True,quality_review=review))
    assert len(result.panels) == 9
    print(json.dumps({'wheel_members':'PASS','out_of_source_installed_imports':'PASS',
                      'manual_catalog':'PASS','public_startup_mapper_split_boundary':'PASS',
                      'panels':len(result.panels),'paid_calls':0}))


if __name__ == '__main__':
    main()
