package com.esp.scolarite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esp.scolarite.entity.Semestre;

@Repository
public interface SemestreRepository extends JpaRepository<Semestre, Long> {
    List<Semestre> findByAnnee(int annee);
    List<Semestre> findBySemestre(String semestre);
}