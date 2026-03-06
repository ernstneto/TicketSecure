package com.ticketsecure.domain.model;

import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Data;
import java.math.BigDecimal;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "ticket_lots")
public class TicketLot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name="total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
}
