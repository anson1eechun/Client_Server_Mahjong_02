package com.mahjong.server;

import com.mahjong.model.Command;
import com.mahjong.model.Packet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServer {
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private volatile boolean running = true;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        Packet p = new Packet(Command.GAME_UPDATE, data); // Broadcasting as generic update
        
        for (ClientHandler client : clients) {
            client.sendPacket(p);
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }
    
    public static void main(String[] args) {
        new GameServer().start(8888);
    }
}
