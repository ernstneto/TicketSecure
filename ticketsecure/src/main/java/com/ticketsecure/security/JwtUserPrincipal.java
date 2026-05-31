package com.ticketsecure.security;

import java.util.UUID;

public record JwtUserPrincipal(UUID id, String email, String role) {
}
