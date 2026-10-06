"""Regression checks for shell orchestration, without a Docker daemon or ripgrep."""
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
DOCKER = r'''#!/usr/bin/env bash
set -eu
case "$1" in
 run) echo fixture-container ;;
 inspect) echo true ;;
 logs)
   # Bigger than a pipe buffer: grep must consume all logs with pipefail enabled.
   printf 'Done (1.0s)! For help, type "help"\n'
   printf '%080000d\n' 0
   [[ "${CASE:-ok}" != crash ]] || echo 'Encountered an unexpected exception'
   ;;
 port) echo '127.0.0.1:31234' ;;
 rm) touch "$FIXTURE_DIR/removed" ;;
 exec)
   case "$3" in
    mc-health|test|sh) : ;;
    cat)
      if [[ "$4" == /data/server.properties ]]; then
       printf 'online-mode=%s\r\nserver-port=25565\r\n' "${ONLINE_FIXTURE:-false}"
      else printf 'eula=true\r\n'; fi ;;
    rcon-cli)
      if [[ "$4" == *'time query'* ]]; then echo 'The time is 42'; elif [[ "$4" == *'validate-tracks'* ]]; then echo 'TRACKS_OK: 1000 rails'; elif [[ "$4" == *'locate entrance'* ]]; then echo 'Overworld: [8, ~, 8]'; elif [[ "$4" == *'locate '* ]]; then echo '/execute in metro-world:metro-world run tp @s 2 3 8'; else echo 'OK'; fi ;;
    *) exit 4 ;;
   esac ;;
 *) exit 5 ;;
esac
'''


class ContainerSmokeTests(unittest.TestCase):
    def run_fixture(self, case="ok", online="false"):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            binary = root / "bin"
            binary.mkdir()
            commands = {"docker": DOCKER,
                        "rg": '#!/bin/sh\necho "ripgrep must not be used" >&2\nexit 99\n',
                        "sleep": '#!/bin/sh\nexit 0\n',
                        "python3": '#!/bin/sh\necho \'{"version":{"protocol":767}}\'\n'}
            for name, content in commands.items():
                path = binary / name
                path.write_text(content)
                path.chmod(0o755)
            env = dict(os.environ, PATH=f"{binary}:{os.environ['PATH']}",
                       CASE=case, ONLINE_FIXTURE=online, FIXTURE_DIR=directory)
            result = subprocess.run(["bash", str(ROOT / "scripts/container-smoke-test.sh")],
                                    cwd=root, env=env, capture_output=True, text=True, timeout=10)
            self.assertTrue((root / "removed").exists(), "container cleanup was skipped")
            self.assertTrue((root / "build/container-check/server.log").exists())
            return result

    def test_success_without_ripgrep_and_with_large_logs(self):
        result = self.run_fixture()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("PASS:", result.stdout)

    def test_wrong_online_mode_fails_and_cleans_up(self):
        self.assertNotEqual(self.run_fixture(online="true").returncode, 0)

    def test_server_crash_fails_and_cleans_up(self):
        self.assertNotEqual(self.run_fixture(case="crash").returncode, 0)
