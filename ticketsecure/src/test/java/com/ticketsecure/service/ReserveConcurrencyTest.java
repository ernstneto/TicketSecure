package com.ticketsecure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ticketsecure.domain.enumerate.Role;
import com.ticketsecure.domain.model.Event;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.domain.model.User;
import com.ticketsecure.dto.ReserveRequestDTO;
import com.ticketsecure.repository.EventRepository;
import com.ticketsecure.repository.TicketLotRepository;
import com.ticketsecure.repository.UserRepository;

@SpringBootTest
public class ReserveConcurrencyTest {
    @Autowired
    private ReserveService reserveService;

    @Autowired
    private TicketLotRepository ticketLotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("Teste de Stress: 10 utilizadores tentam comrar o ultimo bilhete disponível simultaneamente")
    public void testConcurrentReservations() throws InterruptedException {
        String uniqueHash = UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        // 1. Montagem do cenário
        User user = new User();
        user.setName("Test User");
        user.setEmail("test_"+ uniqueHash +"@ticketsecure.com");
        user.setSenhaHash("hashedpassword");
        user.setCpf(uniqueHash); // CPF único para evitar conflitos
        user.setRole(Role.USER);
        user = userRepository.save(user);

        Event event = new Event();
        event.setTitle("Test Event");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setLocation("location test");
        event.setLocal("local test");
        event.setStatus(com.ticketsecure.domain.enumerate.EventStatus.ACTIVE);
        event = eventRepository.save(event);

        TicketLot ticketLot = new TicketLot();
        ticketLot.setEvent(event);
        ticketLot.setName("Test Ticket Lot");
        //ticketLot.setQuantity(1);
        ticketLot.setTotalQuantity(1);
        ticketLot.setAvailableQuantity(1);
        ticketLot.setPrice(new BigDecimal("100.00"));
        ticketLot = ticketLotRepository.save(ticketLot);
        
        // 2. Preparacao da corrida (Multithreading)
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latchdePartida = new CountDownLatch(1);
        CountDownLatch latchdeChegada = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successfulReservations = new AtomicInteger(0);
        AtomicInteger failedReservations = new AtomicInteger(0);

        ReserveRequestDTO requestDTO = new ReserveRequestDTO(user.getId(), ticketLot.getId());

        // 3. Criaras Threads
        for(int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    latchdePartida.await(); // Espera pela largada
                    reserveService.createReserve(requestDTO, "127.0.0.1", "JUnit-Stress-Agent");
                    successfulReservations.incrementAndGet();
                } catch (Exception e) {
                    failedReservations.incrementAndGet();
                } finally {
                    latchdeChegada.countDown(); // Indica que a thread terminou
                }
            });
        }

        // 4. Liberta as 10 Threads
        latchdePartida.countDown();

        // Aguarda todas as Threads terminarem
        latchdeChegada.await();
        executorService.shutdown();

        // 5. Verificacoes
        TicketLot updatedTicketLot = ticketLotRepository.findById(ticketLot.getId()).orElseThrow();

        System.out.println("--- RESULTADO DO STRESS TEST ---");
        System.out.println("Reservas bem-sucedidas: " + successfulReservations.get());
        System.out.println("Reservas falhadas: " + failedReservations.get());
        System.out.println("Quantidade de bilhetes disponíveis após o teste: " + updatedTicketLot.getAvailableQuantity());

        // Verifica que apenas 1 reserva foi bem-sucedida
        assertEquals(1, successfulReservations.get(), "Apenas uma reserva deve ser bem-sucedida");
        assertEquals(9, failedReservations.get(), "Nove reservas devem falhar");
        assertEquals(0, updatedTicketLot.getAvailableQuantity(), "O lote de bilhetes deve estar esgotado");
    }
}
