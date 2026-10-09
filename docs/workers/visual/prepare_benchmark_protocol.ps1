# Read only the coordinator-permitted public Evaluation files at the pinned commit.
$ErrorActionPreference = 'Stop'
git fetch origin refs/heads/Runixs/eval-foundation:refs/remotes/origin/Runixs/eval-foundation
if ($LASTEXITCODE -ne 0) { throw 'Evaluation fetch failed' }
$protocolDestination = 'backend/visual/.venv/eval-protocol'
New-Item -ItemType Directory -Force $protocolDestination | Out-Null
foreach ($protocolFile in @('protocol.py', 'cases.jsonl', 'run-result.schema.json')) {
    $protocolContent = git show "0234159c2a5b4cc9443e79de30e90ef5586661d2:evaluation/image_models/$protocolFile"
    if ($LASTEXITCODE -ne 0) { throw 'Pinned Evaluation file missing' }
    $protocolContent | Set-Content -Encoding UTF8 "$protocolDestination/$protocolFile"
}
