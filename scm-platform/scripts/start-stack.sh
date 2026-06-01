#!/usr/bin/env bash
# 构建 JAR 并启动 docker compose 全栈
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
echo "mvn install -DskipTests ..."
mvn -q install -DskipTests
docker compose -f docker-compose.stack.yml up -d --build
echo "等待端口 8081-8087 ..."
for p in 8081 8082 8083 8084 8085 8086 8087; do
  for _ in $(seq 1 60); do
    if (echo >/dev/tcp/127.0.0.1/$p) 2>/dev/null; then break; fi
    sleep 2
  done
done
echo "全栈就绪。E2E: mvn -pl scm-integration-tests -Pe2e test"
