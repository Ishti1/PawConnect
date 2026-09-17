package com.catconnect.repository;

import com.catconnect.entity.AdoptionListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdoptionListingRepository extends JpaRepository<AdoptionListing, Long> {
    List<AdoptionListing> findByStatus(String status);
}
