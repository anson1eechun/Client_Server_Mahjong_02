package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MahjongRuleEngineTest {

    @Test
    public void testShuffleAndDeal() {
        // 移除造成報錯的 mock(Random.class)
        // 使用固定種子 (42) 來確保每次測試結果一致
        MahjongRuleEngine engine = new MahjongRuleEngine(new Random(42));

        // 136 tiles total (No Flowers).
        assertEquals(136, engine.getRemainingTiles());

        List<PlayerHand> players = new ArrayList<>();
        for (int i=0; i<4; i++) players.add(new PlayerHand());

        engine.shuffle();
        engine.dealInitialHands(players);

        // Check hands
        for (PlayerHand p : players) {
            assertEquals(16, p.getConnectionCount()); // Simply count
        }

        // 136 - (16*4) = 136 - 64 = 72
        assertEquals(72, engine.getRemainingTiles());
    }

    @Test
    public void testDrawUntilEmpty() {
        MahjongRuleEngine engine = new MahjongRuleEngine(new Random());
        int count = 0;
        while (engine.drawTile() != null) {
            count++;
        }
        assertEquals(136, count);
    }
}