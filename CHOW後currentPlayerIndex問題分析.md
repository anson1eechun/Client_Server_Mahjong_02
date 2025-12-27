# CHOW 後 currentPlayerIndex 問題分析

## 問題描述

Player 3 執行 CHOW 後，嘗試出牌時系統顯示 "not their turn (current: 0)"，說明 `currentPlayerIndex` 沒有被正確設置為 3。

## 問題分析

從代碼邏輯來看：

1. **當 Player 3 執行 CHOW 時**：
   - `handleActionResponse` 調用 `performChow(playerIndex)`，`playerIndex = 3`
   - `performChow` 中執行 `currentPlayerIndex = playerIndex;`，應該將 `currentPlayerIndex` 設置為 3

2. **當 Player 3 嘗試出牌時**：
   - `processPlayerAction` 檢查 `playerIndex == currentPlayerIndex`
   - 但系統顯示 `currentPlayerIndex = 0`，說明 `currentPlayerIndex` 沒有被正確設置

## 可能的原因

### 原因 1: `performChow` 執行時 `currentPlayerIndex` 已經是錯誤值

可能 `performChow` 執行時，`currentPlayerIndex` 已經被其他地方修改成了 0。

### 原因 2: `performChow` 執行後 `currentPlayerIndex` 被重置

可能 `performChow` 執行後，在 `broadcastState()` 或其他地方，`currentPlayerIndex` 被重置成了 0。

### 原因 3: 線程同步問題

雖然所有方法都是 `synchronized`，但可能存在某些異步操作導致 `currentPlayerIndex` 被修改。

## 已添加的調試日誌

為了追蹤問題，已經添加了以下日誌：

1. **在 `performChow` 開始時**：
   ```java
   logger.debug("performChow called for Player {}, currentPlayerIndex before = {}", playerIndex, currentPlayerIndex);
   ```

2. **在設置 `currentPlayerIndex` 後**：
   ```java
   logger.debug("After CHOW: currentPlayerIndex set to {}", currentPlayerIndex);
   ```

3. **在 `broadcastState` 前後**：
   ```java
   logger.debug("Before broadcastState after CHOW: currentPlayerIndex = {}", currentPlayerIndex);
   logger.debug("After broadcastState after CHOW: currentPlayerIndex = {}", currentPlayerIndex);
   ```

4. **在發送「請出牌」消息後**：
   ```java
   logger.debug("After sending discard message after CHOW: currentPlayerIndex = {}", currentPlayerIndex);
   ```

5. **在 `processPlayerAction` 處理 PLAY_CARD 時**：
   ```java
   logger.debug("PLAY_CARD request from Player {}, currentPlayerIndex = {}", playerIndex, currentPlayerIndex);
   ```

## 下一步行動

1. **重新運行服務器並測試**，查看新的日誌輸出
2. **根據日誌確定問題所在**：
   - 如果 `performChow` 執行時 `currentPlayerIndex` 已經是 0，說明問題在 `performChow` 執行前
   - 如果 `performChow` 執行後 `currentPlayerIndex` 是 3，但在 `broadcastState` 後變成 0，說明問題在 `broadcastState`
   - 如果 `performChow` 執行後 `currentPlayerIndex` 一直是 3，但在 Player 3 出牌時變成 0，說明問題在 `performChow` 執行後和出牌之間

3. **根據日誌結果修復問題**

## 可能的修復方案

根據日誌結果，可能需要：

1. **確保 `currentPlayerIndex` 在正確的時機設置**
2. **檢查是否有其他地方會修改 `currentPlayerIndex`**
3. **確保 `broadcastState` 不會影響 `currentPlayerIndex`**
4. **檢查是否有線程同步問題**

