@echo off
chcp 65001 >nul
echo 🚀 啟動麻將遊戲伺服器
echo ========================================
echo.

REM 步驟 1: 配置環境變數
echo 📋 步驟 1: 配置環境變數...
set JAVA_DIR=%USERPROFILE%\DevelopmentTools\jdk-17
set MAVEN_DIR=%USERPROFILE%\DevelopmentTools

REM 查找 Maven 目錄
for /d %%i in ("%MAVEN_DIR%\apache-maven*") do set MAVEN_DIR=%%i

if not exist "%JAVA_DIR%\bin\java.exe" (
    echo ❌ Java 17 未找到: %JAVA_DIR%
    echo    請先執行 install_env.ps1 安裝 Java 和 Maven
    pause
    exit /b 1
)

if not exist "%MAVEN_DIR%\bin\mvn.cmd" (
    echo ❌ Maven 未找到: %MAVEN_DIR%
    echo    請先執行 install_env.ps1 安裝 Maven
    pause
    exit /b 1
)

set JAVA_HOME=%JAVA_DIR%
set MAVEN_HOME=%MAVEN_DIR%
set PATH=%JAVA_DIR%\bin;%MAVEN_DIR%\bin;%PATH%

echo    ✅ 環境變數已配置
echo.

REM 步驟 2: 停止舊的 Java 進程
echo 📋 步驟 2: 清理舊進程...
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I /N "java.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo    正在停止運行中的 Java 進程...
    taskkill /F /IM java.exe >nul 2>&1
    timeout /t 1 /nobreak >nul
    echo    ✅ 舊進程已停止
) else (
    echo    ℹ️  沒有運行中的 Java 進程
)
echo.

REM 步驟 3: 編譯專案（如果需要）
echo 📋 步驟 3: 檢查並編譯專案...
if not exist "target\classes" (
    echo    正在編譯專案...
    call "%MAVEN_DIR%\bin\mvn.cmd" clean compile
    if errorlevel 1 (
        echo    ❌ 編譯失敗
        pause
        exit /b 1
    )
    echo    ✅ 編譯完成
) else (
    echo    ✅ 專案已編譯
)
echo.

REM 步驟 4: 啟動伺服器
echo 📋 步驟 4: 啟動 WebSocket 伺服器...
echo    伺服器將在端口 8888 上運行
echo    按 Ctrl+C 停止伺服器
echo.

call "%MAVEN_DIR%\bin\mvn.cmd" exec:java -Dexec.mainClass="com.mahjong.server.MahjongWebSocketServer"

pause

