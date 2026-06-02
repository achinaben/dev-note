$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
if (-not $env:KEYCLOAK_URL) { $env:KEYCLOAK_URL = "http://localhost:8180" }
if (-not $env:SCM_E2E_OMS_AUTH) { $env:SCM_E2E_OMS_AUTH = "none" }
mvn -pl scm-integration-tests -Pe2e-kafka test "-Dcucumber.filter.tags=@E2E-K05"
Pop-Location
exit $LASTEXITCODE
