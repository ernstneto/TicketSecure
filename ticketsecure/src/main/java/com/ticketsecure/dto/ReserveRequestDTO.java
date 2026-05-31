package com.ticketsecure.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record ReserveRequestDTO(
    @NotNull(message = "O ID do usuário é obrigatório")
    @JsonProperty("userId")
    UUID userId,

    @NotNull(message = "O ID do lote de ingressos é obrigatório")
    @JsonProperty("ticketLotId")
    UUID ticketLotId
) {}