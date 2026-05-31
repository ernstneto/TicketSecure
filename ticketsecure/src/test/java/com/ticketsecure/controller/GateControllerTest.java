package com.ticketsecure.controller;

import com.ticketsecure.dto.TicketValidationRequestDTO;
import com.ticketsecure.dto.TicketValidationResponseDTO;
import com.ticketsecure.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// Usamos a mesma estrutura de teste que deu sucesso no seu TicketServiceTest!
@ExtendWith(MockitoExtension.class)
public class GateControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private GateController gateController;

    @Test
    @DisplayName("Endpoint da catraca deve retornar HTTP 200 e ALLOWED (Teste Unitário Puro)")
    void validateTicketEndpoint_Success() {
        // 1. Prepara a resposta fake do Service
        TicketValidationResponseDTO fakeResponse = new TicketValidationResponseDTO(
                "ALLOWED", "Acesso Liberado", "Evento Teste", "Usuário Teste"
        );
        
        // 2. Ensina o Mock
        when(ticketService.validadeAndConsumeTicket(anyString())).thenReturn(fakeResponse);

        // 3. Prepara o Request (DTO)
        TicketValidationRequestDTO request = new TicketValidationRequestDTO("meu-hash-123");

        // 4. Executa o método do Controller DIRETAMENTE (sem simular requisição HTTP)
        ResponseEntity<TicketValidationResponseDTO> responseEntity = gateController.validateTicket(request);

        // 5. Valida os resultados verificando o objeto ResponseEntity
        assertEquals(200, responseEntity.getStatusCode().value(), "O status HTTP deve ser 200 OK");
        assertNotNull(responseEntity.getBody(), "O corpo da resposta não pode ser nulo");
        
        TicketValidationResponseDTO body = responseEntity.getBody();
        assertEquals("ALLOWED", body.status());
        assertEquals("Usuário Teste", body.userName());
    }
}