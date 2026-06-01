# Docker 全栈：MySQL + 四服务（W35）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
Write-Host "构建并启动 MySQL + OMS/WMS/TMS/ERP ..."
docker compose -f docker-compose.yml -f docker-compose.full.yml up -d --build
Write-Host "等待健康检查（约 2~3 分钟）..."
docker compose -f docker-compose.yml -f docker-compose.full.yml ps
Write-Host "边缘栈(Keycloak+JWT网关): .\scripts\start-edge-full.ps1"
Write-Host "E2E smoke: .\scripts\run-e2e-stack-smoke.ps1"
Pop-Location
