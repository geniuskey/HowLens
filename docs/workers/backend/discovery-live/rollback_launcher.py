"""Deployment-only rollback helper; preserves canonical key/config/usage paths.

Run only on an explicit rollback decision after stopping the candidate upstream.
This does not modify product code, reset the ledger, or print environment values.
"""
import sys
from pathlib import Path


if __name__ == '__main__':
    source = Path('/tmp/howlens-discovery-rollback-d977170/backend')
    if not (source / 'howlens/config.py').is_file():
        raise RuntimeError('Prepared rollback source is missing')
    sys.path.insert(0, str(source))
    import howlens.config as config
    config.ENV_FILE = Path('/Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation/backend/.env')
    import uvicorn
    uvicorn.run('howlens.config:configured_app', factory=True, host='0.0.0.0', port=8000,
                access_log=False, log_level='warning')
