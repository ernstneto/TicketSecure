package com.ticketsecure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ticketsecure.domain.enumerate.EventStatus;
import com.ticketsecure.domain.model.Event;
import com.ticketsecure.dto.EventResponseDTO;
import com.ticketsecure.repository.EventRepository;

import jakarta.transaction.Transactional;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public EventResponseDTO createEvent(EventResponseDTO event) {
        Event newEvent = Event.builder()
            .title(event.title())
            .description(event.description())
            .eventDate(event.eventDate())
            .location(event.location())
            .local(event.local())
            .status(EventStatus.ACTIVE)
            .build();

        Event savedEvent = eventRepository.save(newEvent);
        return new EventResponseDTO(
            savedEvent.getId(),
            savedEvent.getTitle(),
            savedEvent.getDescription(),
            savedEvent.getEventDate(),
            savedEvent.getLocation(),
            savedEvent.getLocal(),
            savedEvent.getStatus()
        );
    }
}
