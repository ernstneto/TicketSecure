package com.ticketsecure.dto;

public record TicketValidationResponseDTO(
    String status,          //ALLOED, DENIED
    String message,         //"Acesso Liberado", "Ingresso já utilizado"
    String eventTitle,      // Seguranca de confirmar se é o portão correto
    String userName         // checagem de documento se necessário
) {}
