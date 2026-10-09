# Image quality controls — checked 2026-10-09

The current [generation API reference](https://developers.openai.com/api/reference/resources/images/methods/generate) lists these exact model strings:

| Family | Exact IDs | Quality controls |
| --- | --- | --- |
| Sunburst | `gpt-image-2.5-sunburst`, `gpt-image-2.5-sunburst-2026-09-08` | low, medium, high, xhigh, max, auto |
| Flare | `gpt-image-2.5-flare`, `gpt-image-2.5-flare-2026-09-08` | low, medium, high, xhigh, max, auto |
| Image 2 | `gpt-image-2`, `gpt-image-2-2026-04-21` | low, medium, high, auto |
| Earlier baselines | `gpt-image-1.5`, `gpt-image-1`, `gpt-image-1-mini` | low, medium, high, auto |
| Moving alias | `chatgpt-image-latest` | low, medium, high, auto; do not infer its snapshot |

The [1.5 model page](https://developers.openai.com/api/docs/models/gpt-image-1.5) also lists `gpt-image-1.5-2025-12-16`, included with the same baseline controls/rates. The bounded runner excludes `auto` to keep quality fixed and reproducible. DALL-E 2/3 appear as retired compatibility entries and are excluded. Documentation support does not establish access for an individual account. Earlier model pages now mark 1/1.5/mini deprecated; they remain explicit baseline options, with provider failures recorded rather than substitutions.

[Sunburst](https://developers.openai.com/api/docs/models/gpt-image-2.5-sunburst) emphasizes editing precision; [Flare](https://developers.openai.com/api/docs/models/gpt-image-2.5-flare) emphasizes fast everyday generation. These descriptions are vendor positioning, not measured HowLens quality or speed results.

The Images generation request schema has no `reasoning_effort`. Record it as null with the unsupported-parameter reason and send no effort field. Quality controls rendering; do not relabel quality as reasoning effort. The [edit reference](https://developers.openai.com/api/reference/resources/images/methods/edit) exposes input fidelity only for selected older models; this generation-only runner has no image inputs and omits it. Reference fidelity is unscored and not evaluable with the pinned no-reference fixtures.

The fixed request is `n=1`, `size=1024x1024`, `output_format=png`, `background=opaque`, `moderation=auto`, with explicit model and quality. PNG is retained for comparison; the [guide](https://developers.openai.com/api/docs/guides/image-generation) notes JPEG can be faster, which would require a separate controlled comparison. Latency includes HTTP, response transfer and decoding; reservation and disk/image normalization work is excluded. Each request has a 120-second total timeout in the selected command; timeout does not prove the provider stopped billing.

## Prices and receipts

Use standard, not Batch prices. [Current pricing](https://developers.openai.com/api/docs/pricing) gives both 2.5 families USD5 per million text-input tokens, USD8 per million image-input tokens and USD30 per million image-output tokens. The guide calculator renders a low example, while the 2.5 model pages warn that the Image 2 calculator does not estimate 2.5 token consumption. Its exact selected calculator state was not verified, so all 2.5 pre-run output estimates remain null; Image 2 output counts are never reused for 2.5. Complete numeric usage supplies a post-response list-price estimate. The guide says response usage cannot verify cache hits, so the runner applies no inferred cache discount.

| Earlier baseline | Text input / image input / image output USD per million | Square output estimate low / medium / high USD |
| --- | --- | --- |
| [Image 1](https://developers.openai.com/api/docs/models/gpt-image-1) | 5 / 10 / 40 | .011 / .042 / .167 |
| [Image 1.5](https://developers.openai.com/api/docs/models/gpt-image-1.5) | 5 / 8 / 32 | .009 / .034 / .133 |
| [Mini](https://developers.openai.com/api/docs/models/gpt-image-1-mini) | 2 / 2.5 / 8 | .005 / .011 / .036 |
| Image 2 | 5 / 8 / 30, same standard image-token rates described by guide | .006 / .053 / .211 |

The per-image output estimate excludes input costs. After complete usage arrives, compute a single token estimate instead of adding the output table again. Incomplete usage, alias pricing, unavailable receipts and human scores remain null with reasons. `actual_cost_usd`, `actual_receipt_cost_usd`, and billing evidence stay null: the Image API response is not a billing receipt. The Backend account owner can reconcile organization billing separately using request IDs; do not invent request-level allocation from an aggregate bill.

## Evidence lanes and production

This comparison measures generated guide-output illustrations on synthetic server/cobot/UPS fixtures. It does not identify input devices or score real photo recognition. The separately collected web-photo corpus and the nine user appliance photos belong to the recognition lane, including the unresolved coffee-machine model; no photos are regenerated or manual matches fabricated here. The two earlier ImageGen samples are synthetic illustrations, not measured API outputs.

For production, first validate the guide and its evidence/preconditions through the existing approval gate. Preserve source-manual figures and their document/version/page association through deterministic composition. Supplementary generated illustrations are limited to missing figures and require human review; generation must not replace verified figures or add procedures. This benchmark adds no production route or composition implementation and cannot grant production approval. A three-case exploratory pilot yields no actual winner or defensible p95.
