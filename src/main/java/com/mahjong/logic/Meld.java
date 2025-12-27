package com.mahjong.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a set of tiles (Meld/Open Set).
 * Can be a Chow (Chi), Pong (Pon), or Kong (Kan).
 * 
 * 重構說明：
 * - 移除舊建構子 Meld(Type, Tile)，統一使用 Meld(Type, List<Tile>)
 * - 提供便利方法 createPong(), createKong(), createChow() 簡化創建
 */
public class Meld {
    public enum Type {
        CHOW, PONG, KONG, EYES
    }

    private final Type type;
    private final List<Tile> tiles;
    private final boolean concealed; // ✅ P1-1: 是否暗的（用於暗槓）

    /**
     * 主要建構子：使用完整的牌列表
     * @param type 面子類型
     * @param tiles 牌的列表（必須符合類型要求：CHOW=3張, PONG=3張, KONG=4張）
     */
    public Meld(Type type, List<Tile> tiles) {
        this(type, tiles, false); // 預設為明的
    }

    /**
     * 完整建構子：使用完整的牌列表和是否暗的標記
     * @param type 面子類型
     * @param tiles 牌的列表（必須符合類型要求：CHOW=3張, PONG=3張, KONG=4張）
     * @param concealed 是否暗的（用於暗槓）
     */
    public Meld(Type type, List<Tile> tiles, boolean concealed) {
        if (tiles == null || tiles.isEmpty()) {
            throw new IllegalArgumentException("Tiles list cannot be null or empty");
        }
        this.type = type;
        this.tiles = new ArrayList<>(tiles);
        this.concealed = concealed;
        
        // 驗證牌數是否符合類型
        validateTileCount();
    }

    /**
     * 驗證牌數是否符合面子類型要求
     */
    private void validateTileCount() {
        int count = tiles.size();
        switch (type) {
            case CHOW:
                if (count != 3) {
                    throw new IllegalArgumentException("CHOW must have exactly 3 tiles, got " + count);
                }
                break;
            case PONG:
                if (count != 3) {
                    throw new IllegalArgumentException("PONG must have exactly 3 tiles, got " + count);
                }
                break;
            case KONG:
                if (count != 4) {
                    throw new IllegalArgumentException("KONG must have exactly 4 tiles, got " + count);
                }
                break;
            case EYES:
                if (count != 2) {
                    throw new IllegalArgumentException("EYES must have exactly 2 tiles, got " + count);
                }
                break;
        }
    }

    /**
     * 便利方法：創建碰牌（Pong）
     * @param tile 要碰的牌（會自動複製 3 次）
     * @return 新的 Meld 物件
     */
    public static Meld createPong(Tile tile) {
        return new Meld(Type.PONG, Arrays.asList(tile, tile, tile));
    }

    /**
     * 便利方法：創建槓牌（Kong - 明槓）
     * @param tile 要槓的牌（會自動複製 4 次）
     * @return 新的 Meld 物件
     */
    public static Meld createKong(Tile tile) {
        return new Meld(Type.KONG, Arrays.asList(tile, tile, tile, tile), false);
    }

    /**
     * ✅ P1-1: 便利方法：創建暗槓（Concealed Kong）
     * @param tile 要暗槓的牌（會自動複製 4 次）
     * @return 新的 Meld 物件（concealed = true）
     */
    public static Meld createConcealedKong(Tile tile) {
        return new Meld(Type.KONG, Arrays.asList(tile, tile, tile, tile), true);
    }

    /**
     * 便利方法：創建吃牌（Chow）
     * @param t1 第一張牌
     * @param t2 第二張牌
     * @param t3 第三張牌
     * @return 新的 Meld 物件
     */
    public static Meld createChow(Tile t1, Tile t2, Tile t3) {
        return new Meld(Type.CHOW, Arrays.asList(t1, t2, t3));
    }

    /**
     * 便利方法：創建對眼（Eyes/Pair）
     * @param tile 對眼的牌（會自動複製 2 次）
     * @return 新的 Meld 物件
     */
    public static Meld createEyes(Tile tile) {
        return new Meld(Type.EYES, Arrays.asList(tile, tile));
    }

    public Type getType() {
        return type;
    }

    /**
     * 獲取第一張牌（向後兼容方法）
     * @deprecated 建議使用 getTiles() 獲取完整列表
     */
    @Deprecated
    public Tile getFirstTile() {
        return tiles.isEmpty() ? null : tiles.get(0);
    }

    /**
     * 獲取所有牌的列表（返回副本以避免外部修改）
     */
    public List<Tile> getTiles() {
        return new ArrayList<>(tiles);
    }

    /**
     * 獲取牌的數量
     */
    public int getTileCount() {
        return tiles.size();
    }

    /**
     * ✅ P1-1: 獲取是否為暗的（用於暗槓）
     */
    public boolean isConcealed() {
        return concealed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Meld meld = (Meld) o;
        return type == meld.type && Objects.equals(tiles, meld.tiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, tiles);
    }

    @Override
    public String toString() {
        return type + ":" + tiles;
    }
}
