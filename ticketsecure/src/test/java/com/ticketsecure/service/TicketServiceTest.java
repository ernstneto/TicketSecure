package com.ticketsecure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.ticketsecure.domain.model.User;
import com.ticketsecure.dto.TicketValidationResponseDTO;
import com.ticketsecure.domain.enumerate.TicketStatus;
import com.ticketsecure.domain.model.Event;
import com.ticketsecure.domain.model.Reserve;
import com.ticketsecure.domain.model.Ticket;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;

    @BeforeEach
    public void setUp() {
        // Injeta a variavel de ambiente (qr-salt) no Mockito
        ReflectionTestUtils.setField(ticketService, "qrSalt", "test-salt");

        // Montando um cenario de teste ficticio (Mock)
        User user = new User();
        user.setName("Fã de Testes");
        Event event = new Event();
        event.setTitle("Rock in JUnit");

        TicketLot lot = new TicketLot();
        lot.setEvent(event);
        
        Reserve reserve = new Reserve();
        reserve.setUser(user);
        reserve.setTicketLot(lot);

        Ticket mockTicket = new Ticket(reserve, "hash-super-segura-123");
        mockTicket.setStatus(TicketStatus.VALID);
        this.ticket = mockTicket;
    }

    @Test
    @DisplayName("Deve permitir o acesso e queimar o ingresso se for válido")
    void shouldAllowAccessAndBurnTicket() {
        // 1. Prepara a armadilha: Quando o sistema procurar o Hash, devolva nosso Ingresso Válido
        when(ticketRepository.findBySecurityHash("hash-super-segura-123"))
                .thenReturn(Optional.of(ticket));

        // 2. Executa a ação
        TicketValidationResponseDTO response = ticketService.validadeAndConsumeTicket("hash-super-segura-123");

        // 3. Valida os resultados (Assertions)
        assertEquals("ALLOWED", response.status());
        assertEquals("Fã de Testes", response.userName());
        
        // Verifica se o status mudou para USED
        assertEquals(TicketStatus.USED, ticket.getStatus());
        
        // Verifica se o sistema salvou essa alteração no banco
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    @DisplayName("Deve bloquear cambistas tentando usar ingresso já queimado")
    void shouldBlockAlreadyUsedTicket() {
        // O ingresso já foi usado antes!
        ticket.setStatus(TicketStatus.USED);

        when(ticketRepository.findBySecurityHash("hash-super-segura-123"))
                .thenReturn(Optional.of(ticket));

        TicketValidationResponseDTO response = ticketService.validadeAndConsumeTicket("hash-super-segura-123");

        assertEquals("DENIED", response.status());
        assertEquals("ACESSO NEGADO: Este ingresso já foi utilizado!", response.message());
        
        // Garante que não tentou salvar no banco de dados novamente
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}
