# 需 start-all.ps1 -OmsJwt
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
mvn -pl scm-integration-tests -Pe2e-jwt test
Pop-Location
