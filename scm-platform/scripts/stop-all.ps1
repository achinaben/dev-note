# 停止占用 8081~8084 的 Java 进程（Windows）
$ports = 8081, 8082, 8083, 8084, 8085, 8086, 8087
foreach ($port in $ports) {
    $lines = netstat -ano | Select-String ":$port\s+.*LISTENING"
    foreach ($line in $lines) {
        $parts = ($line -replace '\s+', ' ').ToString().Trim().Split(' ')
        $procId = $parts[-1]
        if ($procId -match '^\d+$') {
            Write-Host "结束端口 $port  PID=$procId"
            taskkill /PID $procId /F 2>$null
        }
    }
}
Write-Host "完成。"
