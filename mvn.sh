#!/bin/bash
# Maven 包裝腳本 - 自動設置 Java 21 環境並執行 Maven 命令
# 使用方法：./mvn.sh test 或 ./mvn.sh compile 等

# 獲取腳本所在目錄
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 設置 Java 環境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 執行 Maven 命令
mvn "$@"

