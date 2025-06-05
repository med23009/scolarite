package com.esp.scolarite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.ElementDeModule;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.entity.UniteEnseignement;

@Repository
public interface ElementDeModuleRepository extends JpaRepository<ElementDeModule, Long> {
    List<ElementDeModule> findByIntitule(String intitule);

    Optional<ElementDeModule> findByCodeEM(String codeEM);

    // Méthode pour trouver les éléments de module par numéro de semestre
    @Query("SELECT e FROM ElementDeModule e WHERE e.semestre = :semestreNum")
    List<ElementDeModule> findBySemestre(@Param("semestreNum") int semestreNum);

    List<ElementDeModule> findByUniteEnseignement(UniteEnseignement ue);

    // Pour filtrer les EM par département (via UE)
    List<ElementDeModule> findByUniteEnseignement_Departement_IdDepartement(Long idDepartement);

    // Pour filtrer les EM par pôle (via UE)
    List<ElementDeModule> findByUniteEnseignement_Pole_IdPole(Long idPole);
    List<ElementDeModule> findByUniteEnseignement_IdUE(Long idUE);



    List<ElementDeModule> findByUniteEnseignement_IdUEIn(List<Long> deptUEIds);
   @Query("SELECT em FROM ElementDeModule em " +
       "JOIN em.uniteEnseignement ue " +
       "WHERE em.id_semestre = :semestre AND ue.departement = :departement")
List<ElementDeModule> findBySemestreAndDepartement(@Param("semestre") Semestre semestre, @Param("departement") Departement departement);

@Query("SELECT em FROM ElementDeModule em " +
       "JOIN em.uniteEnseignement ue " +
       "WHERE em.id_semestre = :semestre AND ue.pole IS NOT NULL")
List<ElementDeModule> findBySemestreAndPoleCommun(@Param("semestre") Semestre semestre);


}