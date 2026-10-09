# W5 App contract audit — source review only

- Reviewed immutable App **0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1**, fetched from `origin/geniuskey/feat-ui-foundation`, 2026-10-09 KST.
- Delivery branch `Runixs/eval-foundation`, parent `e558424fa787d15ae0708ee315c9cc28e5d962a3`. No product edits, merges, device control, credentials, private images or AI calls.
- **Recommendation: App owner should fix F1 before integration approval.** F2–F4 require owner disposition and focused tests. All findings below are source-traced; none is an executed Android failure or claim of live equipment behavior.
- Read only explicitly authorized App files and local common contract. Exact file hashes/test names: [source inventory](../../assets/app-audit/W5-source-inventory.json).

## Findings and minimal owner reproductions

All paths below are relative to `android/app/src/main/java/kr/howlens/app/` at the SHA above. Fixture responses must be synthetic, local MockWebServer responses, never production guide approvals.

### F1 — High: old visual/verification completion attaches to a new analysis

**Proof:** `MainActivity.kt:110–111` leaves Analyze enabled while a visual or verification job is active; `busy` at line 41 excludes these jobs. `ui/AnalysisViewModel.kt:83–103` starts a new analysis without cancelling `visualTask` or `verificationTask`. Visual completion writes panels/images at lines 134/138, and verification completion writes at line 164, without comparing the current analysis ID or request generation. Their identity checks compare against captured analysis A only (lines 117/123/160), so do not protect a new analysis B. New-analysis success also leaves `visualLoading` and `verificationBusy` unchanged (94–96).

**Minimal sequence:** obtain synthetic guide A; start its visual job and hold completed response; tap Analyze again without editing inputs; return synthetic guide B with a different analysis ID/step; release A's completed nine-panel response and images. A's panels/images now enter B's UI, with labels resolved against B (`MainActivity.kt:181`). Repeat with held verification A: its observations appear below B. If A completes before B, B clears data but does not cancel A; if A never completes, B can retain busy state. This is a dependent-job lifecycle defect, not a claim that cooperative cancellation in `edit()` is broken.

**Owner action/test:** cancel dependent tasks on every new analysis and clear their state; bind all result/error writes to current generation/analysis identity. Add two delayed-response tests and a new-analysis-while-polling test; assert no old images, verification, error or busy state enters B. Existing unit tests cover editing/cancelling a fake analysis and cancelling one visual call, not this transition. Real blocker escalated to Coordinator.

### F2 — Medium: successful queued/running responses poll forever

**Proof:** `ui/AnalysisViewModel.kt:120–126` loops with `delay(1200)` and no elapsed deadline or poll budget. `Repository.kt` bounds each HTTP call to 60 seconds, but a server returning HTTP 200/running promptly forever never reaches that timeout. UI remains loading and continues requests until explicit cancellation; text remains available.

**Minimal sequence:** POST returns queued, every GET returns running with matching analysis/mode. Advance fake time beyond an owner-selected total job deadline; current source still polls. No automatic *generation* retry occurs; this is unbounded status polling, not a violation of the two-POST limit.

**Owner action/test:** define a total polling deadline/budget, stop polling with text retained and explicit user recovery. Existing success tests return completed immediately; no queued→running sequence or never-completes case is asserted. Do not silently choose a new contract timeout in Evaluation.

### F3 — Medium: invalid PNG signature-only response becomes unretryable visual success

**Proof:** `data/Repository.kt:153–159` accepts `Content-Type: image/png` and the eight-byte PNG signature without requiring a decodable image. `ui/AnalysisViewModel.kt:138` stores these bytes as successful images. `MainActivity.kt:173–185` enters the success branch for a nonempty map, decodes without validation, silently omits an image if decoding returns null, and does not expose the retry controls at lines 193–198. Compressed-byte limit (2 MiB) also does not bound decoded pixels; line 183 decodes full resolution on the UI path. No OOM occurrence is claimed.

**Minimal sequence:** completed nine-panel response; each asset responds with exactly hex `89504e470d0a1a0a` and PNG content type. Repository accepts all nine, then UI cannot decode them; textual guide remains, blank cards appear, and no image retry is offered. This Android decode outcome is pending execution on owner toolchain.

**Owner action/test:** validate dimensions and successful bounded decoding before marking visual success, route decode failure to retained-text error/retry state. Add the signature-only asset regression and a dimensions-bound test. Existing corrupt-content instrumentation test targets `PhotoLoader` (input photos), not downloaded panels; existing visual success uses a valid 1×1 PNG.

### F4 — Medium, conditional privacy hardening: share URL filter can retain credentials

**Proof:** `data/Models.kt:87–98` checks raw query names against a denylist but then copies the entire source URL. A synthetic `https://manual.example/doc?%74oken=SYNTHETIC_SECRET` has a raw key `%74oken`, not `token`; `https://manual.example/doc#access_token=SYNTHETIC_SECRET` has no query at all. Both meet the local filter and are copied to the share preview/chooser text. No actual secret exposure or malicious registered manual is asserted: the contract requires server-registered public sources, so this is a conditional failure of the client's extra credential filter.

**Owner action/test:** use canonical approved public source URLs, or consistently sanitize/reject credential-bearing queries/fragments before constructing the preview; retain support for genuinely public query links only through an explicit policy. Unit share test checks free-text omission and HTTP rejection. Instrumentation test checks a plain `?token=PRIVATE` fixture by comparing UI to `SharePreview.build` itself, not independently asserting secret absence. Add explicit expected payload assertions for encoded keys/fragments.

## Contract/state coverage actually inspected

| Area | Source evidence and limits |
|---|---|
| HTTP cancellation/bounds | Repository cancels OkHttp via `invokeOnCancellation`; callbacks check continuation activity; connect/read/write/call limits 10/45/30/60s, retries and redirects disabled, JSON/assets capped at 2 MiB. No live disconnect test run here. |
| Non-guide/mock gates | `Models.kt:43–50` blocks visible steps/visual for non-guide, mock, missing referenced step evidence and unsatisfied required conditions; repository gates visual/verification. Server remains responsible for real approval. |
| Visual mapping/retry/text | Requires exactly nine ordered indices 0–8 and each step ID in approved captured analysis; failure preserves analysis; attempt guard permits two user POSTs, no automatic generation retries. F1 defeats current-analysis association; F3 bypasses failure UI. |
| Asset origin/redirect | `Repository.kt:176–180` permits only `/visual-assets/` on configured base and rejects traversal, percent escapes, backslash, query, fragment; redirects disabled. Source tests cover absolute URL and traversal, not real 302 transport. |
| Verification | Serializable three-value enum; repository and VM reject foreign analysis/evidence IDs, VM requires live mode. Upload and evidence test asserts inconclusive and unknown reference rejection. No enumeration sweep or new-analysis overlap test. |
| Confirmation limit | Repository uses ≤2000 Unicode code points, but VM line 82 uses UTF-16 `take(2000)`: 2000 emoji are prematurely cut to 1000; a boundary surrogate pair can be split. Low-severity source mismatch for App follow-up, not an executed failure. |
| Share | Payload omits observations, quote, step instructions, analysis ID and photos; user preview before ACTION_SEND. F4 qualifies URL privacy claim. |
| Rotation/drafts | Activity obtains lifecycle ViewModel; pending camera URI uses `rememberSaveable`, supporting same-process recreation by design. No SavedStateHandle/persisted draft; process-death restoration is not implemented. `edit` clears analysis/dependent data but retains `confirmation`, so owner should decide whether an old operator note may carry to a different equipment analysis. No rotation/background/permission execution here. |

## Test review and actual commands

Read all **11 unit test bodies and 6 instrumentation test bodies**, not merely names. These are declaration counts, **not passed tests**. `malformedPanelsAreRejectedAndVisualCancellationPreservesGuide` tests malformed eight panels but never invokes cancellation. The separate cancellation test calls `takeRequest` once after analysis, which can consume the already-queued analysis request; it does not positively establish that the visual request was received before cancel, assert socket cancellation, or release an old delayed response.

Executed:

```sh
git status --short --branch
git fetch origin geniuskey/feat-ui-foundation
git cat-file -t 0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1
git ls-tree -r --name-only 0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1 android
git show 0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1:android/app/src/main/java/kr/howlens/app/ui/AnalysisViewModel.kt | nl -ba
# Repeated git show reads for only the authorized files in source inventory.
command -v java kotlinc gradle
java -version
```

Results: fetch succeeded; exact object is a commit; tree initially clean at e558424. Only `/usr/bin/java` stub found; `java -version` reports **Unable to locate a Java Runtime**. Standard Android Studio bundled JBR path absent, Gradle cache absent; SDK directory exists but does not make a JVM/toolchain available. No toolchain installed, Gradle invoked, APK built/installed, JVM tests or instrumentation executed. File hashes and test declarations extracted with Python standard library from exact `git show` bytes; this inventory validation is not a product test.

Owner follow-up after fixes: run `./gradlew :app:testDebugUnitTest` on App PC and focused delayed-response/decode/poll/share regressions; then `./gradlew :app:connectedDebugAndroidTest` with explicitly owned test device. Both commands are **unexecuted here**. Full install, camera/gallery permission denial, rotation/process death, network disconnection, real server interoperability, live guide semantics and hardware evaluation remain pending. No paid/live AI or device calls were made.
