package com.catconnect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "adoption_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "cat_name", nullable = false)
    private String catName;

    private String breed;

    @Column(name = "age_months")
    private Integer ageMonths;

    private String gender;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    private Integer likes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "adoption_likes", joinColumns = @JoinColumn(name = "adoption_id"))
    @Column(name = "user_id")
    private java.util.Set<Long> likedBy = new java.util.HashSet<>();

    @Column(name = "shelter_id")
    private Long shelterId;

    private String location;
    
    private String address;
    
    @Column(name = "contact_phone")
    private String contactPhone;

    @Transient
    private String senderName;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "AVAILABLE";
        }
        if (likes == null) {
            likes = 0;
        }
    }
}
