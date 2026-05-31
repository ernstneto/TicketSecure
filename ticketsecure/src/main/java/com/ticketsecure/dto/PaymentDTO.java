package com.ticketsecure.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentDTO(
    @NotNull(message = "O ID da reserva é obrigatório")
    UUID reserveId,

    @NotBlank(message = "O token do cartão é obrigatório")
    String cardToken,

    @NotBlank(message = "O método de pagamento é obrigatório")
    String paymentMethod
) {}
