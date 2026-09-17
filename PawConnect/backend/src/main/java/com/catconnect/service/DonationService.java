package com.catconnect.service;

import com.catconnect.dto.DonationRequest;
import com.catconnect.entity.Donation;
import com.catconnect.entity.DonationCampaign;
import com.catconnect.repository.DonationCampaignRepository;
import com.catconnect.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationCampaignRepository campaignRepository;
    private final DonationRepository donationRepository;

    @Transactional
    public Donation contribute(Long campaignId, Long userId, DonationRequest request) {
        DonationCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        campaign.setRaisedAmount(campaign.getRaisedAmount().add(request.getAmount()));
        campaignRepository.save(campaign);
        return donationRepository.save(Donation.builder()
                .campaignId(campaignId)
                .userId(userId)
                .amount(request.getAmount())
                .message(request.getMessage())
                .build());
    }
}
