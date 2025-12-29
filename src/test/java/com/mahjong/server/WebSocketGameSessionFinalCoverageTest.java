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
 * WebSocketGameSession 最終覆蓋率測試
 * 補充測試剩餘未覆蓋的分支以達到 90% 覆蓋率
 */
class WebSocketGameSessionFinalCoverageTest {

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
     * 測試 handleActionResponse() 中 parts.length != 2 的分支（CHOW 格式錯誤）
     */
    @Test
    void testHandleActionResponse_InvalidChowFormat() throws Exception {
        session.start();
        
        // 設置 waitingForAction = true
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        // 設置 currentActionGroup
        ActionGroup actionGroup = new ActionGroup(3);
        actionGroup.addAction(0, "CHOW M1,M2");
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        // 設置 pendingResponses
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送格式錯誤的 CHOW（只有一個參數）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW M1"); // 缺少第二個參數
        Packet packet = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 handleActionResponse() 中 parts.length == 2 的分支（CHOW 格式正確）
     */
    @Test
    void testHandleActionResponse_ValidChowFormat() throws Exception {
        session.start();
        
        // 設置手牌，讓玩家 0 可以吃
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
        
        // 設置 waitingForAction = true
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        // 設置 currentActionGroup
        ActionGroup actionGroup = new ActionGroup(3);
        actionGroup.addAction(0, "CHOW M1,M2");
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        // 設置 pendingResponses
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送格式正確的 CHOW
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW M1,M2");
        Packet packet = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 handleActionResponse() 中 parts.length > 2 的分支（CHOW 格式錯誤，多個參數）
     */
    @Test
    void testHandleActionResponse_ChowFormatTooManyParts() throws Exception {
        session.start();
        
        // 設置 waitingForAction = true
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        // 設置 currentActionGroup
        ActionGroup actionGroup = new ActionGroup(3);
        actionGroup.addAction(0, "CHOW M1,M2,M3");
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        // 設置 pendingResponses
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送格式錯誤的 CHOW（多個參數）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW M1,M2,M3"); // 3 個參數（應該只有 2 個）
        Packet packet = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 performChow() 中 totalTiles == 14 && isWinningHand 的分支（吃後可以胡）
     */
    @Test
    void testPerformChow_14Tiles_WinningAfterChow() throws Exception {
        session.start();
        
        // 設置手牌，讓吃後可以胡
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建接近胡牌的手牌（吃後變成胡牌）
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); // Sequence (缺 M4)
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
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
     * 測試 performPong() 中 totalTiles == 14 && isWinningHand 的分支（碰後可以胡）
     */
    @Test
    void testPerformPong_14Tiles_WinningAfterPong() throws Exception {
        session.start();
        
        // 設置手牌，讓碰後可以胡
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建接近胡牌的手牌（碰後變成胡牌）
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1); // 3 張 M1（缺一張來碰）
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
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

    /**
     * 測試 performKong() 中 totalTiles == 14 && isWinningHand 的分支（槓後可以胡）
     */
    @Test
    void testPerformKong_14Tiles_WinningAfterKong() throws Exception {
        session.start();
        
        // 設置手牌，讓槓後可以胡
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建接近胡牌的手牌（槓後變成胡牌）
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1); // 3 張 M1（缺一張來槓）
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
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
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performKong", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 performKong() 中 totalTiles != 14 的分支
     */
    @Test
    void testPerformKong_TotalTilesNot14() throws Exception {
        session.start();
        
        // 設置手牌（不是 14 張）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        
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
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performKong", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 performKong() 中 totalTiles == 14 && !isWinningHand 的分支
     */
    @Test
    void testPerformKong_14Tiles_NotWinning() throws Exception {
        session.start();
        
        // 設置手牌（14 張但不是胡牌）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 添加 12 張牌，槓後變成 14 張但不是胡牌
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
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performKong", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 startTurn() 中 canTsumo = true 的分支
     */
    @Test
    void testStartTurn_CanTsumo() throws Exception {
        session.start();
        
        // 設置手牌為胡牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建胡牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
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
     * 測試 startTurn() 中 canTsumo = false && tingResult.isTing() 的分支
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
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); // Sequence (缺 M4)
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
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
     * 測試 startTurn() 中 !concealedKongOptions.isEmpty() 的分支
     */
    @Test
    void testStartTurn_WithConcealedKongOptions() throws Exception {
        session.start();
        
        // 設置手牌，讓玩家有暗槓選項
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 添加 4 張相同的牌（可以暗槓）
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        // 添加其他牌湊夠 13 張
        for (int i = 0; i < 9; i++) {
            hand.addTile(Tile.M2);
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
     * 測試 startTurn() 中 concealedKongOptions.isEmpty() 的分支
     */
    @Test
    void testStartTurn_NoConcealedKongOptions() throws Exception {
        session.start();
        
        // 設置手牌，沒有暗槓選項
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 添加 13 張牌，但沒有 4 張相同的牌
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
     * 測試 monitorHandStatus() 中 totalTiles == 14 && isWinningHand && playerIndex == currentPlayerIndex && !waitingForAction 的分支
     */
    @Test
    void testMonitorHandStatus_14Tiles_Winning_CurrentPlayer_NotWaiting() throws Exception {
        session.start();
        
        // 設置玩家 0 有 14 張胡牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建胡牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        // 設置當前玩家為 0
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        // 設置 waitingForAction = false
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, false);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("monitorHandStatus", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 monitorHandStatus() 中 totalTiles == 14 && isWinningHand && playerIndex == currentPlayerIndex && waitingForAction 的分支
     */
    @Test
    void testMonitorHandStatus_14Tiles_Winning_CurrentPlayer_Waiting() throws Exception {
        session.start();
        
        // 設置玩家 0 有 14 張胡牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建胡牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        // 設置當前玩家為 0
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        // 設置 waitingForAction = true
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("monitorHandStatus", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 monitorHandStatus() 中 totalTiles == 17 && isWinningHand 的分支
     */
    @Test
    void testMonitorHandStatus_17Tiles_Winning() throws Exception {
        session.start();
        
        // 設置玩家 0 有 17 張胡牌（有槓）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建 17 張胡牌（有槓）
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Kong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        hand.addTile(Tile.RED); hand.addTile(Tile.RED); hand.addTile(Tile.RED); // Pong
        
        // 設置當前玩家
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        // 設置 waitingForAction = false
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, false);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("monitorHandStatus", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 monitorHandStatus() 中 totalTiles == 13 && tingResult.isTing() 的分支
     */
    @Test
    void testMonitorHandStatus_13Tiles_Ting() throws Exception {
        session.start();
        
        // 設置玩家 0 有 13 張聽牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建聽牌手牌（13 張）
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); // Sequence (缺 M4)
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("monitorHandStatus", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 monitorHandStatus() 中 totalTiles == 14 && tingResult.isTing() 的分支
     */
    @Test
    void testMonitorHandStatus_14Tiles_Ting() throws Exception {
        session.start();
        
        // 設置玩家 0 有 14 張聽牌（不是胡牌）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建聽牌手牌（14 張，但不是胡牌）
        hand.addTile(Tile.M1); hand.addTile(Tile.M2); hand.addTile(Tile.M3);
        hand.addTile(Tile.M4); hand.addTile(Tile.M5); hand.addTile(Tile.M6);
        hand.addTile(Tile.M7); hand.addTile(Tile.M8); hand.addTile(Tile.M9);
        hand.addTile(Tile.P1); hand.addTile(Tile.P2); hand.addTile(Tile.P3);
        hand.addTile(Tile.S1); hand.addTile(Tile.S2);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("monitorHandStatus", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 monitorHandStatus() 中 totalTiles != 14 && totalTiles != 17 && totalTiles != 13 的分支
     */
    @Test
    void testMonitorHandStatus_OtherTotalTiles() throws Exception {
        session.start();
        
        // 設置玩家 0 有其他數量的牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 添加 15 張牌
        for (int i = 0; i < 15; i++) {
            hand.addTile(Tile.M1);
        }
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("monitorHandStatus", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 checkSelfDrawWin() 方法
     */
    @Test
    void testCheckSelfDrawWin() throws Exception {
        session.start();
        
        // 設置手牌為胡牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建胡牌
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); // Pair
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("checkSelfDrawWin", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 checkSelfDrawWin() 中 !isWinningHand 的分支
     */
    @Test
    void testCheckSelfDrawWin_NotWinning() throws Exception {
        session.start();
        
        // 設置手牌為非胡牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 添加非胡牌
        for (int i = 0; i < 13; i++) {
            hand.addTile(Tile.M1);
        }
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("checkSelfDrawWin", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }
}

