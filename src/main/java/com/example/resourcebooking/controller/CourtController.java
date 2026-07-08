package com.example.resourcebooking.controller;

import com.example.resourcebooking.model.Court;
import com.example.resourcebooking.service.CourtService;
import com.example.resourcebooking.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
public class CourtController {

    @Autowired
    private CourtService courtService;
    
    @Autowired
    private VenueService venueService;

    @GetMapping
    public List<Court> getAllCourts(@RequestParam(required = false) Long venueId) {
        if (venueId != null) {
            return courtService.getCourtsByVenueId(venueId);
        }
        return courtService.getAllCourts();
    }

    @PostMapping
    public ResponseEntity<?> addCourt(@RequestBody Court court) {
        // Need to fetch venue from DB if it's sent just as ID in JSON
        if (court.getVenue() != null && court.getVenue().getId() != null) {
            court.setVenue(venueService.getVenueById(court.getVenue().getId()));
        }
        Court savedCourt = courtService.addCourt(court);
        return ResponseEntity.ok(savedCourt);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCourtStatus(@PathVariable Long id, @RequestBody com.example.resourcebooking.model.CourtStatus status) {
        Court updated = courtService.updateCourtStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
