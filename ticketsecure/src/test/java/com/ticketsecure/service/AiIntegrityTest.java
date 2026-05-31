package com.ticketsecure.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;

@SpringBootTest(properties = {
    "llm.groq.key=${GROQ_API_KEY:}"
})
public class AiIntegrityTest {

    // Extraídos EXATAMENTE da sua lista oficial de disponíveis:
    private static final String GROQ_MODEL = "llama-3.1-8b-instant";
    private static final String OPENROUTER_MODEL = "openai/gpt-oss-20b:free";

    
    @Test
    @DisplayName("Teste de Integridade de Chaves - Verifica se as chaves de API estão operacionais")
    @Disabled("Teste de integridade de chaves - Execute manualmente para verificar as chaves de API antes de rodar outros testes.")
    public void testIntegridadeChavesIA() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        // 1. Coleta as chaves diretamente do Sistema Operacional
        String groqKey = System.getenv("GROQ_API_KEY");
        System.out.println("groqKey: " + groqKey);
        String orKey = System.getenv("OPENROUTER_API_KEY");

        // BACKUP: Se o seu terminal de teste não tiver as variáveis globais, 
        // substitua as strings abaixo pelas suas chaves literais para testar:
        if (groqKey == null || groqKey.isBlank()) {
            groqKey = "gsk_....";
        }
        if (orKey == null || orKey.isBlank()) {
            orKey = "sk-or-v1-...";
        }

        System.out.println("\n=======================================================");
        System.out.println("🧪 INICIANDO VERIFICAÇÃO DE INTEGRIDADE DE CHAVES");
        System.out.println("=======================================================");

        // -----------------------------------------------------------------
        // 🛠️ TESTE 1: GROQ VIA URL OFICIAL
        // -----------------------------------------------------------------
        System.out.println("\n[🤖 ROBO] Testando conexão com a API da Groq...");
        String groqPayload = "{\"model\": \"" + GROQ_MODEL + "\", \"messages\": [{\"role\": \"user\", \"content\": \"ping\"}]}";

        HttpRequest groqRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Authorization", "Bearer " + groqKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(groqPayload))
                .build();

        HttpResponse<String> groqResponse = client.send(groqRequest, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("[📥 STATUS HTTP GROQ]: " + groqResponse.statusCode());
        if (groqResponse.statusCode() != 200) {
            System.out.println("[❌ REJEITADO PELA GROQ]: " + groqResponse.body());
        } else {
            System.out.println("[✅ GROQ OPERANTE E INTEGRADA COM SUCESSO!]");
        }

        // -----------------------------------------------------------------
        // 🛠️ TESTE 2: OPENROUTER VIA URL OFICIAL
        // -----------------------------------------------------------------
        System.out.println("\n[🤖 ROBO] Testando conexão com a API da OpenRouter...");
        // String orPayload = "{\"model\": \"meta-llama/llama-3.3-70b-versatile\", \"messages\": [{\"role\": \"user\", \"content\": \"ping\"}]}";
        String orPayload = "{\"model\": \"" + OPENROUTER_MODEL + "\", \"messages\": [{\"role\": \"user\", \"content\": \"ping\"}]}";


        HttpRequest orRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .header("Authorization", "Bearer " + orKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(orPayload))
                .build();

        HttpResponse<String> orResponse = client.send(orRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("[📥 STATUS HTTP OPENROUTER]: " + orResponse.statusCode());
        if (orResponse.statusCode() != 200) {
            System.out.println("[❌ REJEITADO PELO OPENROUTER]: " + orResponse.body());
        } else {
            System.out.println("[✅ OPENROUTER OPERANTE E INTEGRADA COM SUCESSO!]");
        }

        System.out.println("\n=======================================================");
        boolean sucessoGeral = (groqResponse.statusCode() == 200 || orResponse.statusCode() == 200);
        assertTrue(sucessoGeral, "Erro: Ambos os provedores recusaram a comunicação de rede.");
    }
}