# Kafka 模式 E2E（需 start-all.ps1 -Kafka 已启动四服务）
param(
    [string]$JavaHome = $env:JAVA_HOME
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
if (-not $JavaHome) {
    $ideaJbr = "D:\dev\idea-2026.1.win\jbr"
    if (Test-Path $ideaJbr) { $JavaHome = $ideaJbr }
}
$env:JAVA_HOME = $JavaHome
Set-Location $Root
mvn -pl scm-integration-tests -Pe2e-kafka test
exit $LASTEXITCODE
