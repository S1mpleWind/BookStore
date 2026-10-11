#!/usr/bin/env bash
set -euo pipefail

# 打包课程项目的源码、配置、数据库脚本和文档。
# 默认输出到当前项目目录，不包含依赖库、构建产物、Git 数据和本地密钥。

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
STAMP="$(date +%Y%m%d_%H%M%S)"
OUTPUT="${1:-${ROOT_DIR}/bookstore-source-submission-${STAMP}.zip}"

if ! command -v zip >/dev/null 2>&1; then
  echo "错误：未找到 zip 命令。Ubuntu/Debian 可执行：sudo apt install zip" >&2
  exit 1
fi

case "$OUTPUT" in
  /*) ;;
  *) OUTPUT="${ROOT_DIR}/${OUTPUT}" ;;
esac

mkdir -p "$(dirname "$OUTPUT")"
rm -f "$OUTPUT"

cd "$ROOT_DIR"

zip -rq "$OUTPUT" . \
  -x './.git/*' \
  -x './.idea/*' \
  -x './.vscode/*' \
  -x './**/node_modules/*' \
  -x './frontend/node_modules/*' \
  -x './frontend/dist/*' \
  -x './frontend/.vite/*' \
  -x './backend/target/*' \
  -x './**/target/*' \
  -x './backend/.mvn/wrapper/maven-wrapper.jar' \
  -x './**/*.class' \
  -x './**/*.log' \
  -x './*.zip' \
  -x './**/*.zip' \
  -x './backend/application-local.properties' \
  -x './**/application-local.properties' \
  -x './.env' \
  -x './.env.*' \
  -x './**/.env' \
  -x './**/.env.*' \
  -x './*.key' \
  -x './**/*.key' \
  -x './*.pem' \
  -x './**/*.pem' \
  -x "./$(basename "$OUTPUT")"

echo "打包完成：$OUTPUT"
echo "文件大小：$(du -h "$OUTPUT" | cut -f1)"
echo "内容检查："
unzip -l "$OUTPUT" | grep -E '(^|/)(node_modules|target|\.git)(/|$)|application-local\.properties|\.env($|\.)' >/dev/null \
  && { echo "警告：压缩包中可能包含被排除的文件，请检查。"; exit 1; } \
  || true
