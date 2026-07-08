package com.example.resourcebooking.controller;

import com.example.resourcebooking.model.Booking;
import com.example.resourcebooking.model.User;
import com.example.resourcebooking.repository.UserRepository;
import com.example.resourcebooking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<?> requestBooking(@RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            Long courtId = Long.valueOf(payload.get("courtId").toString());
            LocalDate bookingDate = LocalDate.parse(payload.get("bookingDate").toString());
            LocalTime startTime = LocalTime.parse(payload.get("startTime").toString());
            LocalTime endTime = LocalTime.parse(payload.get("endTime").toString());
            String purpose = (String) payload.get("purpose");
            Long sportId = payload.containsKey("sportId") && payload.get("sportId") != null
                    ? Long.valueOf(payload.get("sportId").toString()) : null;

            Booking booking = bookingService.requestBooking(user.getId(), courtId, bookingDate, startTime, endTime, purpose);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/request-aggregated")
    public ResponseEntity<?> requestAggregatedBooking(@RequestBody Map<String, Object> payload, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Long venueId = Long.valueOf(payload.get("venueId").toString());
        LocalDate bookingDate = LocalDate.parse(payload.get("bookingDate").toString());
        LocalTime startTime = LocalTime.parse(payload.get("startTime").toString());
        LocalTime endTime = LocalTime.parse(payload.get("endTime").toString());

        try {
            Booking booking = bookingService.bookAggregatedSlot(venueId, user.getId(), bookingDate, startTime, endTime);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/slots")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam Long courtId, 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<LocalTime> slots = bookingService.getAvailableSlots(courtId, date);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/my-bookings")
    public List<Booking> getMyBookings(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String when) {
        User user = getAuthenticatedUser(authentication);
        return bookingService.getBookingsByUser(user.getId(), status, when);
    }

    @GetMapping("/pending")
    public List<Booking> getPendingBookings() {
        return bookingService.getPendingBookings();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable Long id) {
        Booking approved = bookingService.approveBooking(id);
        return ResponseEntity.ok(approved);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable Long id) {
        Booking rejected = bookingService.rejectBooking(id);
        return ResponseEntity.ok(rejected);
    }
}
