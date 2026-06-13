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

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    private Integer likes;

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
    }
}
