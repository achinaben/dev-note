# MySQL + four services + mocks + Kafka + Keycloak + OpenResty JWT gateway
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
docker compose -f docker-compose.yml -f docker-compose.full.yml -f docker-compose.edge.yml -f docker-compose.kafka-overlay.yml -f docker-compose.edge-kafka.yml up -d --build
Write-Host "Keycloak: http://localhost:8180"
Write-Host "JWT gateway: http://localhost:8089"
Write-Host "Kafka: localhost:9092"
Write-Host "E2E-K05: .\scripts\run-e2e-edge-kafka.ps1"
Pop-Location
