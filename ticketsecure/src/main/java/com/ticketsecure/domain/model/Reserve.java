package com.ticketsecure.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;
import com.ticketsecure.domain.enumerate.ReserveStatus;

@Data
@Entity
@Table(name = "reserves")
public class Reserve {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "ticket_lot_id", nullable = false)
    private TicketLot ticketLot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReserveStatus status;

    @Column(name = "reverse_date", nullable = false)
    private LocalDateTime reverseDate = LocalDateTime.now();

    @Column(name = "expired_date", nullable = false)
    private LocalDateTime expiredDate;

    @Column(name = "source_ip",  nullable = false)
    private String sourceIP;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
}