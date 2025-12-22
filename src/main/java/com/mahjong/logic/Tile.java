package com.mahjong.logic;

/**
 * Represents a Mahjong tile.
 * Includes Suit and Rank.
 */
public enum Tile {
    // 萬子 (Characters)
    M1(Suit.MAN, 1), M2(Suit.MAN, 2), M3(Suit.MAN, 3), M4(Suit.MAN, 4), M5(Suit.MAN, 5),
    M6(Suit.MAN, 6), M7(Suit.MAN, 7), M8(Suit.MAN, 8), M9(Suit.MAN, 9),

    // 筒子 (Dots/Pin)
    P1(Suit.PIN, 1), P2(Suit.PIN, 2), P3(Suit.PIN, 3), P4(Suit.PIN, 4), P5(Suit.PIN, 5),
    P6(Suit.PIN, 6), P7(Suit.PIN, 7), P8(Suit.PIN, 8), P9(Suit.PIN, 9),

    // 索子 (Bamboo/Sou)
    S1(Suit.SOU, 1), S2(Suit.SOU, 2), S3(Suit.SOU, 3), S4(Suit.SOU, 4), S5(Suit.SOU, 5),
    S6(Suit.SOU, 6), S7(Suit.SOU, 7), S8(Suit.SOU, 8), S9(Suit.SOU, 9),

    // 字牌 (Honors)
    EAST(Suit.WIND, 1), SOUTH(Suit.WIND, 2), WEST(Suit.WIND, 3), NORTH(Suit.WIND, 4),
    RED(Suit.DRAGON, 1), GREEN(Suit.DRAGON, 2), WHITE(Suit.DRAGON, 3);

    private final Suit suit;
    private final int rank;

    Tile(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public boolean isNumberTile() {
        return suit == Suit.MAN || suit == Suit.PIN || suit == Suit.SOU;
    }

    public enum Suit {
        MAN, PIN, SOU, WIND, DRAGON
    }
}
