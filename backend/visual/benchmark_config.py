"""Official Image API controls checked 2026-10-09; sandbox configuration only."""
from __future__ import annotations

DOCS_CHECKED = "2026-10-09"
API_REFERENCE = "https://developers.openai.com/api/reference/resources/images/methods/generate"
PRICING_SOURCE = "https://developers.openai.com/api/docs/pricing"
SUNBURST = "gpt-image-2.5-sunburst-2026-09-08"
FLARE = "gpt-image-2.5-flare-2026-09-08"
BASE_QUALITIES = ("low", "medium", "high")
NEW_QUALITIES = BASE_QUALITIES + ("xhigh", "max")
MODEL_QUALITIES = {
    "gpt-image-1": BASE_QUALITIES, "gpt-image-1-mini": BASE_QUALITIES,
    "gpt-image-1.5": BASE_QUALITIES, "gpt-image-1.5-2025-12-16": BASE_QUALITIES,
    "gpt-image-2": BASE_QUALITIES,
    "gpt-image-2-2026-04-21": BASE_QUALITIES,
    "gpt-image-2.5-sunburst": NEW_QUALITIES, SUNBURST: NEW_QUALITIES,
    "gpt-image-2.5-flare": NEW_QUALITIES, FLARE: NEW_QUALITIES,
    # Moving alias: explicitly selected only, no invented snapshot or xhigh support.
    "chatgpt-image-latest": BASE_QUALITIES,
}
PRESETS = ("legacy-low", "paired-sunburst", "paired-flare", "paired-mini",
           "quality-screen", "current-model-screen")
EFFORT_REASON = "Images generations API has no reasoning_effort parameter; use quality instead"
FIXED_REQUEST = {"n": 1, "size": "1024x1024", "output_format": "png",
                 "background": "opaque", "moderation": "auto"}
# Standard USD per million tokens, not Batch. Cached discounts deliberately not
# inferred: Image API usage cannot reliably attribute the documented cache hits.
TOKEN_RATES = {
    "gpt-image-1": (5, 10, 40), "gpt-image-1-mini": (2, 2.5, 8),
    "gpt-image-1.5": (5, 8, 32), "gpt-image-1.5-2025-12-16": (5, 8, 32),
    "gpt-image-2": (5, 8, 30),
    "gpt-image-2-2026-04-21": (5, 8, 30),
    "gpt-image-2.5-sunburst": (5, 8, 30), SUNBURST: (5, 8, 30),
    "gpt-image-2.5-flare": (5, 8, 30), FLARE: (5, 8, 30),
}
OUTPUT_ESTIMATES = {
    "gpt-image-1": dict(zip(BASE_QUALITIES, (.011, .042, .167))),
    "gpt-image-1-mini": dict(zip(BASE_QUALITIES, (.005, .011, .036))),
    "gpt-image-1.5": dict(zip(BASE_QUALITIES, (.009, .034, .133))),
    "gpt-image-2": dict(zip(BASE_QUALITIES, (.006, .053, .211))),
}


def request_parameters(model, quality="low", reasoning_effort=None):
    if (model not in MODEL_QUALITIES or quality not in MODEL_QUALITIES[model]
            or reasoning_effort is not None):
        raise ValueError("Unsupported Image API model/quality/effort configuration")
    return {"model": model, "quality": quality, **FIXED_REQUEST}


def comparison_specs(cases, preset="legacy-low", configuration=None):
    """Return at most six distinct case/model/quality requests, before credentials."""
    if configuration is not None:
        if preset != "legacy-low" or not isinstance(configuration, dict) or set(configuration) != {"runs"}:
            raise ValueError("Choose one preset or a bounded comparison configuration")
        runs = configuration["runs"]
    elif preset in {"paired-sunburst", "paired-flare", "paired-mini"}:
        model = {"paired-sunburst": SUNBURST, "paired-flare": FLARE,
                 "paired-mini": "gpt-image-1-mini"}[preset]
        runs = [{"case_id": c["case_id"], "model": model, "quality": q}
                for c in cases for q in ("low", "medium")]
    elif preset == "quality-screen":
        runs = [{"case_id": cases[0]["case_id"], "model": m, "quality": q}
                for q in BASE_QUALITIES for m in (SUNBURST, "gpt-image-1-mini")]
    elif preset == "current-model-screen":
        runs = [{"case_id": cases[0]["case_id"], "model": m, "quality": q}
                for q in BASE_QUALITIES for m in (SUNBURST, FLARE)]
    elif preset == "legacy-low":
        runs = [{"case_id": c["case_id"], "model": m, "quality": "low"}
                for c in cases for m in ("gpt-image-1.5", "gpt-image-1-mini")]
    else:
        raise ValueError("Unknown comparison preset")
    if not isinstance(runs, list) or not 1 <= len(runs) <= 6:
        raise ValueError("Comparison must contain 1-6 calls")
    seen, checked = set(), []
    for run in runs:
        if (not isinstance(run, dict) or set(run) - {"case_id", "model", "quality", "reasoning_effort"}
                or not {"case_id", "model", "quality"} <= set(run)
                or not all(isinstance(run[k], str) for k in ("case_id", "model", "quality"))
                or run["case_id"] not in {c["case_id"] for c in cases}):
            raise ValueError("Unsupported comparison request fields or fixture")
        request_parameters(run["model"], run["quality"], run.get("reasoning_effort"))
        key = (run["case_id"], run["model"], run["quality"])
        if key in seen:
            raise ValueError("Duplicate comparison request")
        seen.add(key)
        checked.append(dict(run))
    return checked


def cost_estimate(model, quality, usage=None):
    """A list-price estimate only; never a receipt or additive per-image + tokens."""
    base = {"gpt-image-2-2026-04-21": "gpt-image-2",
            "gpt-image-1.5-2025-12-16": "gpt-image-1.5"}.get(model, model)
    result = {"estimated_output_cost_usd": OUTPUT_ESTIMATES.get(base, {}).get(quality),
              "estimated_total_cost_usd": None, "actual_receipt_cost_usd": None,
              "billing_receipt": None, "pricing_checked": DOCS_CHECKED,
              "pricing_source": PRICING_SOURCE, "cached_discount_applied": False,
              "estimate_reason": "output table excludes input; no complete numeric usage yet"}
    if model.startswith("gpt-image-2.5-"):
        result["estimate_reason"] = "2.5 output token count not verified for this calculator state; await numeric usage"
    if not isinstance(usage, dict) or model not in TOKEN_RATES:
        return result
    detail = usage.get("input_tokens_details", {})
    text, image, output = detail.get("text_tokens"), detail.get("image_tokens"), usage.get("output_tokens")
    if not all(type(n) is int and n >= 0 for n in (text, image, output)):
        return result
    if usage.get("input_tokens") != text + image:
        result["estimate_reason"] = "incomplete or inconsistent input token details"
        return result
    text_rate, image_rate, output_rate = TOKEN_RATES[model]
    result.update(estimated_output_cost_usd=round(output * output_rate / 1_000_000, 8),
                  estimated_total_cost_usd=round((text * text_rate + image * image_rate + output * output_rate) / 1_000_000, 8),
                  estimate_reason="numeric usage at standard uncached list rates; billing receipt unavailable")
    return result
