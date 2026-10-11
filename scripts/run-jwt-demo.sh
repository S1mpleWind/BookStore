#!/usr/bin/env bash
set -euo pipefail
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if ! java -version 2>&1 | head -1 | grep -q '"21\.'; then
  echo '请在项目根目录执行 sdk env，切换到 Java 21 后再运行此脚本。' >&2
  exit 1
fi
# 每次演示生成随机密钥；不会写入源码。重启后旧凭证失效。
export JWT_SECRET="${JWT_SECRET:-$(openssl rand -base64 32)}"
cd "${PROJECT_DIR}/backend"
exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo "$@"
