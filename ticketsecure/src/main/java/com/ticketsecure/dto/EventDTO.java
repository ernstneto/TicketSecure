package com.ticketsecure.dto;

import java.time.LocalDateTime;

import com.ticketsecure.domain.enumerate.EventStatus;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventDTO(
    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date must be in the present or future")
    LocalDateTime eventDate,

    @NotBlank(message = "Location is required")
    String location,

    @NotBlank(message = "Local is required")
    String local,

    @NotNull(message = "Status is required")
    EventStatus status
) {}
