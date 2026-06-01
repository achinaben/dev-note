# 启动 Nginx 网关 :8080（需四服务已在宿主机运行）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
docker compose up -d scm-gateway
Write-Host "网关: http://localhost:8080  Header: X-Api-Key: e2e-gateway-key"
Write-Host "E2E: `$env:SCM_GATEWAY_URL='http://localhost:8080'; .\scripts\run-e2e-gateway-smoke.ps1"
Pop-Location
