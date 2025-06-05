package com.esp.scolarite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.Etudiant;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    List<Etudiant> findByMatriculeContainingOrNomContainingOrPrenomContainingOrEmailContaining(String searchTerm,
            String searchTerm2, String searchTerm3, String searchTerm4);
    
    Optional<Etudiant> findByMatricule(String matricule);
    
    @Query("SELECT e FROM Etudiant e WHERE e.departement = :departement")
    List<Etudiant> findByDepartement(@Param("departement") Departement departement);
@Query("SELECT e FROM Etudiant e WHERE e.departement.idDepartement = :idDepartement AND e.promotion = :promotion")
List<Etudiant> findEtudiantsByDepartementAndPromotion(@Param("idDepartement") Long idDepartement, @Param("promotion") String promotion);




}