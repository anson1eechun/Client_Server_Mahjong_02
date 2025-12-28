package com.mahjong.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.logic.Tile;
import com.mahjong.model.Command;
import com.mahjong.model.Packet;
import com.mahjong.server.WebSocketGameSession;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MahjongGameIT {

    // 模擬 4 個玩家的 WebSocket 連線
    @Mock WebSocket p0;
    @Mock WebSocket p1;
    @Mock WebSocket p2;
    @Mock WebSocket p3;

    private List<WebSocket> players;
    private Map<WebSocket, String> nicknames;
    private WebSocketGameSession session;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        // 初始化玩家列表與暱稱
        players = Arrays.asList(p0, p1, p2, p3);
        nicknames = new ConcurrentHashMap<>();
        nicknames.put(p0, "PlayerEast");
        nicknames.put(p1, "PlayerSouth");
        nicknames.put(p2, "PlayerWest");
        nicknames.put(p3, "PlayerNorth");

        // 建立遊戲局 (這會觸發 MahjongRuleEngine 的初始化)
        session = new WebSocketGameSession(players, nicknames);
    }

    @Test
    @DisplayName("測試遊戲完整流程：開始 -> 莊家起手17張 -> 莊家出牌 -> 下家摸牌")
    void testGameFlow() throws Exception {
        // 1. 啟動遊戲
        System.out.println("=== 步驟 1: 啟動遊戲 ===");
        session.start();

        // 驗證：是否所有人都收到了 GAME_START 指令
        verify(p0, atLeastOnce()).send(contains("GAME_START"));
        verify(p3, atLeastOnce()).send(contains("GAME_START"));

        // 2. 驗證莊家 (p0) 狀態
        // 我們攔截發送給 p0 的所有訊息來分析
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(p0, atLeastOnce()).send(captor.capture());

        // 尋找最後一次的 STATE_UPDATE
        List<String> allMsgs = captor.getAllValues();
        String lastStateJson = allMsgs.stream()
                .filter(msg -> msg.contains("STATE_UPDATE"))
                .reduce((first, second) -> second) // 取最後一個
                .orElse(null);

        assertNotNull(lastStateJson, "應該要收到狀態更新");
        JsonNode statePacket = mapper.readTree(lastStateJson);
        JsonNode data = statePacket.get("data");

        // 驗證莊家手牌數 (16張 + 1張摸牌 = 17張)
        // 注意：您的程式碼邏輯是 "STATE_UPDATE" 裡的 "myHand"
        int handSize = data.get("myHand").size();
        System.out.println("莊家手牌數量: " + handSize);
        assertTrue(handSize == 17 || handSize == 14,
                "莊家手牌應該是 17 (台灣麻將) 或 14 (標準麻將), 實際: " + handSize);

        // 3. 莊家出牌 (模擬 PLAY_CARD)
        // 隨便選一張手牌打出去 (取第一張)
        String tileToDiscard = data.get("myHand").get(0).asText();
        System.out.println("=== 步驟 2: 莊家出牌 " + tileToDiscard + " ===");

        Packet playPacket = new Packet();
        playPacket.setCommand(Command.PLAY_CARD);
        playPacket.setData(Map.of("tile", tileToDiscard));

        // 執行動作
        session.processPlayerAction(p0, playPacket);

        // 4. 驗證下家 (p1) 是否收到輪次通知 (摸牌)
        // 檢查 p1 是否收到 "DRAW" 或 "請出牌" 的訊息
        ArgumentCaptor<String> p1Captor = ArgumentCaptor.forClass(String.class);
        verify(p1, atLeastOnce()).send(p1Captor.capture());

        boolean p1DrewTile = p1Captor.getAllValues().stream()
                .anyMatch(msg -> msg.contains("DRAW") || msg.contains("請出牌"));

        assertTrue(p1DrewTile, "下家 (Player 1) 應該要摸牌或被通知出牌");
        System.out.println("測試成功：流程從 Player 0 順利流轉到 Player 1");
    }
}