package com.esp.scolarite.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esp.scolarite.entity.DossierEtudiant;

@Repository
public interface DossierEtudiantRepository extends JpaRepository<DossierEtudiant, Long> {
   
}