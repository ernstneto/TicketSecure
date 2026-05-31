package com.ticketsecure.dto.llm;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * O pacote completo enviado para a API de Inteligência Artificial.
 * @param model O nome do modelo (ex: "llama3-8b-8192" na Groq ou "google/gemma-7b-it" no OpenRouter).
 * @param messages O histórico da conversa (instrução do sistema + pergunta do usuário).
 * @param temperature O nível de criatividade (0.0 a 1.0). Para busca de ingressos, usamos um valor baixo (ex: 0.2) para evitar alucinações.
 */

public record LlmRequestDTO(
    String model,
    @JsonProperty("messages")
    List<LlmMessageDTO> messageDTOs,
    Double temperature
) {}
