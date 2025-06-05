package com.esp.scolarite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.esp.scolarite.entity.Etudiant;
import com.esp.scolarite.entity.NoteSemestrielle;
import com.esp.scolarite.entity.Semestre;

@Repository
public interface NoteSemestrielleRepository extends JpaRepository<NoteSemestrielle, Long> {
    // Correction: recherche par l'objet Etudiant avec matricule
    @Query("SELECT n FROM NoteSemestrielle n WHERE n.etudiant.matricule = :matricule")
    List<NoteSemestrielle> findByMatriculeEtudiant(@Param("matricule") String matricule);
    
    // Correction: recherche par code d'élément module
    @Query("SELECT n FROM NoteSemestrielle n WHERE n.elementModule.codeEM = :codeEM")
    List<NoteSemestrielle> findByCodeEM(@Param("codeEM") String codeEM);
    
    List<NoteSemestrielle> findByEtudiant(Etudiant etudiant);

    // Correction: utilisez l'objet Semestre plutôt qu'un entier
    @Query("SELECT n FROM NoteSemestrielle n WHERE n.semestre.idSemestre = :semestreId AND n.etudiant.idEtudiant IN :studentIds")
    List<NoteSemestrielle> findBySemestreAndEtudiantIn(@Param("semestreId") Long semestreId, @Param("studentIds") List<Long> studentIds);

    NoteSemestrielle[] findByEtudiantAndSemestre(Etudiant etu, Semestre semestre);
    Optional<NoteSemestrielle> findByEtudiantIdEtudiantAndElementModuleIdEMAndSemestreIdSemestre(
        Long etudiantId, Long elementId, Long semestreId
    );
        List<NoteSemestrielle> findAllByEtudiantAndSemestre(Etudiant etudiant, Semestre semestre);

}