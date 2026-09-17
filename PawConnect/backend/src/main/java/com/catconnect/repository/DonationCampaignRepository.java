package com.catconnect.repository;

import com.catconnect.entity.DonationCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationCampaignRepository extends JpaRepository<DonationCampaign, Long> {
    List<DonationCampaign> findByStatus(String status);
}
