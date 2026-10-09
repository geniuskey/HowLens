# User PDF intake: identity and observation evidence

Authorized exact source paths: coordinator relay `relay_aea122f0e0f3`.
Intake was interrupted by the user's P0 HTTPS deployment request, preserved at the
manifest checkpoint, and completed locally after HTTPS200/auth401 were verified.
No original was moved, added to Git or transmitted to an external service.

[manifest.json](manifest.json) records filename, original path, byte size, SHA256,
cross-checked physical page count, printed product/manufacturer/title/revision,
document-contained source references and unverified provenance/completeness limits.
All original hashes were rechecked unchanged after extraction. PyMuPDF1.28.2 and
pypdf6.19.0 independently agreed on physical page counts.

| Uploaded PDF | Identity / revision | Physical pages | Important distinction |
|---|---|---:|---|
| UM1724 Nucleo64 | STMicroelectronics MB1136 family, Rev17 Sept2025 | 91 | Multiple NUCLEO variants; exact photo label needed |
| WHP application manual | Logosol Wafer Handling Platform, DOC710010010; history10.4 Aug2021 | 94 | Body footer uses /313; original completeness unverified |
| 9b6e38 | Brooks Atmospheric Single-Arm family; Rev1/P-N127206 Sept2005 | 241 | ManualsLib export/Artisan watermark; not just ATM100 |
| be94f9 | Brooks MagnaTran7.1; MN-003-1600-00 Rev2.2 May2001 | 674 | ManualsLib export; generic manual is not robot-specific |
| SLVUB62B | TI TPS65988EVM, RevB Nov2020 | 63 | Emulation configuration does not establish a different hardware model |

[observation_inventory.json](observation_inventory.json) records one visually
reviewed figure page per document, 1-based physical PDF page, printed page, a short
exact caption quote, figure location, caption rectangles in PDF points and a
photo-observation question. Every quote was checked as an exact substring of the
local PDF text and located through PDF text geometry. Five selected pages were
rendered and visually inspected locally; whole-document layout was not certified.

This is intake evidence from actual uploaded PDFs, not synthetic device QA or actual
product-photo evaluation. The user's product photo has not arrived in this dispatch.
Public source references have not been independently fetched/verified in this intake;
mirror metadata and manufacturer-branded PDFs do not prove a current official copy.
Nothing was added to the runtime catalog, reviewed for executable actions, or
approved as a repair/safety guide. Observation questions require no equipment motion,
power change or manipulation.

Full extracted page text and preview images were temporary local work products, not
repository artifacts. Source PDFs remain in the authorized evaluation upload folder.
