package com.example.resourcebooking.controller;

import com.example.resourcebooking.model.Venue;
import com.example.resourcebooking.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.resourcebooking.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    @Autowired
    private VenueService venueService;
    
    @Autowired
    private BookingService bookingService;

    @GetMapping
    public List<Venue> getAllVenues() {
        return venueService.getAllVenues();
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<?> getAggregatedSlots(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "30") int duration) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        try {
            return ResponseEntity.ok(bookingService.getAggregatedSlots(id, date, duration));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addVenue(@RequestBody Venue venue) {
        Venue saved = venueService.addVenue(venue);
        return ResponseEntity.ok(saved);
    }
}
