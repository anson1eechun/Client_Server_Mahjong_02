package com.mahjong.logic;

import java.util.*;

/**
 * 處理麻將遊戲中的動作優先級與執行
 * 這個類別負責：
 * 1. 檢查玩家可執行的動作（吃碰槓胡）
 * 2. 處理動作優先級（胡 > 槓/碰 > 吃）
 * 3. 執行動作並更新遊戲狀態
 * 
 * 這個類別會大幅增加 WMC（複雜度）
 */
public class ActionProcessor {

    public enum ActionType {
        HU(1), // 胡牌 - 最高優先級
        KONG(2), // 槓
        PONG(2), // 碰 - 與槓同優先級
        CHOW(3), // 吃 - 最低優先級
        PASS(4); // 過

        private final int priority;

        ActionType(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * 代表一個可執行的動作
     */
    public static class Action {
        private final ActionType type;
        private final int playerIndex;
        private final Tile targetTile;
        private final List<Tile> involvedTiles; // 用於吃牌時指定順子組成

        public Action(ActionType type, int playerIndex, Tile targetTile) {
            this.type = type;
            this.playerIndex = playerIndex;
            this.targetTile = targetTile;
            this.involvedTiles = new ArrayList<>();
        }

        public ActionType getType() {
            return type;
        }

        public int getPlayerIndex() {
            return playerIndex;
        }

        public Tile getTargetTile() {
            return targetTile;
        }

        public List<Tile> getInvolvedTiles() {
            return involvedTiles;
        }

        public void setInvolvedTiles(List<Tile> tiles) {
            involvedTiles.clear();
            involvedTiles.addAll(tiles);
        }

        @Override
        public String toString() {
            return String.format("Action{type=%s, player=%d, tile=%s}",
                    type, playerIndex, targetTile);
        }
    }

    private final HandValidator validator;
    private final WinStrategy winStrategy;

    public ActionProcessor() {
        this.validator = new HandValidator();
        this.winStrategy = new WinStrategy();
    }

    /**
     * 檢查所有玩家對打出的牌可以執行的動作
     * 
     * @param hands              所有玩家的手牌
     * @param discardedTile      被打出的牌
     * @param discardPlayerIndex 打牌的玩家索引
     * @param currentPlayerIndex 當前輪到的玩家索引
     * @return 可執行的動作列表，已按優先級排序
     */
    public List<Action> checkPossibleActions(
            List<PlayerHand> hands,
            Tile discardedTile,
            int discardPlayerIndex,
            int currentPlayerIndex) {

        List<Action> actions = new ArrayList<>();
        int playerCount = hands.size();

        // 檢查每個玩家（除了打牌者）
        for (int i = 0; i < playerCount; i++) {
            if (i == discardPlayerIndex)
                continue;

            PlayerHand hand = hands.get(i);

            // 1. 檢查胡牌（最高優先級）
            if (canHu(hand, discardedTile)) {
                actions.add(new Action(ActionType.HU, i, discardedTile));
            }

            // 2. 檢查槓（次高優先級）
            if (validator.canKong(hand, discardedTile)) {
                actions.add(new Action(ActionType.KONG, i, discardedTile));
            }

            // 3. 檢查碰（次高優先級）
            if (validator.canPong(hand, discardedTile)) {
                actions.add(new Action(ActionType.PONG, i, discardedTile));
            }

            // 4. 檢查吃（只有下家可以吃）
            int nextPlayer = (discardPlayerIndex + 1) % playerCount;
            if (i == nextPlayer && validator.canChow(hand, discardedTile)) {
                // 吃牌可能有多種組合方式
                List<List<Tile>> chowOptions = getChowOptions(hand, discardedTile);
                for (List<Tile> option : chowOptions) {
                    Action chowAction = new Action(ActionType.CHOW, i, discardedTile);
                    chowAction.setInvolvedTiles(option);
                    actions.add(chowAction);
                }
            }
        }

        // 按優先級排序
        actions.sort(Comparator.comparingInt(a -> a.getType().getPriority()));

        return actions;
    }

    /**
     * 檢查自摸胡牌
     */
    public boolean canSelfDrawWin(PlayerHand hand) {
        return winStrategy.isWinningHand(hand);
    }

    /**
     * 檢查點炮胡牌
     */
    private boolean canHu(PlayerHand hand, Tile discardedTile) {
        // 創建臨時手牌加入被打出的牌
        PlayerHand tempHand = cloneHand(hand);
        tempHand.addTile(discardedTile);

        return winStrategy.isWinningHand(tempHand) ||
                winStrategy.isSevenPairs(tempHand) ||
                winStrategy.isThirteenOrphans(tempHand);
    }

    /**
     * 獲取所有可能的吃牌組合
     */
    private List<List<Tile>> getChowOptions(PlayerHand hand, Tile targetTile) {
        List<List<Tile>> options = new ArrayList<>();

        if (!targetTile.isNumberTile()) {
            return options; // 字牌不能吃
        }

        List<Tile> tiles = hand.getStandingTiles();
        Tile.Suit suit = targetTile.getSuit();
        int rank = targetTile.getRank();

        // 三種可能的順子：
        // 1. (rank-2, rank-1, rank) 例如：目標是3，手上有1,2
        if (rank >= 3) {
            Tile tile1 = findTile(tiles, suit, rank - 2);
            Tile tile2 = findTile(tiles, suit, rank - 1);
            if (tile1 != null && tile2 != null) {
                options.add(Arrays.asList(tile1, tile2, targetTile));
            }
        }

        // 2. (rank-1, rank, rank+1) 例如：目標是3，手上有2,4
        if (rank >= 2 && rank <= 8) {
            Tile tile1 = findTile(tiles, suit, rank - 1);
            Tile tile2 = findTile(tiles, suit, rank + 1);
            if (tile1 != null && tile2 != null) {
                options.add(Arrays.asList(tile1, targetTile, tile2));
            }
        }

        // 3. (rank, rank+1, rank+2) 例如：目標是3，手上有4,5
        if (rank <= 7) {
            Tile tile1 = findTile(tiles, suit, rank + 1);
            Tile tile2 = findTile(tiles, suit, rank + 2);
            if (tile1 != null && tile2 != null) {
                options.add(Arrays.asList(targetTile, tile1, tile2));
            }
        }

        return options;
    }

    /**
     * 在手牌中尋找指定花色和數字的牌
     */
    private Tile findTile(List<Tile> tiles, Tile.Suit suit, int rank) {
        for (Tile tile : tiles) {
            if (tile.getSuit() == suit && tile.getRank() == rank) {
                return tile;
            }
        }
        return null;
    }

    /**
     * 複製手牌（用於胡牌檢查）
     */
    private PlayerHand cloneHand(PlayerHand original) {
        PlayerHand clone = new PlayerHand();
        for (Tile tile : original.getStandingTiles()) {
            clone.addTile(tile);
        }
        // Assuming addMeld(Meld) exists or adapting if it doesn't
        // Original PlayerHand has addMeld(Meld), waiting for verification or I might
        // need to fix it.
        // Looking at ActionProcessor instructions, it calls addMeld(Meld), so
        // PlayerHand needs it.
        // My previous view of PlayerHand showed a method "addMeld(Type type, String
        // tileName)".
        // I might need to add "addMeld(Meld meld)" back or ensure compatibility.
        // The previous tool call output for PlayerHand showed "addMeld(Meld m) {
        // openMelds.add(m); }" logic was replaced or existed?
        // Ah, I modified `addMeld(String tileName)` to `addMeld(Type, String)`.
        // Let's assume `addMeld(Meld)` exists or I'll fix it if compilation fails.
        // For safe measure, I'll check PlayerHand again or just rely on ActionProcessor
        // calling internal mechanics?
        // Actually ActionProcessor `cloneHand` calls `clone.addMeld(meld)`.
        // `PlayerHand.java` has `openMelds` list. I can just access it or add a method.
        for (Meld meld : original.getOpenMelds()) {
            clone.addMeld(meld);
        }
        return clone;
    }

    /**
     * 執行吃牌動作
     */
    public void executeChow(PlayerHand hand, Action action) {
        if (action.getType() != ActionType.CHOW) {
            throw new IllegalArgumentException("Action must be CHOW type");
        }

        List<Tile> chowTiles = action.getInvolvedTiles();
        Tile targetTile = action.getTargetTile();

        // 從手牌移除相關牌（不包括目標牌，因為它來自別人）
        for (Tile tile : chowTiles) {
            if (!tile.equals(targetTile)) {
                hand.removeTile(tile); // 使用 Tile 物件而非 String
            }
        }

        // 組成完整的順子：chowTiles 已經包含完整的 3 張牌（從 getChowOptions 返回）
        // 確保順序正確（排序）
        List<Tile> completeChow = new ArrayList<>(chowTiles);
        completeChow.sort(Comparator.comparing(Tile::getSuit).thenComparingInt(Tile::getRank));
        
        if (completeChow.size() != 3) {
            throw new IllegalStateException("CHOW must have exactly 3 tiles, got " + completeChow.size());
        }
        
        Meld meld = Meld.createChow(completeChow.get(0), completeChow.get(1), completeChow.get(2));
        hand.addMeld(meld);
    }

    /**
     * 執行碰牌動作
     */
    public void executePong(PlayerHand hand, Tile targetTile) {
        // 從手牌移除兩張相同的牌
        int removed = 0;
        List<Tile> tiles = new ArrayList<>(hand.getStandingTiles());
        for (Tile tile : tiles) {
            if (tile.equals(targetTile) && removed < 2) {
                hand.removeTile(tile); // 使用 Tile 物件而非 String
                removed++;
            }
        }

        if (removed != 2) {
            throw new IllegalStateException("Cannot pong: not enough tiles");
        }

        // 添加面子：使用便利方法創建碰牌
        Meld meld = Meld.createPong(targetTile);
        hand.addMeld(meld);
    }

    /**
     * 執行槓牌動作（明槓）
     */
    public void executeKong(PlayerHand hand, Tile targetTile) {
        // 從手牌移除三張相同的牌
        int removed = 0;
        List<Tile> tiles = new ArrayList<>(hand.getStandingTiles());
        for (Tile tile : tiles) {
            if (tile.equals(targetTile) && removed < 3) {
                hand.removeTile(tile); // 使用 Tile 物件而非 String
                removed++;
            }
        }

        if (removed != 3) {
            throw new IllegalStateException("Cannot kong: not enough tiles");
        }

        // 添加面子：使用便利方法創建槓牌
        Meld meld = Meld.createKong(targetTile);
        hand.addMeld(meld);
    }

    /**
     * ✅ P1-1: 檢測玩家是否可以暗槓（手上是否有 4 張相同牌）
     * @param hand 玩家手牌
     * @return 可以暗槓的牌列表
     */
    public List<Tile> getConcealedKongOptions(PlayerHand hand) {
        List<Tile> options = new ArrayList<>();
        Map<Tile, Integer> counts = new HashMap<>();
        
        // 計算每種牌的數量
        for (Tile tile : hand.getStandingTiles()) {
            counts.put(tile, counts.getOrDefault(tile, 0) + 1);
        }
        
        // 找出數量為 4 的牌
        for (Map.Entry<Tile, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == 4) {
                options.add(entry.getKey());
            }
        }
        
        return options;
    }

    /**
     * ✅ P1-1: 執行暗槓動作
     * @param hand 玩家手牌
     * @param tile 要暗槓的牌
     */
    public void executeConcealedKong(PlayerHand hand, Tile tile) {
        // 移除 4 張牌
        int removed = 0;
        List<Tile> tiles = new ArrayList<>(hand.getStandingTiles());
        for (Tile t : tiles) {
            if (t.equals(tile) && removed < 4) {
                hand.removeTile(t);
                removed++;
            }
        }
        
        if (removed != 4) {
            throw new IllegalStateException("Cannot concealed kong: not enough tiles");
        }
        
        // 添加暗槓面子
        Meld kong = Meld.createConcealedKong(tile);
        hand.addMeld(kong);
    }
}
