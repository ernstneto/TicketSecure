package com.ticketsecure.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ticketsecure.dto.TicketLotRequestDTO;
import com.ticketsecure.dto.TicketLotResponseDTO;
import com.ticketsecure.service.TicketLotService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequestMapping("/api/ticket-lots")
public class TicketLotController {
    @Autowired
    private TicketLotService ticketLotService;

    @PostMapping
    public ResponseEntity<TicketLotResponseDTO> createTicketLot(@Valid @RequestBody TicketLotRequestDTO request) {
        try{
            TicketLotResponseDTO response = ticketLotService.createTicketLot(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
}
