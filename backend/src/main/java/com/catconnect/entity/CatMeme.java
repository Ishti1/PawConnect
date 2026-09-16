package com.catconnect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cat_memes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatMeme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String title;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    private Integer likes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meme_likes", joinColumns = @JoinColumn(name = "meme_id"))
    @Column(name = "user_id")
    private java.util.Set<Long> likedBy = new java.util.HashSet<>();

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
