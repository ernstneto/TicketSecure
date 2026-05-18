package com.ticketsecure.dto;

import java.util.UUID;

public record PaymentDTO(
    UUID reserveId,
    String cardToken,           // Simulacao de token gerado pelo gateway de pagamento
    String paymentMethod        // EX: CREDIT_CARD, PIX, DEBIT
) {}
