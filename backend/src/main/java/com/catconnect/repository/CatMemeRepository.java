package com.catconnect.repository;

import com.catconnect.entity.CatMeme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatMemeRepository extends JpaRepository<CatMeme, Long> {
}
