# Third-phone baseline / 57405bb QA checkpoint

[DONE] Samsung SM-F936N Android16/API36 installed both verified baseline and new blue/two-tab APKs; controlled offline error recovered to one actual live non-guide APK result. Phone USB health200 and CELLULAR publicHTTPS health200/protected401 both verified, scrubbed synthetic-only screenshots captured, and exact owned USB reverse mapping cleaned. Same Dispatch continues for integratedab20cc3 Home/auth delta with no additional paid calls; baseline evidence below is preserved independently.

Task task_49156d49810e / Dispatch ctx_15c1e6bdf26a. Device-only QA docs scope; no product edits/main merge/server restart/key/env reads or changes. Existing backend PID74518 stayed untouched during this checkpoint; later upstream upgrade is separately owned by Backend primary after API-idle handoff.

## Hardware / artifacts

- Exactly one USB serial R3CT80CZ2MW initially unauthorized; user replug/RSA approval yielded device at12:45:33KST. No key deletion/deauthorize-all/adb server kill.
- Existing SDK adb37.0.1-15733141; no download/admin/global/fullSDK installation. Manufacturer samsung, model SM-F936N, release16, API36, currentuser0. Cover904x2316; unfolded inner1812x2176.
- Read-only default pm query failed on protecteduser10 SecurityException; --user0 package query passed. No other profile/app data read/changed.
- Baseline source0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1: actual10341344bytes/SHA25688eb7eac202b15a32baf1ee61490083c42c8fd776fb85dd3ff2de8d4410122e2, one verified GET, install-r Success. Initial MOCK/live controls rendered; no baseline paid analysis.
- New blue source57405bb4811f67bb49ea779d883fd2937c98fd7d: actual15034933bytes/SHA2562b39251176178975fd0a9228da55a88903d879e7d2cc9fd4dcdb695d128abb25, one verified GET8679ms, install-r Success. Both kr.howlens.app/version0.1/code1/min26/target34; no uninstall/clear used for these updates.
- Synthetic exact d263f77 TEST-only-upload.png:68bytes/1x1PNG/SHA256431ced6916a2a21a156e38701afe55bbd7f88969fbbfc56d7fe099d47f265460, Pillow verify/load and on-phone byte/hash match. Only this file pushed to /sdcard/Download/HowLens-backend-qa-task49156/ and user explicitly selected it; no personal gallery/photo upload.

## Direct local human checks / APK request

Explicit newer user instruction allowed local human test conversation. Terminal renamed/revealed TEST · 이준영 · 서버 연결. Human replies recorded: two blue tabs normal; settings Demo visible/URL hidden; DemoOFF reveals URL; localhost18000 URL changed; exact testPNG selected; intentional offline confirmation shows server connection error. Camera function/permission/real-photo quality not tested in this network slot.

Request question preset: TEST-only 1x1 synthetic PNG. No procedures. Mode actual-analysis badge, device_id server/DellR750 selection; this is not real equipment identification. Removed only owned reverse mapping for the controlled offline case; user clicked once and saw localized connection error. No server/provider request reached that disconnected endpoint. Mapping restored, one normal retry touch at **2026-10-09T13:15:17.576658+09:00** yielded result screen after **9330ms UI-observed elapsed** (includes UI dump overhead, not pure provider latency).

Result: actual-analysis/live UI badge, **추가 정보가 필요해요 / needs_more_information**, returned observation matches synthetic question and3 missing-information points; no execution-step UI. No fake guide/safety/normal-operation claim. App does not expose raw HTTP status/serialized mode/steps here, so those wire fields were not independently intercepted. Exactly one accepted actual APK analysis in this checkpoint; no paid retry/image/verification call. Server20attempt cap unchanged; no billing/usage ledger read.

## Network / tethering facts

Host loopback +10.102.72.28 /health200/live. Phone direct privateLAN connection timed out3063ms; not proof of AP/client isolation. Exact phone ip route get10.102.72.28: devrmnet_data9 via192.0.0.7 src192.0.0.8; active defaultnetwork111 CELLULAR. PC defaultgateway10.102.72.240/interfaceen0/IPv410.102.72.28. PC api.openai.com DNS resolves, no-key TLSGET/models401 in466ms is reachability only, not authenticated model/balance validation. No SSID/password/key output.

Owned explicit-serial --no-rebind reverse tcp18000->tcp8000 provided phone HTTP200/statusok/modelive. Toyboxnc immediate stdin EOF initially caused empty replies; keeping input open2s produced actual200 in2091ms (includes intentional hold). One premature status subject PASS was corrected before success evidence. Host-address socket-spec experiment was rejected by adbCLI; only own map briefly removed and restored, not a server change. Exact map removed after completion; final reverse list empty, no remove-all/radio changes.

Phone systemcurl -q, CELLULAR, no credentials: public https://premium-proceed-hartford-published.trycloudflare.com/health200 in1525ms; unauth /visual-jobs/device-qa-no-auth401 in650ms. Both PASS/free; no additional paid analysis. PrivateLAN/USB transport, PC outbound internet and protected public authentication are separate layers. URL temporary and tunnel separately owned.

## Evidence / truthful failures

actual-request.json, network-summary.json, public-https.json and screenshots/57405bb-{offline-error,live-non-guide}.png. Screenshots show only synthetic blackpixel/testquestion/result, status/navigation bars removed and visually inspected; no personal content. Raw APKs/hierarchies/captures ignored locally, not staged.

Screenshot helper first used system Python withoutPIL, then encountered Android multi-display diagnostic prefix beforePNG; helpers stopped before any normal API touch. Fixed by using existing backendvenv and stripping347-byte non-image prefix, then cropping/checking1812x2176 app frame. No failed screenshot run counted as request success. Fold transition invalidated old coordinates; subsequent controls used fresh UI bounds. These were QA-tool limitations, not attributed to server/provider/app bugs.

Shared git index holds honored; baseline-only QA docs staged after explicit slot release, no others' product changes included. PDF collection mkdir-only side instruction paths ensured without overwrite: /Users/jymbook/HowLens/docs/assets/manuals/backend/ and active worktree counterpart, not automatic evidence registration.

## Remaining / continuation

Original+blue network goal complete; integratedab20cc3 Home/auth package delta continues under same IDs. No current API request in flight; backend primary may upgrade upstream as separately authorized, worker does not restart services. Existing user app data preserved for baseline updates; different-signer QA reinstall requires preserving testdata/evidence first under new exact-package authorization. Public authenticated APK/discovery, actual equipment/guide/camera/semantic image validation remain untested; real9userphotos not uploaded in this checkpoint.
