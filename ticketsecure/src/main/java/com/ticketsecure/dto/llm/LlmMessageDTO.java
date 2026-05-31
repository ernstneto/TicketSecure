package com.ticketsecure.dto.llm;

/**
 * Representa uma única mensagem no histórico do chat.
 * @param role Quem está falando: "system" (regras), "user" (cliente) ou "assistant" (a IA).
 * @param content O texto da mensagem.
 */
public record LlmMessageDTO(
    String role,
    String content
) {}
