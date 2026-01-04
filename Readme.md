# Client-Server 台灣麻將遊戲

## 📋 專案概述

本專案是一個基於 **WebSocket** 的多人線上台灣麻將遊戲，採用 **Java** 後端 + **Web** 前端架構。

### 核心目標
- ✅ **軟體測試能力展示**：488 個單元測試，整體 Branch Coverage 90%（核心邏輯 91%）
- ✅ **高程式碼品質**：總複雜度 482（WMC > 200），通過 PMD 檢查
- ✅ **完整遊戲邏輯**：支援吃、碰、槓、胡等完整麻將規則
- ✅ **Bug 修復紀錄**：10 個 Bug & Fix 案例，涵蓋 Critical、Major、Logic 等各類問題

### 技術棧
- **後端**：Java 21, Maven, WebSocket (Java-WebSocket)
- **前端**：HTML5, JavaScript, CSS3
- **測試**：JUnit 5, Mockito, Jacoco
- **通訊**：WebSocket (JSON 封包)

---

## 🚀 快速開始

### 環境需求
- Java 17 或以上
- Maven 3.6+
- 現代瀏覽器（Chrome, Firefox, Safari, Edge）

### 安裝步驟

1. **克隆專案**
```bash
git clone https://github.com/anson1eechun/Client_Server_Mahjong_02.git
cd Client_Server_Mahjong_02
```

2. **設定環境（Windows PowerShell，首次使用）**
```powershell
# 臨時允許執行腳本
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# 執行環境設定腳本（如果需要）
.\setup_env.ps1
```

3. **編譯專案**
```bash
mvn clean compile
```

4. **執行測試（可選）**
```bash
mvn test
```

### 快速啟動

**Windows PowerShell（推薦）**：
```powershell
# 設定執行政策（僅需執行一次）
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# 啟動伺服器
.\start.ps1
```

**詳細說明**：請參考 `QUICK_START.md` 或 `Demo展示指南.md`

---

## 🎮 Demo 流程（上台演示）

### 準備階段

#### 步驟 1：啟動伺服器

**Windows PowerShell（推薦）**：
```powershell
# 設定執行政策（僅需執行一次）
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# 使用啟動腳本
.\start.ps1
```

**或手動執行**：
```powershell
# 配置環境變數
$javaDir = "$env:USERPROFILE\DevelopmentTools\jdk-17"
$mavenDir = Get-ChildItem "$env:USERPROFILE\DevelopmentTools" -Directory | Where-Object { $_.Name -like "*maven*" } | Select-Object -First 1
$env:JAVA_HOME = $javaDir
$env:MAVEN_HOME = $mavenDir.FullName
$env:PATH = "$javaDir\bin;$($mavenDir.FullName)\bin;$env:PATH"

# 停止舊進程
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force

# 啟動 WebSocket 伺服器
mvn exec:java -Dexec.mainClass="com.mahjong.server.MahjongWebSocketServer"
```

**Unix/Linux/Mac**：
```bash
# 清理舊程序（確保端口乾淨）
killall java

# 啟動 WebSocket 伺服器
mvn exec:java -Dexec.mainClass="com.mahjong.server.MahjongWebSocketServer"
```

**成功標誌**：
```
Mahjong WebSocket Server started on port: 8888
```

**⚠️ 注意**：保持此終端機視窗開啟，不要關閉！

**詳細說明**：請參考 `QUICK_START.md` 或 `Demo展示指南.md`

---

#### 步驟 2：開啟 4 個瀏覽器視窗（模擬 4 位玩家）

**Windows PowerShell（推薦）**：
```powershell
# 開啟 4 個瀏覽器視窗
Start-Process "src\main\resources\web\index.html"
Start-Sleep -Seconds 1
Start-Process "src\main\resources\web\index.html"
Start-Sleep -Seconds 1
Start-Process "src\main\resources\web\index.html"
Start-Sleep -Seconds 1
Start-Process "src\main\resources\web\index.html"
```

**或使用 Demo 啟動腳本**：
```powershell
.\start_demo.ps1
```

**方法 B：手動開啟**
1. 開啟檔案總管，前往：`src\main\resources\web\`
2. 雙擊 `index.html` 開啟第一個玩家視窗
3. 重複步驟 2，總共開啟 **4 個瀏覽器視窗**（或使用不同瀏覽器）

**Unix/Linux/Mac**：
```bash
# 開啟 4 個瀏覽器視窗
open src/main/resources/web/index.html
open src/main/resources/web/index.html
open src/main/resources/web/index.html
open src/main/resources/web/index.html

# 或使用 Demo 啟動腳本
./start_demo.sh
```

---

### Demo 演示流程

#### 階段 1：登入與連線（1-2 分鐘）

1. **在 4 個瀏覽器視窗中分別輸入暱稱**：
   - 視窗 1：`Player1`（東家）
   - 視窗 2：`Player2`（南家）
   - 視窗 3：`Player3`（西家）
   - 視窗 4：`Player4`（北家）

2. **點擊各視窗的 "Start Game" 按鈕**

3. **等待 4 位玩家全部連線**
   - 伺服器終端會顯示：`New connection: ...`（4 次）
   - 當第 4 位玩家連線後，遊戲自動開始
   - 伺服器終端顯示：`Session Starting...`

**演示重點**：
- ✅ 展示 WebSocket 即時連線
- ✅ 展示多人同步機制
- ✅ 展示自動遊戲開始

---

#### 階段 2：基本遊戲流程（2-3 分鐘）

**演示內容**：

1. **發牌階段**
   - 展示每位玩家收到手牌
   - 展示手牌自動排序
   - 說明：莊家 17 張牌，閒家 16 張牌（台灣麻將規則）

2. **摸牌與出牌**
   - 當前玩家（東家）摸牌
   - 點擊手牌中的任意一張出牌
   - 展示出牌後手牌更新

3. **輪流機制**
   - 展示逆時針輪流（東→南→西→北）
   - 展示當前玩家高亮顯示

**演示重點**：
- ✅ 展示遊戲狀態同步
- ✅ 展示輪流機制
- ✅ 展示即時更新

---

#### 階段 3：吃碰槓動作（3-4 分鐘）

**演示場景 A：碰牌**

1. **設置場景**：
   - Player 1 出牌：`M1`（一萬）
   - Player 2 手中有兩張 `M1`

2. **演示過程**：
   - Player 2 會看到彈出視窗：`[PONG] [SKIP]`
   - 點擊 `PONG` 按鈕
   - 展示：Player 2 的牌減少 2 張，出現碰牌組合
   - 展示：輪到 Player 2 出牌

**演示場景 B：吃牌**

1. **設置場景**：
   - Player 1 出牌：`M2`（二萬）
   - Player 2（下家）手中有 `M1` 和 `M3`

2. **演示過程**：
   - Player 2 會看到彈出視窗：`[CHOW] [SKIP]`
   - 點擊 `CHOW` 按鈕
   - 展示：Player 2 的牌減少 2 張，出現吃牌組合（M1, M2, M3）
   - 展示：輪到 Player 2 出牌

**演示重點**：
- ✅ 展示動作優先級（胡 > 碰/槓 > 吃）
- ✅ 展示即時動作提示
- ✅ 展示狀態同步

---

#### 階段 4：胡牌判定（2-3 分鐘）

**演示場景：自摸胡牌**

1. **設置場景**（可預先準備）：
   - 當前玩家手牌接近胡牌
   - 摸到關鍵牌

2. **演示過程**：
   - 摸牌後，系統檢測到可胡牌
   - 彈出視窗：`[HU] [SKIP]`
   - 點擊 `HU` 按鈕
   - 展示：遊戲結束，顯示獲勝者

**演示重點**：
- ✅ 展示胡牌演算法
- ✅ 展示遊戲結束機制
- ✅ 展示勝利判定

---

#### 階段 5：測試覆蓋率展示（1-2 分鐘）

**在終端機執行**：

```bash
# 執行所有測試
mvn test

# 生成覆蓋率報告
mvn jacoco:report
```

**展示內容**：
1. **測試統計**：
   ```
   Tests run: 488, Failures: 0, Errors: 0
   ```

2. **開啟覆蓋率報告**：
   ```bash
   # Windows PowerShell:
   Start-Process "target\site\jacoco\index.html"
   # Unix/Linux/Mac:
   # open target/site/jacoco/index.html
   ```

3. **展示覆蓋率**：
   - 整體 Branch Coverage: 90%（核心邏輯 91%）
   - 整體 Line Coverage: 90%（核心邏輯 95%）
   - 各類別覆蓋率詳情

**演示重點**：
- ✅ 展示測試完整性（488 個測試，超標 876%）
- ✅ 展示覆蓋率分析（整體達到 90% 分支覆蓋率）
- ✅ 展示程式碼品質

---

### Demo 時間分配建議

| 階段 | 時間 | 重點 |
|------|------|------|
| 準備與連線 | 2 分鐘 | 展示架構與連線 |
| 基本遊戲流程 | 3 分鐘 | 展示核心功能 |
| 吃碰槓動作 | 4 分鐘 | 展示複雜邏輯 |
| 胡牌判定 | 2 分鐘 | 展示演算法 |
| 測試覆蓋率 | 2 分鐘 | 展示測試能力 |
| **總計** | **13 分鐘** | |

---

## 🎯 功能特色

### 核心功能
- ✅ **多人連線**：支援 4 人同時遊戲
- ✅ **即時同步**：WebSocket 即時狀態更新
- ✅ **完整規則**：吃、碰、槓、胡完整實作
- ✅ **動作優先級**：胡 > 碰/槓 > 吃
- ✅ **自動判定**：系統自動檢測可執行動作

### 技術亮點
- ✅ **高測試覆蓋率**：488 個單元測試，整體 Branch Coverage 90%（核心邏輯 91%）
- ✅ **程式碼品質**：總複雜度 482（WMC > 200），通過 PMD 檢查
- ✅ **重構完成**：Meld 類別重構，移除技術債務
- ✅ **整合測試**：完整的遊戲流程測試
- ✅ **Bug 修復**：10 個 Bug & Fix 案例，涵蓋多種測試方法

---

## 🧪 測試說明

### 執行測試

```bash
# 執行所有測試
mvn test

# 執行特定測試類別
mvn test -Dtest=WinStrategyTest
mvn test -Dtest=ActionProcessorTest
mvn test -Dtest=GameFlowIntegrationTest

# 生成覆蓋率報告
mvn clean test jacoco:report

# 查看覆蓋率報告
# Windows PowerShell:
Start-Process "target\site\jacoco\index.html"
# Unix/Linux/Mac:
# open target/site/jacoco/index.html
```

### 測試統計

| 測試類別 | 測試數量 | 狀態 |
|---------|---------|------|
| WinStrategyTest | 8 | ✅ |
| WinStrategyBranchTest | 23 | ✅ |
| WinStrategyAdditionalBranchTest | 30 | ✅ |
| HandValidatorTest | 2 | ✅ |
| HandValidatorBranchTest | 15 | ✅ |
| ScoringCalculatorTest | 4 | ✅ |
| ScoringCalculatorBranchTest | 27 | ✅ |
| MahjongRuleEngineTest | 2 | ✅ |
| MahjongRuleEngineBranchTest | 8 | ✅ |
| ActionProcessorTest | 9 | ✅ |
| ActionProcessorBranchTest | 13 | ✅ |
| ActionProcessorAdditionalBranchTest | 12 | ✅ |
| PlayerHandTest | 16 | ✅ |
| PlayerHandBranchTest | 13 | ✅ |
| PlayerHandAdditionalBranchTest | 9 | ✅ |
| MeldTest | 19 | ✅ |
| MeldBranchTest | 11 | ✅ |
| TingDetectorTest | 9 | ✅ |
| TingDetectorBranchTest | 8 | ✅ |
| GameFlowIntegrationTest | 6 | ✅ |
| MahjongClientTest | 14 | ✅ |
| MahjongClientBranchTest | 12 | ✅ |
| ActionGroupTest | 6 | ✅ |
| MahjongWebSocketServerTest | 17 | ✅ |
| MahjongWebSocketServerBranchTest | 8 | ✅ |
| WebSocketGameSessionTest | 20 | ✅ |
| WebSocketGameSessionAdvancedTest | 21 | ✅ |
| WebSocketGameSessionCoverageTest | 59 | ✅ |
| WebSocketGameSessionExceptionTest | 12 | ✅ |
| WebSocketGameSessionAdditionalBranchTest | 18 | ✅ |
| WebSocketGameSessionFinalBranchTest | 15 | ✅ |
| WebSocketGameSessionRemainingBranchTest | 22 | ✅ |
| WebSocketGameSessionFinalCoverageTest | 25 | ✅ |
| **總計** | **488** | ✅ |

---

## 📁 專案結構

```
Client_Server_Mahjong_02/
├── src/
│   ├── main/
│   │   ├── java/com/mahjong/
│   │   │   ├── client/                    # 客戶端（舊版 Socket，已棄用）
│   │   │   │   └── MahjongClient.java
│   │   │   ├── logic/                     # 遊戲邏輯核心（9 個類別）
│   │   │   │   ├── WinStrategy.java       # 胡牌判定演算法
│   │   │   │   ├── ActionProcessor.java   # 動作處理與優先級仲裁
│   │   │   │   ├── HandValidator.java     # 手牌驗證
│   │   │   │   ├── PlayerHand.java        # 玩家手牌管理
│   │   │   │   ├── Meld.java              # 面子（吃碰槓）類別
│   │   │   │   ├── MahjongRuleEngine.java # 規則引擎（發牌、摸牌）
│   │   │   │   ├── ScoringCalculator.java # 台數計算
│   │   │   │   ├── TingDetector.java      # 聽牌檢測
│   │   │   │   └── Tile.java                 # 牌類別（Enum）
│   │   │   ├── server/                    # 伺服器端（3 個類別）
│   │   │   │   ├── MahjongWebSocketServer.java  # WebSocket 伺服器
│   │   │   │   ├── WebSocketGameSession.java    # 遊戲會話管理
│   │   │   │   └── ActionGroup.java              # 動作群組
│   │   │   └── model/                     # 資料模型
│   │   │       ├── Command.java           # 命令列舉
│   │   │       └── Packet.java            # 封包類別
│   │   └── resources/
│   │       ├── logback.xml                 # 日誌配置
│   │       └── web/                        # Web 前端
│   │           ├── index.html              # 主頁面
│   │           ├── game.js                  # 遊戲邏輯（前端）
│   │           └── style.css               # 樣式表
│   └── test/
│       └── java/com/mahjong/
│           ├── client/                     # 客戶端測試（2 個測試類別）
│           │   ├── MahjongClientTest.java
│           │   └── MahjongClientBranchTest.java
│           ├── logic/                       # 邏輯層測試（20 個測試類別）
│           │   ├── WinStrategyTest.java
│           │   ├── WinStrategyBranchTest.java
│           │   ├── WinStrategyAdditionalBranchTest.java
│           │   ├── ActionProcessorTest.java
│           │   ├── ActionProcessorBranchTest.java
│           │   ├── ActionProcessorAdditionalBranchTest.java
│           │   ├── HandValidatorTest.java
│           │   ├── HandValidatorBranchTest.java
│           │   ├── PlayerHandTest.java
│           │   ├── PlayerHandBranchTest.java
│           │   ├── PlayerHandAdditionalBranchTest.java
│           │   ├── MeldTest.java
│           │   ├── MeldBranchTest.java
│           │   ├── MahjongRuleEngineTest.java
│           │   ├── MahjongRuleEngineBranchTest.java
│           │   ├── ScoringCalculatorTest.java
│           │   ├── ScoringCalculatorBranchTest.java
│           │   ├── TingDetectorTest.java
│           │   ├── TingDetectorBranchTest.java
│           │   └── GameFlowIntegrationTest.java
│           └── server/                      # 伺服器層測試（11 個測試類別）
│               ├── MahjongWebSocketServerTest.java
│               ├── MahjongWebSocketServerBranchTest.java
│               ├── WebSocketGameSessionTest.java
│               ├── WebSocketGameSessionAdvancedTest.java
│               ├── WebSocketGameSessionCoverageTest.java
│               ├── WebSocketGameSessionExceptionTest.java
│               ├── WebSocketGameSessionAdditionalBranchTest.java
│               ├── WebSocketGameSessionFinalBranchTest.java
│               ├── WebSocketGameSessionRemainingBranchTest.java
│               ├── WebSocketGameSessionFinalCoverageTest.java
│               └── ActionGroupTest.java
├── pom.xml                                  # Maven 配置
├── Readme.md                                # 本文件
├── Demo展示指南.md                          # Demo 展示完整指南
├── QUICK_START.md                           # 快速啟動指南
├── game_rules.md                            # 麻將規則說明
├── PQ.md                                    # 專案需求
├── start.ps1                                # PowerShell 啟動腳本（Windows）
├── start.bat                                # Batch 啟動腳本（Windows）
├── start_demo.ps1                           # Demo 演示啟動腳本
├── start_demo.sh                            # Demo 演示啟動腳本（Unix）
├── setup_env.ps1                            # 環境設定腳本
└── logs/                                    # 日誌目錄
    └── mahjong.log
```

---

## 🛠 疑難排解

### 問題 1：端口被占用

**錯誤訊息**：
```
Address already in use: bind
```

**解決方法（Windows PowerShell）**：
```powershell
# 停止所有 Java 進程
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force

# 等待 2 秒後重新啟動
Start-Sleep -Seconds 2
.\start.ps1
```

**解決方法（Unix/Linux/Mac）**：
```bash
killall java
# 然後重新啟動伺服器
```

### 問題 2：無法連線

**檢查項目**：
1. 確認伺服器已啟動（看到 `Server started on port: 8888`）
2. 確認瀏覽器開啟的是 `index.html`（不是其他文件）
3. 確認沒有防火牆阻擋

### 問題 3：遊戲無法開始

**原因**：需要 4 位玩家才能開始遊戲

**解決方法**：開啟 4 個瀏覽器視窗，分別登入

### 問題 4：測試失敗

**解決方法**：
```powershell
# Windows PowerShell
mvn clean compile test

# 或使用啟動腳本重新編譯
.\start.ps1
```

**如果仍有問題**：
```powershell
# 檢查具體失敗的測試
mvn test -Dtest=失敗的測試類別名稱
```

---

## 📊 專案指標

### 已達成目標

- ✅ **測試數量**：488 tests（目標：50+，超標 876%）
- ✅ **總複雜度**：482（目標：>200，超標 141%）
- ✅ **整體 Branch Coverage**：90%（核心邏輯 com.mahjong.logic 達到 91%）
- ✅ **整體 Line Coverage**：90%（核心邏輯 com.mahjong.logic 達到 95%）
- ✅ **Bug & Fix 案例**：10 個（涵蓋 Critical、Major、Logic 等各類問題）
- ✅ **程式碼品質**：通過編譯，無重大錯誤

### 各套件覆蓋率詳情

| 套件 | Branch Coverage | Line Coverage | Instruction Coverage |
|------|----------------|---------------|---------------------|
| com.mahjong.logic | **91%** ✅ | **95%** ✅ | **95%** ✅ |
| com.mahjong.server | **88%** ✅ | **84%** ✅ | **84%** ✅ |
| com.mahjong.model | n/a | **93%** ✅ | **93%** ✅ |
| com.mahjong.client | **83%** ✅ | **96%** ✅ | **96%** ✅ |
| **整體** | **90%** ✅ | **90%** ✅ | **90%** ✅ |

### 待達成目標

- ✅ **整體 Branch Coverage**：90%（目標：90%+，已達成！）
- ⚠️ **com.mahjong.client 覆蓋率**：83%（已大幅提升，主要為舊版 Socket 客戶端）

---

## 📚 相關文件

### 核心文件
- `Readme.md` - 本文件，專案概述與使用說明
- `Demo展示指南.md` - Demo 展示完整指南（**推薦閱讀**）
- `Project issues and solutions.md` - 專案問題分析與解決方案
- `QUICK_START.md` - 快速啟動指南（Windows PowerShell 環境設定）

### 開發文件
- `agent.md` - AI Agent 開發指南
- `PQ.md` - 專案需求規格
- `game_rules.md` - 麻將規則說明

### 報告文件
- `書面報告檢視與修正建議.md` - 書面報告檢視與修正建議

### 啟動腳本
- `start.ps1` - PowerShell 啟動腳本（Windows，推薦）
- `start.bat` - Batch 啟動腳本（Windows）
- `start_demo.ps1` - Demo 演示啟動腳本（Windows）
- `start_demo.sh` - Demo 演示啟動腳本（Unix/Linux/Mac）
- `setup_env.ps1` - 環境設定腳本

---

## 👥 開發團隊

- **專案目標**：展示軟體測試能力
- **開發語言**：Java 17
- **架構模式**：Client-Server (WebSocket)

---

## 📝 授權

本專案為學術專案，用於軟體測試課程展示。

---

## 🎓 Demo 提示

### 上台前準備

#### 檢查清單

- [ ] **環境準備**
  - [ ] Java 17 已安裝並可用
  - [ ] Maven 已安裝並可用
  - [ ] 瀏覽器已準備（Chrome/Safari/Firefox）
  - [ ] 網路連線正常（WebSocket 需要）

- [ ] **功能測試**
  - [ ] 伺服器可以正常啟動
  - [ ] 4 個瀏覽器視窗可以正常連線
  - [ ] 遊戲可以正常開始
  - [ ] 吃碰槓功能正常
  - [ ] 測試可以正常執行

- [ ] **演示準備**
  - [ ] 準備測試結果截圖（備用）
  - [ ] 準備覆蓋率報告截圖（備用）
  - [ ] 熟悉演示流程和時間分配
  - [ ] 準備回答常見問題

- [ ] **時間控制**
  - [ ] 準備 10-15 分鐘的演示內容
  - [ ] 準備簡短版本（5-8 分鐘）以防時間不足

### 演示技巧

1. **強調測試**：重點展示測試覆蓋率和程式碼品質
2. **展示架構**：說明 WebSocket 即時通訊優勢
3. **互動演示**：邀請觀眾參與遊戲（如果時間允許）
4. **技術亮點**：強調重構、測試、程式碼品質

### 快速演示腳本

**一鍵啟動 Demo**（可選）：
```bash
# 在專案根目錄執行
./start_demo.sh
```

如果沒有腳本，可以手動執行：
```bash
# 終端 1：啟動伺服器
killall java && mvn exec:java -Dexec.mainClass="com.mahjong.server.MahjongWebSocketServer"

# 終端 2：開啟 4 個瀏覽器
for i in {1..4}; do open src/main/resources/web/index.html; sleep 1; done
```
