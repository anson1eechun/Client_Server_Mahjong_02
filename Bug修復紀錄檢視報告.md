# Bug 修復紀錄檢視報告

## 整體評估

**總體評價**: ✅ **大部分準確，但部分描述需要微調**

大部分 Bug 修復紀錄都有實際代碼支持，但以下幾點需要修正：

---

## 詳細檢視結果

### ✅ 1. Critical Bug: WinStrategy 順子判定錯誤

**狀態**: ✅ **完全符合**

**驗證**:
- ✅ 測試文件存在：`src/test/java/com/mahjong/logic/WinStrategyTest.java`
- ✅ 測試方法存在：`testBoundarySequence_789()`, `testBoundarySequence_123()`
- ✅ 代碼註解確認：`WinStrategy.java` 第 11 行註解提到「修復內容：順子判定邏輯修正（7,8,9 萬可以組成順子）」

**建議**: 無需修改

---

### ✅ 2. Bug: 吃碰後遊戲卡死

**狀態**: ✅ **完全符合**

**驗證**:
- ✅ `performChow()` 方法（第 526-527 行）確實發送 `discardMsg` 提示出牌
- ✅ `performPong()` 方法（第 641-643 行）確實發送 `discardMsg` 提示出牌
- ✅ 代碼註解確認：「✅ 修復：PONG 後需要出牌（不摸牌），明確提示玩家出牌」

**建議**: 無需修改

---

### ✅ 3. Feature: 動作優先級仲裁錯誤

**狀態**: ✅ **完全符合**

**驗證**:
- ✅ `ActionProcessor.java` 確實有優先級機制：
  - `HU(1)` - 最高優先級
  - `KONG(2)`, `PONG(2)` - 次高優先級
  - `CHOW(3)` - 最低優先級
- ✅ `checkPossibleActions()` 方法（第 140 行）確實按優先級排序：`actions.sort(Comparator.comparingInt(a -> a.getType().getPriority()))`
- ✅ `WebSocketGameSession.resolveDiscard()` 使用 `ActionGroup` 按優先級處理（tierHu=1, tierPong=2, tierChow=3）

**建議**: 無需修改

---

### ✅ 4. Critical Bug: 胡牌檢測破壞原始手牌

**狀態**: ✅ **完全符合**

**驗證**:
- ✅ `HandValidator.canHu()` 方法（第 120-125 行）確實使用 `cloneHand()` 方法
- ✅ 代碼註解確認：「Uses clone to avoid modifying the original hand」
- ✅ `cloneHand()` 方法（第 130-141 行）確實複製所有 standing tiles 和 open melds
- ✅ `TingDetector` 和 `ActionProcessor` 也使用相同的 `cloneHand` 模式

**建議**: 無需修改

---

### ✅ 5. Bug: 槓牌後流程錯誤 (Missing Draw)

**狀態**: ✅ **完全符合**

**驗證**:
- ✅ `performKong()` 方法（第 406 行）確實調用 `startTurn()` 來補牌
- ✅ 代碼註解確認：「Kong -> Draw Replacement Tile -> Discard」
- ✅ `performConcealedKong()` 方法（第 423-426 行）也有補牌邏輯：`Tile replacement = engine.drawTile()`

**建議**: 無需修改

---

### ✅ 6. Major Bug: 牌牆耗盡導致例外

**狀態**: ✅ **完全符合**

**驗證**:
- ✅ `MahjongRuleEngine.drawTile()` 方法（第 38-43 行）確實有 `wall.isEmpty()` 檢查
- ✅ 當牌牆為空時返回 `null`，而不是拋出異常
- ✅ 代碼註解確認：「Wall exhausted (Draw game)」

**建議**: 無需修改

---

### ⚠️ 7. Bug: 移除手牌物件身分不一致

**狀態**: ⚠️ **部分準確，需要微調描述**

**實際情況**:
- ✅ `PlayerHand` 確實有兩個 `removeTile` 方法：
  - `removeTile(Tile tile)` - 第 27-29 行
  - `removeTile(String tileName)` - 第 58-67 行
- ⚠️ **但代碼中仍在使用兩種方式**：
  - `WebSocketGameSession.performChow()` 使用 `removeTile(String)`
  - `WebSocketGameSession.performPong()` 使用 `removeTile(String)`
  - `WebSocketGameSession.processPlayerAction()` 使用 `removeTile(String)`

**問題**: 描述說「統一使用 removeTile(Tile tile)」，但實際代碼中仍在使用 `removeTile(String)`

**建議修改**:
```
原描述：統一使用 removeTile(Tile tile)，並在內部覆寫 equals() 方法確保比對正確。

建議改為：重構 PlayerHand 類別，提供兩種 removeTile 方法（Tile 物件和 String ID），
內部統一使用 toString() 比對確保正確移除，避免因物件參考不同導致的移除失敗。
```

---

### ⚠️ 8. Security Bug: 非法出牌驗證

**狀態**: ⚠️ **部分準確，需要補充說明**

**實際情況**:
- ✅ `WebSocketGameSession.handleActionResponse()` 確實有驗證（第 314-315 行）：
  ```java
  List<String> allowed = currentActionGroup.playerActions.get(playerIndex);
  if (allowed != null && allowed.contains(type))
  ```
- ✅ `WebSocketGameSession.processPlayerAction()` 有檢查移除結果（第 121-142 行）：
  ```java
  boolean removed = hands.get(playerIndex).removeTile(tileStr);
  if (removed) { ... } else {
      logger.warn("Player {} tried to discard {} but tile not found in hand", ...);
  }
  ```

**問題**: 描述說「後端加入伺服器端驗證」，但實際上：
- 有驗證動作是否在允許列表中
- 有檢查移除是否成功（如果牌不在手牌中，`removeTile` 返回 false）
- 但沒有明確的「拋出異常並記錄作弊行為」的邏輯

**建議修改**:
```
原描述：後端加入伺服器端驗證 (Server-Side Validation)，在處理 DISCARD 動作前，
先確認 playerHand.contains(tile)，若無則拋出異常並記錄作弊行為。

建議改為：後端加入伺服器端驗證 (Server-Side Validation)，在處理 DISCARD 動作時，
檢查 removeTile() 的返回值，若移除失敗則記錄警告日誌，防止玩家打出不存在的牌。
同時在 handleActionResponse() 中驗證動作是否在允許列表中。
```

---

### ⚠️ 9. Bug: 莊家起手牌數錯誤

**狀態**: ⚠️ **描述不準確，實際代碼是正確的**

**實際情況**:
- ✅ `MahjongRuleEngine.dealInitialHands()` 確實發給所有人 16 張（第 50-58 行）
- ✅ **但** `WebSocketGameSession.start()` 方法（第 47-51 行）額外給莊家（Player 0）摸一張牌：
  ```java
  // ✅ P0-1: 莊家先摸 1 張牌（莊家應為 17 張起手）
  Tile firstDraw = engine.drawTile();
  if (firstDraw != null) {
      hands.get(0).addTile(firstDraw);
  }
  ```
- ✅ 所以實際結果是正確的：莊家 17 張，閒家 16 張

**問題**: 描述說「系統錯誤地發給所有人 16 張」，但實際上：
- `dealInitialHands` 發 16 張是**設計如此**（因為莊家會額外摸一張）
- 最終結果是正確的

**建議修改**:
```
原描述：遊戲開始時，莊家應該有 17 張牌（含摸牌），閒家 16 張，但系統錯誤地發給所有人 16 張，
導致莊家無法出牌。

建議改為：遊戲開始時，莊家應該有 17 張牌（含摸牌），閒家 16 張。初始發牌邏輯
dealInitialHands() 發給所有人 16 張，然後在 start() 方法中額外給莊家摸一張牌，
確保莊家有 17 張。此設計符合台灣麻將規則。
```

或者，如果確實有過 Bug（後來修復了），可以改為：
```
原描述：...但系統錯誤地發給所有人 16 張，導致莊家無法出牌。

建議改為：...但初始實作時，系統錯誤地發給所有人 16 張後未給莊家額外摸牌，
導致莊家無法出牌。已修復：在 start() 方法中額外給莊家摸一張牌。
```

---

### ✅ 10. Logic Bug: 七對子誤判

**狀態**: ✅ **完全符合**

**驗證**:
- ✅ `WinStrategy.isSevenPairs()` 方法（第 203-240 行）確實有處理 4 張相同牌的情況
- ✅ 第 232-233 行：`else if (count == 4) { pairCount += 2; // 4 tiles = 2 pairs }`
- ✅ 代碼註解確認：「4 tiles = 2 pairs」

**建議**: 無需修改

---

## 總結與建議

### 需要修改的項目

1. **Bug #7** (移除手牌物件身分不一致) - 描述需要微調
2. **Bug #8** (非法出牌驗證) - 描述需要補充說明
3. **Bug #9** (莊家起手牌數錯誤) - 描述不準確，需要修正

### 完全符合的項目

- ✅ Bug #1: WinStrategy 順子判定錯誤
- ✅ Bug #2: 吃碰後遊戲卡死
- ✅ Bug #3: 動作優先級仲裁錯誤
- ✅ Bug #4: 胡牌檢測破壞原始手牌
- ✅ Bug #5: 槓牌後流程錯誤
- ✅ Bug #6: 牌牆耗盡導致例外
- ✅ Bug #10: 七對子誤判

---

## 建議的最終版本

建議將需要修改的 3 個 Bug 描述更新為上述建議版本，其他 7 個保持不變。

**整體評價**: 這份 Bug 修復紀錄**整體上是準確且有用的**，只需要微調 3 個描述即可完全符合實際代碼情況。

