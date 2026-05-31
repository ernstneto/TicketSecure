package com.ticketsecure.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketsecure.config.RabbitMQconfig;
import com.ticketsecure.domain.enumerate.ReserveStatus;
import com.ticketsecure.domain.model.Reserve;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.domain.model.User;
import com.ticketsecure.dto.FraudCheckDTO;
import com.ticketsecure.dto.ReserveRequestDTO;
import com.ticketsecure.dto.ReserveResponseDTO;
import com.ticketsecure.repository.ReserveRepository;
import com.ticketsecure.repository.TicketLotRepository;
import com.ticketsecure.repository.UserRepository;

@Service
public class ReserveService {

    private static final Logger logger = LoggerFactory.getLogger(ReserveService.class);

    private final ReserveRepository reserveRepository;
    private final TicketLotRepository ticketLotRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    public ReserveService(ReserveRepository reserveRepository, 
                          TicketLotRepository ticketLotRepository, 
                          UserRepository userRepository,
                          RabbitTemplate rabbitTemplate) {
        this.reserveRepository = reserveRepository;
        this.ticketLotRepository = ticketLotRepository;
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 1. CRIAÇÃO DA RESERVA (Com bloqueio de Overbooking - Pessimistic Lock)
    */
    @Transactional
    public ReserveResponseDTO createReserve(ReserveRequestDTO request, String sourceIp, String userAgent) {
        logger.info("[RESERVA] Iniciando criacao de reserva com Lock Ativo...");
        
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        TicketLot lot = ticketLotRepository.findByIdForUpdate(request.ticketLotId())
                .orElseThrow(() -> new IllegalArgumentException("Lote de ingressos não encontrado"));

        if (lot.getAvailableQuantity() <= 0) {
            throw new IllegalArgumentException("Ingressos esgotados para este lote!");
        }

        // Bloqueia o ingresso (tira 1 da disponibilidade)
        lot.setAvailableQuantity(lot.getAvailableQuantity() - 1);
        ticketLotRepository.save(lot);

        // Cria a reserva com expiração para daqui a 15 minutos
        Reserve reserve = new Reserve();
        reserve.setUser(user);
        reserve.setTicketLot(lot);
        reserve.setStatus(ReserveStatus.PENDING_PAYMENT);
        reserve.setExpiredDate(LocalDateTime.now().plusMinutes(15));
        reserve.setSourceIP(sourceIp);
        reserve.setUserAgent(userAgent);

        reserve = reserveRepository.save(reserve);

        return new ReserveResponseDTO(
                reserve.getId(), 
                user.getId(), 
                lot.getId(),
                reserve.getStatus(), 
                reserve.getReverseDate(), 
                reserve.getExpiredDate()
        );
    }

    /**
     * 2. PROCESSAMENTO DO PAGAMENTO UNIFICADO (Com Motor Antifraude via RabbitMQ)
     */
    @Transactional
    public ReserveResponseDTO processPaymentToFraudCheck(UUID reserveId, String sourceIp, String userAgent) {
        logger.info("[PAGAMENTO] Iniciando processamento para a Reserva ID: {}", reserveId);

        // 1. Busca a reserva no banco de dados
        Reserve reserve = reserveRepository.findById(reserveId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada: " + reserveId));
        
        // 2. Valida se a reserva está aguardando pagamento
        if (reserve.getStatus() != ReserveStatus.PENDING_PAYMENT) {
            logger.warn("[ERRO] Status invalido para pagamento: {}", reserve.getStatus());
            throw new IllegalStateException("Esta reserva não está pendente de pagamento.");
        }

        // 3. Atualiza os dados de auditoria de rede para o motor antifraude
        reserve.setSourceIP(sourceIp);
        reserve.setUserAgent(userAgent);
        reserveRepository.save(reserve);

        // 4. Monta o dossiê (DTO) esperado pelo Cérebro Python
        FraudCheckDTO dossie = new FraudCheckDTO(
            reserve.getId(),
            reserve.getUser().getId(),
            reserve.getTicketLot().getPrice(),
            java.time.LocalDateTime.now().toString(),
            reserve.getSourceIP()
        );

        // 5. Envia para a Fila do RabbitMQ usando a constante global
        try {
            rabbitTemplate.convertAndSend(RabbitMQconfig.FRAUD_CHECK_QUEUE, dossie);
            logger.info("[SUCESSO] Dossie antifraude enviado para o RabbitMQ!");
        } catch (Exception e) {
            logger.error("[ERRO CRITICO] Falha ao comunicar com o RabbitMQ!", e);
            throw new RuntimeException("Erro ao processar pagamento devido a falha na mensageria.", e);
        }

        // 6. Retorna a confirmação para o Frontend
        return new ReserveResponseDTO(
                reserve.getId(), 
                reserve.getUser().getId(), 
                reserve.getTicketLot().getId(),
                reserve.getStatus(), 
                reserve.getReverseDate(), 
                reserve.getExpiredDate()
        );
    }

    /**
     * 3. ROTINA AUTOMÁTICA (Devolve ingressos não pagos após 15 minutos)
     */
    @Scheduled(fixedRate = 60000) // Roda a cada 1 minuto
    @Transactional
    public void cancelExpiredReserves() {
        LocalDateTime now = LocalDateTime.now();
        List<Reserve> expiredReserves = reserveRepository.findByStatusAndExpiredDateBefore(ReserveStatus.PENDING_PAYMENT, now);

        for (Reserve reserve : expiredReserves) {
            // Muda o status para expirado
            reserve.setStatus(ReserveStatus.EXPIRED);
            
            // Devolve o ingresso para o lote
            TicketLot lot = reserve.getTicketLot();
            lot.setAvailableQuantity(lot.getAvailableQuantity() + 1);
            
            ticketLotRepository.save(lot);
            reserveRepository.save(reserve);
            
            logger.info("Reserva {} expirada. Ingresso devolvido ao lote.", reserve.getId());
        }
    }
}