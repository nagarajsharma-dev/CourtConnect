package com.example.resourcebooking.model;

import java.time.LocalTime;

public class SlotAvailabilityDTO {
    private LocalTime startTime;
    private LocalTime endTime;
    private int availableCourts;

    public SlotAvailabilityDTO(LocalTime startTime, LocalTime endTime, int availableCourts) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.availableCourts = availableCourts;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getAvailableCourts() {
        return availableCourts;
    }

    public void setAvailableCourts(int availableCourts) {
        this.availableCourts = availableCourts;
    }
}
