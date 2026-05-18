package com.ticketsecure.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
import com.ticketsecure.dto.PaymentDTO;
import com.ticketsecure.dto.ReserveRequestDTO;
import com.ticketsecure.dto.ReserveResponseDTO;
import com.ticketsecure.repository.ReserveRepository;
import com.ticketsecure.repository.TicketLotRepository;
import com.ticketsecure.repository.UserRepository;

@Service
public class ReserveService {

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
     * 1. CRIAÇÃO DA RESERVA (Com bloqueio de Overbooking)
     */
    @Transactional
    public ReserveResponseDTO createReserve(ReserveRequestDTO request, String userAgent, String sourceIp) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        TicketLot lot = ticketLotRepository.findById(request.ticketLotId())
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
        reserve.setUserAgent(userAgent);
        reserve.setSourceIP(sourceIp);

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
     * 2. PROCESSAMENTO DO PAGAMENTO (Com Motor Antifraude via RabbitMQ)
     */
    @Transactional
    public ReserveResponseDTO confirmPayment(UUID reserveId) {
        System.out.println("\n[DEBUG - PAGAMENTO] >>> Iniciando processamento para a Reserva ID: " + reserveId);

        // RADAR 1: Tentar encontrar a reserva no banco
        Reserve reserve;
        try {
            reserve = reserveRepository.findById(reserveId)
                    .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada"));
            System.out.println("[DEBUG - PAGAMENTO] Radar 1: Reserva encontrada! Status atual: " + reserve.getStatus());
        } catch (Exception e) {
            System.out.println("[DEBUG - PAGAMENTO - FALHA] Radar 1: Reserva não encontrada na base de dados.");
            throw e;
        }

        // RADAR 2: Validar o status da reserva
        if (reserve.getStatus() != ReserveStatus.PENDING_PAYMENT) {
            System.out.println("[DEBUG - PAGAMENTO - FALHA] Radar 2: Status inválido para pagamento (" + reserve.getStatus() + ").");
            throw new IllegalArgumentException("O pagamento não pode ser processado. Status atual: " + reserve.getStatus());
        }
        System.out.println("[DEBUG - PAGAMENTO] Radar 2: Status válido (PENDING_PAYMENT). Montando dossiê antifraude...");

        // RADAR 3: Montar o DTO e enviar para o RabbitMQ
        try {
            FraudCheckDTO dossie = new FraudCheckDTO(
                reserve.getId(),
                reserve.getUser().getId(),
                reserve.getTicketLot().getPrice(),
                java.time.LocalDateTime.now().toString(), // Converte a data para String
                "N/A" // Como aqui não temos o request HTTP, mandamos "N/A" (Não Aplicável)
            );
            System.out.println("[DEBUG - PAGAMENTO] Dossiê montado com sucesso. Valor total: " + dossie.totalAmount());

            System.out.println("[DEBUG - PAGAMENTO] Tentando comunicar com o RabbitMQ...");
            rabbitTemplate.convertAndSend(RabbitMQconfig.FRAUD_CHECK_QUEUE, dossie);
            System.out.println("[DEBUG - PAGAMENTO] Radar 3: Mensagem ENVIADA com sucesso para a fila do RabbitMQ!\n");

        } catch (Exception e) {
            System.out.println("[DEBUG - PAGAMENTO - FALHA] Radar 3: Erro de conversão ou falha de ligação com o RabbitMQ!");
            e.printStackTrace(); // Isto vai imprimir o erro exato de ligação
            throw e;
        }

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
            
            System.out.println("Reserva " + reserve.getId() + " expirada. Ingresso devolvido ao lote.");
        }
    }

    @Transactional
    public void processPayment(PaymentDTO paymentDTO, String sourceIp, String userAgent) {
        System.out.println("[DEBUG - PAGAMENTO] >>> Iniciando processamento para a Reserva ID: " + paymentDTO.reserveId());

        // 1. BUsca a reserva no banco de dados
        Reserve reserve = reserveRepository.findById(paymentDTO.reserveId())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada" + paymentDTO.reserveId()));

        // 2. Valida se a reserva esta aguardando pagamento
        if (!com.ticketsecure.domain.enumerate.ReserveStatus.PENDING_PAYMENT.equals(reserve.getStatus())) {
            throw new RuntimeException("Esta reserva não está pendente de pagamento.");
        }

        // 3. Atualiza os dados de auditoria de rede diretamente no banco
        reserve.setSourceIP(sourceIp);
        reserve.setUserAgent(userAgent);
        reserveRepository.save(reserve);

        // 4. Monta o dossie (JSON) que será enviado para o cerebro Python
        // Adaptamos para usar o DTO que a IA está esperando na Fila 1
       FraudCheckDTO dossie = new FraudCheckDTO(
            reserve.getId(),
            reserve.getUser().getId(),
            reserve.getTicketLot().getPrice(), // Pega o valor do ingresso no banco
            java.time.LocalDateTime.now().toString(),
            reserve.getSourceIP()
        );

        // 5. Envia para a fila do RabbitMQ (ticketsecure.fraud.check.queue) 
        try {
            // Se você estiver usando o ObjectMapper (Jackson) para converter para JSON:
            // String jsonMessage = objectMapper.writeValueAsString(dossie);
            // rabbitTemplate.convertAndSend(RabbitMQconfig.EXCHANGE_NAME, RabbitMQconfig.ROUTING_KEY_FRAUD_CHECK, jsonMessage);
            
            // Ou se estiver enviando o DTO direto (dependendo da sua configuração do RabbitTemplate):
            rabbitTemplate.convertAndSend("", "ticketsecure.fraud.check.queue", dossie);

            System.out.println("[DEBUG - PAGAMENTO] Radar 3: Mensagem ENVIADA com sucesso para a fila do RabbitMQ!");
        } catch (Exception e) {
            System.err.println("[💥 ERRO] Falha ao comunicar com o RabbitMQ.");
            e.printStackTrace();
        }
    }
}