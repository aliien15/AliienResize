#!/usr/bin/env python3
"""Build and inspect the deployable JAR; shared by CI and the pre-push hook."""
import argparse
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def verify(directory):
    subprocess.run(
        ['mvn', '--batch-mode', '--no-transfer-progress', 'clean', 'verify'],
        cwd=directory, check=True,
    )
    subprocess.run(
        [sys.executable, str(directory / 'tests/verify-packaging.py')],
        cwd=directory, check=True,
    )


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--isolated', action='store_true',
                        help='Build a temporary copy without replacing local target files')
    args = parser.parse_args()
    if args.isolated:
        with tempfile.TemporaryDirectory(prefix='resize-pre-push-') as temporary:
            checkout = Path(temporary)
            shutil.copy2(ROOT / 'pom.xml', checkout / 'pom.xml')
            for directory in ('src', 'tests', 'libs', '.mvn'):
                if (ROOT / directory).exists():
                    shutil.copytree(ROOT / directory, checkout / directory)
            verify(checkout)
    else:
        verify(ROOT)


if __name__ == '__main__':
    main()
