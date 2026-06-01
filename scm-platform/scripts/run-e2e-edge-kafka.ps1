$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
mvn -pl scm-integration-tests -Pe2e-kafka test "-Dcucumber.filter.tags=@E2E-K05"
Pop-Location
exit $LASTEXITCODE
