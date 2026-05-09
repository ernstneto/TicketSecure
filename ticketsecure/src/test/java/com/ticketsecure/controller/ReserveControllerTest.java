package com.ticketsecure.controller;

import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest; // <-- Import correto para o Spring Boot 4+
import org.springframework.test.context.bean.override.mockito.MockitoBean; // <-- Substitui o antigo MockBean
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper; // <-- Import correto para o Jackson 3+

import com.ticketsecure.config.SecurityConfig;
import com.ticketsecure.domain.enumerate.ReserveStatus;
import com.ticketsecure.dto.ReserveRequestDTO;
import com.ticketsecure.dto.ReserveResponseDTO;
import com.ticketsecure.service.ReserveService;

@Import(SecurityConfig.class)
@WebMvcTest(ReserveController.class)
public class ReserveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // <-- Usando a anotação moderna no lugar de @Mock ou @MockBean
    private ReserveService reserveService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void deveRetornar201QuandoCriarReservaComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID ticketLotId = UUID.randomUUID();

        ReserveRequestDTO request = new ReserveRequestDTO(userId, ticketLotId);

        ReserveResponseDTO response = new ReserveResponseDTO(
            UUID.randomUUID(),
            userId,
            ticketLotId,
            ReserveStatus.PENDING_PAYMENT,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(15)
        );

        when(reserveService.createReserve(any(ReserveRequestDTO.class), any(), any()))
            .thenReturn(response);

        mockMvc.perform(post("/api/reserves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }
}