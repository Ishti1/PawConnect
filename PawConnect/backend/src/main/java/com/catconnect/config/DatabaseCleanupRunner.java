package com.catconnect.config;

import com.catconnect.entity.Vet;
import com.catconnect.repository.VetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanupRunner implements CommandLineRunner {

    private final VetRepository vetRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Running database cleanup task...");
        List<Vet> vets = vetRepository.findAll();
        for (Vet vet : vets) {
            if (vet.getName() != null && 
                (vet.getName().equalsIgnoreCase("pethub") || vet.getName().equalsIgnoreCase("vetspital"))) {
                log.info("Deleting vet: {}", vet.getName());
                vetRepository.delete(vet);
            }
        }
        log.info("Database cleanup task completed.");
    }
}
