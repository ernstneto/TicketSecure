package com.ticketsecure.dto;

import java.math.BigDecimal;

import com.ticketsecure.domain.enumerate.EventCategory;

public record EventSearchCriteria(
        String keyword,
        String city,
        EventCategory category,
        BigDecimal maxPrice,
        Double userLatitude,
        Double userLongitude,
        Double maxDistanceKm,
        boolean preferCheaper,
        int limit
) {
    public EventSearchCriteria {
        if (limit <= 0) {
            limit = 5;
        }
    }

    public static EventSearchCriteria defaults() {
        return new EventSearchCriteria(null, null, null, null, null, null, null, false, 5);
    }

    public EventSearchCriteria withCity(String newCity) {
        return new EventSearchCriteria(keyword, newCity, category, maxPrice, userLatitude, userLongitude, maxDistanceKm, preferCheaper, limit);
    }

    public EventSearchCriteria withoutCategory() {
        return new EventSearchCriteria(keyword, city, null, maxPrice, userLatitude, userLongitude, maxDistanceKm, preferCheaper, limit);
    }

    public EventSearchCriteria withoutCity() {
        return new EventSearchCriteria(keyword, null, category, maxPrice, userLatitude, userLongitude, maxDistanceKm, preferCheaper, limit);
    }

    public EventSearchCriteria withoutKeyword() {
        return new EventSearchCriteria(null, city, category, maxPrice, userLatitude, userLongitude, maxDistanceKm, preferCheaper, limit);
    }

    public EventSearchCriteria withoutDistanceLimit() {
        return new EventSearchCriteria(keyword, city, category, maxPrice, userLatitude, userLongitude, null, preferCheaper, limit);
    }
}
