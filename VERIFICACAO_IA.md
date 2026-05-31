# 🤖 Guia de Verificação da Conexão com IA

## Status Atual
- **Testes Unitários**: ✅ Usam Mock (não testam IA real)
- **Testes de Integração**: ❌ Criados mas precisam de chaves de API
- **Problema Principal**: Chaves da API estão com valor padrão "teste"

---

## 1️⃣ Configurar Variáveis de Ambiente

### Windows (PowerShell)
```powershell
# Groq API
[Environment]::SetEnvironmentVariable("GROQ_API_KEY", "sua_chave_aqui", "User")

# OpenRouter API
[Environment]::SetEnvironmentVariable("OPENROUTER_API_KEY", "sua_chave_aqui", "User")

# Verificar se foi configurado
$env:GROQ_API_KEY
$env:OPENROUTER_API_KEY
```

### Windows (CMD)
```cmd
setx GROQ_API_KEY sua_chave_aqui
setx OPENROUTER_API_KEY sua_chave_aqui
```

### Linux/Mac
```bash
export GROQ_API_KEY=sua_chave_aqui
export OPENROUTER_API_KEY=sua_chave_aqui
```

---

## 2️⃣ Obter as Chaves de API

### 🔑 Groq (Recomendado - Grátis)
1. Acesse: https://console.groq.com
2. Crie uma conta
3. Vá para "API Keys"
4. Copie a chave

### 🔑 OpenRouter (Backup)
1. Acesse: https://openrouter.ai
2. Crie uma conta
3. Vá para "Keys"
4. Copie a chave

---

## 3️⃣ Executar os Testes

### Teste Unitário (com Mock - sempre passa)
```bash
cd ticketsecure
./mvnw test -Dtest=AiAgentControllerTest
```

### Teste de Integração (testa IA real)
```bash
cd ticketsecure
./mvnw test -Dtest=AiAgentIntegrationTest
```

### Teste Específico
```bash
# Apenas o teste de conexão com serviço
./mvnw test -Dtest=AiAgentIntegrationTest::deveTestarServicoAiAgentDiretamente

# Apenas o teste de múltiplas requisições
./mvnw test -Dtest=AiAgentIntegrationTest::deveProcessarMultiplasRequisicoes
```

---

## 4️⃣ Verificar Conectividade Rápida

### Com cURL (Windows PowerShell)
```powershell
# Teste Groq
$headers = @{
    "Authorization" = "Bearer $env:GROQ_API_KEY"
    "Content-Type" = "application/json"
}

$body = @{
    model = "llama3-7b-versatile"
    messages = @(
        @{ role = "user"; content = "Olá, você está funcionando?" }
    )
    temperature = 0.2
} | ConvertTo-Json

Invoke-RestMethod -Uri "https://api.groq.com/openai/v1/chat/completions" `
  -Method POST -Headers $headers -Body $body
```

### Com cURL (Linux/Mac)
```bash
curl -X POST https://api.groq.com/openai/v1/chat/completions \
  -H "Authorization: Bearer $GROQ_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama3-7b-versatile",
    "messages": [{"role": "user", "content": "Olá"}],
    "temperature": 0.2
  }'
```

---

## 5️⃣ O que Cada Teste Valida

| Teste | Objetivo | Testa Conexão Real? |
|-------|----------|-------------------|
| `deveRetornarRespostaDaIAComSucesso` | Valida chamada HTTP do Controller | ❌ Mock |
| `deveRetornarRespostaDaIAComSucesso2` | Valida estrutura de resposta | ❌ Mock |
| `deveConectarComOModeloERetornarResposta` | **Testa IA real** | ✅ Real |
| `deveTestarServicoAiAgentDiretamente` | **Testa serviço direto** | ✅ Real |
| `deveProcessarMultiplasRequisicoes` | **Testa resiliência** | ✅ Real |
| `deveRejeitarMensagemVazia` | Valida validação de input | ❌ Mock |
| `deveTentarFallbackSeGroqFalhar` | **Testa fallback Groq→OpenRouter** | ✅ Real |

---

## 6️⃣ Interpretar Resultados

### ✅ Sucesso
```
✅ [SUCESSO] Resposta da IA recebida:
Por favor, aguarde enquanto consulto o banco de dados para os filmes de amanhã.
```

### ❌ Erro: Chave Inválida
```
Unauthorized. Did you forget to set the GROQ_API_KEY?
```

### ❌ Erro: Quota Esgotada
```
429 Too Many Requests - você excedeu o limite da API
```

### ⚠️ Fallback Ativado
```
⚠️ Aviso: Todos os provedores de IA estão indisponíveis
   Verifique se as chaves de API estão corretas nas variáveis de ambiente
```

---

## 7️⃣ Troubleshooting

| Problema | Solução |
|----------|---------|
| `Unauthorized` | Verifique se a chave está correta em `application.properties` ou variáveis de ambiente |
| `Connection refused` | Verifique internet ou se a API está acessível |
| `Null response` | Verifique o formato da resposta da API (pode ter mudado) |
| `429 Too Many Requests` | Aguarde ou atualize a chave com quota disponível |
| Teste passa mas resposta é "instabilidades" | Todas as APIs falharam - verifique chaves e quota |

---

## 8️⃣ Arquitetura de Fallback

```
┌─────────────────────┐
│   Requisição User   │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────┐
    │  Groq API 1  │ ──► ❌ Falha
    │ (llama3-7b)  │
    └──────────────┘
           │
           ▼
    ┌──────────────────┐
    │  Groq API 2      │ ──► ❌ Falha
    │ (llama-3.1-8b)   │
    └──────────────────┘
           │
           ▼
    ┌──────────────────────┐
    │  OpenRouter API      │ ──► ✅ Sucesso
    │ (nemotron-3-super)   │
    └──────────────────────┘
           │
           ▼
   ┌───────────────────┐
   │  Resposta Usuário │
   └───────────────────┘
```

---

## 9️⃣ Próximos Passos

1. ✅ Configure as variáveis de ambiente (Passo 1)
2. ✅ Obtenha as chaves (Passo 2)
3. ✅ Execute `./mvnw test -Dtest=AiAgentIntegrationTest`
4. ✅ Verifique se há respostas da IA
5. ✅ Se falhar, use as dicas de Troubleshooting (Passo 7)

---

## 🔗 Referências
- [Documentação Groq](https://console.groq.com/docs)
- [Documentação OpenRouter](https://openrouter.ai/docs)
- [Modelos disponíveis Groq](https://console.groq.com/docs/models)
