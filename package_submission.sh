#!/usr/bin/env bash
set -euo pipefail
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
python3 - "$PROJECT_DIR" "${1:-}" <<'PY'
from datetime import datetime
from pathlib import Path
import sys
from zipfile import ZipFile, ZIP_DEFLATED

root = Path(sys.argv[1])
output = Path(sys.argv[2]) if sys.argv[2] else root / f'bookstore-hw4-source-{datetime.now():%Y%m%d_%H%M%S}.zip'
if not output.is_absolute():
    output = root / output
output.parent.mkdir(parents=True, exist_ok=True)
# Explicit source allowlist; dependency directories and build output are never traversed.
entries = ['.sdkmanrc', 'README.md', 'package_submission.sh', 'backend/pom.xml',
           'backend/mvnw', 'backend/mvnw.cmd', 'backend/.mvn/wrapper/maven-wrapper.properties',
           'backend/README_ARCH.md', 'backend/src', 'frontend/src', 'frontend/public',
           'frontend/package.json', 'frontend/package-lock.json', 'frontend/index.html',
           'frontend/vite.config.js', 'frontend/eslint.config.js', 'frontend/DESIGN_PRINCIPLES.md',
           'database', 'docs', 'scripts']
files = []
for entry in entries:
    candidate = root / entry
    if not candidate.exists():
        continue
    for path in ([candidate] if candidate.is_file() else sorted(candidate.rglob('*'))):
        if not path.is_file() or path.is_symlink():
            continue
        relative = path.relative_to(root)
        if set(relative.parts) & {'node_modules', 'target', 'dist', '__pycache__', '.git'}:
            continue
        if path.suffix.lower() in {'.jar', '.class', '.zip', '.log', '.pyc', '.key', '.pem'}:
            continue
        if path.name.startswith('.env') or path.name == 'application-local.properties':
            continue
        files.append(path)
if output.resolve() in {path.resolve() for path in files}:
    raise SystemExit('输出路径不能覆盖待提交的源码文件')
with ZipFile(output, 'w', ZIP_DEFLATED) as archive:
    for path in files:
        archive.write(path, path.relative_to(root))
with ZipFile(output) as archive:
    assert not any(name.endswith(('.jar', '.class')) for name in archive.namelist())
print(f'打包完成：{output}（{len(files)} 个源码、配置、脚本和文档文件）')
PY
