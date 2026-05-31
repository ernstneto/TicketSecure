package com.ticketsecure.service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ticketsecure.domain.enumerate.EventCategory;
import com.ticketsecure.dto.EventSearchCriteria;

import org.springframework.stereotype.Component;

@Component
public class EventSearchIntentParser {

    private static final Pattern MAX_PRICE_PATTERN = Pattern.compile(
            "(?:at[eé]|m[aá]ximo|abaixo de|menos de|por|at[eé]\\s+)\\s*R?\\$?\\s*(\\d+(?:[.,]\\d{1,2})?)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern CITY_PATTERN = Pattern.compile(
            "\\b(?:em|na|no)\\s+([A-Za-zÀ-ú\\s]{3,30})",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public EventSearchCriteria parse(String userMessage, Double userLatitude, Double userLongitude) {
        if (userMessage == null || userMessage.isBlank()) {
            return EventSearchCriteria.defaults();
        }

        String normalized = userMessage.toLowerCase(Locale.ROOT);
        EventCategory category = detectCategory(normalized);
        BigDecimal maxPrice = detectMaxPrice(userMessage);
        String city = detectCity(userMessage);
        boolean preferCheaper = normalized.contains("barato")
                || normalized.contains("econômico")
                || normalized.contains("economico")
                || normalized.contains("menor preço")
                || normalized.contains("menor preco");

        boolean preferNear = normalized.contains("perto")
                || normalized.contains("próximo")
                || normalized.contains("proximo")
                || normalized.contains("perto de mim")
                || normalized.contains("na minha região")
                || normalized.contains("na minha regiao");

        Double maxDistanceKm = preferNear ? 15.0 : null;
        String keyword = extractKeyword(userMessage, city, category);

        return new EventSearchCriteria(
                keyword,
                city,
                category,
                maxPrice,
                userLatitude,
                userLongitude,
                maxDistanceKm,
                preferCheaper,
                5
        );
    }

    private EventCategory detectCategory(String normalized) {
        if (containsAny(normalized, "cinema", "filme", "filmes", "sessão", "sessao")) {
            return EventCategory.CINEMA;
        }
        if (containsAny(normalized, "teatro", "peça", "peca", "musical")) {
            return EventCategory.THEATER;
        }
        if (containsAny(normalized, "festival", "festivais")) {
            return EventCategory.FESTIVAL;
        }
        if (containsAny(normalized, "show", "rock", "pop", "música", "musica", "concerto", "banda")) {
            return EventCategory.SHOW;
        }
        return null;
    }

    private BigDecimal detectMaxPrice(String message) {
        Matcher matcher = MAX_PRICE_PATTERN.matcher(message);
        if (matcher.find()) {
            String raw = matcher.group(1).replace(',', '.');
            return new BigDecimal(raw);
        }
        return null;
    }

    private String detectCity(String message) {
        Matcher matcher = CITY_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String extractKeyword(String message, String city, EventCategory category) {
        String keyword = message.trim();
        if (city != null) {
            keyword = keyword.replaceAll("(?i)\\b(?:em|na|no)\\s+" + Pattern.quote(city), "").trim();
        }
        if (keyword.length() < 3) {
            return null;
        }
        return keyword;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
