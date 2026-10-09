import asyncio
from io import BytesIO
from uuid import uuid4
import warnings
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from PIL import Image, UnidentifiedImageError
from .models import Analysis, Device, Verification, VisualJob
from .safety import Approval, ManualRegistry, sanitize
from .store import MemoryStore, StoredAnalysis

MAX_FILE = 10*1024*1024
MAX_PIXELS = 20_000_000
MAX_REQUEST = MAX_FILE + 64*1024


def fail(status, code, message, retryable=False):
    raise HTTPException(status, detail=dict(code=code, message=message, retryable=retryable))


class BoundedRequest:
    """Bound streaming multipart before parser spools private content to disk."""
    def __init__(self, app):
        self.app = app

    async def __call__(self, scope, receive, send):
        if scope['type'] != 'http':
            return await self.app(scope, receive, send)
        total = 0
        async def bounded_receive():
            nonlocal total
            try:
                message = await asyncio.wait_for(receive(), timeout=15)
            except TimeoutError:
                fail(504, 'upload_timeout', 'Upload timed out.', True)
            total += len(message.get('body', b''))
            if total > MAX_REQUEST:
                fail(413, 'request_too_large', 'Upload exceeds request limit.')
            return message
        # FastAPI handles HTTPExceptions raised during parsing.
        await self.app(scope, bounded_receive, send)


def decode(data):
    try:
        with warnings.catch_warnings():
            warnings.simplefilter('error', Image.DecompressionBombWarning)
            with Image.open(BytesIO(data), formats=['JPEG', 'PNG']) as image:
                if image.width*image.height > MAX_PIXELS:
                    fail(413, 'too_many_pixels', 'Photo exceeds 20MP.')
                fmt = image.format
                image.verify()
            with Image.open(BytesIO(data), formats=['JPEG', 'PNG']) as image:
                image.load()
        return fmt
    except (Image.DecompressionBombError, Image.DecompressionBombWarning):
        fail(413, 'too_many_pixels', 'Photo exceeds pixel limit.')
    except (UnidentifiedImageError, OSError, ValueError, SyntaxError):
        fail(415, 'invalid_photo', 'A decodable JPEG or PNG photo is required.')


async def read_photo(photo):
    try:
        if photo.content_type not in {'image/jpeg', 'image/png'}:
            fail(415, 'invalid_photo', 'JPEG or PNG is required.')
        data = await asyncio.wait_for(photo.read(MAX_FILE+1), timeout=15)
        if len(data) > MAX_FILE:
            fail(413, 'photo_too_large', 'Photo exceeds 10 MiB.')
        fmt = await asyncio.to_thread(decode, data)
        if {'JPEG':'image/jpeg', 'PNG':'image/png'}[fmt] != photo.content_type:
            fail(415, 'type_mismatch', 'Photo format does not match its media type.')
        return data
    finally:
        await photo.close()


def create_app(provider=None, registry=None, reviewer=None, timeout_seconds=30,
               max_analyses=16, max_bytes=64*1024*1024):
    app = FastAPI(title='HowLens', version='0.1.0')
    app.add_middleware(BoundedRequest)
    store = MemoryStore(max_analyses, max_bytes)
    app.state.store = store
    manuals = registry if registry is not None else ManualRegistry()
    slots = asyncio.Semaphore(2)
    visual_lock = asyncio.Lock()

    async def upstream(call):
        if provider is None:
            fail(503, 'provider_unconfigured', 'Analysis provider is not configured.', True)
        try:
            async with asyncio.timeout(timeout_seconds):
                async with slots:
                    return await call()
        except TimeoutError:
            fail(504, 'provider_timeout', 'Provider timed out; retry later.', True)
        except Exception:
            fail(503, 'provider_error', 'Provider could not return a valid result; retry later.', True)

    def stored_guide(analysis_id):
        saved = store.analyses.get(analysis_id)
        if saved is None:
            fail(404, 'analysis_not_found', 'Analysis was not found.')
        if saved.analysis.decision != 'guide' or saved.analysis.mode != 'live':
            fail(409, 'guide_required', 'A verified live guide is required.')
        return saved

    @app.get('/health')
    async def health():
        return dict(status='ok', mode='live')

    @app.post('/analyses', response_model=Analysis)
    async def analyses(device_id: Device = Form(...), question: str = Form(..., max_length=2000),
                       photo: UploadFile = File(...)):
        question = question.strip()
        if not 1 <= len(question) <= 2000:
            fail(422, 'invalid_question', 'Question must contain 1–2000 characters.')
        original = await read_photo(photo)
        async def analyze_and_review():
            a = Analysis.model_validate(await provider.analyze(device_id, question, original)).model_copy(deep=True)
            if a.device_id != device_id:
                raise ValueError('Device mismatch')
            a.analysis_id = str(uuid4())
            approval = await reviewer(a.model_copy(deep=True), original, question) if reviewer else Approval()
            if not isinstance(approval, Approval):
                raise ValueError('Invalid independent review')
            return sanitize(a, manuals, approval), approval
        a, approval = await upstream(analyze_and_review)
        store.put(StoredAnalysis(a.model_copy(deep=True), original, approval))
        return a

    @app.post('/analyses/{analysis_id}/visual', response_model=VisualJob, status_code=202)
    async def visual(analysis_id: str):
        saved = stored_guide(analysis_id)
        async with visual_lock:
            attempts = store.attempts.setdefault(analysis_id, [])
            if attempts:
                previous = store.jobs[attempts[-1]]
                if previous.status != 'failed' or len(attempts) >= 2:
                    return previous
            job = VisualJob(job_id=str(uuid4()), analysis_id=analysis_id, status='failed', image_url=None,
                            panels=[], error='Visual integration and semantic quality review are not configured.', mode='live')
            # W1 intentionally fails closed until independent visual package AND
            # semantic panel review/asset storage are integrated. Never call image
            # generation solely because it imports successfully.
            store.jobs[job.job_id] = job
            attempts.append(job.job_id)
            return job

    @app.get('/visual-jobs/{job_id}', response_model=VisualJob)
    async def get_job(job_id: str):
        job = store.jobs.get(job_id)
        if job is None:
            fail(404, 'job_not_found', 'Visual job was not found.')
        return job

    @app.post('/analyses/{analysis_id}/verification', response_model=Verification)
    async def verification(analysis_id: str, photo: UploadFile = File(...),
                           user_confirmation: str | None = Form(None, max_length=2000)):
        saved = stored_guide(analysis_id)
        after = await read_photo(photo)
        async def verify():
            v = Verification.model_validate(await provider.verify(saved.analysis.model_copy(deep=True),
                                           saved.photo, after, user_confirmation)).model_copy(deep=True)
            if v.analysis_id != analysis_id or v.mode != 'live':
                raise ValueError('Verification identity or mode mismatch')
            if not set(v.evidence_ids) <= {e.evidence_id for e in saved.analysis.evidence}:
                raise ValueError('Unknown verification evidence')
            v.limitations.append('Photos show visual changes only; they cannot establish safety, success or normal operation.')
            return v
        return await upstream(verify)

    return app


app = create_app()
