package com.ticketsecure.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketLotResponseDTO(
    UUID id,
    UUID eventId,
    String name,
    //int quantity,
    String price,
    Integer totalQuantity,
    Integer availableQuantity,
    LocalDateTime createdDate
) {

}
