package com.ticketsecure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.ticketsecure.domain.model.Reserve;
import com.ticketsecure.domain.model.Ticket;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.dto.FraudResultDTO;
import com.ticketsecure.repository.ReserveRepository;
import com.ticketsecure.repository.TicketLotRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class FraudResultListener {

    private static final Logger logger = LoggerFactory.getLogger(FraudResultListener.class);
    
    private final ReserveRepository reserveRepository;
    private final TicketLotRepository ticketLotRepository;
    private final TicketService ticketService;

    public FraudResultListener(ReserveRepository reserveRepository,
                                 TicketLotRepository ticketLotRepository,
                                 TicketService ticketService) {
        this.reserveRepository = reserveRepository;
        this.ticketLotRepository = ticketLotRepository;
        this.ticketService = ticketService;
    }
    
    
    @RabbitListener(queues = com.ticketsecure.config.RabbitMQconfig.FRAUD_RESULT_QUEUE)
    @Transactional
    public void processFraudResult(FraudResultDTO result) {
        try {
            logger.info("[LISTENER] O Cerebro Python respondeu com sucesso! Reserva ID: {}, Veredito: {}",
                    result.reserveId(), result.status());
            
            // Busca a reserva correspondente
            Reserve reserve = reserveRepository.findById(result.reserveId())
                    .orElseThrow(() -> new RuntimeException("Alerta: Reserva " + result.reserveId() + " não encontrada no banco!"));

            if ("APPROVED".equals(result.status())) {
                // 1. Atualiza o status da reserva para CONFIRMED
                reserve.setStatus(com.ticketsecure.domain.enumerate.ReserveStatus.CONFIRMED);
                reserveRepository.save(reserve);
                
                // 2. Gera o Ingresso seguro
                Ticket ticket = ticketService.generateTicketForReserve(reserve);
                
                logger.info("[SUCESSO] Reserva confirmada e ingresso gerado. HASH: {}", ticket.getSecurityHash());
                
            } else {
                reserve.setStatus(com.ticketsecure.domain.enumerate.ReserveStatus.CANCELLED);

                TicketLot lot = reserve.getTicketLot();
                lot.setAvailableQuantity(lot.getAvailableQuantity() + 1);
                ticketLotRepository.save(lot);
                reserveRepository.save(reserve);

                logger.warn("[FRAUDE] Fraude detectada! Reserva CANCELLED e ingresso devolvido ao lote. Reserva ID: {}", reserve.getId());
            }
        } catch (Exception e) {
            logger.error("[ERRO CRITICO NO LISTENER] Falha ao processar o veredito para reserva: {}", result.reserveId(), e);
        }
    }
}
