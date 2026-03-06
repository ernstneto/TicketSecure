package com.ticketsecure.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;
import jakarta.persistence.*;

import com.ticketsecure.domain.enumerate.EventStatus;

@Data
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, name="event_date")
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private String location;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private String local;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;
}
