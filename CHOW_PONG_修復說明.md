# CHOW/PONG 後遊戲卡住問題修復

## 問題描述

Player 3 在選擇 CHOW 後，遊戲無法繼續進行。

## 問題原因

在 `performChow()` 和 `performPong()` 方法中，當玩家執行 CHOW 或 PONG 後，如果沒有胡牌，代碼只是調用 `broadcastState()` 然後就結束了。雖然 `currentPlayerIndex` 已經設置為執行動作的玩家，`broadcastState()` 也會正確地發送 `turnIndex`，但客戶端可能沒有明確收到「現在輪到你出牌」的指示。

## 修復方案

在 `performChow()` 和 `performPong()` 方法中，當沒有胡牌時，除了調用 `broadcastState()` 外，還明確發送一個「請出牌」的消息給執行動作的玩家。

### 修改的代碼

#### 1. performChow() 方法

```java
// ✅ 修復：CHOW 後需要出牌（不吃牌），明確提示玩家出牌
broadcastState();
Map<String, Object> discardMsg = new HashMap<>();
discardMsg.put("message", "請出牌");
send(players.get(playerIndex), new Packet(Command.GAME_UPDATE, discardMsg));
```

#### 2. performPong() 方法

```java
// ✅ 修復：PONG 後需要出牌（不摸牌），明確提示玩家出牌
broadcastState();
Map<String, Object> discardMsg = new HashMap<>();
discardMsg.put("message", "請出牌");
send(players.get(playerIndex), new Packet(Command.GAME_UPDATE, discardMsg));
```

同時，在 `performPong()` 的 catch 區塊中也添加了錯誤處理，確保在發生錯誤時也會調用 `nextTurn()`。

## 遊戲流程說明

### CHOW 後的流程：
1. 玩家選擇 CHOW
2. 執行 `performChow()`：
   - 移除 2 張手牌
   - 從海底移除被吃的牌
   - 添加 CHOW Meld
   - 設置 `currentPlayerIndex` 為執行 CHOW 的玩家
   - 檢查是否胡牌
   - 如果沒有胡牌：`broadcastState()` + 發送「請出牌」消息
3. 玩家出牌（不吃牌）

### PONG 後的流程：
1. 玩家選擇 PONG
2. 執行 `performPong()`：
   - 移除 2 張手牌
   - 從海底移除被碰的牌
   - 添加 PONG Meld
   - 設置 `currentPlayerIndex` 為執行 PONG 的玩家
   - 檢查是否胡牌
   - 如果沒有胡牌：`broadcastState()` + 發送「請出牌」消息
3. 玩家出牌（不摸牌）

### KONG 後的流程（對比）：
1. 玩家選擇 KONG
2. 執行 `performKong()`：
   - 移除 3 張手牌
   - 從海底移除被槓的牌
   - 添加 KONG Meld
   - 設置 `currentPlayerIndex` 為執行 KONG 的玩家
   - **調用 `startTurn()` 補牌**
3. 補牌後，檢查是否槓上開花
4. 如果沒有胡牌，玩家出牌

## 測試建議

1. **測試 CHOW 後出牌**：
   - 讓一個玩家執行 CHOW
   - 確認該玩家可以正常出牌
   - 確認遊戲流程繼續

2. **測試 PONG 後出牌**：
   - 讓一個玩家執行 PONG
   - 確認該玩家可以正常出牌
   - 確認遊戲流程繼續

3. **測試 CHOW/PONG 後胡牌**：
   - 讓一個玩家在執行 CHOW/PONG 後剛好可以胡牌
   - 確認系統正確提示玩家可以胡牌

4. **測試錯誤處理**：
   - 模擬執行 CHOW/PONG 時發生錯誤
   - 確認系統能夠正確處理錯誤並繼續遊戲

## 相關檔案

- `src/main/java/com/mahjong/server/WebSocketGameSession.java`
  - `performChow()` 方法（約第 405-477 行）
  - `performPong()` 方法（約第 505-580 行）

## 注意事項

1. 這個修復確保了在 CHOW/PONG 後，客戶端能夠明確收到「請出牌」的指示
2. 雖然 `broadcastState()` 已經包含了 `turnIndex`，但額外的消息可以提供更好的用戶體驗
3. 這個修復不會影響其他遊戲流程（如 KONG 後的流程）

