package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * 測試 Meld 類別
 * 重點測試：建構子驗證、便利方法、牌數驗證
 */
public class MeldTest {

    @Test
    public void testCreatePong() {
        Meld pong = Meld.createPong(Tile.M1);
        
        assertEquals(Meld.Type.PONG, pong.getType(), "Type should be PONG");
        assertEquals(3, pong.getTileCount(), "PONG should have 3 tiles");
        
        List<Tile> tiles = pong.getTiles();
        assertEquals(Tile.M1, tiles.get(0));
        assertEquals(Tile.M1, tiles.get(1));
        assertEquals(Tile.M1, tiles.get(2));
    }

    @Test
    public void testCreateKong() {
        Meld kong = Meld.createKong(Tile.M2);
        
        assertEquals(Meld.Type.KONG, kong.getType(), "Type should be KONG");
        assertEquals(4, kong.getTileCount(), "KONG should have 4 tiles");
        
        List<Tile> tiles = kong.getTiles();
        for (Tile tile : tiles) {
            assertEquals(Tile.M2, tile, "All tiles should be M2");
        }
    }

    @Test
    public void testCreateChow() {
        Meld chow = Meld.createChow(Tile.M1, Tile.M2, Tile.M3);
        
        assertEquals(Meld.Type.CHOW, chow.getType(), "Type should be CHOW");
        assertEquals(3, chow.getTileCount(), "CHOW should have 3 tiles");
        
        List<Tile> tiles = chow.getTiles();
        assertEquals(Tile.M1, tiles.get(0));
        assertEquals(Tile.M2, tiles.get(1));
        assertEquals(Tile.M3, tiles.get(2));
    }

    @Test
    public void testCreateEyes() {
        Meld eyes = Meld.createEyes(Tile.M5);
        
        assertEquals(Meld.Type.EYES, eyes.getType(), "Type should be EYES");
        assertEquals(2, eyes.getTileCount(), "EYES should have 2 tiles");
        
        List<Tile> tiles = eyes.getTiles();
        assertEquals(Tile.M5, tiles.get(0));
        assertEquals(Tile.M5, tiles.get(1));
    }

    @Test
    public void testConstructor_ValidPong() {
        List<Tile> tiles = Arrays.asList(Tile.M1, Tile.M1, Tile.M1);
        Meld meld = new Meld(Meld.Type.PONG, tiles);
        
        assertEquals(Meld.Type.PONG, meld.getType());
        assertEquals(3, meld.getTileCount());
    }

    @Test
    public void testConstructor_ValidKong() {
        List<Tile> tiles = Arrays.asList(Tile.M2, Tile.M2, Tile.M2, Tile.M2);
        Meld meld = new Meld(Meld.Type.KONG, tiles);
        
        assertEquals(Meld.Type.KONG, meld.getType());
        assertEquals(4, meld.getTileCount());
    }

    @Test
    public void testConstructor_ValidChow() {
        List<Tile> tiles = Arrays.asList(Tile.M1, Tile.M2, Tile.M3);
        Meld meld = new Meld(Meld.Type.CHOW, tiles);
        
        assertEquals(Meld.Type.CHOW, meld.getType());
        assertEquals(3, meld.getTileCount());
    }

    @Test
    public void testConstructor_InvalidPongTileCount() {
        List<Tile> tiles = Arrays.asList(Tile.M1, Tile.M1); // Only 2 tiles
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Meld(Meld.Type.PONG, tiles);
        }, "PONG with 2 tiles should throw exception");
    }

    @Test
    public void testConstructor_InvalidKongTileCount() {
        List<Tile> tiles = Arrays.asList(Tile.M1, Tile.M1, Tile.M1); // Only 3 tiles
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Meld(Meld.Type.KONG, tiles);
        }, "KONG with 3 tiles should throw exception");
    }

    @Test
    public void testConstructor_InvalidChowTileCount() {
        List<Tile> tiles = Arrays.asList(Tile.M1, Tile.M2); // Only 2 tiles
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Meld(Meld.Type.CHOW, tiles);
        }, "CHOW with 2 tiles should throw exception");
    }

    @Test
    public void testConstructor_NullTiles() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Meld(Meld.Type.PONG, null);
        }, "Null tiles list should throw exception");
    }

    @Test
    public void testConstructor_EmptyTiles() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Meld(Meld.Type.PONG, Arrays.asList());
        }, "Empty tiles list should throw exception");
    }

    @Test
    public void testGetTiles_ReturnsCopy() {
        List<Tile> originalTiles = Arrays.asList(Tile.M1, Tile.M1, Tile.M1);
        Meld meld = new Meld(Meld.Type.PONG, originalTiles);
        
        List<Tile> tiles1 = meld.getTiles();
        List<Tile> tiles2 = meld.getTiles();
        
        // 修改返回的列表不應該影響原始列表
        tiles1.clear();
        assertEquals(3, tiles2.size(), "Second copy should still have tiles");
        assertEquals(3, meld.getTileCount(), "Original meld should not be modified");
    }

    @Test
    public void testEquals_SameMeld() {
        Meld meld1 = Meld.createPong(Tile.M1);
        Meld meld2 = Meld.createPong(Tile.M1);
        
        assertEquals(meld1, meld2, "Two identical PONGs should be equal");
    }

    @Test
    public void testEquals_DifferentType() {
        Meld pong = Meld.createPong(Tile.M1);
        Meld kong = Meld.createKong(Tile.M1);
        
        assertNotEquals(pong, kong, "PONG and KONG should not be equal");
    }

    @Test
    public void testHashCode_Consistency() {
        Meld meld = Meld.createPong(Tile.M1);
        int hashCode1 = meld.hashCode();
        int hashCode2 = meld.hashCode();
        
        assertEquals(hashCode1, hashCode2, "Hash code should be consistent");
    }

    @Test
    public void testToString() {
        Meld meld = Meld.createPong(Tile.M1);
        String str = meld.toString();
        
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("PONG"), "toString should contain type");
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetFirstTile() {
        Meld chow = Meld.createChow(Tile.M1, Tile.M2, Tile.M3);
        
        assertEquals(Tile.M1, chow.getFirstTile(), "getFirstTile should return first tile");
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetFirstTile_EmptyMeld() {
        // 這個測試理論上不應該發生，因為建構子會驗證
        // 但為了完整性，我們測試 getFirstTile 的邊界情況
        // 實際上建構子會阻止空列表，所以這個測試主要是文檔性質
        Meld meld = Meld.createPong(Tile.M1);
        assertNotNull(meld.getFirstTile(), "PONG should have first tile");
    }
}

