package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HandValidatorTest {

    @Test
    public void testCanPong() {
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.P5);
        
        HandValidator validator = new HandValidator();
        assertTrue(validator.canPong(hand, Tile.M1));
        assertFalse(validator.canPong(hand, Tile.P5));
        assertFalse(validator.canPong(hand, Tile.S1));
    }

    @Test
    public void testCanChow() {
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M3);
        
        HandValidator validator = new HandValidator();
        // M2, M3 + M1 -> Valid (1,2,3)
        assertTrue(validator.canChow(hand, Tile.M1)); 
        // M2, M3 + M4 -> Valid (2,3,4)
        assertTrue(validator.canChow(hand, Tile.M4)); 
        
        // Honors cannot chow
        assertFalse(validator.canChow(hand, Tile.EAST));
    }
}
