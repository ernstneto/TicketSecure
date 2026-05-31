package com.ticketsecure.dto;

import java.util.UUID;

public record AuthResponseDTO(
    String token,
    UUID userId,
    String email,
    String role
) {}
