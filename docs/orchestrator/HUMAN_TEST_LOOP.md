# Human test rounds

User instruction 2026-10-09 13:54–13:55: one Coordinator, three synchronized phones, focused checks only. `DEVICE_ROUND.json` is the current synchronization record; do not put secrets in it.

1. SETUP/UPDATE: Coordinator announces the brief device-control window. Prepare one immutable APK from an identified commit, record SHA256. Through each owner PC authenticated Orca/SSH transport, verify artifact SHA, exact adb serial/user and use install -r. No automatic uninstall or data clearing. APK signing is now from Kim Taewan for all three phones.
2. Record actual install receipt for EACH device. A disconnected/failed device stays pending; do not claim all3 synchronized.
3. App connection state currently lives in memory and is cleared by process replacement. Restore real mode, public base URL, and masked demo bearer after an update; never put the OpenAI key in an app or an APK. Demo bearer provisioning uses recipient-only encryption or same-owner local memory, no plaintext chat/log.
4. TEST OPEN: announce APK/source and server version. All AI device controllers idle. People test capture -> preview -> actual server result, with registered device analysis or unregistered product discovery as appropriate. No background automation touches phones.
5. Feedback here: person/device + action + observed result + expected result; screenshot optional. Coordinator records issue and gives scoped product Worker a reproduction.
6. Run only new/changed behavior checks and build/install checks. Previously passed baseline suites are not repeated without a concrete affected failure. Prepare next immutable artifact, then announce UPDATE and repeat.

During SETUP, camera-only readiness and authenticated end-to-end readiness are separate. Health200, build success or an APK screenshot does not prove actual analysis success. Do not force unknown household products into a registered Dell/UR/APC workflow.
