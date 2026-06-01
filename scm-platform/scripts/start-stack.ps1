# 构建 JAR 并 docker compose 全栈启动
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
Write-Host "mvn install -DskipTests ..."
mvn -q install -DskipTests
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "mvn install failed" }
docker compose -f docker-compose.stack.yml up -d --build
Write-Host "全栈: 8081-8087  E2E: mvn -pl scm-integration-tests -Pe2e test"
Pop-Location
