package com.ticketsecure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ticketsecure.domain.enumerate.EventCategory;
import com.ticketsecure.domain.enumerate.EventStatus;
import com.ticketsecure.domain.model.Event;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.dto.EventSearchCriteria;
import com.ticketsecure.repository.EventRepository;
import com.ticketsecure.repository.TicketLotRepository;

@ExtendWith(MockitoExtension.class)
class EventSearchServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketLotRepository ticketLotRepository;

    @InjectMocks
    private EventSearchService eventSearchService;

    private Event cheapEvent;
    private Event expensiveEvent;
    private TicketLot cheapLot;
    private TicketLot expensiveLot;

    @BeforeEach
    void setUp() {
        cheapEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Show Barato")
                .category(EventCategory.SHOW)
                .city("São Paulo")
                .local("Arena SP")
                .location("São Paulo")
                .latitude(-23.55)
                .longitude(-46.63)
                .eventDate(LocalDateTime.now().plusDays(3))
                .status(EventStatus.ACTIVE)
                .build();

        expensiveEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Festival Premium")
                .category(EventCategory.FESTIVAL)
                .city("São Paulo")
                .local("Autódromo")
                .location("São Paulo")
                .latitude(-23.70)
                .longitude(-46.70)
                .eventDate(LocalDateTime.now().plusDays(10))
                .status(EventStatus.ACTIVE)
                .build();

        cheapLot = TicketLot.builder()
                .id(UUID.randomUUID())
                .event(cheapEvent)
                .name("Pista")
                .price(new BigDecimal("50.00"))
                .availableQuantity(20)
                .build();

        expensiveLot = TicketLot.builder()
                .id(UUID.randomUUID())
                .event(expensiveEvent)
                .name("VIP")
                .price(new BigDecimal("300.00"))
                .availableQuantity(10)
                .build();
    }

    @Test
    void shouldRankCheaperEventFirstWhenPreferCheaper() {
        when(eventRepository.searchActiveEvents(any(), any(), any(), any(), any()))
                .thenReturn(List.of(expensiveEvent, cheapEvent));
        when(ticketLotRepository.findByEvent_IdOrderByPriceAsc(cheapEvent.getId()))
                .thenReturn(List.of(cheapLot));
        when(ticketLotRepository.findByEvent_IdOrderByPriceAsc(expensiveEvent.getId()))
                .thenReturn(List.of(expensiveLot));

        var results = eventSearchService.search(new EventSearchCriteria(
                null, null, null, null, null, null, null, true, 5));

        assertFalse(results.isEmpty());
        assertEquals("Show Barato", results.get(0).title());
    }

    @Test
    void shouldFilterByMaxPrice() {
        when(eventRepository.searchActiveEvents(any(), any(), any(), any(), any()))
                .thenReturn(List.of(expensiveEvent, cheapEvent));
        when(ticketLotRepository.findByEvent_IdOrderByPriceAsc(cheapEvent.getId()))
                .thenReturn(List.of(cheapLot));
        when(ticketLotRepository.findByEvent_IdOrderByPriceAsc(expensiveEvent.getId()))
                .thenReturn(List.of(expensiveLot));

        var results = eventSearchService.search(new EventSearchCriteria(
                null, null, null, new BigDecimal("80.00"), null, null, null, false, 5));

        assertEquals(1, results.size());
        assertEquals("Show Barato", results.get(0).title());
    }
}
