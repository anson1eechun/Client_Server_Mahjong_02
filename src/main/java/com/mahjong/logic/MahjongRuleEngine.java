package com.mahjong.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Core game engine.
 * Manages the wall, dealing, and turn flow.
 */
public class MahjongRuleEngine {
    private final List<Tile> wall;
    private final Random random;

    // Dependency Injection for Random to allow test seeding
    public MahjongRuleEngine(Random random) {
        this.random = random;
        this.wall = new ArrayList<>();
        initializeWall();
    }

    private void initializeWall() {
        wall.clear();
        for (Tile tile : Tile.values()) {
            // Taiwan Mahjong: 144 tiles (4 of each)
            // Flowers are ignored per scope limitation
            for (int i = 0; i < 4; i++) {
                wall.add(tile);
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(wall, random);
    }

    public Tile drawTile() {
        if (wall.isEmpty()) {
            return null; // Wall exhausted (Draw game)
        }
        return wall.remove(0);
    }

    public int getRemainingTiles() {
        return wall.size();
    }
    
    // Helper to deal initial hands (16 tiles each)
    public void dealInitialHands(List<PlayerHand> players) {
        for (int i = 0; i < 16; i++) {
            for (PlayerHand player : players) {
                Tile t = drawTile();
                if (t != null) {
                    player.addTile(t);
                }
            }
        }
        players.forEach(PlayerHand::sort);
    }
}
