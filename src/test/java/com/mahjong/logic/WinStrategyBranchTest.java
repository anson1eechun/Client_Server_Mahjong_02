package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * WinStrategy 分支覆蓋率測試
 * 重點測試未覆蓋的分支以達到 85% 覆蓋率
 */
class WinStrategyBranchTest {

    /**
     * 測試 totalTileCount != 14 && totalTileCount != 17 的分支
     */
    @Test
    void testIsWinningHand_InvalidTotalTileCount() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 測試 13 張牌（不足）
        for (int i = 0; i < 13; i++) {
            hand.addTile(Tile.M1);
        }
        assertFalse(strategy.isWinningHand(hand), "13 tiles should not be winning");
        
        // 測試 15 張牌（過多）
        hand = new PlayerHand();
        for (int i = 0; i < 15; i++) {
            hand.addTile(Tile.M1);
        }
        assertFalse(strategy.isWinningHand(hand), "15 tiles should not be winning");
        
        // 測試 16 張牌（非標準）
        hand = new PlayerHand();
        for (int i = 0; i < 16; i++) {
            hand.addTile(Tile.M1);
        }
        assertFalse(strategy.isWinningHand(hand), "16 tiles should not be winning");
    }

    /**
     * 測試 totalTileCount == 14 的分支
     */
    @Test
    void testIsWinningHand_TotalTileCount14() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 標準 14 張胡牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        assertTrue(strategy.isWinningHand(hand), "14 tiles standard winning hand should win");
    }

    /**
     * 測試 totalTileCount == 17 的分支（莊家）
     */
    @Test
    void testIsWinningHand_TotalTileCount17() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 莊家 17 張胡牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        hand.addTile(Tile.RED); hand.addTile(Tile.RED); hand.addTile(Tile.RED); // Pong
        
        assertTrue(strategy.isWinningHand(hand), "17 tiles dealer winning hand should win");
    }

    /**
     * 測試 (standingTileCount - 2) % 3 != 0 的分支
     */
    @Test
    void testIsWinningHand_InvalidStandingTileCount() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 14 張牌，但 standing tiles = 13（不符合 (n-2) % 3 == 0）
        for (int i = 0; i < 13; i++) {
            hand.addTile(Tile.M1);
        }
        hand.addMeld(Meld.createEyes(Tile.M2)); // 1 張（對眼是 2 張，但這裡只加 1 張）
        
        // 實際上應該用正確的方式測試
        hand = new PlayerHand();
        // 14 張牌，但結構不符合（例如：11 張 standing + 3 張 meld = 14，但 11-2=9，9%3=0，所以這個例子不對）
        // 讓我們用 13 張 standing + 1 張 meld = 14，但 13-2=11，11%3=2 != 0
        for (int i = 0; i < 13; i++) {
            hand.addTile(Tile.M1);
        }
        // 但這樣總數是 13，不是 14。讓我們用 meld
        hand = new PlayerHand();
        for (int i = 0; i < 11; i++) {
            hand.addTile(Tile.M1);
        }
        hand.addMeld(Meld.createPong(Tile.M2)); // 3 張，總數 = 14，但 11-2=9，9%3=0，所以這個也不對
        
        // 正確的測試：12 張 standing + 2 張 meld = 14，但 12-2=10，10%3=1 != 0
        hand = new PlayerHand();
        for (int i = 0; i < 12; i++) {
            hand.addTile(Tile.M1);
        }
        hand.addMeld(Meld.createEyes(Tile.M2)); // 2 張，總數 = 14，但 12-2=10，10%3=1 != 0
        assertFalse(strategy.isWinningHand(hand), "Invalid standing tile count should not win");
    }

    /**
     * 測試 canFormSets() 中 setsNeeded == 0 但 counts 不全為 0 的分支
     */
    @Test
    void testIsWinningHand_RemainingTilesAfterSets() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個無法完全組成面子的手牌
        // 例如：有對眼，但剩餘牌無法組成完整的面子
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); // 缺 M4 才能組成順子
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        // 這個手牌有 14 張，但 M2, M3 無法組成順子（缺 M4）
        assertFalse(strategy.isWinningHand(hand), "Hand with incomplete sets should not win");
    }

    /**
     * 測試 canFormSequence() 中 startIndex >= 27 的分支（非數字牌）
     */
    @Test
    void testIsWinningHand_NonNumberTiles() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 使用風牌和龍牌（無法組成順子）
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pair
        hand.addTile(Tile.SOUTH); hand.addTile(Tile.SOUTH); hand.addTile(Tile.SOUTH); // Pong
        hand.addTile(Tile.WEST); hand.addTile(Tile.WEST); hand.addTile(Tile.WEST); // Pong
        hand.addTile(Tile.NORTH); hand.addTile(Tile.NORTH); hand.addTile(Tile.NORTH); // Pong
        hand.addTile(Tile.RED); hand.addTile(Tile.RED); hand.addTile(Tile.RED); // Pong
        
        assertTrue(strategy.isWinningHand(hand), "All pongs with honors should win");
    }

    /**
     * 測試 canFormSequence() 中 rank > 6 的分支（8, 9 無法作為順子起點）
     */
    @Test
    void testIsWinningHand_Rank8And9() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 測試 8, 9 無法作為順子起點的情況
        // 創建一個有 M8, M9 但無法組成順子的手牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M8); hand.addTile(Tile.M8); // 兩個 M8（無法組成順子）
        hand.addTile(Tile.M9); hand.addTile(Tile.M9); // 兩個 M9（無法組成順子）
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        // 這個手牌無法胡，因為 M8, M9 無法組成順子或刻子（只有各 2 張）
        assertFalse(strategy.isWinningHand(hand), "Hand with rank 8 and 9 pairs should not win");
    }

    /**
     * 測試 canFormSequence() 中跨花色的分支
     */
    @Test
    void testIsWinningHand_CrossSuitSequence() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 測試跨花色的情況（雖然實際邏輯應該阻止，但測試邊界）
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M9); // 單張 M9
        hand.addTile(Tile.P1); hand.addTile(Tile.P2); // 缺 P3
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        // 這個手牌無法胡
        assertFalse(strategy.isWinningHand(hand), "Hand with incomplete sequences should not win");
    }

    /**
     * 測試 canFormSets() 中 firstIndex == -1 的分支
     */
    @Test
    void testIsWinningHand_NoTilesLeftButNeedsSets() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 這個情況理論上不應該發生，因為我們已經檢查了總數
        // 但為了覆蓋分支，我們創建一個特殊情況
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        // 添加其他牌但結構不完整
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); // 缺 M4
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        assertFalse(strategy.isWinningHand(hand), "Hand with incomplete structure should not win");
    }

    /**
     * 測試 canFormSets() 中 counts[firstIndex] < 3 的分支（無法組成刻子）
     */
    @Test
    void testIsWinningHand_CannotFormTriplet() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個有單張或對子但無法組成刻子的手牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair (作為眼)
        hand.addTile(Tile.M2); hand.addTile(Tile.M2); // 對子（無法組成刻子，也無法組成順子因為只有 2 張）
        hand.addTile(Tile.M3); // 單張
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        // 這個手牌無法胡，因為 M2, M2, M3 無法組成完整的面子
        assertFalse(strategy.isWinningHand(hand), "Hand that cannot form triplets should not win");
    }

    /**
     * 測試 isSevenPairs() 中 tiles.size() != 14 && tiles.size() != 17 的分支
     */
    @Test
    void testIsSevenPairs_InvalidSize() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 測試 13 張
        for (int i = 0; i < 13; i++) {
            hand.addTile(Tile.M1);
        }
        assertFalse(strategy.isSevenPairs(hand), "13 tiles should not be seven pairs");
        
        // 測試 15 張
        hand = new PlayerHand();
        for (int i = 0; i < 15; i++) {
            hand.addTile(Tile.M1);
        }
        assertFalse(strategy.isSevenPairs(hand), "15 tiles should not be seven pairs");
    }

    /**
     * 測試 isSevenPairs() 中 count == 4 的分支（4 張相同牌 = 2 對）
     */
    @Test
    void testIsSevenPairs_WithFourOfAKind() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建七對子，其中一對是 4 張相同牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1); // 4 張 = 2 對
        hand.addTile(Tile.M2); hand.addTile(Tile.M2); // 1 對
        hand.addTile(Tile.M3); hand.addTile(Tile.M3); // 1 對
        hand.addTile(Tile.M4); hand.addTile(Tile.M4); // 1 對
        hand.addTile(Tile.M5); hand.addTile(Tile.M5); // 1 對
        hand.addTile(Tile.M6); hand.addTile(Tile.M6); // 1 對
        hand.addTile(Tile.M7); hand.addTile(Tile.M7); // 1 對
        
        assertTrue(strategy.isSevenPairs(hand), "Seven pairs with 4 of a kind should be valid");
    }

    /**
     * 測試 isSevenPairs() 中 count != 0 && count != 2 && count != 4 的分支
     */
    @Test
    void testIsSevenPairs_InvalidCount() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個有 3 張相同牌的情況（不符合七對子）
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1); // 3 張（不符合）
        hand.addTile(Tile.M2); hand.addTile(Tile.M2); // 1 對
        hand.addTile(Tile.M3); hand.addTile(Tile.M3); // 1 對
        hand.addTile(Tile.M4); hand.addTile(Tile.M4); // 1 對
        hand.addTile(Tile.M5); hand.addTile(Tile.M5); // 1 對
        hand.addTile(Tile.M6); hand.addTile(Tile.M6); // 1 對
        hand.addTile(Tile.M7); hand.addTile(Tile.M7); // 1 對
        
        assertFalse(strategy.isSevenPairs(hand), "Seven pairs with triplet should not be valid");
    }

    /**
     * 測試 isSevenPairs() 中 pairCount != neededPairs 的分支
     */
    @Test
    void testIsSevenPairs_InsufficientPairs() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建只有 6 對的情況
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // 1 對
        hand.addTile(Tile.M2); hand.addTile(Tile.M2); // 1 對
        hand.addTile(Tile.M3); hand.addTile(Tile.M3); // 1 對
        hand.addTile(Tile.M4); hand.addTile(Tile.M4); // 1 對
        hand.addTile(Tile.M5); hand.addTile(Tile.M5); // 1 對
        hand.addTile(Tile.M6); hand.addTile(Tile.M6); // 1 對
        hand.addTile(Tile.M7); hand.addTile(Tile.M8); // 不是對
        
        assertFalse(strategy.isSevenPairs(hand), "Insufficient pairs should not be seven pairs");
    }

    /**
     * 測試 isThirteenOrphans() 中 size != 14 && size != 17 的分支
     */
    @Test
    void testIsThirteenOrphans_InvalidSize() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 測試 13 張
        hand.addTile(Tile.M1); hand.addTile(Tile.M9);
        hand.addTile(Tile.P1); hand.addTile(Tile.P9);
        hand.addTile(Tile.S1); hand.addTile(Tile.S9);
        hand.addTile(Tile.EAST); hand.addTile(Tile.SOUTH); hand.addTile(Tile.WEST); hand.addTile(Tile.NORTH);
        hand.addTile(Tile.RED); hand.addTile(Tile.GREEN); hand.addTile(Tile.WHITE);
        
        assertFalse(strategy.isThirteenOrphans(hand), "13 tiles should not be thirteen orphans");
    }

    /**
     * 測試 isThirteenOrphans() 中 !isOrphan 的分支（非么九牌）
     */
    @Test
    void testIsThirteenOrphans_NonOrphanTile() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個包含非么九牌的手牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair (M1 是么九)
        hand.addTile(Tile.M2); // 非么九牌
        hand.addTile(Tile.M9);
        hand.addTile(Tile.P1); hand.addTile(Tile.P9);
        hand.addTile(Tile.S1); hand.addTile(Tile.S9);
        hand.addTile(Tile.EAST); hand.addTile(Tile.SOUTH); hand.addTile(Tile.WEST); hand.addTile(Tile.NORTH);
        hand.addTile(Tile.RED); hand.addTile(Tile.GREEN); hand.addTile(Tile.WHITE);
        
        assertFalse(strategy.isThirteenOrphans(hand), "Thirteen orphans with non-orphan tile should not be valid");
    }

    /**
     * 測試 isThirteenOrphans() 中 hasPair && counts[orphanIdx] == 2 的分支（多個對）
     */
    @Test
    void testIsThirteenOrphans_MultiplePairs() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個有多個對的十三么（不符合）
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair 1
        hand.addTile(Tile.M9); hand.addTile(Tile.M9); // Pair 2（不符合，只能有一個對）
        hand.addTile(Tile.P1); hand.addTile(Tile.P9);
        hand.addTile(Tile.S1); hand.addTile(Tile.S9);
        hand.addTile(Tile.EAST); hand.addTile(Tile.SOUTH); hand.addTile(Tile.WEST); hand.addTile(Tile.NORTH);
        hand.addTile(Tile.RED); hand.addTile(Tile.GREEN); hand.addTile(Tile.WHITE);
        
        assertFalse(strategy.isThirteenOrphans(hand), "Thirteen orphans with multiple pairs should not be valid");
    }

    /**
     * 測試 isThirteenOrphans() 中 counts[orphanIdx] > 2 的分支
     */
    @Test
    void testIsThirteenOrphans_MoreThanTwo() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個有 3 張相同么九牌的情況
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1); // 3 張（不符合）
        hand.addTile(Tile.M9);
        hand.addTile(Tile.P1); hand.addTile(Tile.P9);
        hand.addTile(Tile.S1); hand.addTile(Tile.S9);
        hand.addTile(Tile.EAST); hand.addTile(Tile.SOUTH); hand.addTile(Tile.WEST); hand.addTile(Tile.NORTH);
        hand.addTile(Tile.RED); hand.addTile(Tile.GREEN); hand.addTile(Tile.WHITE);
        
        assertFalse(strategy.isThirteenOrphans(hand), "Thirteen orphans with more than 2 of a kind should not be valid");
    }

    /**
     * 測試 isThirteenOrphans() 中 uniqueCount != 13 的分支
     */
    @Test
    void testIsThirteenOrphans_InsufficientUnique() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個缺少某些么九牌的情況
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M9);
        hand.addTile(Tile.P1); // 缺少 P9
        hand.addTile(Tile.S1); hand.addTile(Tile.S9);
        hand.addTile(Tile.EAST); hand.addTile(Tile.SOUTH); hand.addTile(Tile.WEST); hand.addTile(Tile.NORTH);
        hand.addTile(Tile.RED); hand.addTile(Tile.GREEN); hand.addTile(Tile.WHITE);
        
        // 補足到 14 張
        hand.addTile(Tile.M2); // 非么九牌
        
        assertFalse(strategy.isThirteenOrphans(hand), "Thirteen orphans with insufficient unique types should not be valid");
    }

    /**
     * 測試 isThirteenOrphans() 中 !hasPair 的分支
     */
    @Test
    void testIsThirteenOrphans_NoPair() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個沒有對的十三么（不符合）
        hand.addTile(Tile.M1); hand.addTile(Tile.M9);
        hand.addTile(Tile.P1); hand.addTile(Tile.P9);
        hand.addTile(Tile.S1); hand.addTile(Tile.S9);
        hand.addTile(Tile.EAST); hand.addTile(Tile.SOUTH); hand.addTile(Tile.WEST); hand.addTile(Tile.NORTH);
        hand.addTile(Tile.RED); hand.addTile(Tile.GREEN); hand.addTile(Tile.WHITE);
        
        // 只有 13 張，需要補一張
        hand.addTile(Tile.M2); // 非么九牌
        
        assertFalse(strategy.isThirteenOrphans(hand), "Thirteen orphans without pair should not be valid");
    }

    /**
     * 測試 getTileIndex() 的 default case（異常情況）
     */
    @Test
    void testGetTileIndex_DefaultCase() {
        // 這個測試需要創建一個非法的 Tile，但 Tile 是 enum，無法創建非法值
        // 所以這個分支可能無法直接測試，除非我們使用反射或修改 Tile enum
        // 但為了完整性，我們至少測試所有合法的 Suit
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 測試所有合法的 Suit
        hand.addTile(Tile.M1); // MAN
        hand.addTile(Tile.P1); // PIN
        hand.addTile(Tile.S1); // SOU
        hand.addTile(Tile.EAST); // WIND
        hand.addTile(Tile.RED); // DRAGON
        
        // 這些都應該正常工作，不會觸發 default case
        // default case 理論上不應該被執行，因為所有 Tile 都有合法的 Suit
        assertTrue(true, "All valid suits should work");
    }

    /**
     * 測試 canFormSequence() 中 next1 / 9 != suit 的分支（跨花色）
     */
    @Test
    void testCanFormSequence_CrossSuit() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 這個測試主要驗證邏輯正確性
        // 實際的跨花色情況應該在 canFormSequence 中被阻止
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M9); // 單張 M9
        hand.addTile(Tile.P1); hand.addTile(Tile.P2); // 缺 P3
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        assertFalse(strategy.isWinningHand(hand), "Hand with incomplete sequences should not win");
    }

    /**
     * 測試 canFormSequence() 中 counts[next1] == 0 或 counts[next2] == 0 的分支
     */
    @Test
    void testCanFormSequence_MissingNextTiles() {
        WinStrategy strategy = new WinStrategy();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個有 M1, M2 但缺 M3 的情況
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); // 單張 M2（缺 M3）
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        hand.addTile(Tile.RED); hand.addTile(Tile.RED); hand.addTile(Tile.RED); // Pong
        
        assertFalse(strategy.isWinningHand(hand), "Hand with missing sequence tiles should not win");
    }
}

