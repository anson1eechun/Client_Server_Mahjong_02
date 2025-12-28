# Quick Start Script for Mahjong Game Server
# Run this after setting execution policy: Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

Write-Host "🚀 Starting Mahjong Game Server" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Configure environment variables
Write-Host "📋 Step 1: Configuring environment variables..." -ForegroundColor Yellow
$javaDir = "$env:USERPROFILE\DevelopmentTools\jdk-17"
$mavenDir = Get-ChildItem "$env:USERPROFILE\DevelopmentTools" -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like "*maven*" } | Select-Object -First 1

if (-not $mavenDir) {
    Write-Host "❌ Maven installation directory not found" -ForegroundColor Red
    Write-Host "   Please run install_env.ps1 first to install Maven" -ForegroundColor Yellow
    exit 1
}

$env:JAVA_HOME = $javaDir
$env:MAVEN_HOME = $mavenDir.FullName
$env:PATH = "$javaDir\bin;$($mavenDir.FullName)\bin;$env:PATH"

Write-Host "   ✅ Environment variables configured" -ForegroundColor Green
Write-Host ""

# Step 2: Stop old Java processes
Write-Host "📋 Step 2: Cleaning up old processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "   Stopping running Java processes..." -ForegroundColor Gray
    $javaProcesses | Stop-Process -Force
    Start-Sleep -Seconds 1
    Write-Host "   ✅ Old processes stopped" -ForegroundColor Green
} else {
    Write-Host "   ℹ️  No running Java processes" -ForegroundColor Gray
}

Write-Host ""

# Step 3: Compile project (if needed)
Write-Host "📋 Step 3: Checking and compiling project..." -ForegroundColor Yellow
if (-not (Test-Path "target\classes")) {
    Write-Host "   Compiling project..." -ForegroundColor Gray
    mvn clean compile
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   ❌ Compilation failed" -ForegroundColor Red
        exit 1
    }
    Write-Host "   ✅ Compilation completed" -ForegroundColor Green
} else {
    Write-Host "   ✅ Project already compiled" -ForegroundColor Green
}

Write-Host ""

# Step 4: Start server
Write-Host "📋 Step 4: Starting WebSocket server..." -ForegroundColor Yellow
Write-Host "   Server will run on port 8888" -ForegroundColor Gray
Write-Host "   Press Ctrl+C to stop the server" -ForegroundColor Gray
Write-Host ""

mvn exec:java -Dexec.mainClass="com.mahjong.server.MahjongWebSocketServer"

