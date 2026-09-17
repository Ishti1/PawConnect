package com.catconnect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cat_moments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatMoment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String caption;

    private String imageUrl;

    @Column(name = "media_type")
    private String mediaType;

    private Integer likes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "moment_likes", joinColumns = @JoinColumn(name = "moment_id"))
    @Column(name = "user_id")
    private java.util.Set<Long> likedBy = new java.util.HashSet<>();

    @Column(name = "shared_moment_id")
    private Long sharedMomentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (likes == null) {
            likes = 0;
        }
        if (mediaType == null) {
            mediaType = "IMAGE";
        }
    }
}
