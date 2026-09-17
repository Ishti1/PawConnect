package com.catconnect.repository;

import com.catconnect.entity.CatMoment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatMomentRepository extends JpaRepository<CatMoment, Long> {
    List<CatMoment> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<CatMoment> findAllByOrderByCreatedAtDesc();
}
