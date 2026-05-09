package com.ticketsecure.service;

import java.time.LocalDateTime;
import java.util.UUID;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ticketsecure.domain.enumerate.ReserveStatus;
import com.ticketsecure.domain.model.Reserve;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.domain.model.User;
import com.ticketsecure.dto.ReserveRequestDTO;
import com.ticketsecure.dto.ReserveResponseDTO;
import com.ticketsecure.repository.ReserveRepository;
import com.ticketsecure.repository.TicketLotRepository;
import com.ticketsecure.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ReserveService {
    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketLotRepository ticketLotRepository;

    @Transactional
    public ReserveResponseDTO createReserve(ReserveRequestDTO request, String sourceIP, String userAgent) {
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TicketLot ticketLot = ticketLotRepository.findById(request.ticketLotId())
            .orElseThrow(() -> new IllegalArgumentException("Lote de ingressos não encontrado"));
        
        if (ticketLot.getAvailableQuantity() <= 0) {
            throw new IllegalArgumentException("Lote de ingressos esgotado");
        }
        
        ticketLot.setAvailableQuantity(ticketLot.getAvailableQuantity() - 1);
        ticketLotRepository.save(ticketLot);

        Reserve reserve = new Reserve();
        reserve.setUser(user);
        reserve.setTicketLot(ticketLot);
        reserve.setStatus(ReserveStatus.PENDING_PAYMENT);
        reserve.setReverseDate(LocalDateTime.now());
        reserve.setExpiredDate(LocalDateTime.now().plusMinutes(15));
        reserve.setSourceIP(sourceIP);
        reserve.setUserAgent(userAgent);

        Reserve savedReserve = reserveRepository.save(reserve);


        return new ReserveResponseDTO(
            savedReserve.getId(),
            savedReserve.getUser().getId(),
            savedReserve.getTicketLot().getId(),
            savedReserve.getStatus(),
            savedReserve.getReverseDate(),
            savedReserve.getExpiredDate()
        );

    }

    @Transactional
    public ReserveResponseDTO confirmPayment(UUID reserveId) {
        Reserve reserve = reserveRepository.findById(reserveId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada"));
        if (reserve.getStatus() != ReserveStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("O pagamento não pode ser processado. Status atual: " + reserve.getStatus());
        }
        reserve.setStatus(ReserveStatus.CONFIRMED);
        Reserve savedReserve = reserveRepository.save(reserve);

        return new ReserveResponseDTO(
            savedReserve.getId(),
            savedReserve.getUser().getId(),
            savedReserve.getTicketLot().getId(),
            savedReserve.getStatus(),
            savedReserve.getReverseDate(),
            savedReserve.getExpiredDate()
        );
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void processExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<Reserve> expiredReserves = reserveRepository.findByStatusAndExpiredDateBefore(ReserveStatus.PENDING_PAYMENT, now);

        if (!expiredReserves.isEmpty()) {
            System.out.println("Encontradas "+ expiredReserves.size() + " reservas expiradas. A processar...");
            
            for (Reserve reserve : expiredReserves) {
                reserve.setStatus(ReserveStatus.EXPIRED);
                reserveRepository.save(reserve);

                TicketLot ticketLot = reserve.getTicketLot();
                ticketLot.setAvailableQuantity(ticketLot.getAvailableQuantity() + 1);
                ticketLotRepository.save(ticketLot);

                System.out.println("Reserva " + reserve.getId() + " expirada. Ingresso devolvido ao lote.");
                
            }
        }
    }
}

