# B2C 全链路轻量压测：建单 + mock-pay，统计成功率与耗时
param(
    [int]$Iterations = 20,
    [string]$OmsBase = "http://localhost:8081",
    [string]$PayBase = "http://localhost:8085"
)

$ErrorActionPreference = "Stop"
$ok = 0
$fail = 0
$times = @()

Write-Host "压测 $Iterations 次 -> $OmsBase"

for ($i = 1; $i -le $Iterations; $i++) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $token = "ct-stress-$i-$(Get-Random)"
    $body = @{
        client_token = $token
        buyer_id     = "U10001"
        channel      = "APP"
        address_id   = "ADDR100"
        lines        = @(@{ sku_id = "SKU001"; qty = "2"; warehouse_id = "WH-SH-01" })
    } | ConvertTo-Json -Depth 5
    try {
        $create = Invoke-RestMethod -Method Post -Uri "$OmsBase/api/v1/orders" `
            -Headers @{ "Idempotency-Key" = $token } `
            -ContentType "application/json" -Body $body
        $orderNo = $create.data.orders[0].order_no
        Invoke-RestMethod -Method Post -Uri "$PayBase/trigger" -ContentType "application/json" `
            -Body (@{ order_no = $orderNo; notify_id = "notify-$token" } | ConvertTo-Json) | Out-Null
        $diag = Invoke-RestMethod -Method Get -Uri "$OmsBase/api/v1/ops/orders/$orderNo/diag"
        if ($diag.data.status -eq "PAID") {
            $ok++
        } else {
            $fail++
            Write-Warning "[$i] 状态非 PAID: $($diag.data.status)"
        }
    } catch {
        $fail++
        Write-Warning "[$i] $($_.Exception.Message)"
    }
    $sw.Stop()
    $times += $sw.ElapsedMilliseconds
}

$avg = if ($times.Count -gt 0) { [math]::Round(($times | Measure-Object -Average).Average, 1) } else { 0 }
Write-Host "完成: 成功=$ok 失败=$fail 平均耗时=${avg}ms"
