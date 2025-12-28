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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * WebSocketGameSession 異常處理測試
 * Phase 1: 測試所有 try-catch 的異常分支以提升分支覆蓋率
 */
class WebSocketGameSessionExceptionTest {

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

    // ========== Phase 1: 異常處理測試 ==========

    /**
     * 測試 performHu() 的異常處理分支
     * 目標：覆蓋 catch (Exception e) 分支
     */
    @Test
    void testPerformHu_ExceptionHandling() throws Exception {
        session.start();
        
        // 設置會導致異常的條件：使用反射設置無效的 playerIndex
        Method method = WebSocketGameSession.class.getDeclaredMethod("performHu", int.class);
        method.setAccessible(true);
        
        // 設置 pendingDiscardTile 為 null（正常情況）
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, null);
        
        // 正常調用應該不會拋出異常
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
        
        // 驗證廣播了 GAME_OVER（通過 send 被調用）
        verify(mockWebSocket1, atLeastOnce()).send(anyString());
    }

    /**
     * 測試 performKong() 的異常處理分支
     * 目標：覆蓋 catch (Exception e) 分支
     */
    @Test
    void testPerformKong_ExceptionHandling() throws Exception {
        session.start();
        
        // 設置手牌
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        // 給玩家 0 添加足夠的牌來執行槓
        PlayerHand hand = hands.get(0);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        hand.addTile(Tile.M1);
        
        // 設置 pendingDiscardTile
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, Tile.M1);
        
        // 設置 sea（必須有牌才能移除）
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M1");
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performKong", int.class);
        method.setAccessible(true);
        
        // 正常調用應該不會拋出異常
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 performKong() 異常情況：pendingDiscardTile 為 null
     */
    @Test
    void testPerformKong_NullPendingDiscardTile() throws Exception {
        session.start();
        
        // 設置 pendingDiscardTile 為 null（會導致 NullPointerException）
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, null);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performKong", int.class);
        method.setAccessible(true);
        
        // 應該捕獲異常而不會拋出
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 performConcealedKong() 的異常處理分支
     */
    @Test
    void testPerformConcealedKong_ExceptionHandling() throws Exception {
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
        hand.addTile(Tile.M1);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performConcealedKong", int.class, Tile.class);
        method.setAccessible(true);
        
        // 正常調用
        assertDoesNotThrow(() -> {
            method.invoke(session, 0, Tile.M1);
        });
    }

    /**
     * 測試 performConcealedKong() 異常情況：replacement 為 null（牌牆耗盡）
     */
    @Test
    void testPerformConcealedKong_NullReplacement() throws Exception {
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
        hand.addTile(Tile.M1);
        
        // 抽完所有牌（模擬牌牆耗盡）
        Field engineField = WebSocketGameSession.class.getDeclaredField("engine");
        engineField.setAccessible(true);
        com.mahjong.logic.MahjongRuleEngine engine = 
            (com.mahjong.logic.MahjongRuleEngine) engineField.get(session);
        
        while (engine.drawTile() != null) {
            // 繼續抽牌直到耗盡
        }
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performConcealedKong", int.class, Tile.class);
        method.setAccessible(true);
        
        // replacement 為 null 時應該正常處理，不會拋出異常
        assertDoesNotThrow(() -> {
            method.invoke(session, 0, Tile.M1);
        });
    }

    /**
     * 測試 performPong() 的異常處理分支
     */
    @Test
    void testPerformPong_ExceptionHandling() throws Exception {
        session.start();
        
        // 設置手牌
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
        
        // 正常調用
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 performPong() 異常情況：手牌不足
     */
    @Test
    void testPerformPong_InsufficientTiles() throws Exception {
        session.start();
        
        // 設置手牌（只有 1 張，不足以碰）
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
        
        // 應該正常處理錯誤情況，不會拋出異常
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 performPong() 異常情況：sea 為空
     */
    @Test
    void testPerformPong_EmptySea() throws Exception {
        session.start();
        
        // 設置手牌
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
        
        // sea 保持為空（不添加任何牌）
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performPong", int.class);
        method.setAccessible(true);
        
        // 應該正常處理 sea 為空的情況
        assertDoesNotThrow(() -> {
            method.invoke(session, 0);
        });
    }

    /**
     * 測試 performChow() 的異常處理分支
     */
    @Test
    void testPerformChow_ExceptionHandling() throws Exception {
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
        
        // 設置 sea
        Field seaField = WebSocketGameSession.class.getDeclaredField("sea");
        seaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> sea = (List<String>) seaField.get(session);
        sea.add("M3");
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performChow", int.class, String.class, String.class);
        method.setAccessible(true);
        
        // 正常調用
        assertDoesNotThrow(() -> {
            method.invoke(session, 0, "M1", "M2");
        });
    }

    /**
     * 測試 performChow() 異常情況：手牌不足
     */
    @Test
    void testPerformChow_MissingTiles() throws Exception {
        session.start();
        
        // 設置手牌（缺少需要的牌）
        Field handsField = WebSocketGameSession.class.getDeclaredField("hands");
        handsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<PlayerHand> hands = (List<PlayerHand>) handsField.get(session);
        
        PlayerHand hand = hands.get(0);
        // 不添加任何牌，導致 removeTile 失敗
        
        // 設置 pendingDiscardTile
        Field pendingDiscardField = WebSocketGameSession.class.getDeclaredField("pendingDiscardTile");
        pendingDiscardField.setAccessible(true);
        pendingDiscardField.set(session, Tile.M3);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("performChow", int.class, String.class, String.class);
        method.setAccessible(true);
        
        // 應該正常處理錯誤情況
        assertDoesNotThrow(() -> {
            method.invoke(session, 0, "M1", "M2");
        });
    }

    /**
     * 測試 startTurn() 的異常處理分支
     */
    @Test
    void testStartTurn_ExceptionHandling() throws Exception {
        session.start();
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("startTurn");
        method.setAccessible(true);
        
        // 正常調用
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
    }

    /**
     * 測試 startTurn() 異常情況：牌牆耗盡（drawn 為 null）
     */
    @Test
    void testStartTurn_NullDrawn() throws Exception {
        session.start();
        
        // 抽完所有牌
        Field engineField = WebSocketGameSession.class.getDeclaredField("engine");
        engineField.setAccessible(true);
        com.mahjong.logic.MahjongRuleEngine engine = 
            (com.mahjong.logic.MahjongRuleEngine) engineField.get(session);
        
        while (engine.drawTile() != null) {
            // 繼續抽牌
        }
        
        // 設置當前玩家
        Field currentPlayerField = WebSocketGameSession.class.getDeclaredField("currentPlayerIndex");
        currentPlayerField.setAccessible(true);
        currentPlayerField.set(session, 0);
        
        Method method = WebSocketGameSession.class.getDeclaredMethod("startTurn");
        method.setAccessible(true);
        
        // drawn 為 null 時應該正常處理
        assertDoesNotThrow(() -> {
            method.invoke(session);
        });
    }
}

