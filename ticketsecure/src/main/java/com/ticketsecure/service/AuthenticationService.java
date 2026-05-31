package com.ticketsecure.service;

import com.ticketsecure.domain.model.User;
import com.ticketsecure.dto.AuthRequestDTO;
import com.ticketsecure.dto.AuthResponseDTO;
import com.ticketsecure.repository.UserRepository;
import com.ticketsecure.security.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository,
                                 JwtTokenService jwtTokenService,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDTO authenticate(AuthRequestDTO authRequest) {
        User user = userRepository.findByEmail(authRequest.email())
                .orElseThrow(() -> {
                    logger.warn("Tentativa de login com email não encontrado: {}", authRequest.email());
                    return new BadCredentialsException("Email ou senha inválidos");
                });

        if (!passwordEncoder.matches(authRequest.senha(), user.getSenhaHash())) {
            logger.warn("Tentativa de login com senha inválida para: {}", authRequest.email());
            throw new BadCredentialsException("Email ou senha inválidos");
        }

        String token = jwtTokenService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        logger.info("Login realizado com sucesso para: {}", authRequest.email());

        return new AuthResponseDTO(token, user.getId(), user.getEmail(), user.getRole().name());
    }
}
