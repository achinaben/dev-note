# 汇总 surefire 测试结果（不依赖 JaCoCo）
param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"
Push-Location $Root
try {
    $reports = Get-ChildItem -Recurse -Filter "TEST-*.xml" -Path . |
        Where-Object { $_.FullName -match "\\target\\surefire-reports\\" }
    $total = 0
    $failed = 0
    $skipped = 0
    foreach ($r in $reports) {
        [xml]$xml = Get-Content $r.FullName -Raw
        $suite = $xml.testsuite
        if (-not $suite) { continue }
        $total += [int]$suite.tests
        $failed += [int]$suite.failures + [int]$suite.errors
        $skipped += [int]$suite.skipped
    }
    $passed = $total - $failed - $skipped
    Write-Host "=== scm-platform test summary ==="
    Write-Host "Suites (XML files): $($reports.Count)"
    Write-Host "Tests:  $total"
    Write-Host "Passed: $passed"
    Write-Host "Failed: $failed"
    Write-Host "Skipped:$skipped"
    if ($failed -gt 0) { exit 1 }
} finally {
    Pop-Location
}
