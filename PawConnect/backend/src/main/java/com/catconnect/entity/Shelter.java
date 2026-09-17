package com.catconnect.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shelters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String phone;
    private String email;
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer capacity;
    private String city;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "map_link")
    private String mapLink;

    private Integer likes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shelter_likes", joinColumns = @JoinColumn(name = "shelter_id"))
    @Column(name = "user_id")
    private java.util.Set<Long> likedBy = new java.util.HashSet<>();

    @PrePersist
    void onCreate() {
        if (likes == null) {
            likes = 0;
        }
    }
}
