package com.catconnect.repository;

import com.catconnect.entity.Vet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VetRepository extends JpaRepository<Vet, Long> {
    List<Vet> findByEmergencyTrue();
    List<Vet> findByCityContainingIgnoreCase(String city);
    List<Vet> findByEmergencyTrueAndCityContainingIgnoreCase(String city);
}
