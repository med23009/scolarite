package com.esp.scolarite.entity ;



import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "DossierEtudiant")
public class DossierEtudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDossier;
    private String matriculeEtudiant;
    private Integer semestreAccumules;
    private Integer creditsAccumules;
    private Integer anneeCourante;
    private Double moyenneGenerale;
    private String decision;
    private String status_parcours;

     @OneToOne
    @JoinColumn(name = "idEtudiant", referencedColumnName = "idEtudiant")
    @JsonIgnore // Ajoutez cette annotation
    private Etudiant etudiant;

    @OneToMany(mappedBy = "dossierEtudiant")
    @JsonIgnore // Ajoutez cette annotation
    private List<BulletinSemestrielle> bulletins;

    @ManyToOne
    @JoinColumn(name = "idSemestre", referencedColumnName = "idSemestre")
    @JsonIgnore // Ajoutez cette annotation
    private Semestre semestreCourant;
    public DossierEtudiant(Long idDossier, String matriculeEtudiant, Integer semestreAccumules,
            Integer creditsAccumules, Integer anneeCourante, Double moyenneGenerale,
            String decision, String status_parcours, Etudiant etudiant, List<BulletinSemestrielle> bulletins,
            Semestre semestre) {
        this.idDossier = idDossier;
        this.matriculeEtudiant = matriculeEtudiant;
        this.semestreAccumules = semestreAccumules;
        this.creditsAccumules = creditsAccumules;
        this.anneeCourante = anneeCourante;
        this.moyenneGenerale = moyenneGenerale;
        this.decision = decision;
        this.status_parcours = status_parcours;
        this.etudiant = etudiant;
        this.bulletins = bulletins;
        this.semestreCourant = semestre;
    }

    public Long getIdDossier() {
        return idDossier;
    }

    public void setIdDossier(Long idDossier) {
        this.idDossier = idDossier;
    }


    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public List<BulletinSemestrielle> getBulletins() {
        return bulletins;
    }

    public void setBulletins(List<BulletinSemestrielle> bulletins) {
        this.bulletins = bulletins;
    }

    public Semestre getSemestre() {
        return semestreCourant;
    }

    public void setSemestre(Semestre semestre) {
        this.semestreCourant = semestre;
    }

    public String getMatriculeEtudiant() {
        return matriculeEtudiant;
    }

    public void setMatriculeEtudiant(String matriculeEtudiant) {
        this.matriculeEtudiant = matriculeEtudiant;
    }

    public Integer getSemestreAccumules() {
        return semestreAccumules;
    }

    public void setSemestreAccumules(Integer semestreAccumules) {
        this.semestreAccumules = semestreAccumules;
    }

    public Integer getCreditsAccumules() {
        return creditsAccumules;
    }

    public void setCreditsAccumules(Integer creditsAccumules) {
        this.creditsAccumules = creditsAccumules;
    }




    public Integer getAnneeCourante() {
        return anneeCourante;
    }

    public void setAnneeCourante(Integer anneeCourante) {
        this.anneeCourante = anneeCourante;
    }

    public Double getMoyenneGenerale() {
        return moyenneGenerale;
    }

    public void setMoyenneGenerale(Double moyenneGenerale) {
        this.moyenneGenerale = moyenneGenerale;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getStatus_parcours() {
        return status_parcours;
    }

    public void setStatus_parcours(String status_parcours) {
        this.status_parcours = status_parcours;
    }



    // Getters et setters
}

