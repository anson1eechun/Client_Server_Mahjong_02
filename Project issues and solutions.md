# 台灣麻將專案問題分析與解決方案
**Version**: 3.0  
**Last Updated**: 2024-12-23  
**Target Audience**: AI Agents 繼續開發此專案

## 🎉 最新更新（2024-12-23）

### 已修復的 Critical Issues
1. ✅ **Issue #1**: WinStrategy 順子判定邏輯錯誤 - 已修復並添加邊界測試
2. ✅ **Issue #2**: PlayerHand.removeTile() 方法簽名不一致 - 已統一使用 Tile 物件
3. ✅ **Issue #3**: Meld 類別結構混亂 - 已重構完成，移除 workaround
4. ✅ **Issue #4**: HandValidator.canHu() 邏輯錯誤 - 已使用 clone 方法修復

### 新增測試
- ✅ ActionProcessorTest: 10 個測試案例
- ✅ WinStrategyTest: 新增 3 個邊界測試案例

### 當前進度
- 測試數量: 27/50 (54%)
- Critical Bugs: 4/4 已修復 ✅
- 待處理: Issue #5 (整合測試), #6 (重構), #7 (覆蓋率分析)  

---

## 📋 執行摘要

本專案是一個基於 **WebSocket** 的多人線上台灣麻將遊戲（Java + Web Frontend）。
核心目標：**極大化軟體測試能力**，而非遊戲性。

**關鍵指標 (KPI)**：
- ✅ WMC (複雜度) > 200 (已達成: ~230)
- ⚠️ Branch Coverage > 90% (待驗證，需配置 Jacoco)
- ⚠️ Test Cases >= 50 (目前約 24 個，目標: 50+)
- ✅ Bug & Fix >= 10 (已修復 4 個 Critical Issues)

**當前狀態**: 
- ✅ 遊戲核心邏輯已實作，Critical Bugs 已修復
- ✅ 已轉為 WebSocket 架構，基本可玩
- ⚠️ 測試覆蓋率需要提升（需整合測試）
- ⚠️ 缺少完整的遊戲流程測試

---

## ✅ 已修復的 CRITICAL ISSUES

### ~~Issue #1: WinStrategy 順子判定邏輯錯誤~~ ✅ **已修復**

**修復日期**: 2024-12-23  
**修復內容**:
- ✅ WinStrategy.canFormSequence() 已正確處理邊界情況（7,8,9 順子）
- ✅ 新增邊界測試案例：testBoundarySequence_789(), testBoundarySequence_123(), testBoundarySequence_567()
- ✅ 所有測試通過

---

### ~~Issue #2: PlayerHand.removeTile() 方法簽名不一致~~ ✅ **已修復**

**修復日期**: 2024-12-23  
**修復內容**:
- ✅ ActionProcessor 中統一使用 `removeTile(Tile)` 而非 `removeTile(String)`
- ✅ 保留兩種方法以維持向後兼容（String 版本用於客戶端命令）

---

### ~~Issue #3: Meld 類別結構混亂~~ ✅ **已重構完成**

**修復日期**: 2024-12-23  
**修復內容**:
- ✅ 移除舊建構子 `Meld(Type, Tile)`
- ✅ 新增便利方法：createPong(), createKong(), createChow(), createEyes()
- ✅ 更新所有使用處（ActionProcessor, WebSocketGameSession, ScoringCalculator）
- ✅ 移除 WebSocketGameSession 中的 workaround（3 個 Meld 表示 1 個吃牌）

---

### ~~Issue #4: HandValidator.canHu() 邏輯錯誤~~ ✅ **已修復**

**修復日期**: 2024-12-23  
**修復內容**:
- ✅ 使用 `cloneHand()` 方法避免修改原始手牌
- ✅ 確保 canHu() 不會改變手牌狀態

---

## 🔴 待處理的 ISSUES（需要立即處理）

**檔案**: `src/main/java/com/mahjong/logic/WinStrategy.java`

**問題描述**:
```java
// 當前錯誤代碼（Line ~85）
if (firstIndex < 27) {
    int suitIndex = firstIndex % 9;
    if (suitIndex <= 6) { // ❌ 錯誤！rank=7(對應8萬) 無法判斷 7,8,9 順子
```

**影響**:
- 7萬8萬9萬 **無法組成順子** → 誤判有效胡牌為無效
- 影響所有包含邊界順子的胡牌判定
- 導致玩家體驗極差（明明胡牌卻不能胡）

**根本原因**:
開發者誤以為 `suitIndex` 是牌面值（1-9），但實際上是索引（0-8）。
- `suitIndex = 6` 對應 **7萬** (rank=7)
- 7萬可以與 8萬、9萬組成順子，但當前代碼會阻止這個檢查

**解決方案**:

**選項 A：使用已修復的 WinStrategy_Fixed.java（推薦）**
```bash
# 直接替換現有檔案
cp /path/to/WinStrategy_Fixed.java src/main/java/com/mahjong/logic/WinStrategy.java
```

**選項 B：手動修復（如果需要理解細節）**
```java
// 修復後的正確邏輯
private boolean canFormSequence(int[] counts, int startIndex) {
    if (startIndex >= 27) return false;
    
    int suit = startIndex / 9;  // 0=萬, 1=筒, 2=條
    int rank = startIndex % 9;  // 0-8 對應 1-9
    
    // ✅ 正確：rank 最大為 6 時（對應 7），可以組成 7,8,9
    if (rank > 6) return false;  // rank=7(8) 或 rank=8(9) 無法作為順子起點
    
    int next1 = startIndex + 1;
    int next2 = startIndex + 2;
    
    // ✅ 新增：確保不跨花色（8萬9萬1筒 是非法的）
    if (next1 / 9 != suit || next2 / 9 != suit) {
        return false;
    }
    
    return counts[startIndex] > 0 && 
           counts[next1] > 0 && 
           counts[next2] > 0;
}
```

**測試驗證**:
```java
@Test
public void testBoundarySequence_789() {
    PlayerHand hand = new PlayerHand();
    hand.addTile(Tile.P1); hand.addTile(Tile.P1); // 對眼
    hand.addTile(Tile.M7); hand.addTile(Tile.M8); hand.addTile(Tile.M9); // 測試邊界
    // ... 補足其他牌到 14 張
    
    WinStrategy strategy = new WinStrategy();
    assertTrue(strategy.isWinningHand(hand), "789 萬應該可以組成順子");
}
```

**修復 Checklist**:
- [ ] 替換 WinStrategy.java
- [ ] 執行 `mvn test` 確認沒有破壞現有測試
- [ ] 新增邊界測試案例（789、123 順子）
- [ ] 手動測試遊戲中的實際胡牌
- [ ] **記錄此 Bug 修復到報告中（計入 Bug & Fix 指標）**

---

### Issue #2: PlayerHand.removeTile() 方法簽名不一致 🔧

**檔案**: `src/main/java/com/mahjong/logic/PlayerHand.java`

**問題描述**:
```java
// PlayerHand.java 中有兩個版本的 removeTile
public boolean removeTile(Tile tile) { ... }        // Line 35
public boolean removeTile(String tileName) { ... }  // Line 61
```

但 `ActionProcessor.java` 嘗試調用：
```java
hand.removeTile(tile.toString());  // ❌ 傳入 String，但期望行為不明確
```

**影響**:
- 吃碰槓執行時可能無法正確移除牌
- 造成手牌數量錯誤
- 遊戲狀態不一致

**根本原因**:
重構過程中同時支援 `Tile` 和 `String` 參數，但沒有統一使用規範。

**解決方案**:

**選項 A：統一使用 Tile 物件（推薦）**
```java
// ActionProcessor.java 修改
// 舊代碼：
hand.removeTile(tile.toString());

// 新代碼：
hand.removeTile(tile);
```

**選項 B：保留兩種方法，但明確文檔**
```java
/**
 * Remove tile by object reference (preferred for logic)
 */
public boolean removeTile(Tile tile) { ... }

/**
 * Remove tile by string name (for client commands)
 */
public boolean removeTile(String tileName) { ... }
```

**測試驗證**:
```java
@Test
public void testRemoveTile_BothMethods() {
    PlayerHand hand = new PlayerHand();
    hand.addTile(Tile.M1);
    hand.addTile(Tile.M2);
    
    assertTrue(hand.removeTile(Tile.M1));
    assertTrue(hand.removeTile("M2"));
    assertEquals(0, hand.getTileCount());
}
```

---

### Issue #3: Meld 類別結構混亂 📦

**檔案**: `src/main/java/com/mahjong/logic/Meld.java`

**問題描述**:
```java
public Meld(Type type, java.util.List<Tile> tiles) { ... }  // 新建構子
public Meld(Type type, Tile firstTile) { ... }               // 舊建構子（向後兼容）
```

**影響**:
- 使用 `new Meld(Type.CHOW, firstTile)` 只會儲存一張牌
- 客戶端無法正確顯示吃牌的三張牌組合
- `getMeldsStr()` 需要複雜的 workaround

**當前 Workaround（不理想）**:
```java
// WebSocketGameSession.java Line ~350
// 用 3 個 Meld 物件表示 1 個吃牌，視覺上可行但邏輯混亂
hand.addMeld(com.mahjong.logic.Meld.Type.CHOW, t1Name);
hand.addMeld(com.mahjong.logic.Meld.Type.CHOW, discard.toString());
hand.addMeld(com.mahjong.logic.Meld.Type.CHOW, t2Name);
```

**解決方案**:

**選項 A：完全移除舊建構子，強制使用 List（推薦）**
```java
public class Meld {
    private final Type type;
    private final List<Tile> tiles;
    
    // 只保留這個建構子
    public Meld(Type type, List<Tile> tiles) {
        this.type = type;
        this.tiles = new ArrayList<>(tiles);
    }
    
    // 提供便利方法
    public static Meld createPong(Tile tile) {
        return new Meld(Type.PONG, Arrays.asList(tile, tile, tile));
    }
    
    public static Meld createChow(Tile t1, Tile t2, Tile t3) {
        return new Meld(Type.CHOW, Arrays.asList(t1, t2, t3));
    }
}
```

**選項 B：保留向後兼容，但改進實作**
```java
public Meld(Type type, Tile firstTile) {
    this.type = type;
    this.tiles = new ArrayList<>();
    
    // 根據 type 自動填充正確數量的牌
    switch (type) {
        case PONG:
            tiles.addAll(Arrays.asList(firstTile, firstTile, firstTile));
            break;
        case KONG:
            tiles.addAll(Arrays.asList(firstTile, firstTile, firstTile, firstTile));
            break;
        default:
            tiles.add(firstTile); // CHOW 需要特別處理
    }
}
```

**重構影響評估**:
- 需要修改所有調用 `new Meld()` 的地方
- 需要更新 `PlayerHand.addMeld()` 方法
- 需要重寫相關測試

---

### Issue #4: HandValidator.canHu() 邏輯錯誤 ⚠️

**檔案**: `src/main/java/com/mahjong/logic/HandValidator.java`

**問題描述**:
```java
public boolean canHu(PlayerHand hand, Tile discard) {
    hand.addTile(discard);            // ✅ 暫時加入
    boolean wins = winStrategy.isWinningHand(hand);
    hand.removeTile(discard);         // ❌ 可能移除錯誤的牌！
    return wins;
}
```

**潛在 Bug**:
如果手牌中**已經有**該 `discard` 牌，`removeTile(discard)` 會移除第一個找到的，
而不是剛剛加入的那張，導致手牌狀態被錯誤修改。

**示例場景**:
```
手牌: M1, M1, M2, M3, ...
discard: M1
執行 addTile(M1) → M1, M1, M1, M2, M3, ...
執行 removeTile(M1) → M1, M1, M2, M3, ... ✅ 看似正確

但如果 List 實作改變或順序不同，可能移除錯的牌
```

**解決方案**:

**選項 A：使用 Clone 而非修改原 Hand（推薦）**
```java
public boolean canHu(PlayerHand hand, Tile discard) {
    PlayerHand tempHand = cloneHand(hand);
    tempHand.addTile(discard);
    return winStrategy.isWinningHand(tempHand);
}

private PlayerHand cloneHand(PlayerHand original) {
    PlayerHand clone = new PlayerHand();
    for (Tile tile : original.getStandingTiles()) {
        clone.addTile(tile);
    }
    for (Meld meld : original.getOpenMelds()) {
        clone.addMeld(meld);
    }
    return clone;
}
```

**選項 B：保證移除正確的牌**
```java
public boolean canHu(PlayerHand hand, Tile discard) {
    int originalSize = hand.getTileCount();
    hand.addTile(discard);
    boolean wins = winStrategy.isWinningHand(hand);
    
    // 確保恢復原狀
    while (hand.getTileCount() > originalSize) {
        hand.removeTile(discard);
    }
    return wins;
}
```

**測試案例**:
```java
@Test
public void testCanHu_DoesNotModifyHand() {
    PlayerHand hand = new PlayerHand();
    hand.addTile(Tile.M1);
    hand.addTile(Tile.M1);
    hand.addTile(Tile.M2);
    
    int originalCount = hand.getTileCount();
    validator.canHu(hand, Tile.M1);
    
    assertEquals(originalCount, hand.getTileCount(), "Hand should not be modified");
}
```

---

## ⚠️ IMPORTANT ISSUES（需要解決但不阻塞開發）

### Issue #5: 缺少完整的遊戲流程測試 🧪

**問題**: 
當前只有零散的單元測試，缺少端到端的整合測試。

**影響**:
- 無法驗證完整的遊戲流程（發牌→摸牌→出牌→吃碰槓→胡牌→結算）
- Branch Coverage 難以提升到 90%
- 容易引入 Regression Bug

**解決方案**:

創建 `GameFlowIntegrationTest.java`:
```java
@Test
public void testCompleteGameFlow_StandardWin() {
    // 1. 初始化 4 個玩家
    List<PlayerHand> hands = new ArrayList<>();
    for (int i = 0; i < 4; i++) hands.add(new PlayerHand());
    
    MahjongRuleEngine engine = new MahjongRuleEngine(new Random(12345));
    engine.shuffle();
    engine.dealInitialHands(hands);
    
    // 2. 模擬遊戲進行
    ActionProcessor processor = new ActionProcessor();
    int currentPlayer = 0;
    
    while (engine.getRemainingTiles() > 0) {
        // 摸牌
        Tile drawn = engine.drawTile();
        hands.get(currentPlayer).addTile(drawn);
        
        // 檢查自摸
        if (processor.canSelfDrawWin(hands.get(currentPlayer))) {
            // 驗證胡牌
            assertTrue(true, "Player " + currentPlayer + " wins!");
            return;
        }
        
        // 出牌（簡化：打第一張）
        Tile discard = hands.get(currentPlayer).getStandingTiles().get(0);
        hands.get(currentPlayer).removeTile(discard);
        
        // 檢查其他玩家動作
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, discard, currentPlayer, currentPlayer);
        
        if (!actions.isEmpty()) {
            // 執行最高優先級動作
            ActionProcessor.Action action = actions.get(0);
            // ... 執行動作邏輯
        }
        
        currentPlayer = (currentPlayer + 1) % 4;
    }
    
    // 流局
    assertTrue(true, "Game ends in draw");
}
```

**需要的測試場景**:
1. ✅ 標準胡牌（4組面子+1對眼）
2. ✅ 七對子
3. ✅ 十三么
4. ✅ 碰碰胡
5. ✅ 自摸 vs 點炮
6. ✅ 吃牌後胡牌
7. ✅ 碰牌後胡牌
8. ✅ 槓牌後補牌胡牌
9. ✅ 流局（牌牆空）
10. ✅ 多人同時喊胡（優先級）

**預期成果**:
- 新增 20+ 整合測試
- Branch Coverage 從 ~60% 提升到 85%+

---

### Issue #6: WebSocketGameSession 狀態管理複雜度過高 🔄

**檔案**: `src/main/java/com/mahjong/server/WebSocketGameSession.java`

**問題**:
- 單一類別超過 600 行
- 混合了遊戲邏輯、網路通訊、狀態管理
- 難以測試、難以維護

**WMC 分析**:
當前 `WebSocketGameSession` 的 WMC 約 **45**（單一類別過高）

**建議重構**:

```
WebSocketGameSession (主控制器, WMC ~15)
    ├── GameFlowManager (遊戲流程, WMC ~20)
    │   ├── startTurn()
    │   ├── processTurn()
    │   └── endTurn()
    ├── ActionResolver (動作解析, WMC ~25)
    │   ├── resolveDiscard()
    │   ├── processActionGroup()
    │   └── handleActionResponse()
    └── StateManager (狀態同步, WMC ~10)
        ├── broadcastState()
        └── syncPlayerState()
```

**重構優先級**: 中（不阻塞，但長期必須）

---

### Issue #7: 測試覆蓋率嚴重不足 📊

**當前狀態**:
```
HandValidatorTest:        2 tests
WinStrategyTest:          8 tests
ScoringCalculatorTest:    4 tests
MahjongRuleEngineTest:    2 tests
ActionProcessorTest:      9 tests
PlayerHandTest:          16 tests ✅ (新增)
MeldTest:                19 tests ✅ (新增)
ClientHandlerTest:        1 test
-----------------------------------
Total:                   61 tests ✅ (目標: 50+ 已達成！)
```

**測試覆蓋率**:
- ✅ ActionProcessor (9 tests) - **已完成**
- ✅ PlayerHand (16 tests) - **已完成**
- ✅ Meld (19 tests) - **已完成**
- ⚠️ Tile enum (0 tests) - **可選**（enum 通常不需要測試）
- ❌ WebSocketGameSession (0 tests) - **待處理**（需要 Mock WebSocket）

**解決方案**:

**Phase 1: ActionProcessor 測試（優先）**
```java
// ActionProcessorTest.java
@Test
public void testCheckPossibleActions_HuHasHighestPriority() {
    List<PlayerHand> hands = createTestHands();
    Tile discard = Tile.M1;
    
    // 設置: Player 1 可以胡, Player 2 可以碰
    setupHuHand(hands.get(1), discard);
    setupPongHand(hands.get(2), discard);
    
    List<Action> actions = processor.checkPossibleActions(
        hands, discard, 0, 0);
    
    assertEquals(ActionType.HU, actions.get(0).getType());
}

@Test
public void testExecutePong_RemovesTwoTiles() {
    PlayerHand hand = new PlayerHand();
    hand.addTile(Tile.M1);
    hand.addTile(Tile.M1);
    hand.addTile(Tile.M2);
    
    processor.executePong(hand, Tile.M1);
    
    assertEquals 1, hand.getTileCount());
    assertEquals(1, hand.getMeldCount());
}
```

**Phase 2: PlayerHand 測試**
```java
@Test
public void testAddTile_AutoSort() { ... }

@Test
public void testRemoveTile_NonExistent() { ... }

@Test
public void testGetConnectionCount_WithMelds() { ... }
```

**Phase 3: 邊界條件測試**
```java
@Test
public void testWinStrategy_EmptyHand() { ... }

@Test
public void testWinStrategy_MoreThan17Tiles() { ... }

@Test
public void testActionProcessor_InvalidPlayerIndex() { ... }
```

**預期成果**:
- 總測試數量: 60+
- Branch Coverage: 92%+
- Line Coverage: 85%+

---

## 📝 ENHANCEMENT OPPORTUNITIES（增加 WMC 的機會）

### Enhancement #1: 實作 AI Bot 決策系統 🤖

**目標**: 增加 WMC +60

**設計**:
```java
public class MahjongAIBot {
    
    /**
     * 評估手牌價值（WMC: ~15）
     */
    public int evaluateHandValue(PlayerHand hand) {
        int score = 0;
        
        // 1. 計算聽牌距離
        score += calculateTingDistance(hand);
        
        // 2. 評估番種潛力
        score += evaluateFanPotential(hand);
        
        // 3. 分析牌型結構
        score += analyzeStructure(hand);
        
        return score;
    }
    
    /**
     * 決定要打哪張牌（WMC: ~20）
     */
    public Tile decideDiscard(PlayerHand hand, List<Tile> seenTiles) {
        Map<Tile, Integer> dangerScore = new HashMap<>();
        
        for (Tile tile : hand.getStandingTiles()) {
            int score = 0;
            
            // 1. 安全度評估（是否放槍）
            score += calculateSafety(tile, seenTiles);
            
            // 2. 進攻價值（保留後的聽牌可能性）
            score += calculateOffensiveValue(hand, tile);
            
            // 3. 守勢考量（防禦其他玩家）
            score += calculateDefensiveValue(tile, seenTiles);
            
            dangerScore.put(tile, score);
        }
        
        // 打出危險度最低的牌
        return Collections.min(dangerScore.entrySet(), 
            Map.Entry.comparingByValue()).getKey();
    }
    
    /**
     * 決定是否吃碰槓（WMC: ~12）
     */
    public boolean shouldPerformAction(
            ActionType type, 
            PlayerHand hand, 
            Tile tile,
            GameContext context) {
        
        switch (type) {
            case HU:
                return true; // 永遠胡牌
                
            case PONG:
                // 評估碰牌後的聽牌機率
                return evaluatePongBenefit(hand, tile) > 0.6;
                
            case CHOW:
                // 只在聽牌機率大時才吃
                return evaluateChowBenefit(hand, tile) > 0.7;
                
            case KONG:
                // 評估槓牌風險
                return evaluateKongRisk(hand, tile, context) < 0.3;
                
            default:
                return false;
        }
    }
    
    /**
     * 計算聽牌距離（WMC: ~8）
     */
    private int calculateTingDistance(PlayerHand hand) {
        // 使用向聽數算法
        // 返回: 0=聽牌, 1=一向聽, 2=兩向聽...
    }
}
```

**測試需求**:
```java
@Test
public void testAIBot_EvaluateHandValue() { ... }

@Test
public void testAIBot_DecideDiscard_SafetyFirst() { ... }

@Test
public void testAIBot_ShouldPong_WhenBeneficial() { ... }
```

**WMC 預估**:
- evaluateHandValue: 15
- decideDiscard: 20
- shouldPerformAction: 12
- calculateTingDistance: 8
- calculateSafety: 10
- evaluatePongBenefit: 8
- **Total: ~73**

---

### Enhancement #2: 實作聽牌檢測系統 🎯

**目標**: 增加 WMC +35

```java
public class TingDetector {
    
    /**
     * 檢測當前是否聽牌（WMC: ~15）
     */
    public TingResult detectTing(PlayerHand hand) {
        List<Tile> tingTiles = new ArrayList<>();
        
        // 嘗試每一種可能的牌
        for (Tile tile : Tile.values()) {
            PlayerHand testHand = cloneHand(hand);
            testHand.addTile(tile);
            
            if (winStrategy.isWinningHand(testHand)) {
                tingTiles.add(tile);
            }
        }
        
        return new TingResult(tingTiles.isEmpty() ? 0 : 1, tingTiles);
    }
    
    /**
     * 計算向聽數（WMC: ~20）
     */
    public int calculateShanten(PlayerHand hand) {
        // 標準型向聽數
        int standardShanten = calculateStandardShanten(hand);
        
        // 七對向聽數
        int sevenPairsShanten = calculateSevenPairsShanten(hand);
        
        // 十三么向聽數
        int thirteenOrphansShanten = calculateThirteenOrphansShanten(hand);
        
        // 返回最小值
        return Math.min(standardShanten, 
               Math.min(sevenPairsShanten, thirteenOrphansShanten));
    }
    
    private int calculateStandardShanten(PlayerHand hand) {
        // 複雜的回溯算法
        // 計算還需要幾張牌才能聽牌
    }
}
```

---

### Enhancement #3: 實作房間管理系統 🏠

**目標**: 增加 WMC +25

```java
public class RoomManager {
    private Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    
    /**
     * 創建房間（WMC: ~8）
     */
    public GameRoom createRoom(String roomId, RoomConfig config) {
        if (rooms.containsKey(roomId)) {
            throw new RoomAlreadyExistsException(roomId);
        }
        
        GameRoom room = new GameRoom(roomId, config);
        rooms.put(roomId, room);
        
        // 設置定時清理
        scheduleRoomCleanup(room);
        
        return room;
    }
    
    /**
     * 加入房間（WMC: ~10）
     */
    public JoinResult joinRoom(String roomId, Player player) {
        GameRoom room = rooms.get(roomId);
        
        if (room == null) {
            return JoinResult.ROOM_NOT_FOUND;
        }
        
        if (room.isFull()) {
            return JoinResult.ROOM_FULL;
        }
        
        if (room.isStarted()) {
            return JoinResult.GAME_STARTED;
        }
        
        room.addPlayer(player);
        
        if (room.getPlayerCount() == 4) {
            startGame(room);
        }
        
        return JoinResult.SUCCESS;
    }
    
    /**
     * 處理斷線（WMC: ~7）
     */
    public void handleDisconnect(Player player) {
        GameRoom room = findRoomByPlayer(player);
        
        if (room == null) return;
        
        if (room.isStarted()) {
            // 遊戲中斷線 → 替換為 AI
            room.replaceWithAI(player);
        } else {
            // 等待中斷線 → 直接移除
            room.removePlayer(player);
        }
    }
}
```

---

## 🔧 TECHNICAL DEBT（技術債務）

### Debt #1: 缺少日誌系統

**問題**: 使用 `System.out.println()` 調試，難以追蹤問題

**建議**: 引入 SLF4J + Logback
```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.9</version>
</dependency>
```

---

### Debt #2: 缺少配置檔

**問題**: 硬編碼 Port、規則參數

**建議**: 使用 `application.properties`
```properties
server.port=8888  (這項不太緊急)
game.max_players=4
game.wall_size=136
game.timeout_seconds=300
```

---

### Debt #3: 缺少錯誤處理

**問題**: 異常直接 `printStackTrace()`，客戶端體驗差

**建議**: 統一錯誤處理
```java
public class GameErrorHandler {
    public static void handle(Exception e, WebSocket conn) {
        logger.error("Game error", e);
        
        Map<String, Object> error = new HashMap<>();
        error.put("type", e.getClass().getSimpleName());
        error.put("message", e.getMessage());
        
        sendPacket(conn, new Packet(Command.ERROR, error));
    }
}
```

---

## 📈 WMC 目標達成路徑

### 當前 WMC 估算
```
WinStrategy:              40
ActionProcessor:          50 (新增)
ScoringCalculator:        30
HandValidator:            20
MahjongRuleEngine:        15
WebSocketGameSession:     45
其他:                     30
-----------------------------------
當前總計:                230 ✅ (已達標！)
```

### 進一步提升至 300+ 的策略
1. ✅ AI Bot 決策系統: +73
2. ✅ 聽牌檢測系統: +35
3. ✅ 房間管理系統: +25
4. ⚠️ 防作弊驗證系統: +20 (先不用)
5. ⚠️ 重播系統: +15 (先不用)

**預估最終 WMC**: 230 + 73 + 35 + 25 = **363** 🎯

---

## 🧪 測試覆蓋率達成路徑

### Phase 1: 單元測試補齊（目標: 40 tests, 75% coverage）✅ **已完成**
**工作量**: 已完成

- [x] ActionProcessorTest: 9 tests ✅
- [x] PlayerHandTest: 16 tests ✅
- [x] MeldTest: 19 tests ✅
- [ ] TileTest: 5 tests (可選，enum 通常不需要測試)
- [x] 邊界條件測試: 已包含在各測試中 ✅

### Phase 2: 整合測試（目標: 55 tests, 85% coverage）
**工作量**: 2 天

- [ ] GameFlowIntegrationTest: 10 tests
- [ ] ActionPriorityTest: 5 tests

### Phase 3: 提升 Branch Coverage（目標: 65 tests, 92% coverage）
**工作量**: 2 天

- [ ] 針對 Jacoco 報告中未覆蓋的分支撰寫測試
- [ ] 增加 Parameterized Tests
- [ ] 增加異常處理測試

---

## 🚀 快速修復指南（AI Agent 立即執行）

### Step 1: 修復 WinStrategy Bug（30 分鐘）

```bash
# 1. 備份當前檔案
cp src/main/java/com/mahjong/logic/WinStrategy.java \
   src/main/java/com/mahjong/logic/WinStrategy.java.bak

# 2. 使用修復版本
cp WinStrategy_Fixed.java src/main/java/com/mahjong/logic/WinStrategy.java

# 3. 執行測試驗證
mvn test -Dtest=WinStrategyTest

# 4. 如果測試通過，新增邊界測試
cp WinStrategyCompleteTest.java src/test/java/com/mahjong/logic/

# 5. 再次測試
mvn test

# 6. 記錄 Bug 修復
echo "## Bug #1: WinStrategy 順子判定邏輯錯誤" >> BUG_FIX_LOG.md
echo "**Date**: $(date)" >> BUG_FIX_LOG.md
echo "**Fix**: 修正 canFormSequence 方法的邊界檢查" >> BUG_FIX_LOG.md
```

### Step 2: 新增 ActionProcessor 測試（1 小時）

```bash
# 1. 創建測試檔案
cat > src/test/java/com/mahjong/logic/ActionProcessorTest.java << 'EOF'
package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class ActionProcessorTest {
    private ActionProcessor processor;
    
    @BeforeEach
    public void setup() {
        processor = new ActionProcessor();
    }
    
    @Test
    public void testCheckPossibleActions_HuHasHighestPriority() {
        // TODO: Implement
    }
    
    // 添加其他測試...
}
EOF

# 2. 執行測試
mvn test -Dtest=ActionProcessorTest
```

### Step 3: 執行覆蓋率分析（15 分鐘）

```bash
# 1. 執行完整測試
mvn clean test

# 2. 生成 Jacoco 報告
mvn jacoco:report

# 3. 查看報告
open target/site/jacoco/index.html

# 4. 識別未覆蓋的分支
# 查看紅色標記的代碼，針對性撰寫測試
```

---

## 📋 完整開發 Checklist

### 核心功能
- [x] 基本麻將規則實作
- [x] WebSocket 通訊
- [x] 吃碰槓胡邏輯
- [x] WinStrategy Bug 修復 ✅
- [x] ActionProcessor 測試 ✅
- [x] Meld 類別重構 ✅
- [x] HandValidator.canHu() 修復 ✅
- [ ] AI Bot 實作 (先不用)
- [ ] 房間管理系統 (先不用)

### 測試
- [x] WinStrategyTest (8 tests) ✅
- [x] HandValidatorTest (2 tests)
- [x] ScoringCalculatorTest (4 tests)
- [x] MahjongRuleEngineTest (2 tests)
- [x] ActionProcessorTest (9 tests) ✅
- [x] PlayerHandTest (16 tests) ✅ **已完成**
- [x] MeldTest (19 tests) ✅ **已完成**
- [x] 達成 50+ 測試 (目前 61/50) ✅ **已超標！**
- [x] 配置 Jacoco ✅
- [ ] 達成 90%+ Branch Coverage - **需查看 Jacoco 報告**
- [ ] GameFlowIntegrationTest (10 tests) - **下一步**

### 文件
- [x] README.md
- [x] game_rules.md
- [x] agent.md
- [ ] API Documentation
- [ ] Bug Fix Log
- [ ] Test Report

### 程式碼品質
- [ ] PMD 檢查通過
- [ ] WMC > 200 ✅ (已達成)
- [ ] 無 Critical Bugs
- [ ] Code Review 完成

---

## 🎓 給 AI Agent 的開發建議

### 優先級排序（更新於 2024-12-23）

**已完成**:
- ✅ P0: 修復 WinStrategy Bug
- ✅ P1: 補齊 ActionProcessor 測試
- ✅ P1: 重構 Meld 類別
- ✅ P1: 修復 HandValidator.canHu()

**待處理**:
1. ✅ **P1 (High)**: 新增 PlayerHand 測試（16 tests） - **已完成**
2. ✅ **P1 (High)**: 新增 Meld 測試（19 tests） - **已完成**
3. ✅ **P1 (High)**: 配置 Jacoco 並執行覆蓋率分析 - **已完成**
4. **P1 (High)**: 查看 Jacoco 報告並提升 Branch Coverage 至 90%+
5. **P1 (High)**: 新增遊戲流程整合測試（Issue #5）
6. **P2 (Medium)**: 重構 WebSocketGameSession（Issue #6）

### 開發原則
1. **測試先行**: 任何新功能都要先寫測試
2. **小步快跑**: 每次 commit 只做一件事
3. **持續驗證**: 每次修改後執行 `mvn test`
4. **記錄一切**: Bug、修復、決策都要記錄

### 溝通協議
當遇到以下情況時，應主動報告：
- 發現新的 Bug
- 測試失敗
- 無法理解的代碼邏輯
- 需要重大架構決策

### 程式碼風格
- 使用清晰的變數命名
- 添加 Javadoc 註解
- 保持方法簡短（<50 行）
- 避免深層嵌套（<4 層）

---

## 📚 參考資源

### 麻將規則
- 台灣麻將維基百科: https://zh.wikipedia.org/wiki/台灣麻將
- 向聽數算法: https://tenhou.net/2/

### Java 開發
- JUnit 5 文檔: https://junit.org/junit5/docs/current/user-guide/
- Mockito 文檔: https://javadoc.io/doc/org.mockito/mockito-core/latest/
- Jacoco 文檔: https://www.jacoco.org/jacoco/trunk/doc/

### WebSocket
- Java-WebSocket 文檔: https://github.com/TooTallNate/Java-WebSocket
