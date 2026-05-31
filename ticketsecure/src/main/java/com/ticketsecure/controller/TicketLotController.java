package com.ticketsecure.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketsecure.domain.model.Event;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.repository.EventRepository;
import com.ticketsecure.repository.TicketLotRepository;

@RestController
@RequestMapping("/api/events")
public class TicketLotController {

    private static final Logger logger = LoggerFactory.getLogger(TicketLotController.class);

    private final TicketLotRepository ticketLotRepository;
    private final EventRepository eventRepository;

    public TicketLotController(TicketLotRepository ticketLotRepository, EventRepository eventRepository) {
        this.ticketLotRepository = ticketLotRepository;
        this.eventRepository = eventRepository;
    }

    @PostMapping("/{eventId}/lots")
    public ResponseEntity<TicketLot> createTicketLot(@PathVariable UUID eventId, @RequestBody TicketLot ticketLot) {
        logger.debug("Criando lote - Nome: {}, Preco: {}, Quantidade: {}",
                ticketLot.getName(), ticketLot.getPrice(), ticketLot.getTotalQuantity());
        // 1. Busca o evento no banco de dados para vincular ao lote
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado"));

        // 2. Configura os dados do lote
        ticketLot.setEvent(event);
        ticketLot.setAvailableQuantity(ticketLot.getTotalQuantity()); // A quantidade disponível começa cheia
        if (ticketLot.getTotalQuantity() == null || ticketLot.getTotalQuantity() <= 0) {
            throw new IllegalArgumentException("A quantidade total é obrigatória e deve ser maior que zero!");
        }
        ticketLot.setCreatedDate(LocalDateTime.now());
 
        // 3. Salva no banco
        TicketLot savedLot = ticketLotRepository.save(ticketLot);
        logger.info("Lote de ingressos criado com sucesso. Evento ID: {}, Lote ID: {}", eventId, savedLot.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLot);
        
    }
}