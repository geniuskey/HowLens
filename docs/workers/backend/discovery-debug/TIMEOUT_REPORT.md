# [DONE] Routed discovery timeout investigation — 2026-10-09

Task `task_469080bcd6a2`, dispatch `ctx_0693a2b4302d`; investigated source `670dd10`. The existing discovery route correctly returns a retryable JSON 504 for the actual nested provider timeout path. No production router change is justified by the available evidence; added three focused regression cases instead.

## What the live evidence proves

Coordinator supplied: label stage requested `gpt-6-luna`, elapsed 8001 ms, no provider HTTP status, returned model unknown, usage absent. This matches the configured eight-second stage deadline expiring before the provider response. The smoke harness then encountered an HTTP error and failed to decode its body as JSON.

Coordinator correction `relay_e0928bb01829`: **the public HTTP status was not preserved; HTTP 500 was a hypothesis, not an observed fact**. No precise cause of a public 500 can therefore be established. The non-JSON body could have been produced beyond the owned route; no gateway defect is asserted without status/content-type or a traceback.

## Exact route behavior

`DiscoveryService.discover` wraps queue, cache and provider work in its existing 30-second deadline. The routed provider can hit its shorter label deadline first. `discovery_router.py` awaits that task, catches built-in TimeoutError, and raises the existing contract HTTPException with status 504. Its cleanup cancels the disconnect watcher and gathers both tasks with return_exceptions=True.

Reproduced through the real routed adapter, DiscoveryProvider, DiscoveryService and FastAPI TestClient with `raise_server_exceptions=False`, so an escaping server exception would be observable as an HTTP 500. The timed handler uses MockTransport and waits until cancellation; the eight-second live deadline is scaled to 20 ms. All cases returned content-type application/json and exactly:

```json
{"detail":{"code":"discovery_timeout","message":"Product discovery timed out; retry later.","retryable":true}}
```

Each case also asserts one attempted provider request, zero searches, no cached result, released service capacity, cancellation of in-flight work, and no upstream-detail leakage. Cases: routed label asyncio deadline, outer DiscoveryService deadline, httpx.ReadTimeout conversion.

## Verification and handoff

Command from `backend/`:

```sh
/tmp/howlens-main-validation-20261009/bin/python -m pytest tests/test_discovery_api.py::test_real_discovery_deadline_returns_retryable_json_and_releases_work -q
```

Result: **3 passed in 0.50 seconds**, one existing Starlette deprecation warning. Only `backend/tests/test_discovery_api.py` and this report changed. No paid calls, environment/key access, process changes, Android edits, runtime budget changes or Git mutations.

Root should preserve the actual public and direct-backend HTTP status, content-type and a short sanitized error-body classification in the next authorized smoke, before attempting JSON decoding. Compare them to locate any gateway/transport transformation. Runtime timeout tuning and deployment remain root-owned; this investigation does not require restarting the currently tested server.
