# E2E-14：jdbc + 严格出库（需先 start-all.ps1 -Jdbc -WmsStrict）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "D:\dev\idea-2026.1.win\jbr" }
Push-Location $Root
mvn -pl scm-integration-tests -Pe2e-strict-jdbc test
Pop-Location
