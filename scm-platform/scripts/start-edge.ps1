# 一键启动 Keycloak + Nginx 网关（四服务需另启 start-all）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
docker compose -f docker-compose.yml -f docker-compose.keycloak.yml up -d scm-gateway keycloak
Write-Host "Keycloak: http://localhost:8180  (admin/admin, e2e-user/e2e-pass)"
Write-Host "网关:     http://localhost:8080  Header: X-Api-Key: e2e-gateway-key"
Write-Host ""
Write-Host "四服务 + JWT 验签:"
Write-Host "  .\scripts\start-all.ps1 -OmsJwt -OmsJwtJwks"
Write-Host "网关 E2E:"
Write-Host "  `$env:SCM_GATEWAY_URL='http://localhost:8080'; .\scripts\run-e2e-gateway-smoke.ps1"
Pop-Location
