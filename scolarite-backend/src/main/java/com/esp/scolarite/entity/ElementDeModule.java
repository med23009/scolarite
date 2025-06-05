package com.esp.scolarite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "ElementDeModule")
public class ElementDeModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEM;

    private String codeEU;
    private String codeEM;
    private String intitule;
    private float  nombreCredits;
    private float coefficient;
    private int semestre;
    private int heuresCM;
    private int heuresTD;
    private int heuresTP;

    @JsonIgnore
    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "idUE")
    private UniteEnseignement uniteEnseignement;
    
    @ManyToOne
    @JoinColumn(name = "idSemestre")
    private Semestre id_semestre;

    @OneToOne
    @JoinColumn(name = "reponsableEM")
    private User responsableEM;

    public User getResponsableEM() {
        return responsableEM;
    }

    public void setResponsableEM(User responsableEM) {
        this.responsableEM = responsableEM;
    }

    public Long getIdEM() {
        return idEM;
    }

    public void setIdEM(Long idEM) {
        this.idEM = idEM;
    }

    public String getCodeEU() {
        return codeEU;
    }

    public void setCodeEU(String codeEU) {
        this.codeEU = codeEU;
    }

    public String getCodeEM() {
        return codeEM;
    }

    public void setCodeEM(String codeEM) {
        this.codeEM = codeEM;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public float getNombreCredits() {
        return nombreCredits;
    }

    public void setNombreCredits(float nombreCredits) {
        this.nombreCredits = nombreCredits;
    }

    public float getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(float coefficient) {
        this.coefficient = coefficient;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public int getHeuresCM() {
        return heuresCM;
    }

    public void setHeuresCM(int heuresCM) {
        this.heuresCM = heuresCM;
    }

    public int getHeuresTD() {
        return heuresTD;
    }

    public void setHeuresTD(int heuresTD) {
        this.heuresTD = heuresTD;
    }

    public int getHeuresTP() {
        return heuresTP;
    }

    public void setHeuresTP(int heuresTP) {
        this.heuresTP = heuresTP;
    }


    public UniteEnseignement getUniteEnseignement() {
        return uniteEnseignement;
    }

    public void setUniteEnseignement(UniteEnseignement uniteEnseignement) {
        this.uniteEnseignement = uniteEnseignement;
    }
    
    public Semestre getId_semestre() {
        return id_semestre;
    }

    public void setId_semestre(Semestre id_semestre) {
        this.id_semestre = id_semestre;
    }
}