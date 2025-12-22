package com.mahjong.server;

import com.mahjong.logic.MahjongRuleEngine;
import com.mahjong.logic.PlayerHand;
import com.mahjong.logic.Tile;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameSession {
    private final List<ClientHandler> players;
    private final MahjongRuleEngine engine;
    private final List<PlayerHand> hands;
    private int currentPlayerIndex = 0;

    public GameSession(List<ClientHandler> players) {
        this.players = players;
        this.engine = new MahjongRuleEngine(new Random());
        this.hands = new ArrayList<>();
        for (int i=0; i<4; i++) hands.add(new PlayerHand());
    }

    public void start() {
        // Shuffle and Deal
        engine.shuffle();
        engine.dealInitialHands(hands);

        // Notify Game Start
        broadcast(new Packet(Command.GAME_START, null));

        // Send initial hands to players
        for (int i = 0; i < 4; i++) {
            Map<String, Object> data = new HashMap<>();
            // TODO: Serialize Hand structure properly (List of Tuples or strings?)
            // For now, simplified string representation
            data.put("hand", hands.get(i).toString());
            players.get(i).sendPacket(new Packet(Command.GAME_UPDATE, data));
        }

        // Start first turn
        startTurn();
    }

    private void startTurn() {
        // Draw tile
        Tile drawn = engine.drawTile();
        if (drawn == null) {
            broadcast(new Packet(Command.GAME_OVER, Map.of("reason", "Draw Game (Wall Empty)")));
            return;
        }

        PlayerHand currentHand = hands.get(currentPlayerIndex);
        currentHand.addTile(drawn);
        
        // Notify player
        ClientHandler currentPlayer = players.get(currentPlayerIndex);
        Map<String, Object> data = new HashMap<>();
        data.put("action", "DRAW");
        data.put("tile", drawn.toString());
        currentPlayer.sendPacket(new Packet(Command.GAME_UPDATE, data));

        // Wait for discard... (Async handling needed in real implementation)
        // Here we just print state
        System.out.println("Player " + currentPlayerIndex + " drew " + drawn);
    }

    public void broadcast(Packet packet) {
        for (ClientHandler p : players) {
            p.sendPacket(packet);
        }
    }
}
