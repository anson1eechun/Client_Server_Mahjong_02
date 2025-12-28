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
 * WebSocketGameSession 進階測試
 * 測試私有方法和複雜場景以提升覆蓋率
 */
class WebSocketGameSessionAdvancedTest {

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

    // 輔助方法：使用反射設置手牌
    private void setupHand(int playerIndex, List<Tile> tiles) throws Exception {
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(playerIndex);
        hand.getStandingTiles().clear();
        for (Tile tile : tiles) {
            hand.addTile(tile);
        }
    }

    // 輔助方法：使用反射獲取手牌
    @SuppressWarnings("unchecked")
    private List<PlayerHand> getHands() throws Exception {
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        return (List<PlayerHand>) handsField.get(session);
    }

    // 輔助方法：設置 pendingDiscardTile
    private void setPendingDiscardTile(Tile tile) throws Exception {
        Field field = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        field.setAccessible(true);
        field.set(session, tile);
    }

    // 輔助方法：設置 waitingForAction
    private void setWaitingForAction(boolean value) throws Exception {
        Field field = WebSocketGameSession.class.getDeclaredField("waitingForAction");
        field.setAccessible(true);
        field.set(session, value);
    }

    // 輔助方法：設置 currentActionGroup
    private void setCurrentActionGroup(ActionGroup group) throws Exception {
        Field field = WebSocketGameSession.class.getDeclaredField("currentActionGroup");
        field.setAccessible(true);
        field.set(session, group);
    }

    // 輔助方法：設置 currentPlayerIndex
    private void setCurrentPlayerIndex(int index) throws Exception {
        Field field = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        field.setAccessible(true);
        field.set(session, index);
    }

    @Test
    void testResolveDiscard_WithActions() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置玩家 1 的手牌，使其可以碰 M1
        List<Tile> handTiles = new ArrayList<>();
        handTiles.add(Tile.M1);
        handTiles.add(Tile.M1);
        handTiles.add(Tile.M2);
        handTiles.add(Tile.M3);
        handTiles.add(Tile.M4);
        handTiles.add(Tile.M5);
        handTiles.add(Tile.M6);
        handTiles.add(Tile.M7);
        handTiles.add(Tile.M8);
        handTiles.add(Tile.M9);
        handTiles.add(Tile.P1);
        handTiles.add(Tile.P2);
        handTiles.add(Tile.P3);
        handTiles.add(Tile.P4);
        handTiles.add(Tile.P5);
        handTiles.add(Tile.P6);
        setupHand(1, handTiles);
        
        // 設置當前玩家為 0，並讓玩家 0 出 M1
        setCurrentPlayerIndex(0);
        
        // 玩家 0 出牌 M1
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCard = new Packet(Command.PLAY_CARD, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, playCard);
        });
        
        // 驗證玩家 1 收到了動作請求（如果可以碰）
        verify(mockWebSocket2, atLeastOnce()).send(anyString());
    }

    @Test
    void testResolveDiscard_NoActions() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置所有玩家手牌，使其無法對 M1 做任何動作
        List<Tile> handTiles = new ArrayList<>();
        handTiles.add(Tile.M2);
        handTiles.add(Tile.M3);
        handTiles.add(Tile.M4);
        handTiles.add(Tile.M5);
        handTiles.add(Tile.M6);
        handTiles.add(Tile.M7);
        handTiles.add(Tile.M8);
        handTiles.add(Tile.M9);
        handTiles.add(Tile.P1);
        handTiles.add(Tile.P2);
        handTiles.add(Tile.P3);
        handTiles.add(Tile.P4);
        handTiles.add(Tile.P5);
        handTiles.add(Tile.P6);
        handTiles.add(Tile.P7);
        handTiles.add(Tile.P8);
        
        for (int i = 0; i < 4; i++) {
            setupHand(i, new ArrayList<>(handTiles));
        }
        
        setCurrentPlayerIndex(0);
        
        // 玩家 0 出牌 M1
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCard = new Packet(Command.PLAY_CARD, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, playCard);
        });
        
        // 應該直接進入下一回合（沒有動作）
        verify(mockWebSocket1, atLeastOnce()).send(anyString());
    }

    @Test
    void testHandleActionResponse_Skip() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置動作組和等待狀態
        ActionGroup actionGroup = new ActionGroup(1);
        actionGroup.addAction(1, "HU");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        setPendingDiscardTile(Tile.M1);
        setCurrentPlayerIndex(0);
        
        // 玩家 1 跳過
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
    }

    @Test
    void testHandleActionResponse_Skip_Priority0() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置 Priority 0 動作組（自摸）
        ActionGroup actionGroup = new ActionGroup(0);
        actionGroup.addAction(0, "HU");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        setPendingDiscardTile(null);
        setCurrentPlayerIndex(0);
        
        // 玩家 0 跳過自摸
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, actionPacket);
        });
    }

    @Test
    void testHandleActionResponse_Hu() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置可以胡牌的手牌
        List<Tile> winningHand = createWinningHand();
        setupHand(1, winningHand);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(1);
        actionGroup.addAction(1, "HU");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        setPendingDiscardTile(Tile.M1);
        setCurrentPlayerIndex(0);
        
        // 玩家 1 胡牌
        Map<String, Object> data = new HashMap<>();
        data.put("type", "HU");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
        
        // 驗證遊戲結束訊息
        verify(mockWebSocket2, atLeastOnce()).send(anyString());
    }

    @Test
    void testHandleActionResponse_Pong() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置玩家 1 可以碰 M1
        List<Tile> handTiles = new ArrayList<>();
        handTiles.add(Tile.M1);
        handTiles.add(Tile.M1);
        handTiles.add(Tile.M2);
        handTiles.add(Tile.M3);
        handTiles.add(Tile.M4);
        handTiles.add(Tile.M5);
        handTiles.add(Tile.M6);
        handTiles.add(Tile.M7);
        handTiles.add(Tile.M8);
        handTiles.add(Tile.M9);
        handTiles.add(Tile.P1);
        handTiles.add(Tile.P2);
        handTiles.add(Tile.P3);
        setupHand(1, handTiles);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(2);
        actionGroup.addAction(1, "PONG");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        setPendingDiscardTile(Tile.M1);
        setCurrentPlayerIndex(0);
        
        // 設置 sea（海底）
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M1");
        
        // 玩家 1 碰牌
        Map<String, Object> data = new HashMap<>();
        data.put("type", "PONG");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
        
        // 驗證碰牌訊息
        verify(mockWebSocket2, atLeastOnce()).send(anyString());
    }

    @Test
    void testHandleActionResponse_Chow() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置玩家 1 可以吃 M1（需要 M2, M3）
        List<Tile> handTiles = new ArrayList<>();
        handTiles.add(Tile.M2);
        handTiles.add(Tile.M3);
        handTiles.add(Tile.M4);
        handTiles.add(Tile.M5);
        handTiles.add(Tile.M6);
        handTiles.add(Tile.M7);
        handTiles.add(Tile.M8);
        handTiles.add(Tile.M9);
        handTiles.add(Tile.P1);
        handTiles.add(Tile.P2);
        handTiles.add(Tile.P3);
        handTiles.add(Tile.P4);
        handTiles.add(Tile.P5);
        setupHand(1, handTiles);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(3);
        actionGroup.addAction(1, "CHOW M2,M3");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        setPendingDiscardTile(Tile.M1);
        setCurrentPlayerIndex(0);
        
        // 設置 sea
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M1");
        
        // 玩家 1 吃牌
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW M2,M3");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
        
        // 驗證吃牌訊息
        verify(mockWebSocket2, atLeastOnce()).send(anyString());
    }

    @Test
    void testHandleActionResponse_Kong() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置玩家 1 可以槓 M1
        List<Tile> handTiles = new ArrayList<>();
        handTiles.add(Tile.M1);
        handTiles.add(Tile.M1);
        handTiles.add(Tile.M1);
        handTiles.add(Tile.M2);
        handTiles.add(Tile.M3);
        handTiles.add(Tile.M4);
        handTiles.add(Tile.M5);
        handTiles.add(Tile.M6);
        handTiles.add(Tile.M7);
        handTiles.add(Tile.M8);
        handTiles.add(Tile.M9);
        handTiles.add(Tile.P1);
        handTiles.add(Tile.P2);
        setupHand(1, handTiles);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(2);
        actionGroup.addAction(1, "KONG");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        setPendingDiscardTile(Tile.M1);
        setCurrentPlayerIndex(0);
        
        // 設置 sea
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M1");
        
        // 玩家 1 槓牌
        Map<String, Object> data = new HashMap<>();
        data.put("type", "KONG");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
        
        // 驗證槓牌訊息
        verify(mockWebSocket2, atLeastOnce()).send(anyString());
    }

    @Test
    void testHandleActionResponse_InvalidAction() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(1);
        actionGroup.addAction(1, "HU");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        setPendingDiscardTile(Tile.M1);
        
        // 玩家 1 嘗試無效動作
        Map<String, Object> data = new HashMap<>();
        data.put("type", "INVALID_ACTION");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
    }

    @Test
    void testHandleActionResponse_NotWaiting() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 不設置 waitingForAction
        setWaitingForAction(false);
        
        // 玩家 1 發送動作回應
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
    }

    @Test
    void testHandleActionResponse_NotInPendingList() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置動作組，但玩家 2 不在 pending 列表中
        ActionGroup actionGroup = new ActionGroup(1);
        actionGroup.addAction(1, "HU");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        
        // 玩家 2 發送動作回應（不在列表中）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "SKIP");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket3, actionPacket);
        });
    }

    @Test
    void testProcessNextActionGroup_EmptyQueue() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 使用反射調用 processNextActionGroup（當佇列為空時）
        Method method = WebSocketGameSession.class.getDeclaredMethod("processNextActionGroup");
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
    }

    @Test
    void testProcessNextActionGroup_WithActions() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置動作佇列
        Field actionQueueField = WebSocketGameSession.class.getDeclaredField("actionQueue");
        actionQueueField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.LinkedList<ActionGroup> actionQueue = 
            (java.util.LinkedList<ActionGroup>) actionQueueField.get(session);
        
        ActionGroup actionGroup = new ActionGroup(1);
        actionGroup.addAction(1, "HU");
        actionQueue.add(actionGroup);
        
        setPendingDiscardTile(Tile.M1);
        
        // 使用反射調用 processNextActionGroup
        Method method = WebSocketGameSession.class.getDeclaredMethod("processNextActionGroup");
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
        
        // 驗證玩家收到了動作請求
        verify(mockWebSocket2, atLeastOnce()).send(anyString());
    }

    @Test
    void testNextTurn() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 使用反射調用 nextTurn
        Method method = WebSocketGameSession.class.getDeclaredMethod("nextTurn");
        method.setAccessible(true);
        
        setCurrentPlayerIndex(0);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
        
        // 驗證下一回合開始
        verify(mockWebSocket2, atLeastOnce()).send(anyString());
    }

    @Test
    void testStartTurn_FirstTurn() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置 isFirstTurn
        Field isFirstTurnField = WebSocketGameSession.class.getDeclaredField("isFirstTurn");
        isFirstTurnField.setAccessible(true);
        isFirstTurnField.set(session, true);
        
        setCurrentPlayerIndex(0);
        
        // 使用反射調用 startTurn
        Method method = WebSocketGameSession.class.getDeclaredMethod("startTurn");
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
    }

    @Test
    void testStartTurn_NormalTurn() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置 isFirstTurn 為 false
        Field isFirstTurnField = WebSocketGameSession.class.getDeclaredField("isFirstTurn");
        isFirstTurnField.setAccessible(true);
        isFirstTurnField.set(session, false);
        
        setCurrentPlayerIndex(1);
        
        // 使用反射調用 startTurn
        Method method = WebSocketGameSession.class.getDeclaredMethod("startTurn");
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
        
        // 驗證玩家收到了摸牌訊息
        verify(mockWebSocket2, atLeastOnce()).send(anyString());
    }

    @Test
    void testStartTurn_WallEmpty() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置 isFirstTurn 為 false
        Field isFirstTurnField = WebSocketGameSession.class.getDeclaredField("isFirstTurn");
        isFirstTurnField.setAccessible(true);
        isFirstTurnField.set(session, false);
        
        setCurrentPlayerIndex(1);
        
        // 清空牌牆（通過反射）
        Field engineField = WebSocketGameSession.class.getDeclaredField("engine");
        engineField.setAccessible(true);
        com.mahjong.logic.MahjongRuleEngine engine = 
            (com.mahjong.logic.MahjongRuleEngine) engineField.get(session);
        
        // 抽完所有牌
        while (engine.drawTile() != null) {
            // 繼續抽牌直到牌牆空
        }
        
        // 使用反射調用 startTurn
        Method method = WebSocketGameSession.class.getDeclaredMethod("startTurn");
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
        
        // 驗證遊戲結束訊息
        for (WebSocket socket : players) {
            verify(socket, atLeastOnce()).send(anyString());
        }
    }

    @Test
    void testPerformPong_MissingTiles() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置玩家 1 手牌（沒有足夠的牌可以碰）
        List<Tile> handTiles = new ArrayList<>();
        handTiles.add(Tile.M1);
        handTiles.add(Tile.M2);
        handTiles.add(Tile.M3);
        setupHand(1, handTiles);
        
        setPendingDiscardTile(Tile.M1);
        setCurrentPlayerIndex(0);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(2);
        actionGroup.addAction(1, "PONG");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        
        // 設置 sea
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M1");
        
        // 嘗試碰牌（應該失敗）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "PONG");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
    }

    @Test
    void testPerformChow_MissingTiles() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置玩家 1 手牌（沒有足夠的牌可以吃）
        List<Tile> handTiles = new ArrayList<>();
        handTiles.add(Tile.M2);
        handTiles.add(Tile.M4);
        setupHand(1, handTiles);
        
        setPendingDiscardTile(Tile.M1);
        setCurrentPlayerIndex(0);
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(3);
        actionGroup.addAction(1, "CHOW M2,M3");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        
        // 設置 sea
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M1");
        
        // 嘗試吃牌（應該失敗）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW M2,M3");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
    }

    @Test
    void testPerformChow_InvalidFormat() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置動作組
        ActionGroup actionGroup = new ActionGroup(3);
        actionGroup.addAction(1, "CHOW M2,M3");
        setCurrentActionGroup(actionGroup);
        setWaitingForAction(true);
        setPendingDiscardTile(Tile.M1);
        
        // 嘗試吃牌（格式錯誤）
        Map<String, Object> data = new HashMap<>();
        data.put("type", "CHOW INVALID");
        Packet actionPacket = new Packet(Command.ACTION, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket2, actionPacket);
        });
    }

    @Test
    void testResolveDiscard_FirstTurn() throws Exception {
        // 啟動遊戲
        session.start();
        
        // 設置 isFirstTurn
        Field isFirstTurnField = WebSocketGameSession.class.getDeclaredField("isFirstTurn");
        isFirstTurnField.setAccessible(true);
        isFirstTurnField.set(session, true);
        
        setCurrentPlayerIndex(0);
        
        // 設置玩家 0 的手牌
        List<Tile> handTiles = new ArrayList<>();
        handTiles.add(Tile.M1);
        handTiles.add(Tile.M2);
        handTiles.add(Tile.M3);
        setupHand(0, handTiles);
        
        // 玩家 0 出牌
        Map<String, Object> data = new HashMap<>();
        data.put("tile", "M1");
        Packet playCard = new Packet(Command.PLAY_CARD, data);
        
        assertDoesNotThrow(() -> {
            session.processPlayerAction(mockWebSocket1, playCard);
        });
    }

    // 輔助方法：創建胡牌手牌
    private List<Tile> createWinningHand() {
        List<Tile> hand = new ArrayList<>();
        // 標準胡牌：4 個順子 + 1 對眼
        hand.add(Tile.M1);
        hand.add(Tile.M2);
        hand.add(Tile.M3);
        hand.add(Tile.M4);
        hand.add(Tile.M5);
        hand.add(Tile.M6);
        hand.add(Tile.M7);
        hand.add(Tile.M8);
        hand.add(Tile.M9);
        hand.add(Tile.P1);
        hand.add(Tile.P1);
        hand.add(Tile.P1);
        hand.add(Tile.S1);
        hand.add(Tile.S1);
        return hand;
    }
}

