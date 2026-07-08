package com.example.resourcebooking.service;

import com.example.resourcebooking.model.Court;
import com.example.resourcebooking.model.CourtStatus;
import com.example.resourcebooking.repository.CourtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourtService {

    @Autowired
    private CourtRepository courtRepository;

    public List<Court> getAllCourts() {
        return courtRepository.findAll();
    }
    
    public List<Court> getCourtsByVenueId(Long venueId) {
        return courtRepository.findByVenue_Id(venueId);
    }

    public List<Court> getAvailableCourts() {
        return courtRepository.findByStatus(CourtStatus.AVAILABLE);
    }

    public Court getCourtById(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Court not found"));
    }

    public Court addCourt(Court court) {
        return courtRepository.save(court);
    }

    public Court updateCourtStatus(Long id, CourtStatus status) {
        Court court = getCourtById(id);
        court.setStatus(status);
        return courtRepository.save(court);
    }

    public void deleteCourt(Long id) {
        courtRepository.deleteById(id);
    }
}
