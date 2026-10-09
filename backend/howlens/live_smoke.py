"""One paid, synthetic-image analysis only after explicit coordinator budget approval.

Does not enable the public server paid switch or print images, prompts, keys or raw responses.
"""
import argparse
from io import BytesIO
import json
import os
from PIL import Image
from fastapi.testclient import TestClient
from .config import load_local_env
from .manual_catalog import load_registry
from .main import create_app
from .openai_provider import OpenAIResponsesProvider, conservative_review


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--authorized-one-call', action='store_true')
    parser.add_argument('--model', required=True)
    args = parser.parse_args()
    load_local_env()
    key = os.environ.get('OPENAI_API_KEY','').strip()
    if not args.authorized_one_call or not key:
        print(json.dumps({'key_present':bool(key),'attempted':False,'reason':'authorization_or_key_missing'}))
        return
    registry = load_registry()
    provider = OpenAIResponsesProvider(api_key=key, model=args.model, registry=registry,
                                      calls_authorized=True, max_output_tokens=1500, timeout_seconds=25)
    image = BytesIO()
    Image.new('RGB',(64,64),'gray').save(image,format='PNG')
    with TestClient(create_app(provider=provider, registry=registry, reviewer=conservative_review)) as client:
        result = client.post('/analyses', data={'device_id':'server',
                'question':'Synthetic blank-image API smoke only. Describe uncertainty. Use registered excerpts only if relevant; do not identify blank equipment or provide procedures.'},
                files={'photo':('synthetic.png',image.getvalue(),'image/png')})
    body = result.json()
    report = {'attempted':True,'model':args.model,'api_http_status':provider.last_http_status,
              'backend_http_status':result.status_code,'usage':provider.last_usage,
              'decision':body.get('decision'),'mode':body.get('mode'),
              'evidence_count':len(body.get('evidence',[])),'steps_count':len(body.get('steps',[])),
              'estimated_usd_upper':None,'billing_balance_verified':False}
    if provider.last_usage and args.model=='gpt-4.1-mini':
        report['estimated_usd_upper'] = round((provider.last_usage['input_tokens']*.4 +
                                             provider.last_usage['output_tokens']*1.6)/1_000_000,8)
    if result.status_code != 200:
        report['error_code'] = body.get('detail',{}).get('code')
    print(json.dumps(report))


if __name__ == '__main__':
    main()
