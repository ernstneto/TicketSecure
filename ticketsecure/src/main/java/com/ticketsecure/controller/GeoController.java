package com.ticketsecure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketsecure.dto.ResolvedLocation;
import com.ticketsecure.service.GeocodingService;

@RestController
@RequestMapping("/api/geo")
public class GeoController {

    private final GeocodingService geocodingService;

    public GeoController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/reverse")
    public ResponseEntity<ResolvedLocation> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng) {
        return geocodingService.reverseGeocode(lat, lng)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
