package com.mahjong.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MahjongWebSocketServer extends WebSocketServer {
    private final ObjectMapper mapper = new ObjectMapper();
    // Map WebSocket connection to Nickname
    private final Map<WebSocket, String> players = new ConcurrentHashMap<>();

    private final List<WebSocket> waitingQueue = new ArrayList<>();
    private WebSocketGameSession currentSession;

    public MahjongWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
        String nickname = players.remove(conn);
        waitingQueue.remove(conn);
        if (nickname != null) {
            broadcastMessage("Server", nickname + " has left the game.");
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // System.out.println("Received: " + message);
        try {
            Packet packet = mapper.readValue(message, Packet.class);
            handlePacket(conn, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Mahjong WebSocket Server started on port: " + getPort());
    }

    private void handlePacket(WebSocket conn, Packet packet) {
        Command cmd = packet.getCommand();
        if (cmd == Command.LOGIN) {
            String nickname = (String) packet.getData().get("nickname");
            players.put(conn, nickname);

            if (!waitingQueue.contains(conn)) {
                waitingQueue.add(conn);
            }

            // Send Login Success
            sendPacket(conn, new Packet(Command.LOGIN_SUCCESS, null));

            // Broadcast join
            broadcastMessage("Server", nickname + " joined! (" + waitingQueue.size() + "/4)");

            // Check if we can start game
            if (waitingQueue.size() == 4) {
                startGame();
            }

        } else if (cmd == Command.PLAY_CARD || cmd == Command.ACTION) {
            if (currentSession != null) {
                currentSession.processPlayerAction(conn, packet);
            }
        }
    }

    private void startGame() {
        try {
            broadcastMessage("Server", "4 Players Ready! Starting Game...");
            // Create copies of list to avoid concurrency issues during modification
            List<WebSocket> sessionPlayers = new ArrayList<>(waitingQueue);

            currentSession = new WebSocketGameSession(sessionPlayers, players);
            currentSession.start();

            // Clear waiting queue as they are now in game
            waitingQueue.clear();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("CRITICAL ERROR Starting Game: " + e.getMessage());
            broadcastMessage("Server", "Error starting game: " + e.getMessage());
        }
    }

    private void broadcastMessage(String sender, String msg) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "[" + sender + "] " + msg);
        broadcast(new Packet(Command.GAME_UPDATE, data));
    }

    private void sendPacket(WebSocket conn, Packet packet) {
        try {
            conn.send(mapper.writeValueAsString(packet));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcast(Packet packet) {
        try {
            String json = mapper.writeValueAsString(packet);
            broadcast(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 8888;
        new MahjongWebSocketServer(port).start();
    }
}
