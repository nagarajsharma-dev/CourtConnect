package com.example.resourcebooking.service;

import com.example.resourcebooking.model.Booking;
import com.example.resourcebooking.model.Court;
import com.example.resourcebooking.model.CourtStatus;
import com.example.resourcebooking.model.Sport;
import com.example.resourcebooking.model.User;
import com.example.resourcebooking.model.Venue;
import com.example.resourcebooking.model.SlotAvailabilityDTO;
import com.example.resourcebooking.repository.BookingRepository;
import com.example.resourcebooking.repository.CourtRepository;
import com.example.resourcebooking.repository.SportRepository;
import com.example.resourcebooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private com.example.resourcebooking.repository.VenueRepository venueRepository;

    private boolean isSlotAvailable(Long courtId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Booking> approvedBookings = bookingRepository.findByCourtIdAndBookingDateAndStatus(courtId, date, "APPROVED");
        for (Booking b : approvedBookings) {
            if (startTime.isBefore(b.getEndTime()) && endTime.isAfter(b.getStartTime())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request a booking for a specific court + sport + time slot.
     *
     * Business rules:
     *  1. Same court + same sport + overlapping time → REJECTED (already taken)
     *  2. Same user + any court/sport + overlapping time → REJECTED (user double-booked)
     *  3. Same court + different sport + overlapping time → ALLOWED (multipurpose court)
     *  4. Different time, same user, any court/sport → ALLOWED
     */
    public Booking requestBooking(Long userId, Long courtId, Long sportId, LocalDate bookingDate,
                                   LocalTime startTime, LocalTime endTime, String purpose) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Sport sport = sportId != null
                ? sportRepository.findById(sportId).orElseThrow(() -> new RuntimeException("Sport not found"))
                : null;

        // Rule 1: same court + same sport + overlapping time → blocked
        if (sport != null) {
            List<Booking> courtSportOverlaps = bookingRepository.findOverlappingBookings(
                    courtId, sportId, bookingDate, startTime, endTime);
            if (!courtSportOverlaps.isEmpty()) {
                throw new RuntimeException("This court is already booked for " + sport.getName() + " during this time slot.");
            }
        }

        // Rule 2: same user at any court for overlapping times → blocked
        List<Booking> userOverlaps = bookingRepository.findOverlappingBookingsByUser(userId, bookingDate, startTime, endTime);
        if (!userOverlaps.isEmpty()) {
            throw new RuntimeException("You already have an active booking during this time slot.");
        }

        // Check if within venue hours
        Venue venue = court.getVenue();
        if (venue != null && (startTime.isBefore(venue.getOpenTime()) || endTime.isAfter(venue.getCloseTime()))) {
            throw new RuntimeException("Booking time is outside venue operational hours (" + venue.getOpenTime() + " - " + venue.getCloseTime() + ").");
        }

        Booking booking = new Booking(court, user, sport, bookingDate, startTime, endTime, purpose, "PENDING");
        return bookingRepository.save(booking);
    }

    /** Backward-compatible overload (no sport) */
    public Booking requestBooking(Long userId, Long courtId, LocalDate bookingDate,
                                   LocalTime startTime, LocalTime endTime, String purpose) {
        return requestBooking(userId, courtId, null, bookingDate, startTime, endTime, purpose);
    }

    public List<SlotAvailabilityDTO> getAggregatedSlots(Long venueId, LocalDate date, int durationMinutes) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        List<Court> venueCourts = courtRepository.findByVenue_Id(venueId);
        List<Court> activeCourts = new ArrayList<>();
        for (Court c : venueCourts) {
            if (CourtStatus.AVAILABLE == c.getStatus()) {
                activeCourts.add(c);
            }
        }

        List<SlotAvailabilityDTO> aggregatedSlots = new ArrayList<>();
        aggregatedSlots.clear();
        LocalTime current = venue.getOpenTime();
        while (current.isBefore(venue.getCloseTime())) {
            LocalTime slotEnd = current.plusMinutes(durationMinutes);
            // Prevent infinite loop if slotEnd wraps past midnight or goes beyond closeTime
            if (slotEnd.isBefore(current) || slotEnd.isAfter(venue.getCloseTime())) {
                break;
            }
            
            int availableCount = 0;
            for (Court court : activeCourts) {
                if (isSlotAvailable(court.getId(), date, current, slotEnd)) {
                    availableCount++;
                }
            }
            if (availableCount > 0) {
                aggregatedSlots.add(new SlotAvailabilityDTO(current, slotEnd, availableCount));
            }
            current = slotEnd; // Advance by duration
        }
        return aggregatedSlots;
    }

    public List<LocalTime> getAvailableSlots(Long courtId, LocalDate date) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));
        Venue venue = court.getVenue();

        List<Booking> approvedBookings = bookingRepository.findByCourtIdAndBookingDateAndStatus(courtId, date, "APPROVED");

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime current = venue.getOpenTime();

        while (current.isBefore(venue.getCloseTime())) {
            LocalTime slotEnd = current.plusHours(1);
            if (slotEnd.isBefore(current) || slotEnd.isAfter(venue.getCloseTime())) {
                break; 
            }
            boolean isOverlapping = false;
            for (Booking b : approvedBookings) {
                if (current.isBefore(b.getEndTime()) && slotEnd.isAfter(b.getStartTime())) {
                    isOverlapping = true;
                    break;
                }
            }

            if (!isOverlapping) availableSlots.add(current);
            current = slotEnd;
        }

        return availableSlots;
    }

    public Booking bookAggregatedSlot(Long venueId, Long userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Court> venueCourts = courtRepository.findByVenue_Id(venueId);
        Court availableCourt = null;

        for (Court court : venueCourts) {
            if (CourtStatus.AVAILABLE == court.getStatus() && isSlotAvailable(court.getId(), date, startTime, endTime)) {
                availableCourt = court;
                break;
            }
        }

        if (availableCourt == null) {
            throw new RuntimeException("No available courts for the requested time slot.");
        }

        return requestBooking(userId, availableCourt.getId(), null, date, startTime, endTime, "Aggregated Booking");
    }

    public List<Booking> getBookingsByUser(Long userId, String status, String when) {
        if ("upcoming".equalsIgnoreCase(when)) {
            return bookingRepository.findUpcomingBookingsByUser(userId, LocalDate.now());
        } else if ("past".equalsIgnoreCase(when)) {
            return bookingRepository.findPastBookingsByUser(userId, LocalDate.now());
        } else {
            return bookingRepository.findBookingsByUserAndStatus(userId, status);
        }
    }

    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatus("PENDING");
    }

    public Booking approveBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Re-check same court+sport overlap at approval time
        Sport sport = booking.getSport();
        if (sport != null) {
            List<Booking> courtSportOverlaps = bookingRepository.findOverlappingBookings(
                    booking.getCourt().getId(), sport.getId(),
                    booking.getBookingDate(), booking.getStartTime(), booking.getEndTime());
            courtSportOverlaps.removeIf(b -> b.getId().equals(bookingId));
            if (!courtSportOverlaps.isEmpty()) {
                booking.setStatus("REJECTED");
                bookingRepository.save(booking);
                throw new RuntimeException("Another booking for " + sport.getName() + " was approved for this time slot. Booking rejected.");
            }
        }

        // Re-check user double-booking
        List<Booking> userOverlaps = bookingRepository.findOverlappingBookingsByUser(
                booking.getUser().getId(), booking.getBookingDate(), booking.getStartTime(), booking.getEndTime());
        userOverlaps.removeIf(b -> b.getId().equals(bookingId));
        if (!userOverlaps.isEmpty()) {
            booking.setStatus("REJECTED");
            bookingRepository.save(booking);
            throw new RuntimeException("The user already has another booking approved for this time slot. Booking rejected.");
        }

        booking.setStatus("APPROVED");
        return bookingRepository.save(booking);
    }

    public Booking rejectBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("REJECTED");
        return bookingRepository.save(booking);
    }
}
