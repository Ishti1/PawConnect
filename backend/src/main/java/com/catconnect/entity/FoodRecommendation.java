package com.catconnect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "food_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "age_group", nullable = false)
    private String ageGroup;

    @Column(name = "health_condition")
    private String healthCondition;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal rating;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "image_url")
    private String imageUrl;

    private Integer likes;

    @PrePersist
    void onCreate() {
        if (likes == null) {
            likes = 0;
        }
    }
}
