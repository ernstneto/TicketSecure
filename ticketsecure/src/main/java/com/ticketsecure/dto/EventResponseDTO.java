package com.ticketsecure.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ticketsecure.domain.enumerate.EventStatus;
import com.ticketsecure.domain.enumerate.EventCategory;

public record EventResponseDTO(
    String id,
    String title,
    String description,
    LocalDateTime eventDate,
    String location,
    String local,
    EventStatus status,
    EventCategory category,
    String city,
    Double latitude,
    Double longitude
) 
{
    public EventResponseDTO(UUID id, String title, String description, LocalDateTime eventDate,
                            String location, String local, EventStatus status) {
        this(id.toString(), title, description, eventDate, location, local, status,
                EventCategory.OTHER, location, null, null);
    }

    public EventResponseDTO(UUID id, String title, String description, LocalDateTime eventDate,
                            String location, String local, EventStatus status,
                            EventCategory category, String city, Double latitude, Double longitude) {
        this(id.toString(), title, description, eventDate, location, local, status,
                category, city, latitude, longitude);
    }
} 
