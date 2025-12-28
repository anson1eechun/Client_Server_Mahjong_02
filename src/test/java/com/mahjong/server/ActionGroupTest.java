package com.mahjong.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 測試 ActionGroup 類別
 */
class ActionGroupTest {

    private ActionGroup actionGroup;

    @BeforeEach
    void setUp() {
        actionGroup = new ActionGroup(1);
    }

    @Test
    void testConstructor() {
        ActionGroup group = new ActionGroup(2);
        assertNotNull(group);
        assertTrue(group.players.isEmpty());
        assertTrue(group.playerActions.isEmpty());
    }

    @Test
    void testAddAction_SinglePlayer() {
        actionGroup.addAction(0, "HU");
        
        assertTrue(actionGroup.players.contains(0));
        assertTrue(actionGroup.playerActions.containsKey(0));
        assertEquals(1, actionGroup.playerActions.get(0).size());
        assertEquals("HU", actionGroup.playerActions.get(0).get(0));
    }

    @Test
    void testAddAction_MultipleActions() {
        actionGroup.addAction(0, "HU");
        actionGroup.addAction(0, "PONG");
        
        assertEquals(1, actionGroup.players.size());
        assertEquals(2, actionGroup.playerActions.get(0).size());
        assertTrue(actionGroup.playerActions.get(0).contains("HU"));
        assertTrue(actionGroup.playerActions.get(0).contains("PONG"));
    }

    @Test
    void testAddAction_MultiplePlayers() {
        actionGroup.addAction(0, "HU");
        actionGroup.addAction(1, "PONG");
        actionGroup.addAction(2, "CHOW");
        
        assertEquals(3, actionGroup.players.size());
        assertTrue(actionGroup.players.contains(0));
        assertTrue(actionGroup.players.contains(1));
        assertTrue(actionGroup.players.contains(2));
        assertEquals(1, actionGroup.playerActions.get(0).size());
        assertEquals(1, actionGroup.playerActions.get(1).size());
        assertEquals(1, actionGroup.playerActions.get(2).size());
    }

    @Test
    void testAddAction_DuplicatePlayer() {
        actionGroup.addAction(0, "HU");
        actionGroup.addAction(0, "PONG");
        actionGroup.addAction(0, "HU"); // 重複添加
        
        assertEquals(1, actionGroup.players.size());
        assertEquals(3, actionGroup.playerActions.get(0).size());
    }

    @Test
    void testPriority() {
        ActionGroup huGroup = new ActionGroup(1);
        ActionGroup pongGroup = new ActionGroup(2);
        ActionGroup chowGroup = new ActionGroup(3);
        
        assertEquals(1, huGroup.priority);
        assertEquals(2, pongGroup.priority);
        assertEquals(3, chowGroup.priority);
    }
}

