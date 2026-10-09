# HowLens product truth

Platform: android. Kotlin / Jetpack Compose / CameraX; Python FastAPI contract v0.1.
Audience: Korean-reading field-service personnel checking equipment from a photo and question. Exact field site, gloves, lighting and device class are not verified; design assumptions, not research findings.
Task: identify selected equipment, submit a photo/question, understand evidence and missing conditions, read only server-approved guide steps, inspect optional nine-panel explanation, submit verification photo, share public source-backed summary.
Equipment IDs: server = Dell PowerEdge R750; cobot = UR5e; ups = APC Smart-UPS (exact SKU pending).
Truth: guide / needs_more_information / stop. Non-guide has zero steps. Preconditions and source version/pages remain visible. Photos never prove safety or normal operation. mock is test-only and visually explicit; live means real analysis, never safety approval. No new API, equipment control, invented success, or API keys in client.
Preserve: camera and album input, device/question editing, loading/cancel, retries, connection configuration, demo scenarios, evidence, visual generation and allowed retry, verification, share behavior in ../RUNBOOK.md.
Scope: design handoff only; no Android edits or tested APK. User delegates direction choice and forbids additional approval/interview. PRODUCT/DESIGN persist here by explicit ownership instruction, not project root.

Latest user priority (coordinator relay, 2026-10-09): very little visible reading; photo-first, one obvious next action, no chip clouds, labels/eyebrows, explanatory walls or fake dashboard metrics. One small mode label and essential stop/needs-info reason remain; evidence progressively disclosed. Existing camera uses TakePicture intent, not continuous CameraX preview. Do not promise a live viewfinder in this handoff.

Visual authority: user-supplied team-lead HowLens reference relayed 11:52 KST. Primary #0052FF, cool gray #F3F4F6, restrained teal #14B8A6, scanner-square mark and HowLens wordmark. This supersedes initial teal concepts. Keep user minimal-reading priority.

Latest functional scope (11:54 KST coordinator relay): two real tabs 사진 분석 / 카메라. Photo tab keeps gallery/static-photo flow; camera tab adds local CameraX preview with deliberate still capture. Both use same photo API; no video streaming or auto-submit. This supersedes earlier no-tabs/optional-preview language. Camera prototype gate belongs to implementation coordinator.
