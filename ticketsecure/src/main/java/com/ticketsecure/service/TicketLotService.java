package com.ticketsecure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.dto.TicketLotRequestDTO;
import com.ticketsecure.dto.TicketLotResponseDTO;
import com.ticketsecure.repository.EventRepository;
import com.ticketsecure.repository.TicketLotRepository;

import com.ticketsecure.domain.model.Event;
import jakarta.transaction.Transactional;

@Service
public class TicketLotService {
    @Autowired
    private TicketLotRepository ticketLotRepository;

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public TicketLotResponseDTO createTicketLot(TicketLotRequestDTO request) {
        Event event = eventRepository.findById(request.eventId())
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        TicketLot newLot = TicketLot.builder()
            .event(event)
            .name(request.name())
            .quantity(request.quantity())
            .price(request.price())
            .totalQuantity(request.totalQuantity())
            .availableQuantity(request.totalQuantity())
            .build();

        TicketLot savedTicketLot = ticketLotRepository.save(newLot);

        return new TicketLotResponseDTO(
            savedTicketLot.getId(),
            savedTicketLot.getEvent().getId(),
            savedTicketLot.getName(),
            savedTicketLot.getQuantity(),
            savedTicketLot.getPrice().toString(),
            savedTicketLot.getTotalQuantity(),
            savedTicketLot.getAvailableQuantity(),
            savedTicketLot.getCreatedDate()
        );
    }
    
}
