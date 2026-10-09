"""Run exact composed snapshot tests with TCP disabled and explicit helper import paths."""
import os
from pathlib import Path
import socket
import sys

snapshot=Path(sys.argv[1]).resolve()
os.chdir(snapshot/'backend')
sys.path[:0]=[str(snapshot/'backend'),str(snapshot/'backend/tests'),str(snapshot/'backend/visual/tests')]
original=socket.socket.connect

def deny_tcp(self,address):
    if self.family in (socket.AF_INET,socket.AF_INET6):
        raise AssertionError('Release audit prohibits TCP calls')
    return original(self,address)

socket.socket.connect=deny_tcp
import pytest
raise SystemExit(pytest.main(sys.argv[2:]))
