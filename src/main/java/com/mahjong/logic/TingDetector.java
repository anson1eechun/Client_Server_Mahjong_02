package com.mahjong.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * 聽牌檢測器
 * 用於檢測玩家是否聽牌，以及聽哪些牌
 */
public class TingDetector {
    
    private final WinStrategy winStrategy;
    
    public TingDetector() {
        this.winStrategy = new WinStrategy();
    }
    
    /**
     * 檢測當前手牌是否聽牌
     * @param hand 玩家手牌
     * @return 聽牌結果，包含是否聽牌和聽哪些牌
     */
    public TingResult detectTing(PlayerHand hand) {
        List<Tile> tingTiles = new ArrayList<>();
        
        // 檢查手牌總數是否正確（13 張或 14 張）
        int totalTiles = hand.getConnectionCount();
        if (totalTiles != 13 && totalTiles != 14) {
            return new TingResult(false, tingTiles);
        }
        
        // 嘗試每一種可能的牌，看是否能胡牌
        for (Tile tile : Tile.values()) {
            // 跳過不存在的牌（如果 Tile enum 中有特殊值）
            if (tile == null) continue;
            
            // 創建臨時手牌，加入這張牌
            PlayerHand testHand = cloneHand(hand);
            testHand.addTile(tile);
            
            // 檢查是否為胡牌
            if (winStrategy.isWinningHand(testHand)) {
                tingTiles.add(tile);
            }
        }
        
        return new TingResult(!tingTiles.isEmpty(), tingTiles);
    }
    
    /**
     * 檢查手牌是否已經可以胡牌（14 張或 17 張）
     * @param hand 玩家手牌
     * @return true 如果可以胡牌
     */
    public boolean isWinningHand(PlayerHand hand) {
        return winStrategy.isWinningHand(hand);
    }
    
    /**
     * 檢查手牌在加入指定牌後是否可以胡牌
     * @param hand 玩家手牌
     * @param tile 要加入的牌
     * @return true 如果可以胡牌
     */
    public boolean canWinWithTile(PlayerHand hand, Tile tile) {
        PlayerHand testHand = cloneHand(hand);
        testHand.addTile(tile);
        return winStrategy.isWinningHand(testHand);
    }
    
    /**
     * 克隆手牌（避免修改原始手牌）
     */
    private PlayerHand cloneHand(PlayerHand original) {
        PlayerHand clone = new PlayerHand();
        // 複製所有手牌
        for (Tile tile : original.getStandingTiles()) {
            clone.addTile(tile);
        }
        // 複製所有明牌（Meld）
        for (Meld meld : original.getOpenMelds()) {
            clone.addMeld(meld);
        }
        return clone;
    }
    
    /**
     * 聽牌結果類別
     */
    public static class TingResult {
        private final boolean isTing;
        private final List<Tile> tingTiles;
        
        public TingResult(boolean isTing, List<Tile> tingTiles) {
            this.isTing = isTing;
            this.tingTiles = new ArrayList<>(tingTiles);
        }
        
        public boolean isTing() {
            return isTing;
        }
        
        public List<Tile> getTingTiles() {
            return new ArrayList<>(tingTiles);
        }
        
        public int getTingCount() {
            return tingTiles.size();
        }
        
        @Override
        public String toString() {
            if (!isTing) {
                return "Not Ting";
            }
            StringBuilder sb = new StringBuilder("Ting: ");
            for (int i = 0; i < tingTiles.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(tingTiles.get(i).toString());
            }
            return sb.toString();
        }
    }
}

