package com.example.resourcebooking.service;

import com.example.resourcebooking.model.Booking;
import com.example.resourcebooking.model.Court;
import com.example.resourcebooking.model.Sport;
import com.example.resourcebooking.model.User;
import com.example.resourcebooking.repository.BookingRepository;
import com.example.resourcebooking.repository.CourtRepository;
import com.example.resourcebooking.repository.SportRepository;
import com.example.resourcebooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private CourtRepository   courtRepository;
    @Mock private UserRepository    userRepository;
    @Mock private SportRepository   sportRepository;

    @InjectMocks
    private BookingService bookingService;

    private Court court1;
    private Court court2;
    private User  user;
    private Sport sport;

    @BeforeEach
    void setUp() {
        court1 = new Court(); court1.setId(1L); court1.setName("Badminton Court 1");
        court2 = new Court(); court2.setId(2L); court2.setName("Tennis Court A");
        user   = new User();  user.setId(1L);   user.setUsername("player1");
        sport  = new Sport("BADMINTON"); sport.setId(10L);
    }

    /** Scenario 1: Booking a completely free slot → should succeed */
    @Test
    void testRequestBooking_FreeSlot_Success() {
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportRepository.findById(10L)).thenReturn(Optional.of(sport));

        when(bookingRepository.findOverlappingBookings(eq(1L), eq(10L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findOverlappingBookingsByUser(eq(1L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking result = bookingService.requestBooking(1L, 1L, 10L, LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "Practice");

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    /** Scenario 2: Same court + same sport + overlapping time → should FAIL */
    @Test
    void testRequestBooking_SameCourt_SameSport_Overlap_Fail() {
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportRepository.findById(10L)).thenReturn(Optional.of(sport));

        Booking existing = new Booking(court1, user, sport, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), "-", "APPROVED");
        when(bookingRepository.findOverlappingBookings(eq(1L), eq(10L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(List.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.requestBooking(1L, 1L, 10L, LocalDate.now(),
                        LocalTime.of(10, 30), LocalTime.of(11, 30), "Match"));

        assertTrue(ex.getMessage().contains("already booked for BADMINTON"));
    }

    /** Scenario 3: Same user, different court, overlapping time → should FAIL (user double-booked) */
    @Test
    void testRequestBooking_SameUser_DifferentCourt_Overlap_Fail() {
        when(courtRepository.findById(2L)).thenReturn(Optional.of(court2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportRepository.findById(10L)).thenReturn(Optional.of(sport));

        // No court-sport overlap on court2
        when(bookingRepository.findOverlappingBookings(eq(2L), eq(10L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList());
        // But user is already booked elsewhere at this time
        Booking userBookingElsewhere = new Booking(court1, user, sport, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), "-", "APPROVED");
        when(bookingRepository.findOverlappingBookingsByUser(eq(1L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(List.of(userBookingElsewhere));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.requestBooking(1L, 2L, 10L, LocalDate.now(),
                        LocalTime.of(10, 30), LocalTime.of(11, 30), "Extra match"));

        assertTrue(ex.getMessage().contains("already have an active booking"));
    }

    /** Scenario 4: Same user, different non-overlapping time slots → should succeed */
    @Test
    void testRequestBooking_SameUser_NonOverlappingTime_Success() {
        when(courtRepository.findById(2L)).thenReturn(Optional.of(court2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportRepository.findById(10L)).thenReturn(Optional.of(sport));

        when(bookingRepository.findOverlappingBookings(eq(2L), eq(10L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findOverlappingBookingsByUser(eq(1L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList()); // No overlap — different time slot
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        // User books 12:00-13:00 on a different court (their earlier booking was 10:00-11:00)
        Booking result = bookingService.requestBooking(1L, 2L, 10L, LocalDate.now(),
                LocalTime.of(12, 0), LocalTime.of(13, 0), "Afternoon match");

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    /** Scenario 5: Same court, same time, DIFFERENT sport → should succeed (multipurpose court) */
    @Test
    void testRequestBooking_SameCourt_SameTime_DifferentSport_Success() {
        Sport tenniseSport = new Sport("TENNIS"); tenniseSport.setId(20L);

        when(courtRepository.findById(1L)).thenReturn(Optional.of(court1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportRepository.findById(20L)).thenReturn(Optional.of(tenniseSport));

        // No overlap for TENNIS on court1 at this time (BADMINTON is booked, TENNIS is not)
        when(bookingRepository.findOverlappingBookings(eq(1L), eq(20L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findOverlappingBookingsByUser(eq(1L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking result = bookingService.requestBooking(1L, 1L, 20L, LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "Tennis session");

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }
}
