# 快速啟動腳本 - 配置環境並啟動伺服器
# 在每次新的 PowerShell 會話中使用此腳本

Write-Host "🚀 快速啟動麻將遊戲伺服器" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步驟 1: 配置環境變數
Write-Host "📋 步驟 1: 配置環境變數..." -ForegroundColor Yellow
& .\setup_env.ps1

if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne $null) {
    Write-Host "❌ 環境配置失敗，請先執行 install_env.ps1 安裝 Java 和 Maven" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步驟 2: 停止舊的 Java 進程（如果有的話）
Write-Host "📋 步驟 2: 清理舊進程..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "   發現運行中的 Java 進程，正在停止..." -ForegroundColor Gray
    $javaProcesses | Stop-Process -Force
    Start-Sleep -Seconds 1
    Write-Host "   ✅ 已清理舊進程" -ForegroundColor Green
} else {
    Write-Host "   ℹ️  沒有運行中的 Java 進程" -ForegroundColor Gray
}

Write-Host ""

# 步驟 3: 編譯專案（如果需要）
Write-Host "📋 步驟 3: 檢查並編譯專案..." -ForegroundColor Yellow
if (-not (Test-Path "target\classes")) {
    Write-Host "   正在編譯專案..." -ForegroundColor Gray
    mvn clean compile
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   ❌ 編譯失敗" -ForegroundColor Red
        exit 1
    }
    Write-Host "   ✅ 編譯完成" -ForegroundColor Green
} else {
    Write-Host "   ✅ 專案已編譯" -ForegroundColor Green
}

Write-Host ""

# 步驟 4: 啟動伺服器
Write-Host "📋 步驟 4: 啟動 WebSocket 伺服器..." -ForegroundColor Yellow
Write-Host "   伺服器將在端口 8888 上運行" -ForegroundColor Gray
Write-Host "   按 Ctrl+C 停止伺服器" -ForegroundColor Gray
Write-Host ""

mvn exec:java -Dexec.mainClass="com.mahjong.server.MahjongWebSocketServer"

