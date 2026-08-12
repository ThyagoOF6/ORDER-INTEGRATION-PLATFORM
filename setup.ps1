#!/usr/bin/env pwsh
# Setup Script for Order Integration Platform
# Pré-requisitos: Java 21+ instalado

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Order Integration Platform - Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Java
Write-Host "✓ Verificando Java..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Java encontrado:" -ForegroundColor Green
    Write-Host "  $($javaVersion[0])" -ForegroundColor Green
} else {
    Write-Host "✗ ERRO: Java não encontrado!" -ForegroundColor Red
    Write-Host "  Instale Java 21+ e adicione ao PATH" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✓ Baixando Gradle wrapper..." -ForegroundColor Yellow

# URLs
$gradleUrl = "https://services.gradle.org/distributions/gradle-8.5-bin.zip"
$wrapperJarUrl = "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar"

# Criar diretório gradle/wrapper se não existir
$wrapperDir = "gradle/wrapper"
if (-not (Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

# Download gradle-wrapper.jar
$jarPath = "$wrapperDir/gradle-wrapper.jar"
if (-not (Test-Path $jarPath)) {
    try {
        Invoke-WebRequest -Uri $wrapperJarUrl -OutFile $jarPath -UseBasicParsing
        Write-Host "✓ gradle-wrapper.jar baixado" -ForegroundColor Green
    } catch {
        Write-Host "! Aviso: Não foi possível baixar gradle-wrapper.jar" -ForegroundColor Yellow
        Write-Host "  Continuando com Gradle download (pode ser mais lento)..." -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "✓ Compilando projeto..." -ForegroundColor Yellow
Write-Host ""

# Executar build
& .\gradlew.bat build --refresh-dependencies 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✓ Setup concluído com sucesso!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Próximos passos:" -ForegroundColor Cyan
    Write-Host "  1. Abra em VS Code:  code ." -ForegroundColor White
    Write-Host "  2. Explore a estrutura de módulos" -ForegroundColor White
    Write-Host "  3. Rode testes:      .\gradlew.bat test" -ForegroundColor White
    Write-Host "  4. Inicie app:       .\gradlew.bat bootRun -p bootstrap" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "✗ Build falhou!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Dica: Verifique que Java 21+ está instalado" -ForegroundColor Yellow
    exit 1
}
