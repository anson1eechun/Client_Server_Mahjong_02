# Agent Context & Protocols (AI 協作規範)

## 1. 專案身份與目標 (Project Identity)
* **專案名稱**: Java Multiplayer Mahjong (Testing Focused)
* **專案目標**: 開發一個 Client-Server 架構的麻將遊戲，核心目標並非遊戲性，而是**「極大化軟體測試能力」**。
* **關鍵指標 (KPI)**:
    * WMC (複雜度) > 200 (需透過設計模式將邏輯集中處理)。
    * Branch Coverage > 90% (這是硬指標，**任何邏輯程式碼都必須可被單元測試**)。
    * Bug & Fix >= 10 (需在開發過程中記錄重構與修復過程)。

## 2. 技術堆疊 (Tech Stack)
* **Language**: Java 17+
* **Build Tool**: Maven
* **Testing**: Junit 5 (Jupiter), Mockito (處理 Socket/UI 隔離), Jacoco (覆蓋率報告)
* **Static Analysis**: PMD
* **Network**: Java IO (Socket/ServerSocket) 或 Java NIO
* **UI**: JavaFX (推薦) 或 Swing (需將 View 與 Logic 徹底分離)

## 3. 開發準則 (Development Guidelines) - **AI 必須嚴格遵守**

### 3.1 架構原則 (MVC/MVP)
* **禁止**將遊戲邏輯寫在 UI 類別中。UI 類別 (`View`) 只能負責顯示與捕捉輸入。
* 所有麻將規則（胡牌判斷、吃碰槓檢核、算分）必須封裝在純 Java 類別 (`Model`) 中，不依賴任何 UI 或 Network 庫，以確保能進行高效率的單元測試。

### 3.2 測試驅動 (Testability First)
* 在生成功能代碼前，請先思考：「這段程式碼如何測試？」
* 如果遇到 `Socket` 連線或 `Random` 洗牌等難以測試的依賴，必須使用 **Dependency Injection (依賴注入)** 或 **Interface** 隔離，以便使用 Mock。
* **例子**: 不要直接在方法裡 `new Random()`，而是透過建構子傳入 `Random` 物件，測試時才能固定種子 (Seed)。

### 3.3 代碼風格
* 變數命名需清晰可讀 (e.g., `isWinningHand` 而非 `flag`)。
* 複雜邏輯需加上註解 (Javadoc)。

## 4. 當前專案狀態 (Project Status)
* **Phase**: [初始化/規劃階段]
* **Current Task**: 定義需求規格與類別圖架構。
* **Next Step**: 建立 Maven 專案結構與基礎 Server/Client 通訊骨架。

## 5. 檔案結構預覽 (Directory Structure)
```text
src/
  main/java/com/mahjong/
    ├── server/        # 伺服器端邏輯 (Socket, Room, ClientHandler)
    ├── client/        # 客戶端邏輯 (Socket, UI Controller)
    ├── logic/         # 麻將核心規則 (重點測試區: RuleEngine, Tile, PlayerHand)
    ├── model/         # 資料模型 (Packet, GameState)
    └── App.java
  test/java/com/mahjong/
    ├── logic/         # 針對規則的密集單元測試
    └── server/        # 針對連線的 Mock 測試