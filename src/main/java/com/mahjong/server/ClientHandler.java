package com.mahjong.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameServer server;
    private DataInputStream in;
    private DataOutputStream out;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile boolean running = true;
    private String nickname;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            
            while (running) {
                // Read UTF string (JSON)
                String json = in.readUTF();
                Packet packet = mapper.readValue(json, Packet.class);
                handlePacket(packet);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
            server.removeClient(this);
        } finally {
            close();
        }
    }
    
    private void handlePacket(Packet packet) {
        System.out.println("Received: " + packet);
        Command cmd = packet.getCommand();
        if (cmd == Command.LOGIN) {
            this.nickname = (String) packet.getData().get("nickname");
            sendPacket(new Packet(Command.LOGIN_SUCCESS, null));
            server.broadcast("Player " + nickname + " joined!");
        } 
        // Logic for other commands...
    }

    public void sendPacket(Packet packet) {
        try {
            String json = mapper.writeValueAsString(packet);
            out.writeUTF(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }
    
    public void close() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
