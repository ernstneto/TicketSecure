package com.ticketsecure.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticketsecure.domain.enumerate.EventCategory;
import com.ticketsecure.dto.ChatResponseDTO;
import com.ticketsecure.dto.EventSearchCriteria;
import com.ticketsecure.dto.EventSuggestionDTO;
import com.ticketsecure.dto.ResolvedLocation;

@Service
public class AiAgentService {

    private final EventSearchService eventSearchService;
    private final EventSearchIntentParser eventSearchIntentParser;
    private final GeocodingService geocodingService;

    public AiAgentService(
            EventSearchService eventSearchService,
            EventSearchIntentParser eventSearchIntentParser,
            GeocodingService geocodingService) {
        this.eventSearchService = eventSearchService;
        this.eventSearchIntentParser = eventSearchIntentParser;
        this.geocodingService = geocodingService;
    }

    public ChatResponseDTO processChat(String sessionId, String userMessage, Double latitude, Double longitude) {
        EventSearchCriteria criteria = eventSearchIntentParser.parse(userMessage, latitude, longitude);
        String locationLabel = resolveLocationLabel(criteria, latitude, longitude);

        if (criteria.city() == null && latitude != null && longitude != null) {
            Optional<ResolvedLocation> resolved = geocodingService.reverseGeocode(latitude, longitude);
            if (resolved.isPresent() && resolved.get().city() != null) {
                criteria = criteria.withCity(resolved.get().city());
            }
        }

        List<EventSuggestionDTO> suggestions = eventSearchService.search(criteria);
        String reply = buildDeterministicReply(suggestions, criteria, locationLabel);
        return new ChatResponseDTO(reply, suggestions, locationLabel);
    }

    private String resolveLocationLabel(EventSearchCriteria criteria, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return criteria.city();
        }
        return geocodingService.reverseGeocode(latitude, longitude)
                .map(ResolvedLocation::displayName)
                .orElse(String.format("%.4f, %.4f", latitude, longitude));
    }

    private String buildDeterministicReply(
            List<EventSuggestionDTO> suggestions,
            EventSearchCriteria criteria,
            String locationLabel) {

        if (suggestions.isEmpty()) {
            return buildEmptyReply(criteria, locationLabel);
        }

        StringBuilder sb = new StringBuilder();
        if (locationLabel != null && !locationLabel.isBlank()) {
            sb.append("📍 Região detectada: **").append(locationLabel).append("**\n\n");
        }

        sb.append("Encontrei **").append(suggestions.size()).append("** opção(ões) no catálogo TicketSecure:\n\n");

        int rank = 1;
        for (EventSuggestionDTO s : suggestions) {
            sb.append(rank++).append(". **").append(s.title()).append("**");
            sb.append(" — ").append(formatCategory(s.category()));
            sb.append(" · a partir de **R$ ").append(s.priceFrom()).append("**");
            if (s.distanceKm() != null) {
                sb.append(" · ~").append(String.format("%.1f km", s.distanceKm()));
            }
            sb.append("\n   ").append(s.venue()).append(" · ").append(s.availableTickets()).append(" ingressos\n");
        }

        sb.append("\nUse os **cards abaixo** para escolher. Para reservar via API/Postman, anote o `lotId` do card.");
        return sb.toString();
    }

    private String buildEmptyReply(EventSearchCriteria criteria, String locationLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append("Não encontrei eventos no catálogo com esses filtros");

        if (criteria.category() != null) {
            sb.append(" (tipo: **").append(formatCategory(criteria.category())).append("**)");
        }
        if (criteria.city() != null) {
            sb.append(" em **").append(criteria.city()).append("**");
        } else if (locationLabel != null) {
            sb.append(" perto de **").append(locationLabel).append("**");
        }
        sb.append(".\n\n");

        sb.append("**Dicas:**\n");
        sb.append("- Tente uma busca mais ampla, ex.: *show em São Paulo* ou *eventos até R$ 150*\n");
        sb.append("- Ative **Perto de mim** ou digite a cidade no chat\n");
        sb.append("- O catálogo só lista eventos **cadastrados no TicketSecure** — não inventamos cinemas ou preços externos\n");

        if (criteria.category() == EventCategory.CINEMA) {
            sb.append("\n💡 Seu banco pode ter eventos antigos sem categoria Cinema. ");
            sb.append("Reinicie o backend com banco vazio para carregar o **catálogo demo**, ou cadastre cinemas via API.");
        }

        return sb.toString();
    }

    private String formatCategory(EventCategory category) {
        if (category == null) {
            return "Evento";
        }
        return switch (category) {
            case SHOW -> "Show";
            case CINEMA -> "Cinema";
            case THEATER -> "Teatro";
            case FESTIVAL -> "Festival";
            default -> "Evento";
        };
    }

    public void clearSessionMemory(String sessionId) {
        // reservado para futura memória conversacional
    }
}
