#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="${SCM_AI_DEV_CONTRACTS:-$ROOT/../业务方案/ai-dev/contracts}"
DST="$ROOT/scm-contract-check/src/test/resources/openapi"
if [[ ! -d "$SRC" ]]; then
  echo "源目录不存在: $SRC" >&2
  exit 1
fi
mkdir -p "$DST"
cp -f "$SRC"/*.yaml "$DST/"
echo "已同步到 $DST"
