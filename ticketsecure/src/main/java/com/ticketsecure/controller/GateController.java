package com.ticketsecure.controller;

import com.ticketsecure.dto.TicketValidationRequestDTO;
import com.ticketsecure.dto.TicketValidationResponseDTO;
import com.ticketsecure.service.TicketService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gate")
public class GateController {

    private static final Logger logger = LoggerFactory.getLogger(GateController.class);
    private final TicketService ticketService;

    public GateController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/validate")
    public ResponseEntity<TicketValidationResponseDTO> validateTicket(@Valid @RequestBody TicketValidationRequestDTO request) {
        logger.info("[CATRACA] Tentativa de validacao de ingresso. Hash: {}", request.securityHash());

        TicketValidationResponseDTO response = ticketService.validadeAndConsumeTicket(request.securityHash());

        logger.info("[CATRACA] Resultado para {}: {} -> {}", response.userName(), response.status(), response.message());
        
        return ResponseEntity.ok(response);
    }

}
