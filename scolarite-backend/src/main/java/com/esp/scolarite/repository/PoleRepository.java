package com.esp.scolarite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.esp.scolarite.entity.Pole;

@Repository
public interface PoleRepository extends JpaRepository<Pole, Long> {
    // Find poles by responsable email
    List<Pole> findByResponsableEmail(String email);
    
    // Find pole by code
    Optional<Pole> findByCodePole(String codePole);
}
