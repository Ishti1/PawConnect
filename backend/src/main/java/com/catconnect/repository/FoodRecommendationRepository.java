package com.catconnect.repository;

import com.catconnect.entity.FoodRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRecommendationRepository extends JpaRepository<FoodRecommendation, Long> {
    List<FoodRecommendation> findByAgeGroup(String ageGroup);
}
