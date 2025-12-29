package com.mahjong.server;

import com.mahjong.logic.MahjongRuleEngine;
import com.mahjong.logic.PlayerHand;
import com.mahjong.logic.Tile;
import com.mahjong.logic.HandValidator;
import com.mahjong.logic.Meld;
import com.mahjong.logic.TingDetector;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;
import org.java_websocket.WebSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WebSocketGameSession {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketGameSession.class);
    
    private final List<WebSocket> players;
    private final Map<WebSocket, String> nickNames;
    private final MahjongRuleEngine engine;
    private final List<PlayerHand> hands;
    private final List<String> sea; // Discarded tiles
    private int currentPlayerIndex = 0; // 0=East, 1=South, 2=West, 3=North
    private final ObjectMapper mapper = new ObjectMapper();

    public WebSocketGameSession(List<WebSocket> players, Map<WebSocket, String> nickNames) {
        this.players = players;
        this.nickNames = nickNames;
        this.engine = new MahjongRuleEngine(new Random());
        this.hands = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            hands.add(new PlayerHand());
        this.sea = new ArrayList<>();
    }

    public void start() {
        logger.info("Session Starting...");
        // 1. Shuffle
        engine.shuffle();

        // 2. Deal
        engine.dealInitialHands(hands);
        
        // ✅ P0-1: 莊家先摸 1 張牌（莊家應為 17 張起手）
        Tile firstDraw = engine.drawTile();
        if (firstDraw != null) {
            hands.get(0).addTile(firstDraw);
        }
        
        // Debug: 檢查發牌結果
        for (int i = 0; i < 4; i++) {
            logger.debug("Player {} hand size: {}, tiles: {}", 
                i, hands.get(i).getTileCount(), hands.get(i).getTilesStr());
        }

        // 3. Notify Game Start
        broadcast(new Packet(Command.GAME_START, null));

        // 4. Send Initial State
        logger.debug("Broadcasting initial state...");
        broadcastState();

        // 5. Start First Turn (East - 莊家)
        // 莊家第一輪不用再摸牌，直接出牌
        currentPlayerIndex = 0;
        isFirstTurn = true;
        broadcastState();
        
        // 提示莊家出牌
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "莊家請出牌");
        send(players.get(0), new Packet(Command.GAME_UPDATE, msg));
    }

    private final com.mahjong.logic.ActionProcessor processor = new com.mahjong.logic.ActionProcessor();

    // START: Action Logic Fields
    private final LinkedList<ActionGroup> actionQueue = new LinkedList<>();
    private ActionGroup currentActionGroup = null;
    private final Set<Integer> pendingResponses = new HashSet<>();
    private boolean waitingForAction = false;
    private Tile pendingDiscardTile = null;
    private final HandValidator validator = new HandValidator(); // Kept for Tsumo check or remove if not needed?
    private final TingDetector tingDetector = new TingDetector(); // 聽牌檢測器
    private boolean isFirstTurn = false; // 標記是否為莊家第一輪
    // END: Action Logic Fields
    
    public void processPlayerAction(WebSocket conn, Packet packet) {
        int playerIndex = players.indexOf(conn);
        Command cmd = packet.getCommand();

        // ✅ 修復：PLAY_CARD 應該優先處理
        // 當玩家是當前玩家時，應該能夠出牌（除非正在等待其他玩家的動作回應）
        if (cmd == Command.PLAY_CARD) {
            logger.debug("PLAY_CARD request from Player {}, currentPlayerIndex = {}, waitingForAction = {}", 
                playerIndex, currentPlayerIndex, waitingForAction);
            if (playerIndex == currentPlayerIndex) {
                // 如果正在等待動作，只有在等待自摸選擇時（priority 0）才能出牌
                // 其他情況（等待其他玩家回應）不應該出牌
                if (waitingForAction) {
                    logger.debug("waitingForAction is true, currentActionGroup = {}", currentActionGroup);
                    if (currentActionGroup != null && currentActionGroup.priority == 0) {
                        // 等待自摸選擇時，玩家選擇跳過後可以出牌
                        // 但實際上，跳過後 waitingForAction 會被設置為 false
                        // 所以這裡應該不會執行到
                        logger.debug("Player {} trying to play card while waiting for self-draw choice", playerIndex);
                    } else {
                        logger.debug("Player {} tried to play card but waiting for other players' actions", playerIndex);
                        return;
                    }
                }
                
                String tileStr = (String) packet.getData().get("tile");
                Tile tile = Tile.valueOf(tileStr);

                logger.debug("Player {} trying to discard {}", playerIndex, tileStr);
                // Logic: Remove from hand, Add to Sea
                boolean removed = hands.get(playerIndex).removeTile(tileStr);
                logger.debug("Tile removal result: {}", removed);
                if (removed) {
                    sea.add(tileStr);
                    hands.get(playerIndex).sort();
                    logger.debug("Before broadcastState in PLAY_CARD: currentPlayerIndex = {}", currentPlayerIndex);
                    broadcastState();
                    logger.debug("After broadcastState in PLAY_CARD: currentPlayerIndex = {}", currentPlayerIndex);

                    // 如果正在等待動作，先清除狀態
                    if (waitingForAction) {
                        waitingForAction = false;
                        currentActionGroup = null;
                        pendingResponses.clear();
                    }

                    logger.debug("Before resolveDiscard: currentPlayerIndex = {}", currentPlayerIndex);
                    resolveDiscard(tile, playerIndex);
                    logger.debug("After resolveDiscard: currentPlayerIndex = {}", currentPlayerIndex);
                } else {
                    logger.warn("Player {} tried to discard {} but tile not found in hand", playerIndex, tileStr);
                }
            } else {
                logger.debug("Player {} tried to play card but not their turn (current: {})", playerIndex, currentPlayerIndex);
            }
            return;
        }

        // Handling Action Response (Pong/Skip) -- Must happen even if not current player
        if (waitingForAction) {
            handleActionResponse(playerIndex, packet);
            return;
        }

        if (playerIndex != currentPlayerIndex) {
            return;
        }
    }

    private void resolveDiscard(Tile discard, int discarderIdx) {
        logger.debug("resolveDiscard called: discard = {}, discarderIdx = {}, currentPlayerIndex = {}", 
            discard, discarderIdx, currentPlayerIndex);
        
        // ✅ 修復：莊家第一次出牌後，重置 isFirstTurn
        // 這樣下次輪到 Player 0 時，就不會再跳過摸牌
        if (discarderIdx == 0 && isFirstTurn) {
            logger.debug("Dealer's first discard completed, resetting isFirstTurn");
            isFirstTurn = false;
        }
        
        actionQueue.clear();
        currentActionGroup = null;
        pendingResponses.clear();

        ActionGroup tierHu = new ActionGroup(1);
        ActionGroup tierPong = new ActionGroup(2);
        ActionGroup tierChow = new ActionGroup(3);

        // Use ActionProcessor to get all valid actions
        List<com.mahjong.logic.ActionProcessor.Action> allActions = processor.checkPossibleActions(hands, discard,
                discarderIdx, currentPlayerIndex);
        logger.debug("checkPossibleActions returned {} actions", allActions.size());

        for (com.mahjong.logic.ActionProcessor.Action act : allActions) {
            int pIdx = act.getPlayerIndex();
            switch (act.getType()) {
                case HU:
                    tierHu.addAction(pIdx, "HU");
                    break;
                case PONG:
                    tierPong.addAction(pIdx, "PONG");
                    break;
                case KONG:
                    tierPong.addAction(pIdx, "KONG");
                    break;
                case CHOW:
                    // Extract the 2 tiles from involvedTiles (which has 3)
                    // The discard is 'discard'. Find the others.
                    List<Tile> inv = act.getInvolvedTiles();
                    List<String> others = new ArrayList<>();
                    boolean discardFound = false;
                    for (Tile t : inv) {
                        // Compare using equals? Or suit/rank.
                        // discard is the exact object passed? Maybe not.
                        if (!discardFound && t.getSuit() == discard.getSuit() && t.getRank() == discard.getRank()) {
                            discardFound = true;
                        } else {
                            others.add(t.toString());
                        }
                    }
                    if (others.size() == 2) {
                        tierChow.addAction(pIdx, "CHOW " + others.get(0) + "," + others.get(1));
                    }
                    break;
                default:
                    break;
            }
        }

        // Add non-empty groups to queue
        if (!tierHu.players.isEmpty())
            actionQueue.add(tierHu);
        if (!tierPong.players.isEmpty())
            actionQueue.add(tierPong);
        if (!tierChow.players.isEmpty())
            actionQueue.add(tierChow);

        if (actionQueue.isEmpty()) {
            logger.debug("Action queue is empty, calling nextTurn(), currentPlayerIndex before = {}", currentPlayerIndex);
            nextTurn();
            logger.debug("After nextTurn() in resolveDiscard, currentPlayerIndex = {}", currentPlayerIndex);
        } else {
            logger.debug("Action queue has {} groups, calling processNextActionGroup()", actionQueue.size());
            pendingDiscardTile = discard;
            processNextActionGroup();
        }
    }

    private synchronized void processNextActionGroup() {
        if (actionQueue.isEmpty()) {
            logger.debug("Action Queue empty. Moving to next turn.");
            // No more actions, proceed to next turn
            waitingForAction = false;
            pendingDiscardTile = null;
            nextTurn();
            return;
        }

        currentActionGroup = actionQueue.poll(); // Get top priority group
        waitingForAction = true;
        pendingResponses.clear();
        pendingResponses.addAll(currentActionGroup.players);

        logger.debug("Processing Action Group. Priority: {}, Players: {}", 
            currentActionGroup.priority, currentActionGroup.players);

        // Send Requests
        for (Integer pIdx : currentActionGroup.players) {
            List<String> actions = currentActionGroup.playerActions.get(pIdx);

            logger.debug("Asking Player {} for actions: {}", pIdx, actions);

            Map<String, Object> data = new HashMap<>();
            data.put("action", "CHOOSE_ACTION"); // generic command
            data.put("choices", actions); // List of strings
            data.put("tile", pendingDiscardTile.toString());
            send(players.get(pIdx), new Packet(Command.ACTION_REQUEST, data));

            // IMPROVED MESSAGE: Waiting for Player X (ACTION)
            broadcastMessage("Game", "Waiting for Player " + pIdx + " to " + actions + "...");
        }
    }

    private synchronized void handleActionResponse(int playerIndex, Packet packet) {
        if (!waitingForAction || currentActionGroup == null) {
            logger.debug("Ignore ActionResponse from P{} (Not waiting)", playerIndex);
            return;
        }
        if (!pendingResponses.contains(playerIndex)) {
            logger.debug("Ignore ActionResponse from P{} (Not in pending list {})", 
                playerIndex, pendingResponses);
            return;
        }

        Command cmd = packet.getCommand();
        if (cmd == Command.ACTION) {
            String type = (String) packet.getData().get("type"); // Chosen action or SKIP
            logger.debug("Received Action: {} from Player {}", type, playerIndex);

            if ("SKIP".equals(type)) {
                broadcastMessage("Game", "Player " + playerIndex + " skipped.");
                pendingResponses.remove(playerIndex);
                logger.debug("Pending responses remaining: {}", pendingResponses);

                if (pendingResponses.isEmpty()) {
                    // Everyone in this group skipped/resolved.

                    // SPECIAL CASE: Priority 0 (Self-Draw) Skip
                    if (currentActionGroup.priority == 0) {
                        logger.debug("Player skipped Self-Draw. Resuming discard phase.");
                        waitingForAction = false;
                        currentActionGroup = null;
                        // Do NOT call nextTurn() or processNextActionGroup()
                        // User is now free to discard via PLAY_CARD command
                    } else {
                        // Regular Discard Response Skip
                        logger.debug("Group resolved (All skipped). Moving to next group.");
                        processNextActionGroup();
                    }
                }
            } else {
                // Player chose an action (HU, PONG, KONG, CHOW)
                // Validate if they actually had that option
                List<String> allowed = currentActionGroup.playerActions.get(playerIndex);
                if (allowed != null && allowed.contains(type)) {
                    // ACTION CONFIRMED!
                    actionQueue.clear();
                    pendingResponses.clear();
                    currentActionGroup = null; // Done

                    if ("HU".equals(type))
                        performHu(playerIndex);
                    else if ("KONG".equals(type))
                        performKong(playerIndex);
                    else if ("PONG".equals(type))
                        performPong(playerIndex);
                    else if (type.startsWith("CHOW ")) {
                        // Parse "CHOW M2,M3"
                        String content = type.substring(5); // "M2,M3"
                        String[] parts = content.split(",");
                        if (parts.length == 2) {
                            performChow(playerIndex, parts[0], parts[1]);
                        } else {
                            System.err.println("Invalid Chow format: " + type);
                        }
                    }
                    else if (type.startsWith("CONCEALED_KONG ")) {
                        // ✅ P1-1: 處理暗槓
                        String tileStr = type.substring(15); // "CONCEALED_KONG M1" -> "M1"
                        Tile tile = Tile.valueOf(tileStr);
                        performConcealedKong(playerIndex, tile);
                    }
                } else {
                    System.err.println("[ERROR] Player " + playerIndex + " tried invalid action " + type);
                }
            }
        }
    }

    private synchronized void performHu(int playerIndex) {
        try {
            broadcastMessage("Game", "Player " + playerIndex + " HU! Game Over.");

            // Add the winning tile to hand for display (if it exists)
            // 注意：自摸胡牌時 pendingDiscardTile 可能為 null
            PlayerHand hand = hands.get(playerIndex);
            if (pendingDiscardTile != null) {
                hand.addTile(pendingDiscardTile);
            }

            waitingForAction = false;
            pendingDiscardTile = null;

            // Broadcast Final State
            broadcastState();

            // Send Game Over Packet
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Player " + playerIndex + " Wins!");
            broadcast(new Packet(Command.GAME_OVER, data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void performKong(int playerIndex) {
        try {
            // Exposed Kong (Ming Gang)
            // ✅ 修復：檢查 pendingDiscardTile 是否為 null
            if (pendingDiscardTile == null) {
                System.err.println("[ERROR] PerformKong failed: pendingDiscardTile is null");
                waitingForAction = false;
                return;
            }
            
            PlayerHand hand = hands.get(playerIndex);
            String tileName = pendingDiscardTile.toString();

            // Remove 3 matching tiles from hand
            hand.removeTile(tileName);
            hand.removeTile(tileName);
            hand.removeTile(tileName);

            // Add Meld (4 tiles technically, but displayed as Meld)
            // In Taiwan MJ, Exposed Kong is 4 tiles.
            // For visual simplicity we might just add the one meld object
            if (!sea.isEmpty())
                sea.remove(sea.size() - 1); // Remove from sea
            // 使用便利方法創建槓牌
            Tile kongTile = Tile.valueOf(tileName);
            Meld kongMeld = Meld.createKong(kongTile);
            hand.addMeld(kongMeld);

            waitingForAction = false;
            pendingDiscardTile = null;
            currentPlayerIndex = playerIndex;

            broadcastMessage("Game", "Player " + playerIndex + " KONG!");

            // Kong -> Draw Replacement Tile -> Discard
            // We call startTurn() but we must ensure we don't change player (already set)
            // But startTurn draws a tile. Correct.
            startTurn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ✅ P1-1: 執行暗槓
     */
    private synchronized void performConcealedKong(int playerIndex, Tile tile) {
        try {
            PlayerHand hand = hands.get(playerIndex);
            
            // 執行暗槓
            processor.executeConcealedKong(hand, tile);
            
            // 補牌（從牌尾補 1 張）
            Tile replacement = engine.drawTile();
            if (replacement != null) {
                hand.addTile(replacement);
            }
            
            broadcastMessage("Game", "Player " + playerIndex + " 暗槓 " + tile.toString());
            broadcastState();
            
            // 檢查槓上開花
            if (tingDetector.isWinningHand(hand)) {
                logger.debug("Player {} 槓上開花!", playerIndex);
                // 提示玩家可以胡牌
                Map<String, Object> actReq = new HashMap<>();
                actReq.put("action", "CHOOSE_ACTION");
                actReq.put("choices", Arrays.asList("HU", "SKIP"));
                actReq.put("tile", replacement != null ? replacement.toString() : "");
                send(players.get(playerIndex), new Packet(Command.ACTION_REQUEST, actReq));
                
                waitingForAction = true;
                pendingResponses.clear();
                pendingResponses.add(playerIndex);
                currentActionGroup = new ActionGroup(0);
                currentActionGroup.addAction(playerIndex, "HU");
                return;
            }
            
            waitingForAction = false;
            // 繼續該玩家回合（等待出牌）
        } catch (Exception e) {
            e.printStackTrace();
            broadcastMessage("System", "Error performing Concealed Kong: " + e.getMessage());
            waitingForAction = false;
        }
    }

    private synchronized void performChow(int playerIndex, String t1Name, String t2Name) {
        try {
            logger.debug("performChow called for Player {}, currentPlayerIndex before = {}", playerIndex, currentPlayerIndex);
            PlayerHand hand = hands.get(playerIndex);
            
            // ✅ 修復：檢查 pendingDiscardTile 是否為 null
            if (pendingDiscardTile == null) {
                System.err.println("[ERROR] PerformChow failed: pendingDiscardTile is null");
                waitingForAction = false;
                nextTurn();
                return;
            }
            
            Tile discard = pendingDiscardTile;

            // Remove the 2 specific tiles from hand
            boolean r1 = hand.removeTile(t1Name);
            boolean r2 = hand.removeTile(t2Name);

            if (r1 && r2) {
                if (!sea.isEmpty())
                    sea.remove(sea.size() - 1);

                // 使用重構後的 Meld 類別：創建完整的吃牌（3 張牌）
                Tile t1 = Tile.valueOf(t1Name);
                Tile t2 = Tile.valueOf(t2Name);
                // 組成完整的順子並排序
                List<Tile> chowTiles = new ArrayList<>();
                chowTiles.add(t1);
                chowTiles.add(t2);
                chowTiles.add(discard);
                chowTiles.sort(Comparator.comparing(Tile::getSuit).thenComparingInt(Tile::getRank));
                
                // 使用便利方法創建 Meld
                Meld chowMeld = Meld.createChow(chowTiles.get(0), chowTiles.get(1), chowTiles.get(2));
                hand.addMeld(chowMeld);

                broadcastMessage("Game", "Player " + playerIndex + " CHOW (" + t1Name + "," + discard.toString() + "," + t2Name + ")!");

                waitingForAction = false;
                pendingDiscardTile = null;
                currentPlayerIndex = playerIndex;
                logger.debug("After CHOW: currentPlayerIndex set to {}, waitingForAction = {}", currentPlayerIndex, waitingForAction);

                // 監測手牌狀態（檢查是否聽牌或胡牌）
                monitorHandStatus(playerIndex);

                // Check if player can win after CHOW
                // 執行 CHOW 後，手牌已經改變，需要檢查是否可以胡牌
                // 執行 CHOW 後：手牌 -2，Meld +1（3張），總牌數應該還是 14 張
                // 使用 TingDetector 檢查是否為胡牌
                int totalTiles = hand.getConnectionCount(); // 手牌 + Meld 總數
                if (totalTiles == 14) {
                    if (tingDetector.isWinningHand(hand)) {
                        logger.debug("Player {} can HU after CHOW!", playerIndex);
                        // 提示玩家可以胡牌
                        Map<String, Object> actReq = new HashMap<>();
                        actReq.put("action", "CHOOSE_ACTION");
                        actReq.put("choices", Arrays.asList("HU", "SKIP"));
                        actReq.put("tile", "");
                        send(players.get(playerIndex), new Packet(Command.ACTION_REQUEST, actReq));
                        broadcastMessage("Game", "Player " + playerIndex + " can HU! Choose to HU or continue playing.");
                        // 設置等待玩家選擇
                        waitingForAction = true;
                        pendingResponses.clear();
                        pendingResponses.add(playerIndex);
                        currentActionGroup = new ActionGroup(0);
                        currentActionGroup.addAction(playerIndex, "HU");
                        return; // 等待玩家選擇，不繼續出牌流程
                    }
                }

                // ✅ 修復：CHOW 後需要出牌（不吃牌），明確提示玩家出牌
                logger.debug("Before broadcastState after CHOW: currentPlayerIndex = {}", currentPlayerIndex);
                broadcastState();
                logger.debug("After broadcastState after CHOW: currentPlayerIndex = {}", currentPlayerIndex);
                Map<String, Object> discardMsg = new HashMap<>();
                discardMsg.put("message", "請出牌");
                send(players.get(playerIndex), new Packet(Command.GAME_UPDATE, discardMsg));
                logger.debug("After sending discard message after CHOW: currentPlayerIndex = {}", currentPlayerIndex);
            } else {
                System.err.println("Error: performChow missing tiles " + t1Name + ", " + t2Name);
                waitingForAction = false;
                nextTurn();
            }
        } catch (Exception e) {
            e.printStackTrace();
            waitingForAction = false;
            nextTurn();
        }
    }

    private boolean hasTile(List<Tile> tiles, Tile anchor, int offset) {
        // Only works for Number tiles of same suit
        if (!anchor.isNumberTile())
            return false;
        int targetRank = anchor.getRank() + offset;
        if (targetRank < 1 || targetRank > 9)
            return false;

        for (Tile t : tiles) {
            if (t.getSuit() == anchor.getSuit() && t.getRank() == targetRank) {
                return true;
            }
        }
        return false;
    }

    private void removeTileOffset(PlayerHand hand, Tile anchor, int offset) {
        // Find name of tile with offset
        // We iterate Hand again to find it precisely
        int targetRank = anchor.getRank() + offset;
        List<Tile> tiles = hand.getStandingTiles();
        for (Tile t : tiles) {
            if (t.getSuit() == anchor.getSuit() && t.getRank() == targetRank) {
                hand.removeTile(t.toString());
                return; // Remove only one
            }
        }
    }

    private synchronized void performPong(int playerIndex) {
        try {
            PlayerHand hand = hands.get(playerIndex);

            // ✅ 修復：檢查 pendingDiscardTile 是否為 null
            if (pendingDiscardTile == null) {
                System.err.println("[ERROR] PerformPong failed: pendingDiscardTile is null");
                waitingForAction = false;
                return;
            }

            // 1. Remove 2 tiles from hand
            boolean r1 = hand.removeTile(pendingDiscardTile.toString());
            boolean r2 = hand.removeTile(pendingDiscardTile.toString());

            if (!r1 || !r2) {
                System.err.println("[ERROR] PerformPong failed: Missing tiles in hand for P" + playerIndex);
                // Recovery: Reset state and continue
                waitingForAction = false;
                pendingDiscardTile = null;
                nextTurn();
                return;
            }

            // 2. Add Meld to Hand
            if (sea.isEmpty()) {
                System.err.println("[ERROR] PerformPong failed: Sea is empty!");
                waitingForAction = false;
                pendingDiscardTile = null;
                return;
            }
            sea.remove(sea.size() - 1); // Take discard from sea
            // 使用便利方法創建碰牌
            Meld pongMeld = Meld.createPong(pendingDiscardTile);
            hand.addMeld(pongMeld);

            waitingForAction = false;
            pendingDiscardTile = null;

            // 3. Set Turn to this player
            currentPlayerIndex = playerIndex;
            logger.debug("After PONG: currentPlayerIndex set to {}, waitingForAction = {}", currentPlayerIndex, waitingForAction);

            broadcastMessage("Game", "Player " + playerIndex + " PONG!");

            // 4. 監測手牌狀態（檢查是否聽牌或胡牌）
            monitorHandStatus(playerIndex);
            
            // 5. Check if player can win after PONG
            // 執行 PONG 後，手牌已經改變，需要檢查是否可以胡牌
            // 執行 PONG 後：手牌 -2，Meld +1（3張），總牌數應該還是 14 張
            // 使用 TingDetector 檢查是否為胡牌
            int totalTiles = hand.getConnectionCount(); // 手牌 + Meld 總數
            if (totalTiles == 14) {
                if (tingDetector.isWinningHand(hand)) {
                    logger.debug("Player {} can HU after PONG!", playerIndex);
                    // 提示玩家可以胡牌
                    Map<String, Object> actReq = new HashMap<>();
                    actReq.put("action", "CHOOSE_ACTION");
                    actReq.put("choices", Arrays.asList("HU", "SKIP"));
                    actReq.put("tile", "");
                    send(players.get(playerIndex), new Packet(Command.ACTION_REQUEST, actReq));
                    broadcastMessage("Game", "Player " + playerIndex + " can HU! Choose to HU or continue playing.");
                    // 設置等待玩家選擇
                    waitingForAction = true;
                    pendingResponses.clear();
                    pendingResponses.add(playerIndex);
                    currentActionGroup = new ActionGroup(0);
                    currentActionGroup.addAction(playerIndex, "HU");
                    return; // 等待玩家選擇，不繼續出牌流程
                }
            }

            // 6. IMPORTANT: Pong -> No Draw -> Must Discard
            logger.debug("Before broadcastState after PONG: currentPlayerIndex = {}", currentPlayerIndex);
            broadcastState();
            logger.debug("After broadcastState after PONG: currentPlayerIndex = {}", currentPlayerIndex);
            // ✅ 修復：PONG 後需要出牌（不摸牌），明確提示玩家出牌
            Map<String, Object> discardMsg = new HashMap<>();
            discardMsg.put("message", "請出牌");
            send(players.get(playerIndex), new Packet(Command.GAME_UPDATE, discardMsg));
            logger.debug("After sending discard message after PONG: currentPlayerIndex = {}", currentPlayerIndex);

        } catch (Exception e) {
            e.printStackTrace();
            broadcastMessage("System", "Error performing Pong: " + e.getMessage());
            waitingForAction = false;
            nextTurn();
        }
    }

    private void nextTurn() {
        try {
            logger.debug("nextTurn called: currentPlayerIndex before = {}", currentPlayerIndex);
            currentPlayerIndex = (currentPlayerIndex + 1) % 4;
            logger.debug("nextTurn: currentPlayerIndex after = {}, about to call startTurn()", currentPlayerIndex);
            startTurn();
            logger.debug("nextTurn: startTurn() completed");
        } catch (Exception e) {
            logger.error("Error in nextTurn", e);
            broadcastMessage("System", "Error in nextTurn: " + e.getMessage());
        }
    }

    private void startTurn() {
        try {
            logger.debug("startTurn called: currentPlayerIndex = {}, isFirstTurn = {}", currentPlayerIndex, isFirstTurn);
            // ✅ P0-1: 如果是莊家第一輪，跳過摸牌
            if (currentPlayerIndex == 0 && isFirstTurn) {
                logger.debug("Skipping first turn for dealer");
                isFirstTurn = false;
                // 明確提示莊家出牌
                Map<String, Object> msg = new HashMap<>();
                msg.put("message", "莊家請出牌");
                send(players.get(0), new Packet(Command.GAME_UPDATE, msg));
                return;
            }
            
            // Draw tile for current player
            logger.debug("Drawing tile for Player {}", currentPlayerIndex);
            Tile drawn = engine.drawTile();
            if (drawn == null) {
                logger.warn("Wall is empty, game should end");
                Map<String, Object> data = new HashMap<>();
                data.put("message", "Game Over - Wall Empty!");
                broadcast(new Packet(Command.GAME_OVER, data));
                return;
            }

            PlayerHand finalHand = hands.get(currentPlayerIndex);
            finalHand.addTile(drawn);
            logger.debug("Player {} drew tile {}, hand size now: {}", 
                currentPlayerIndex, drawn, finalHand.getTileCount());

            // Notify everyone
            broadcastState();

            // Send specific "You Drew X" message
            Map<String, Object> drawMsg = new HashMap<>();
            drawMsg.put("action", "DRAW");
            drawMsg.put("tile", drawn.toString());
            send(players.get(currentPlayerIndex), new Packet(Command.GAME_UPDATE, drawMsg));

            logger.info("Turn: Player {} drew {}", currentPlayerIndex, drawn);

        // --- CHECK SELF-DRAW WIN (Tsumo) ---
        // 使用 TingDetector 檢查自摸：手牌已經包含摸到的牌，直接檢查是否為胡牌
        boolean canTsumo = tingDetector.isWinningHand(finalHand);
        
        // 監測手牌狀態（包括聽牌狀態）
        if (!canTsumo) {
            // 如果還沒胡牌，檢查是否聽牌
            TingDetector.TingResult tingResult = tingDetector.detectTing(finalHand);
            if (tingResult.isTing()) {
                logger.debug("Player {} is Ting, waiting for: {}", 
                    currentPlayerIndex, tingResult.getTingTiles());
            }
        }

        if (canTsumo) {
            logger.debug("Player {} can Self-Draw HU!", currentPlayerIndex);

            // We reuse the Action mechanism.
            // Create a pseudo ActionGroup for Self-Draw
            currentActionGroup = new ActionGroup(0); // Priority 0 (Highest)
            currentActionGroup.addAction(currentPlayerIndex, "HU");
            // Also need option to Discard (SKIP -> Discard Phase)
            // Actually, if we use "CHOOSE_ACTION", the client shows buttons.
            // If they click "HU" -> performHu.
            // If they click "SKIP" -> They just return to normal Discard state.

            waitingForAction = true;
            pendingResponses.clear();
            pendingResponses.add(currentPlayerIndex);

            Map<String, Object> actReq = new HashMap<>();
            actReq.put("action", "CHOOSE_ACTION");
            actReq.put("choices", Arrays.asList("HU", "SKIP"));
            actReq.put("tile", drawn.toString());
            send(players.get(currentPlayerIndex), new Packet(Command.ACTION_REQUEST, actReq));

            broadcastMessage("Game", "Player " + currentPlayerIndex + " is deciding on Self-Draw...");
            return;
        }

        // ✅ P1-1: 檢查暗槓（在自摸檢查之後）
        List<Tile> concealedKongOptions = processor.getConcealedKongOptions(finalHand);
        
        if (!concealedKongOptions.isEmpty()) {
            List<String> actions = new ArrayList<>();
            for (Tile tile : concealedKongOptions) {
                actions.add("CONCEALED_KONG " + tile.toString());
            }
            actions.add("SKIP");
            
            waitingForAction = true;
            pendingResponses.clear();
            pendingResponses.add(currentPlayerIndex);
            currentActionGroup = new ActionGroup(0);
            for (String action : actions) {
                currentActionGroup.addAction(currentPlayerIndex, action);
            }
            
            Map<String, Object> actReq = new HashMap<>();
            actReq.put("action", "CHOOSE_ACTION");
            actReq.put("choices", actions);
            actReq.put("tile", "");
            send(players.get(currentPlayerIndex), new Packet(Command.ACTION_REQUEST, actReq));
            
            broadcastMessage("Game", "Player " + currentPlayerIndex + " can Concealed Kong...");
            return;
        }
        
        // ✅ 修復：如果沒有自摸和暗槓，明確提示玩家可以出牌
        // If no Tsumo and no Concealed Kong, user just plays a card
        Map<String, Object> discardMsg = new HashMap<>();
        discardMsg.put("message", "請出牌");
        send(players.get(currentPlayerIndex), new Packet(Command.GAME_UPDATE, discardMsg));
        } catch (Exception e) {
            logger.error("Error in startTurn for Player {}", currentPlayerIndex, e);
            broadcastMessage("System", "Error in startTurn: " + e.getMessage());
        }
    }

    /**
     * 監測玩家手牌狀態（聽牌和胡牌）
     * 在關鍵時刻調用此方法來檢查手牌狀態
     */
    private void monitorHandStatus(int playerIndex) {
        PlayerHand hand = hands.get(playerIndex);
        int totalTiles = hand.getConnectionCount();
        
        // 檢查是否為胡牌（14 張或 17 張）
        if (totalTiles == 14 || totalTiles == 17) {
            if (tingDetector.isWinningHand(hand)) {
                logger.debug("Player {} has a winning hand!", playerIndex);
                // 如果當前輪到該玩家，且不在等待動作狀態，則提示自摸
                if (playerIndex == currentPlayerIndex && !waitingForAction) {
                    logger.debug("Player {} can Self-Draw HU!", playerIndex);
                    // 觸發自摸檢查
                    checkSelfDrawWin(playerIndex);
                }
            }
        }
        
        // 檢查聽牌狀態（13 張或 14 張）
        if (totalTiles == 13 || totalTiles == 14) {
            TingDetector.TingResult tingResult = tingDetector.detectTing(hand);
            if (tingResult.isTing()) {
                logger.debug("Player {} is Ting, waiting for: {}", 
                    playerIndex, tingResult.getTingTiles());
            }
        }
    }
    
    /**
     * 檢查自摸胡牌（用於監測機制）
     */
    private void checkSelfDrawWin(int playerIndex) {
        PlayerHand hand = hands.get(playerIndex);
        if (tingDetector.isWinningHand(hand)) {
            logger.debug("Player {} can Self-Draw HU!", playerIndex);
            
            // 創建 ActionGroup 提示玩家選擇
            currentActionGroup = new ActionGroup(0); // Priority 0 (Highest)
            currentActionGroup.addAction(playerIndex, "HU");
            
            waitingForAction = true;
            pendingResponses.clear();
            pendingResponses.add(playerIndex);
            
            Map<String, Object> actReq = new HashMap<>();
            actReq.put("action", "CHOOSE_ACTION");
            actReq.put("choices", Arrays.asList("HU", "SKIP"));
            actReq.put("tile", "");
            send(players.get(playerIndex), new Packet(Command.ACTION_REQUEST, actReq));
            
            broadcastMessage("Game", "Player " + playerIndex + " can HU! Choose to HU or continue playing.");
        }
    }

    private void broadcastState() {
        // We construct a specific view for EACH player
        // because they should not see opponents' hands.

        for (int i = 0; i < 4; i++) {
            Map<String, Object> state = new HashMap<>();
            state.put("action", "STATE_UPDATE");
            state.put("myIndex", i);
            state.put("turnIndex", currentPlayerIndex);
            state.put("sea", sea);

            // My Hand (Standing)
            state.put("myHand", hands.get(i).getTilesStr());
            // Melds (Shared visibility? Actually everyone sees everyone's melds)
            // Ideally we send everyone's melds.
            // For now, let's just make sure WE see our melds.
            // Actually, Opponents needs to see my melds too.
            // Let's add all players melds to the state.

            List<List<String>> allMelds = new ArrayList<>();
            for (PlayerHand h : hands) {
                allMelds.add(h.getMeldsStr());
            }
            state.put("allMelds", allMelds); // [[M1,M1,M1], [], [Red,Red,Red], []]

            // Opponent Hand Counts (Standing only)
            List<Integer> counts = new ArrayList<>();
            for (PlayerHand h : hands)
                counts.add(h.getTileCount());
            state.put("handCounts", counts);

            state.put("nicknames", getNicknamesList());

            send(players.get(i), new Packet(Command.GAME_UPDATE, state));
        }
    }

    private void broadcastMessage(String sender, String msg) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "[" + sender + "] " + msg);
        broadcast(new Packet(Command.GAME_UPDATE, data));
    }

    private List<String> getNicknamesList() {
        List<String> names = new ArrayList<>();
        for (WebSocket ws : players)
            names.add(nickNames.get(ws));
        return names;
    }

    private void broadcast(Packet packet) {
        try {
            String json = mapper.writeValueAsString(packet);
            for (WebSocket ws : players)
                ws.send(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void send(WebSocket ws, Packet packet) {
        try {
            ws.send(mapper.writeValueAsString(packet));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
