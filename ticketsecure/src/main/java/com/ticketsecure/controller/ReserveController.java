package com.ticketsecure.controller;

import com.ticketsecure.dto.PaymentDTO;
import com.ticketsecure.dto.ReserveRequestDTO;
import com.ticketsecure.dto.ReserveResponseDTO;
import com.ticketsecure.dto.ReserveStatusDTO;
import com.ticketsecure.domain.model.Reserve;
import com.ticketsecure.domain.enumerate.ReserveStatus;
import com.ticketsecure.security.NetworkAuditService;
import com.ticketsecure.service.ReserveService;
import com.ticketsecure.repository.ReserveRepository;
import com.ticketsecure.repository.TicketRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reserves")
public class ReserveController {

    private static final Logger logger = LoggerFactory.getLogger(ReserveController.class);

    private final ReserveService reserveService;
    private final NetworkAuditService networkAuditService;
    private final ReserveRepository reserveRepository;
    private final TicketRepository ticketRepository;

    public ReserveController(
            ReserveService reserveService,
            NetworkAuditService networkAuditService,
            ReserveRepository reserveRepository,
            TicketRepository ticketRepository) {
        this.reserveService = reserveService;
        this.networkAuditService = networkAuditService;
        this.reserveRepository = reserveRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Endpoint para CRIAR a reserva inicial
     */
    @PostMapping
    public ResponseEntity<ReserveResponseDTO> createReserve(
            @Valid @RequestBody ReserveRequestDTO request,
            HttpServletRequest httpRequest) {

        // Extrai dados da rede para rastreabilidade
        String sourceIp = networkAuditService.extractClientIp(httpRequest);
        String userAgent = networkAuditService.extractUserAgent(httpRequest);

        ReserveResponseDTO response = reserveService.createReserve(request, sourceIp, userAgent);

        // Retorna 201 Created
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Endpoint assíncrono para iniciar o fluxo de pagamento e análise antifraude.
     */
    @PostMapping("/pay")
    public ResponseEntity<String> processPayment(
            @Valid @RequestBody PaymentDTO paymentDTO,
            HttpServletRequest request) {

        String sourceIp = networkAuditService.extractClientIp(request);
        String userAgent = networkAuditService.extractUserAgent(request);

        logger.info("[PAGAMENTO] Reserva ID: {}, IP: {}, UserAgent: {}", paymentDTO.reserveId(), sourceIp, userAgent);

        // Chamada corrigida apontando para o método unificado do Service
        reserveService.processPaymentToFraudCheck(paymentDTO.reserveId(), sourceIp, userAgent);

        return ResponseEntity.ok("Pagamento recebido. Análise de risco e emissão assíncrona iniciadas.");
    }

    /**
     * Endpoint de Polling para o Front-end consultar o veredito final da transação.
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<ReserveStatusDTO> getReserveStatus(@PathVariable UUID id) {
        Reserve reserve = reserveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva com o ID " + id + " não encontrada."));

        String hash = null;
        if (ReserveStatus.CONFIRMED.equals(reserve.getStatus())) {
            hash = ticketRepository.findByReserveId(id)
                    .map(com.ticketsecure.domain.model.Ticket::getSecurityHash)
                    .orElse(null);
        }

        var response = new ReserveStatusDTO(reserve.getId(), reserve.getStatus().name(), hash);
        return ResponseEntity.ok(response);
    }
}