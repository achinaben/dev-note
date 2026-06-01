# 将 ai-dev OpenAPI 同步到 scm-contract-check（CI 无上级目录时使用）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Src = Join-Path (Split-Path $Root -Parent) "业务方案\ai-dev\contracts"
$Dst = Join-Path $Root "scm-contract-check\src\test\resources\openapi"
if (-not (Test-Path $Src)) {
    Write-Error "源目录不存在: $Src"
}
New-Item -ItemType Directory -Force -Path $Dst | Out-Null
Copy-Item -Path (Join-Path $Src "*.yaml") -Destination $Dst -Force
Write-Host "已同步到 $Dst"
