# 一键启动 OMS/WMS/TMS/ERP（各开一个后台 Java 进程，日志写入 scripts/logs）
param(
    [string]$JavaHome = $env:JAVA_HOME,
    [int]$WaitSeconds = 90,
    [switch]$Kafka,
    [switch]$Jdbc,
    [switch]$WmsInventory,
    [switch]$WmsStrict,
    [switch]$OmsJwt,
    [switch]$OmsJwtJwks
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$LogDir = Join-Path $PSScriptRoot "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

if (-not $JavaHome) {
    $ideaJbr = "D:\dev\idea-2026.1.win\jbr"
    if (Test-Path $ideaJbr) { $JavaHome = $ideaJbr }
}
if (-not $JavaHome) {
    Write-Error "请设置 JAVA_HOME 或安装 JDK 17+"
}

$services = @(
    @{ Module = "scm-oms-service"; Port = 8081 },
    @{ Module = "scm-wms-service"; Port = 8082 },
    @{ Module = "scm-tms-service"; Port = 8083 },
    @{ Module = "scm-erp-service"; Port = 8084 },
    @{ Module = "scm-mock-pay"; Port = 8085 },
    @{ Module = "scm-mock-carrier"; Port = 8086 },
    @{ Module = "scm-mock-inventory"; Port = 8087 }
)

$jvmArgs = "-Xmx256m -Xms128m -XX:MaxMetaspaceSize=128m"
if ($WmsInventory) { Write-Host "OMS 库存提供方: WMS (profile wms-inventory)" }
if ($WmsStrict) { Write-Host "WMS 严格出库: relaxed-handover=false (profile wms-strict)" }
if ($OmsJwt) { Write-Host "OMS JWT 鉴权: enabled (profile jwt)" }
if ($OmsJwtJwks) { Write-Host "OMS JWT RS256 验签: enabled (profile jwt-jwks，需 Keycloak 8180)" }
$env:MAVEN_OPTS = $jvmArgs
$env:JAVA_HOME = $JavaHome

Write-Host "工作目录: $Root"
Write-Host "JAVA_HOME: $JavaHome"
Write-Host "MAVEN_OPTS: $jvmArgs"
if ($Jdbc) { Write-Host "存储: MySQL jdbc (3307-3310)" }
if ($Kafka) { Write-Host "事件总线: Kafka (localhost:9092)" }

if ($Jdbc) {
    Write-Host "启动 MySQL (docker compose)..."
    Push-Location $Root
    docker compose up -d mysql-erp mysql-oms mysql-wms mysql-tms 2>&1 | Out-Host
    Pop-Location
    $mysqlPorts = @(3307, 3308, 3309, 3310)
    $mysqlDeadline = (Get-Date).AddSeconds(90)
    foreach ($port in $mysqlPorts) {
        while ((Get-Date) -lt $mysqlDeadline) {
            try {
                $c = New-Object System.Net.Sockets.TcpClient
                $c.Connect("127.0.0.1", $port)
                $c.Close()
                break
            } catch { Start-Sleep -Seconds 2 }
        }
    }
}

if ($Kafka) {
    Write-Host "启动 Kafka (docker compose)..."
    Push-Location $Root
    docker compose up -d kafka 2>&1 | Out-Host
    Pop-Location
    $kafkaDeadline = (Get-Date).AddSeconds(60)
    $kafkaUp = $false
    while ((Get-Date) -lt $kafkaDeadline) {
        try {
            $c = New-Object System.Net.Sockets.TcpClient
            $c.Connect("127.0.0.1", 9092)
            $c.Close()
            $kafkaUp = $true
            break
        } catch { Start-Sleep -Seconds 2 }
    }
    if (-not $kafkaUp) {
        Write-Warning "Kafka 9092 未就绪，服务可能回退或启动失败，请检查 docker compose logs kafka"
    }
}

Write-Host "预编译依赖（mvn install -DskipTests）..."
Push-Location $Root
mvn -q install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Pop-Location
    Write-Error "mvn install 失败"
}
Pop-Location

foreach ($s in $services) {
    $profList = [System.Collections.Generic.List[string]]::new()
    if ($Jdbc) { [void]$profList.Add("jdbc") }
    if ($Kafka) { [void]$profList.Add("kafka") }
    if ($WmsInventory -and $s.Module -eq "scm-oms-service") { [void]$profList.Add("wms-inventory") }
    if ($OmsJwt -and $s.Module -eq "scm-oms-service") { [void]$profList.Add("jwt") }
    if ($OmsJwtJwks -and $s.Module -eq "scm-oms-service") { [void]$profList.Add("jwt-jwks") }
    if ($WmsStrict -and $s.Module -eq "scm-wms-service") { [void]$profList.Add("wms-strict") }
    $profileArg = if ($profList.Count -gt 0) { "-Dspring-boot.run.profiles=$($profList -join ',')" } else { "" }
    $logFile = Join-Path $LogDir ($s.Module + ".log")
    $runArgs = "-Dspring-boot.run.jvmArguments=`"$jvmArgs`" $profileArg"
    $cmd = "set JAVA_HOME=$JavaHome&& set MAVEN_OPTS=$jvmArgs&& cd /d `"$Root`" && mvn -q -pl $($s.Module) spring-boot:run $runArgs > `"$logFile`" 2>&1"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $cmd -WindowStyle Minimized
    Write-Host "已启动 $($s.Module) -> 端口 $($s.Port)  日志: $logFile"
    Start-Sleep -Seconds 8
}

Write-Host "等待服务就绪（最多 ${WaitSeconds}s）..."
$deadline = (Get-Date).AddSeconds($WaitSeconds)
while ((Get-Date) -lt $deadline) {
    $allUp = $true
    foreach ($s in $services) {
        try {
            $client = New-Object System.Net.Sockets.TcpClient
            $client.Connect("127.0.0.1", $s.Port)
            $client.Close()
        } catch {
            $allUp = $false
            break
        }
    }
    if ($allUp) {
        Write-Host "四服务已就绪。"
        if ($Jdbc) { Write-Host "运行 jdbc E2E: .\run-e2e-jdbc.ps1" }
        if ($Kafka) { Write-Host "运行 Kafka E2E: .\run-e2e-kafka.ps1" }
        Write-Host "运行 E2E: .\run-e2e.ps1"
        if ($WmsInventory) { Write-Host "运行 WMS 库存 E2E: .\run-e2e-wms-inventory.ps1" }
        if ($WmsStrict) { Write-Host "运行严格出库 E2E: .\run-e2e-strict-outbound.ps1" }
        if ($Jdbc -and $WmsStrict) { Write-Host "运行严格+jdbc E2E: .\run-e2e-strict-jdbc.ps1" }
        exit 0
    }
    Start-Sleep -Seconds 2
}
Write-Warning "超时：部分端口未监听，请查看 $LogDir 下日志。"
exit 1
