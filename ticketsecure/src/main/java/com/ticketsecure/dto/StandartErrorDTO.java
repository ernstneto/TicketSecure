package com.ticketsecure.dto;

import java.time.LocalDateTime;

public record StandartErrorDTO(
    LocalDateTime timestamp,
    Integer status,
    String error,
    String message,
    String path  
){} 