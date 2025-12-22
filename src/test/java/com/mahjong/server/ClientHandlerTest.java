package com.mahjong.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientHandlerTest {

    @Test
    public void testHandleLoginPacket() throws IOException {
        // Mock Socket and Server
        Socket mockSocket = mock(Socket.class);
        GameServer mockServer = mock(GameServer.class);

        // Prepare Mock Input/Output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        
        // Packet to receive: LOGIN
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "TestUser");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String jsonInput = new ObjectMapper().writeValueAsString(loginPacket);
        
        // DataInputStream reads UTF. writeUTF writes 2-byte length then utf bytes.
        ByteArrayOutputStream inputBuffer = new ByteArrayOutputStream();
        new java.io.DataOutputStream(inputBuffer).writeUTF(jsonInput);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBuffer.toByteArray());

        when(mockSocket.getInputStream()).thenReturn(inputStream);
        when(mockSocket.getOutputStream()).thenReturn(outContent);

        ClientHandler handler = new ClientHandler(mockSocket, mockServer);
        
        // Run handler in a thread or just call run()? 
        // calling run() block until loop finishes or exception.
        // We need to break the loop. 
        // ClientHandler loop: while(running). 
        // We can close handler after 1 packet?
        // Let's modify ClientHandler to be testable or run in separate thread and interrupt?
        // Or refactor handlePacket extraction.
        
        // Refactoring strictly for testability is good practice.
        // But here I'll try to just start it in a thread and close it quickly.
        
        Thread t = new Thread(handler);
        t.start();
        
        try {
            Thread.sleep(500); // Wait for processing
        } catch (InterruptedException e) {}
        
        handler.close();
        
        // Verify Server broadcast called
        verify(mockServer, atLeastOnce()).broadcast(contains("TestUser"));
    }
}
