package com.esp.scolarite.repository;

import com.esp.scolarite.entity.ActionHistorique;
import com.esp.scolarite.entity.ActionHistorique.TypeAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface ActionHistoriqueRepository extends JpaRepository<ActionHistorique, Long> {
    // Filtrage par utilisateur
    List<ActionHistorique> findByUtilisateur(String utilisateur);
    Page<ActionHistorique> findByUtilisateur(String utilisateur, Pageable pageable);
    
    // Filtrage par type d'action
    List<ActionHistorique> findByType(TypeAction type);
    Page<ActionHistorique> findByType(TypeAction type, Pageable pageable);
    
    // Filtrage par utilisateur et type d'action
    List<ActionHistorique> findByUtilisateurAndType(String utilisateur, TypeAction type);
    Page<ActionHistorique> findByUtilisateurAndType(String utilisateur, TypeAction type, Pageable pageable);
    
    // Filtrage par action
    List<ActionHistorique> findByActionContainingIgnoreCase(String action);
    Page<ActionHistorique> findByActionContainingIgnoreCase(String action, Pageable pageable);
    
    // Filtrage par période
    List<ActionHistorique> findByDateActionBetween(LocalDateTime debut, LocalDateTime fin);
    Page<ActionHistorique> findByDateActionBetween(LocalDateTime debut, LocalDateTime fin, Pageable pageable);
    
    // Recherche avancée combinant plusieurs critères
    @Query("SELECT a FROM ActionHistorique a WHERE " +
           "(:utilisateur IS NULL OR a.utilisateur = :utilisateur) AND " +
           "(:type IS NULL OR a.type = :type) AND " +
           "(:action IS NULL OR LOWER(a.action) LIKE LOWER(CONCAT('%', :action, '%'))) AND " +
           "(:debut IS NULL OR a.dateAction >= :debut) AND " +
           "(:fin IS NULL OR a.dateAction <= :fin)")
    Page<ActionHistorique> rechercheAvancee(
            @Param("utilisateur") String utilisateur,
            @Param("type") TypeAction type,
            @Param("action") String action,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            Pageable pageable);
}
