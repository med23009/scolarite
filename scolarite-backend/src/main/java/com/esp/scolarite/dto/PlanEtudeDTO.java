package com.esp.scolarite.dto;

import com.esp.scolarite.entity.ElementDeModule;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * DTO pour représenter le plan d'étude d'un étudiant
 */
public class PlanEtudeDTO {
    private String matricule;
    private String nomEtudiant;
    private String prenomEtudiant;
    private String semestre;
    private int totalCredits;
    private String departementCode;
    private String departementNom;
    private List<ElementDeModule> modulesNonValides;
    private Map<String, List<ElementDeModule>> modulesParUE;
    private boolean semestreBloque;
    private String messageBloquage;
    // Map pour stocker les notes des modules (clé: codeEM, valeur: note générale)
    private Map<String, Double> notesModules;
    // Map pour stocker le statut de validation des modules (clé: codeEM, valeur: statut de validation)
    private Map<String, String> validationStatuts;

    public PlanEtudeDTO() {
        this.semestreBloque = false; // Par défaut, le semestre n'est pas bloqué
        this.notesModules = new HashMap<>(); // Initialiser la map des notes
        this.validationStatuts = new HashMap<>(); // Initialiser la map des statuts de validation
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNomEtudiant() {
        return nomEtudiant;
    }

    public void setNomEtudiant(String nomEtudiant) {
        this.nomEtudiant = nomEtudiant;
    }

    public String getPrenomEtudiant() {
        return prenomEtudiant;
    }

    public void setPrenomEtudiant(String prenomEtudiant) {
        this.prenomEtudiant = prenomEtudiant;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public String getDepartementCode() {
        return departementCode;
    }

    public void setDepartementCode(String departementCode) {
        this.departementCode = departementCode;
    }

    public String getDepartementNom() {
        return departementNom;
    }

    public void setDepartementNom(String departementNom) {
        this.departementNom = departementNom;
    }

    public List<ElementDeModule> getModulesNonValides() {
        return modulesNonValides;
    }

    public void setModulesNonValides(List<ElementDeModule> modulesNonValides) {
        this.modulesNonValides = modulesNonValides;
    }

    public Map<String, List<ElementDeModule>> getModulesParUE() {
        return modulesParUE;
    }

    public void setModulesParUE(Map<String, List<ElementDeModule>> modulesParUE) {
        this.modulesParUE = modulesParUE;
    }

    public boolean isSemestreBloque() {
        return semestreBloque;
    }

    public void setSemestreBloque(boolean semestreBloque) {
        this.semestreBloque = semestreBloque;
    }

    public String getMessageBloquage() {
        return messageBloquage;
    }

    public void setMessageBloquage(String messageBloquage) {
        this.messageBloquage = messageBloquage;
    }
    
    public Map<String, Double> getNotesModules() {
        return notesModules;
    }
    
    public void setNotesModules(Map<String, Double> notesModules) {
        this.notesModules = notesModules;
    }
    
    public Map<String, String> getValidationStatuts() {
        return validationStatuts;
    }

    public void setValidationStatuts(Map<String, String> validationStatuts) {
        this.validationStatuts = validationStatuts;
    }
    
    // Méthode utilitaire pour ajouter une note à un module
    public void addNoteModule(String codeEM, Double note) {
        if (this.notesModules == null) {
            this.notesModules = new HashMap<>();
        }
        this.notesModules.put(codeEM, note);
    }
    
    // Méthode utilitaire pour ajouter un statut de validation à un module
    public void addValidationStatut(String codeEM, String statut) {
        if (this.validationStatuts == null) {
            this.validationStatuts = new HashMap<>();
        }
        this.validationStatuts.put(codeEM, statut);
    }
} 