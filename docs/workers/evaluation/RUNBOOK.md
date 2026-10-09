# W1-EVALUATION runbook

Run from the repository root with Python 3.10+; standard library only. Validated locally with Python 3.14.2. No dependencies, API keys, paid model calls, actual photos, or equipment are needed for offline checks.

```sh
PYTHONDONTWRITEBYTECODE=1 python3 evaluation/run.py
PYTHONDONTWRITEBYTECODE=1 python3 -W error::ResourceWarning -m unittest discover -s evaluation -p 'test_*.py' -v
```

The first command prints JSON with 11 validated synthetic responses, 21 validated case definitions, 21 pending cases, and both live evaluation fields `not_run`. Exit code 0 means these structural checks passed; it does not mean the product or an equipment procedure passed a safety evaluation. The second command runs 13 regression tests with mutation subcases and a loopback HTTP double.

To validate an intentionally broken or externally supplied **mock fixture bundle**:

```sh
python3 evaluation/run.py --fixtures /absolute/path/to/bundle.json
```

The bundle format is demonstrated in `evaluation/fixtures/contract.json`. A non-guide analysis containing steps must produce exit code 1 and `non-guide steps must be empty`. The regression suite creates that corruption in a temporary file, asserts the error and exit code, and removes the temporary file. It does not alter the checked-in fixture or expectations. `--cases` similarly selects a case-definition file.

## Optional live API health smoke — not executed for W1

After a backend URL is supplied, substitute it for the example address:

```sh
python3 evaluation/run.py --base-url http://127.0.0.1:8000 --timeout 5
```

This opt-in command validates fixtures first, then sends **only GET /health**. It checks HTTP 200, JSON content type, required status/mode fields, and the `live | mock` mode enum. The status string is reported verbatim because v0.1 defines no status enum; a structurally valid status is not a readiness guarantee. The report preserves the server's reported mode; a mock response never becomes a live model success. Redirects are rejected, reads are limited to 64 KiB, and timeout must be finite and positive. Failures return exit code 1. No POST requests, TEST evidence, guide fixtures, or images are sent to the API. The loopback tests cover both reported modes, malformed response fields, HTTP 503, and rejected redirects; they are synthetic transport tests, not a live backend evaluation.

Analysis upload, visual generation/polling, retry behavior, verification upload, and HTTP 404/409/413/415/422/504 integration checks are not implemented in this health smoke. They require a later integration task with actual input artifacts and authorized model usage. Do not use the mock guide to request image generation.

## Fixture and safety-case boundaries

- `contract.json`: three analyses (guide, needs_more_information, stop), five visual states (queued, running, completed, generation failure, quality failure), and three verification results.
- Every response is mock. The guide has one fictional display-only step and TEST-marked evidence. Its `example.invalid` source and `/visual-assets/TEST-*` paths are intentional nonexistent placeholders; no supporting PDF or images are supplied. The nine completed panels repeat the single stored step, which checks panel order and references without inventing nine procedures.
- `safety_cases.json`: seven scenarios for each of server/Dell PowerEdge R750, cobot/UR5e, and ups/APC Smart-UPS: blur, equipment mismatch, ambiguity, lack of evidence, prompt injection without supporting evidence, identified danger, and unclear before/after photos. Expected decision sets are fixed in the file; danger requires stop, and an unclear comparison requires inconclusive.
- All 21 cases remain pending because actual equipment identification, photos, and registered evidence are absent. Before/after cases additionally require real before/after photos and a separately approved live guide. Definition validation does not execute the cases. No case is marked passed.

## Validator scope and local profile

`evaluation/validator.py` can validate Analysis, VisualJob, or Verification dictionaries independently. It checks required fields, types/enums, unique identifiers, positive integer PDF pages, evidence references, guide step count, required preconditions, non-guide step exclusion, corresponding analysis IDs/modes, relative asset paths, panel indices/order/references, and verification evidence references. Bundle validation additionally enforces mock mode and TEST evidence markings. Unknown extra keys are tolerated.

The evaluation profile requires nonblank identifier/content strings, a completed visual with an image and no error, a failed visual with a nonblank error, and ordered unique panels even while incomplete. Asset paths reject encoded components, dot segments, queries, fragments, and external addresses. These are explicit conservative fixture/consumer checks, not edits to the shared contract; escalate a producer conflict instead of quietly relaxing them. Missing safety-case inputs cannot be marked ready.

Structural checks cannot establish that a URL is a registered public source, a quotation exactly matches a PDF, a page belongs to the selected equipment, evidence supports a procedure, observed prerequisites are true, or images semantically match steps. The failed-visual regression proves the local validator preserves its original input text, not that an untested backend/app preserves text. These require backend validation and subsequent real-input evaluation. Photograph comparisons cannot certify safety, successful repair, or normal operation.
