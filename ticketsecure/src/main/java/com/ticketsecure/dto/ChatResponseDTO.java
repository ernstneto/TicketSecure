package com.ticketsecure.dto;

import java.util.List;

public record ChatResponseDTO(
        String reply,
        List<EventSuggestionDTO> suggestions,
        String locationLabel
) {
    public ChatResponseDTO(String reply) {
        this(reply, List.of(), null);
    }

    public ChatResponseDTO(String reply, List<EventSuggestionDTO> suggestions) {
        this(reply, suggestions, null);
    }
}
