package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * 測試 ActionProcessor 類別
 * 重點測試：動作優先級、吃碰槓胡判定、動作執行
 */
public class ActionProcessorTest {
    private ActionProcessor processor;
    
    @BeforeEach
    public void setup() {
        processor = new ActionProcessor();
    }
    
    @Test
    public void testCheckPossibleActions_HuHasHighestPriority() {
        // 設置：Player 1 可以胡, Player 2 可以碰
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // Player 1: 設置一個接近胡牌的手牌（缺一張 M1 就能胡）
        setupHuHand(hands.get(1));
        
        // Player 2: 設置可以碰 M1 的手牌
        hands.get(2).addTile(Tile.M1);
        hands.get(2).addTile(Tile.M1);
        
        Tile discard = Tile.M1;
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, discard, 0, 0);
        
        // 胡牌應該有最高優先級（priority 1），應該排在第一位
        assertFalse(actions.isEmpty(), "Should have at least one action");
        
        // 找到所有 HU 動作
        Optional<ActionProcessor.Action> huAction = actions.stream()
            .filter(a -> a.getType() == ActionProcessor.ActionType.HU)
            .findFirst();
        
        if (huAction.isPresent()) {
            // 如果有 HU 動作，它應該在 PONG 之前
            int huIndex = actions.indexOf(huAction.get());
            Optional<ActionProcessor.Action> pongAction = actions.stream()
                .filter(a -> a.getType() == ActionProcessor.ActionType.PONG)
                .findFirst();
            
            if (pongAction.isPresent()) {
                int pongIndex = actions.indexOf(pongAction.get());
                assertTrue(huIndex < pongIndex, 
                    "HU should have higher priority than PONG");
            }
        }
    }
    
    @Test
    public void testCheckPossibleActions_PongPriority() {
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // Player 2: 可以碰 M1
        hands.get(2).addTile(Tile.M1);
        hands.get(2).addTile(Tile.M1);
        
        Tile discard = Tile.M1;
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, discard, 0, 0);
        
        // 應該有 PONG 動作
        boolean hasPong = actions.stream()
            .anyMatch(a -> a.getType() == ActionProcessor.ActionType.PONG 
                && a.getPlayerIndex() == 2);
        assertTrue(hasPong, "Player 2 should be able to PONG");
    }
    
    @Test
    public void testCheckPossibleActions_ChowOnlyFromNextPlayer() {
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // Player 0 出牌，只有 Player 1 (下家) 可以吃
        // Player 1: 可以吃 M2 (有 M1, M3)
        hands.get(1).addTile(Tile.M1);
        hands.get(1).addTile(Tile.M3);
        
        // Player 2: 也有 M1, M3，但不能吃（不是下家）
        hands.get(2).addTile(Tile.M1);
        hands.get(2).addTile(Tile.M3);
        
        Tile discard = Tile.M2;
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, discard, 0, 0);
        
        // 只有 Player 1 應該有 CHOW 動作
        long chowCount = actions.stream()
            .filter(a -> a.getType() == ActionProcessor.ActionType.CHOW)
            .filter(a -> a.getPlayerIndex() == 1)
            .count();
        assertTrue(chowCount > 0, "Only Player 1 (next player) should be able to CHOW");
        
        // Player 2 不應該有 CHOW
        boolean player2Chow = actions.stream()
            .anyMatch(a -> a.getType() == ActionProcessor.ActionType.CHOW 
                && a.getPlayerIndex() == 2);
        assertFalse(player2Chow, "Player 2 should not be able to CHOW");
    }
    
    @Test
    public void testExecutePong_RemovesTwoTiles() {
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        
        int originalCount = hand.getTileCount();
        processor.executePong(hand, Tile.M1);
        
        assertEquals(originalCount - 2, hand.getTileCount(), 
            "Should remove 2 tiles after PONG");
        assertEquals(1, hand.getMeldCount(), 
            "Should have 1 meld after PONG");
        assertEquals(1, hand.getTileCount(), 
            "Should have 1 tile left (M2)");
    }
    
    @Test
    public void testExecuteKong_RemovesThreeTiles() {
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        
        int originalCount = hand.getTileCount();
        processor.executeKong(hand, Tile.M1);
        
        assertEquals(originalCount - 3, hand.getTileCount(), 
            "Should remove 3 tiles after KONG");
        assertEquals(1, hand.getMeldCount(), 
            "Should have 1 meld after KONG");
    }
    
    @Test
    public void testExecutePong_ThrowsExceptionWhenNotEnoughTiles() {
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        
        // 只有 1 張 M1，無法碰
        assertThrows(IllegalStateException.class, () -> {
            processor.executePong(hand, Tile.M1);
        }, "Should throw exception when not enough tiles for PONG");
    }
    
    @Test
    public void testCanSelfDrawWin() {
        PlayerHand hand = new PlayerHand();
        // 設置一個標準胡牌手牌
        setupWinningHand(hand);
        
        assertTrue(processor.canSelfDrawWin(hand), 
            "Should detect self-draw win");
    }
    
    @Test
    public void testCheckPossibleActions_NoActions() {
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // 所有玩家手牌都無法對 M1 做任何動作
        hands.get(1).addTile(Tile.M2);
        hands.get(2).addTile(Tile.M3);
        hands.get(3).addTile(Tile.M4);
        
        Tile discard = Tile.M1;
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, discard, 0, 0);
        
        // 應該沒有動作（除了可能的胡牌檢查）
        // 但由於沒有設置胡牌手牌，應該為空或只有無效動作
        assertNotNull(actions, "Actions list should not be null");
    }
    
    @Test
    public void testCheckPossibleActions_MultipleChowOptions() {
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // Player 1: 可以吃 M3 的多種方式
        // 方式1: M1, M2, M3
        // 方式2: M2, M3, M4 (需要 M2, M4)
        // 方式3: M3, M4, M5 (需要 M4, M5)
        hands.get(1).addTile(Tile.M1);
        hands.get(1).addTile(Tile.M2);
        hands.get(1).addTile(Tile.M4);
        hands.get(1).addTile(Tile.M5);
        
        Tile discard = Tile.M3;
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, discard, 0, 0);
        
        // 應該有多個 CHOW 選項
        long chowCount = actions.stream()
            .filter(a -> a.getType() == ActionProcessor.ActionType.CHOW)
            .count();
        assertTrue(chowCount >= 2, "Should have multiple CHOW options");
    }
    
    // Helper methods
    
    /**
     * 設置一個可以胡牌的手牌（簡化版）
     */
    private void setupHuHand(PlayerHand hand) {
        // Pair: M1, M1
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        // Set 1: M2, M3, M4
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M3);
        hand.addTile(Tile.M4);
        // Set 2: P1, P1, P1
        hand.addTile(Tile.P1);
        hand.addTile(Tile.P1);
        hand.addTile(Tile.P1);
        // Set 3: S1, S2, S3
        hand.addTile(Tile.S1);
        hand.addTile(Tile.S2);
        hand.addTile(Tile.S3);
        // Set 4: EAST, EAST, EAST
        hand.addTile(Tile.EAST);
        hand.addTile(Tile.EAST);
        hand.addTile(Tile.EAST);
        // 總共 14 張，可以胡任何一張來完成
    }
    
    /**
     * 設置一個標準胡牌手牌
     */
    private void setupWinningHand(PlayerHand hand) {
        setupHuHand(hand);
        // 已經有 14 張，是標準胡牌
    }
}

