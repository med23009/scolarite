package com.esp.scolarite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.entity.UniteEnseignement;

@Repository
public interface UniteEnseignementRepository extends JpaRepository<UniteEnseignement, Long> {
    Optional<UniteEnseignement> findByCodeUE(String codeUE); // Ajoutez cette méthode

    // Trouver les UEs par l'email du responsable du pôle
    List<UniteEnseignement> findByPoleResponsableEmail(String email);

    List<UniteEnseignement> findByDepartementAndSemestre(Departement departement, Semestre semestre);

    List<UniteEnseignement> findByDepartementIdDepartement(Long idDepartement);

    List<UniteEnseignement> findByPoleIdPole(Long idPole);


    List<UniteEnseignement> findByDepartement(Departement departement);
}
