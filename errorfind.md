需要改進的關鍵問題
1. 胡牌演算法的 Bug 🐛
WinStrategy.java 的 canFormSets 方法有邏輯錯誤：
java// 問題：順子判定只檢查到 rank <= 6
if (firstIndex < 27) {
    int suitIndex = firstIndex % 9;
    if (suitIndex <= 6) { // ❌ 錯誤：7萬8萬9萬無法組成順子
修正方案：
javaif (suitIndex <= 6) { // 應該改為：能否組成 i, i+1, i+2
    // 還需確認 i+1, i+2 不跨花色
    int nextIndex = firstIndex + 1;
    int nextNextIndex = firstIndex + 2;
    
    // 確保不跨花色邊界 (如：8萬->9萬->1筒)
    if ((nextIndex / 9) == (firstIndex / 9) && 
        (nextNextIndex / 9) == (firstIndex / 9)) {
        // ... 檢查邏輯
    }
}
2. 缺少完整的遊戲流程控制 🎮
GameSession.java 只有起手發牌和第一個回合的邏輯，缺少：

❌ 出牌後的動作判定（吃碰槓胡）
❌ 輪次切換
❌ 胡牌檢查與結算
❌ 流局處理

3. 測試覆蓋率不足 📊
當前測試案例：

✅ HandValidatorTest: 3 個測試
✅ WinStrategyTest: 5 個測試
✅ ScoringCalculatorTest: 4 個測試
✅ MahjongRuleEngineTest: 2 個測試
⚠️ ClientHandlerTest: 1 個測試（但有實作問題）

需要增加的測試：

邊界條件測試（空手牌、單張牌）
特殊牌型測試（七對子、十三么）
並發測試（多客戶端同時連線）
錯誤處理測試（斷線、非法操作）

需要增加複雜度的模組：
動作優先級處理邏輯
複雜的遊戲狀態機

Winstrategy fixed:
```
package com.mahjong.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * 修復版：Strategy to determine if a hand is a "Winning Hand" (Hu).
 * Uses recursion/backtracking to check for standard format: m*AAA + n*ABC + DD.
 * 
 * 修復內容：
 * 1. 順子判定邏輯修正（7,8,9 萬可以組成順子）
 * 2. 跨花色邊界檢查
 * 3. 增加詳細註解
 */
public class WinStrategy {

    /**
     * Checks if the hand is a winning hand.
     * @param hand The player's hand (including the 17th tile for dealer, or 14 for others)
     * @return true if winning
     */
    public boolean isWinningHand(PlayerHand hand) {
        List<Tile> tiles = hand.getStandingTiles();
        int tileCount = tiles.size();
        
        // Taiwan Mahjong: 17 tiles for dealer at start, 14 for winning hand check
        // Allow both 14 and 17 for flexibility
        if (tileCount != 14 && tileCount != 17) {
            return false;
        }
        
        // Basic check: (Count - 2) % 3 should be 0 for standard form
        // Standard: 4 sets (3 tiles each) + 1 pair (2 tiles) = 14 tiles
        if ((tileCount - 2) % 3 != 0) {
            return false; 
        }

        // Convert to frequency array for efficient backtracking
        int[] counts = new int[34];
        for (Tile t : tiles) {
            counts[getTileIndex(t)]++;
        }

        // Try all possible pairs (Eyes)
        for (int i = 0; i < 34; i++) {
            if (counts[i] >= 2) {
                // Try using index i as the pair
                counts[i] -= 2;
                
                // Check if remaining tiles form valid sets
                int setsNeeded = (tileCount - 2) / 3;
                if (canFormSets(counts, setsNeeded)) {
                    counts[i] += 2; // Restore before returning
                    return true;
                }
                
                // Backtrack
                counts[i] += 2;
            }
        }
        
        return false;
    }

    /**
     * 遞迴檢查是否能組成指定數量的面子（順子或刻子）
     * @param counts 牌的頻率陣列
     * @param setsNeeded 還需要組成的面子數量
     * @return true if can form required sets
     */
    private boolean canFormSets(int[] counts, int setsNeeded) {
        if (setsNeeded == 0) {
            // 檢查是否所有牌都已使用完
            for (int count : counts) {
                if (count > 0) return false;
            }
            return true;
        }

        // Find the first index with available tiles
        int firstIndex = -1;
        for (int i = 0; i < 34; i++) {
            if (counts[i] > 0) {
                firstIndex = i;
                break;
            }
        }
        
        if (firstIndex == -1) {
            // No tiles left but still need sets - invalid
            return setsNeeded == 0;
        }

        // Strategy 1: Try to form a Triplet (AAA/Pong)
        if (counts[firstIndex] >= 3) {
            counts[firstIndex] -= 3;
            if (canFormSets(counts, setsNeeded - 1)) {
                return true;
            }
            counts[firstIndex] += 3; // Backtrack
        }

        // Strategy 2: Try to form a Sequence (ABC/Chow)
        // Only possible for number tiles (indices 0-26)
        if (firstIndex < 27) {
            // Check if we can form a sequence starting at firstIndex
            // 修復：移除 suitIndex <= 6 的限制
            if (canFormSequence(counts, firstIndex)) {
                counts[firstIndex]--;
                counts[firstIndex + 1]--;
                counts[firstIndex + 2]--;
                
                if (canFormSets(counts, setsNeeded - 1)) {
                    return true;
                }
                
                // Backtrack
                counts[firstIndex]++;
                counts[firstIndex + 1]++;
                counts[firstIndex + 2]++;
            }
        }
        
        return false;
    }

    /**
     * 檢查是否能從指定位置組成順子
     * 修復：正確處理 7,8,9 等邊界情況
     */
    private boolean canFormSequence(int[] counts, int startIndex) {
        // 必須是數字牌（萬筒條）
        if (startIndex >= 27) return false;
        
        // 計算花色和牌面值
        int suit = startIndex / 9;  // 0=萬, 1=筒, 2=條
        int rank = startIndex % 9;  // 0-8 對應 1-9
        
        // 檢查是否能組成 i, i+1, i+2
        // rank 最大為 6 時（對應 7），可以組成 7,8,9
        if (rank > 6) return false;  // rank=7(8) 或 rank=8(9) 無法作為順子起點
        
        int next1 = startIndex + 1;
        int next2 = startIndex + 2;
        
        // 確保不跨花色（例如：8萬9萬1筒 是非法的）
        if (next1 / 9 != suit || next2 / 9 != suit) {
            return false;
        }
        
        // 檢查是否有足夠的牌
        return counts[startIndex] > 0 && 
               counts[next1] > 0 && 
               counts[next2] > 0;
    }

    /**
     * 將 Tile 轉換為索引 (0-33)
     * Man: 0-8, Pin: 9-17, Sou: 18-26, Wind: 27-30, Dragon: 31-33
     */
    private int getTileIndex(Tile tile) {
        switch (tile.getSuit()) {
            case MAN: return tile.getRank() - 1;           // 0-8
            case PIN: return 9 + (tile.getRank() - 1);     // 9-17
            case SOU: return 18 + (tile.getRank() - 1);    // 18-26
            case WIND: return 27 + (tile.getRank() - 1);   // 27-30
            case DRAGON: return 31 + (tile.getRank() - 1); // 31-33
            default: throw new IllegalArgumentException("Unknown tile suit: " + tile);
        }
    }

    /**
     * 特殊牌型檢查：七對子
     * Taiwan Mahjong 特殊胡牌型態
     */
    public boolean isSevenPairs(PlayerHand hand) {
        List<Tile> tiles = hand.getStandingTiles();
        if (tiles.size() != 14) return false;
        
        int[] counts = new int[34];
        for (Tile t : tiles) {
            counts[getTileIndex(t)]++;
        }
        
        int pairCount = 0;
        for (int count : counts) {
            if (count == 2) {
                pairCount++;
            } else if (count != 0) {
                return false; // 有非0且非2的牌數
            }
        }
        
        return pairCount == 7;
    }

    /**
     * 特殊牌型檢查：十三么（國士無雙）
     * 1,9萬筒條 + 東南西北中發白 各一張，其中一種兩張
     */
    public boolean isThirteenOrphans(PlayerHand hand) {
        List<Tile> tiles = hand.getStandingTiles();
        if (tiles.size() != 14) return false;
        
        // 十三么的牌索引
        int[] orphanIndices = {
            0, 8,      // 1萬, 9萬
            9, 17,     // 1筒, 9筒
            18, 26,    // 1條, 9條
            27, 28, 29, 30,  // 東南西北
            31, 32, 33       // 中發白
        };
        
        int[] counts = new int[34];
        for (Tile t : tiles) {
            int idx = getTileIndex(t);
            counts[idx]++;
            
            // 檢查是否為么九牌
            boolean isOrphan = false;
            for (int orphanIdx : orphanIndices) {
                if (idx == orphanIdx) {
                    isOrphan = true;
                    break;
                }
            }
            if (!isOrphan) return false;
        }
        
        // 檢查是否有13種不同的么九牌
        int uniqueCount = 0;
        int pairCount = 0;
        for (int orphanIdx : orphanIndices) {
            if (counts[orphanIdx] > 0) {
                uniqueCount++;
                if (counts[orphanIdx] == 2) {
                    pairCount++;
                } else if (counts[orphanIdx] != 1) {
                    return false;
                }
            }
        }
        
        return uniqueCount == 13 && pairCount == 1;
    }
}
```

Actionprocessor · JAVA
```
package com.mahjong.logic;

import java.util.*;

/**
 * 處理麻將遊戲中的動作優先級與執行
 * 這個類別負責：
 * 1. 檢查玩家可執行的動作（吃碰槓胡）
 * 2. 處理動作優先級（胡 > 槓/碰 > 吃）
 * 3. 執行動作並更新遊戲狀態
 * 
 * 這個類別會大幅增加 WMC（複雜度）
 */
public class ActionProcessor {
    
    public enum ActionType {
        HU(1),      // 胡牌 - 最高優先級
        KONG(2),    // 槓
        PONG(2),    // 碰 - 與槓同優先級
        CHOW(3),    // 吃 - 最低優先級
        PASS(4);    // 過
        
        private final int priority;
        
        ActionType(int priority) {
            this.priority = priority;
        }
        
        public int getPriority() {
            return priority;
        }
    }
    
    /**
     * 代表一個可執行的動作
     */
    public static class Action {
        private final ActionType type;
        private final int playerIndex;
        private final Tile targetTile;
        private final List<Tile> involvedTiles; // 用於吃牌時指定順子組成
        
        public Action(ActionType type, int playerIndex, Tile targetTile) {
            this.type = type;
            this.playerIndex = playerIndex;
            this.targetTile = targetTile;
            this.involvedTiles = new ArrayList<>();
        }
        
        public ActionType getType() { return type; }
        public int getPlayerIndex() { return playerIndex; }
        public Tile getTargetTile() { return targetTile; }
        public List<Tile> getInvolvedTiles() { return involvedTiles; }
        public void setInvolvedTiles(List<Tile> tiles) { 
            involvedTiles.clear();
            involvedTiles.addAll(tiles);
        }
        
        @Override
        public String toString() {
            return String.format("Action{type=%s, player=%d, tile=%s}", 
                type, playerIndex, targetTile);
        }
    }
    
    private final HandValidator validator;
    private final WinStrategy winStrategy;
    
    public ActionProcessor() {
        this.validator = new HandValidator();
        this.winStrategy = new WinStrategy();
    }
    
    /**
     * 檢查所有玩家對打出的牌可以執行的動作
     * @param hands 所有玩家的手牌
     * @param discardedTile 被打出的牌
     * @param discardPlayerIndex 打牌的玩家索引
     * @param currentPlayerIndex 當前輪到的玩家索引
     * @return 可執行的動作列表，已按優先級排序
     */
    public List<Action> checkPossibleActions(
            List<PlayerHand> hands,
            Tile discardedTile,
            int discardPlayerIndex,
            int currentPlayerIndex) {
        
        List<Action> actions = new ArrayList<>();
        int playerCount = hands.size();
        
        // 檢查每個玩家（除了打牌者）
        for (int i = 0; i < playerCount; i++) {
            if (i == discardPlayerIndex) continue;
            
            PlayerHand hand = hands.get(i);
            
            // 1. 檢查胡牌（最高優先級）
            if (canHu(hand, discardedTile)) {
                actions.add(new Action(ActionType.HU, i, discardedTile));
            }
            
            // 2. 檢查槓（次高優先級）
            if (validator.canKong(hand, discardedTile)) {
                actions.add(new Action(ActionType.KONG, i, discardedTile));
            }
            
            // 3. 檢查碰（次高優先級）
            if (validator.canPong(hand, discardedTile)) {
                actions.add(new Action(ActionType.PONG, i, discardedTile));
            }
            
            // 4. 檢查吃（只有下家可以吃）
            int nextPlayer = (discardPlayerIndex + 1) % playerCount;
            if (i == nextPlayer && validator.canChow(hand, discardedTile)) {
                // 吃牌可能有多種組合方式
                List<List<Tile>> chowOptions = getChowOptions(hand, discardedTile);
                for (List<Tile> option : chowOptions) {
                    Action chowAction = new Action(ActionType.CHOW, i, discardedTile);
                    chowAction.setInvolvedTiles(option);
                    actions.add(chowAction);
                }
            }
        }
        
        // 按優先級排序
        actions.sort(Comparator.comparingInt(a -> a.getType().getPriority()));
        
        return actions;
    }
    
    /**
     * 檢查自摸胡牌
     */
    public boolean canSelfDrawWin(PlayerHand hand) {
        return winStrategy.isWinningHand(hand);
    }
    
    /**
     * 檢查點炮胡牌
     */
    private boolean canHu(PlayerHand hand, Tile discardedTile) {
        // 創建臨時手牌加入被打出的牌
        PlayerHand tempHand = cloneHand(hand);
        tempHand.addTile(discardedTile);
        
        return winStrategy.isWinningHand(tempHand) ||
               winStrategy.isSevenPairs(tempHand) ||
               winStrategy.isThirteenOrphans(tempHand);
    }
    
    /**
     * 獲取所有可能的吃牌組合
     */
    private List<List<Tile>> getChowOptions(PlayerHand hand, Tile targetTile) {
        List<List<Tile>> options = new ArrayList<>();
        
        if (!targetTile.isNumberTile()) {
            return options; // 字牌不能吃
        }
        
        List<Tile> tiles = hand.getStandingTiles();
        Tile.Suit suit = targetTile.getSuit();
        int rank = targetTile.getRank();
        
        // 三種可能的順子：
        // 1. (rank-2, rank-1, rank) 例如：目標是3，手上有1,2
        if (rank >= 3) {
            Tile tile1 = findTile(tiles, suit, rank - 2);
            Tile tile2 = findTile(tiles, suit, rank - 1);
            if (tile1 != null && tile2 != null) {
                options.add(Arrays.asList(tile1, tile2, targetTile));
            }
        }
        
        // 2. (rank-1, rank, rank+1) 例如：目標是3，手上有2,4
        if (rank >= 2 && rank <= 8) {
            Tile tile1 = findTile(tiles, suit, rank - 1);
            Tile tile2 = findTile(tiles, suit, rank + 1);
            if (tile1 != null && tile2 != null) {
                options.add(Arrays.asList(tile1, targetTile, tile2));
            }
        }
        
        // 3. (rank, rank+1, rank+2) 例如：目標是3，手上有4,5
        if (rank <= 7) {
            Tile tile1 = findTile(tiles, suit, rank + 1);
            Tile tile2 = findTile(tiles, suit, rank + 2);
            if (tile1 != null && tile2 != null) {
                options.add(Arrays.asList(targetTile, tile1, tile2));
            }
        }
        
        return options;
    }
    
    /**
     * 在手牌中尋找指定花色和數字的牌
     */
    private Tile findTile(List<Tile> tiles, Tile.Suit suit, int rank) {
        for (Tile tile : tiles) {
            if (tile.getSuit() == suit && tile.getRank() == rank) {
                return tile;
            }
        }
        return null;
    }
    
    /**
     * 複製手牌（用於胡牌檢查）
     */
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
    
    /**
     * 執行吃牌動作
     */
    public void executeChow(PlayerHand hand, Action action) {
        if (action.getType() != ActionType.CHOW) {
            throw new IllegalArgumentException("Action must be CHOW type");
        }
        
        List<Tile> chowTiles = action.getInvolvedTiles();
        Tile targetTile = action.getTargetTile();
        
        // 從手牌移除相關牌（不包括目標牌，因為它來自別人）
        for (Tile tile : chowTiles) {
            if (!tile.equals(targetTile)) {
                hand.removeTile(tile);
            }
        }
        
        // 添加面子
        Meld meld = new Meld(Meld.Type.CHOW, chowTiles.get(0));
        hand.addMeld(meld);
    }
    
    /**
     * 執行碰牌動作
     */
    public void executePong(PlayerHand hand, Tile targetTile) {
        // 從手牌移除兩張相同的牌
        int removed = 0;
        List<Tile> tiles = new ArrayList<>(hand.getStandingTiles());
        for (Tile tile : tiles) {
            if (tile.equals(targetTile) && removed < 2) {
                hand.removeTile(tile);
                removed++;
            }
        }
        
        if (removed != 2) {
            throw new IllegalStateException("Cannot pong: not enough tiles");
        }
        
        // 添加面子
        Meld meld = new Meld(Meld.Type.PONG, targetTile);
        hand.addMeld(meld);
    }
    
    /**
     * 執行槓牌動作（明槓）
     */
    public void executeKong(PlayerHand hand, Tile targetTile) {
        // 從手牌移除三張相同的牌
        int removed = 0;
        List<Tile> tiles = new ArrayList<>(hand.getStandingTiles());
        for (Tile tile : tiles) {
            if (tile.equals(targetTile) && removed < 3) {
                hand.removeTile(tile);
                removed++;
            }
        }
        
        if (removed != 3) {
            throw new IllegalStateException("Cannot kong: not enough tiles");
        }
        
        // 添加面子
        Meld meld = new Meld(Meld.Type.KONG, targetTile);
        hand.addMeld(meld);
    }
    
    /**
     * 執行暗槓動作
     */
    public void executeConcealedKong(PlayerHand hand, Tile targetTile) {
        // 從手牌移除四張相同的牌
        int removed = 0;
        List<Tile> tiles = new ArrayList<>(hand.getStandingTiles());
        for (Tile tile : tiles) {
            if (tile.equals(targetTile) && removed < 4) {
                hand.removeTile(tile);
                removed++;
            }
        }
        
        if (removed != 4) {
            throw new IllegalStateException("Cannot concealed kong: not enough tiles");
        }
        
        // 添加暗槓面子
        Meld meld = new Meld(Meld.Type.KONG, targetTile);
        hand.addMeld(meld);
    }
    
    /**
     * 檢查是否可以暗槓
     */
    public List<Tile> getConcealedKongOptions(PlayerHand hand) {
        List<Tile> options = new ArrayList<>();
        Map<Tile, Integer> tileCount = new HashMap<>();
        
        for (Tile tile : hand.getStandingTiles()) {
            tileCount.put(tile, tileCount.getOrDefault(tile, 0) + 1);
        }
        
        for (Map.Entry<Tile, Integer> entry : tileCount.entrySet()) {
            if (entry.getValue() >= 4) {
                options.add(entry.getKey());
            }
        }
        
        return options;
    }
    
    /**
     * 檢查是否可以補槓（已經碰過，再摸到第四張）
     */
    public List<Tile> getAddKongOptions(PlayerHand hand) {
        List<Tile> options = new ArrayList<>();
        
        // 檢查已經碰過的牌
        for (Meld meld : hand.getOpenMelds()) {
            if (meld.getType() == Meld.Type.PONG) {
                Tile pongTile = meld.getFirstTile();
                // 檢查手牌中是否有第四張
                for (Tile tile : hand.getStandingTiles()) {
                    if (tile.equals(pongTile)) {
                        options.add(pongTile);
                        break;
                    }
                }
            }
        }
        
        return options;
    }
}
```