"""Dependency-free contract v0.1 checks; structural validity is NOT safety approval."""
from urllib.parse import urlsplit


class Invalid(ValueError):
    pass


def require(condition, message):
    if not condition:
        raise Invalid(message)


def fields(obj, names, label):
    require(isinstance(obj, dict), f"{label}: expected object")
    require(set(names.split()) <= obj.keys(), f"{label}: missing required fields")


def string(value, label):
    require(isinstance(value, str) and bool(value.strip()), f"{label}: expected nonempty string")


def strings(value, label):
    require(isinstance(value, list), f"{label}: expected array")
    for item in value:
        string(item, label)


def array(value, label):
    require(isinstance(value, list), f"{label}: expected array")


def enum(value, allowed, label):
    require(isinstance(value, str) and value in allowed.split(), f"{label}: invalid enum")


def unique(items, key):
    ids = []
    for item in items:
        require(isinstance(item, dict) and key in item, f"{key}: missing ID")
        string(item[key], key)
        ids.append(item[key])
    require(len(ids) == len(set(ids)), f"{key}: duplicate ID")
    return set(ids)


def references(ids, known, label, nonempty=False):
    strings(ids, label)
    require(not nonempty or bool(ids), f"{label}: requires evidence")
    require(set(ids) <= known, f"{label}: unknown reference")


def asset(value, label):
    string(value, label)
    url = urlsplit(value)
    require(value.startswith('/visual-assets/') and not url.scheme and not url.netloc
            and not url.query and not url.fragment and '%' not in value
            and '\\' not in value and all(p not in ('.', '..', '') for p in value.split('/')[1:]),
            f"{label}: expected safe relative visual asset path")


def analysis(obj, fixture=False):
    fields(obj, 'analysis_id device_id decision observations evidence preconditions steps warnings missing_information mode', 'Analysis')
    string(obj['analysis_id'], 'analysis_id')
    enum(obj['device_id'], 'server cobot ups', 'device_id')
    enum(obj['decision'], 'guide needs_more_information stop', 'decision')
    enum(obj['mode'], 'live mock', 'mode')
    for key in ('observations', 'warnings', 'missing_information'):
        strings(obj[key], key)
    for key in ('evidence', 'preconditions', 'steps'):
        array(obj[key], key)
    evidence_ids = unique(obj['evidence'], 'evidence_id')
    unique(obj['preconditions'], 'precondition_id')
    unique(obj['steps'], 'step_id')
    for item in obj['evidence']:
        fields(item, 'evidence_id document_id document_version pdf_page printed_page section quote source_url', 'evidence')
        for key in ('document_id', 'document_version', 'section', 'quote', 'source_url'):
            string(item[key], key)
        require(type(item['pdf_page']) is int and item['pdf_page'] >= 1, 'pdf_page: expected positive integer')
        require(item['printed_page'] is None or isinstance(item['printed_page'], str), 'printed_page: expected string/null')
        url = urlsplit(item['source_url'])
        require(url.scheme in ('https', 'http') and bool(url.netloc), 'source_url: expected public URL shape')
        if fixture:
            require(item['document_id'].startswith('TEST-') and item['document_version'].startswith('TEST-')
                    and item['quote'].startswith('TEST ONLY:'), 'fixture evidence must be TEST-marked')
    for item in obj['preconditions']:
        fields(item, 'precondition_id description status required evidence_ids', 'precondition')
        string(item['description'], 'description')
        enum(item['status'], 'satisfied unsatisfied unknown', 'precondition status')
        require(type(item['required']) is bool, 'required: expected boolean')
        references(item['evidence_ids'], evidence_ids, 'precondition evidence_ids')
        if obj['decision'] == 'guide' and item['required']:
            require(item['status'] == 'satisfied', 'guide: required precondition not satisfied')
    if obj['decision'] != 'guide':
        require(obj['steps'] == [], 'non-guide steps must be empty')
    else:
        require(1 <= len(obj['steps']) <= 9, 'guide: requires 1–9 steps')
    for item in obj['steps']:
        fields(item, 'step_id description evidence_ids visual_hint', 'step')
        string(item['description'], 'description')
        string(item['visual_hint'], 'visual_hint')
        references(item['evidence_ids'], evidence_ids, 'step evidence_ids', nonempty=True)
    if fixture:
        require(obj['mode'] == 'mock', 'fixture cannot be live approval')
    return obj


def visual(obj, original):
    analysis(original)
    fields(obj, 'job_id analysis_id status image_url panels error mode', 'VisualJob')
    string(obj['job_id'], 'job_id')
    require(obj['analysis_id'] == original['analysis_id'], 'visual analysis_id mismatch')
    require(original['decision'] == 'guide', 'visual requires guide')
    enum(obj['status'], 'queued running completed failed', 'visual status')
    enum(obj['mode'], 'live mock', 'mode')
    require(obj['mode'] == original['mode'], 'visual mode mismatch')
    require(obj['error'] is None or isinstance(obj['error'], str), 'error: expected string/null')
    if obj['image_url'] is not None:
        asset(obj['image_url'], 'image_url')
    array(obj['panels'], 'panels')
    indices = []
    for panel in obj['panels']:
        fields(panel, 'index step_id image_url', 'panel')
        require(type(panel['index']) is int and 0 <= panel['index'] <= 8, 'panel index out of range')
        indices.append(panel['index'])
        require(isinstance(panel['step_id'], str) and panel['step_id'] in {s['step_id'] for s in original['steps']}, 'panel unknown step_id')
        asset(panel['image_url'], 'panel image_url')
    require(indices == sorted(set(indices)), 'panels must be unique and in row-major index order')
    if obj['status'] == 'completed':
        require(indices == list(range(9)), 'completed visual requires exactly 9 ordered panels')
        require(obj['image_url'] is not None and obj['error'] is None, 'completed visual requires image and no error')
    if obj['status'] == 'failed':
        string(obj['error'], 'failed visual error')
    return obj


def verification(obj, original):
    analysis(original)
    fields(obj, 'analysis_id result observations evidence_ids missing_information limitations mode', 'Verification')
    require(original['decision'] == 'guide', 'verification requires guide')
    require(obj['analysis_id'] == original['analysis_id'], 'verification analysis_id mismatch')
    enum(obj['result'], 'observed_change issue_remaining inconclusive', 'verification result')
    enum(obj['mode'], 'live mock', 'mode')
    require(obj['mode'] == original['mode'], 'verification mode mismatch')
    for key in ('observations', 'missing_information', 'limitations'):
        strings(obj[key], key)
    references(obj['evidence_ids'], {e['evidence_id'] for e in original['evidence']}, 'verification evidence_ids')
    return obj


def bundle(data):
    fields(data, 'synthetic_only notice analyses visual_jobs verifications', 'fixture bundle')
    require(data['synthetic_only'] is True, 'fixtures must declare synthetic_only')
    string(data['notice'], 'fixture notice')
    array(data['analyses'], 'analyses')
    unique(data['analyses'], 'analysis_id')
    originals = {}
    for item in data['analyses']:
        analysis(item, fixture=True)
        originals[item['analysis_id']] = item
    for key, validate in (('visual_jobs', visual), ('verifications', verification)):
        array(data[key], key)
        for item in data[key]:
            require(isinstance(item, dict) and isinstance(item.get('analysis_id'), str)
                    and item['analysis_id'] in originals, f'{key}: unknown analysis_id')
            validate(item, originals[item['analysis_id']])
    return sum(len(data[key]) for key in ('analyses', 'visual_jobs', 'verifications'))


def safety_cases(data):
    fields(data, 'notice cases', 'safety cases')
    string(data['notice'], 'notice')
    array(data['cases'], 'cases')
    unique(data['cases'], 'case_id')
    for case in data['cases']:
        fields(case, 'case_id device_id scenario expected_decisions expected_verification forbidden_behavior required_inputs available_inputs status', 'safety case')
        enum(case['device_id'], 'server cobot ups', 'device_id')
        for key in ('scenario', 'forbidden_behavior'):
            string(case[key], key)
        for key in ('expected_decisions', 'expected_verification', 'required_inputs', 'available_inputs'):
            strings(case[key], key)
        require(bool(case['required_inputs']), 'case requires input inventory')
        for value in case['expected_decisions']:
            enum(value, 'guide needs_more_information stop', 'expected decision')
        for value in case['expected_verification']:
            enum(value, 'observed_change issue_remaining inconclusive', 'expected verification')
        enum(case['status'], 'pending ready', 'case status')
        require(set(case['available_inputs']) <= set(case['required_inputs']), 'unexpected available input')
        if not set(case['required_inputs']) <= set(case['available_inputs']):
            require(case['status'] == 'pending', 'missing photos/equipment/evidence must remain pending')
    return len(data['cases'])
