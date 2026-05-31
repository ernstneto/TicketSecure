package com.ticketsecure.service;

import com.ticketsecure.domain.enumerate.Role;
import com.ticketsecure.domain.model.User;
import com.ticketsecure.dto.UserRequestDTO;
import com.ticketsecure.dto.UserResponseDTO;
import com.ticketsecure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if (userRepository.existsByEmail(userRequestDTO.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if (userRepository.findByCpf(userRequestDTO.cpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        User user = User.builder()
                .name(userRequestDTO.name())
                .email(userRequestDTO.email())
                .senhaHash(passwordEncoder.encode(userRequestDTO.senha()))
                .cpf(userRequestDTO.cpf())
                .role(Role.CLIENT)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("Usuário criado com sucesso: {}", savedUser.getEmail());

        return new UserResponseDTO(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getCpf(),
            savedUser.getRole()
        );
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getCpf(), user.getRole());
    }
}
