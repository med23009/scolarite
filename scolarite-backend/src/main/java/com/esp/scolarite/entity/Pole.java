package com.esp.scolarite.entity;

import jakarta.persistence.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "Pole")
public class Pole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPole;
    private String intitule;
    private String codePole;
    private String description;
    private String nom_responsable;
        
    @OneToOne
    @JoinColumn(name = "id_membre")
    private User responsable;

    @OneToMany(mappedBy = "pole")
    @JsonIgnore
    private List<UniteEnseignement> uniteEnseignements;

    public Long getIdPole() {
        return idPole;
    }

    public void setIdPole(Long idPole) {
        this.idPole = idPole;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public String getCodePole() {
        return codePole;
    }

    public void setCodePole(String codePole) {
        this.codePole = codePole;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNom_responsable() {
        return nom_responsable;
    }

    public void setNom_responsable(String nom_responsable) {
        this.nom_responsable = nom_responsable;
    }

    public User getResponsable() {
        return responsable;
    }

    public void setResponsable(User responsable) {
        this.responsable = responsable;
    }

    public List<UniteEnseignement> getUniteEnseignement() {
    return uniteEnseignements;
}

public void setUniteEnseignement(List<UniteEnseignement> uniteEnseignement) {
    this.uniteEnseignements = uniteEnseignement;
}

    
}