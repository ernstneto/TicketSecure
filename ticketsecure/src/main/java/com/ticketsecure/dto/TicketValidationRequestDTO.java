package com.ticketsecure.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketValidationRequestDTO(
    @NotBlank(message = "O hash de segurança é obrigatório")
    String securityHash
) {}
