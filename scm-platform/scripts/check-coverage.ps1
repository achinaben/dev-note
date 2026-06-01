# 解析 JaCoCo CSV，检查行覆盖率是否达到阈值（需先 mvn -Pcoverage verify）
param(
    [double]$Minimum = 0.50,
    [string]$Root = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"
$csvFiles = Get-ChildItem -Recurse -Filter "jacoco.csv" -Path $Root |
    Where-Object { $_.FullName -match "\\target\\site\\jacoco\\" }
if (-not $csvFiles) {
    Write-Error "未找到 jacoco.csv，请先运行: mvn -Pcoverage verify"
}
$below = @()
foreach ($csv in $csvFiles) {
    $rows = Import-Csv $csv.FullName
    $total = ($rows | Where-Object { $_.LINE -match '^\d+$' } | ForEach-Object { [int]$_.LINE } | Measure-Object -Sum).Sum
    $missed = ($rows | Where-Object { $_.LINE_MISSED -match '^\d+$' } | ForEach-Object { [int]$_.LINE_MISSED } | Measure-Object -Sum).Sum
    if ($total -eq 0) { continue }
    $ratio = ($total - $missed) / $total
    $rel = $csv.FullName.Substring($Root.Length).TrimStart('\')
    Write-Host ("{0,-50} line coverage {1:P1}" -f $rel, $ratio)
    if ($ratio -lt $Minimum) { $below += "$rel ($([math]::Round($ratio*100,1))%)" }
}
if ($below.Count -gt 0) {
    Write-Error "低于阈值 $Minimum : $($below -join '; ')"
}
Write-Host "全部模块行覆盖率 >= $Minimum"
