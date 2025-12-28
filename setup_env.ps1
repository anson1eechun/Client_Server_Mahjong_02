# Quick environment setup script
# Run this script in a new PowerShell terminal to configure Java and Maven
# Usage: .\setup_env.ps1

Write-Host "🔧 Configuring Java 17 and Maven environment variables..." -ForegroundColor Cyan
Write-Host ""

# Set Java and Maven paths
$javaDir = "$env:USERPROFILE\DevelopmentTools\jdk-17"
$mavenDir = Get-ChildItem "$env:USERPROFILE\DevelopmentTools" -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like "*maven*" } | Select-Object -First 1

if (-not $mavenDir) {
    Write-Host "❌ Maven installation directory not found" -ForegroundColor Red
    Write-Host "   Please run install_env.ps1 first to install Maven" -ForegroundColor Yellow
    exit 1
}

# Configure environment variables (current session only)
$env:JAVA_HOME = $javaDir
$env:MAVEN_HOME = $mavenDir.FullName
$env:PATH = "$javaDir\bin;$($mavenDir.FullName)\bin;$env:PATH"

Write-Host "✅ Environment variables configured (current session)" -ForegroundColor Green
Write-Host ""
Write-Host "Java version:" -ForegroundColor Yellow
& "$javaDir\bin\java.exe" -version 2>&1 | Select-String "version" | Select-Object -First 1
Write-Host ""
Write-Host "Maven version:" -ForegroundColor Yellow
& "$($mavenDir.FullName)\bin\mvn.cmd" -version 2>&1 | Select-String "Apache Maven" | Select-Object -First 1
Write-Host ""
Write-Host "💡 Tip: This configuration is only valid for current PowerShell session" -ForegroundColor Gray
Write-Host "   To make it permanent, see installation guide for permanent setup steps" -ForegroundColor Gray
