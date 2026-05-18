package com.ticketsecure.dto;

import java.util.UUID;

public record FraudResultDTO (UUID reserveId, String status) {}
