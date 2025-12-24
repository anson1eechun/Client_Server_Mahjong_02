package com.mahjong.server;

import com.mahjong.logic.MahjongRuleEngine;
import com.mahjong.logic.PlayerHand;
import com.mahjong.logic.Tile;
import com.mahjong.logic.HandValidator;
import com.mahjong.logic.Meld;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;
import org.java_websocket.WebSocket;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class WebSocketGameSession {
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
        System.out.println("Session Starting...");
        // 1. Shuffle
        engine.shuffle();

        // 2. Deal
        engine.dealInitialHands(hands);

        // 3. Notify Game Start
        broadcast(new Packet(Command.GAME_START, null));

        // 4. Send Initial State
        broadcastState();

        // 5. Start First Turn (East)
        startTurn();
    }

    private final com.mahjong.logic.ActionProcessor processor = new com.mahjong.logic.ActionProcessor();

    // START: Action Logic Fields
    private final LinkedList<ActionGroup> actionQueue = new LinkedList<>();
    private ActionGroup currentActionGroup = null;
    private final Set<Integer> pendingResponses = new HashSet<>();
    private boolean waitingForAction = false;
    private Tile pendingDiscardTile = null;
    private final HandValidator validator = new HandValidator(); // Kept for Tsumo check or remove if not needed?
    // END: Action Logic Fields
    
    public void processPlayerAction(WebSocket conn, Packet packet) {
        int playerIndex = players.indexOf(conn);

        // Handling Action Response (Pong/Skip) -- Must happen even if not current player
        if (waitingForAction) {
            handleActionResponse(playerIndex, packet);
            return;
        }

        if (playerIndex != currentPlayerIndex) {
            return;
        }

        Command cmd = packet.getCommand();
        if (cmd == Command.PLAY_CARD) {
            String tileStr = (String) packet.getData().get("tile");
            Tile tile = Tile.valueOf(tileStr);

            // Logic: Remove from hand, Add to Sea
            boolean removed = hands.get(playerIndex).removeTile(tileStr);
            if (removed) {
                sea.add(tileStr);
                hands.get(playerIndex).sort();
                broadcastState();

                resolveDiscard(tile, playerIndex);
            }
        }
    }

    private void resolveDiscard(Tile discard, int discarderIdx) {
        actionQueue.clear();
        currentActionGroup = null;
        pendingResponses.clear();

        ActionGroup tierHu = new ActionGroup(1);
        ActionGroup tierPong = new ActionGroup(2);
        ActionGroup tierChow = new ActionGroup(3);

        // Use ActionProcessor to get all valid actions
        List<com.mahjong.logic.ActionProcessor.Action> allActions = processor.checkPossibleActions(hands, discard,
                discarderIdx, currentPlayerIndex);

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
            nextTurn();
        } else {
            pendingDiscardTile = discard;
            processNextActionGroup();
        }
    }

    private synchronized void processNextActionGroup() {
        if (actionQueue.isEmpty()) {
            System.out.println("[DEBUG] Action Queue empty. Moving to next turn.");
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

        System.out.println("[DEBUG] Processing Action Group. Priority: " + currentActionGroup.priority
                + ", Players: " + currentActionGroup.players);

        // Send Requests
        for (Integer pIdx : currentActionGroup.players) {
            List<String> actions = currentActionGroup.playerActions.get(pIdx);

            System.out.println("Asking Player " + pIdx + " for actions: " + actions);

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
            System.out.println("[DEBUG] Ignore ActionResponse from P" + playerIndex + " (Not waiting)");
            return;
        }
        if (!pendingResponses.contains(playerIndex)) {
            System.out.println("[DEBUG] Ignore ActionResponse from P" + playerIndex + " (Not in pending list "
                    + pendingResponses + ")");
            return;
        }

        Command cmd = packet.getCommand();
        if (cmd == Command.ACTION) {
            String type = (String) packet.getData().get("type"); // Chosen action or SKIP
            System.out.println("[DEBUG] Received Action: " + type + " from Player " + playerIndex);

            if ("SKIP".equals(type)) {
                broadcastMessage("Game", "Player " + playerIndex + " skipped.");
                pendingResponses.remove(playerIndex);
                System.out.println("[DEBUG] Pending responses remaining: " + pendingResponses);

                if (pendingResponses.isEmpty()) {
                    // Everyone in this group skipped/resolved.

                    // SPECIAL CASE: Priority 0 (Self-Draw) Skip
                    if (currentActionGroup.priority == 0) {
                        System.out.println("[DEBUG] Player skipped Self-Draw. Resuming discard phase.");
                        waitingForAction = false;
                        currentActionGroup = null;
                        // Do NOT call nextTurn() or processNextActionGroup()
                        // User is now free to discard via PLAY_CARD command
                    } else {
                        // Regular Discard Response Skip
                        System.out.println("[DEBUG] Group resolved (All skipped). Moving to next group.");
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
                } else {
                    System.err.println("[ERROR] Player " + playerIndex + " tried invalid action " + type);
                }
            }
        }
    }

    private synchronized void performHu(int playerIndex) {
        try {
            broadcastMessage("Game", "Player " + playerIndex + " HU! Game Over.");

            // Add the winning tile to hand for display
            PlayerHand hand = hands.get(playerIndex);
            hand.addTile(pendingDiscardTile);

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

    private synchronized void performChow(int playerIndex, String t1Name, String t2Name) {
        try {
            PlayerHand hand = hands.get(playerIndex);
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

                broadcastState();
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

            broadcastMessage("Game", "Player " + playerIndex + " PONG!");

            // 4. IMPORTANT: Pong -> No Draw -> Must Discard
            broadcastState();

        } catch (Exception e) {
            e.printStackTrace();
            broadcastMessage("System", "Error performing Pong: " + e.getMessage());
        }
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % 4;
        startTurn();
    }

    private void startTurn() {
        // Draw tile for current player
        Tile drawn = engine.drawTile();
        if (drawn == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Game Over - Wall Empty!");
            broadcast(new Packet(Command.GAME_OVER, data));
            return;
        }

        PlayerHand finalHand = hands.get(currentPlayerIndex);
        finalHand.addTile(drawn);

        // Notify everyone
        broadcastState();

        // Send specific "You Drew X" message
        Map<String, Object> drawMsg = new HashMap<>();
        drawMsg.put("action", "DRAW");
        drawMsg.put("tile", drawn.toString());
        send(players.get(currentPlayerIndex), new Packet(Command.GAME_UPDATE, drawMsg));

        System.out.println("Turn: Player " + currentPlayerIndex + " drew " + drawn);

        // --- CHECK SELF-DRAW WIN (Tsumo) ---
        // Per game_rules.md: [Check Win] immediately after draw
        // Actually canHu checks "if I add this tile".
        // We already added it. Validator expects hand WITHOUT the tile usually?
        // Let's check Validator.canHu doc: "temporary add... and check".
        // The hand ALREADY has the tile. So we should remove it before calling canHu,
        // or update canHu to check existing hand.
        // Validator.canHu: `hand.addTile(discard); boolean wins = ...;
        // hand.removeTile(discard);`
        // So it EXPECTS the tile to NOT be in hand.

        // Correct usage for Tsumo:
        finalHand.removeTile(drawn.toString()); // Temporarily remove
        boolean canTsumo = validator.canHu(finalHand, drawn);
        finalHand.addTile(drawn); // Put it back

        if (canTsumo) {
            System.out.println("[DEBUG] Player " + currentPlayerIndex + " can Self-Draw HU!");

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
        }
        // If no Tsumo, user just plays a card (client waits for click)
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
