#!/bin/bash

# Git 問題排查與修復腳本

echo "🔧 Git 問題排查與修復"
echo "======================"
echo ""

# 1. 檢查並清理鎖定文件
echo "1. 檢查 Git 鎖定文件..."
if [ -f .git/index.lock ]; then
    echo "   ⚠️  發現鎖定文件，正在清理..."
    rm -f .git/index.lock
    echo "   ✅ 鎖定文件已清除"
else
    echo "   ✅ 沒有鎖定文件"
fi

# 2. 檢查卡住的 Git 進程
echo ""
echo "2. 檢查 Git 進程..."
GIT_PROCS=$(ps aux | grep -E "git (status|add|commit|push)" | grep -v grep | awk '{print $2}')
if [ -n "$GIT_PROCS" ]; then
    echo "   ⚠️  發現卡住的 Git 進程："
    ps aux | grep -E "git (status|add|commit|push)" | grep -v grep
    echo ""
    read -p "   是否要終止這些進程？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "$GIT_PROCS" | xargs kill -9 2>/dev/null
        echo "   ✅ 進程已終止"
    fi
else
    echo "   ✅ 沒有卡住的 Git 進程"
fi

# 3. 測試 Git 狀態
echo ""
echo "3. 測試 Git 狀態（使用 --porcelain 模式，較快）..."
time git status --porcelain > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "   ✅ Git 狀態正常"
    git status --porcelain | head -10
else
    echo "   ❌ Git 狀態仍有問題"
fi

echo ""
echo "======================"
echo "✅ 排查完成"

