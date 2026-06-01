#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
docker compose -f docker-compose.yml -f docker-compose.full.yml -f docker-compose.edge.yml -f docker-compose.kafka-overlay.yml -f docker-compose.edge-kafka.yml down
