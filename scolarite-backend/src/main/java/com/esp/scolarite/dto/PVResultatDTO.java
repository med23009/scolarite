package com.esp.scolarite.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * DTO pour les données du Procès-Verbal des résultats des étudiants
 */
@Data
public class PVResultatDTO {
    // Informations générales du PV
    private String departement;
    private int semestre;
    private int annee;
    
    // Liste des modules regroupés par pôle
    private List<ModuleHeaderDTO> modulesDepartement;
    private List<ModuleHeaderDTO> modulesST;
    private List<ModuleHeaderDTO> modulesHE;
    
    // Résultats des étudiants
    private List<EtudiantResultatDTO> resultatsEtudiants;
    
    /**
     * DTO pour les en-têtes des modules (colonnes)
     */
    @Data
    public static class ModuleHeaderDTO {
        private String codeUE;   // Code de l'Unité d'Enseignement
        private String intituleUE; // Intitulé de l'UE
        private String codeEM;   // Code de l'Élément de Module
        private String intituleEM; // Intitulé de l'EM
        private int credits;     // Nombre de crédits
        private float coefficient; // Coefficient
    }
    
    /**
     * DTO pour les résultats d'un étudiant
     */
    @Data
    public static class EtudiantResultatDTO {
        private Long idEtudiant;
        private String matricule;
        private String nom;
        private String prenom;
        private Map<String, NoteModuleDTO> notes; // Clé: codeEM, Valeur: note
        private double moyenneGenerale;
        private String decision; // ADMIS, AJOURNÉ, etc.
    }
    
    /**
     * DTO pour la note d'un module
     */
    @Data
    public static class NoteModuleDTO {
        private double valeur;
        private String validation; // V (Validé), NV (Non Validé), VCE, VCI, etc.
        private double moyenneUE; // Moyenne de l'UE à laquelle appartient ce module
    }
}