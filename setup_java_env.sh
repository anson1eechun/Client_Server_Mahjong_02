#!/bin/bash
# 設置此專案所需的 Java 環境（使用 Java 23 編譯 Java 21 程式碼）
# 使用方法：source setup_java_env.sh 或 . setup_java_env.sh

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 驗證設置
echo "✅ Java 環境已設置（使用 Java 23 編譯 Java 21 程式碼）"
echo "   JAVA_HOME: $JAVA_HOME"
echo "   Java 版本："
java -version
echo ""
echo "💡 提示：現在可以執行 mvn 命令了"

