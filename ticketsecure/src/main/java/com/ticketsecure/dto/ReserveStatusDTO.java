package com.ticketsecure.dto;

import java.util.UUID;

public record ReserveStatusDTO(
    UUID reserveId,
    String status,              // PENDING_PAYMENT, CONFIRMED, CANCELLED
    String securityHash         // Virá preenchido apenas se o status for CONFIRMED
) {}
