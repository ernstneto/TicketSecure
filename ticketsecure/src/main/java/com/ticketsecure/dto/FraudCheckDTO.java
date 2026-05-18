package com.ticketsecure.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FraudCheckDTO(
    UUID reserveId,
    UUID userId,
    BigDecimal totalAmount,
    String attemptTime,
    String sourceIp
) 
{}
