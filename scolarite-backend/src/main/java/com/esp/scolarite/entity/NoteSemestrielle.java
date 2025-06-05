package com.esp.scolarite.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "NoteSemestrielle")
public class NoteSemestrielle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNote;

    private double noteRattrapage;
    private double noteDevoir;
    private double noteExamen;
    
    private int annee;
    
    // Numéro du semestre (1 ou 2)
    private int semestreNumero;
    
    private double compensationInterne;
    private double compensationExterne;
    
    @ManyToOne
    @JoinColumn(name = "idBulletin")
    @JsonBackReference
    private BulletinSemestrielle bulletinSemestrielle;

    @ManyToOne
    @JoinColumn(name = "idElementModule")
    private ElementDeModule elementModule;

    
    @ManyToOne
    @JoinColumn(name = "semestre_id")
    private Semestre semestre;


    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    public Etudiant etudiant;

    public NoteSemestrielle() {
    }

    // Getters and setters
    public Long getIdNote() {
        return idNote;
    }

    public void setIdNote(Long idNote) {
        this.idNote = idNote;
    }

    public double getNoteRattrapage() {
        return noteRattrapage;
    }

    public void setNoteRattrapage(double noteRattrapage) {
        this.noteRattrapage = noteRattrapage;
    }

    public double getNoteDevoir() {
        return noteDevoir;
    }

    public void setNoteDevoir(double noteDevoir) {
        this.noteDevoir = noteDevoir;
    }

    public double getNoteExamen() {
        return noteExamen;
    }

    public void setNoteExamen(double noteExamen) {
        this.noteExamen = noteExamen;
    }

    

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }
    
    public int getSemestreNumero() {
        return semestreNumero;
    }

    public void setSemestreNumero(int semestreNumero) {
        this.semestreNumero = semestreNumero;
    }
    

    public double getCompensationInterne() {
        return compensationInterne;
    }

    public void setCompensationInterne(double compensationInterne) {
        this.compensationInterne = compensationInterne;
    }

    public double getCompensationExterne() {
        return compensationExterne;
    }

    public void setCompensationExterne(double compensationExterne) {
        this.compensationExterne = compensationExterne;
    }

    public ElementDeModule getElementModule() {
        return elementModule;
    }

    public void setElementModule(ElementDeModule elementModule) {
        this.elementModule = elementModule;
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
    
    public BulletinSemestrielle getBulletinSemestrielle() {
        return bulletinSemestrielle;
    }

    public void setBulletinSemestrielle(BulletinSemestrielle bulletinSemestrielle) {
        this.bulletinSemestrielle = bulletinSemestrielle;
    }
    
    public double getNoteGenerale() {
        // Si une note de rattrapage existe, elle remplace la note d'examen
        double noteExamenFinal = (noteRattrapage > 0) ? noteRattrapage : noteExamen;
    
        // Calcul de la note générale : 40% devoir + 60% examen (ou rattrapage)
        return (0.4 * noteDevoir) + (0.6 * noteExamenFinal);
    }
    
    public float getCredit() {
        // Retourne le crédit associé à l'élément de module
        return (elementModule != null) ? elementModule.getNombreCredits() : 0;
    }
}