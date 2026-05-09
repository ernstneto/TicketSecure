package com.ticketsecure.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketLotRequestDTO(
    @NotNull(message="Event ID is required")
    UUID eventId,
    @NotBlank(message="Name is required")
    String name,
    @Min(value=1, message="Quantity must be at least 1")
    int quantity,
    @DecimalMin(value="0.0", inclusive=false, message="Price must be greater than 0")
    BigDecimal price,
    @NotNull(message="Total quantity is required")
    @Min(value=1, message="Total quantity must be at least 1")
    Integer totalQuantity
) {
    
}
