#!/usr/bin/env bash
# Keycloak + Nginx 网关（四服务另启 scripts/start-all.sh）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
docker compose -f docker-compose.yml -f docker-compose.keycloak.yml up -d scm-gateway keycloak
echo "Keycloak: http://localhost:8180"
echo "Gateway:  http://localhost:8080  X-Api-Key: e2e-gateway-key"
