package com.ticketsecure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketsecure.domain.enumerate.ReserveStatus;
import com.ticketsecure.domain.model.Reserve;

@Repository
public interface ReserveRepository extends JpaRepository<Reserve, UUID> {
    
    List<Reserve> findByUserId(UUID userId);
    
    List<Reserve> findByTicketLotId(UUID ticketLotId);
    
    List<Reserve> findByTicketLot_Event_Id(UUID eventId);

    List<Reserve> findByStatusAndExpiredDateBefore(ReserveStatus status, LocalDateTime dateTime);
}