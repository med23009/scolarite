package com.esp.scolarite.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.*;

@Data
@Entity
@Table(name = "BulletinSemestrielle")
public class BulletinSemestrielle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBulletin;
   
    private int totalCredits;
    private double moyenne;
    private String decision;

    @ManyToOne
    @JoinColumn(name = "idDossier")
    public DossierEtudiant dossierEtudiant;

    @ManyToOne
    @JoinColumn(name = "idSemestre")
    private Semestre semestre;
    
    
    @ManyToOne
    @JoinColumn(name = "idEtudiant")
    private Etudiant etudiant;
    @OneToMany(mappedBy = "bulletinSemestrielle", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<NoteSemestrielle> notes; // Relation avec NoteSemestrielle



    // Default constructor
    public BulletinSemestrielle() {
        this.notes = new ArrayList<>();
    }

    // Getters and setters
    public Long getIdBulletin() {
        return idBulletin;
    }

    public void setIdBulletin(Long idBulletin) {
        this.idBulletin = idBulletin;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public double getMoyenne() {
        return moyenne;
    }

    public void setMoyenne(double moyenne) {
        this.moyenne = moyenne;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public DossierEtudiant getDossierEtudiant() {
        return dossierEtudiant;
    }

    public void setDossierEtudiant(DossierEtudiant dossierEtudiant) {
        this.dossierEtudiant = dossierEtudiant;
    }

    public Semestre getSemestre() {
        return semestre;
    }

    public void setSemestre(Semestre semestre) {
        this.semestre = semestre;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public List<NoteSemestrielle> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteSemestrielle> notes) {
        this.notes = notes;
    }

    public void calculerMoyennes() {
        if (notes == null || notes.isEmpty()) {
            this.moyenne = 0.0;
            this.totalCredits = 0;
            this.decision = "NON VALIDÉ";
            return;
        }

        double totalPoints = 0.0;
        int credits = 0;

        for (NoteSemestrielle note : notes) {
            if (note.getElementModule() != null) {
                float moduleCredits = note.getElementModule().getNombreCredits();
                totalPoints += note.getNoteGenerale() * moduleCredits;
                credits += moduleCredits;
            }
        }

        this.totalCredits = credits;
        this.moyenne = credits > 0 ? totalPoints / credits : 0.0;
        this.decision = this.moyenne >= 10.0 ? "VALIDÉ" : "NON VALIDÉ";
    }
}