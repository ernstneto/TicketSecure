package com.ticketsecure.controller;

import com.ticketsecure.dto.PaymentDTO;
import com.ticketsecure.dto.ReserveStatusDTO;
import com.ticketsecure.domain.model.Reserve;
import com.ticketsecure.domain.enumerate.ReserveStatus;
import com.ticketsecure.security.NetworkAuditService;
import com.ticketsecure.service.ReserveService;
import com.ticketsecure.repository.ReserveRepository;
import com.ticketsecure.repository.TicketRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reserves")
public class ReserveController {

    private final ReserveService reserveService;
    private final NetworkAuditService networkAuditService;
    private final ReserveRepository reserveRepository;
    private final TicketRepository ticketRepository;

    // Injeção de dependências via construtor (Melhor prática para testabilidade)
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
     * Endpoint assíncrono para iniciar o fluxo de pagamento e análise antifraude.
     */
    @PostMapping("/pay")
    public ResponseEntity<String> processPayment(
            @RequestBody PaymentDTO paymentDTO,
            HttpServletRequest request) {

        // 1. Extração isolada dos dados de rede (Camada L7) usando o serviço especialista
        String sourceIp = networkAuditService.extractClientIp(request);
        String userAgent = networkAuditService.extractUserAgent(request);

        System.out.println("\n>>> Entrou no Controller! O ID recebido foi: " + paymentDTO.reserveId());
        System.out.println("[🔒 AUDITORIA] Compra iniciada pelo IP: " + sourceIp + " via " + userAgent);

        // 2. Delegação segura para a camada de serviço processar e disparar para o RabbitMQ
        reserveService.processPayment(paymentDTO, sourceIp, userAgent);

        // Retorno imediato (200 OK) para evitar o travamento da thread HTTP do cliente
        return ResponseEntity.ok("Pagamento recebido. Análise de risco e emissão assíncrona iniciadas.");
    }

    /**
     * Endpoint de Polling para o Front-end ou Postman consultar o veredito final da transação.
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<ReserveStatusDTO> getReserveStatus(@PathVariable UUID id) {
        // Busca a reserva para identificar o estado atual no banco
        Reserve reserve = reserveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva com o ID " + id + " não foi encontrada no servidor."));

        String hash = null;
        
        // Se o motor assíncrono já tiver confirmado a reserva, captura o hash gerado pelo TicketService
        if (ReserveStatus.CONFIRMED.equals(reserve.getStatus())) {
            hash = ticketRepository.findByReserveId(id)
                    .map(com.ticketsecure.domain.model.Ticket::getSecurityHash)
                    .orElse(null);
        }

        // Monta a resposta limpa para o cliente externo
        var response = new ReserveStatusDTO(
                reserve.getId(),
                reserve.getStatus().name(),
                hash
        );

        return ResponseEntity.ok(response);
    }
}