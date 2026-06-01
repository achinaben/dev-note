# 启动 Keycloak（JWT 联调）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
docker compose -f docker-compose.keycloak.yml up -d
Write-Host "Keycloak: http://localhost:8180  admin/admin"
Write-Host "Realm scm 用户 e2e-user / e2e-pass  见 deploy/jwt-oms-integration.md"
