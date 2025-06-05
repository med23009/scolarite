package com.esp.scolarite.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@Entity
public class ActionHistorique {
    
    /**
     * Types d'actions possibles dans le système
     */
    public enum TypeAction {
        LOGIN("Connexion"),
        LOGOUT("Déconnexion"),
        AJOUT("Ajout"),
        MODIFICATION("Modification"),
        SUPPRESSION("Suppression"),
        IMPORT("Importation"),
        EXPORT("Exportation"),
        AUTRE("Autre action");
        
        private final String libelle;
        
        TypeAction(String libelle) {
            this.libelle = libelle;
        }
        
        @JsonValue
        public String getLibelle() {
            return libelle;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String utilisateur; // Email ou nom
    
    @Enumerated(EnumType.STRING)
    private TypeAction type;    // Type d'action (LOGIN, AJOUT, etc.)
    
    private String action;      // Exemple : "Création étudiant"
    private String details;     // Informations sur ce qui a été modifié
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateAction;

    // Getters & Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUtilisateur() {
        return utilisateur;
    }
    
    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public LocalDateTime getDateAction() {
        return dateAction;
    }
    
    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }
    
    public TypeAction getType() {
        return type;
    }
    
    public void setType(TypeAction type) {
        this.type = type;
    }
}
