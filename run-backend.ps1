# run-backend.ps1
Clear-Host

# 1. Defina sua chave de forma limpa
$groqKey = $env:GROQ_API_KEY
$env:GROQ_API_KEY = $groqKey

Write-Host "=======================================================" -ForegroundColor Green
Write-Host " INICIALIZANDO BACKEND DO TICKETSECURE COM SEGURANCA" -ForegroundColor Green
Write-Host "=======================================================" -ForegroundColor Green
Write-Host " -> Variavel [GROQ_API_KEY] injetada na memoria." -ForegroundColor Cyan
Write-Host " -> Subindo o servidor Spring Boot..." -ForegroundColor Yellow
Write-Host "-------------------------------------------------------"

# Executa o ciclo do Maven
cd .\ticketsecure\
.\mvnw clean spring-boot:run


