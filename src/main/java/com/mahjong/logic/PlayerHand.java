package com.mahjong.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the tiles in a player's hand.
 * Supports adding/removing tiles and managing exposed melds.
 */
public class PlayerHand {
    private final List<Tile> standingTiles; // Hand tiles (hidden)
    private final List<Meld> openMelds; // Exposed melds (Chow/Pong/Kong)

    public PlayerHand() {
        this.standingTiles = new ArrayList<>();
        this.openMelds = new ArrayList<>();
    }

    public void addTile(Tile tile) {
        if (tile == null)
            return;
        standingTiles.add(tile);
        sort();
    }

    public boolean removeTile(Tile tile) {
        return standingTiles.remove(tile);
    }

    public void addMeld(Meld meld) {
        openMelds.add(meld);
    }

    public List<Tile> getStandingTiles() {
        return new ArrayList<>(standingTiles); // Return copy
    }

    public List<Meld> getOpenMelds() {
        return new ArrayList<>(openMelds); // Return copy
    }

    public int getConnectionCount() {
        // Taiwan Mahjong: 16 tiles standard hand size
        // Total count = standing + melds
        int count = standingTiles.size();
        for (Meld meld : openMelds) {
            // 使用 Meld 的實際牌數
            count += meld.getTileCount();
        }
        return count;
    }

    public void sort() {
        standingTiles.sort(Comparator.comparing(Tile::getSuit).thenComparingInt(Tile::getRank));
    }

    public boolean removeTile(String tileName) {
        // Use index to avoid ConcurrentModificationException if iterator logic fails
        for (int i = 0; i < standingTiles.size(); i++) {
            if (standingTiles.get(i).toString().equals(tileName)) {
                standingTiles.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<String> getTilesStr() {
        List<String> list = new ArrayList<>();
        for (Tile t : standingTiles)
            list.add(t.toString());
        return list;
    }

    public int getTileCount() {
        return standingTiles.size();
    }

    /**
     * 添加面子（使用便利方法）
     * @deprecated 建議使用 addMeld(Meld) 方法，此方法僅用於向後兼容
     */
    @Deprecated
    public void addMeld(Meld.Type type, String tileName) {
        Tile t = Tile.valueOf(tileName);
        Meld m;
        
        switch (type) {
            case PONG:
                m = Meld.createPong(t);
                break;
            case KONG:
                m = Meld.createKong(t);
                break;
            case CHOW:
                // CHOW 不能只用一張牌創建，這是一個設計缺陷
                // 但為了向後兼容，我們創建一個臨時的（不正確的）Meld
                // 調用者應該使用 addMeld(Meld) 並傳入完整的 3 張牌
                throw new IllegalArgumentException(
                    "Cannot create CHOW with single tile. Use addMeld(Meld) with complete chow tiles.");
            case EYES:
                m = Meld.createEyes(t);
                break;
            default:
                throw new IllegalArgumentException("Unknown meld type: " + type);
        }
        
        openMelds.add(m);
    }

    public List<String> getMeldsStr() {
        List<String> list = new ArrayList<>();
        for (Meld m : openMelds) {
            // Updated to use actual tiles in logic
            List<Tile> tiles = m.getTiles();
            if (tiles.size() == 1 && (m.getType() == Meld.Type.PONG || m.getType() == Meld.Type.KONG)) {
                // Fallback for simplified melds (if any exist)
                // Pong = 3 copies, Kong = 4 copies ?
                // Assuming Pong = 3.
                Tile t = tiles.get(0);
                list.add(t.toString());
                list.add(t.toString());
                list.add(t.toString());
            } else if (tiles.size() == 1 && m.getType() == Meld.Type.CHOW) {
                // If it's a simplification, we might fail to show correct sequence.
                // But for now just show what we have.
                list.add(tiles.get(0).toString());
            } else {
                for (Tile t : tiles) {
                    list.add(t.toString());
                }
            }
        }
        return list;
    }

    public int getMeldCount() {
        return openMelds.size();
    }
}
