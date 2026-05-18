package com.ticketsecure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketsecure.domain.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findBySecurityHash(String sercurityHash);
    java.util.Optional<Ticket> findByReserveId(UUID reserveId);
}
