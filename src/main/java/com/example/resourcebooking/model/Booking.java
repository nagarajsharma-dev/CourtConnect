package com.example.resourcebooking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The sport being booked — allows multiple sports at same court/time */
    @ManyToOne
    @JoinColumn(name = "sport_id")
    private com.example.resourcebooking.model.Sport sport;

    @NotNull(message = "Booking date is required")
    @Column(nullable = false)
    private LocalDate bookingDate;

    @NotNull(message = "Start time is required")
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(nullable = false)
    private LocalTime endTime;

    private String purpose;

    @Column(nullable = false)
    private String status; // e.g., PENDING, APPROVED, REJECTED, CANCELLED

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Booking() {}

    public Booking(Court court, User user, com.example.resourcebooking.model.Sport sport, LocalDate bookingDate, LocalTime startTime, LocalTime endTime, String purpose, String status) {
        this.court = court;
        this.user = user;
        this.sport = sport;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = status;
    }

    // Keep old constructor for backward compat (sport = null)
    public Booking(Court court, User user, LocalDate bookingDate, LocalTime startTime, LocalTime endTime, String purpose, String status) {
        this(court, user, null, bookingDate, startTime, endTime, purpose, status);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Court getCourt() { return court; }
    public void setCourt(Court court) { this.court = court; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public com.example.resourcebooking.model.Sport getSport() { return sport; }
    public void setSport(com.example.resourcebooking.model.Sport sport) { this.sport = sport; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
