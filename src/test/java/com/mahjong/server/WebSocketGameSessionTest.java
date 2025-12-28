package com.mahjong.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.logic.PlayerHand;
import com.mahjong.logic.Tile;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 測試 WebSocketGameSession 類別
 * 提升覆蓋率：Instruction, Branch, Line Coverage
 */
class WebSocketGameSessionTest {

    @Mock
    private WebSocket mockWebSocket1;

    @Mock
    private WebSocket mockWebSocket2;

    @Mock
    private WebSocket mockWebSocket3;

    @Mock
    private WebSocket mockWebSocket4;

    private List<WebSocket> players;
    private Map<WebSocket, String> nickNames;
    private WebSocketGameSession session;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 設置 Mock WebSocket
        when(mockWebSocket1.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockWebSocket2.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12346));
        when(mockWebSocket3.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12347));
        when(mockWebSocket4.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12348));
        
        // 設置玩家列表和暱稱
        players = new ArrayList<>();
        players.add(mockWebSocket1);
        players.add(mockWebSocket2);
        players.add(mockWebSocket3);
        players.add(mockWebSocket4);
        
        nickNames = new HashMap<>();
        nickNames.put(mockWebSocket1, "Player1");
        nickNames.put(mockWebSocket2, "Player2");
        nickNames.put(mockWebSocket3, "Player3");
        nickNames.put(mockWebSocket4, "Player4");
        
        session = new WebSocketGameSession(players, nickNames);
        mapper = new ObjectMapper();
    }

    @Test
    void testConstructor() {
        assertNotNull(session);
        WebSocketGameSession newSession = new WebSocketGameSession(players, nickNames);
        assertNotNull(newSession);
    }

    @Test
    void testStart() throws Exception {
        // 測試遊戲開始
        assertDoesNotThrow(() -> {
            session.start();
        });
        
        // 驗證所有玩家都收到了訊息
        for (WebSocket socket : players) {
            verify(socket, atLeastOnce()).send(anyString());
        }
    }

    @Test
    void testProcessPlayerAction_PlayCard_CurrentPlayer() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 為玩家 0 添加一張牌以便出牌
        // 注意：由於手牌是私有的，我們通過 processPlayerAction 來測試
        
        // 測試當前玩家出牌
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        
        // 由於手牌可能沒有 M1，這個測試主要驗證方法不會崩潰
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, playCardPacket);
        });
    }

    @Test
    void testProcessPlayerAction_PlayCard_NotCurrentPlayer() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 測試非當前玩家嘗試出牌
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, playCardPacket);
        });
    }

    @Test
    void testProcessPlayerAction_Action_Skip() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 測試動作回應（跳過）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    @Test
    void testProcessPlayerAction_Action_Hu() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 測試動作回應（胡牌）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "HU");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    @Test
    void testProcessPlayerAction_Action_Pong() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 測試動作回應（碰）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "PONG");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    @Test
    void testProcessPlayerAction_Action_Chow() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 測試動作回應（吃）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW M2,M3");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    @Test
    void testProcessPlayerAction_Action_Kong() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 測試動作回應（槓）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "KONG");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    @Test
    void testProcessPlayerAction_Action_ConcealedKong() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 測試動作回應（暗槓）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CONCEALED_KONG M1");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    @Test
    void testProcessPlayerAction_InvalidCommand() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 測試無效的命令
        Packet invalidPacket = new Packet(Command.LOGIN, null);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, invalidPacket);
        });
    }

    @Test
    void testProcessPlayerAction_NullPacket() {
        // 測試 null 封包（應該拋出 NullPointerException）
        assertThrows(NullPointerException.class, () -> {
            session.processPlayerAction(mockWebSocket1, null);
        });
    }

    @Test
    void testProcessPlayerAction_InvalidPlayer() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 創建一個不在玩家列表中的 WebSocket
        WebSocket invalidSocket = mock(WebSocket.class);
        
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(invalidSocket, playCardPacket);
        });
    }

    @Test
    void testProcessPlayerAction_PlayCard_WithWaitingAction() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 模擬等待動作的狀態（通過發送動作請求）
        // 然後嘗試出牌
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, playCardPacket);
        });
    }

    @Test
    void testProcessPlayerAction_PlayCard_TileNotFound() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 嘗試出一個不在手牌中的牌
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M9"); // 可能不在手牌中
        Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, playCardPacket);
        });
    }

    @Test
    void testBroadcastState() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // broadcastState 應該在 start() 中被調用
        // 驗證所有玩家都收到了狀態更新
        for (WebSocket socket : players) {
            verify(socket, atLeastOnce()).send(anyString());
        }
    }

    @Test
    void testMultipleRounds() throws Exception {
        // 測試多個回合
        session.start();
        
        // 模擬多個玩家出牌
        for (int i = 0; i < 4; i++) {
            final int index = i;
            Map<String, Object> data = new HashMap<>();
            data.put("tile", "M" + (i + 1));
            Packet playCardPacket = new Packet(Command.PLAY_CARD, data);
            final Packet packetFinal = playCardPacket;
            
            assertDoesNotThrow(() -> {
                session.processPlayerAction(players.get(index), packetFinal);
            });
        }
    }

    @Test
    void testActionResponse_WhenNotWaiting() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 在沒有等待動作時發送動作回應
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    @Test
    void testActionResponse_InvalidAction() throws Exception {
        // 先啟動遊戲
        session.start();
        
        // 發送無效的動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "INVALID_ACTION");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    @Test
    void testGameFlow_StartToEnd() throws Exception {
        // 測試完整的遊戲流程
        session.start();
        
        // 驗證遊戲已啟動
        for (WebSocket socket : players) {
            verify(socket, atLeastOnce()).send(anyString());
        }
        
        // 模擬一些遊戲動作
        Map<String, Object> playData = new HashMap<>();
        playData.put("tile", "M1");
        Packet playCard = new Packet(Command.PLAY_CARD, playData);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, playCard);
        });
    }
}

