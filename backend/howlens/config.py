"""Explicit local env loader; diagnostics never return secret values or model text."""
from pathlib import Path
import os
from dotenv import load_dotenv
from .manual_catalog import load_registry
from .openai_provider import OpenAIResponsesProvider, conservative_review

ENV_FILE = Path(__file__).resolve().parents[1] / '.env'


def load_local_env():
    load_dotenv(ENV_FILE, override=False)


def readiness():
    load_local_env()
    return {'key_present':bool(os.environ.get('OPENAI_API_KEY','').strip()),
            'model_configured':bool(os.environ.get('OPENAI_MODEL','').strip()),
            'paid_calls_enabled':os.environ.get('HOWLENS_PAID_CALLS_ENABLED') == 'true',
            'manual_entries':sum(len(load_registry().excerpts(device)) for device in ('server','cobot','ups')),
            'independent_physical_review_ready':False,
            'visual_integrated':False}


def configured_app():
    from .main import create_app
    load_local_env()
    registry = load_registry()
    key = os.environ.get('OPENAI_API_KEY','').strip()
    model = os.environ.get('OPENAI_MODEL','').strip()
    provider = None
    if key and model:
        provider = OpenAIResponsesProvider(api_key=key, model=model, registry=registry,
                    calls_authorized=os.environ.get('HOWLENS_PAID_CALLS_ENABLED') == 'true',
                    max_calls=int(os.environ.get('HOWLENS_MAX_PROVIDER_CALLS','') or '0'),
                    ledger_path=ENV_FILE.parent / '.provider-usage.json')
    return create_app(provider=provider, registry=registry, reviewer=conservative_review)


if __name__ == '__main__':
    import json
    print(json.dumps(readiness()))
