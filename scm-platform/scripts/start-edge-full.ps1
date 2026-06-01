# MySQL + 四服务 + mock + Keycloak + OpenResty JWT 网关
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
docker compose -f docker-compose.yml -f docker-compose.full.yml -f docker-compose.edge.yml up -d --build
Write-Host "Keycloak: http://localhost:8180"
Write-Host "JWT 网关: http://localhost:8089"
Write-Host "E2E: `$env:SCM_GATEWAY_JWT_URL='http://localhost:8089'; .\scripts\run-e2e-gateway-jwt.ps1"
Pop-Location
