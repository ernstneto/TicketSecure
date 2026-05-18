package com.ticketsecure.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

import com.ticketsecure.domain.model.Reserve;
import com.ticketsecure.domain.model.Ticket;
import com.ticketsecure.dto.TicketValidationResponseDTO;
import com.ticketsecure.repository.TicketRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;


@Service
public class TicketService {
    private final TicketRepository ticketRepository;

    @Value("${ticketsecure.security.qr-salt:default-salt}")
    private String qrSalt;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket generateTicketForReserve(Reserve reserve) {
       // 1. Gera o Hash de Segurança Único
        String securityHash = generateSecurityHash(reserve);

        // 2. Cria a nova entidade Ticket (o status VALID é setado no construtor)
        Ticket newTicket = new Ticket(reserve, securityHash);

        // 3. Salva no banco de dados
        return ticketRepository.save(newTicket);
    }

    private String generateSecurityHash(Reserve reserve) {
        try {
            String rawData = reserve.getId().toString() + reserve.getUser().getId() + qrSalt;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro Crítico: Algoritmo SHA-256 não encontrado no servidor.", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public TicketValidationResponseDTO validadeAndConsumeTicket(String securityHash) {
        // 1. Procura o ingresso pelo hash gerado na criptografia
        var ticketOptional = ticketRepository.findBySecurityHash(securityHash);
        
        if (ticketOptional.isEmpty()) {
            return new TicketValidationResponseDTO("DENIED", "INGRESSO INVÁLIDO: Assinatura digital não reconhecida.", null, null);
        }

        Ticket ticket = ticketOptional.get();

        // 2.Valida o status atual do ingresso
        switch (ticket.getStatus()) {
            case USED -> {
                return new TicketValidationResponseDTO("DENIED", "ACESSO NEGADO: Este ingresso já foi utilizado!", 
                        ticket.getReserve().getTicketLot().getEvent().getTitle(), 
                        ticket.getReserve().getUser().getName());
            }
            case CANCELLED -> {
                return new TicketValidationResponseDTO("DENIED", "ACESSO NEGADO: Este ingresso foi cancelado pelo sistema.", 
                        ticket.getReserve().getTicketLot().getEvent().getTitle(), 
                        ticket.getReserve().getUser().getName());
            }
            case VALID -> {
                // 3. Altera o status para USED imediatamente (Garante a atomicidade)
                ticket.setStatus(com.ticketsecure.domain.enumerate.TicketStatus.USED);
                ticketRepository.save(ticket);
                
                return new TicketValidationResponseDTO("ALLOWED", "ACESSO LIBERADO! Bom evento.", 
                        ticket.getReserve().getTicketLot().getEvent().getTitle(), 
                        ticket.getReserve().getUser().getName());
            }
            default -> {
                return new TicketValidationResponseDTO("DENIED", "ERRO: Estado de ingresso desconhecido.", null, null);
            }
        }
    }
}
