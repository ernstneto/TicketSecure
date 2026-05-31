package com.ticketsecure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketsecure.domain.model.TicketLot;
import jakarta.persistence.LockModeType;

@Repository
public interface TicketLotRepository extends JpaRepository<TicketLot, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketLot t WHERE t.id = :id")
    Optional<TicketLot> findByIdForUpdate(@Param("id") UUID id);

    List<TicketLot> findByEvent_IdOrderByPriceAsc(UUID eventId);
}
