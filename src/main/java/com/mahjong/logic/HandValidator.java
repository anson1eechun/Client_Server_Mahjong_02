package com.mahjong.logic;

import java.util.List;

/**
 * Validates player actions like Chow, Pong, Kong.
 */
public class HandValidator {

    /**
     * Checks if a player can Pong (Pon) a discarded tile.
     * Requirement: Player has at least 2 consecutive copies of the target tile.
     */
    public boolean canPong(PlayerHand hand, Tile discard) {
        int count = 0;
        for (Tile t : hand.getStandingTiles()) {
            if (t == discard) {
                count++;
            }
        }
        return count >= 2;
    }

    /**
     * Checks if a player can Kong (Kan) a discarded tile.
     * Requirement: Player has 3 consecutive copies of the target tile.
     */
    public boolean canKong(PlayerHand hand, Tile discard) {
        int count = 0;
        for (Tile t : hand.getStandingTiles()) {
            if (t == discard) {
                count++;
            }
        }
        return count >= 3;
    }

    /**
     * Checks if a player can Chow (Chi) a discarded tile.
     * Chow is only allowed from the player directly to the left (handled by turn
     * logic, not here).
     * Here we only check if the hand has the tiles to complete a sequence.
     */
    public boolean canChow(PlayerHand hand, Tile discard) {
        return !getChowCombinations(hand, discard).isEmpty();
    }

    /**
     * Returns a list of valid Chow combinations.
     * Each combination is represented as string like "M1,M2" (the 2 tiles from
     * hand).
     */
    public java.util.List<String> getChowCombinations(PlayerHand hand, Tile discard) {
        java.util.List<String> options = new java.util.ArrayList<>();

        if (!discard.isNumberTile()) {
            return options;
        }

        List<Tile> tiles = hand.getStandingTiles();
        Tile.Suit suit = discard.getSuit();
        int rank = discard.getRank();

        // 1. Check (r-2, r-1)
        if (contains(tiles, suit, rank - 2) && contains(tiles, suit, rank - 1)) {
            String c1 = tileStr(suit, rank - 2);
            String c2 = tileStr(suit, rank - 1);
            options.add("CHOW " + c1 + "," + c2);
        }

        // 2. Check (r-1, r+1)
        if (contains(tiles, suit, rank - 1) && contains(tiles, suit, rank + 1)) {
            String c1 = tileStr(suit, rank - 1);
            String c2 = tileStr(suit, rank + 1);
            options.add("CHOW " + c1 + "," + c2);
        }

        // 3. Check (r+1, r+2)
        if (contains(tiles, suit, rank + 1) && contains(tiles, suit, rank + 2)) {
            String c1 = tileStr(suit, rank + 1);
            String c2 = tileStr(suit, rank + 2);
            options.add("CHOW " + c1 + "," + c2);
        }

        return options;
    }

    // Helper to constructing tile string
    private String tileStr(Tile.Suit suit, int rank) {
        // Find the specific Tile enum name?
        // Iterate Tile.values() or guess?
        // Tile enum names are like M1, P2.
        // We can reconstruct it.
        // Or better, let's just find the tile object from hand and use .toString()?
        // Actually, we need strictly the string representation.
        // M1 is MAN 1.
        String prefix = "";
        switch (suit) {
            case MAN:
                prefix = "M";
                break;
            case PIN:
                prefix = "P";
                break;
            case SOU:
                prefix = "S";
                break;
            default:
                return "";
        }
        return prefix + rank;
    }

    private final WinStrategy winStrategy = new WinStrategy();

    /**
     * Checks if a player can Hu (Win) on a discarded tile.
     * Uses clone to avoid modifying the original hand.
     */
    public boolean canHu(PlayerHand hand, Tile discard) {
        // Clone the hand to avoid modifying the original
        PlayerHand tempHand = cloneHand(hand);
        tempHand.addTile(discard);
        return winStrategy.isWinningHand(tempHand);
    }
    
    /**
     * Clone a PlayerHand for safe testing without modifying the original.
     */
    private PlayerHand cloneHand(PlayerHand original) {
        PlayerHand clone = new PlayerHand();
        // Copy all standing tiles
        for (Tile tile : original.getStandingTiles()) {
            clone.addTile(tile);
        }
        // Copy all open melds
        for (Meld meld : original.getOpenMelds()) {
            clone.addMeld(meld);
        }
        return clone;
    }

    // Helper to check containment
    private boolean contains(List<Tile> tiles, Tile.Suit suit, int rank) {
        if (rank < 1 || rank > 9)
            return false;
        for (Tile t : tiles) {
            if (t.getSuit() == suit && t.getRank() == rank) {
                return true;
            }
        }
        return false;
    }
}
