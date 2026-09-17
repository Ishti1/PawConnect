package com.catconnect.repository;

import com.catconnect.entity.LostFoundPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LostFoundPostRepository extends JpaRepository<LostFoundPost, Long> {
    List<LostFoundPost> findByStatus(String status);
}
