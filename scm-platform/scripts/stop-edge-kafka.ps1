$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
docker compose -f docker-compose.yml -f docker-compose.full.yml -f docker-compose.edge.yml -f docker-compose.kafka-overlay.yml -f docker-compose.edge-kafka.yml down
Pop-Location
