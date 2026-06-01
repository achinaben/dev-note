#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
docker compose -f docker-compose.yml -f docker-compose.gateway-jwt.yml up -d scm-gateway-jwt
echo "JWT gateway: http://localhost:8089"
