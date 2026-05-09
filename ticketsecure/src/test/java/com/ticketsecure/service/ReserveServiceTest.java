package com.ticketsecure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ticketsecure.domain.enumerate.ReserveStatus;
import com.ticketsecure.domain.model.Reserve;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.domain.model.User;
import com.ticketsecure.dto.ReserveRequestDTO;
import com.ticketsecure.dto.ReserveResponseDTO;
import com.ticketsecure.repository.ReserveRepository;
import com.ticketsecure.repository.UserRepository;
import com.ticketsecure.repository.TicketLotRepository;


@ExtendWith(MockitoExtension.class)
public class ReserveServiceTest {
    @Mock
    private ReserveRepository reserveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketLotRepository ticketLotRepository;

    @InjectMocks
    private ReserveService reserveService;

    private User user;
    private TicketLot ticketLot;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");

        ticketLot = new TicketLot();
        ticketLot.setId(UUID.randomUUID());
        ticketLot.setAvailableQuantity(10);
    }

    @Test
    public void deveCriarReservaComSucessoEDiminuirQuantidade() {
        ReserveRequestDTO request = new ReserveRequestDTO(user.getId(), ticketLot.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(ticketLotRepository.findById(ticketLot.getId())).thenReturn(Optional.of(ticketLot));

        Reserve savedReserve = new Reserve();
        savedReserve.setId(UUID.randomUUID());
        savedReserve.setUser(user);
        savedReserve.setTicketLot(ticketLot);
        savedReserve.setStatus(ReserveStatus.PENDING_PAYMENT);
        savedReserve.setReverseDate(LocalDateTime.now());
        savedReserve.setExpiredDate(LocalDateTime.now().plusMinutes(15));

        when(reserveRepository.save(any(Reserve.class))).thenReturn(savedReserve);

        ReserveResponseDTO response = reserveService.createReserve(request, "127.0.0.1", "Postman-Runtime");
        
        assertNotNull(response);
        assertEquals(ReserveStatus.PENDING_PAYMENT, response.status());
        assertEquals(9, ticketLot.getAvailableQuantity());

        verify(ticketLotRepository, times(1)).save(ticketLot);
        verify(reserveRepository, times(1)).save(any(Reserve.class));
    }

    @Test
    void deveLancarExcecaoQuandoLoteEsgotado() {
        ticketLot.setAvailableQuantity(0);
        ReserveRequestDTO request = new ReserveRequestDTO(user.getId(), ticketLot.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(ticketLotRepository.findById(ticketLot.getId())).thenReturn(Optional.of(ticketLot));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reserveService.createReserve(request, "127.0.0.1", "Postman-Runtime");
        });

        assertEquals("Lote de ingressos esgotado", exception.getMessage());

        verify(ticketLotRepository, never()).save(any());
        verify(reserveRepository, never()).save(any());

    }
}