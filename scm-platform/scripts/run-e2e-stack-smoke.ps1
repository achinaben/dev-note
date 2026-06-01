# Docker 全栈上跑 @smoke E2E（需 start-edge-full 或 start-docker-stack 已就绪）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
mvn -pl scm-integration-tests -Pe2e test "-Dcucumber.filter.tags=@smoke and @e2e"
Pop-Location
