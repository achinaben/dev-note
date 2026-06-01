# 将 ai-dev OpenAPI 契约同步到 deploy/openapi（供网关/文档工具导入）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Src = Join-Path (Split-Path $Root -Parent) "业务方案\ai-dev\contracts"
$Dst = Join-Path $Root "deploy\openapi"
if (-not (Test-Path $Src)) {
    Write-Error "源目录不存在: $Src"
}
New-Item -ItemType Directory -Force -Path $Dst | Out-Null
Copy-Item -Path (Join-Path $Src "*.yaml") -Destination $Dst -Force
Write-Host "已导出到 $Dst"
Get-ChildItem $Dst -Filter "*.yaml" | ForEach-Object { Write-Host "  - $($_.Name)" }
