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
 * WebSocketGameSession 剩餘分支覆蓋率測試
 * 補充測試剩餘未覆蓋的分支以達到 90% 覆蓋率
 */
class WebSocketGameSessionRemainingBranchTest {

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
     * 測試 hasTile() 中 !anchor.isNumberTile() 的分支
     */
    @Test
    void testHasTile_NonNumberTile() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("hasTile", List.class, Tile.class, int.class);
        method.setAccessible(true);
        
        List<Tile> tiles = new ArrayList<>();
        tiles.add(Tile.EAST);
        
        // 測試字牌（不是數字牌）
        boolean result = (Boolean) method.invoke(session, tiles, Tile.EAST, 0);
        assertFalse(result, "Non-number tile should return false");
    }

    /**
     * 測試 hasTile() 中 targetRank < 1 的分支
     */
    @Test
    void testHasTile_TargetRankLessThan1() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("hasTile", List.class, Tile.class, int.class);
        method.setAccessible(true);
        
        List<Tile> tiles = new ArrayList<>();
        tiles.add(Tile.M1);
        
        // 測試 offset 導致 targetRank < 1
        boolean result = (Boolean) method.invoke(session, tiles, Tile.M1, -1);
        assertFalse(result, "Target rank < 1 should return false");
    }

    /**
     * 測試 hasTile() 中 targetRank > 9 的分支
     */
    @Test
    void testHasTile_TargetRankGreaterThan9() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("hasTile", List.class, Tile.class, int.class);
        method.setAccessible(true);
        
        List<Tile> tiles = new ArrayList<>();
        tiles.add(Tile.M9);
        
        // 測試 offset 導致 targetRank > 9
        boolean result = (Boolean) method.invoke(session, tiles, Tile.M9, 1);
        assertFalse(result, "Target rank > 9 should return false");
    }

    /**
     * 測試 hasTile() 中 t.getSuit() != anchor.getSuit() 的分支
     */
    @Test
    void testHasTile_DifferentSuit() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("hasTile", List.class, Tile.class, int.class);
        method.setAccessible(true);
        
        List<Tile> tiles = new ArrayList<>();
        tiles.add(Tile.P1); // 不同花色
        
        // 測試不同花色
        boolean result = (Boolean) method.invoke(session, tiles, Tile.M1, 0);
        assertFalse(result, "Different suit should return false");
    }

    /**
     * 測試 hasTile() 中 t.getRank() != targetRank 的分支
     */
    @Test
    void testHasTile_DifferentRank() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("hasTile", List.class, Tile.class, int.class);
        method.setAccessible(true);
        
        List<Tile> tiles = new ArrayList<>();
        tiles.add(Tile.M2); // 不同數字
        
        // 測試不同數字
        boolean result = (Boolean) method.invoke(session, tiles, Tile.M1, 0);
        assertFalse(result, "Different rank should return false");
    }

    /**
     * 測試 hasTile() 中 t.getSuit() == anchor.getSuit() && t.getRank() == targetRank 的分支
     */
    @Test
    void testHasTile_MatchingTile() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("hasTile", List.class, Tile.class, int.class);
        method.setAccessible(true);
        
        List<Tile> tiles = new ArrayList<>();
        tiles.add(Tile.M1);
        
        // 測試匹配的牌
        boolean result = (Boolean) method.invoke(session, tiles, Tile.M1, 0);
        assertTrue(result, "Matching tile should return true");
    }

    /**
     * 測試 removeTileOffset() 方法
     */
    @Test
    void testRemoveTileOffset() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("removeTileOffset", PlayerHand.class, Tile.class, int.class);
        method.setAccessible(true);
        
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M2);
        
        // 測試移除 offset 為 1 的牌（M2）
        assertDoesNotThrow(() -> {
            method.invoke(session, hand, Tile.M1, 1);
        });
        
        // 驗證 M2 被移除
        assertFalse(hand.getStandingTiles().contains(Tile.M2), "M2 should be removed");
    }

    /**
     * 測試 removeTileOffset() 中 t.getSuit() != anchor.getSuit() 的分支
     */
    @Test
    void testRemoveTileOffset_DifferentSuit() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("removeTileOffset", PlayerHand.class, Tile.class, int.class);
        method.setAccessible(true);
        
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.P1); // 不同花色
        
        // 測試不同花色（不應該移除）
        assertDoesNotThrow(() -> {
            method.invoke(session, hand, Tile.M1, 0);
        });
    }

    /**
     * 測試 removeTileOffset() 中 t.getRank() != targetRank 的分支
     */
    @Test
    void testRemoveTileOffset_DifferentRank() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("removeTileOffset", PlayerHand.class, Tile.class, int.class);
        method.setAccessible(true);
        
        PlayerHand hand = new PlayerHand();
        hand.addTile(Tile.M2); // 不同數字
        
        // 測試不同數字（不應該移除）
        assertDoesNotThrow(() -> {
            method.invoke(session, hand, Tile.M1, 0);
        });
    }

    /**
     * 測試 performConcealedKong() 中 replacement != null && isWinningHand 的分支
     */
    @Test
    void testPerformConcealedKong_ReplacementNotNull_Winning() throws Exception {
        session.start();
        
        // 設置手牌，讓暗槓後補牌可以胡
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 創建接近胡牌的手牌（暗槓後補牌可以胡）
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1); // 4 張 M1（暗槓）
        hand.addTile(Tile.M2); hand.addTile(Tile.M3); hand.addTile(Tile.M4); // Sequence
        hand.addTile(Tile.P1); hand.addTile(Tile.P1); hand.addTile(Tile.P1); // Pong
        hand.addTile(Tile.S1); hand.addTile(Tile.S2); hand.addTile(Tile.S3); // Sequence
        hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); hand.addTile(Tile.EAST); // Pong
        
        // 設置 pendingResponses
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(0);
        actionGroup.addAction(0, "CONCEALED_KONG M1");
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        // 通過 handleActionResponse 觸發
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CONCEALED_KONG M1");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    /**
     * 測試 performConcealedKong() 中 replacement != null && !isWinningHand 的分支
     */
    @Test
    void testPerformConcealedKong_ReplacementNotNull_NotWinning() throws Exception {
        session.start();
        
        // 設置手牌（暗槓後補牌不會胡）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1); hand.addTile(Tile.M1);
        hand.addTile(Tile.M2); hand.addTile(Tile.M3);
        
        // 設置 pendingResponses
        Field pendingResponsesField = WebSocketGameSession.class.getDeclaredField("pendingResponses");
        pendingResponsesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Integer> pendingResponses = (java.util.Set<Integer>) pendingResponsesField.get(session);
        pendingResponses.add(0);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(0);
        actionGroup.addAction(0, "CONCEALED_KONG M1");
        Field currentActionGroupField = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        currentActionGroupField.setAccessible(true);
        currentActionGroupField.set(session, actionGroup);
        
        Field waitingField = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        waitingField.setAccessible(true);
        waitingField.setBoolean(session, true);
        
        // 通過 handleActionResponse 觸發
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CONCEALED_KONG M1");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    /**
     * 測試 performPong() 中 totalTiles != 14 的分支
     */
    @Test
    void testPerformPong_TotalTilesNot14() throws Exception {
        session.start();
        
        // 設置手牌（不是 14 張）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
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
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performPong", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
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
        // 添加 12 張牌，吃後變成 14 張但不是胡牌
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
        // 添加 12 張牌，碰後變成 14 張但不是胡牌
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

    /**
     * 測試 monitorHandStatus() 中 totalTiles == 14 && isWinningHand && playerIndex != currentPlayerIndex 的分支
     */
    @Test
    void testMonitorHandStatus_14Tiles_Winning_NotCurrentPlayer() throws Exception {
        session.start();
        
        // 設置玩家 1 有 14 張胡牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(1);
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
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("monitorHandStatus", int.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session, 1);
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
     * 測試 startTurn() 中 canTsumo = false && !tingResult.isTing() 的分支
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
        // 創建一個普通手牌
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
}

