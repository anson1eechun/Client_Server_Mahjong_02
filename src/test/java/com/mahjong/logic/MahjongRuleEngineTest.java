package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MahjongRuleEngineTest {

    @Test
    public void testShuffleAndDeal() {
        Random mockRandom = mock(Random.class);
        // Determine seed or behavior if needed, but for simple shuffle check, real Random with fixed seed is better, or just use mock to verify interaction.
        // Actually, let's use a real Random with seed 42 for deterministic behavior if strictly needed.
        // But here we test behavior: deal 16 tiles to 4 players.
        
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
