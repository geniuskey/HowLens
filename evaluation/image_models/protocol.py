"""Offline benchmark planning/scoring. No provider clients, credentials or paid execution."""
import argparse
import hashlib
import json
import math
from pathlib import Path

ROOT = Path(__file__).parent
CRITERIA = ('scene_alignment', 'topology_fidelity', 'arrows_labels', 'no_invented_action', 'visible_grid')


def require(ok, message):
    if not ok:
        raise ValueError(message)


def digest(value):
    return hashlib.sha256(json.dumps(value, sort_keys=True, ensure_ascii=False).encode()).hexdigest()


def cases(path):
    rows = [json.loads(line) for line in Path(path).read_text().splitlines() if line.strip()]
    require(len({r['case_id'] for r in rows}) == len(rows), 'duplicate case ID')
    for row in rows:
        require(row['sandbox_only'] is True, 'not sandbox-only')
        require(row['asset_basis'] in ('synthetic', 'authorized'), 'unapproved asset basis')
        require(len(row['scene_step_ids']) == 9, 'requires nine scene IDs')
        require(set(row['scene_step_ids']) <= {s['step_id'] for s in row['steps']}, 'unknown step ID')
        for asset in row['reference_assets']:
            require(asset['authorization_record'] and len(asset['sha256']) == 64, 'missing asset authorization/hash')
            require(Path(asset['path']).is_file(), 'reference missing')
            require(hashlib.sha256(Path(asset['path']).read_bytes()).hexdigest() == asset['sha256'], 'asset hash mismatch')
        require(row['asset_basis'] != 'authorized' or row['reference_assets'], 'authorized case requires references')
    return rows


def estimate(rate, usage):
    """USD; fixed output proxy OR measured image output tokens, never both."""
    total = 0.0
    for kind in ('text_input', 'cached_text_input', 'image_input', 'cached_image_input', 'text_output'):
        amount = usage.get(kind, 0)
        require(type(amount) in (int, float) and math.isfinite(amount) and amount >= 0, 'invalid token amount')
        price = rate.get(kind)
        require(price is not None or amount == 0, f'uncalibrated {kind}')
        total += amount * (price or 0) / 1e6
    if usage.get('image_output') is not None:
        amount = usage['image_output']
        require(type(amount) in (int,float) and math.isfinite(amount) and amount >= 0, 'invalid output tokens')
        require(rate.get('image_output') is not None, 'missing output rate')
        total += amount * rate['image_output'] / 1e6
    else:
        require(rate.get('output_image_proxy_usd') is not None, 'image output requires calibration tokens')
        count = usage.get('images', 1)
        require(type(count) is int and count >= 0, 'invalid image count')
        total += count * rate['output_image_proxy_usd']
    return round(total, 8)


def plan(rows, model, settings):
    output = []
    for case in rows:
        run_id = digest([case, model, settings])[:20]
        output.append({'schema_version':'1', 'run_id':run_id, 'case_id':case['case_id'],
            'track':case['track'],
            'model':model, 'settings':settings, 'prompt':case['prompt'], 'prompt_sha256':digest(case['prompt']),
            'case_sha256':digest(case), 'reference_assets':case['reference_assets'],
            'scene_step_ids':case['scene_step_ids'], 'text_sha256':digest(case['steps']),
            'criteria_version':'draft-awaiting-team-lead', 'sandbox_only':True,
            'status':'dry_run', 'attempt':1, 'paid_authorization':None,
            'measurement_source':'not_run', 'provider':None, 'model_snapshot':None,
            'seed':None, 'retry_reason':None, 'billing_evidence':None, 'panels':[],
            'latency_ms':None, 'actual_cost_usd':None, 'usage':None, 'output_path':None,
            'http_status':None, 'error_code':None, 'request_id':None, 'started_at_utc':None,
            'raw_dimensions':None, 'normalized_dimensions':None, 'normalization':None,
            'human_review':None, 'product_safety_approval':False})
    return output


def geometry(width, height, panels, scene_ids):
    require(type(width) is int and type(height) is int and 96 <= width and 96 <= height
            and width * height <= 20_000_000 and width % 3 == height % 3 == 0, 'invalid 3x3 geometry')
    require(len(panels) == 9 and len(scene_ids) == 9, 'nine panels required')
    w, h = width // 3, height // 3
    for index, panel in enumerate(panels):
        x, y = index % 3 * w, index // 3 * h
        require(type(panel['index']) is int and panel['index'] == index, 'row-major index mismatch')
        require(panel['step_id'] == scene_ids[index], 'step mapping mismatch')
        require(panel['box'] == [x,y,x+w,y+h], 'crop geometry mismatch')
    return {'container_geometry':'passed', 'scene_semantics':'requires_human_review'}


def score(case, result, image_path):
    from PIL import Image
    require(Path(image_path).stat().st_size <= 10*1024*1024, 'PNG exceeds 10 MiB')
    require(result['case_id'] == case['case_id'] and result['case_sha256'] == digest(case), 'case mismatch')
    require(result['sandbox_only'] is True and result['product_safety_approval'] is False, 'sandbox boundary violation')
    require(result['status'] == 'success', 'only successful outputs can be visually scored')
    with Image.open(image_path) as im:
        require(im.format == 'PNG' and getattr(im,'n_frames',1) == 1, 'expected still PNG')
        require(im.width * im.height <= 20_000_000, 'image too large')
        im.verify()
    with Image.open(image_path) as im:
        im.load()
        auto = geometry(im.width, im.height, result['panels'], case['scene_step_ids'])
    preserved = result['text_sha256'] == digest(case['steps'])
    review = result.get('human_review')
    verdict = 'pending_human_review'
    if review:
        require(review.get('reviewer') and review.get('reviewed_at_utc'), 'review identity required')
        ratings = review['ratings']
        require(set(CRITERIA) <= ratings.keys(), 'incomplete human review')
        require(all(type(ratings[k]) is int and ratings[k] in (0,1,2) for k in CRITERIA), 'invalid human rating')
        # Conservative candidate qualification, not a weighted ranking or product approval.
        verdict = 'candidate_pass' if all(ratings[k] == 2 for k in CRITERIA) else 'needs_revision'
        if ratings['no_invented_action'] < 2:
            verdict = 'reject_unsafe'
    if not preserved:
        verdict = 'reject_text_changed'
    return {**auto, 'text_preserved':preserved, 'verdict':verdict, 'product_safety_approval':False}


def summarize(rows):
    groups = {}
    for row in rows:
        if row['status'] == 'dry_run':
            require(row['latency_ms'] is None and row['actual_cost_usd'] is None, 'mock timing/cost must remain null')
            continue
        if row.get('measurement_source') != 'live':
            continue
        require(row['status'] in ('success','api_error','timeout','cancelled'), 'invalid result status')
        require(row['sandbox_only'] is True and row['product_safety_approval'] is False, 'invalid boundary')
        key = row['model'] + ':' + digest([row['settings'], row['track'], row['criteria_version']])[:12]
        groups.setdefault(key, []).append(row)
    result = {}
    for key, attempts in groups.items():
        successes = [r for r in attempts if r['status'] == 'success']
        times = [r['latency_ms'] for r in successes if r.get('latency_ms') is not None]
        require(all(type(t) in (int,float) and math.isfinite(t) and t >= 0 for t in times), 'invalid latency')
        unique = {(r['run_id'],r['attempt']) for r in attempts}
        require(len(unique) == len(attempts), 'duplicate attempt record')
        ids = {r['run_id'] for r in attempts}
        costs = [r['actual_cost_usd'] for r in attempts if r.get('actual_cost_usd') is not None]
        require(all(type(c) in (int,float) and math.isfinite(c) and c >= 0 for c in costs), 'invalid actual cost')
        result[key] = {'attempts':len(attempts), 'successful_latency_n':len(times), 'raw_success_latency_ms':times,
            'failed_attempts':[{'run_id':r['run_id'],'status':r['status'],'latency_ms':r.get('latency_ms')} for r in attempts if r['status'] != 'success'],
            'p50_ms':None, 'p95_ms':None,
            'api_error_rate':sum(r['status']=='api_error' for r in attempts)/len(attempts),
            'retry_run_rate':sum(any(r['run_id']==i and r['attempt']>1 for r in attempts) for i in ids)/len(ids),
            'known_cost_subtotal_usd':round(sum(costs),8),
            'actual_total_cost_usd':round(sum(costs),8) if len(costs)==len(attempts) else None}
        if len(times) >= 20:
            ordered=sorted(times)
            result[key].update(p50_ms=ordered[math.ceil(.50*len(times))-1], p95_ms=ordered[math.ceil(.95*len(times))-1])
    return {'live_groups':result, 'dry_run_count':sum(r['status']=='dry_run' for r in rows),
            'excluded_nonlive_count':sum(r['status']!='dry_run' and r.get('measurement_source')!='live' for r in rows),
            'statistics_policy':'n<20: raw only; otherwise nearest-rank percentiles of successes, failures separate'}


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('command', choices=['dry-run','summary','score','estimate'])
    p.add_argument('--cases', default=str(ROOT/'cases.jsonl'))
    p.add_argument('--models', nargs='+', default=['gpt-image-1.5','gpt-image-1-mini'])
    p.add_argument('--results');p.add_argument('--image');p.add_argument('--rate');p.add_argument('--usage')
    args=p.parse_args()
    if args.command=='dry-run':
        for model in args.models:
            for row in plan(cases(args.cases),model,{'size':'1024x1024','quality':'low','format':'png','concurrency':1,'retries':0}):
                print(json.dumps(row,ensure_ascii=False))
    elif args.command=='estimate':
        print(json.dumps({'estimated_usd':estimate(json.loads(Path(args.rate).read_text()),json.loads(Path(args.usage).read_text())), 'actual_usd':None}))
    else:
        rows=[json.loads(line) for line in Path(args.results).read_text().splitlines() if line.strip()]
        if args.command=='summary': print(json.dumps(summarize(rows),indent=2))
        else:
            require(len(rows)==1, 'score one result per file')
            case=next(c for c in cases(args.cases) if c['case_id']==rows[0]['case_id'])
            print(json.dumps(score(case,rows[0],args.image),indent=2))

if __name__=='__main__':
    main()
