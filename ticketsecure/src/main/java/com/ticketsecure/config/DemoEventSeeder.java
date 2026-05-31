package com.ticketsecure.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ticketsecure.domain.enumerate.EventCategory;
import com.ticketsecure.domain.enumerate.EventStatus;
import com.ticketsecure.domain.model.Event;
import com.ticketsecure.domain.model.TicketLot;
import com.ticketsecure.repository.EventRepository;
import com.ticketsecure.repository.TicketLotRepository;

@Component
public class DemoEventSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoEventSeeder.class);

    private final EventRepository eventRepository;
    private final TicketLotRepository ticketLotRepository;

    public DemoEventSeeder(EventRepository eventRepository, TicketLotRepository ticketLotRepository) {
        this.eventRepository = eventRepository;
        this.ticketLotRepository = ticketLotRepository;
    }

    @Override
    public void run(String... args) {
        if (eventRepository.count() > 0) {
            return;
        }

        seedEvent(
                "Rock in Paulista",
                "Show de rock nacional ao ar livre na região central.",
                EventCategory.SHOW,
                "São Paulo",
                "Av. Paulista, 900",
                "Palco Paulista",
                -23.5614, -46.6558,
                LocalDateTime.now().plusDays(5),
                new LotSeed("Pista", BigDecimal.valueOf(89.90), 120),
                new LotSeed("VIP", BigDecimal.valueOf(189.90), 40));

        seedEvent(
                "Cinema Premium: Duna — Parte Três",
                "Sessão IMAX com ingressos numerados.",
                EventCategory.CINEMA,
                "São Paulo",
                "Shopping Iguatemi",
                "Cinemark Iguatemi",
                -23.5762, -46.6888,
                LocalDateTime.now().plusDays(2),
                new LotSeed("Inteira", BigDecimal.valueOf(54.90), 80),
                new LotSeed("Meia", BigDecimal.valueOf(27.45), 30));

        seedEvent(
                "Festival Eletrônico Aurora",
                "DJs nacionais e internacionais em arena fechada.",
                EventCategory.FESTIVAL,
                "São Paulo",
                "Interlagos",
                "Autódromo de Interlagos",
                -23.7036, -46.6997,
                LocalDateTime.now().plusDays(12),
                new LotSeed("Lote Promocional", BigDecimal.valueOf(149.00), 200),
                new LotSeed("Backstage", BigDecimal.valueOf(399.00), 25));

        seedEvent(
                "Peça: O Fantasma da Ópera",
                "Musical clássico em temporada limitada.",
                EventCategory.THEATER,
                "São Paulo",
                "Bela Vista",
                "Teatro Renault",
                -23.5565, -46.6490,
                LocalDateTime.now().plusDays(8),
                new LotSeed("Plateia", BigDecimal.valueOf(120.00), 60),
                new LotSeed("Camarote", BigDecimal.valueOf(250.00), 15));

        seedEvent(
                "Show Acústico MPB",
                "Noite intimista com artistas da MPB.",
                EventCategory.SHOW,
                "Campinas",
                "Centro",
                "Teatro Municipal de Campinas",
                -22.9056, -47.0608,
                LocalDateTime.now().plusDays(6),
                new LotSeed("Geral", BigDecimal.valueOf(75.00), 90));

        seedEvent(
                "Cinema Classic: O Senhor dos Anéis (Maratona)",
                "Maratona estendida com os três filmes.",
                EventCategory.CINEMA,
                "São Paulo",
                "Pinheiros",
                "Cinearte",
                -23.5671, -46.6919,
                LocalDateTime.now().plusDays(4),
                new LotSeed("Maratona", BigDecimal.valueOf(45.00), 50));

        logger.info("[SEED] Catalogo demo de eventos carregado com sucesso.");
    }

    private void seedEvent(String title, String description, EventCategory category, String city,
                           String location, String local, double lat, double lng,
                           LocalDateTime eventDate, LotSeed... lots) {
        Event event = Event.builder()
                .title(title)
                .description(description)
                .category(category)
                .city(city)
                .location(location)
                .local(local)
                .latitude(lat)
                .longitude(lng)
                .eventDate(eventDate)
                .status(EventStatus.ACTIVE)
                .build();
        event = eventRepository.save(event);

        for (LotSeed lotSeed : lots) {
            TicketLot lot = TicketLot.builder()
                    .event(event)
                    .name(lotSeed.name())
                    .price(lotSeed.price())
                    .totalQuantity(lotSeed.quantity())
                    .availableQuantity(lotSeed.quantity())
                    .build();
            ticketLotRepository.save(lot);
        }
    }

    private record LotSeed(String name, BigDecimal price, int quantity) {}
}
