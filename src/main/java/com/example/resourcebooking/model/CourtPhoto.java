package com.example.resourcebooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "court_photos")
public class CourtPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Photo URL is required")
    @Column(nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    @JsonIgnore
    private Court court;

    public CourtPhoto() {}

    public CourtPhoto(String url, Court court) {
        this.url = url;
        this.court = court;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Court getCourt() { return court; }
    public void setCourt(Court court) { this.court = court; }
}
