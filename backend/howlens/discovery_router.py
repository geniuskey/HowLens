"""Discovery route reuses the v0.1 upload/decoding boundary, never analysis storage."""
import asyncio
from fastapi import APIRouter, File, Form, Request, UploadFile
from .discovery_models import ProductDiscovery


def discovery_router(service, decode_pool, read_photo, fail):
    router = APIRouter()

    @router.post('/product-discoveries', response_model=ProductDiscovery)
    async def discoveries(request: Request, photo: UploadFile = File(...),
                          question: str = Form(''), model_hint: str = Form('')):
        try:
            question, model_hint = question.strip(), model_hint.strip()
            if len(question) > 2000:
                fail(422, 'invalid_question', 'Question must contain at most 2000 characters.')
            if len(model_hint) > 200:
                fail(422, 'invalid_model_hint', 'Model hint must contain at most 200 characters.')
            original = await read_photo(photo, decode_pool)
        finally:
            await photo.close()
        if service is None:
            fail(503, 'discovery_unconfigured', 'Product discovery provider is not configured.', True)

        async def watch_disconnect():
            # Multipart is fully consumed. A blocking receive is cancelled when work
            # finishes; AnyIO's cancelled-scope polling strands the middleware's
            # asyncio.wait_for child receive under TestClient.
            while (await request.receive()).get('type') != 'http.disconnect':
                pass

        work = asyncio.create_task(service.discover(original, question, model_hint))
        disconnect = asyncio.create_task(watch_disconnect())
        try:
            done, _ = await asyncio.wait({work, disconnect}, return_when=asyncio.FIRST_COMPLETED)
            if disconnect in done:
                fail(499, 'request_cancelled', 'Discovery request was cancelled.')
            return await work
        except TimeoutError:
            fail(504, 'discovery_timeout', 'Product discovery timed out; retry later.', True)
        except asyncio.CancelledError:
            raise
        except Exception as error:
            from fastapi import HTTPException
            if isinstance(error, HTTPException):
                raise
            fail(503, 'discovery_provider_error', 'Product discovery could not return a valid result.', True)
        finally:
            for task in (work, disconnect):
                if not task.done():
                    task.cancel()
            await asyncio.gather(work, disconnect, return_exceptions=True)

    return router
