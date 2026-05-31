package com.ticketsecure.dto.llm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmResponseDTO(
    List<Choice> choices
) {
    // Removed duplicate record Choice. Using the static inner class below for Jackson.
     
    // Metodo utilitario para extrair direto o texto da resposta da AI
    public String getAssistantReply() {
        if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().content();
        } else {
            return "Desculpe, o agente de IA está indisponível no momento.";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private LlmMessageDTO message;
        
        public LlmMessageDTO getMessage() {
            return message;
        }

        public void setMessage(LlmMessageDTO message) {
            this.message = message;
        }
    }
}
