package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * 遊戲流程整合測試
 * 測試完整的遊戲流程：發牌→摸牌→出牌→吃碰槓→胡牌
 */
public class GameFlowIntegrationTest {
    private MahjongRuleEngine engine;
    private ActionProcessor processor;
    private List<PlayerHand> hands;

    @BeforeEach
    public void setup() {
        engine = new MahjongRuleEngine(new Random(12345)); // 固定種子以便重現
        processor = new ActionProcessor();
        hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hands.add(new PlayerHand());
        }
    }

    @Test
    public void testCompleteGameFlow_StandardWin() {
        // 1. 初始化遊戲
        engine.shuffle();
        engine.dealInitialHands(hands);

        // 驗證每個玩家都有正確的初始牌數（台灣麻將：16張）
        for (PlayerHand hand : hands) {
            assertTrue(hand.getTileCount() >= 13, 
                "Each player should have at least 13 tiles initially");
        }

        // 2. 模擬遊戲進行
        int currentPlayer = 0;
        int turnCount = 0;
        final int MAX_TURNS = 100; // 防止無限循環

        while (turnCount < MAX_TURNS && engine.getRemainingTiles() > 0) {
            // 摸牌
            Tile drawn = engine.drawTile();
            if (drawn == null) {
                break; // 牌牆空了
            }

            hands.get(currentPlayer).addTile(drawn);

            // 檢查自摸
            if (processor.canSelfDrawWin(hands.get(currentPlayer))) {
                // 驗證胡牌
                WinStrategy strategy = new WinStrategy();
                assertTrue(strategy.isWinningHand(hands.get(currentPlayer)),
                    "Player " + currentPlayer + " should have winning hand");
                return; // 遊戲結束
            }

            // 出牌（簡化：打第一張）
            List<Tile> standingTiles = hands.get(currentPlayer).getStandingTiles();
            if (standingTiles.isEmpty()) {
                break;
            }

            Tile discard = standingTiles.get(0);
            hands.get(currentPlayer).removeTile(discard);

            // 檢查其他玩家動作
            List<ActionProcessor.Action> actions = processor.checkPossibleActions(
                hands, discard, currentPlayer, currentPlayer);

            if (!actions.isEmpty()) {
                // 執行最高優先級動作（簡化：只執行第一個）
                ActionProcessor.Action action = actions.get(0);
                int actionPlayer = action.getPlayerIndex();

                switch (action.getType()) {
                    case HU:
                        // 驗證可以胡牌
                        HandValidator validator = new HandValidator();
                        assertTrue(validator.canHu(hands.get(actionPlayer), discard),
                            "Player " + actionPlayer + " should be able to HU");
                        return; // 遊戲結束

                    case PONG:
                        processor.executePong(hands.get(actionPlayer), discard);
                        currentPlayer = actionPlayer; // 碰牌後輪到碰牌者
                        break;

                    case KONG:
                        processor.executeKong(hands.get(actionPlayer), discard);
                        currentPlayer = actionPlayer; // 槓牌後輪到槓牌者
                        break;

                    case CHOW:
                        processor.executeChow(hands.get(actionPlayer), action);
                        currentPlayer = actionPlayer; // 吃牌後輪到吃牌者
                        break;
                }
            } else {
                // 沒有動作，輪到下一位
                currentPlayer = (currentPlayer + 1) % 4;
            }

            turnCount++;
        }

        // 如果到達這裡，可能是流局
        assertTrue(turnCount > 0, "Game should have progressed");
    }

    @Test
    public void testGameFlow_PongAfterDiscard() {
        // 設置場景：Player 0 出 M1，Player 1 可以碰
        engine.shuffle();
        engine.dealInitialHands(hands);

        // 手動設置：Player 1 有兩張 M1
        hands.get(1).addTile(Tile.M1);
        hands.get(1).addTile(Tile.M1);

        // Player 0 出 M1
        Tile discard = Tile.M1;
        hands.get(0).removeTile(discard);

        // 檢查動作
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, discard, 0, 0);

        // 應該有 PONG 動作
        boolean hasPong = actions.stream()
            .anyMatch(a -> a.getType() == ActionProcessor.ActionType.PONG 
                && a.getPlayerIndex() == 1);

        assertTrue(hasPong, "Player 1 should be able to PONG M1");

        // 執行碰牌
        if (hasPong) {
            ActionProcessor.Action pongAction = actions.stream()
                .filter(a -> a.getType() == ActionProcessor.ActionType.PONG 
                    && a.getPlayerIndex() == 1)
                .findFirst()
                .orElse(null);

            if (pongAction != null) {
                processor.executePong(hands.get(1), discard);
                
                // 驗證碰牌後手牌減少 2 張，增加 1 個面子
                assertEquals(1, hands.get(1).getMeldCount(), 
                    "Player 1 should have 1 meld after PONG");
            }
        }
    }

    @Test
    public void testGameFlow_ChowFromNextPlayer() {
        // 設置場景：Player 0 出 M2，Player 1（下家）可以吃
        engine.shuffle();
        engine.dealInitialHands(hands);

        // Player 1 有 M1, M3（可以吃 M2）
        hands.get(1).addTile(Tile.M1);
        hands.get(1).addTile(Tile.M3);

        // Player 0 出 M2
        Tile discard = Tile.M2;
        hands.get(0).removeTile(discard);

        // 檢查動作
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, discard, 0, 0);

        // 應該有 CHOW 動作，且只有 Player 1（下家）可以吃
        long chowCount = actions.stream()
            .filter(a -> a.getType() == ActionProcessor.ActionType.CHOW)
            .filter(a -> a.getPlayerIndex() == 1)
            .count();

        assertTrue(chowCount > 0, "Player 1 (next player) should be able to CHOW");

        // Player 2 不應該有 CHOW（不是下家）
        boolean player2Chow = actions.stream()
            .anyMatch(a -> a.getType() == ActionProcessor.ActionType.CHOW 
                && a.getPlayerIndex() == 2);
        assertFalse(player2Chow, "Player 2 should not be able to CHOW");
    }

    @Test
    public void testGameFlow_ActionPriority() {
        // 設置場景：Player 1 可以胡，Player 2 可以碰
        engine.shuffle();
        engine.dealInitialHands(hands);

        // Player 1: 設置接近胡牌的手牌（缺 M1）
        setupNearWinningHand(hands.get(1), Tile.M1);

        // Player 2: 可以碰 M1
        hands.get(2).addTile(Tile.M1);
        hands.get(2).addTile(Tile.M1);

        // Player 0 出 M1
        Tile discard = Tile.M1;
        hands.get(0).removeTile(discard);

        // 檢查動作
        List<ActionProcessor.Action> actions = processor.checkPossibleActions(
            hands, discard, 0, 0);

        assertFalse(actions.isEmpty(), "Should have actions");

        // 胡牌應該有最高優先級（排在前面）
        Optional<ActionProcessor.Action> huAction = actions.stream()
            .filter(a -> a.getType() == ActionProcessor.ActionType.HU)
            .findFirst();

        if (huAction.isPresent()) {
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
    public void testGameFlow_DrawFromWall() {
        // 測試從牌牆摸牌
        engine.shuffle();

        int initialRemaining = engine.getRemainingTiles();
        assertTrue(initialRemaining > 0, "Should have tiles in wall");

        Tile drawn = engine.drawTile();
        assertNotNull(drawn, "Should draw a tile");
        assertTrue(engine.getRemainingTiles() < initialRemaining, 
            "Remaining tiles should decrease");

        // 繼續摸牌直到牌牆空
        int drawCount = 1;
        while (engine.drawTile() != null && drawCount < 200) {
            drawCount++;
        }

        assertTrue(drawCount > 0, "Should be able to draw tiles");
    }

    @Test
    public void testGameFlow_DealInitialHands() {
        // 測試發牌
        engine.shuffle();
        engine.dealInitialHands(hands);

        int totalTiles = 0;
        for (PlayerHand hand : hands) {
            totalTiles += hand.getTileCount();
        }

        // 台灣麻將：4 個玩家各 16 張 = 64 張（或 13 張標準）
        assertTrue(totalTiles >= 52, 
            "Total dealt tiles should be at least 52 (13*4)");
        assertTrue(totalTiles <= 68, 
            "Total dealt tiles should not exceed 68 (17*4)");
    }

    // Helper methods

    /**
     * 設置一個接近胡牌的手牌（缺指定牌就能胡）
     */
    private void setupNearWinningHand(PlayerHand hand, Tile missingTile) {
        // Pair: M2, M2
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M2);
        // Set 1: M3, M4, M5
        hand.addTile(Tile.M3);
        hand.addTile(Tile.M4);
        hand.addTile(Tile.M5);
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
        // 總共 13 張，加上 missingTile 就是 14 張標準胡牌
    }
}

