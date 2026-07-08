package com.example.resourcebooking.controller;

import com.example.resourcebooking.model.*;
import com.example.resourcebooking.repository.SportRepository;
import com.example.resourcebooking.repository.UserRepository;
import com.example.resourcebooking.service.CourtService;
import com.example.resourcebooking.service.FileStorageService;
import com.example.resourcebooking.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/courts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourtController {

    @Autowired
    private CourtService courtService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueService venueService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCourt(
            @RequestParam("name") String name,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "amenities", required = false) List<String> amenities,
            @RequestParam(value = "sportIds", required = false) List<Long> sportIds,
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            @RequestParam(value = "pricePerHour", required = false) Double pricePerHour,
            Authentication authentication) {

        Court court = new Court();
        court.setName(name);
        court.setStatus(CourtStatus.AVAILABLE);
        court.setPricePerHour(pricePerHour != null ? pricePerHour : 0.0);

        // Link to venue if provided
        if (venueId != null) {
            Venue venue = venueService.getVenueById(venueId);
            court.setVenue(venue);
            court.setLocation(location != null ? location : venue.getAddress());
        } else {
            court.setLocation(location != null ? location : "");
        }

        if (amenities != null) court.setAmenities(amenities);

        if (sportIds != null && !sportIds.isEmpty()) {
            Set<Sport> sports = new HashSet<>();
            for (Long sportId : sportIds) {
                sportRepository.findById(sportId).ifPresent(sports::add);
            }
            court.setSports(sports);
        }

        if (photos != null && photos.length > 0) {
            List<CourtPhoto> courtPhotos = new ArrayList<>();
            for (MultipartFile photo : photos) {
                if (!photo.isEmpty()) {
                    String photoUrl = fileStorageService.storeFile(photo);
                    courtPhotos.add(new CourtPhoto(photoUrl, court));
                }
            }
            court.setPhotos(courtPhotos);
        }

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(court::setCreatedBy);
        }

        Court savedCourt = courtService.addCourt(court);
        return ResponseEntity.ok(savedCourt);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourt(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "status", required = false) CourtStatus status) {

        Court court = courtService.getCourtById(id);
        if (name != null) court.setName(name);
        if (location != null) court.setLocation(location);
        if (status != null) court.setStatus(status);

        return ResponseEntity.ok(courtService.addCourt(court));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.ok("Court deleted successfully");
    }
}
