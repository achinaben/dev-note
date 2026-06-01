#!/usr/bin/env bash
# MySQL + four services + mocks + Kafka + Keycloak + OpenResty JWT gateway :8089
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
COMPOSE="docker compose -f docker-compose.yml -f docker-compose.full.yml -f docker-compose.edge.yml -f docker-compose.kafka-overlay.yml -f docker-compose.edge-kafka.yml"
echo "$COMPOSE up -d --build ..."
$COMPOSE up -d --build
echo "Waiting for health checks (about 3-5 minutes in CI)..."
$COMPOSE ps
echo ""
echo "Keycloak:  http://localhost:8180"
echo "JWT gateway: http://localhost:8089"
echo "Services:  8081-8087"
echo "Kafka:     localhost:9092"
echo "E2E-K05:   bash scripts/run-e2e-edge-kafka.sh"
