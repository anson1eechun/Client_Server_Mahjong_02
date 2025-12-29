package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ActionProcessor 額外分支覆蓋率測試
 * 補充測試未覆蓋的分支以達到 90% 覆蓋率
 */
class ActionProcessorAdditionalBranchTest {

    /**
     * 測試 checkPossibleActions() 中 i == discardPlayerIndex 的分支（跳過打牌者）
     */
    @Test
    void testCheckPossibleActions_SkipDiscardPlayer() {
        ActionProcessor processor = new ActionProcessor();
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // 設置手牌，讓玩家 1 可以碰
        hands.get(1).addTile(Tile.M1);
        hands.get(1).addTile(Tile.M1);
        
        // 玩家 0 打出 M1，但玩家 0 自己不能碰自己的牌
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, Tile.M1, 0, 1);
        
        // 驗證玩家 0 沒有動作（因為是打牌者）
        boolean player0HasAction = actions.stream()
            .anyMatch(a -> a.getPlayerIndex() == 0);
        assertFalse(player0HasAction, "Discard player should not have actions");
    }

    /**
     * 測試 checkPossibleActions() 中 i != discardPlayerIndex 的分支
     */
    @Test
    void testCheckPossibleActions_OtherPlayers() {
        ActionProcessor processor = new ActionProcessor();
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // 設置手牌，讓玩家 1 可以碰
        hands.get(1).addTile(Tile.M1);
        hands.get(1).addTile(Tile.M1);
        
        // 玩家 0 打出 M1
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, Tile.M1, 0, 1);
        
        // 驗證玩家 1 有動作
        boolean player1HasAction = actions.stream()
            .anyMatch(a -> a.getPlayerIndex() == 1);
        assertTrue(player1HasAction, "Other players should have actions");
    }

    /**
     * 測試 checkPossibleActions() 中 i == nextPlayer 的分支（吃牌）
     */
    @Test
    void testCheckPossibleActions_Chow_NextPlayer() {
        ActionProcessor processor = new ActionProcessor();
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // 設置手牌，讓玩家 1（下家）可以吃
        hands.get(1).addTile(Tile.M1);
        hands.get(1).addTile(Tile.M2);
        
        // 玩家 0 打出 M3
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, Tile.M3, 0, 1);
        
        // 驗證玩家 1 有吃牌動作
        boolean hasChow = actions.stream()
            .anyMatch(a -> a.getType() == ActionProcessor.ActionType.CHOW && a.getPlayerIndex() == 1);
        assertTrue(hasChow, "Next player should be able to chow");
    }

    /**
     * 測試 checkPossibleActions() 中 i != nextPlayer 的分支（非下家不能吃）
     */
    @Test
    void testCheckPossibleActions_Chow_NotNextPlayer() {
        ActionProcessor processor = new ActionProcessor();
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // 設置手牌，讓玩家 2（非下家）可以吃
        hands.get(2).addTile(Tile.M1);
        hands.get(2).addTile(Tile.M2);
        
        // 玩家 0 打出 M3
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, Tile.M3, 0, 1);
        
        // 驗證玩家 2 沒有吃牌動作（因為不是下家）
        boolean hasChow = actions.stream()
            .anyMatch(a -> a.getType() == ActionProcessor.ActionType.CHOW && a.getPlayerIndex() == 2);
        assertFalse(hasChow, "Non-next player should not be able to chow");
    }

    /**
     * 測試 getChowOptions() 中 !targetTile.isNumberTile() 的分支
     */
    @Test
    void testGetChowOptions_NonNumberTile() {
        ActionProcessor processor = new ActionProcessor();
        List<PlayerHand> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
        
        // 設置手牌
        hands.get(1).addTile(Tile.EAST);
        hands.get(1).addTile(Tile.EAST);
        
        // 玩家 0 打出字牌（不能吃）
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, Tile.SOUTH, 0, 1);
        
        // 驗證沒有吃牌動作（字牌不能吃）
        boolean hasChow = actions.stream()
            .anyMatch(a -> a.getType() == ActionProcessor.ActionType.CHOW);
        assertFalse(hasChow, "Non-number tiles cannot be chowed");
    }

    /**
     * 測試 executeKong() 中 removed != 3 的分支（異常情況）
     */
    @Test
    void testExecuteKong_InsufficientTiles() {
        ActionProcessor processor = new ActionProcessor();
        PlayerHand hand = new PlayerHand();
        
        // 只添加 2 張牌（不足以槓）
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        
        // 應該拋出異常
        assertThrows(IllegalStateException.class, () -> {
            processor.executeKong(hand, Tile.M1);
        }, "Kong requires 3 tiles, should throw exception");
    }

    /**
     * 測試 executePong() 中 removed != 2 的分支（異常情況）
     */
    @Test
    void testExecutePong_InsufficientTiles() {
        ActionProcessor processor = new ActionProcessor();
        PlayerHand hand = new PlayerHand();
        
        // 只添加 1 張牌（不足以碰）
        hand.addTile(Tile.M1);
        
        // 應該拋出異常
        assertThrows(IllegalStateException.class, () -> {
            processor.executePong(hand, Tile.M1);
        }, "Pong requires 2 tiles, should throw exception");
    }

    /**
     * 測試 executeConcealedKong() 中 removed != 4 的分支（異常情況）
     */
    @Test
    void testExecuteConcealedKong_InsufficientTiles() {
        ActionProcessor processor = new ActionProcessor();
        PlayerHand hand = new PlayerHand();
        
        // 只添加 3 張牌（不足以暗槓）
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        
        // 應該拋出異常
        assertThrows(IllegalStateException.class, () -> {
            processor.executeConcealedKong(hand, Tile.M1);
        }, "Concealed kong requires 4 tiles, should throw exception");
    }

    /**
     * 測試 executeChow() 中 action.getType() != CHOW 的分支
     */
    @Test
    void testExecuteChow_InvalidActionType() {
        ActionProcessor processor = new ActionProcessor();
        PlayerHand hand = new PlayerHand();
        
        // 創建一個非 CHOW 動作
        ActionProcessor.Action action = new ActionProcessor.Action(
            ActionProcessor.ActionType.PONG, 0, Tile.M1);
        
        // 應該拋出異常
        assertThrows(IllegalArgumentException.class, () -> {
            processor.executeChow(hand, action);
        }, "ExecuteChow should only accept CHOW actions");
    }

    /**
     * 測試 getConcealedKongOptions() 中 entry.getValue() != 4 的分支
     */
    @Test
    void testGetConcealedKongOptions_NoFourOfAKind() {
        ActionProcessor processor = new ActionProcessor();
        PlayerHand hand = new PlayerHand();
        
        // 添加 3 張相同的牌（不足以暗槓）
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        
        List<Tile> options = processor.getConcealedKongOptions(hand);
        
        // 應該沒有選項
        assertTrue(options.isEmpty(), "Should have no concealed kong options");
    }

    /**
     * 測試 getConcealedKongOptions() 中 entry.getValue() == 4 的分支
     */
    @Test
    void testGetConcealedKongOptions_FourOfAKind() {
        ActionProcessor processor = new ActionProcessor();
        PlayerHand hand = new PlayerHand();
        
        // 添加 4 張相同的牌
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        
        List<Tile> options = processor.getConcealedKongOptions(hand);
        
        // 應該有選項
        assertFalse(options.isEmpty(), "Should have concealed kong options");
        assertTrue(options.contains(Tile.M1), "Should include M1 as option");
    }
}

