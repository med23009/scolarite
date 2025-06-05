package com.esp.scolarite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esp.scolarite.entity.BulletinSemestrielle;
import com.esp.scolarite.entity.Etudiant;

@Repository
public interface BulletinSemestrielleRepository extends JpaRepository<BulletinSemestrielle, Long> {

    List<BulletinSemestrielle> findByEtudiant(Etudiant etudiant);

    Optional<BulletinSemestrielle> findByEtudiantMatriculeAndSemestreIdSemestre(String matricule, Long idSemestre);
        void deleteByEtudiantMatriculeAndSemestreIdSemestre(String matricule, Long idSemestre);

}
