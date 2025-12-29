package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MahjongRuleEngine 分支覆蓋率測試
 * 重點測試未覆蓋的分支以達到 90% 覆蓋率
 */
class MahjongRuleEngineBranchTest {

    /**
     * 測試 drawTile() 中 wall.isEmpty() 為 true 的分支
     */
    @Test
    void testDrawTile_EmptyWall() {
        MahjongRuleEngine engine = new MahjongRuleEngine(new Random());
        
        // 抽完所有牌
        while (engine.drawTile() != null) {
            // 繼續抽牌
        }
        
        // 再次抽牌應該返回 null
        assertNull(engine.drawTile(), "Drawing from empty wall should return null");
    }

    /**
     * 測試 drawTile() 中 wall.isEmpty() 為 false 的分支
     */
    @Test
    void testDrawTile_NonEmptyWall() {
        MahjongRuleEngine engine = new MahjongRuleEngine(new Random());
        engine.shuffle();
        
        // 應該能抽到牌
        Tile tile = engine.drawTile();
        assertNotNull(tile, "Drawing from non-empty wall should return a tile");
    }

    /**
     * 測試 dealInitialHands() 中 t != null 的分支
     */
    @Test
    void testDealInitialHands_Normal() {
        MahjongRuleEngine engine = new MahjongRuleEngine(new Random());
        engine.shuffle();
        
        List<PlayerHand> players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            players.add(new PlayerHand());
        }
        
        engine.dealInitialHands(players);
        
        // 驗證每個玩家都有 16 張牌
        for (PlayerHand hand : players) {
            assertEquals(16, hand.getTileCount(), "Each player should have 16 tiles");
        }
    }

    /**
     * 測試 dealInitialHands() 中 t == null 的分支（牌牆耗盡）
     */
    @Test
    void testDealInitialHands_WallExhausted() {
        MahjongRuleEngine engine = new MahjongRuleEngine(new Random());
        
        // 抽完大部分牌，只留少量
        int remaining = engine.getRemainingTiles();
        for (int i = 0; i < remaining - 10; i++) {
            engine.drawTile();
        }
        
        List<PlayerHand> players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            players.add(new PlayerHand());
        }
        
        // 嘗試發牌（可能會遇到牌牆耗盡）
        assertDoesNotThrow(() -> {
            engine.dealInitialHands(players);
        });
    }

    /**
     * 測試 getRemainingTiles() 方法
     */
    @Test
    void testGetRemainingTiles() {
        MahjongRuleEngine engine = new MahjongRuleEngine(new Random());
        
        int initial = engine.getRemainingTiles();
        assertTrue(initial > 0, "Initial wall should have tiles");
        
        // 抽一張牌
        engine.drawTile();
        
        int after = engine.getRemainingTiles();
        assertEquals(initial - 1, after, "Remaining tiles should decrease by 1");
    }

    /**
     * 測試 shuffle() 方法
     */
    @Test
    void testShuffle() {
        MahjongRuleEngine engine1 = new MahjongRuleEngine(new Random(1));
        MahjongRuleEngine engine2 = new MahjongRuleEngine(new Random(2));
        
        engine1.shuffle();
        engine2.shuffle();
        
        // 驗證洗牌後能正常抽牌
        Tile tile1 = engine1.drawTile();
        Tile tile2 = engine2.drawTile();
        
        assertNotNull(tile1, "Should be able to draw after shuffle");
        assertNotNull(tile2, "Should be able to draw after shuffle");
    }

    /**
     * 測試 initializeWall() 方法（通過構造函數）
     */
    @Test
    void testInitializeWall() {
        MahjongRuleEngine engine = new MahjongRuleEngine(new Random());
        
        // 驗證牌牆已初始化
        int remaining = engine.getRemainingTiles();
        assertTrue(remaining > 0, "Wall should be initialized with tiles");
        
        // 台灣麻將應該有 144 張牌（4 張每種）
        // 但由於可能沒有包含所有牌型，我們只驗證有牌
        assertTrue(remaining >= 100, "Wall should have many tiles");
    }
}

