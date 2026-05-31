package com.ticketsecure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketsecure.domain.enumerate.EventCategory;
import com.ticketsecure.domain.enumerate.EventStatus;
import com.ticketsecure.domain.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    long countByStatus(EventStatus status);

    @Query("""
            SELECT e FROM Event e
            WHERE e.status = :status
              AND e.eventDate >= :now
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:city IS NULL OR :city = ''
                   OR LOWER(e.city) LIKE LOWER(CONCAT('%', :city, '%'))
                   OR LOWER(e.location) LIKE LOWER(CONCAT('%', :city, '%')))
              AND (:category IS NULL OR e.category = :category)
            ORDER BY e.eventDate ASC
            """)
    List<Event> searchActiveEvents(
            @Param("status") EventStatus status,
            @Param("now") java.time.LocalDateTime now,
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("category") EventCategory category);
}
