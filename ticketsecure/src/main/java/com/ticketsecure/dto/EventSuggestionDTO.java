package com.ticketsecure.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ticketsecure.domain.enumerate.EventCategory;

public record EventSuggestionDTO(
        UUID eventId,
        UUID lotId,
        String title,
        EventCategory category,
        String city,
        String venue,
        LocalDateTime eventDate,
        BigDecimal priceFrom,
        int availableTickets,
        Double distanceKm
) {}
