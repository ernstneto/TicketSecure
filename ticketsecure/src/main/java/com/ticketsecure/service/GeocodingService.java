package com.ticketsecure.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketsecure.dto.ResolvedLocation;

@Service
public class GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/reverse?lat=%s&lon=%s&format=json&addressdetails=1&accept-language=pt-BR";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<ResolvedLocation> reverseGeocode(double latitude, double longitude) {
        try {
            String url = String.format(NOMINATIM_URL, latitude, longitude);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TicketSecure/1.0 (contact@ticketsecure.local)")
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode address = root.path("address");

            String city = firstNonBlank(
                    text(address, "city"),
                    text(address, "town"),
                    text(address, "municipality"),
                    text(address, "county"),
                    text(address, "state_district"));

            String neighbourhood = firstNonBlank(
                    text(address, "suburb"),
                    text(address, "neighbourhood"),
                    text(address, "quarter"),
                    text(address, "district"));

            String displayName = root.path("display_name").asText(null);
            if (displayName == null && city == null) {
                return Optional.empty();
            }

            if (displayName == null) {
                displayName = neighbourhood != null ? neighbourhood + ", " + city : city;
            }

            return Optional.of(new ResolvedLocation(city, neighbourhood, displayName));
        } catch (Exception e) {
            logger.error("[GEO] Falha na geocodificacao reversa: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
