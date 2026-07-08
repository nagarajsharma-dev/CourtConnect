package com.example.resourcebooking.repository;

import com.example.resourcebooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByStatus(String status);
    List<Booking> findByCourtIdAndBookingDateAndStatus(Long courtId, LocalDate bookingDate, String status);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status != 'REJECTED' AND b.bookingDate >= :today ORDER BY b.bookingDate ASC")
    List<Booking> findUpcomingBookingsByUser(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.bookingDate < :today ORDER BY b.bookingDate DESC")
    List<Booking> findPastBookingsByUser(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status ORDER BY b.bookingDate DESC")
    List<Booking> findBookingsByUserAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * Court-level overlap: same court + same sport + overlapping time.
     * Different sports on the same court at the same time are ALLOWED.
     */
    @Query("SELECT b FROM Booking b " +
           "WHERE b.court.id = :courtId " +
           "AND b.sport.id = :sportId " +
           "AND b.bookingDate = :bookingDate " +
           "AND b.status != 'REJECTED' " +
           "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    List<Booking> findOverlappingBookings(@Param("courtId") Long courtId,
                                          @Param("sportId") Long sportId,
                                          @Param("bookingDate") LocalDate bookingDate,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime);

    /**
     * User-level overlap: same user across any court/sport at overlapping time.
     * A user cannot be in two places at the same time.
     */
    @Query("SELECT b FROM Booking b " +
           "WHERE b.user.id = :userId " +
           "AND b.bookingDate = :bookingDate " +
           "AND b.status != 'REJECTED' " +
           "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    List<Booking> findOverlappingBookingsByUser(@Param("userId") Long userId,
                                                @Param("bookingDate") LocalDate bookingDate,
                                                @Param("startTime") LocalTime startTime,
                                                @Param("endTime") LocalTime endTime);
}
