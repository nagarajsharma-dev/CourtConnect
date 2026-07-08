package com.example.resourcebooking.controller;

import com.example.resourcebooking.model.Sport;
import com.example.resourcebooking.repository.SportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sports")
public class SportController {

    @Autowired
    private SportRepository sportRepository;

    @GetMapping
    public List<Sport> getAllSports() {
        return sportRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSport(@RequestBody Sport sport) {
        return ResponseEntity.ok(sportRepository.save(sport));
    }
}
