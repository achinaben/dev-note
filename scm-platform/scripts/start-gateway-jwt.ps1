# 启动 OpenResty JWT 网关（需先 start-all -OmsJwt）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
docker compose -f docker-compose.gateway-jwt-local.yml up -d --build
Write-Host "JWT 网关: http://localhost:8089  (X-Api-Key: e2e-gateway-key)"
Write-Host "E2E: `$env:SCM_GATEWAY_JWT_URL='http://localhost:8089'; `$env:SCM_GATEWAY_JWT='1'; `$env:SCM_JWT_ISSUER='http://localhost:8180/realms/scm'; .\scripts\run-e2e-gateway-jwt.ps1"
Pop-Location
