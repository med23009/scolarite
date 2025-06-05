package com.esp.scolarite.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Departement")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDepartement;

    @Column(unique = true, nullable = false)
    private String codeDep;

    @Column(nullable = false)
    private String intitule;

    private String description;

    private String nom_responsable;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idMembre")
    @JsonIgnore(false)
    private User responsable;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idResponsable")
    @JsonIgnore(false)
    private User responsableDepartement;

    @Getter
    @Setter
    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL)
    @JsonIgnore 
    private List<UniteEnseignement> UE_list = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "departement")
    @JsonIgnore
    private final List<Etudiant> etudiants = new ArrayList<>();

    // Constructor avec ID (pour JPA)
    public Departement() {
    }

    // Constructor sans ID (pour création)
    public Departement(String codeDep, String intitule, String description, String nom_responsable) {
        this.codeDep = codeDep;
        this.intitule = intitule;
        this.description = description;
        this.nom_responsable = nom_responsable;
    }

    // Getters et Setters
    public Long getIdDep() {
        return idDepartement;
    }

    public void setIdDep(Long idDep) {
        this.idDepartement = idDep;
    }

    public String getCodeDep() {
        return codeDep;
    }

    public void setCodeDep(String codeDep) {
        this.codeDep = codeDep;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
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

    
}