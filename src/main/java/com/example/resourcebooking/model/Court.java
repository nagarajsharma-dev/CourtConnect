package com.example.resourcebooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "courts")
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Venue back-reference
    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @NotBlank(message = "Court name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Location is required")
    @Column(nullable = false)
    private String location;

    @ElementCollection
    @CollectionTable(name = "court_amenities", joinColumns = @JoinColumn(name = "court_id"))
    @Column(name = "amenity")
    private List<String> amenities;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourtPhoto> photos;

    @ManyToMany
    @JoinTable(
        name = "court_sports",
        joinColumns = @JoinColumn(name = "court_id"),
        inverseJoinColumns = @JoinColumn(name = "sport_id")
    )
    private Set<Sport> sports;

    private Double pricePerHour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Court() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    
    public Long getVenueId() { return venue != null ? venue.getId() : null; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public List<CourtPhoto> getPhotos() { return photos; }
    public void setPhotos(List<CourtPhoto> photos) { this.photos = photos; }

    public Set<Sport> getSports() { return sports; }
    public void setSports(Set<Sport> sports) { this.sports = sports; }

    public Double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }

    public CourtStatus getStatus() { return status; }
    public void setStatus(CourtStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
