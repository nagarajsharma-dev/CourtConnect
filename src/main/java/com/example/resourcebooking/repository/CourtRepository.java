package com.example.resourcebooking.repository;

import com.example.resourcebooking.model.Court;
import com.example.resourcebooking.model.CourtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByStatus(CourtStatus status);
    List<Court> findByVenue_Id(Long venueId);
}
