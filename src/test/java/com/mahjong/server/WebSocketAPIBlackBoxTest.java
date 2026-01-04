package com.mahjong.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * WebSocket API 黑箱測試
 * 
 * 測試目標：
 * 1. 從外部視角測試所有 WebSocket API（Command）
 * 2. 不依賴內部實作細節
 * 3. 測試正常流程和錯誤處理
 * 4. 驗證 API 回應格式
 */
class WebSocketAPIBlackBoxTest {

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
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new MahjongWebSocketServer(8888);
        mapper = new ObjectMapper();

        // 設置 Mock WebSocket 的基本行為
        when(mockWebSocket1.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockWebSocket2.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12346));
        when(mockWebSocket3.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12347));
        when(mockWebSocket4.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12348));
    }

    // ==================== LOGIN Command 測試 ====================

    @Test
    void testLogin_ValidNickname_ShouldReceiveLoginSuccess() throws Exception {
        // 測試：有效的 nickname 應該收到 LOGIN_SUCCESS
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "Player1");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = mapper.writeValueAsString(loginPacket);

        server.onMessage(mockWebSocket1, json);

        // 驗證：應該收到 LOGIN_SUCCESS
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockWebSocket1, atLeastOnce()).send(captor.capture());

        String responseJson = captor.getValue();
        Packet response = mapper.readValue(responseJson, Packet.class);
        assertEquals(Command.LOGIN_SUCCESS, response.getCommand(), 
            "Should receive LOGIN_SUCCESS after valid login");
    }

    @Test
    void testLogin_EmptyNickname_ShouldStillProcess() throws Exception {
        // 測試：空 nickname 的處理（黑箱測試：觀察系統行為）
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = mapper.writeValueAsString(loginPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });

        // 系統應該仍然處理（不崩潰）
        verify(mockWebSocket1, atLeastOnce()).send(anyString());
    }

    @Test
    void testLogin_NullNickname_ShouldStillProcess() throws Exception {
        // 測試：null nickname 的處理
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", null);
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = mapper.writeValueAsString(loginPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testLogin_MissingNicknameField_ShouldStillProcess() throws Exception {
        // 測試：缺少 nickname 欄位
        Map<String, Object> data = new HashMap<>();
        // 不添加 nickname
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = mapper.writeValueAsString(loginPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testLogin_SpecialCharacters_ShouldProcess() throws Exception {
        // 測試：特殊字符 nickname
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "玩家@#$%^&*()");
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = mapper.writeValueAsString(loginPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
        verify(mockWebSocket1, atLeastOnce()).send(anyString());
    }

    @Test
    void testLogin_VeryLongNickname_ShouldProcess() throws Exception {
        // 測試：超長 nickname
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "A".repeat(1000));
        Packet loginPacket = new Packet(Command.LOGIN, data);
        String json = mapper.writeValueAsString(loginPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testLogin_FourPlayers_ShouldStartGame() throws Exception {
        // 測試：4 個玩家登入後應該自動開始遊戲
        WebSocket[] sockets = {mockWebSocket1, mockWebSocket2, mockWebSocket3, mockWebSocket4};
        String[] nicknames = {"Player1", "Player2", "Player3", "Player4"};

        for (int i = 0; i < 4; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("nickname", nicknames[i]);
            Packet loginPacket = new Packet(Command.LOGIN, data);
            String json = mapper.writeValueAsString(loginPacket);
            server.onMessage(sockets[i], json);
        }

        // 驗證：所有玩家都應該收到訊息（包括遊戲開始）
        for (WebSocket socket : sockets) {
            verify(socket, atLeast(2)).send(anyString()); // 至少 LOGIN_SUCCESS 和 GAME_START
        }
    }

    // ==================== PLAY_CARD Command 測試 ====================

    @Test
    void testPlayCard_WithoutLogin_ShouldNotCrash() throws Exception {
        // 測試：未登入就出牌（時序錯誤）
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        String json = mapper.writeValueAsString(playCardPacket);

        // 應該不崩潰（黑箱測試：觀察系統行為）
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testPlayCard_ValidTile_ShouldProcess() throws Exception {
        // 測試：有效的牌名
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        String json = mapper.writeValueAsString(playCardPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testPlayCard_InvalidTile_ShouldProcess() throws Exception {
        // 測試：無效的牌名
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        data.put("tile", "INVALID_TILE");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        String json = mapper.writeValueAsString(playCardPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testPlayCard_MissingTileField_ShouldProcess() throws Exception {
        // 測試：缺少 tile 欄位
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        // 不添加 tile
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        String json = mapper.writeValueAsString(playCardPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testPlayCard_NullTile_ShouldProcess() throws Exception {
        // 測試：null tile
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        data.put("tile", null);
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        String json = mapper.writeValueAsString(playCardPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    // ==================== ACTION Command 測試 ====================

    @Test
    void testAction_SKIP_ShouldProcess() throws Exception {
        // 測試：SKIP 動作
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = mapper.writeValueAsString(actionPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testAction_CHOW_ShouldProcess() throws Exception {
        // 測試：CHOW 動作
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW");
        data.put("tile1", "M1");
        data.put("tile2", "M3");
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = mapper.writeValueAsString(actionPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testAction_PONG_ShouldProcess() throws Exception {
        // 測試：PONG 動作
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        data.put("type", "PONG");
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = mapper.writeValueAsString(actionPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testAction_KONG_ShouldProcess() throws Exception {
        // 測試：KONG 動作
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        data.put("type", "KONG");
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = mapper.writeValueAsString(actionPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testAction_HU_ShouldProcess() throws Exception {
        // 測試：HU 動作
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        data.put("type", "HU");
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = mapper.writeValueAsString(actionPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testAction_InvalidType_ShouldProcess() throws Exception {
        // 測試：無效的動作類型
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        data.put("type", "INVALID_ACTION");
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = mapper.writeValueAsString(actionPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    @Test
    void testAction_MissingTypeField_ShouldProcess() throws Exception {
        // 測試：缺少 type 欄位
        setupGameWithFourPlayers();

        Map<String, Object> data = new HashMap<>();
        // 不添加 type
        Packet actionPacket = new Packet(Command.ACTION, data);
        String json = mapper.writeValueAsString(actionPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, json);
        });
    }

    // ==================== 錯誤處理測試 ====================

    @Test
    void testInvalidJson_ShouldNotCrash() {
        // 測試：無效的 JSON
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, "invalid json {");
        });
    }

    @Test
    void testNullMessage_ShouldNotCrash() {
        // 測試：null 訊息
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, (String) null);
        });
    }

    @Test
    void testEmptyMessage_ShouldNotCrash() {
        // 測試：空訊息
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, "");
        });
    }

    @Test
    void testMalformedPacket_MissingCommand_ShouldNotCrash() {
        // 測試：缺少 command 欄位的封包
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, "{\"data\":{}}");
        });
    }

    @Test
    void testMalformedPacket_InvalidCommand_ShouldNotCrash() {
        // 測試：無效的 command
        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, "{\"command\":\"INVALID_COMMAND\",\"data\":{}}");
        });
    }

    @Test
    void testConnectionLifecycle_OnOpen_ShouldNotCrash() {
        // 測試：連接開啟
        assertDoesNotThrow(() -> {
            server.onOpen(mockWebSocket1, mockHandshake);
        });
    }

    @Test
    void testConnectionLifecycle_OnClose_ShouldNotCrash() {
        // 測試：連接關閉
        assertDoesNotThrow(() -> {
            server.onClose(mockWebSocket1, 1000, "Normal closure", true);
        });
    }

    @Test
    void testConnectionLifecycle_OnError_ShouldNotCrash() {
        // 測試：連接錯誤
        Exception testException = new RuntimeException("Test error");
        assertDoesNotThrow(() -> {
            server.onError(mockWebSocket1, testException);
        });
    }

    @Test
    void testConnectionLifecycle_OnStart_ShouldNotCrash() {
        // 測試：伺服器啟動
        assertDoesNotThrow(() -> {
            server.onStart();
        });
    }

    // ==================== 時序測試 ====================

    @Test
    void testTiming_PlayCardBeforeGameStart_ShouldNotCrash() throws Exception {
        // 測試：遊戲開始前出牌
        // 先登入一個玩家（但遊戲還沒開始）
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("nickname", "Player1");
        Packet loginPacket = new Packet(Command.LOGIN, loginData);
        String loginJson = mapper.writeValueAsString(loginPacket);
        server.onMessage(mockWebSocket1, loginJson);

        // 嘗試出牌（遊戲還沒開始）
        Map<String, Object> playData = new HashMap<>();
        playData.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, playData);
        String playJson = mapper.writeValueAsString(playCardPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, playJson);
        });
    }

    @Test
    void testTiming_ActionBeforeGameStart_ShouldNotCrash() throws Exception {
        // 測試：遊戲開始前執行動作
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("nickname", "Player1");
        Packet loginPacket = new Packet(Command.LOGIN, loginData);
        String loginJson = mapper.writeValueAsString(loginPacket);
        server.onMessage(mockWebSocket1, loginJson);

        // 嘗試執行動作（遊戲還沒開始）
        Map<String, Object> actionData = new HashMap<>();
        actionData.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, actionData);
        String actionJson = mapper.writeValueAsString(actionPacket);

        assertDoesNotThrow(() -> {
            server.onMessage(mockWebSocket1, actionJson);
        });
    }

    // ==================== 輔助方法 ====================

    /**
     * 設置 4 個玩家並開始遊戲
     */
    private void setupGameWithFourPlayers() throws Exception {
        WebSocket[] sockets = {mockWebSocket1, mockWebSocket2, mockWebSocket3, mockWebSocket4};
        String[] nicknames = {"Player1", "Player2", "Player3", "Player4"};

        for (int i = 0; i < 4; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("nickname", nicknames[i]);
            Packet loginPacket = new Packet(Command.LOGIN, data);
            String json = mapper.writeValueAsString(loginPacket);
            server.onMessage(sockets[i], json);
        }

        // 等待遊戲開始（給系統一點時間處理）
        Thread.sleep(100);
    }
}

