package com.esp.scolarite.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "UniteEnseignement")
public class UniteEnseignement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUE;

    private String codeUE;
    private String intitule;
    private int nbEM;
    private int annee;
    @SuppressWarnings("unused")
    private int semestre;
    
    // Champ semestre_num avec valeur par défaut 0
    @Column(name = "semestre_num")
    private Integer semestreNum = 0;

    @JsonBackReference
    @OneToMany(mappedBy = "uniteEnseignement")
    private List<ElementDeModule> elementsDeModule;

    @ManyToOne
    @JoinColumn(name = "semestre_id")
    @JsonIgnore // Ajoutez cette annotation
    private Semestre semestree;

    @ManyToOne
    @JoinColumn(name = "idDepartement" , nullable = true)
    @JsonIgnore // Ajoutez cette annotation
    private Departement departement;

    @ManyToOne
    @JoinColumn(name = "idPole", nullable = true)
    @JsonIgnore
    private Pole pole;


    public UniteEnseignement() {
        this.semestreNum = 0; // Définir une valeur par défaut
    }

    public Long getIdUE() {
        return idUE;
    }

    public void setIdUE(Long idUE) {
        this.idUE = idUE;
    }

    public String getCodeUE() {
        return codeUE;
    }

    public void setCodeUE(String codeUE) {
        this.codeUE = codeUE;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public int getNbEM() {
        return nbEM;
    }

    public void setNbEM(int nbEM) {
        this.nbEM = nbEM;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    
    
    public void setSemestre(int semestre) {
        this.semestre = semestre;
        this.semestreNum = semestre; // Mettre à jour aussi semestreNum
    }

    public Integer getSemestreNum() {
        return semestreNum;
    }

    public void setSemestreNum(Integer semestreNum) {
        this.semestreNum = semestreNum;
    }

    public List<ElementDeModule> getElementsDeModule() {
        return elementsDeModule;
    }

    public void setElementsDeModule(List<ElementDeModule> elementsDeModule) {
        this.elementsDeModule = elementsDeModule;
    }

    public Semestre getSemestre() {
        return semestree;
    }

    public void setSemestre(Semestre semestre) {
        this.semestree = semestre;
    }
    
    public Departement getDepartement() {
        return departement;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    public Pole getPole() {
        return pole;
    }

    public void setPole(Pole pole) {
        this.pole = pole;
    }
}