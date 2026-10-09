# W5 packaging recheck — 2026-10-09

**Result: packaging/discovery acceptance passed for the specified combined snapshot.** Default pytest discovery collected the full 75 tests without CLI import overrides; the non-editable wheel contains and imports the Visual package outside the source tree, and the installed public startup/mapper/split probe passed with nine synthetic panels. `pip check` found no broken requirements; unchanged runtime test suites were not rerun.

## Inputs and isolation

- Evaluation checkout: branch `Runixs/eval-foundation`, starting HEAD `d4cbc460ba4f5a267d76c2ae851edf440de99c24`.
- Backend source: commit `d167c43002c9eff02dc388455782102ed8734cd2`, paths `backend/howlens/`, `backend/tests/`, and `backend/pyproject.toml` only.
- Visual source: commit `63dd0f80155b120fcd9bd07f7fb3bf6cf75773dc`, path `backend/visual/` only.
- Disposable combined snapshot: `/tmp/howlens-w5-recheck-0HdjyM`; Python 3.14.2; separate `venv/` for editable source dependencies and `installed/` for the built non-editable wheel.
- Archives contained only the named tracked paths. Commands used `env -i` with a disposable `HOME`; no API key, `.env` file, live API, device, or product source was accessed. The synthetic probe blocks TCP and reports `paid_calls: 0`.

## Commands and results

From the evaluation repo root, the snapshot was composed with:

```sh
SNAPSHOT=/tmp/howlens-w5-recheck-0HdjyM
git archive d167c43002c9eff02dc388455782102ed8734cd2 backend/howlens backend/tests backend/pyproject.toml | tar -x -C "$SNAPSHOT"
git archive 63dd0f80155b120fcd9bd07f7fb3bf6cf75773dc backend/visual | tar -x -C "$SNAPSHOT"
python3 -m venv "$SNAPSHOT/venv"
env -i PATH=/opt/homebrew/bin:/usr/bin:/bin HOME="$SNAPSHOT" "$SNAPSHOT/venv/bin/python" -m pip install -e "$SNAPSHOT/backend[test]" -r "$SNAPSHOT/backend/visual/requirements.txt"
```

Dependency installation succeeded (pinned Backend requirements plus Visual Pillow/httpx constraints). The acceptance commands and observed results were:

```sh
cd /tmp/howlens-w5-recheck-0HdjyM/backend
env -i PATH=/opt/homebrew/bin:/usr/bin:/bin HOME=/tmp/howlens-w5-recheck-0HdjyM PYTHONDONTWRITEBYTECODE=1 /tmp/howlens-w5-recheck-0HdjyM/venv/bin/python -m pytest --collect-only -q
# PASS — 75 tests collected in 1.52s, exit 0; no CLI import-mode or test-path overrides.

mkdir -p /tmp/howlens-w5-recheck-0HdjyM/wheel
env -i PATH=/opt/homebrew/bin:/usr/bin:/bin HOME=/tmp/howlens-w5-recheck-0HdjyM /tmp/howlens-w5-recheck-0HdjyM/venv/bin/python -m pip wheel --no-deps /tmp/howlens-w5-recheck-0HdjyM/backend -w /tmp/howlens-w5-recheck-0HdjyM/wheel
# PASS — howlens_backend-0.1.0-py3-none-any.whl built; SHA-256 1cd0b58c356aa854b26919ef893283913cc985287f6853950c537ea5232490ba.

python3 -m venv /tmp/howlens-w5-recheck-0HdjyM/installed
env -i PATH=/opt/homebrew/bin:/usr/bin:/bin HOME=/tmp/howlens-w5-recheck-0HdjyM /tmp/howlens-w5-recheck-0HdjyM/installed/bin/python -m pip install /tmp/howlens-w5-recheck-0HdjyM/wheel/howlens_backend-0.1.0-py3-none-any.whl
# PASS — installed non-editably into a fresh venv.
env -i PATH=/opt/homebrew/bin:/usr/bin:/bin HOME=/tmp/howlens-w5-recheck-0HdjyM /tmp/howlens-w5-recheck-0HdjyM/installed/bin/python -m pip check
# PASS — No broken requirements found.
(cd /tmp && env -i PATH=/opt/homebrew/bin:/usr/bin:/bin HOME=/tmp/howlens-w5-recheck-0HdjyM PYTHONDONTWRITEBYTECODE=1 /tmp/howlens-w5-recheck-0HdjyM/installed/bin/python -I /tmp/howlens-w5-recheck-0HdjyM/backend/tests/installed_wheel_probe.py /tmp/howlens-w5-recheck-0HdjyM/installed /tmp/howlens-w5-recheck-0HdjyM/wheel/howlens_backend-0.1.0-py3-none-any.whl)
# PASS — wheel members, out-of-source installed imports, manual catalog, public startup/mapper/split boundary; 9 panels, paid_calls 0.
```

No command returned a failure. The collection command imports test modules to enumerate cases, but did not execute test bodies; the full 75 runtime tests were intentionally not repeated under this task. The boundary probe uses a synthetic provider/reviewer and proves package wiring and nine-panel mapping/splitting only, not real image generation, semantic correctness, or safety approval.

## Evidence and remaining scope

- `docs/assets/release-recheck/plain-collection.txt`: exact isolated collection command and concise result.
- `docs/assets/release-recheck/wheel-build.txt`, `wheel-install.txt`, `pip-check.txt`, `installed-probe.txt`: command outputs for build, fresh install, dependency check, and installed public boundary.
- `docs/assets/release-recheck/install.txt`: dependency installation output.
- No runtime suite rerun was needed because all requested changed packaging/discovery checks passed. No product files or shared contracts changed.
