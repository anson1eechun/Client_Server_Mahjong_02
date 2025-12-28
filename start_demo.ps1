# Client-Server Mahjong 快速 Demo 啟動腳本 (Windows PowerShell)
# 用途：一鍵啟動伺服器和 4 個瀏覽器視窗

Write-Host "🎮 Client-Server Mahjong Demo 啟動腳本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 檢查 Java 版本
Write-Host "📋 步驟 1: 檢查 Java 環境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "   Java 版本: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ 找不到 Java，請先安裝 Java 17 或以上版本" -ForegroundColor Red
    exit 1
}

# 2. 檢查 Maven
Write-Host ""
Write-Host "📋 步驟 2: 檢查 Maven 環境..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "   $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ 找不到 Maven，請先安裝 Maven 3.6+ 或使用 Maven Wrapper" -ForegroundColor Red
    Write-Host "   下載地址: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    exit 1
}

# 3. 清理舊程序
Write-Host ""
Write-Host "📋 步驟 3: 清理舊程序..." -ForegroundColor Yellow
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1
Write-Host "   ✅ 已清理舊的 Java 程序" -ForegroundColor Green

# 4. 安裝依賴並編譯
Write-Host ""
Write-Host "📋 步驟 4: 安裝依賴並編譯專案..." -ForegroundColor Yellow
Write-Host "   這可能需要幾分鐘時間，請稍候..." -ForegroundColor Gray
try {
    mvn clean compile
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   ❌ 編譯失敗，請檢查錯誤訊息" -ForegroundColor Red
        exit 1
    }
    Write-Host "   ✅ 編譯成功" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Maven 執行失敗: $_" -ForegroundColor Red
    exit 1
}

# 5. 啟動伺服器（背景執行）
Write-Host ""
Write-Host "📋 步驟 5: 啟動 WebSocket 伺服器..." -ForegroundColor Yellow
$serverJob = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    mvn exec:java -Dexec.mainClass="com.mahjong.server.MahjongWebSocketServer"
}

# 等待伺服器啟動
Write-Host "   ⏳ 等待伺服器啟動（3 秒）..." -ForegroundColor Gray
Start-Sleep -Seconds 3

# 檢查伺服器是否啟動成功
if ($serverJob.State -eq "Running") {
    Write-Host "   ✅ 伺服器已啟動（Job ID: $($serverJob.Id)）" -ForegroundColor Green
} else {
    Write-Host "   ❌ 伺服器啟動失敗，請檢查錯誤訊息" -ForegroundColor Red
    Receive-Job $serverJob
    exit 1
}

# 6. 開啟 4 個瀏覽器視窗
Write-Host ""
Write-Host "📋 步驟 6: 開啟 4 個瀏覽器視窗..." -ForegroundColor Yellow
$webPath = Join-Path $PSScriptRoot "src\main\resources\web\index.html"

if (-not (Test-Path $webPath)) {
    Write-Host "   ❌ 找不到 $webPath" -ForegroundColor Red
    exit 1
}

for ($i = 1; $i -le 4; $i++) {
    Start-Process $webPath
    Write-Host "   ✅ 已開啟瀏覽器視窗 $i" -ForegroundColor Green
    Start-Sleep -Seconds 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Demo 準備完成！" -ForegroundColor Green
Write-Host ""
Write-Host "📝 下一步操作：" -ForegroundColor Yellow
Write-Host "   1. 在 4 個瀏覽器視窗中分別輸入暱稱：" -ForegroundColor White
Write-Host "      - Player1, Player2, Player3, Player4" -ForegroundColor Gray
Write-Host "   2. 點擊各視窗的 'Start Game' 按鈕" -ForegroundColor White
Write-Host "   3. 等待 4 位玩家全部連線後，遊戲自動開始" -ForegroundColor White
Write-Host ""
Write-Host "💡 提示：" -ForegroundColor Yellow
Write-Host "   - 伺服器正在背景執行（Job ID: $($serverJob.Id)）" -ForegroundColor Gray
Write-Host "   - 要停止伺服器，請執行：Stop-Job $($serverJob.Id); Remove-Job $($serverJob.Id)" -ForegroundColor Gray
Write-Host "   - 或執行：Get-Process -Name java | Stop-Process -Force" -ForegroundColor Gray
Write-Host ""
Write-Host "🎉 祝 Demo 順利！" -ForegroundColor Cyan

