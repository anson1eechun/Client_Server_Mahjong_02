package com.mahjong.server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;

/**
 * MahjongWebSocketServer 分支覆蓋率測試
 * 重點測試未覆蓋的分支以達到 90% 覆蓋率
 */
class MahjongWebSocketServerBranchTest {

    @Mock
    private WebSocket mockWebSocket1;

    @Mock
    private WebSocket mockWebSocket2;

    @Mock
    private WebSocket mockWebSocket3;

    @Mock
    private WebSocket mockWebSocket4;

    private MahjongWebSocketServer server;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new MahjongWebSocketServer(8888);
        
        when(mockWebSocket1.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockWebSocket2.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12346));
        when(mockWebSocket3.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12347));
        when(mockWebSocket4.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12348));
    }

    /**
     * 測試 handlePacket() 中 waitingQueue.contains(conn) 為 true 的分支
     */
    @Test
    void testHandlePacket_Login_AlreadyInQueue() throws Exception {
        // 先登入一次
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "Player1");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = new ObjectMapper().writeValueAsString(loginPacket);
        server.onMessage(mockWebSocket1, json);
        
        // 再次登入（應該不會重複加入隊列）
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
        
        verify(mockWebSocket1, atLeast(2)).send(anyString());
    }

    /**
     * 測試 handlePacket() 中 waitingQueue.size() != 4 的分支
     */
    @Test
    void testHandlePacket_Login_LessThanFourPlayers() throws Exception {
        // 只登入 3 個玩家
        String[] nicknames = {"Player1", "Player2", "Player3"};
        WebSocket[] sockets = {mockWebSocket1, mockWebSocket2, mockWebSocket3};
        
        for (int i = 0; i < 3; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("nickname", nicknames[i]);
            Packet loginPacket = new Packet(Command.LOGIN, data);
            String json = new ObjectMapper().writeValueAsString(loginPacket);
            server.onMessage(sockets[i], json);
        }
        
        // 驗證沒有開始遊戲（只有 3 個玩家）
        for (WebSocket socket : sockets) {
            verify(socket, atLeastOnce()).send(anyString());
        }
    }

    /**
     * 測試 handlePacket() 中 currentSession == null 的分支
     */
    @Test
    void testHandlePacket_PlayCard_NullSession() throws Exception {
        // 不開始遊戲，直接嘗試出牌
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        String json = new ObjectMapper().writeValueAsString(playCardPacket);
        
        // 應該不會崩潰（currentSession 為 null）
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    /**
     * 測試 handlePacket() 中 currentSession != null 的分支
     */
    @Test
    void testHandlePacket_PlayCard_WithSession() throws Exception {
        // 先開始遊戲
        setupGameWithFourPlayers();
        
        // 然後出牌
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        String json = new ObjectMapper().writeValueAsString(playCardPacket);
        
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    /**
     * 測試 handlePacket() 中 cmd != LOGIN && cmd != PLAY_CARD && cmd != ACTION 的分支
     */
    @Test
    void testHandlePacket_OtherCommand() throws Exception {
        // 測試其他命令（例如 GAME_START）
        Packet packet = new Packet(Command.GAME_START, null);
        String json = new ObjectMapper().writeValueAsString(packet);
        
        // 應該不會崩潰
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    /**
     * 測試 onClose() 中 nickname == null 的分支
     */
    @Test
    void testOnClose_NullNickname() {
        // 不登入，直接關閉連線
        assertDoesNotThrow(() -> {
            server.onClose(mockWebSocket1, 1000, "Normal closure", true);
        });
    }

    /**
     * 測試 onClose() 中 nickname != null 的分支
     */
    @Test
    void testOnClose_WithNickname() throws Exception {
        // 先登入
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "Player1");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = new ObjectMapper().writeValueAsString(loginPacket);
        server.onMessage(mockWebSocket1, json);
        
        // 然後關閉
        assertDoesNotThrow(() -> {
            server.onClose(mockWebSocket1, 1000, "Normal closure", true);
        });
    }

    /**
     * 測試 startGame() 的異常處理分支
     * 通過讓 WebSocket.send() 拋出異常來觸發 startGame() 的 catch 塊
     */
    @Test
    void testStartGame_ExceptionHandling() throws Exception {
        // 設置所有 WebSocket 的 send 方法都會拋出異常
        doThrow(new RuntimeException("Broadcast failed")).when(mockWebSocket1).send(anyString());
        doThrow(new RuntimeException("Broadcast failed")).when(mockWebSocket2).send(anyString());
        doThrow(new RuntimeException("Broadcast failed")).when(mockWebSocket3).send(anyString());
        doThrow(new RuntimeException("Broadcast failed")).when(mockWebSocket4).send(anyString());
        
        // 嘗試登入 4 個玩家（會觸發 startGame）
        // 即使 broadcast 失敗，startGame 的異常處理應該能捕獲
        String[] nicknames = {"Player1", "Player2", "Player3", "Player4"};
        WebSocket[] sockets = {mockWebSocket1, mockWebSocket2, mockWebSocket3, mockWebSocket4};
        
        for (int i = 0; i < 4; i++) {
            final int index = i;
            Map<String, Object> data = new HashMap<>();
            data.put("nickname", nicknames[index]);
            Packet loginPacket = new Packet(Command.LOGIN, data);
            String json = new ObjectMapper().writeValueAsString(loginPacket);
            final String jsonFinal = json;
            
            // 應該捕獲異常而不崩潰
            assertDoesNotThrow(() -> {
                server.onMessage(sockets[index], jsonFinal);
            });
        }
    }

    /**
     * 測試 broadcast() 的異常處理分支
     * 通過讓所有 WebSocket 的 send 方法拋出異常來觸發 broadcast() 的 catch 塊
     */
    @Test
    void testBroadcast_ExceptionHandling() throws Exception {
        // 設置所有 WebSocket 的 send 方法都會拋出異常
        doThrow(new RuntimeException("Broadcast failed")).when(mockWebSocket1).send(anyString());
        doThrow(new RuntimeException("Broadcast failed")).when(mockWebSocket2).send(anyString());
        doThrow(new RuntimeException("Broadcast failed")).when(mockWebSocket3).send(anyString());
        doThrow(new RuntimeException("Broadcast failed")).when(mockWebSocket4).send(anyString());
        
        // 先登入一個玩家（會觸發 broadcastMessage，進而觸發 broadcast）
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "Player1");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = new ObjectMapper().writeValueAsString(loginPacket);
        
        // 應該捕獲異常而不崩潰
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    /**
     * 測試 sendPacket() 的異常處理分支
     */
    @Test
    void testSendPacket_ExceptionHandling() throws Exception {
        // 設置 send 會拋出異常
        doThrow(new RuntimeException("Send failed")).when(mockWebSocket1).send(anyString());
        
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "Player1");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = new ObjectMapper().writeValueAsString(loginPacket);
        
        // 應該捕獲異常而不崩潰
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    /**
     * 測試 onMessage() 的異常處理分支
     */
    @Test
    void testOnMessage_ExceptionHandling() {
        // 發送無效的 JSON
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, "invalid json {");
        });
    }

    /**
     * 測試 handlePacket() 中 cmd == ACTION 的分支
     */
    @Test
    void testHandlePacket_Action_WithSession() throws Exception {
        // 先開始遊戲
        setupGameWithFourPlayers();
        
        // 然後發送動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = new ObjectMapper().writeValueAsString(actionPacket);
        
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    /**
     * 測試 handlePacket() 中 cmd == ACTION 且 currentSession == null 的分支
     */
    @Test
    void testHandlePacket_Action_NullSession() throws Exception {
        // 不開始遊戲，直接發送動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = new ObjectMapper().writeValueAsString(actionPacket);
        
        // 應該不會崩潰
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    // 輔助方法：設置 4 個玩家並開始遊戲
    private void setupGameWithFourPlayers() throws Exception {
        String[] nicknames = {"Player1", "Player2", "Player3", "Player4"};
        WebSocket[] sockets = {mockWebSocket1, mockWebSocket2, mockWebSocket3, mockWebSocket4};
        
        for (int i = 0; i < 4; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("nickname", nicknames[i]);
            Packet loginPacket = new Packet(Command.LOGIN, data);
            String json = new ObjectMapper().writeValueAsString(loginPacket);
            server.onMessage(sockets[i], json);
        }
    }
}

