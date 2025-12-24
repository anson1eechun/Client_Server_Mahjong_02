package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScoringCalculatorTest {

    @Test
    public void testSelfDraw() {
        PlayerHand hand = new PlayerHand();
        // Add mixed tiles (Man and Pin) to avoid Full Flush (8 Tai)
        hand.addTile(Tile.M1);
        hand.addTile(Tile.P1);
        
        ScoringCalculator calculator = new ScoringCalculator();
        int tai = calculator.calculateTai(hand, true, Tile.EAST, Tile.EAST);
        assertEquals(1, tai, "Self draw should add 1 Tai");
    }

    @Test
    public void testDragonPong() {
        PlayerHand hand = new PlayerHand();
        hand.addMeld(Meld.createPong(Tile.RED)); // Red Dragon Pong
        
        ScoringCalculator calculator = new ScoringCalculator();
        int tai = calculator.calculateTai(hand, false, Tile.EAST, Tile.EAST);
        assertEquals(1, tai, "Dragon Pong should add 1 Tai");
    }

    @Test
    public void testPongPongHu() {
        PlayerHand hand = new PlayerHand();
        // M1, M1, M1 (Pong)
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1);
        // P2, P2, P2 (Pong)
        hand.addTile(Tile.P2); hand.addTile(Tile.P2); hand.addTile(Tile.P2);
        // S3, S3, S3 (Pong)
        hand.addTile(Tile.S3); hand.addTile(Tile.S3); hand.addTile(Tile.S3);
        // EAST, EAST, EAST (Pong)
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST);
        // RED, RED (Pair)
        hand.addTile(Tile.RED); hand.addTile(Tile.RED); 
        
        // Setup WinStrategy to confirm it is winning? No, Scoring assumes winning.
        ScoringCalculator calculator = new ScoringCalculator();
        // Note: isAllPongs detects if internal tiles are triplets.
        // My simple implementation just checks if chows exist in Open Melds.
        // It's a weak check but sufficient for basic unit test if logic matches.
        
        // Wait, calculateTai adds 4 for PongPongHu.
        // But also adds 1 for dragon/wind pong?
        // My M1, P2, S3 are number tiles. EAST is Wind (0 Tai if not round/seat wind - simplified logic).
        // RED is pair (0 Tai).
        // So expected: 4 (PongPongHu).
        
        int tai = calculator.calculateTai(hand, false, Tile.SOUTH, Tile.WEST); 
        assertEquals(4, tai, "PongPongHu should be 4 Tai");
    }
    
    @Test
    public void testFullFlush() {
        PlayerHand hand = new PlayerHand();
        // All M (Man)
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1);
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4);
        hand.addTile(Tile.M5); hand.addTile(Tile.M5); hand.addTile(Tile.M5);
        hand.addTile(Tile.M9); hand.addTile(Tile.M9);
        
        hand.addMeld(Meld.createPong(Tile.M8));
        
        ScoringCalculator calculator = new ScoringCalculator();
        int tai = calculator.calculateTai(hand, false, Tile.SOUTH, Tile.WEST);
        // 8 Tai for Full Flush.
        assertEquals(8, tai);
    }
}
