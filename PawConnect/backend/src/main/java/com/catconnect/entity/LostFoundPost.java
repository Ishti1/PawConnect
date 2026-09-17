package com.catconnect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lost_found_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostFoundPost {

    public enum PostType { LOST, FOUND }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    // Transient means it is NOT in the database, but it IS sent in the JSON.
    @Transient
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostType postType;

    @Column(name = "cat_description", nullable = false, columnDefinition = "TEXT")
    private String catDescription;

    @Column(name = "last_seen_location")
    private String lastSeenLocation;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "map_link")
    private String mapLink;

    private Integer likes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lostfound_likes", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    private java.util.Set<Long> likedBy = new java.util.HashSet<>();

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (likes == null) {
            likes = 0;
        }
    }
}