package com.mahjong.logic;

import java.util.Objects;

/**
 * Represents a set of tiles (Meld/Open Set).
 * Can be a Chow (Chi), Pong (Pon), or Kong (Kan).
 */
public class Meld {
    public enum Type {
        CHOW, PONG, KONG, EYES
    }

    private final Type type;
    private final java.util.List<Tile> tiles;

    public Meld(Type type, java.util.List<Tile> tiles) {
        this.type = type;
        this.tiles = new java.util.ArrayList<>(tiles);
    }

    // Backward compatibility constructor
    public Meld(Type type, Tile firstTile) {
        this.type = type;
        this.tiles = new java.util.ArrayList<>();
        this.tiles.add(firstTile);
        // For Chow/Pong/Kong we probably meant 3 of these?
        // But for backward compatibility let's just add one,
        // however getFirstTile will return it.
    }

    public Type getType() {
        return type;
    }

    public Tile getFirstTile() {
        return tiles.isEmpty() ? null : tiles.get(0);
    }

    public java.util.List<Tile> getTiles() {
        return new java.util.ArrayList<>(tiles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Meld meld = (Meld) o;
        return type == meld.type && java.util.Objects.equals(tiles, meld.tiles);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, tiles);
    }

    @Override
    public String toString() {
        return type + ":" + tiles;
    }
}
