#!/bin/bash
# Script para testar a conexão com os modelos de IA

echo "🔧 VERIFICADOR DE CONEXÃO IA - TicketSecure"
echo "==========================================="
echo ""

# Verifica se as variáveis de ambiente estão configuradas
echo "1️⃣  Verificando variáveis de ambiente..."

if [ -z "$GROQ_API_KEY" ]; then
    echo "❌ GROQ_API_KEY não está definida"
    echo "   Configure com: export GROQ_API_KEY=sua_chave"
else
    echo "✅ GROQ_API_KEY está configurada (${GROQ_API_KEY:0:10}...)"
fi

if [ -z "$OPENROUTER_API_KEY" ]; then
    echo "❌ OPENROUTER_API_KEY não está definida"
    echo "   Configure com: export OPENROUTER_API_KEY=sua_chave"
else
    echo "✅ OPENROUTER_API_KEY está configurada (${OPENROUTER_API_KEY:0:10}...)"
fi

echo ""
echo "2️⃣  Testando conectividade básica com Groq..."
curl -s -X POST https://api.groq.com/openai/v1/chat/completions \
  -H "Authorization: Bearer $GROQ_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model": "llama3-7b-versatile", "messages": [{"role": "user", "content": "Hi"}], "temperature": 0.2}' \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Conexão com Groq bem-sucedida"
else
    echo "❌ Falha ao conectar com Groq"
fi

echo ""
echo "3️⃣  Testando conectividade básica com OpenRouter..."
curl -s -X POST https://openrouter.ai/api/v1/chat/completions \
  -H "Authorization: Bearer $OPENROUTER_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model": "nvidia/nemotron-3-super-120b-a12b:free", "messages": [{"role": "user", "content": "Hi"}], "temperature": 0.2}' \
  > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Conexão com OpenRouter bem-sucedida"
else
    echo "❌ Falha ao conectar com OpenRouter"
fi

echo ""
echo "4️⃣  Para executar os testes de integração:"
echo "   mvn test -Dtest=AiAgentIntegrationTest"
echo ""
echo "   Para testes unitários com Mock:"
echo "   mvn test -Dtest=AiAgentControllerTest"
