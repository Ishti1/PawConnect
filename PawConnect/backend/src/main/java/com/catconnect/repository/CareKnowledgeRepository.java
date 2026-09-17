package com.catconnect.repository;

import com.catconnect.entity.CareKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareKnowledgeRepository extends JpaRepository<CareKnowledge, Long> {
    List<CareKnowledge> findByCategory(String category);
}
