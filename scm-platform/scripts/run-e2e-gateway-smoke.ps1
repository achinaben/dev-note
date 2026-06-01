# E2E-GW01：未设 SCM_GATEWAY_URL 时直连 OMS；设则走网关并带 X-Api-Key
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root
mvn -pl scm-integration-tests -Pe2e-gateway-smoke test
Pop-Location
