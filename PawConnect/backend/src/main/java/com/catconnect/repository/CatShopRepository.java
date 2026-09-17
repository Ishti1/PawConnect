package com.catconnect.repository;

import com.catconnect.entity.CatShop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatShopRepository extends JpaRepository<CatShop, Long> {
}