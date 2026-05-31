package com.ticketsecure.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ticketsecure.dto.StandartErrorDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackages = "com.ticketsecure")
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandartErrorDTO> handlerIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        StandartErrorDTO error = new StandartErrorDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Erro de Validação / Regra de Negócio",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandartErrorDTO> handlerGenericException(Exception ex, HttpServletRequest request) {
        StandartErrorDTO error = new StandartErrorDTO(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Erro Interno do Servidor",
            "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.",
            request.getRequestURI()
        );
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
