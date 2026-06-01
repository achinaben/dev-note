#!/usr/bin/env bash
# CI / 本地：单元测试 + 起四服务 + Cucumber E2E
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "== mvn test =="
mvn -B -q test

echo "== 启动服务 =="
export WAIT_SECONDS="${WAIT_SECONDS:-180}"
bash "$(dirname "$0")/start-all.sh"

echo "== E2E =="
mvn -B -q -pl scm-integration-tests -Pe2e test

echo "== 停止服务 =="
for p in 8081 8082 8083 8084; do
  pid=$(ss -lntp 2>/dev/null | awk -v port=":$p" '$0 ~ port {gsub(/.*pid=/,"",$0); gsub(/,.*/,"",$0); print $0; exit}' || true)
  if [[ -n "${pid:-}" ]]; then kill "$pid" 2>/dev/null || true; fi
done
echo "CI E2E 完成。"
