@echo off
REM Script para testar a conexão com IA usando as chaves do .env
REM Executa os testes de integração com a IA

echo.
echo ========================================
echo  TESTE DE CONEXAO COM IA (Groq + OpenRouter)
echo ========================================
echo.

REM Navega para a pasta do projeto
cd /d "%~dp0ticketsecure"

echo [1/3] Limpando compilações anteriores...
call mvnw clean

echo.
echo [2/3] Compilando projeto...
call mvnw compile

echo.
echo [3/3] Executando testes de integração com IA...
echo.
call mvnw test -Dtest=AiAgentIntegrationTest -X

echo.
echo ========================================
echo  FIM DOS TESTES
echo ========================================
echo.
echo Resultados:
echo   - Se viu "✅ [SUCESSO]" = IA conectada e respondendo
echo   - Se viu "❌ Falha" = Verifique as chaves em .env
echo   - Se viu "instabilidades" = Ambos provedores falharam
echo.
pause
