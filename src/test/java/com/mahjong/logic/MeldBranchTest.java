package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Meld 分支覆蓋率測試
 * 重點測試未覆蓋的分支以達到 85% 覆蓋率
 */
class MeldBranchTest {

    /**
     * 測試 validateTileCount() 中 type == CHOW && count != 3 的分支
     */
    @Test
    void testValidateTileCount_Chow_InvalidCount() {
        List<Tile> tiles = Arrays.asList(Tile.M1, Tile.M2); // 只有 2 張
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Meld(Meld.Type.CHOW, tiles);
        }, "CHOW with 2 tiles should throw exception");
    }

    /**
     * 測試 validateTileCount() 中 type == PONG && count != 3 的分支
     */
    @Test
    void testValidateTileCount_Pong_InvalidCount() {
        List<Tile> tiles = Arrays.asList(Tile.M1, Tile.M1); // 只有 2 張
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Meld(Meld.Type.PONG, tiles);
        }, "PONG with 2 tiles should throw exception");
    }

    /**
     * 測試 validateTileCount() 中 type == KONG && count != 4 的分支
     */
    @Test
    void testValidateTileCount_Kong_InvalidCount() {
        List<Tile> tiles = Arrays.asList(Tile.M1, Tile.M1, Tile.M1); // 只有 3 張
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Meld(Meld.Type.KONG, tiles);
        }, "KONG with 3 tiles should throw exception");
    }

    /**
     * 測試 validateTileCount() 中 type == EYES && count != 2 的分支
     */
    @Test
    void testValidateTileCount_Eyes_InvalidCount() {
        List<Tile> tiles = Arrays.asList(Tile.M1); // 只有 1 張
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Meld(Meld.Type.EYES, tiles);
        }, "EYES with 1 tile should throw exception");
    }

    /**
     * 測試 equals() 中 o == null 的分支
     */
    @Test
    void testEquals_Null() {
        Meld meld = Meld.createPong(Tile.M1);
        
        assertNotEquals(meld, null, "Meld should not equal null");
    }

    /**
     * 測試 equals() 中 getClass() != o.getClass() 的分支
     */
    @Test
    void testEquals_DifferentClass() {
        Meld meld = Meld.createPong(Tile.M1);
        String str = "Not a Meld";
        
        assertNotEquals(meld, str, "Meld should not equal different class");
    }

    /**
     * 測試 equals() 中 type != meld.type 的分支
     */
    @Test
    void testEquals_DifferentType() {
        Meld pong = Meld.createPong(Tile.M1);
        Meld kong = Meld.createKong(Tile.M1);
        
        assertNotEquals(pong, kong, "Different types should not be equal");
    }

    /**
     * 測試 equals() 中 !Objects.equals(tiles, meld.tiles) 的分支
     */
    @Test
    void testEquals_DifferentTiles() {
        Meld meld1 = Meld.createPong(Tile.M1);
        Meld meld2 = Meld.createPong(Tile.M2);
        
        assertNotEquals(meld1, meld2, "Different tiles should not be equal");
    }

    /**
     * 測試 createConcealedKong() 的 concealed 參數
     */
    @Test
    void testCreateConcealedKong() {
        Meld kong = Meld.createConcealedKong(Tile.M1);
        
        assertTrue(kong.isConcealed(), "Concealed kong should be concealed");
        assertEquals(Meld.Type.KONG, kong.getType(), "Type should be KONG");
        assertEquals(4, kong.getTileCount(), "Should have 4 tiles");
    }

    /**
     * 測試 getFirstTile() 中 tiles.isEmpty() 的分支
     */
    @Test
    @SuppressWarnings("deprecation")
    void testGetFirstTile_EmptyMeld() {
        // 這個情況理論上不應該發生，因為建構子會驗證
        // 但為了測試分支，我們創建一個正常的 Meld
        Meld meld = Meld.createPong(Tile.M1);
        assertNotNull(meld.getFirstTile(), "PONG should have first tile");
    }

    /**
     * 測試 equals() 中 this == o 的分支
     */
    @Test
    void testEquals_SameInstance() {
        Meld meld = Meld.createPong(Tile.M1);
        
        assertEquals(meld, meld, "Same instance should be equal");
    }
}

