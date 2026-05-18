package com.ticketsecure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ticketsecure.dto.TicketValidationRequestDTO;
import com.ticketsecure.dto.TicketValidationResponseDTO;
import com.ticketsecure.service.TicketService;

@RestController
@RequestMapping("/api/gate")
public class GateController {
    private final TicketService ticketService;

    public GateController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/validate")
    public ResponseEntity<TicketValidationResponseDTO> validateTicket(@RequestBody TicketValidationRequestDTO request) {
        System.out.println("\n[🚧 CATRACA] Nova tentativa de acesso recebida via hardware!");

        TicketValidationResponseDTO response = ticketService.validadeAndConsumeTicket(request.securityHash());

        System.out.println("[🚧 CATRACA] Resultado para " + response.userName() + ": " + response.status() + " -> " + response.message());
        System.out.println("--------------------------------------------------");
        
        return ResponseEntity.ok(response);
    }

}
