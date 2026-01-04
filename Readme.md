# Client-Server 台灣麻將遊戲

## 📋 專案概述

本專案是一個基於 **WebSocket** 的多人線上台灣麻將遊戲，採用 **Java** 後端 + **Web** 前端架構。專案重點展示**軟體測試能力**，包含完整的白箱測試、黑箱測試、系統測試和整合測試。

### 核心目標
- ✅ **軟體測試能力展示**：580+ 個測試方法，整體 Branch Coverage 90%（核心邏輯 91%）
- ✅ **高程式碼品質**：總複雜度 482（WMC > 200，超標 141%），通過 PMD 檢查
- ✅ **完整遊戲邏輯**：支援吃、碰、槓、胡等完整麻將規則
- ✅ **Bug 修復紀錄**：10 個 Bug & Fix 案例，涵蓋 Critical、Major、Logic 等各類問題
- ✅ **全面測試覆蓋**：涵蓋單元測試、分支覆蓋、黑箱測試、系統測試、整合測試

### 技術棧
- **後端**：Java 21, Maven, WebSocket (Java-WebSocket)
- **前端**：HTML5, JavaScript, CSS3
- **測試**：JUnit 5, Mockito, JaCoCo
- **通訊**：WebSocket (JSON 封包)
- **代碼品質**：PMD, JaCoCo

---

## 🚀 快速開始

### 環境需求
- Java 21 或以上
- Maven 3.6+
- 瀏覽器（Chrome, Firefox, Safari, Edge）

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

## 🧪 測試說明

### 📊 測試統計總覽

| 項目 | 數量 | 狀態 |
|------|------|------|
| **總測試文件數** | 39 個 | ✅ |
| **總測試方法數** | 580+ 個 | ✅ |
| **測試框架** | JUnit 5 (Jupiter) | ✅ |
| **Mock 框架** | Mockito 5.14.2 | ✅ |
| **覆蓋率工具** | JaCoCo 0.8.12 | ✅ |
| **整體 Branch Coverage** | 90% | ✅ |
| **核心邏輯 Branch Coverage** | 91% | ✅ |

### 🎯 測試類型分類

本專案採用**多層次測試策略**，涵蓋以下測試類型：

#### 1. **單元測試 (Unit Tests)** - 白箱測試

**特徵**：測試單一類別或方法的功能，使用 Mock 隔離依賴

**文件命名**：`*Test.java`（不含 Branch/Coverage/Integration/Exception/Advanced/BlackBox/System）

**測試文件**（7 個）：
- `MahjongClientTest.java` - 測試客戶端基本功能（14 個測試方法）
- `MahjongRuleEngineTest.java` - 測試規則引擎（2 個測試方法）
- `HandValidatorTest.java` - 測試手牌驗證（2 個測試方法）
- `ActionProcessorTest.java` - 測試動作處理（9 個測試方法）
- `WebSocketGameSessionTest.java` - 測試遊戲會話基本功能（20 個測試方法）
- `MahjongWebSocketServerTest.java` - 測試 WebSocket 伺服器（17 個測試方法）
- `ActionGroupTest.java` - 測試動作群組（6 個測試方法）

**測試方法**：
- 使用 `@Mock` 和 Mockito 模擬依賴
- 測試正常流程和邊界條件
- 驗證方法返回值

---

#### 2. **分支覆蓋率測試 (Branch Coverage Tests)** - 白箱測試

**特徵**：專門針對程式碼分支覆蓋率，測試所有 if-else、switch-case 分支

**文件命名**：`*BranchTest.java` 或 `*BranchCoverage90Test.java`

**測試文件**（15 個）：
- `MahjongClientBranchTest.java` - 客戶端分支覆蓋（12 個測試方法）
- `ActionProcessorBranchTest.java` - 動作處理分支覆蓋（13 個測試方法）
- `ActionProcessorAdditionalBranchTest.java` - 額外分支覆蓋（12 個測試方法）
- `HandValidatorBranchTest.java` - 手牌驗證分支覆蓋（15 個測試方法）
- `PlayerHandBranchTest.java` - 玩家手牌分支覆蓋（13 個測試方法）
- `PlayerHandAdditionalBranchTest.java` - 額外分支覆蓋（9 個測試方法）
- `ScoringCalculatorBranchTest.java` - 計分計算分支覆蓋（27 個測試方法）
- `TingDetectorBranchTest.java` - 聽牌檢測分支覆蓋（8 個測試方法）
- `WinStrategyBranchTest.java` - 胡牌策略分支覆蓋（23 個測試方法）
- `WinStrategyAdditionalBranchTest.java` - 額外分支覆蓋（30 個測試方法）
- `MeldBranchTest.java` - 面子分支覆蓋（11 個測試方法）
- `MahjongRuleEngineBranchTest.java` - 規則引擎分支覆蓋（8 個測試方法）
- `MahjongWebSocketServerBranchTest.java` - 伺服器分支覆蓋（8 個測試方法）
- `WebSocketGameSessionBranchCoverage90Test.java` - 遊戲會話 90% 分支覆蓋
- `WebSocketGameSessionAdditionalBranchTest.java` - 額外分支覆蓋（18 個測試方法）
- `WebSocketGameSessionFinalBranchTest.java` - 最終分支覆蓋（15 個測試方法）
- `WebSocketGameSessionRemainingBranchTest.java` - 剩餘分支覆蓋（22 個測試方法）

**測試方法**：
- 使用反射 (`java.lang.reflect`) 測試私有方法
- 測試所有條件分支（true/false）
- 測試 switch-case 的每個 case
- 測試異常分支

---

#### 3. **覆蓋率測試 (Coverage Tests)** - 白箱測試

**特徵**：專門針對整體覆蓋率（指令、行、分支）

**文件命名**：`*CoverageTest.java` 或 `*FinalCoverageTest.java`

**測試文件**（2 個）：
- `WebSocketGameSessionCoverageTest.java` - 遊戲會話覆蓋率（59 個測試方法）
- `WebSocketGameSessionFinalCoverageTest.java` - 最終覆蓋率（25 個測試方法）

**測試方法**：
- 測試未覆蓋的程式碼路徑
- 使用反射測試私有方法
- 測試邊界條件和異常情況

---

#### 4. **異常處理測試 (Exception Tests)** - 白箱測試

**特徵**：專門測試異常處理分支和錯誤情況

**文件命名**：`*ExceptionTest.java`

**測試文件**（1 個）：
- `WebSocketGameSessionExceptionTest.java` - 遊戲會話異常處理（12 個測試方法）

**測試內容**：
- ✅ 測試所有 try-catch 的異常分支
- ✅ 測試網路異常（SocketException, IOException）
- ✅ 測試 JSON 解析異常
- ✅ 測試空指標異常
- ✅ 測試非法參數異常

**測試方法**：
- 使用 `doThrow()` 模擬異常
- 使用 `assertThrows()` 驗證異常
- 測試異常處理邏輯

---

#### 5. **進階測試 (Advanced Tests)** - 白箱測試

**特徵**：測試私有方法、複雜場景、邊界條件

**文件命名**：`*AdvancedTest.java`

**測試文件**（1 個）：
- `WebSocketGameSessionAdvancedTest.java` - 遊戲會話進階測試（21 個測試方法）

**測試內容**：
- ✅ 使用反射測試私有方法
- ✅ 測試複雜的遊戲狀態轉換
- ✅ 測試邊界條件
- ✅ 測試併發場景

**測試方法**：
- 使用 `java.lang.reflect` 訪問私有方法
- 使用 `Field.setAccessible(true)` 訪問私有欄位
- 測試複雜的狀態機

---

#### 6. **整合測試 (Integration Tests)** - 灰箱/黑箱測試

**特徵**：測試多個組件協同工作，模擬完整遊戲流程

**文件命名**：`*IntegrationTest.java`

**測試文件**（3 個）：
- `GameFlowIntegrationTest.java` - 完整遊戲流程整合測試（6 個測試方法）
  - ✅ 完整遊戲流程：發牌→摸牌→出牌→吃碰槓→胡牌
  - ✅ 多個類別協同工作（MahjongRuleEngine + ActionProcessor + PlayerHand）
  - ✅ 動作優先級測試（胡 > 碰 > 吃）
  - ✅ 從牌牆摸牌流程
  - ✅ 發牌流程驗證

- `WebSocketServerIntegrationTest.java` - WebSocket 伺服器與遊戲會話整合測試（15+ 個測試方法）
  - ✅ 測試 MahjongWebSocketServer + WebSocketGameSession 整合
  - ✅ 測試遊戲生命週期（開始、進行、結束）
  - ✅ 測試多客戶端連接和狀態同步
  - ✅ 測試玩家斷線處理

- `ClientServerIntegrationTest.java` - 客戶端與伺服器整合測試（15+ 個測試方法）
  - ✅ 使用模擬 WebSocket 客戶端測試完整訊息流
  - ✅ 測試多客戶端互動和錯誤處理
  - ✅ 測試請求-回應配對

**測試方法**：
- 使用 Mock WebSocket 模擬客戶端
- 測試完整業務流程
- 驗證多個組件之間的互動

---

#### 7. **黑箱測試 (Black Box Tests)** - 黑箱測試

**特徵**：從外部視角測試，不依賴內部實作

**文件命名**：`*BlackBoxTest.java`

**測試文件**（2 個）：
- `WebSocketAPIBlackBoxTest.java` - WebSocket API 黑箱測試（30+ 個測試方法）
  - ✅ 測試所有 Command 的輸入輸出（LOGIN, PLAY_CARD, ACTION）
  - ✅ 測試錯誤處理（無效輸入、格式錯誤等）
  - ✅ 測試邊界條件（空字串、null、特殊字符、超長字串）
  - ✅ 測試時序錯誤（遊戲開始前的動作）

- `GameFlowBlackBoxTest.java` - 遊戲流程黑箱測試（12+ 個測試方法）
  - ✅ 從玩家視角測試完整遊戲流程
  - ✅ 測試動作優先級和非法動作處理
  - ✅ 測試狀態同步機制
  - ✅ 測試錯誤處理（無效牌名、缺少欄位等）

**測試方法**：
- 只通過 WebSocket API 測試
- 不直接訪問內部狀態
- 驗證系統行為是否符合預期

---

#### 8. **系統測試 (System Tests)** - 系統測試

**特徵**：測試整個系統的併發和穩定性

**文件命名**：`*SystemTest.java`

**測試文件**（1 個）：
- `MultiClientSystemTest.java` - 多客戶端系統測試（10+ 個測試方法）
  - ✅ 測試多個客戶端同時連接
  - ✅ 測試併發登入處理
  - ✅ 測試併發動作處理
  - ✅ 測試狀態同步機制
  - ✅ 測試系統在併發情況下的穩定性
  - ✅ 測試廣播訊息機制
  - ✅ 測試併發壓力下的系統表現

**測試方法**：
- 使用 ExecutorService 模擬併發
- 測試系統在壓力下的穩定性
- 驗證狀態同步機制

---

### 📈 測試覆蓋率詳情

| 套件 | Branch Coverage | Line Coverage | Instruction Coverage | 狀態 |
|------|----------------|---------------|---------------------|------|
| com.mahjong.logic | **91%** | **95%** | **95%** | ✅ |
| com.mahjong.server | **88%** | **84%** | **84%** | ✅ |
| com.mahjong.model | n/a | **93%** | **93%** | ✅ |
| com.mahjong.client | **83%** | **96%** | **96%** | ✅ |
| **整體** | **90%** | **90%** | **90%** | ✅ |

---

### 🛠 執行測試

#### 執行所有測試
```bash
mvn test
```

#### 執行特定測試類別
```bash
# 單元測試
mvn test -Dtest=WinStrategyTest
mvn test -Dtest=ActionProcessorTest

# 整合測試
mvn test -Dtest=GameFlowIntegrationTest
mvn test -Dtest=WebSocketServerIntegrationTest

# 黑箱測試
mvn test -Dtest=WebSocketAPIBlackBoxTest
mvn test -Dtest=GameFlowBlackBoxTest

# 系統測試
mvn test -Dtest=MultiClientSystemTest
```

#### 生成覆蓋率報告
```bash
# 執行測試並生成報告
mvn clean test jacoco:report

# 查看覆蓋率報告
# Windows PowerShell:
Start-Process "target\site\jacoco\index.html"
# Unix/Linux/Mac:
# open target/site/jacoco/index.html
```

#### 執行 PMD 代碼品質檢查
```bash
mvn pmd:pmd
```

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
│   │   │   │   ├── WinStrategy.java       # 胡牌判定演算法（WMC: ~85）
│   │   │   │   ├── ActionProcessor.java   # 動作處理與優先級仲裁（WMC: ~75）
│   │   │   │   ├── HandValidator.java    # 手牌驗證（WMC: ~35）
│   │   │   │   ├── PlayerHand.java        # 玩家手牌管理（WMC: ~25）
│   │   │   │   ├── Meld.java              # 面子（吃碰槓）類別（WMC: ~20）
│   │   │   │   ├── MahjongRuleEngine.java # 規則引擎（發牌、摸牌）（WMC: ~15）
│   │   │   │   ├── ScoringCalculator.java # 台數計算（WMC: ~45）
│   │   │   │   ├── TingDetector.java      # 聽牌檢測（WMC: ~30）
│   │   │   │   └── Tile.java              # 牌類別（Enum）（WMC: ~3）
│   │   │   ├── server/                    # 伺服器端（3 個類別）
│   │   │   │   ├── MahjongWebSocketServer.java  # WebSocket 伺服器（WMC: ~25）
│   │   │   │   ├── WebSocketGameSession.java    # 遊戲會話管理（WMC: ~180）
│   │   │   │   └── ActionGroup.java              # 動作群組（WMC: ~5）
│   │   │   └── model/                     # 資料模型
│   │   │       ├── Command.java           # 命令列舉（WMC: 1）
│   │   │       └── Packet.java            # 封包類別（WMC: ~8）
│   │   └── resources/
│   │       ├── logback.xml                 # 日誌配置
│   │       └── web/                        # Web 前端
│   │           ├── index.html              # 主頁面
│   │           ├── game.js                 # 遊戲邏輯（前端）
│   │           └── style.css               # 樣式表
│   └── test/
│       └── java/com/mahjong/
│           ├── client/                     # 客戶端測試（2 個測試類別）
│           │   ├── MahjongClientTest.java
│           │   └── MahjongClientBranchTest.java
│           ├── logic/                      # 邏輯層測試（20 個測試類別）
│           │   ├── *Test.java              # 單元測試
│           │   ├── *BranchTest.java        # 分支覆蓋率測試
│           │   ├── *AdditionalBranchTest.java  # 額外分支覆蓋率測試
│           │   └── GameFlowIntegrationTest.java # 整合測試
│           └── server/                     # 伺服器層測試（17 個測試類別）
│               ├── *Test.java              # 單元測試
│               ├── *BranchTest.java        # 分支覆蓋率測試
│               ├── *CoverageTest.java      # 覆蓋率測試
│               ├── *ExceptionTest.java     # 異常處理測試
│               ├── *AdvancedTest.java      # 進階測試
│               ├── *BlackBoxTest.java      # 黑箱測試（新增）
│               ├── *IntegrationTest.java    # 整合測試（新增）
│               └── *SystemTest.java        # 系統測試（新增）
├── pom.xml                                  # Maven 配置
├── Readme.md                                # 本文件
├── TEST_TYPES_ANALYSIS.md                   # 測試類型分析報告
├── TESTING_GAP_ANALYSIS.md                 # 測試缺口分析報告
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

## 📚 文件說明

### 核心文件

#### 1. **Readme.md**（本文件）
- **用途**：專案概述與使用說明
- **內容**：
  - 專案概述和核心目標
  - 快速開始指南
  - 詳細的測試說明（各類型測試方法）
  - 專案結構
  - 文件說明
  - 專案指標

#### 2. **TEST_TYPES_ANALYSIS.md**
- **用途**：測試類型分析報告
- **內容**：
  - 測試統計（39 個測試文件，580+ 個測試方法）
  - 8 種測試類型詳細分類
  - 測試文件結構
  - 測試技術棧
  - 測試品質指標
  - 最新更新（2026-01-04）

#### 3. **TESTING_GAP_ANALYSIS.md**
- **用途**：測試缺口分析報告
- **內容**：
  - 測試類型覆蓋情況（白箱、黑箱、系統、整合測試）
  - 各類型測試的詳細分析
  - 已完成的測試和缺失的測試
  - 優先級建議和實施計劃
  - 測試覆蓋率目標

#### 4. **Demo展示指南.md**
- **用途**：Demo 展示完整指南
- **內容**：
  - 準備階段說明
  - 詳細的演示流程
  - 時間分配建議
  - 演示技巧和注意事項

#### 5. **QUICK_START.md**
- **用途**：快速啟動指南
- **內容**：
  - Windows PowerShell 環境設定
  - 快速啟動步驟
  - 常見問題解決方案

### 開發文件

#### 6. **PQ.md**
- **用途**：專案需求規格
- **內容**：
  - 核心功能模組定義
  - 專案限制與指標（WMC > 200, 測試數 >= 50）
  - 規則簡化聲明

#### 7. **game_rules.md**
- **用途**：麻將規則說明
- **內容**：
  - 牌組設定
  - 開局流程
  - 完整 Workflow 流程圖
  - 特殊規則處理



## 📊 專案指標

### 已達成目標

| 指標 | 目標 | 實際 | 達成率 | 狀態 |
|------|------|------|--------|------|
| **測試數量** | 50+ | 580+ | 1060% | ✅ |
| **總複雜度 (WMC)** | >200 | 482 | 241% | ✅ |
| **整體 Branch Coverage** | 90%+ | 90% | 100% | ✅ |
| **核心邏輯 Branch Coverage** | 90%+ | 91% | 101% | ✅ |
| **整體 Line Coverage** | 90%+ | 90% | 100% | ✅ |
| **Bug & Fix 案例** | - | 10 個 | - | ✅ |

### 測試類型覆蓋情況

| 測試類型 | 測試文件數 | 測試方法數 | 覆蓋率 | 狀態 |
|---------|-----------|-----------|--------|------|
| **單元測試** | 7 | 70+ | - | ✅ |
| **分支覆蓋率測試** | 15 | 200+ | 90%+ | ✅ |
| **覆蓋率測試** | 2 | 84 | - | ✅ |
| **異常處理測試** | 1 | 12 | - | ✅ |
| **進階測試** | 1 | 21 | - | ✅ |
| **整合測試** | 3 | 36+ | ~70% | ✅ |
| **黑箱測試** | 2 | 42+ | ~60% | ✅ |
| **系統測試** | 1 | 10+ | ~50% | ✅ |
| **總計** | **39** | **580+** | **90%** | ✅ |

### 各套件覆蓋率詳情

| 套件 | Branch Coverage | Line Coverage | Instruction Coverage |
|------|----------------|---------------|---------------------|
| com.mahjong.logic | **91%** ✅ | **95%** ✅ | **95%** ✅ |
| com.mahjong.server | **88%** ✅ | **84%** ✅ | **84%** ✅ |
| com.mahjong.model | n/a | **93%** ✅ | **93%** ✅ |
| com.mahjong.client | **83%** ✅ | **96%** ✅ | **96%** ✅ |
| **整體** | **90%** ✅ | **90%** ✅ | **90%** ✅ |

### WMC (總複雜度) 分析

**總 WMC**: **482**（目標：>200，超標 141%）

**主要貢獻類別**：
1. **WebSocketGameSession**: ~180 (37%)
2. **WinStrategy**: ~85 (18%)
3. **ActionProcessor**: ~75 (16%)
4. **ScoringCalculator**: ~45 (9%)
5. **其他類別**: ~97 (20%)

**前 3 個類別貢獻了 71% 的總複雜度**，符合專案設計目標。

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

**成功標誌**：
```
Mahjong WebSocket Server started on port: 8888
```

#### 步驟 2：開啟 4 個瀏覽器視窗

**Windows PowerShell（推薦）**：
```powershell
# 使用 Demo 啟動腳本
.\start_demo.ps1
```

**或手動開啟**：
1. 開啟檔案總管，前往：`src\main\resources\web\`
2. 雙擊 `index.html` 開啟第一個玩家視窗
3. 重複步驟 2，總共開啟 **4 個瀏覽器視窗**

### Demo 演示流程

#### 階段 1：登入與連線（1-2 分鐘）
- 在 4 個瀏覽器視窗中分別輸入暱稱並連線
- 展示 WebSocket 即時連線和多人同步機制

#### 階段 2：基本遊戲流程（2-3 分鐘）
- 展示發牌、摸牌、出牌流程
- 展示輪流機制和狀態同步

#### 階段 3：吃碰槓動作（3-4 分鐘）
- 展示碰牌、吃牌動作
- 展示動作優先級（胡 > 碰/槓 > 吃）

#### 階段 4：胡牌判定（2-3 分鐘）
- 展示自摸胡牌
- 展示胡牌演算法和遊戲結束機制

#### 階段 5：測試覆蓋率展示（1-2 分鐘）
- 執行測試並展示結果
- 展示覆蓋率報告

**總計時間**：約 13 分鐘

---

## 🎯 功能特色

### 核心功能
- ✅ **多人連線**：支援 4 人同時遊戲
- ✅ **即時同步**：WebSocket 即時狀態更新
- ✅ **完整規則**：吃、碰、槓、胡完整實作
- ✅ **動作優先級**：胡 > 碰/槓 > 吃
- ✅ **自動判定**：系統自動檢測可執行動作

### 技術亮點
- ✅ **高測試覆蓋率**：580+ 個測試方法，整體 Branch Coverage 90%（核心邏輯 91%）
- ✅ **程式碼品質**：總複雜度 482（WMC > 200，超標 141%），通過 PMD 檢查
- ✅ **全面測試策略**：涵蓋 8 種測試類型（單元、分支、覆蓋、異常、進階、整合、黑箱、系統）
- ✅ **重構完成**：Meld 類別重構，移除技術債務
- ✅ **Bug 修復**：10 個 Bug & Fix 案例，涵蓋多種測試方法

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

### 問題 5：Java 版本不匹配

**錯誤訊息**：
```
Unsupported class file major version 65.0
```

**解決方法**：
1. 確認專案使用 Java 21
2. 在 IntelliJ IDEA 中：File → Settings → Build, Execution, Deployment → Build Tools → Maven → Maven Runner → 選擇 Java 21
3. 或在命令列設定 `JAVA_HOME` 環境變數指向 Java 21

---

## 📝 測試詳細說明

### 測試方法分類詳解

#### 白箱測試方法

1. **單元測試方法**
   - 使用 `@Mock` 和 `@InjectMocks` 隔離依賴
   - 使用 `when().thenReturn()` 模擬方法行為
   - 使用 `verify()` 驗證方法調用
   - 使用 `assertThat()` 和 `assertEquals()` 驗證結果

2. **分支覆蓋率測試方法**
   - 使用反射 (`getDeclaredMethod()`, `setAccessible(true)`) 測試私有方法
   - 測試所有 if-else 分支（true 和 false 路徑）
   - 測試 switch-case 的每個 case
   - 測試異常分支（try-catch）

3. **覆蓋率測試方法**
   - 使用 JaCoCo 生成覆蓋率報告
   - 分析未覆蓋的程式碼路徑
   - 針對性地添加測試以提升覆蓋率

4. **異常處理測試方法**
   - 使用 `doThrow().when()` 模擬異常
   - 使用 `assertThrows()` 驗證異常類型
   - 測試異常處理邏輯和錯誤訊息

5. **進階測試方法**
   - 使用 `Field.setAccessible(true)` 訪問私有欄位
   - 使用 `Method.invoke()` 調用私有方法
   - 測試複雜的狀態機和狀態轉換

#### 黑箱測試方法

6. **API 黑箱測試方法**
   - 只通過 WebSocket API 測試
   - 不直接訪問內部狀態
   - 測試各種輸入組合（有效、無效、邊界值）
   - 驗證 API 回應格式和內容

7. **遊戲流程黑箱測試方法**
   - 從玩家視角模擬完整遊戲流程
   - 測試動作優先級和非法動作處理
   - 測試時序錯誤和狀態同步

#### 系統測試方法

8. **多客戶端系統測試方法**
   - 使用 `ExecutorService` 模擬併發
   - 使用 `CountDownLatch` 同步多線程
   - 測試系統在壓力下的穩定性
   - 驗證狀態同步機制

#### 整合測試方法

9. **整合測試方法**
   - 使用 Mock WebSocket 模擬客戶端
   - 測試多個組件協同工作
   - 驗證完整的業務流程
   - 測試組件之間的互動



