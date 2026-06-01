# Keycloak RS256 + OMS jwt,jwt-jwks — 需先 start-keycloak.ps1 与 start-all.ps1 -OmsJwt -OmsJwtJwks
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
$env:SCM_JWT_STRICT = "1"
mvn -pl scm-integration-tests -Pe2e-jwt-jwks test
Pop-Location
