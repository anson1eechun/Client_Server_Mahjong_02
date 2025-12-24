#!/bin/bash

# Client-Server Mahjong 快速 Demo 啟動腳本
# 用途：一鍵啟動伺服器和 4 個瀏覽器視窗

echo "🎮 Client-Server Mahjong Demo 啟動腳本"
echo "========================================"
echo ""

# 1. 清理舊程序
echo "📋 步驟 1: 清理舊程序..."
killall java 2>/dev/null
if [ $? -eq 0 ]; then
    echo "   ✅ 已清理舊的 Java 程序"
else
    echo "   ℹ️  沒有運行中的 Java 程序"
fi
sleep 1

# 2. 啟動伺服器（背景執行）
echo ""
echo "📋 步驟 2: 啟動 WebSocket 伺服器..."
cd "$(dirname "$0")"
mvn exec:java -Dexec.mainClass="com.mahjong.server.MahjongWebSocketServer" > /dev/null 2>&1 &
SERVER_PID=$!

# 等待伺服器啟動
echo "   ⏳ 等待伺服器啟動（3 秒）..."
sleep 3

# 檢查伺服器是否啟動成功
if ps -p $SERVER_PID > /dev/null; then
    echo "   ✅ 伺服器已啟動（PID: $SERVER_PID）"
else
    echo "   ❌ 伺服器啟動失敗，請檢查錯誤訊息"
    exit 1
fi

# 3. 開啟 4 個瀏覽器視窗
echo ""
echo "📋 步驟 3: 開啟 4 個瀏覽器視窗..."
WEB_PATH="src/main/resources/web/index.html"

if [ ! -f "$WEB_PATH" ]; then
    echo "   ❌ 找不到 $WEB_PATH"
    exit 1
fi

for i in {1..4}; do
    open "$WEB_PATH"
    echo "   ✅ 已開啟瀏覽器視窗 $i"
    sleep 1
done

echo ""
echo "========================================"
echo "✅ Demo 準備完成！"
echo ""
echo "📝 下一步操作："
echo "   1. 在 4 個瀏覽器視窗中分別輸入暱稱："
echo "      - Player1, Player2, Player3, Player4"
echo "   2. 點擊各視窗的 'Start Game' 按鈕"
echo "   3. 等待 4 位玩家全部連線後，遊戲自動開始"
echo ""
echo "💡 提示："
echo "   - 伺服器正在背景執行（PID: $SERVER_PID）"
echo "   - 要停止伺服器，請執行：kill $SERVER_PID"
echo "   - 或執行：killall java"
echo ""
echo "🎉 祝 Demo 順利！"

