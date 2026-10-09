"""Runtime-only immutable c462a29 launcher; no product source edits or key copy."""
import logging
import os
from pathlib import Path
import sys


if __name__ == '__main__':
    source = Path('/tmp/howlens-discovery-runtime-c462a29/backend')
    if not (source / 'howlens/config.py').is_file():
        raise RuntimeError('Prepared immutable patch source is missing')
    sys.path.insert(0, str(source))
    import howlens.config as config
    config.ENV_FILE = Path('/Users/jymbook/orca/workspaces/HowLens/feat-backend-foundation/backend/.env')
    path = '/tmp/howlens-discovery-c462a29-gates.log'
    descriptor = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_APPEND | os.O_NOFOLLOW, 0o600)
    os.close(descriptor)
    logger = logging.getLogger('howlens.discovery')
    logger.setLevel(logging.INFO)
    handler = logging.FileHandler(path, encoding='utf-8')
    handler.setFormatter(logging.Formatter('%(asctime)s %(message)s'))
    logger.addHandler(handler)
    logger.propagate = False
    import uvicorn
    uvicorn.run('howlens.config:configured_app', factory=True, host='0.0.0.0', port=8000,
                access_log=False, log_level='warning')
