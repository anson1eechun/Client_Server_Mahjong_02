package com.mahjong.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * 修復版：Strategy to determine if a hand is a "Winning Hand" (Hu).
 * Uses recursion/backtracking to check for standard format: m*AAA + n*ABC + DD.
 * 
 * 修復內容：
 * 1. 順子判定邏輯修正（7,8,9 萬可以組成順子）
 * 2. 跨花色邊界檢查
 * 3. 增加詳細註解
 */
public class WinStrategy {

    /**
     * Checks if the hand is a winning hand.
     * 
     * @param hand The player's hand (including the 17th tile for dealer, or 14 for
     *             others)
     * @return true if winning
     */
    public boolean isWinningHand(PlayerHand hand) {
        // ✅ 修復：考慮已經組成的 meld（碰/吃/槓）
        // 標準麻將胡牌：4 個面子（每個 3 張） + 1 對眼（2 張） = 14 張
        // 如果玩家已經有 K 個 meld，那麼 standing tiles 需要組成 (4-K) 個面子 + 1 對眼
        
        List<Tile> tiles = hand.getStandingTiles();
        List<Meld> melds = hand.getOpenMelds();
        
        // 計算 meld 佔用的牌數
        int meldTileCount = 0;
        for (Meld meld : melds) {
            meldTileCount += meld.getTileCount(); // 碰/吃=3, 槓=4
        }
        
        // 總牌數 = standing tiles + meld tiles，應該是 14 或 17（莊家）
        int totalTileCount = tiles.size() + meldTileCount;
        if (totalTileCount != 14 && totalTileCount != 17) {
            return false;
        }
        
        // standing tiles 的數量
        int standingTileCount = tiles.size();
        
        // standing tiles 需要組成 (4 - meldSetsCount) 個面子 + 1 對眼
        // 所以 standing tiles 的數量應該是：(4 - meldSetsCount) * 3 + 2 = 14 - meldSetsCount * 3
        // 這等價於：(standingTileCount - 2) % 3 == 0 且 standingTileCount == 14 - meldTileCount
        // 基本檢查：standing tiles 數量應該滿足 (Count - 2) % 3 == 0
        if ((standingTileCount - 2) % 3 != 0) {
            return false;
        }

        // Convert to frequency array for efficient backtracking
        int[] counts = new int[34];
        for (Tile t : tiles) {
            counts[getTileIndex(t)]++;
        }

        // Try all possible pairs (Eyes)
        for (int i = 0; i < 34; i++) {
            if (counts[i] >= 2) {
                // Try using index i as the pair
                counts[i] -= 2;

                // Check if remaining tiles form valid sets
                int setsNeeded = (standingTileCount - 2) / 3;
                if (canFormSets(counts, setsNeeded)) {
                    counts[i] += 2; // Restore before returning
                    return true;
                }

                // Backtrack
                counts[i] += 2;
            }
        }

        return false;
    }

    /**
     * 遞迴檢查是否能組成指定數量的面子（順子或刻子）
     * 
     * @param counts     牌的頻率陣列
     * @param setsNeeded 還需要組成的面子數量
     * @return true if can form required sets
     */
    private boolean canFormSets(int[] counts, int setsNeeded) {
        if (setsNeeded == 0) {
            // 檢查是否所有牌都已使用完
            for (int count : counts) {
                if (count > 0)
                    return false;
            }
            return true;
        }

        // Find the first index with available tiles
        int firstIndex = -1;
        for (int i = 0; i < 34; i++) {
            if (counts[i] > 0) {
                firstIndex = i;
                break;
            }
        }

        if (firstIndex == -1) {
            // No tiles left but still need sets - invalid
            return setsNeeded == 0;
        }

        // Strategy 1: Try to form a Triplet (AAA/Pong)
        if (counts[firstIndex] >= 3) {
            counts[firstIndex] -= 3;
            if (canFormSets(counts, setsNeeded - 1)) {
                return true;
            }
            counts[firstIndex] += 3; // Backtrack
        }

        // Strategy 2: Try to form a Sequence (ABC/Chow)
        // Only possible for number tiles (indices 0-26)
        if (firstIndex < 27) {
            // Check if we can form a sequence starting at firstIndex
            // 修復：移除 suitIndex <= 6 的限制
            if (canFormSequence(counts, firstIndex)) {
                counts[firstIndex]--;
                counts[firstIndex + 1]--;
                counts[firstIndex + 2]--;

                if (canFormSets(counts, setsNeeded - 1)) {
                    return true;
                }

                // Backtrack
                counts[firstIndex]++;
                counts[firstIndex + 1]++;
                counts[firstIndex + 2]++;
            }
        }

        return false;
    }

    /**
     * 檢查是否能從指定位置組成順子
     * 修復：正確處理 7,8,9 等邊界情況
     */
    private boolean canFormSequence(int[] counts, int startIndex) {
        // 必須是數字牌（萬筒條）
        if (startIndex >= 27)
            return false;

        // 計算花色和牌面值
        int suit = startIndex / 9; // 0=萬, 1=筒, 2=條
        int rank = startIndex % 9; // 0-8 對應 1-9

        // 檢查是否能組成 i, i+1, i+2
        // rank 最大為 6 時（對應 7），可以組成 7,8,9
        if (rank > 6)
            return false; // rank=7(8) 或 rank=8(9) 無法作為順子起點

        int next1 = startIndex + 1;
        int next2 = startIndex + 2;

        // 確保不跨花色（例如：8萬9萬1筒 是非法的）
        if (next1 / 9 != suit || next2 / 9 != suit) {
            return false;
        }

        // 檢查是否有足夠的牌
        return counts[startIndex] > 0 &&
                counts[next1] > 0 &&
                counts[next2] > 0;
    }

    /**
     * 將 Tile 轉換為索引 (0-33)
     * Man: 0-8, Pin: 9-17, Sou: 18-26, Wind: 27-30, Dragon: 31-33
     */
    private int getTileIndex(Tile tile) {
        switch (tile.getSuit()) {
            case MAN:
                return tile.getRank() - 1; // 0-8
            case PIN:
                return 9 + (tile.getRank() - 1); // 9-17
            case SOU:
                return 18 + (tile.getRank() - 1); // 18-26
            case WIND:
                return 27 + (tile.getRank() - 1); // 27-30
            case DRAGON:
                return 31 + (tile.getRank() - 1); // 31-33
            default:
                throw new IllegalArgumentException("Unknown tile suit: " + tile);
        }
    }

    /**
     * 特殊牌型檢查：七對子
     * Taiwan Mahjong 特殊胡牌型態
     */
    public boolean isSevenPairs(PlayerHand hand) {
        List<Tile> tiles = hand.getStandingTiles();
        if (tiles.size() != 16) { // Taiwan MJ usually 16? Standard is 13+1.
            // WAIT: Taiwan MJ is 16 tiles. But user code uses 13+1 (standard).
            // Currently my project is set up for 13+1 logic (see constructor of Hand).
            // WinStrategy above checks for 14 or 17.
            // If the user wants Taiwan rules strictly, it should be 16+1 (17).
            // But let's stick to what's provided for now, assuming 13+1 logic elsewhere
            // unless changed.
            // Oh, the method above checks 14 or 17. Let's make this consistent.
            // Seven pairs requires 14 tiles (Standard) or 16 tiles (Taiwan)?
            // Standard Seven Pairs is 14 tiles.
            // If Taiwan (16 tiles), you need 8 pairs.
            // Let's adapt based on hand size?
            if (tiles.size() != 14 && tiles.size() != 17)
                return false;
        }

        int[] counts = new int[34];
        for (Tile t : tiles) {
            counts[getTileIndex(t)]++;
        }

        int pairCount = 0;
        int neededPairs = tiles.size() / 2;

        for (int count : counts) {
            if (count == 2) {
                pairCount++;
            } else if (count == 4) {
                pairCount += 2; // 4 tiles = 2 pairs
            } else if (count != 0) {
                return false; // Any single or triplet breaks 7 pairs
            }
        }

        return pairCount == neededPairs;
    }

    /**
     * 特殊牌型檢查：十三么（國士無雙）
     * 1,9萬筒條 + 東南西北中發白 各一張，其中一種兩張
     */
    public boolean isThirteenOrphans(PlayerHand hand) {
        List<Tile> tiles = hand.getStandingTiles();
        int size = tiles.size();
        if (size != 14 && size != 17)
            return false; // Basic check

        // 十三么的牌索引
        int[] orphanIndices = {
                0, 8, // 1萬, 9萬
                9, 17, // 1筒, 9筒
                18, 26, // 1條, 9條
                27, 28, 29, 30, // 東南西北
                31, 32, 33 // 中發白
        };

        int[] counts = new int[34];
        for (Tile t : tiles) {
            int idx = getTileIndex(t);
            counts[idx]++;

            // 檢查是否為么九牌
            boolean isOrphan = false;
            for (int orphanIdx : orphanIndices) {
                if (idx == orphanIdx) {
                    isOrphan = true;
                    break;
                }
            }
            if (!isOrphan)
                return false;
        }

        // 檢查是否有足夠的么九牌種類 (Standard 13, Taiwan might differ but usually 13)
        // Check for 13 unique orphans + 1 pair
        int uniqueCount = 0;
        boolean hasPair = false;

        for (int orphanIdx : orphanIndices) {
            if (counts[orphanIdx] > 0) {
                uniqueCount++;
                if (counts[orphanIdx] == 2) {
                    if (hasPair)
                        return false; // Only one pair allowed
                    hasPair = true;
                } else if (counts[orphanIdx] > 2) {
                    return false;
                }
            }
        }

        // Ensure we have all 13 types
        return uniqueCount == 13 && hasPair;
    }
}
