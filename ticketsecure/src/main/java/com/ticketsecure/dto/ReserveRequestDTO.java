package com.ticketsecure.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record ReserveRequestDTO(
    @NotNull(message = "O ID do usuário é obrigatório")
    UUID userId,

    @NotNull(message = "O ID do lote de ingressos é obrigatório")
    UUID ticketLotId
) {}