package com.ticketsecure.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ticketsecure.domain.enumerate.EventStatus;

public record EventResponseDTO(
    String id,
    String title,
    String description,
    LocalDateTime eventDate,
    String location,
    String local,
    EventStatus status
) 
{
    public EventResponseDTO(UUID id, String title, String description, LocalDateTime eventDate, String location, String local, EventStatus status) {
        this(id.toString(), title, description, eventDate, location, local, status);
    }
} 
