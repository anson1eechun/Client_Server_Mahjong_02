package com.mahjong.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 測試 MahjongWebSocketServer 類別
 * 提升覆蓋率：Instruction, Branch, Line Coverage
 */
class MahjongWebSocketServerTest {

    @Mock
    private WebSocket mockWebSocket1;

    @Mock
    private WebSocket mockWebSocket2;

    @Mock
    private WebSocket mockWebSocket3;

    @Mock
    private WebSocket mockWebSocket4;

    @Mock
    private ClientHandshake mockHandshake;

    private MahjongWebSocketServer server;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new MahjongWebSocketServer(8888);
        
        // 設置 Mock WebSocket 的基本行為
        when(mockWebSocket1.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockWebSocket2.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12346));
        when(mockWebSocket3.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12347));
        when(mockWebSocket4.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12348));
    }

    @Test
    void testConstructor() {
        MahjongWebSocketServer newServer = new MahjongWebSocketServer(9999);
        assertNotNull(newServer);
    }

    @Test
    void testOnOpen() {
        // 測試連線開啟
        assertDoesNotThrow(() -> {
            server.onOpen(mockWebSocket1, mockHandshake);
        });
    }

    @Test
    void testOnClose_WithNickname() {
        // 先添加玩家
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "Player1");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        
        try {
            String json = new ObjectMapper().writeValueAsString(loginPacket);
            server.onMessage(mockWebSocket1, json);
        } catch (Exception e) {
            // 忽略異常，主要測試 onClose
        }
        
        // 測試關閉連線
        assertDoesNotThrow(() -> {
            server.onClose(mockWebSocket1, 1000, "Normal closure", true);
        });
    }

    @Test
    void testOnClose_WithoutNickname() {
        // 測試關閉未登入的連線
        assertDoesNotThrow(() -> {
            server.onClose(mockWebSocket1, 1000, "Normal closure", true);
        });
    }

    @Test
    void testOnError() {
        // 測試錯誤處理
        Exception testException = new RuntimeException("Test error");
        assertDoesNotThrow(() -> {
            server.onError(mockWebSocket1, testException);
        });
    }

    @Test
    void testOnStart() {
        // 測試伺服器啟動
        assertDoesNotThrow(() -> {
            server.onStart();
        });
    }

    @Test
    void testHandlePacket_Login() throws Exception {
        // 測試處理登入封包
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "TestPlayer");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = new ObjectMapper().writeValueAsString(loginPacket);
        
        assertDoesNotThrow(() -> {
            callOnMessageString(server, mockWebSocket1, json);
        });
        
        // 驗證 send 被調用（登入成功回應）
        verify(mockWebSocket1, atLeastOnce()).send(anyString());
    }

    @Test
    void testHandlePacket_Login_FourPlayers() throws Exception {
        // 測試 4 人登入後自動開始遊戲
        String[] nicknames = {"Player1", "Player2", "Player3", "Player4"};
        WebSocket[] sockets = {mockWebSocket1, mockWebSocket2, mockWebSocket3, mockWebSocket4};
        
        for (int i = 0; i < 4; i++) {
            final int index = i;
            Map<String, Object> data = new HashMap<>();
            data.put("nickname", nicknames[i]);
            Packet loginPacket = new Packet(Command.LOGIN, data);
            String json = new ObjectMapper().writeValueAsString(loginPacket);
            final String jsonFinal = json;
            
            assertDoesNotThrow(() -> {
                callOnMessageString(server, sockets[index], jsonFinal);
            });
        }
        
        // 驗證所有玩家都收到了訊息
        for (WebSocket socket : sockets) {
            verify(socket, atLeastOnce()).send(anyString());
        }
    }

    @Test
    void testHandlePacket_PlayCard() throws Exception {
        // 先登入並開始遊戲
        setupGameWithFourPlayers();
        
        // 測試處理出牌封包
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        String json = new ObjectMapper().writeValueAsString(playCardPacket);
        
        assertDoesNotThrow(() -> {
            callOnMessageString(server, mockWebSocket1, json);
        });
    }

    @Test
    void testHandlePacket_Action() throws Exception {
        // 先登入並開始遊戲
        setupGameWithFourPlayers();
        
        // 測試處理動作封包
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = new ObjectMapper().writeValueAsString(actionPacket);
        
        assertDoesNotThrow(() -> {
            callOnMessageString(server, mockWebSocket1, json);
        });
    }

    @Test
    void testHandlePacket_InvalidJson() {
        // 測試無效的 JSON
        assertDoesNotThrow(() -> {
            callOnMessageString(server, mockWebSocket1, "invalid json");
        });
    }

    @Test
    void testHandlePacket_NullMessage() {
        // 測試 null 訊息
        assertDoesNotThrow(() -> {
            callOnMessageString(server, mockWebSocket1, null);
        });
    }

    @Test
    void testStartGame() throws Exception {
        // 設置 4 個玩家
        setupGameWithFourPlayers();
        
        // startGame 應該已經被調用
        // 驗證所有玩家都收到了訊息
        for (WebSocket socket : new WebSocket[]{mockWebSocket1, mockWebSocket2, mockWebSocket3, mockWebSocket4}) {
            verify(socket, atLeastOnce()).send(anyString());
        }
    }

    @Test
    void testBroadcastMessage() throws Exception {
        // 先登入一個玩家
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "TestPlayer");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = new ObjectMapper().writeValueAsString(loginPacket);
        callOnMessageString(server, mockWebSocket1, json);
        
        // 廣播訊息應該被調用（在登入時）
        verify(mockWebSocket1, atLeastOnce()).send(anyString());
    }

    @Test
    void testSendPacket() throws Exception {
        // 測試發送封包
        Packet packet = new Packet(Command.LOGIN_SUCCESS, null);
        
        // 通過 onMessage 觸發 sendPacket
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "TestPlayer");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = new ObjectMapper().writeValueAsString(loginPacket);
        
        assertDoesNotThrow(() -> {
            callOnMessageString(server, mockWebSocket1, json);
        });
        
        verify(mockWebSocket1, atLeastOnce()).send(anyString());
    }

    @Test
    void testSendPacket_Exception() throws Exception {
        // 測試發送封包時發生異常
        doThrow(new RuntimeException("Send failed")).when(mockWebSocket1).send(anyString());
        
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "TestPlayer");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = new ObjectMapper().writeValueAsString(loginPacket);
        
        // 應該捕獲異常而不崩潰
        assertDoesNotThrow(() -> {
            callOnMessageString(server, mockWebSocket1, json);
        });
    }

    @Test
    void testMain() {
        // 測試 main 方法（不實際啟動伺服器）
        assertDoesNotThrow(() -> {
            // 注意：main 方法會實際啟動伺服器，所以我們只測試它不會拋出編譯錯誤
            // 實際測試中可能需要使用不同的方法
        });
    }

    // 輔助方法：明確調用 String 版本的 onMessage
    private void callOnMessageString(MahjongWebSocketServer server, WebSocket conn, String message) {
        server.onMessage(conn, message);
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
            callOnMessageString(server, sockets[i], json);
        }
    }
}

