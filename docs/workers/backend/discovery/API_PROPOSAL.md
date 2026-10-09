# [CONTRACT] Additive product discovery

Coordinator accepted this proposal on 2026-10-09 in dispatch `ctx_34b6d97bd407`.
This document does not alter shared contract ownership. Existing v0.1 `/analyses`,
device enum, guide approval, visual jobs and verification behavior remain intact.

## Request

`POST /product-discoveries`, `multipart/form-data`:

| Field | Type | Limits |
|---|---|---|
| `photo` | Required file | Decodable JPEG/PNG; media type must match; <=10 MiB and <=20,000,000 pixels |
| `question` | Optional string; default empty | Trim whitespace; <=2,000 characters |
| `model_hint` | Optional string; default empty | Trim whitespace; <=200 characters; a hint cannot establish identity |

Photo processing reuses `BoundedRequest`, `read_photo` and `DecodePool`. Multipart
request ceiling is 10 MiB + 64 KiB. File/decode has the existing 15-second limit;
upload streaming has the existing 30-second deadline. Provider discovery then has
a separate 30-second total deadline, including queue waits. Upload plus discovery
can consequently exceed 30 seconds; the provider phase cannot.

## Response

HTTP 200. Every top-level and nested field is required. No confidence percentage,
device enum, analysis ID, repair steps or guide approval appear in this DTO.

```json
{
  "discovery_id": "server-generated-uuid",
  "status": "needs_more_information",
  "candidates": [],
  "missing_information": [
    "Upload a sharp close-up of the manufacturer and complete model label, plus an overall product photo."
  ],
  "mode": "live"
}
```

Candidate shape (illustrative only; not a real search result):

```json
{
  "manufacturer": "Dell",
  "model": "PowerEdge R750",
  "summary": "A descriptive product summary grounded in the cited search results.",
  "sources": [
    {
      "title": "Dell PowerEdge R750",
      "url": "https://www.dell.com/en-us/shop/poweredge-r750/spd/poweredge-r750",
      "retrieved_at": "2026-10-09T00:00:00Z"
    }
  ],
  "match_notes": ["Printed label and source support this product candidate."]
}
```

| Field | Type / bounds |
|---|---|
| `discovery_id` | Nonempty string, <=200 chars; UUID for provider-generated results |
| `status` | `candidate`, `needs_more_information`, `not_found` |
| `candidates` | 0–3 candidates; current conservative implementation returns at most one exact printed identity |
| `manufacturer`, `model` | Nonempty strings, <=200 chars |
| `summary` | Nonempty descriptive string, <=1,000 chars |
| `sources` | 1–3 per candidate |
| `title` | Nonempty string, <=200 chars; metadata title, else actual URL hostname |
| `url` | Public HTTP(S) URL, <=2,048 chars, derived from API source/citation metadata |
| `retrieved_at` | Server-assigned UTC ISO8601 timestamp when the source-bearing provider response was received |
| `match_notes` | 1–4 strings, each nonempty and <=1,000 chars |
| `missing_information` | 0–5 strings, each nonempty and <=1,000 chars |
| `mode` | `live` or `mock`; production adapter is live, test doubles explicitly mock |

`candidate` requires readable printed manufacturer + complete model, an exact
matching model in search output, and a consulted/cited source whose title or path
supports that complete model and whose title/hostname supports the manufacturer.
This remains a candidate, not independent real-device identification or approval.
Printed label ambiguity, model variants, missing/weak/unsafe sources or suspicious
procedural output produce `needs_more_information`. A completed single search
returning zero product matches produces `not_found`; that is a bounded-search result,
not proof that the product does not exist. Unreadable labels stop before web search.

The optional question is accepted and included in the cache identity; it does not
become a repair request or web instruction. Only printed identity terms are passed
to search. The hint goes only to the label-reading stage as untrusted context.

## Errors

Existing shape: `detail: {code, message, retryable}`. FastAPI missing-field errors
remain `detail` arrays.

| HTTP | Codes / meaning |
|---|---|
| 413 | Existing request/photo/pixel size codes |
| 415 | Existing media/decoding/type mismatch codes |
| 422 | `invalid_question`, `invalid_model_hint`, or FastAPI field validation |
| 503 | `discovery_unconfigured`, `discovery_provider_error` (includes unauthorized/exhausted paid budget) |
| 504 | Existing upload/decode timeouts or `discovery_timeout` |
| 499 | `request_cancelled` after ASGI client disconnect; upstream work is cancelled |

Upstream content, keys, environment values and provider exception text are not
included in error responses. There is no automatic retry or live-to-mock fallback.

## Provider, budget and privacy bounds

- Same `OpenAIResponsesProvider` instance, existing `OPENAI_API_KEY`, `OPENAI_MODEL`,
  `HOWLENS_PAID_CALLS_ENABLED`, `HOWLENS_MAX_PROVIDER_CALLS` and usage ledger.
  No new account budget or implicit authorization. Max configured attempts remains 20.
- At most two Responses requests per discovery: label OCR (<=700 output tokens),
  then required `web_search` (<=1,500 output tokens, low search context, max one tool
  call). The second stage gets no photo or user-supplied URL. Both use `store:false`.
- Failed, timed-out and cancelled attempts consume the existing ledger reservation.
  Search fees are not covered by the old text-token estimate; `estimated_usd_upper`
  becomes null after a search reservation instead of understating cost.
- Two discovery jobs in flight maximum. Sixteen bounded locks coalesce duplicate
  requests. Cache holds <=16 response DTOs for 300 seconds, keyed by SHA256 of
  photo hash + question + model hint + config version/model. No raw photo, question,
  hint, provider envelope or credentials are retained by cache; response may contain
  the printed manufacturer/model. Expired entries are purged on access.
- Source URLs must match actual `url_citation` or `web_search_call.action.sources`;
  model-authored URLs alone are discarded. Credentials, private/non-global IPs,
  local/special-use names, alternate numeric IP notation, backslashes, controls and
  unusual ports are rejected. This backend does not DNS-resolve or fetch source
  URLs; there is no server-side arbitrary-URL fetch or DNS-rebinding fetch path.
- Webpage instructions are untrusted. Strict DTOs exclude guide fields; server
  gates printed identity/source support, and rejects suspicious/procedural summary
  content. Natural-language guards are conservative, not a proof of semantic truth.

## Verified primary references

- [Responses web search](https://developers.openai.com/api/docs/guides/tools-web-search):
  `web_search`, required tool choice, consulted source include, low context, model
  support (including `gpt-4.1-mini`), and newer-tool guidance.
- [Images and vision](https://developers.openai.com/api/docs/guides/images-vision):
  Responses image input and data URLs.
- [GPT-4.1 mini](https://developers.openai.com/api/docs/models/gpt-4.1-mini): image
  input, Responses and structured output support.
- [Responses create reference](https://developers.openai.com/api/reference/python/resources/responses/methods/create):
  `max_tool_calls` request/response field.

Fetched on 2026-10-09. No local OpenAI SDK is installed; the existing implementation
uses pinned httpx REST. Runtime account/tool compatibility still requires a bounded
paid check; documentation and synthetic HTTP tests do not prove it.
