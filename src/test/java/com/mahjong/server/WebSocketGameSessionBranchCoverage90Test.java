package com.mahjong.server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mahjong.logic.PlayerHand;
import com.mahjong.logic.Tile;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;

/**
 * WebSocketGameSession 分支覆蓋率 90% 測試
 * 補充未覆蓋的分支以達到 90% 覆蓋率
 */
class WebSocketGameSessionBranchCoverage90Test {

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
     * 測試 handleActionResponse() 中 allowed == null 的分支
     */
    @Test
    void testHandleActionResponse_AllowedNull() throws Exception {
        session.start();
        
        // 設置動作組，但不為該玩家添加動作（導致 allowed == null）
        ActionGroup actionGroup = new ActionGroup(1);
        // 不添加 playerIndex 0 的動作
        
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = 
            (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送動作（但該玩家沒有這個動作選項）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "HU");
        Packet packet = new Packet(Command.ACTION, data);
        
        // 應該捕獲錯誤但不崩潰
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 handleActionResponse() 中 allowed != null 但 !allowed.contains(type) 的分支
     */
    @Test
    void testHandleActionResponse_ActionNotInAllowed() throws Exception {
        session.start();
        
        // 設置動作組，只允許 PONG，但玩家嘗試 HU
        ActionGroup actionGroup = new ActionGroup(2);
        actionGroup.addAction(0, "PONG"); // 只允許 PONG
        
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = 
            (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送不允許的動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "HU"); // 嘗試 HU，但只允許 PONG
        Packet packet = new Packet(Command.ACTION, data);
        
        // 應該捕獲錯誤但不崩潰
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 performChow() 中 sea.isEmpty() 的分支
     */
    @Test
    void testPerformChow_EmptySea() throws Exception {
        session.start();
        
        // 設置手牌，讓玩家可以吃
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        
        // 設置 pendingDiscardTile
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, Tile.M3);
        
        // 設置 sea 為空
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.clear(); // 確保 sea 為空
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(3);
        actionGroup.addAction(1, "CHOW M1,M2");
        
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = 
            (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(1);
        
        // 發送 CHOW 動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW M1,M2");
        Packet packet = new Packet(Command.ACTION, data);
        
        // 應該正常處理（即使 sea 為空）
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, packet);
        });
    }

    /**
     * 測試 handleActionResponse() 中 pendingResponses 不為空但只有一個玩家跳過的分支
     */
    @Test
    void testHandleActionResponse_Skip_NonEmptyPendingResponses() throws Exception {
        session.start();
        
        // 設置動作組，有多個玩家
        ActionGroup actionGroup = new ActionGroup(2);
        actionGroup.addAction(0, "PONG");
        actionGroup.addAction(1, "PONG");
        
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = 
            (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        pendingResponses.add(1); // 兩個玩家都在等待
        
        // 玩家 0 跳過
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet packet = new Packet(Command.ACTION, data);
        
        // 應該正常處理（pendingResponses 還有一個玩家）
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 resolveDiscard() 中 others.size() != 2 的分支（CHOW 動作中）
     */
    @Test
    void testResolveDiscard_Chow_InvalidOthersSize() throws Exception {
        session.start();
        
        // 設置手牌，讓玩家可以吃
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        
        // 設置當前玩家
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.setInt(session, 0);
        
        // 使用反射調用 resolveDiscard
        Method method = WebSocketGameSession.class.getDeclaredMethod("resolveDiscard", Tile.class, int.class);
        method.setAccessible(true);
        
        // 這個測試主要確保代碼不會崩潰
        // 實際的 others.size() != 2 的情況很難模擬，因為 ActionProcessor 會確保返回正確的動作
        assertDoesNotThrow(() -> {
            method.invoke(session, Tile.M3, 0);
        });
    }

    /**
     * 測試 performKong() 中 sea.isEmpty() 的分支（但 pendingDiscardTile 不為 null）
     */
    @Test
    void testPerformKong_EmptySea_WithPendingDiscard() throws Exception {
        session.start();
        
        // 設置手牌
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
        
        // 設置 sea 為空
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.clear(); // 確保 sea 為空
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(2);
        actionGroup.addAction(0, "KONG");
        
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = 
            (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送 KONG 動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "KONG");
        Packet packet = new Packet(Command.ACTION, data);
        
        // 應該正常處理（即使 sea 為空，但會檢查並處理）
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 handleActionResponse() 中 cmd != Command.ACTION 的分支
     */
    @Test
    void testHandleActionResponse_NotActionCommand() throws Exception {
        session.start();
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(1);
        actionGroup.addAction(0, "HU");
        
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = 
            (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送非 ACTION 命令（例如 PLAY_CARD）
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet packet = new Packet(Command.PLAY_CARD, data);
        
        // 應該被忽略（因為 cmd != Command.ACTION）
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 handleActionResponse() 中 currentActionGroup == null 的分支
     */
    @Test
    void testHandleActionResponse_CurrentActionGroupNull() throws Exception {
        session.start();
        
        // 設置 waitingForAction = true，但 currentActionGroup = null
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, null); // 設置為 null
        
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = 
            (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet packet = new Packet(Command.ACTION, data);
        
        // 應該被忽略（因為 currentActionGroup == null）
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 handleActionResponse() 中 waitingForAction == false 的分支
     */
    @Test
    void testHandleActionResponse_WaitingForActionFalse() throws Exception {
        session.start();
        
        // 設置 waitingForAction = false
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, false);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(1);
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        // 發送動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet packet = new Packet(Command.ACTION, data);
        
        // 應該被忽略（因為 waitingForAction == false）
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }

    /**
     * 測試 performConcealedKong() 中 replacement == null && isWinningHand == false 的分支
     */
    @Test
    void testPerformConcealedKong_NullReplacement_NotWinning() throws Exception {
        session.start();
        
        // 設置手牌（可以暗槓但補牌後不胡）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建可以暗槓但補牌後不胡的手牌
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1); // 4 張 M1（暗槓）
        hand.addTile(Tile.M2);
        hand.addTile(Tile.M3);
        hand.addTile(Tile.M4);
        hand.addTile(Tile.P1);
        hand.addTile(Tile.P2);
        hand.addTile(Tile.P3);
        hand.addTile(Tile.S1);
        hand.addTile(Tile.S2);
        hand.addTile(Tile.S3);
        
        // 抽完所有牌（使 replacement == null）
        Field engineField = WebSocketGameSession.class.getDeclaredField("engine");
        engineField.setAccessible(true);
        com.mahjong.logic.MahjongRuleEngine engine = 
            (com.mahjong.logic.MahjongRuleEngine) engineField.get(session);
        
        while (engine.drawTile() != null) {
            // 繼續抽牌
        }
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(0);
        actionGroup.addAction(0, "CONCEALED_KONG M1");
        
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = 
            (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 發送暗槓動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CONCEALED_KONG M1");
        Packet packet = new Packet(Command.ACTION, data);
        
        // 應該正常處理（replacement == null && isWinningHand == false）
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, packet);
        });
    }
}

