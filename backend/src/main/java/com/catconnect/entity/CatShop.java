package com.catconnect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cat_shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String phone;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal rating;

    @Column(name = "open_hours")
    private String openHours;

    private String city;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "map_link")
    private String mapLink;

    private Integer likes;

    @PrePersist
    void onCreate() {
        if (likes == null) {
            likes = 0;
        }
    }
}