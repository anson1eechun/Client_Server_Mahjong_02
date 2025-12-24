package com.mahjong.logic;

import java.util.List;

/**
 * Calculates the score (Tai/Fan) for a winning hand.
 * Supports basic Taiwan 16-tile patterns.
 */
public class ScoringCalculator {

    /**
     * calculates the total Tai (Fan) for the hand.
     * Assumes the hand is already a winning hand.
     */
    public int calculateTai(PlayerHand hand, boolean isSelfDraw, Tile roundWind, Tile seatWind) {
        int tai = 0;
        
        // Base Tai for Self Draw
        if (isSelfDraw) {
            tai += 1;
        }

        // Check for Dragon Pongs (Red, Green, White) - 1 Tai each
        if (hasPongOrKong(hand, Tile.RED)) tai++;
        if (hasPongOrKong(hand, Tile.GREEN)) tai++;
        if (hasPongOrKong(hand, Tile.WHITE)) tai++;

        // Check for Round Wind and Seat Wind
        if (roundWind != null && hasPongOrKong(hand, roundWind)) tai++;
        if (seatWind != null && hasPongOrKong(hand, seatWind)) tai++;

        // Check for Flush (Pure or Mixed)
        if (isFullFlush(hand)) {
            tai += 8; // Qing Yi Se
        } else if (isHalfFlush(hand)) {
            tai += 4; // Hun Yi Se
        }

        // Check for All Pongs (Pong Pong Hu)
        if (isAllPongs(hand)) {
            tai += 4;
        }

        return tai;
    }

    private boolean hasPongOrKong(PlayerHand hand, Tile target) {
        // Check open melds
        for (Meld meld : hand.getOpenMelds()) {
            if ((meld.getType() == Meld.Type.PONG || meld.getType() == Meld.Type.KONG)) {
                // 檢查 Meld 中是否包含目標牌
                if (meld.getTiles().contains(target)) {
                    return true;
                }
            }
        }
        // Check standing tiles (hidden pongs/kongs are hard to detect without full decomposition)
        // For simplicity in this simplified rule engine, we might check if count >= 3
        int count = 0;
        for (Tile t : hand.getStandingTiles()) {
            if (t == target) count++;
        }
        return count >= 3;
    }

    private boolean isFullFlush(PlayerHand hand) {
        Tile.Suit firstSuit = null;
        
        // Check standing
        for (Tile t : hand.getStandingTiles()) {
            if (t.getSuit() == Tile.Suit.DRAGON || t.getSuit() == Tile.Suit.WIND) return false;
            if (firstSuit == null) firstSuit = t.getSuit();
            else if (firstSuit != t.getSuit()) return false;
        }
        
        // Check melds
        for (Meld m : hand.getOpenMelds()) {
            // 對於清一色檢查，我們檢查 Meld 中所有牌的花色
            for (Tile t : m.getTiles()) {
                if (t.getSuit() == Tile.Suit.DRAGON || t.getSuit() == Tile.Suit.WIND) return false;
                if (firstSuit == null) firstSuit = t.getSuit();
                else if (firstSuit != t.getSuit()) return false;
            }
        }
        
        return true;
    }

    private boolean isHalfFlush(PlayerHand hand) {
        Tile.Suit suit = null;
        boolean hasHonors = false;

        java.util.List<Tile> allTiles = new java.util.ArrayList<>(hand.getStandingTiles());
        for (Meld m : hand.getOpenMelds()) {
            // 添加 Meld 中的所有牌（對於半清一色檢查，我們需要所有牌）
            allTiles.addAll(m.getTiles());
        }

        for (Tile t : allTiles) {
            if (t.getSuit() == Tile.Suit.DRAGON || t.getSuit() == Tile.Suit.WIND) {
                hasHonors = true;
            } else {
                if (suit == null) suit = t.getSuit();
                else if (suit != t.getSuit()) return false; // Mixed numeric suits
            }
        }
        return hasHonors && suit != null;
    }

    private boolean isAllPongs(PlayerHand hand) {
        // Check Open Melds: Must not contain Chow
        for (Meld m : hand.getOpenMelds()) {
            if (m.getType() == Meld.Type.CHOW) return false;
        }
        
        // Check Standing Tiles: Must be decomposable into Triplet(3) and Pair(2).
        int[] counts = new int[34];
        for (Tile t : hand.getStandingTiles()) {
            counts[getActionIndex(t)]++;
        }
        
        boolean foundPair = false;
        for (int c : counts) {
            if (c == 0) continue;
            
            if (c == 2) {
                if (foundPair) return false; // Two pairs -> not PongPongHu (unless 7 pairs, but that is disjoint)
                foundPair = true;
            } else if (c == 3) {
                // OK
            } else if (c == 4) {
                 // 4 tiles in hand? Could be AAA + A? No.
                 // Could be pair + pair? yes.
                 // But for PongPongHu, usually we declare Kong if we have 4.
                 // If 4 in hand, it's 7 pairs or Kong.
                 // Let's conservative fail or assume Kong?
                 // Simple logic: strictly 2 or 3.
                 return false; 
            } else {
                return false; // 1, or > 4
            }
        }
        
        return foundPair;
    }

    private int getActionIndex(Tile tile) {
        switch (tile.getSuit()) {
            case MAN: return tile.getRank() - 1;       // 0-8
            case PIN: return 9 + (tile.getRank() - 1); // 9-17
            case SOU: return 18 + (tile.getRank() - 1);// 18-26
            case WIND: return 27 + (tile.getRank() - 1);// 27-30
            case DRAGON: return 31 + (tile.getRank() - 1);// 31-33
            default: throw new IllegalArgumentException("Unknown tile suit");
        }
    }
}
