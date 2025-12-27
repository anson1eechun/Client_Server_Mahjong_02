
## 優先級說明

- 🔴 **P0 (Critical)**: 必須實作，影響遊戲核心功能
- 🟠 **P1 (High)**: 重要功能，顯著提升遊戲體驗
- 🟡 **P2 (Medium)**: 有價值的功能，可依時間安排
- 🟢 **P3 (Low)**: 錦上添花，可延後實作

---

## Phase 1: 核心功能完善

### 🔴 P0-1: 修正莊家起手牌數

**目標**: 符合台灣麻將規則，莊家應為 17 張起手

**現況問題**:
```java
// 當前實作：所有玩家都是 16 張
engine.dealInitialHands(hands);  // 每人 16 張
```

**實作方案**:
```java
// WebSocketGameSession.java
public void start() {
    engine.shuffle();
    engine.dealInitialHands(hands);  // 每人 16 張
    
    // ✅ 莊家先摸 1 張
    Tile firstDraw = engine.drawTile();
    if (firstDraw != null) {
        hands.get(0).addTile(firstDraw);
    }
    
    broadcast(new Packet(Command.GAME_START, null));
    broadcastState();
    
    // 莊家第一輪不用再摸牌，直接出牌
    currentPlayerIndex = 0;
    isFirstTurn = true;  // 新增標記
    broadcastState();
}

private void startTurn() {
    // 如果是莊家第一輪，跳過摸牌
    if (currentPlayerIndex == 0 && isFirstTurn) {
        isFirstTurn = false;
        // 提示莊家出牌
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "莊家請出牌");
        send(players.get(0), new Packet(Command.GAME_UPDATE, msg));
        return;
    }
    
    // 一般摸牌流程
    Tile drawn = engine.drawTile();
    // ...
}
```

**相關檔案**:
- `src/main/java/com/mahjong/server/WebSocketGameSession.java`

**測試需求**:
```java
@Test
public void testDealerHas17TilesAtStart() {
    // 驗證莊家起手 17 張
    engine.shuffle();
    engine.dealInitialHands(hands);
    
    Tile firstDraw = engine.drawTile();
    hands.get(0).addTile(firstDraw);
    
    assertEquals(17, hands.get(0).getConnectionCount(), 
        "Dealer should have 17 tiles at start");
    assertEquals(16, hands.get(1).getConnectionCount(), 
        "Other players should have 16 tiles");
}
```

**預估時間**: 2 小時  
**困難度**: ⭐☆☆☆☆

---

### 🔴 P0-2: 胡牌結算視窗

**目標**: 胡牌後顯示結算視窗，展示台數、得分、手牌分析

**功能需求**:

1. **結算資訊顯示**
   - 胡牌者暱稱
   - 胡牌類型（自摸 / 點炮）
   - 點炮者暱稱（如果是點炮）
   - 胡牌牌型展示
   - 台數明細
   - 總分計算

2. **台數明細**
   - 基本台數（自摸 +1）
   - 番種台數（清一色 +8、碰碰胡 +4 等）
   - 特殊台數（槓上開花、海底撈月等）

3. **手牌分析**
   - 刻子（AAA）
   - 順子（ABC）
   - 對眼（DD）
   - 明槓、暗槓標示

**實作方案**:

#### 後端：新增結算資料結構
```java
// 新增檔案：src/main/java/com/mahjong/model/GameResult.java
package com.mahjong.model;

import java.util.List;
import java.util.Map;

public class GameResult {
    private int winnerIndex;
    private String winnerName;
    private WinType winType;  // SELF_DRAW, DISCARD_WIN
    private Integer loserIndex;  // 點炮者（如果有）
    private String loserName;
    
    private List<String> winningHand;  // 胡牌手牌
    private Map<String, Integer> taiDetails;  // 台數明細
    private int totalTai;
    private int basePoints;  // 底分
    private int totalPoints;  // 總分
    
    private HandAnalysis handAnalysis;  // 手牌分析
    
    // Getters and Setters...
}

// 手牌分析
public class HandAnalysis {
    private List<MeldInfo> triplets;   // 刻子
    private List<MeldInfo> sequences;  // 順子
    private MeldInfo eyes;             // 對眼
    private List<MeldInfo> kongs;      // 槓子
    
    // Getters and Setters...
}

public class MeldInfo {
    private String type;  // "TRIPLET", "SEQUENCE", "EYES", "KONG"
    private List<String> tiles;
    private boolean concealed;  // 是否暗的
    
    // Getters and Setters...
}

public enum WinType {
    SELF_DRAW,    // 自摸
    DISCARD_WIN   // 點炮
}
```

#### 後端：計算結算資訊
```java
// WebSocketGameSession.java
private synchronized void performHu(int playerIndex) {
    PlayerHand hand = hands.get(playerIndex);
    
    // 判斷胡牌類型
    boolean isSelfDraw = (pendingDiscardTile == null);
    WinType winType = isSelfDraw ? WinType.SELF_DRAW : WinType.DISCARD_WIN;
    
    // 計算台數
    ScoringCalculator calculator = new ScoringCalculator();
    int totalTai = calculator.calculateTai(hand, isSelfDraw, 
        currentRoundWind, currentSeatWind);
    
    // 台數明細
    Map<String, Integer> taiDetails = calculator.getTaiDetails(hand, 
        isSelfDraw, currentRoundWind, currentSeatWind);
    
    // 手牌分析
    HandAnalysis analysis = analyzeWinningHand(hand);
    
    // 計分
    int basePoints = 100;  // 底分（可配置）
    int totalPoints = basePoints * (int)Math.pow(2, totalTai);
    
    // 建立結算資料
    GameResult result = new GameResult();
    result.setWinnerIndex(playerIndex);
    result.setWinnerName(getNickname(playerIndex));
    result.setWinType(winType);
    
    if (!isSelfDraw) {
        result.setLoserIndex(lastDiscardPlayerIndex);
        result.setLoserName(getNickname(lastDiscardPlayerIndex));
    }
    
    result.setWinningHand(hand.getTilesStr());
    result.setTaiDetails(taiDetails);
    result.setTotalTai(totalTai);
    result.setBasePoints(basePoints);
    result.setTotalPoints(totalPoints);
    result.setHandAnalysis(analysis);
    
    // 廣播結算資訊
    Map<String, Object> data = new HashMap<>();
    data.put("result", result);
    broadcast(new Packet(Command.GAME_OVER, data));
}

// 分析胡牌手牌
private HandAnalysis analyzeWinningHand(PlayerHand hand) {
    HandAnalysis analysis = new HandAnalysis();
    
    // 使用 WinStrategy 分解手牌
    WinStrategy strategy = new WinStrategy();
    WinStrategy.HandDecomposition decomp = strategy.decomposeHand(hand);
    
    // 轉換為 MeldInfo
    analysis.setTriplets(convertToMeldInfo(decomp.getTriplets()));
    analysis.setSequences(convertToMeldInfo(decomp.getSequences()));
    analysis.setEyes(convertToMeldInfo(decomp.getEyes()));
    
    // 添加明槓、暗槓
    List<MeldInfo> kongs = new ArrayList<>();
    for (Meld meld : hand.getOpenMelds()) {
        if (meld.getType() == Meld.Type.KONG) {
            MeldInfo info = new MeldInfo();
            info.setType("KONG");
            info.setTiles(meld.getTiles().stream()
                .map(Tile::toString)
                .collect(Collectors.toList()));
            info.setConcealed(false);  // 明槓
            kongs.add(info);
        }
    }
    analysis.setKongs(kongs);
    
    return analysis;
}
```

#### 後端：擴展 ScoringCalculator
```java
// ScoringCalculator.java
public Map<String, Integer> getTaiDetails(PlayerHand hand, 
                                          boolean isSelfDraw,
                                          Tile roundWind, 
                                          Tile seatWind) {
    Map<String, Integer> details = new LinkedHashMap<>();
    
    // 基本台
    if (isSelfDraw) {
        details.put("自摸", 1);
    }
    
    // 三元牌
    if (hasPongOrKong(hand, Tile.RED)) {
        details.put("紅中", 1);
    }
    if (hasPongOrKong(hand, Tile.GREEN)) {
        details.put("青發", 1);
    }
    if (hasPongOrKong(hand, Tile.WHITE)) {
        details.put("白板", 1);
    }
    
    // 圈風、門風
    if (roundWind != null && hasPongOrKong(hand, roundWind)) {
        details.put("圈風", 1);
    }
    if (seatWind != null && hasPongOrKong(hand, seatWind)) {
        details.put("門風", 1);
    }
    
    // 花色
    if (isFullFlush(hand)) {
        details.put("清一色", 8);
    } else if (isHalfFlush(hand)) {
        details.put("混一色", 4);
    }
    
    // 碰碰胡
    if (isAllPongs(hand)) {
        details.put("碰碰胡", 4);
    }
    
    // 七對子
    WinStrategy strategy = new WinStrategy();
    if (strategy.isSevenPairs(hand)) {
        details.put("七對子", 4);
    }
    
    // 十三么
    if (strategy.isThirteenOrphans(hand)) {
        details.put("十三么", 16);
    }
    
    return details;
}
```

#### 前端：結算視窗 UI
```javascript
// game.js
function showGameResult(result) {
    // 創建遮罩
    const overlay = document.createElement('div');
    overlay.id = 'result-overlay';
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.8);
        z-index: 1000;
        display: flex;
        justify-content: center;
        align-items: center;
    `;
    
    // 創建結算視窗
    const resultWindow = document.createElement('div');
    resultWindow.style.cssText = `
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 20px;
        padding: 40px;
        min-width: 600px;
        max-width: 800px;
        color: white;
        box-shadow: 0 20px 60px rgba(0,0,0,0.5);
    `;
    
    // 標題
    const title = document.createElement('h1');
    title.style.cssText = `
        text-align: center;
        font-size: 48px;
        margin-bottom: 30px;
        text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
    `;
    
    if (result.winType === 'SELF_DRAW') {
        title.innerHTML = `🎉 ${result.winnerName} 自摸！`;
    } else {
        title.innerHTML = `🎊 ${result.winnerName} 胡牌！<br>
            <span style="font-size: 24px;">${result.loserName} 點炮</span>`;
    }
    resultWindow.appendChild(title);
    
    // 手牌展示
    const handSection = createHandDisplay(result);
    resultWindow.appendChild(handSection);
    
    // 台數明細
    const taiSection = createTaiDetails(result);
    resultWindow.appendChild(taiSection);
    
    // 總分
    const scoreSection = document.createElement('div');
    scoreSection.style.cssText = `
        background: rgba(255,255,255,0.2);
        border-radius: 10px;
        padding: 20px;
        margin-top: 20px;
        text-align: center;
    `;
    scoreSection.innerHTML = `
        <div style="font-size: 24px; margin-bottom: 10px;">
            總計：${result.totalTai} 台
        </div>
        <div style="font-size: 36px; font-weight: bold;">
            ${result.totalPoints} 分
        </div>
        <div style="font-size: 16px; margin-top: 10px; opacity: 0.8;">
            （底分 ${result.basePoints} × 2^${result.totalTai}）
        </div>
    `;
    resultWindow.appendChild(scoreSection);
    
    // 關閉按鈕
    const closeBtn = document.createElement('button');
    closeBtn.innerText = '確認';
    closeBtn.style.cssText = `
        width: 100%;
        margin-top: 30px;
        padding: 15px;
        font-size: 20px;
        background: #4CAF50;
        color: white;
        border: none;
        border-radius: 10px;
        cursor: pointer;
        transition: all 0.3s;
    `;
    closeBtn.onmouseover = () => closeBtn.style.background = '#45a049';
    closeBtn.onmouseout = () => closeBtn.style.background = '#4CAF50';
    closeBtn.onclick = () => {
        overlay.remove();
        // 可選：返回大廳或開始新局
    };
    resultWindow.appendChild(closeBtn);
    
    overlay.appendChild(resultWindow);
    document.body.appendChild(overlay);
}

function createHandDisplay(result) {
    const section = document.createElement('div');
    section.style.cssText = `
        background: rgba(255,255,255,0.1);
        border-radius: 10px;
        padding: 20px;
        margin-top: 20px;
    `;
    
    const title = document.createElement('h3');
    title.innerText = '胡牌牌型';
    title.style.marginBottom = '15px';
    section.appendChild(title);
    
    const analysis = result.handAnalysis;
    
    // 顯示刻子
    if (analysis.triplets && analysis.triplets.length > 0) {
        const tripletDiv = document.createElement('div');
        tripletDiv.innerHTML = '<strong>刻子：</strong>';
        analysis.triplets.forEach(meld => {
            tripletDiv.innerHTML += ` [${meld.tiles.join(' ')}]`;
        });
        section.appendChild(tripletDiv);
    }
    
    // 顯示順子
    if (analysis.sequences && analysis.sequences.length > 0) {
        const seqDiv = document.createElement('div');
        seqDiv.innerHTML = '<strong>順子：</strong>';
        analysis.sequences.forEach(meld => {
            seqDiv.innerHTML += ` [${meld.tiles.join(' ')}]`;
        });
        section.appendChild(seqDiv);
    }
    
    // 顯示對眼
    if (analysis.eyes) {
        const eyesDiv = document.createElement('div');
        eyesDiv.innerHTML = `<strong>對眼：</strong> [${analysis.eyes.tiles.join(' ')}]`;
        section.appendChild(eyesDiv);
    }
    
    // 顯示槓子
    if (analysis.kongs && analysis.kongs.length > 0) {
        const kongDiv = document.createElement('div');
        kongDiv.innerHTML = '<strong>槓：</strong>';
        analysis.kongs.forEach(meld => {
            const type = meld.concealed ? '暗槓' : '明槓';
            kongDiv.innerHTML += ` ${type}[${meld.tiles.join(' ')}]`;
        });
        section.appendChild(kongDiv);
    }
    
    return section;
}

function createTaiDetails(result) {
    const section = document.createElement('div');
    section.style.cssText = `
        background: rgba(255,255,255,0.1);
        border-radius: 10px;
        padding: 20px;
        margin-top: 20px;
    `;
    
    const title = document.createElement('h3');
    title.innerText = '台數明細';
    title.style.marginBottom = '15px';
    section.appendChild(title);
    
    const table = document.createElement('table');
    table.style.cssText = 'width: 100%; border-collapse: collapse;';
    
    // 台數列表
    for (const [name, tai] of Object.entries(result.taiDetails)) {
        const row = document.createElement('tr');
        row.style.borderBottom = '1px solid rgba(255,255,255,0.2)';
        
        const nameCell = document.createElement('td');
        nameCell.innerText = name;
        nameCell.style.padding = '10px 0';
        
        const taiCell = document.createElement('td');
        taiCell.innerText = `+${tai} 台`;
        taiCell.style.cssText = 'text-align: right; font-weight: bold;';
        
        row.appendChild(nameCell);
        row.appendChild(taiCell);
        table.appendChild(row);
    }
    
    section.appendChild(table);
    return section;
}

// 在 handlePacket 中處理
function handlePacket(packet) {
    if (packet.command === "GAME_OVER") {
        const result = packet.data.result;
        showGameResult(result);
    }
    // ...
}
```

#### CSS 樣式
```css
/* style.css */
#result-overlay {
    animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

.result-window {
    animation: slideIn 0.4s ease-out;
}

@keyframes slideIn {
    from {
        transform: translateY(-50px);
        opacity: 0;
    }
    to {
        transform: translateY(0);
        opacity: 1;
    }
}
```

**相關檔案**:
- `src/main/java/com/mahjong/model/GameResult.java` (新增)
- `src/main/java/com/mahjong/model/HandAnalysis.java` (新增)
- `src/main/java/com/mahjong/server/WebSocketGameSession.java` (修改)
- `src/main/java/com/mahjong/logic/ScoringCalculator.java` (擴展)
- `src/main/java/com/mahjong/logic/WinStrategy.java` (新增分解方法)
- `src/main/resources/web/game.js` (修改)
- `src/main/resources/web/style.css` (修改)

**測試需求**:
```java
@Test
public void testGameResultGeneration() {
    // 測試結算資料生成
}

@Test
public void testTaiDetailsCalculation() {
    // 測試台數明細計算
}

@Test
public void testHandAnalysis() {
    // 測試手牌分析
}
```

**預估時間**: 8 小時  
**困難度**: ⭐⭐⭐☆☆

---

### 🟠 P1-1: 暗槓功能

**目標**: 實作暗槓（手上 4 張相同牌）

**功能需求**:

1. **檢測暗槓**
   - 玩家摸牌後，檢查手上是否有 4 張相同
   - 提示玩家可以暗槓

2. **執行暗槓**
   - 從手牌移除 4 張
   - 添加暗槓面子（標記為暗的）
   - 從牌尾補 1 張牌
   - 繼續該玩家的回合

3. **暗槓計分**
   - 暗槓 +1 台（可配置）
   - 槓上開花檢測

**實作方案**:

#### 後端：暗槓檢測
```java
// ActionProcessor.java
public List<Tile> getConcealedKongOptions(PlayerHand hand) {
    List<Tile> options = new ArrayList<>();
    Map<Tile, Integer> counts = new HashMap<>();
    
    // 計算每種牌的數量
    for (Tile tile : hand.getStandingTiles()) {
        counts.put(tile, counts.getOrDefault(tile, 0) + 1);
    }
    
    // 找出數量為 4 的牌
    for (Map.Entry<Tile, Integer> entry : counts.entrySet()) {
        if (entry.getValue() == 4) {
            options.add(entry.getKey());
        }
    }
    
    return options;
}

public void executeConcealedKong(PlayerHand hand, Tile tile) {
    // 移除 4 張牌
    for (int i = 0; i < 4; i++) {
        hand.removeTile(tile);
    }
    
    // 添加暗槓面子
    Meld kong = Meld.createConcealedKong(tile);  // 新增方法
    hand.addMeld(kong);
}
```

#### Meld 類別擴展
```java
// Meld.java
public class Meld {
    private final Type type;
    private final List<Tile> tiles;
    private final boolean concealed;  // ✅ 新增：是否暗的
    
    public Meld(Type type, List<Tile> tiles, boolean concealed) {
        this.type = type;
        this.tiles = new ArrayList<>(tiles);
        this.concealed = concealed;
        validateTileCount();
    }
    
    // 明槓（舊方法，concealed = false）
    public static Meld createKong(Tile tile) {
        return new Meld(Type.KONG, 
            Arrays.asList(tile, tile, tile, tile), 
            false);
    }
    
    // ✅ 新增：暗槓
    public static Meld createConcealedKong(Tile tile) {
        return new Meld(Type.KONG, 
            Arrays.asList(tile, tile, tile, tile), 
            true);
    }
    
    public boolean isConcealed() {
        return concealed;
    }
}
```

#### 遊戲流程整合
```java
// WebSocketGameSession.java
private void startTurn() {
    Tile drawn = engine.drawTile();
    if (drawn == null) {
        // 流局
        return;
    }
    
    hands.get(currentPlayerIndex).addTile(drawn);
    broadcastState();
    
    // 1. 檢查自摸
    if (tingDetector.isWinningHand(hands.get(currentPlayerIndex))) {
        sendActionRequest(currentPlayerIndex, "HU", "SKIP");
        return;
    }
    
    // 2. ✅ 檢查暗槓
    List<Tile> concealedKongOptions = 
        processor.getConcealedKongOptions(hands.get(currentPlayerIndex));
    
    if (!concealedKongOptions.isEmpty()) {
        List<String> actions = new ArrayList<>();
        for (Tile tile : concealedKongOptions) {
            actions.add("CONCEALED_KONG " + tile.toString());
        }
        actions.add("SKIP");
        
        sendMultipleActionRequest(currentPlayerIndex, actions);
        waitingForAction = true;
        return;
    }
    
    // 3. 等待出牌
}

private void handleActionResponse(int playerIndex, Packet packet) {
    String type = (String) packet.getData().get("type");
    
    if (type.startsWith("CONCEALED_KONG ")) {
        String tileStr = type.substring(15);  // "CONCEALED_KONG M1" -> "M1"
        Tile tile = Tile.valueOf(tileStr);
        
        // 執行暗槓
        processor.executeConcealedKong(hands.get(playerIndex), tile);
        
        // 補牌
        Tile replacement = engine.drawTile();
        if (replacement != null) {
            hands.get(playerIndex).addTile(replacement);
        }
        
        broadcastMessage("Game", 
            "Player " + playerIndex + " 暗槓 " + tileStr);
        broadcastState();
        
        // 檢查槓上開花
        if (tingDetector.isWinningHand(hands.get(playerIndex))) {
            sendActionRequest(playerIndex, "HU", "SKIP");
            kongDrawWin = true;  // ✅ 標記槓上開花
            return;
        }
        
        waitingForAction = false;
        // 繼續該玩家回合（等待出牌）
    }
    // ...
}
```

**相關檔案**:
- `src/main/java/com/mahjong/logic/ActionProcessor.java` (擴展)
- `src/main/java/com/mahjong/logic/Meld.java` (擴展)
- `src/main/java/com/mahjong/server/WebSocketGameSession.java` (修改)
- `src/main/resources/web/game.js` (修改)

**測試需求**:
```java
@Test
public void testConcealedKongDetection() {
    // 測試暗槓檢測
}

@Test
public void testConcealedKongExecution() {
    // 測試暗槓執行
}

@Test
public void testConcealedKongDrawReplacement() {
    // 測試槓後補牌
}
```

**預估時間**: 4 小時  
**困難度**: ⭐⭐☆☆☆

---

### 🟠 P1-2: 補槓功能

**目標**: 實作補槓（碰後摸到第 4 張）

**功能需求**:

1. **檢測補槓**
   - 玩家摸牌後，檢查是否可以補槓
   - 已經碰過的牌，摸到第 4 張

2. **執行補槓**
   - 將碰（PONG）升級為槓（KONG）
   - 從牌尾補 1 張牌
   - 繼續該玩家的回合

3. **搶槓檢測**
   - 其他玩家可以搶槓胡牌
   - 搶槓 +1 台

**實作方案**:

#### 後端：補槓檢測
```java
// ActionProcessor.java
public List<Tile> getAddKongOptions(PlayerHand hand) {
    List<Tile> options = new ArrayList<>();
    
    // 找出已經碰過的牌
    List<Tile> pongedTiles = new ArrayList<>();
    for (Meld meld : hand.getOpenMelds()) {
        if (meld.getType() == Meld.Type.PONG) {
            pongedTiles.add(meld.getTiles().get(0));
        }
    }
    
    // 檢查手上是否有對應的牌
    for (Tile tile : pongedTiles) {
        if (hand.getStandingTiles().contains(tile)) {
            options.add(tile);
        }
    }
    
    return options;
}

public void executeAddKong(PlayerHand hand, Tile tile) {
    // 1. 找到對應的 PONG
    Meld pongToUpgrade = null;
    for (Meld meld : hand.getOpenMelds()) {
        if (meld.getType() == Meld.Type.PONG && 
            meld.getTiles().get(0).equals(tile)) {
            pongToUpgrade = meld;
            break;
        }
    }
    
    if (pongToUpgrade == null) {
        throw new IllegalStateException("No PONG found for tile: " + tile);
    }
    
    // 2. 移除舊的 PONG
    hand.removeMeld(pongToUpgrade);  // ✅ 需要新增此方法
    
    // 3. 從手牌移除第 4 張
    hand.removeTile(tile);
    
    // 4. 添加 KONG（明槓，因為是從 PONG 升級）
    Meld kong = Meld.createKong(tile);
    hand.addMeld(kong);
}
```

#### PlayerHand 擴展
```java
// PlayerHand.java
public void removeMeld(Meld meld) {
    openMelds.remove(meld);
}
```

#### 搶槓檢測
```java
// WebSocketGameSession.java
private void handleActionResponse(int playerIndex, Packet packet) {
    String type = (String) packet.getData().get("type");
    
    if (type.startsWith("ADD_KONG ")) {
        String tileStr = type.substring(9);
        Tile tile = Tile.valueOf(tileStr);
        
        // ✅ 搶槓檢測：詢問其他玩家是否要胡
        List<Integer> canRobKong = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i == playerIndex) continue;
            
            // 檢查是否可以用這張牌胡
            if (validator.canHu(hands.get(i), tile)) {
                canRobKong.add(i);
            }
        }
        
        if (!canRobKong.isEmpty()) {
            // 等待搶槓
            waitingForRobKong = true;
            pendingKongTile = tile;
            pendingKongPlayer = playerIndex;
            
            for (int i : canRobKong) {
                sendActionRequest(i, "ROB_KONG", "SKIP");
            }
            return;
        }
        
        // 沒人搶槓，執行補槓
        executeAddKongInternal(playerIndex, tile);
    }
    
    if (type.equals("ROB_KONG")) {
        // 執行搶槓胡牌
        robKongWin = true;  // ✅ 標記搶槓
        performHu(playerIndex);
    }
    // ...
}

private void executeAddKongInternal(int playerIndex, Tile tile) {
    processor.executeAddKong(hands.get(playerIndex), tile);
    
    // 補牌
    Tile replacement = engine.drawTile();
    if (replacement != null) {
        hands.get(playerIndex).addTile(replacement);
    }
    
    broadcastMessage("Game", 
        "Player " + playerIndex + " 補槓 " + tile.toString());
    broadcastState();
    
    // 檢查槓上開花
    if (tingDetector.isWinningHand(hands.get(playerIndex))) {
        sendActionRequest(playerIndex, "HU", "SKIP");
        kongDrawWin = true;
        return;
    }
    
    waitingForAction = false;
}
```

**相關檔案**:
- `src/main/java/com/mahjong/logic/ActionProcessor.java` (擴展)
- `src/main/java/com/mahjong/logic/PlayerHand.java` (擴展)
- `src/main/java/com/mahjong/server/WebSocketGameSession.java` (修改)

**測試需求**:
```java
@Test
public void testAddKongDetection() {
    // 測試補槓檢測
}

@Test
public void testAddKongExecution() {
    // 測試補槓執行
}

@Test
public void testRobKong() {
    // 測試搶槓
}
```

**預估時間**: 5 小時  
**困難度**: ⭐⭐⭐☆☆

---

## Phase 2: 進階遊戲功能

### 🟡 P2-1: 進階計分系統

**目標**: 實作更多台數類型

**新增台數**:

| 台數名稱 | 台數 | 檢測條件 |
|---------|------|---------|
| 門清 | +1 | 沒有吃碰明槓 |
| 平胡 | +1 | 4 順子 + 1 對眼（無刻子） |
| 槓上開花 | +1 | 槓後補牌自摸 |
| 搶槓 | +1 | 搶別人的補槓胡牌 |
| 海底撈月 | +1 | 最後一張牌胡牌 |
| 河底撈魚 | +1 | 別人打最後一張牌胡 |
| 天胡 | +16 | 莊家起手胡牌 |
| 地胡 | +16 | 閒家第一輪胡牌 |
| 人胡 | +8 | 第一輪吃碰後胡牌 |
| 大三元 | +16 | 中發白都碰/槓 |
| 小三元 | +8 | 中發白其中兩個碰/槓，一個對眼 |
| 大四喜 | +16 | 東南西北都碰/槓 |
| 小四喜 | +8 | 東南西北其中三個碰/槓，一個對眼 |
| 字一色 | +16 | 全部字牌 |
| 綠一色 | +16 | 全部綠色牌（23468條+發） |
| 九蓮寶燈 | +16 | 1112345678999 + 任意一張同花色 |

**實作方案**:

```java
// ScoringCalculator.java
public Map<String, Integer> getTaiDetails(PlayerHand hand, 
                                          GameContext context) {
    Map<String, Integer> details = new LinkedHashMap<>();
    
    // 基本台
    if (context.isSelfDraw) {
        details.put("自摸", 1);
    }
    
    // ✅ 門清
    if (isMenQing(hand)) {
        details.put("門清", 1);
    }
    
    // ✅ 平胡
    if (isPingHu(hand)) {
        details.put("平胡", 1);
    }
    
    // ✅ 槓上開花
    if (context.isKongDrawWin) {
        details.put("槓上開花", 1);
    }
    
    // ✅ 搶槓
    if (context.isRobKong) {
        details.put("搶槓", 1);
    }
    
    // ✅ 海底撈月
    if (context.isLastTile && context.isSelfDraw) {
        details.put("海底撈月", 1);
    }
    
    // ✅ 河底撈魚
    if (context.isLastTile && !context.isSelfDraw) {
        details.put("河底撈魚", 1);
    }
    
    // ✅ 天胡
    if (context.isDealerFirstTurn && context.isSelfDraw) {
        details.put("天胡", 16);
        return details;  // 天胡不計其他
    }
    
    // ✅ 地胡
    if (!context.isDealer && context.isFirstTurn && context.isSelfDraw) {
        details.put("地胡", 16);
        return details;  // 地胡不計其他
    }
    
    // ✅ 大三元
    if (hasBigThreeDragons(hand)) {
        details.put("大三元", 16);
    }
    
    // ✅ 小三元
    else if (hasSmallThreeDragons(hand)) {
        details.put("小三元", 8);
    }
    
    // ✅ 大四喜
    if (hasBigFourWinds(hand)) {
        details.put("大四喜", 16);
    }
    
    // ✅ 小四喜
    else if (hasSmallFourWinds(hand)) {
        details.put("小四喜", 8);
    }
    
    // ✅ 字一色
    if (isAllHonors(hand)) {
        details.put("字一色", 16);
    }
    
    // ✅ 綠一色
    if (isAllGreen(hand)) {
        details.put("綠一色", 16);
    }
    
    // ✅ 九蓮寶燈
    if (isNineTreasures(hand)) {
        details.put("九蓮寶燈", 16);
    }
    
    // ... 其他台數
    
    return details;
}

// 檢測方法
private boolean isMenQing(PlayerHand hand) {
    // 沒有吃碰明槓（只有暗槓可以）
    for (Meld meld : hand.getOpenMelds()) {
        if (meld.getType() != Meld.Type.KONG || !meld.isConcealed()) {
            return false;
        }
    }
    return true;
}

private boolean isPingHu(PlayerHand hand) {
    // 4 順子 + 1 對眼，沒有刻子
    WinStrategy.HandDecomposition decomp = 
        new WinStrategy().decomposeHand(hand);
    
    return decomp.getTriplets().isEmpty() && 
           decomp.getSequences().size() == 4 &&
           decomp.getEyes() != null;
}

private boolean hasBigThreeDragons(PlayerHand hand) {
    // 中發白都碰/槓
    return hasPongOrKong(hand, Tile.RED) &&
           hasPongOrKong(hand, Tile.GREEN) &&
           hasPongOrKong(hand, Tile.WHITE);
}

private boolean hasSmallThreeDragons(PlayerHand hand) {
    // 兩個碰/槓，一個對眼
    int pongCount = 0;
    boolean hasEyes = false;
    
    List<Tile> dragons = Arrays.asList(Tile.RED, Tile.GREEN, Tile.WHITE);
    for (Tile dragon : dragons) {
        if (hasPongOrKong(hand, dragon)) {
            pongCount++;
        } else if (hasEyes(hand, dragon)) {
            hasEyes = true;
        }
    }
    
    return pongCount == 2 && hasEyes;
}

private boolean isAllGreen(PlayerHand hand) {
    // 23468條 + 發
    List<Tile> greenTiles = Arrays.asList(
        Tile.S2, Tile.S3, Tile.S4, Tile.S6, Tile.S8, Tile.GREEN
    );
    
    for (Tile tile : hand.getAllTiles()) {
        if (!greenTiles.contains(tile)) {
            return false;
        }
    }
    return true;
}

private boolean isNineTreasures(PlayerHand hand) {
    // 1112345678999 + 任意一張同花色
    // 僅檢查基本形狀
    int[] counts = new int[9];
    Tile.Suit suit = null;
    
    for (Tile tile : hand.getStandingTiles()) {
        if (!tile.isNumberTile()) return false;
        
        if (suit == null) {
            suit = tile.getSuit();
        } else if (tile.getSuit() != suit) {
            return false;  // 必須同花色
        }
        
        counts[tile.getRank() - 1]++;
    }
    
    // 檢查 1 和 9 至少 3 張
    if (counts[0] < 3 || counts[8] < 3) return false;
    
    // 檢查 2-8 至少 1 張
    for (int i = 1; i <= 7; i++) {
        if (counts[i] < 1) return false;
    }
    
    return true;
}
```

**GameContext 資料結構**:
```java
// 新增檔案：src/main/java/com/mahjong/model/GameContext.java
public class GameContext {
    private boolean isSelfDraw;
    private boolean isKongDrawWin;      // 槓上開花
    private boolean isRobKong;          // 搶槓
    private boolean isLastTile;         // 最後一張牌
    private boolean isDealer;           // 是否莊家
    private boolean isDealerFirstTurn;  // 莊家第一輪
    private boolean isFirstTurn;        // 第一輪
    private Tile roundWind;             // 圈風
    private Tile seatWind;              // 門風
    
    // Getters and Setters...
}
```

**預估時間**: 10 小時  
**困難度**: ⭐⭐⭐⭐☆

---

### 🟡 P2-2: 聽牌提示系統

**目標**: 顯示玩家當前聽哪些牌

**功能需求**:

1. **即時聽牌檢測**
   - 每次手牌變動後檢測
   - 顯示聽牌狀態

2. **聽牌牌型展示**
   - 列出所有聽牌
   - 顯示每張牌的剩餘數量

3. **向聽數顯示**
   - 0 向聽 = 聽牌
   - 1 向聽 = 一張進聽
   - 2 向聽 = 兩張進聽

**實作方案**:

```javascript
// game.js
function renderState(state) {
    // ... 現有渲染邏輯
    
    // ✅ 顯示聽牌提示
    if (state.tingInfo) {
        showTingHint(state.tingInfo);
    }
}

function showTingHint(tingInfo) {
    // 移除舊提示
    const oldHint = document.getElementById('ting-hint');
    if (oldHint) oldHint.remove();
    
    if (!tingInfo.isTing) {
        // 顯示向聽數
        if (tingInfo.shanten > 0) {
            const hint = document.createElement('div');
            hint.id = 'ting-hint';
            hint.style.cssText = `
                position: absolute;
                top: 50px;
                right: 20px;
                background: rgba(255,152,0,0.9);
                color: white;
                padding: 10px 20px;
                border-radius: 10px;
                font-size: 16px;
                z-index: 50;
            `;
            hint.innerHTML = `${tingInfo.shanten} 向聽`;
            document.getElementById('game-table').appendChild(hint);
        }
        return;
    }
    
    // 聽牌提示
    const hint = document.createElement('div');
    hint.id = 'ting-hint';
    hint.style.cssText = `
        position: absolute;
        top: 50px;
        right: 20px;
        background: rgba(76,175,80,0.9);
        color: white;
        padding: 15px 25px;
        border-radius: 15px;
        z-index: 50;
        box-shadow: 0 4px 15px rgba(0,0,0,0.3);
    `;
    
    let html = '<div style="font-size: 18px; font-weight: bold; margin-bottom: 10px;">🎯 聽牌</div>';
    html += '<div style="font-size: 14px;">等待：</div>';
    html += '<div style="display: flex; flex-wrap: wrap; gap: 5px; margin-top: 5px;">';
    
    tingInfo.tingTiles.forEach(tile => {
        const remaining = tingInfo.remaining[tile] || 0;
        html += `
            <div style="
                background: white;
                color: #2c3e50;
                padding: 5px 10px;
                border-radius: 5px;
                font-weight: bold;
                font-size: 14px;
            ">
                ${tile} <span style="font-size: 10px; color: #666;">(${remaining})</span>
            </div>
        `;
    });
    
    html += '</div>';
    hint.innerHTML = html;
    
    document.getElementById('game-table').appendChild(hint);
}
```

**後端支援**:
```java
// WebSocketGameSession.java
private void broadcastState() {
    for (int i = 0; i < 4; i++) {
        Map<String, Object> state = new HashMap<>();
        // ... 現有狀態
        
        // ✅ 添加聽牌資訊
        TingDetector.TingResult tingResult = 
            tingDetector.detectTing(hands.get(i));
        
        Map<String, Object> tingInfo = new HashMap<>();
        tingInfo.put("isTing", tingResult.isTing());
        tingInfo.put("tingTiles", tingResult.getTingTiles().stream()
            .map(Tile::toString)
            .collect(Collectors.toList()));
        
        // 計算剩餘數量
        Map<String, Integer> remaining = new HashMap<>();
        for (Tile tile : tingResult.getTingTiles()) {
            int count = countRemainingTiles(tile);
            remaining.put(tile.toString(), count);
        }
        tingInfo.put("remaining", remaining);
        
        // 向聽數（可選）
        int shanten = calculateShanten(hands.get(i));
        tingInfo.put("shanten", shanten);
        
        state.put("tingInfo", tingInfo);
        
        send(players.get(i), new Packet(Command.GAME_UPDATE, state));
    }
}

private int countRemainingTiles(Tile tile) {
    int total = 4;  // 每種牌 4 張
    
    // 扣除已打出的
    total -= (int) sea.stream()
        .filter(t -> t.equals(tile.toString()))
        .count();
    
    // 扣除所有玩家手上的（包括自己）
    for (PlayerHand hand : hands) {
        total -= (int) hand.getStandingTiles().stream()
            .filter(t -> t.equals(tile))
            .count();
        
        // 扣除明牌中的
        for (Meld meld : hand.getOpenMelds()) {
            total -= (int) meld.getTiles().stream()
                .filter(t -> t.equals(tile))
                .count();
        }
    }
    
    return Math.max(0, total);
}
```

**預估時間**: 4 小時  
**困難度**: ⭐⭐☆☆☆

---

## 技術債務清理

### 🟠 T1: 配置 Jacoco 並查看覆蓋率報告

**目標**: 確認達成 90% Branch Coverage

**執行步驟**:
```bash
# 執行測試並生成報告
mvn clean test jacoco:report

# 查看報告
open target/site/jacoco/index.html
```

**改進方向**:
- 針對未覆蓋的分支撰寫測試
- 增加邊界條件測試
- 增加異常處理測試

**預估時間**: 4 小時  
**困難度**: ⭐⭐☆☆☆

---

### 🟠 T2: 重構 WebSocketGameSession

**目標**: 降低單一類別複雜度

**問題**: 
- 單一類別超過 600 行
- WMC 約 45（過高）

**重構方案**:
```
WebSocketGameSession (主控制器, WMC ~15)
    ├── GameFlowManager (遊戲流程, WMC ~20)
    ├── ActionResolver (動作解析, WMC ~25)
    └── StateManager (狀態同步, WMC ~10)
```

**預估時間**: 8 小時  
**困難度**: ⭐⭐⭐☆☆

---

### 🟡 T3: 引入日誌系統

**目標**: 使用 SLF4J + Logback 取代 System.out.println()

**設定**:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.9</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.11</version>
</dependency>
```

**使用範例**:
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketGameSession {
    private static final Logger logger = 
        LoggerFactory.getLogger(WebSocketGameSession.class);
    
    public void start() {
        logger.info("Game session starting with {} players", players.size());
        // ...
    }
}
```

**預估時間**: 3 小時  
**困難度**: ⭐☆☆☆☆

---

### 🟡 T4: 統一錯誤處理

**目標**: 建立錯誤處理機制

**實作**:
```java
// GameErrorHandler.java
public class GameErrorHandler {
    private static final Logger logger = 
        LoggerFactory.getLogger(GameErrorHandler.class);
    
    public static void handle(Exception e, WebSocket conn, String context) {
        logger.error("Error in {}: {}", context, e.getMessage(), e);
        
        Map<String, Object> error = new HashMap<>();
        error.put("type", e.getClass().getSimpleName());
        error.put("message", getUserFriendlyMessage(e));
        error.put("code", getErrorCode(e));
        
        sendPacket(conn, new Packet(Command.ERROR, error));
    }
    
    private static String getUserFriendlyMessage(Exception e) {
        if (e instanceof IllegalStateException) {
            return "遊戲狀態錯誤，請重新整理頁面";
        } else if (e instanceof IllegalArgumentException) {
            return "無效的操作";
        } else {
            return "發生錯誤，請稍後再試";
        }
    }
    
    private static int getErrorCode(Exception e) {
        // 定義錯誤代碼
        return 500;
    }
}
```

**預估時間**: 4 小時  
**困難度**: ⭐⭐☆☆☆

---

## 測試與品質提升

### 🔴 Q1: 補齊單元測試

**目標**: 測試數量達到 80+

**待補充測試**:
- TingDetectorTest: 10 tests
- ScoringCalculatorTest (進階): 15 tests
- MahjongRuleEngineTest (進階): 5 tests
- WebSocketGameSessionTest (Mock): 10 tests

**預估時間**: 10 小時  
**困難度**: ⭐⭐⭐☆☆

---

### 🟠 Q2: 整合測試擴充

**目標**: 覆蓋完整遊戲流程

**測試場景**:
1. 完整遊戲流程（發牌→胡牌→結算）
2. 暗槓→補牌→槓上開花
3. 補槓→搶槓
4. 多人同時喊胡（優先級）
5. 流局處理
6. 斷線重連

**預估時間**: 8 小時  
**困難度**: ⭐⭐⭐☆☆

---
