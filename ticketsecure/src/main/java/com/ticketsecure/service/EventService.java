package com.ticketsecure.service;

import org.springframework.stereotype.Service;

import com.ticketsecure.domain.enumerate.EventStatus;
import com.ticketsecure.domain.enumerate.EventCategory;
import com.ticketsecure.domain.model.Event;
import com.ticketsecure.dto.EventResponseDTO;
import com.ticketsecure.repository.EventRepository;

import jakarta.transaction.Transactional;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventResponseDTO createEvent(EventResponseDTO event) {
        EventCategory category = event.category() != null ? event.category() : EventCategory.OTHER;
        String city = event.city() != null && !event.city().isBlank() ? event.city() : event.location();

        Event newEvent = Event.builder()
            .title(event.title())
            .description(event.description())
            .eventDate(event.eventDate())
            .location(event.location())
            .local(event.local())
            .category(category)
            .city(city)
            .latitude(event.latitude())
            .longitude(event.longitude())
            .status(EventStatus.ACTIVE)
            .build();

        Event savedEvent = eventRepository.save(newEvent);
        return toDto(savedEvent);
    }

    public EventResponseDTO toDto(Event savedEvent) {
        return new EventResponseDTO(
            savedEvent.getId(),
            savedEvent.getTitle(),
            savedEvent.getDescription(),
            savedEvent.getEventDate(),
            savedEvent.getLocation(),
            savedEvent.getLocal(),
            savedEvent.getStatus(),
            savedEvent.getCategory() != null ? savedEvent.getCategory() : EventCategory.OTHER,
            savedEvent.getCity() != null ? savedEvent.getCity() : savedEvent.getLocation(),
            savedEvent.getLatitude(),
            savedEvent.getLongitude()
        );
    }
}
