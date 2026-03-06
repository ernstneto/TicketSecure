package com.ticketsecure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketsecure.domain.model.TicketLot;

@Repository
public interface TicketLotRepository extends JpaRepository<TicketLot, UUID> {
    List<TicketLot> findByEventId(UUID eventId);
}
