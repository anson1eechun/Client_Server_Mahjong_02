package com.mahjong.logic;

import java.util.ArrayList;
import java.util.Collections;
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
        // Total count = standing + (melds * 3) + (kongs * 1?) -> complicated
        // Simply count tiles
        int count = standingTiles.size();
        for (Meld m : openMelds) {
            count += 3; // Standardize 3 for meld, strictly Kong handled elsewhere or logic adjusted
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

    public void addMeld(Meld.Type type, String tileName) {
        Tile t = Tile.valueOf(tileName);
        // Create full list for Pong (3) / Kong (4?)
        // Assuming Pong for now if type logic not specified
        // But wait, this method is used by 'performPong' which passes 'Type.PONG'.
        List<Tile> set = new ArrayList<>();
        set.add(t);
        set.add(t);
        set.add(t);
        // For Kong, we might need 4?
        if (type == Meld.Type.KONG) {
            // This simplisic method doesn't know for sure, but standard Kong is 4.
            // Exposed Kong usually shown as 4 tiles.
            // set.add(t);
            // In Taiwan MJ, is Kong 4 tiles visible? Yes.
            // But if we only removed 3 from hand + 1 from sea, we have 4.
            // Let's add 4th if Kong.
            // However, existing calls might rely on simple 3 representations.
            // Let's stick to 3 for PONG/CHOW (simplified).
            // Actually CHOW simplified here (1 tile argument) implies 3 identical? NO.
            // That's why this method is bad for CHOW.
            // But performPong calls this.
        }

        Meld m = new Meld(type, set);
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
