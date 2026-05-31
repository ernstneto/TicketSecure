package com.ticketsecure.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketsecure.domain.enumerate.EventCategory;
import com.ticketsecure.dto.EventResponseDTO;
import com.ticketsecure.dto.EventSearchCriteria;
import com.ticketsecure.dto.EventSuggestionDTO;
import com.ticketsecure.service.EventSearchService;
import com.ticketsecure.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final EventSearchService eventSearchService;

    public EventController(EventService eventService, EventSearchService eventSearchService) {
        this.eventService = eventService;
        this.eventSearchService = eventSearchService;
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventResponseDTO eventRequestDTO) {
        EventResponseDTO createdEvent = eventService.createEvent(eventRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventSuggestionDTO>> searchEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double maxDistanceKm,
            @RequestParam(defaultValue = "false") boolean preferCheaper,
            @RequestParam(defaultValue = "5") int limit) {

        EventSearchCriteria criteria = new EventSearchCriteria(
                q, city, category, maxPrice, lat, lng, maxDistanceKm, preferCheaper, limit);

        return ResponseEntity.ok(eventSearchService.search(criteria));
    }
}
