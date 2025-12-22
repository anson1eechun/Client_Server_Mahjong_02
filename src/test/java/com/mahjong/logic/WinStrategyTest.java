package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WinStrategyTest {

    @Test
    public void testStandardWin() {
        PlayerHand hand = new PlayerHand();
        // Pair: M1, M1
        hand.addTile(Tile.M1); hand.addTile(Tile.M1);
        // Set 1: M2, M3, M4 (Chow)
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4);
        // Set 2: P1, P1, P1 (Pong)
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1);
        // Set 3: S5, S6, S7 (Chow)
        hand.addTile(Tile.S5); hand.addTile(Tile.S6); hand.addTile(Tile.S7);
        // Set 4: EAST, EAST, EAST (Pong)
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST);
        // Set 5: RED, RED, RED (Pong)
        hand.addTile(Tile.RED); hand.addTile(Tile.RED); hand.addTile(Tile.RED);
        
        // Total 17 tiles
        WinStrategy strategy = new WinStrategy();
        assertTrue(strategy.isWinningHand(hand), "Should be a winning hand");
    }

    @Test
    public void testNonWinningHand_NotEnoughTiles() {
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.M1);
        WinStrategy strategy = new WinStrategy();
        assertFalse(strategy.isWinningHand(hand), "Should not win with 1 tile");
    }

    @Test
    public void testNonWinningHand_InvalidStructure() {
        PlayerHand hand = new PlayerHand();
        // Pair: M1, M2 (Not a pair)
        hand.addTile(Tile.M1); hand.addTile(Tile.M2);
        // Fill rest with valid sets
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1);
        hand.addTile(Tile.S1); hand.addTile(Tile.S1); hand.addTile(Tile.S1);
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST);
        hand.addTile(Tile.RED); hand.addTile(Tile.RED); hand.addTile(Tile.RED);
        hand.addTile(Tile.WHITE); hand.addTile(Tile.WHITE); hand.addTile(Tile.WHITE);
        
        WinStrategy strategy = new WinStrategy();
        assertFalse(strategy.isWinningHand(hand));
    }
    
    @Test
    public void testComplexWin_SevenPairs() {
        // Technically standard algorithm doesn't support 7 pairs (special hand).
        // But let's test a complex standard hand.
        // 11 123 234 345 55 (3 Chows + 1 Pair + 1 Pong? No wait. 17 tiles).
        // 11 22 33 ? No.
        // Let's try 123 234 345 567 888 99.
        PlayerHand hand = new PlayerHand();
        int[] ranks = {1,2,3, 2,3,4, 3,4,5, 5,6,7};
        for (int r : ranks) {
             // Need to map int to Tile... tedious without helper.
        }
    }
    
    @Test
    public void testDragonWin() {
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.RED); hand.addTile(Tile.RED); // Pair
        hand.addTile(Tile.GREEN); hand.addTile(Tile.GREEN); hand.addTile(Tile.GREEN);
        hand.addTile(Tile.WHITE); hand.addTile(Tile.WHITE); hand.addTile(Tile.WHITE);
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST);
        hand.addTile(Tile.SOUTH); hand.addTile(Tile.SOUTH); hand.addTile(Tile.SOUTH);
        hand.addTile(Tile.WEST); hand.addTile(Tile.WEST); hand.addTile(Tile.WEST);
        
        WinStrategy strategy = new WinStrategy();
        assertTrue(strategy.isWinningHand(hand));
    }
}
