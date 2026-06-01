#!/usr/bin/env bash
# MySQL + 四服务 + mock + Keycloak + OpenResty JWT 网关 :8089
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
COMPOSE="docker compose -f docker-compose.yml -f docker-compose.full.yml -f docker-compose.edge.yml"
echo "$COMPOSE up -d --build ..."
$COMPOSE up -d --build
echo "等待健康检查（约 3~5 分钟）..."
$COMPOSE ps
echo ""
echo "Keycloak:  http://localhost:8180"
echo "JWT 网关:  http://localhost:8089  (X-Api-Key + Bearer)"
echo "四服务:    8081-8087"
echo "E2E:       SCM_GATEWAY_JWT_URL=http://localhost:8089 mvn -pl scm-integration-tests -Pe2e-gateway-smoke test"
