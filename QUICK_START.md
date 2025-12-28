# 快速啟動指南

## ⚠️ PowerShell 執行政策問題

如果您看到以下錯誤：
```
因為這個系統上已停用指令碼執行，所以無法載入...
```

這是因為 Windows PowerShell 的**執行政策（Execution Policy）**限制。

## ✅ 解決方案（選擇一種）

### 方法 1：臨時允許執行（推薦）

在 PowerShell 中執行：
```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
```

然後執行啟動腳本：
```powershell
.\start.ps1
```

**優點**：只影響當前會話，最安全

### 方法 2：直接繞過執行

使用以下命令直接執行（無需修改執行政策）：
```powershell
powershell -ExecutionPolicy Bypass -File ".\start.ps1"
```

### 方法 3：手動執行命令

如果不想修改執行政策，可以手動執行以下命令：

```powershell
# 1. 配置環境變數
$javaDir = "$env:USERPROFILE\DevelopmentTools\jdk-17"
$mavenDir = Get-ChildItem "$env:USERPROFILE\DevelopmentTools" -Directory | Where-Object { $_.Name -like "*maven*" } | Select-Object -First 1
$env:JAVA_HOME = $javaDir
$env:MAVEN_HOME = $mavenDir.FullName
$env:PATH = "$javaDir\bin;$($mavenDir.FullName)\bin;$env:PATH"

# 2. 停止舊進程
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force

# 3. 編譯專案
mvn clean compile

# 4. 啟動伺服器
mvn exec:java -Dexec.mainClass="com.mahjong.server.MahjongWebSocketServer"
```

## 📝 完整啟動流程

1. **開啟 PowerShell**（在專案目錄中）

2. **臨時允許執行腳本**：
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
   ```

3. **執行啟動腳本**：
   ```powershell
   .\start.ps1
   ```

4. **等待伺服器啟動**，看到以下訊息表示成功：
   ```
   Mahjong WebSocket Server started on port: 8888
   ```

5. **開啟瀏覽器**，訪問遊戲頁面：
   ```powershell
   Start-Process "src\main\resources\web\index.html"
   ```
   重複 4 次以開啟 4 個玩家視窗

## 🔍 檢查執行政策

查看當前執行政策：
```powershell
Get-ExecutionPolicy
```

查看所有範圍的執行政策：
```powershell
Get-ExecutionPolicy -List
```

## 💡 提示

- `-Scope Process` 只影響當前 PowerShell 會話，關閉後自動恢復
- 這是最安全的做法，不需要管理員權限
- 每次開啟新的 PowerShell 時都需要執行一次

