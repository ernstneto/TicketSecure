package com.ticketsecure.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticketsecure.domain.enumerate.EventStatus;
import com.ticketsecure.domain.model.Event;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.dto.EventSearchCriteria;
import com.ticketsecure.dto.EventSuggestionDTO;
import com.ticketsecure.repository.EventRepository;
import com.ticketsecure.repository.TicketLotRepository;
import com.ticketsecure.util.GeoUtils;

@Service
public class EventSearchService {

    private final EventRepository eventRepository;
    private final TicketLotRepository ticketLotRepository;

    public EventSearchService(EventRepository eventRepository, TicketLotRepository ticketLotRepository) {
        this.eventRepository = eventRepository;
        this.ticketLotRepository = ticketLotRepository;
    }

    public List<EventSuggestionDTO> search(EventSearchCriteria criteria) {
        List<EventSuggestionDTO> results = executeSearch(criteria);
        if (!results.isEmpty()) {
            return results;
        }

        if (criteria.category() != null) {
            results = executeSearch(criteria.withoutCategory());
            if (!results.isEmpty()) {
                return results;
            }
        }

        if (criteria.city() != null) {
            results = executeSearch(criteria.withoutCity());
            if (!results.isEmpty()) {
                return results;
            }
        }

        if (criteria.keyword() != null) {
            results = executeSearch(criteria.withoutKeyword());
            if (!results.isEmpty()) {
                return results;
            }
        }

        if (criteria.maxDistanceKm() != null) {
            results = executeSearch(criteria.withoutDistanceLimit());
            if (!results.isEmpty()) {
                return results;
            }
        }

        return executeSearch(new EventSearchCriteria(
                null, null, null, criteria.maxPrice(),
                criteria.userLatitude(), criteria.userLongitude(),
                null, criteria.preferCheaper(), criteria.limit()));
    }

    private List<EventSuggestionDTO> executeSearch(EventSearchCriteria criteria) {
        List<Event> events = eventRepository.searchActiveEvents(
                EventStatus.ACTIVE,
                LocalDateTime.now(),
                blankToNull(criteria.keyword()),
                blankToNull(criteria.city()),
                criteria.category());

        List<ScoredSuggestion> scored = new ArrayList<>();

        for (Event event : events) {
            Optional<TicketLot> bestLot = findBestAvailableLot(event.getId(), criteria.maxPrice());
            if (bestLot.isEmpty()) {
                continue;
            }

            TicketLot lot = bestLot.get();
            Double distanceKm = calculateDistanceKm(event, criteria.userLatitude(), criteria.userLongitude());

            if (criteria.maxDistanceKm() != null && distanceKm != null && distanceKm > criteria.maxDistanceKm()) {
                continue;
            }

            double score = computeScore(event, lot, distanceKm, criteria.preferCheaper());
            scored.add(new ScoredSuggestion(
                    toSuggestion(event, lot, distanceKm),
                    score));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(ScoredSuggestion::score).reversed())
                .limit(criteria.limit())
                .map(ScoredSuggestion::suggestion)
                .toList();
    }

    public String buildCatalogContext(List<EventSuggestionDTO> suggestions) {
        if (suggestions.isEmpty()) {
            return "[CATÁLOGO] Nenhum evento disponível corresponde à busca no momento.";
        }

        StringBuilder sb = new StringBuilder("[CATÁLOGO VERIFICADO DO SISTEMA — use APENAS estes dados]\n");
        int index = 1;
        for (EventSuggestionDTO s : suggestions) {
            sb.append(index++).append(". **").append(s.title()).append("**\n");
            sb.append("   - Categoria: ").append(s.category()).append("\n");
            sb.append("   - Cidade: ").append(s.city()).append(" | Local: ").append(s.venue()).append("\n");
            sb.append("   - Data: ").append(s.eventDate()).append("\n");
            sb.append("   - Preço a partir de: R$ ").append(s.priceFrom()).append("\n");
            sb.append("   - Ingressos disponíveis: ").append(s.availableTickets()).append("\n");
            if (s.distanceKm() != null) {
                sb.append("   - Distância estimada: ").append(String.format("%.1f km", s.distanceKm())).append("\n");
            }
            sb.append("   - ID evento: ").append(s.eventId()).append(" | ID lote: ").append(s.lotId()).append("\n");
        }
        return sb.toString();
    }

    private Optional<TicketLot> findBestAvailableLot(java.util.UUID eventId, BigDecimal maxPrice) {
        return ticketLotRepository.findByEvent_IdOrderByPriceAsc(eventId).stream()
                .filter(lot -> lot.getAvailableQuantity() != null && lot.getAvailableQuantity() > 0)
                .filter(lot -> maxPrice == null || lot.getPrice().compareTo(maxPrice) <= 0)
                .findFirst();
    }

    private Double calculateDistanceKm(Event event, Double userLat, Double userLng) {
        if (userLat == null || userLng == null || event.getLatitude() == null || event.getLongitude() == null) {
            return null;
        }
        return GeoUtils.haversineKm(userLat, userLng, event.getLatitude(), event.getLongitude());
    }

    private double computeScore(Event event, TicketLot lot, Double distanceKm, boolean preferCheaper) {
        double score = 0;
        if (distanceKm != null) {
            score += 3.0 / (1.0 + distanceKm);
        }
        if (lot.getPrice() != null) {
            double priceScore = 2.0 / (1.0 + lot.getPrice().doubleValue());
            score += priceScore;
            if (preferCheaper) {
                score += priceScore * 1.5;
            }
        }
        score += Math.min(lot.getAvailableQuantity(), 10) / 10.0;

        long daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), event.getEventDate());
        if (daysUntil >= 0 && daysUntil <= 7) {
            score += 0.5;
        }
        return score;
    }

    private EventSuggestionDTO toSuggestion(Event event, TicketLot lot, Double distanceKm) {
        return new EventSuggestionDTO(
                event.getId(),
                lot.getId(),
                event.getTitle(),
                event.getCategory() != null ? event.getCategory() : com.ticketsecure.domain.enumerate.EventCategory.OTHER,
                event.getCity() != null ? event.getCity() : event.getLocation(),
                event.getLocal(),
                event.getEventDate(),
                lot.getPrice(),
                lot.getAvailableQuantity(),
                distanceKm);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record ScoredSuggestion(EventSuggestionDTO suggestion, double score) {}
}
