package com.ticketsecure.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ticketsecure.domain.enumerate.TicketStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="tickets")
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "reserve_id", nullable = false, unique = true)
    private Reserve reserve;

    @Column(name = "security_hash", nullable = false, unique = true, length = 64)
    private String securityHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    // Construtor vazio (obrigatorio para o JPA)
    public Ticket() {}

    public Ticket(Reserve reserve, String securityHash) {
        this.reserve = reserve;
        this.securityHash = securityHash;
        this.status = TicketStatus.VALID;
        this.createdDate = LocalDateTime.now();
    }

    // --- GETTERS ---
    public UUID getId() { return id; }
    public Reserve getReserve() { return reserve; }
    public String getSecurityHash() { return securityHash; }
    public TicketStatus getStatus() { return status; }
    
    // --- SETTERS ---
    public void setStatus(TicketStatus status) { this.status = status; }
}
