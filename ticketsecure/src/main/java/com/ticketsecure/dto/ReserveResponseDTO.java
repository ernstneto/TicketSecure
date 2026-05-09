package com.ticketsecure.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ticketsecure.domain.enumerate.ReserveStatus;

public record ReserveResponseDTO(
    UUID id,
    UUID userId,
    UUID ticketLotId,
    ReserveStatus status,
    LocalDateTime reserveDate,
    LocalDateTime expiredDate
) 

{}
