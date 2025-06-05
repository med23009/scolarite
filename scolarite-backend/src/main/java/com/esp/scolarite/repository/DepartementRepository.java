package com.esp.scolarite.repository;
import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DepartementRepository extends JpaRepository<Departement, Long> {
    List<Departement> findByIntituleContaining(String keyword);
    
    // Méthode pour trouver un département par son code (complet ou partiel)
    List<Departement> findByCodeDepContaining(String codeDep);

    // Dans DepartementRepository.java
    Optional<Departement> findByCodeDep(String codeDep);
    
    // Méthode pour trouver exactement un département par son intitulé
    Optional<Departement> findByIntitule(String intitule);
    
    // Méthode pour trouver les départements par l'email du responsable
    List<Departement> findByResponsableEmail(String email);

    Departement findByResponsable(User user);
}