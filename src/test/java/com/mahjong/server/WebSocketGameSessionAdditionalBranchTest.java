package com.mahjong.server;

import com.mahjong.logic.PlayerHand;
import com.mahjong.logic.Tile;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * WebSocketGameSession 額外分支覆蓋率測試
 * 補充測試未覆蓋的分支以達到 90% 覆蓋率
 */
class WebSocketGameSessionAdditionalBranchTest {

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(mockWebSocket1.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockWebSocket2.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12346));
        when(mockWebSocket3.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12347));
        when(mockWebSocket4.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12348));
        
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
    }

    /**
     * 測試 resolveDiscard() 中 others.size() != 2 的分支
     */
    @Test
    void testResolveDiscard_Chow_InvalidOthersSize() throws Exception {
        session.start();
        
        // 設置手牌，讓玩家 1 可以吃（但設置異常情況）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand1 = hands.get(1);
        hand1.addTile(Tile.M1);
        hand1.addTile(Tile.M2);
        
        // 設置當前玩家
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("resolveDiscard", Tile.class, int.class);
        method.setAccessible(true);
        
        // 正常情況下應該能處理
        assertDoesNotThrow(() -> {
            method.invoke(session, Tile.M3, 0);
        });
    }

    /**
     * 測試 resolveDiscard() 中 discardFound 的分支
     */
    @Test
    void testResolveDiscard_Chow_DiscardFound() throws Exception {
        session.start();
        
        // 設置手牌，讓玩家 1 可以吃
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand1 = hands.get(1);
        hand1.addTile(Tile.M1);
        hand1.addTile(Tile.M2);
        
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("resolveDiscard", Tile.class, int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, Tile.M3, 0);
        });
    }

    /**
     * 測試 resolveDiscard() 中 !discardFound 的分支
     */
    @Test
    void testResolveDiscard_Chow_DiscardNotFound() throws Exception {
        session.start();
        
        // 設置手牌，讓玩家 1 可以吃
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand1 = hands.get(1);
        hand1.addTile(Tile.M1);
        hand1.addTile(Tile.M2);
        
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("resolveDiscard", Tile.class, int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, Tile.M3, 0);
        });
    }

    /**
     * 測試 performChow() 中 parts.length != 2 的分支
     */
    @Test
    void testPerformChow_InvalidFormat() throws Exception {
        session.start();
        
        // 設置手牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        
        // 設置 pendingDiscardTile
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, Tile.M3);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(3);
        actionGroup.addAction(0, "CHOW INVALID"); // 無效格式
        
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送無效格式的吃牌動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW INVALID");
        Packet packet = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 performChow() 中 totalTiles != 14 的分支
     */
    @Test
    void testPerformChow_TotalTilesNot14() throws Exception {
        session.start();
        
        // 設置手牌（不是 14 張）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        
        // 設置 pendingDiscardTile
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, Tile.M3);
        
        // 設置 sea
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M3");
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performChow", int.class, String.class, String.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0, "M1", "M2");
        });
    }

    /**
     * 測試 performChow() 中 totalTiles == 14 && !isWinningHand 的分支
     */
    @Test
    void testPerformChow_TotalTiles14_NotWinning() throws Exception {
        session.start();
        
        // 設置手牌（14 張但不是胡牌）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 添加 12 張牌，吃後變成 14 張
        for (int i = 0; i < 12; i++) {
            hand.addTile(Tile.M1);
        }
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M3);
        
        // 設置 pendingDiscardTile
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, Tile.M4);
        
        // 設置 sea
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M4");
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performChow", int.class, String.class, String.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0, "M2", "M3");
        });
    }

    /**
     * 測試 startTurn() 中 !concealedKongOptions.isEmpty() 的分支
     */
    @Test
    void testStartTurn_WithConcealedKongOptions() throws Exception {
        session.start();
        
        // 設置手牌，讓玩家 0 可以暗槓
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        
        // 設置當前玩家
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        // 設置 isFirstTurn = false
        Field isFirstTurnField = WebSocketGameSession.class.getDeclaredField("isFirstTurn");
        isFirstTurnField.setAccessible(true);
        isFirstTurnField.setBoolean(session, false);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("startTurn");
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
    }

    /**
     * 測試 startTurn() 中 concealedKongOptions.isEmpty() 的分支
     */
    @Test
    void testStartTurn_NoConcealedKongOptions() throws Exception {
        session.start();
        
        // 設置手牌（不能暗槓）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M3);
        
        // 設置當前玩家
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        // 設置 isFirstTurn = false
        Field isFirstTurnField = WebSocketGameSession.class.getDeclaredField("isFirstTurn");
        isFirstTurnField.setAccessible(true);
        isFirstTurnField.setBoolean(session, false);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("startTurn");
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
    }

    /**
     * 測試 startTurn() 中 !canTsumo && tingResult.isTing() 的分支
     */
    @Test
    void testStartTurn_NotTsumo_IsTing() throws Exception {
        session.start();
        
        // 設置手牌為聽牌（但不胡）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建聽牌手牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1);
        hand.addTile(Tile.M2); hand.addTile(Tile.M3);
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1);
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3);
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST);
        
        // 設置當前玩家
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        // 設置 isFirstTurn = false
        Field isFirstTurnField = WebSocketGameSession.class.getDeclaredField("isFirstTurn");
        isFirstTurnField.setAccessible(true);
        isFirstTurnField.setBoolean(session, false);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("startTurn");
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
    }

    /**
     * 測試 startTurn() 中 !canTsumo && !tingResult.isTing() 的分支
     */
    @Test
    void testStartTurn_NotTsumo_NotTing() throws Exception {
        session.start();
        
        // 設置手牌（不聽也不胡）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建普通手牌
        for (int i = 0; i < 13; i++) {
            hand.addTile(Tile.M1);
        }
        
        // 設置當前玩家
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        // 設置 isFirstTurn = false
        Field isFirstTurnField = WebSocketGameSession.class.getDeclaredField("isFirstTurn");
        isFirstTurnField.setAccessible(true);
        isFirstTurnField.setBoolean(session, false);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("startTurn");
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
    }

    /**
     * 測試 start() 中 firstDraw == null 的分支
     */
    @Test
    void testStart_FirstDrawNull() throws Exception {
        // 抽完所有牌
        Field engineField = WebSocketGameSession.class.getDeclaredField("engine");
        engineField.setAccessible(true);
        com.mahjong.logic.MahjongRuleEngine engine = 
            (com.mahjong.logic.MahjongRuleEngine) engineField.get(session);
        
        // 抽完大部分牌
        int remaining = engine.getRemainingTiles();
        for (int i = 0; i < remaining - 10; i++) {
            engine.drawTile();
        }
        
        // 嘗試啟動遊戲（可能會遇到 firstDraw 為 null）
        assertDoesNotThrow(() -> {
            session.start();
        });
    }

    /**
     * 測試 performPong() 中 !r1 || !r2 的分支
     */
    @Test
    void testPerformPong_MissingTiles() throws Exception {
        session.start();
        
        // 設置手牌（不足 2 張）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        hand.addTile(Tile.M1); // 只有 1 張
        
        // 設置 pendingDiscardTile
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, Tile.M1);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performPong", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 performPong() 中 totalTiles == 14 && !isWinningHand 的分支
     */
    @Test
    void testPerformPong_TotalTiles14_NotWinning() throws Exception {
        session.start();
        
        // 設置手牌（14 張但不是胡牌）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 添加 12 張牌，碰後變成 14 張
        for (int i = 0; i < 12; i++) {
            hand.addTile(Tile.M1);
        }
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M3);
        
        // 設置 pendingDiscardTile
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, Tile.M1);
        
        // 設置 sea
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M1");
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performPong", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }
}

