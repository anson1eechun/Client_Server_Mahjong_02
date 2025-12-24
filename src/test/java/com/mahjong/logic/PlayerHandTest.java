package com.mahjong.logic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * 測試 PlayerHand 類別
 * 重點測試：添加/移除牌、面子管理、排序功能
 */
public class PlayerHandTest {
    private PlayerHand hand;

    @BeforeEach
    public void setup() {
        hand = new PlayerHand();
    }

    @Test
    public void testAddTile_AutoSort() {
        // 添加不按順序的牌
        hand.addTile(Tile.M5);
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M9);
        hand.addTile(Tile.M1);

        List<Tile> tiles = hand.getStandingTiles();
        
        // 驗證自動排序
        assertEquals(Tile.M1, tiles.get(0), "First tile should be M1");
        assertEquals(Tile.M2, tiles.get(1), "Second tile should be M2");
        assertEquals(Tile.M5, tiles.get(2), "Third tile should be M5");
        assertEquals(Tile.M9, tiles.get(3), "Fourth tile should be M9");
    }

    @Test
    public void testAddTile_NullTile() {
        int initialCount = hand.getTileCount();
        hand.addTile(null);
        
        // null 不應該被添加
        assertEquals(initialCount, hand.getTileCount(), "Null tile should not be added");
    }

    @Test
    public void testRemoveTile_ByObject() {
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M3);

        assertTrue(hand.removeTile(Tile.M2), "Should successfully remove M2");
        assertEquals(2, hand.getTileCount(), "Should have 2 tiles remaining");
        assertFalse(hand.getStandingTiles().contains(Tile.M2), "M2 should not be in hand");
    }

    @Test
    public void testRemoveTile_ByString() {
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M3);

        assertTrue(hand.removeTile("M2"), "Should successfully remove M2 by string");
        assertEquals(2, hand.getTileCount(), "Should have 2 tiles remaining");
    }

    @Test
    public void testRemoveTile_NonExistent() {
        hand.addTile(Tile.M1);
        
        assertFalse(hand.removeTile(Tile.M2), "Should return false for non-existent tile");
        assertFalse(hand.removeTile("M2"), "Should return false for non-existent tile by string");
        assertEquals(1, hand.getTileCount(), "Tile count should remain 1");
    }

    @Test
    public void testRemoveTile_MultipleSameTiles() {
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);

        assertTrue(hand.removeTile(Tile.M1), "Should remove first M1");
        assertEquals(2, hand.getTileCount(), "Should have 2 M1 tiles remaining");
        
        assertTrue(hand.removeTile("M1"), "Should remove second M1");
        assertEquals(1, hand.getTileCount(), "Should have 1 M1 tile remaining");
    }

    @Test
    public void testAddMeld() {
        Meld pong = Meld.createPong(Tile.M1);
        hand.addMeld(pong);

        assertEquals(1, hand.getMeldCount(), "Should have 1 meld");
        List<Meld> melds = hand.getOpenMelds();
        assertEquals(Meld.Type.PONG, melds.get(0).getType(), "Meld type should be PONG");
    }

    @Test
    public void testGetConnectionCount_WithMelds() {
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M3);
        
        Meld pong = Meld.createPong(Tile.M4);
        hand.addMeld(pong);

        // 3 standing tiles + 3 tiles in pong = 6
        assertEquals(6, hand.getConnectionCount(), "Connection count should include melds");
    }

    @Test
    public void testGetConnectionCount_WithKong() {
        hand.addTile(Tile.M1);
        Meld kong = Meld.createKong(Tile.M2);
        hand.addMeld(kong);

        // 1 standing tile + 4 tiles in kong = 5
        assertEquals(5, hand.getConnectionCount(), "Kong should count as 4 tiles");
    }

    @Test
    public void testGetTilesStr() {
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        hand.addTile(Tile.P3);

        List<String> tileStrings = hand.getTilesStr();
        assertEquals(3, tileStrings.size(), "Should have 3 tile strings");
        assertTrue(tileStrings.contains("M1"), "Should contain M1");
        assertTrue(tileStrings.contains("M2"), "Should contain M2");
        assertTrue(tileStrings.contains("P3"), "Should contain P3");
    }

    @Test
    public void testGetMeldsStr() {
        Meld pong = Meld.createPong(Tile.M1);
        Meld chow = Meld.createChow(Tile.M2, Tile.M3, Tile.M4);
        hand.addMeld(pong);
        hand.addMeld(chow);

        List<String> meldStrings = hand.getMeldsStr();
        // Pong has 3 tiles, Chow has 3 tiles = 6 total
        assertEquals(6, meldStrings.size(), "Should have 6 meld tile strings");
    }

    @Test
    public void testGetStandingTiles_ReturnsCopy() {
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);

        List<Tile> tiles1 = hand.getStandingTiles();
        List<Tile> tiles2 = hand.getStandingTiles();

        // 修改返回的列表不應該影響原始列表
        tiles1.clear();
        assertEquals(2, hand.getTileCount(), "Original hand should not be modified");
        assertEquals(2, tiles2.size(), "Second copy should still have tiles");
    }

    @Test
    public void testGetOpenMelds_ReturnsCopy() {
        Meld pong = Meld.createPong(Tile.M1);
        hand.addMeld(pong);

        List<Meld> melds1 = hand.getOpenMelds();
        List<Meld> melds2 = hand.getOpenMelds();

        // 修改返回的列表不應該影響原始列表
        melds1.clear();
        assertEquals(1, hand.getMeldCount(), "Original hand should not be modified");
        assertEquals(1, melds2.size(), "Second copy should still have meld");
    }

    @Test
    public void testSort_MultipleSuits() {
        hand.addTile(Tile.S9);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.P5);
        hand.addTile(Tile.M3);
        hand.addTile(Tile.EAST);

        List<Tile> tiles = hand.getStandingTiles();
        
        // 應該按花色排序：萬、筒、條、風、龍
        assertEquals(Tile.M1, tiles.get(0));
        assertEquals(Tile.M3, tiles.get(1));
        assertEquals(Tile.P5, tiles.get(2));
        assertEquals(Tile.S9, tiles.get(3));
        assertEquals(Tile.EAST, tiles.get(4));
    }

    @Test
    public void testGetTileCount_EmptyHand() {
        assertEquals(0, hand.getTileCount(), "Empty hand should have 0 tiles");
    }

    @Test
    public void testGetMeldCount_NoMelds() {
        assertEquals(0, hand.getMeldCount(), "Hand with no melds should return 0");
    }
}

