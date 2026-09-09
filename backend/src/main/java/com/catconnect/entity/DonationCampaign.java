package com.catconnect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "shelter_id")
    private Long shelterId;

    @Column(name = "goal_amount", nullable = false)
    private BigDecimal goalAmount;

    @Column(name = "raised_amount")
    private BigDecimal raisedAmount;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "mobile_banking")
    private String mobileBanking;

    @Column(name = "payment_instructions", columnDefinition = "TEXT")
    private String paymentInstructions;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Transient
    private String senderName;

    private String status;

    @Column(name = "user_id")
    private Long userId;

    private Integer likes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "campaign_likes", joinColumns = @JoinColumn(name = "campaign_id"))
    @Column(name = "user_id")
    private java.util.Set<Long> likedBy = new java.util.HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (raisedAmount == null) {
            raisedAmount = BigDecimal.ZERO;
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (likes == null) {
            likes = 0;
        }
    }
}
