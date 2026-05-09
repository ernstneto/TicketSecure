package com.ticketsecure.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketsecure.dto.ReserveRequestDTO;
import com.ticketsecure.dto.ReserveResponseDTO;
import com.ticketsecure.service.ReserveService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reserves")
public class ReserveController {

    @Autowired
    private ReserveService reserveService;

    @PostMapping
    public ResponseEntity<ReserveResponseDTO> createReserve(
            @Valid @RequestBody ReserveRequestDTO requestDTO, 
            HttpServletRequest httpRequest) {
        
        try {
            // Pegando dados do cabeçalho da requisição para auditoria/segurança
            String sourceIP = httpRequest.getRemoteAddr();
            String userAgent = httpRequest.getHeader("User-Agent");

            ReserveResponseDTO response = reserveService.createReserve(requestDTO, sourceIP, userAgent);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ReserveResponseDTO> payReserve(@PathVariable UUID id) {
        System.out.println(">>> Entrou no Controller! O ID recebido foi: " + id);
        ReserveResponseDTO response = reserveService.confirmPayment(id);
        return ResponseEntity.ok(response);
        
    }
}