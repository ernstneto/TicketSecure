package com.ticketsecure.controller;

import com.ticketsecure.dto.AuthRequestDTO;
import com.ticketsecure.dto.AuthResponseDTO;
import com.ticketsecure.service.AuthenticationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO authRequest) {
        try {
            AuthResponseDTO response = authenticationService.authenticate(authRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            logger.warn("Falha de autenticação: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }
}
