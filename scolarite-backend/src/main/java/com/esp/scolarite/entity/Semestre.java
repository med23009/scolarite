package com.esp.scolarite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "semestre")
public class Semestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSemestre;

    private int nombreSemaines;
    private Date dateDebut;
    private Date dateFin;
    private int annee;
    private String semestre;

    private int creditSpecialite;
    private int creditHE;
    private int creditST;

    @OneToMany(mappedBy = "semestre")
    @JsonIgnore
    private List<NoteSemestrielle> notes;

    @OneToMany(mappedBy = "semestreCourant")
    @JsonIgnore
    private List<DossierEtudiant> dossiersEtudiants;

    @OneToMany(mappedBy = "semestre")
    @JsonIgnore
    private List<ElementDeModule> elementsModule;

    // Getters & Setters

    public Long getIdSemestre() {
        return idSemestre;
    }

    public void setIdSemestre(Long idSemestre) {
        this.idSemestre = idSemestre;
    }

    public int getNombreSemaines() {
        return nombreSemaines;
    }

    public void setNombreSemaines(int nombreSemaines) {
        this.nombreSemaines = nombreSemaines;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public int getCreditSpecialite() {
        return creditSpecialite;
    }

    public void setCreditSpecialite(int creditSpecialite) {
        this.creditSpecialite = creditSpecialite;
    }

    public int getCreditHE() {
        return creditHE;
    }

    public void setCreditHE(int creditHE) {
        this.creditHE = creditHE;
    }

    public int getCreditST() {
        return creditST;
    }

    public void setCreditST(int creditST) {
        this.creditST = creditST;
    }

    public List<NoteSemestrielle> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteSemestrielle> notes) {
        this.notes = notes;
    }

    public List<DossierEtudiant> getDossiersEtudiants() {
        return dossiersEtudiants;
    }

    public void setDossiersEtudiants(List<DossierEtudiant> dossiersEtudiants) {
        this.dossiersEtudiants = dossiersEtudiants;
    }

    public List<ElementDeModule> getElementsModule() {
        return elementsModule;
    }

    public void setElementsModule(List<ElementDeModule> elementsModule) {
        this.elementsModule = elementsModule;
    }

}
