    package com.catconnect.entity;

    import jakarta.persistence.*;
    import lombok.*;

    import java.math.BigDecimal;

    @Entity
    @Table(name = "vets")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Vet {

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

        @Column(name = "is_emergency")
        private Boolean emergency;

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

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "vet_likes", joinColumns = @JoinColumn(name = "vet_id"))
        @Column(name = "user_id")
        private java.util.Set<Long> likedBy = new java.util.HashSet<>();

        @PrePersist
        void onCreate() {
            if (likes == null) {
                likes = 0;
            }
            if (emergency == null) {
                emergency = false;
            }
        }
    }
