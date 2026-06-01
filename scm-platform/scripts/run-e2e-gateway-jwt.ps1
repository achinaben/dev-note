# E2E-GW01~03：网关 smoke + JWT scope
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
if (-not $env:SCM_GATEWAY_JWT_URL) { $env:SCM_GATEWAY_JWT_URL = "http://localhost:8089" }
if (-not $env:SCM_GATEWAY_JWT) { $env:SCM_GATEWAY_JWT = "1" }
if (-not $env:SCM_JWT_ISSUER) { $env:SCM_JWT_ISSUER = "http://localhost:8180/realms/scm" }
mvn -pl scm-integration-tests -Pe2e-gateway-smoke test
Pop-Location
