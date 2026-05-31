package com.ticketsecure.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import com.ticketsecure.domain.enumerate.EventStatus;
import com.ticketsecure.domain.enumerate.EventCategory;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "event_local", nullable = false)
    private String local;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @Builder.Default
    private EventCategory category = EventCategory.OTHER;

    @Column(nullable = true)
    @Builder.Default
    private String city = "";

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @PrePersist
    @PreUpdate
    void ensureDefaults() {
        if (category == null) {
            category = EventCategory.OTHER;
        }
        if (city == null || city.isBlank()) {
            city = location != null ? location : "";
        }
    }
}
