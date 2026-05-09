package com.ticketsecure.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.ticketsecure.domain.enumerate.Role;

public record UserResponseDTO(
    UUID id,
    String name,
    String email,
    String cpf,
    Role role,
    LocalDate createdDate
) {
    public UserResponseDTO(UUID id, String name, String email, String cpf, Role role) {
        this(id, name, email, cpf, role, LocalDate.now());
    }
}
