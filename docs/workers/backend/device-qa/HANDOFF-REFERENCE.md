# Reference phone handoff — same Dispatch

Installed referencec503985 app SHA2563e8e61c0949c753b9d751c69b94d45549876093307ccd340c337c43b29958f4a/12942834bytes and test SHA2568356abbe7a9f417421c25d63f10cf040d3d3cde6d923d7ce60a8515b0a54617e/1011298bytes. Both assigneduser0 install-r Success. Previous integratedab20 app/test also verified/installed, after preserved QAuser0 privatebackup and explicitly authorized signer-change reinstall; no other apps/profiles touched.

Control facts for Coordinator:
- adb: /Users/jymbook/Library/Android/sdk/platform-tools/adb
- serial: R3CT80CZ2MW; user0; SamsungSM-F936N; Android16/API36
- package/activity: kr.howlens.app / kr.howlens.app.MainActivity
- test: kr.howlens.app.test / androidx.test.runner.AndroidJUnitRunner
- observed inner display1812x2176, cover904x2316; no font/rotation/radio changes
- last observed current screenHome; Profile accessible top-right/bottom내정보, then앱설정
- last Settings showedDemoON; maskedtoken field not yet focused, publicURL not yet configured by this worker
- no token read/injection/log/screenshot; auth readiness NOTconfirmed
- newcentralCamera immediate-preview/back/capture not yet validated; do not claim CameraReady or TESTOPEN

Actual Home first observed13:58:25.876314KST with nativehero, categorycards/recommendedexamplecards, five bottomlabels홈/가이드/촬영하기/AI도움/내정보. Prior13:54 capture was Guide tab and an initialHome status was corrected; originalguide image must not be delivered asHome screenshot. During fieldprep, Settings changed backtoHome without QA navigation, so all phone taps/screenshots/hierarchy reads PAUSED toavoid competinghuman/private-token control; Coordinator checkpoint requested for exclusiveprep or immediatehandoff.

Baseline realAPK one accepted USB non-guide request13:15 /9330msUI-observed remains valid and separate; source57405bb actualanalysisbadge, needs_more_information UI, noexecution-stepUI, synthetic68bytePNG only. Sourceab20 OfflineScreenTest7passed/8.976s using synthetic/offline fixtures before newer no-repeat policy; livePanels namedtest is fixtureUI, not real image generation. No additional paidcalls. CurrentAPIHOLD honored, idleACKs sent; latestprimary deployment/server controls belong to other owner. QA never restarted backend.

Artifacts local (private binaries/backups ignored, do not commit):
- docs/workers/backend/device-qa/artifacts/ — fourapp/test generations preserved; baseline/reference hashes recorded
- artifacts/qa-user0-before-ab20cc3.tar — 5632bytes/7entries, mode0600; no contents printed or shared
- screenshots/57405bb-offline-error.png and57405bb-live-non-guide.png — inspected synthetic-only scrubbed appframes
- raw/ — ignored UI diagnostics/captures; no further collection after private owner input
- actual-request.json / network-summary.json / public-https.json — baseline safe metadata
- baseline QA-only commit a9211bcf864ee6dd1caf60ffdaf397a8a35b0716; index was reportedempty

Owned reverse tcp18000 mapping was removed; final list empty atbaselinecompletion. No other mappings/adb servers killed. PhoneCELLULAR publicHTTPShealth200/protected401 confirmed free, privateLAN route rmnet_data9 explains timeout withoutclaimingAPisolation. Currentprotectedpublicbase supplied byCoordinator is https://premium-proceed-hartford-published.trycloudflare.com; token/user provisioning separately owned. Future realphoto submission needs explicit APIHOLD RELEASE, correct pathway (Profileproductfinder for unregistered household product), user-selectedscene and allocatedproviderattempts; no extra QA calls are made here.

Latest user policy: singleCoordinator synchronizedphone control, no repeatedpassed suites, no competingtaps, owner/private auth setup first. After worker_done, this worker idles and takes no further phone action.

## Final override / stop

Latest user/Coordinator removes per-phone manual provisioning in favor of centrally built nextAPK/preset/autoLIVE policy. ROOT TAKES OVER NOW; no further taps, screenshot/hierarchy reads, humanquestions, suites or paidcalls. CurrentR1 referenceapps preserved; lastknownscreenHome, Demo/auth configuration remainsunconfirmed; nextR2 install/configuration exclusively Root-owned. Same-PC .demo-token was NEVER read/copied/injected by thisworker; no ephemeral credentialfile was created, so none needs deletion. OpenAI/accountcredential ban unchanged. Baseline oneUSB/non-guide success is separate from R1camera/auth readiness and must not be relabeled as newR2/live recognition success.
