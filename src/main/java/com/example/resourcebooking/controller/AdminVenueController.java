package com.example.resourcebooking.controller;

import com.example.resourcebooking.model.Venue;
import com.example.resourcebooking.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.Map;

/**
 * Admin-only endpoints for managing venues.
 * Secured via @PreAuthorize("hasRole('ADMIN')").
 */
@RestController
@RequestMapping("/api/admin/venues")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVenueController {

    @Autowired
    private VenueService venueService;

    @PostMapping
    public ResponseEntity<?> createVenue(@RequestBody Map<String, Object> body) {
        try {
            Venue venue = new Venue();
            venue.setName((String) body.get("name"));
            venue.setAddress((String) body.get("address"));
            venue.setCity((String) body.get("city"));
            venue.setAmenities((String) body.getOrDefault("amenities", ""));

            String openTimeStr = (String) body.getOrDefault("openTime", "08:00");
            String closeTimeStr = (String) body.getOrDefault("closeTime", "22:00");
            venue.setOpenTime(LocalTime.parse(openTimeStr));
            venue.setCloseTime(LocalTime.parse(closeTimeStr));

            return ResponseEntity.ok(venueService.addVenue(venue));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVenue(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Venue venue = venueService.getVenueById(id);
            if (body.containsKey("name"))      venue.setName((String) body.get("name"));
            if (body.containsKey("address"))   venue.setAddress((String) body.get("address"));
            if (body.containsKey("city"))      venue.setCity((String) body.get("city"));
            if (body.containsKey("amenities")) venue.setAmenities((String) body.get("amenities"));
            if (body.containsKey("openTime"))  venue.setOpenTime(LocalTime.parse((String) body.get("openTime")));
            if (body.containsKey("closeTime")) venue.setCloseTime(LocalTime.parse((String) body.get("closeTime")));
            return ResponseEntity.ok(venueService.addVenue(venue));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.ok("Venue deleted successfully");
    }
}
