# Third phone 57405bb QA — active results

Task task_49156d49810e / dispatch ctx_15c1e6bdf26a. QA-only paths; no product edits/server restart/key/env changes. Git index hold acknowledged to Coordinator; write only this unstaged QA report until release.

## Hardware / artifacts

Initial unauthorized USB serial R3CT80CZ2MW became authorized device at12:45:33KST after user replug/approval; no adb key deletion/reconnect-all/server kill. Actual getprop: samsung, SM-F936N, Android16, API36, currentuser0. Initial cover display904x2316; user unfolded during layout check, inner UI1812wide. Read-only default pm package query hit protecteduser10 SecurityException; corrected --user0, package kr.howlens.app present; no profile/data changes.

Baseline source0e2aa94a93db5f2ab2cfc8f7e1a834b4e69eb6e1 APK10341344bytes/SHA25688eb7eac202b15a32baf1ee61490083c42c8fd776fb85dd3ff2de8d4410122e2; downloadedonce, verified, install-r Success. Baseline MOCK/live toggle and URL/question/device/gallery controls rendered; no baseline paid analysis performed.

New source57405bb4811f67bb49ea779d883fd2937c98fd7d APK15034933bytes/SHA2562b39251176178975fd0a9228da55a88903d879e7d2cc9fd4dcdb695d128abb25; downloadedonce in8679ms, verified, install-r Success preserving data. Visible terminal renamed/revealed TEST · 이준영 · 서버 연결 per explicit direct-local-human QA override. User reply: two blue tabs normal; setting Demo visible but URL hidden; after Demooff, user confirms server URL field appears. These are NEW APK findings, not attributed to baseline.

Synthetic only: exact d263f77 68byte1x1PNG, SHA256431ced6916a2a21a156e38701afe55bbd7f88969fbbfc56d7fe099d47f265460, verify/load PASS; pushed only to /sdcard/Download/HowLens-backend-qa-task49156/TEST-only-upload.png and scanned that file. No personal gallery photo selected/read/uploaded.

## Network evidence

Host /health loopback and10.102.72.28 both200/live; dedicated Uvicorn PID74518 remains running untouched. Device directLAN toyboxnc connect timed out3063ms. Preexisting reverse list empty; owned specific-serial --no-rebind tcp18000->tcp8000 mapping added.

Initial reverse netcat returned empty response due stdin EOF; one premature status subject PASS was explicitly corrected to Coordinator before success was claimed. Attempt to use tcp:127.0.0.1:8000 destination was rejected by adb37 CLI (Invalid destination port); only owned mapping was removed during that experiment and immediately restored as standard tcp18000->tcp8000. No other mappings/server changed.

Actual successful phone command keeps stdin open while reading response:
`adb -s R3CT80CZ2MW shell "(printf 'GET /health HTTP/1.1\\r\\nHost: localhost\\r\\nConnection: close\\r\\n\\r\\n'; sleep 2) | toybox nc -w 3 -W 3 127.0.0.1 18000"`.

Result HTTP/1.1 200 OK, serveruvicorn, body statusok/modelive, command2091ms (includes deliberate2s hold; not pure latency). Host service unchanged. Reverse mapping will be removed exactly after tests; no remove-all/radio changes.

## Pending active checks

User asked to change app server URL to http://127.0.0.1:18000/; awaiting direct reply. Synthetic exact-file picker, connection-error/offline recovery UI and exactly one actual paid analysis through NEW APK remain pending. No paid request in this task so far; backend20attempt cap unchanged. No unverified guide/camera/image-generation success claimed. No private screenshots captured; raw hierarchy files ignored and not delivered.

## Completed checkpoint

User confirmed localhost18000 address and exact syntheticfile; controlled offline confirmation produced connection error. One normal retry touch after restored mapping at13:15:17KST produced actual-analysis/non-guide screen in9330ms UI-observed; exact text preserved in actual-request.json, no raw HTTP interception claimed. PC outboundDNS/TLS401 and phone CELLULAR publicHTTPS health200/protected401 passed; exact map cleaned to empty. Screenshots cropped/visually checked synthetic only. Final baseline summary in REPORT; same Dispatch continues integratedab20cc3 delta without paid calls.
