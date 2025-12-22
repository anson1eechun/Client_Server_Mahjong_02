package com.mahjong.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MahjongClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile boolean running = true;
    private Consumer<Packet> onPacketReceived;
    
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        
        new Thread(this::listen).start();
    }
    
    public void setOnPacketReceived(Consumer<Packet> listener) {
        this.onPacketReceived = listener;
    }

    private void listen() {
        try {
            while (running) {
                String json = in.readUTF();
                Packet packet = mapper.readValue(json, Packet.class);
                if (onPacketReceived != null) {
                    onPacketReceived.accept(packet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public void send(Packet packet) {
        try {
            String json = mapper.writeValueAsString(packet);
            out.writeUTF(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // API Wrappers
    public void login(String nickname) {
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", nickname);
        send(new Packet(Command.LOGIN, data));
    }
}
