// src/main/java/com/esp/scolarite/dto/ElementDeModuleDTO.java
package com.esp.scolarite.dto;

public class ElementDeModuleDTO {
    private Long idEM;
    private String codeEM;
    private String codeEU;
    private String intitule;
    private float nombreCredits;
    private float coefficient;
    private Long idSemestre;
    private int semestre;
    private int heuresCM;
    private int heuresTD;
    private int heuresTP;
    private String responsableEM;
    private String ueIntitule;
    private String departement;
    private String pole;
    
    // Constructeurs, getters et setters
    public ElementDeModuleDTO() {}
    
    public ElementDeModuleDTO(Long idEM, String codeEM, String codeEU, String intitule, int nombreCredits,
                             float coefficient, int semestre, int heuresCM, int heuresTD, int heuresTP,
                             String responsableEM, String ueIntitule, String departement, String pole) {
        this.idEM = idEM;
        this.codeEM = codeEM;
        this.codeEU = codeEU;
        this.intitule = intitule;
        this.nombreCredits = nombreCredits;
        this.coefficient = coefficient;
        this.semestre = semestre;
        this.heuresCM = heuresCM;
        this.heuresTD = heuresTD;
        this.heuresTP = heuresTP;
        this.responsableEM = responsableEM;
        this.ueIntitule = ueIntitule;
        this.departement = departement;
        this.pole = pole;
    }
    
    // Getters et setters
    public Long getIdEM() { return idEM; }
    public void setIdEM(Long idEM) { this.idEM = idEM; }
    
    public String getCodeEM() { return codeEM; }
    public void setCodeEM(String codeEM) { this.codeEM = codeEM; }
    
    public String getCodeEU() { return codeEU; }
    public void setCodeEU(String codeEU) { this.codeEU = codeEU; }
    
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    
    public float getNombreCredits() { return nombreCredits; }
    public void setNombreCredits(float nombreCredits) { this.nombreCredits = nombreCredits; }
    
    public float getCoefficient() { return coefficient; }
    public void setCoefficient(float coefficient) { this.coefficient = coefficient; }

    public Long getIdSemestre() { return idSemestre; }
    public void setIdSemestre(Long idSemestre) { this.idSemestre = idSemestre;}
    
    public int getSemestre() { return semestre; }
    public void setSemestre(int semestre) { this.semestre = semestre; }
    
    public int getHeuresCM() { return heuresCM; }
    public void setHeuresCM(int heuresCM) { this.heuresCM = heuresCM; }
    
    public int getHeuresTD() { return heuresTD; }
    public void setHeuresTD(int heuresTD) { this.heuresTD = heuresTD; }
    
    public int getHeuresTP() { return heuresTP; }
    public void setHeuresTP(int heuresTP) { this.heuresTP = heuresTP; }
    
    public String getResponsableEM() { return responsableEM; }
    public void setResponsableEM(String responsableEM) { this.responsableEM = responsableEM; }
    
    public String getUeIntitule() { return ueIntitule; }
    public void setUeIntitule(String ueIntitule) { this.ueIntitule = ueIntitule; }
    
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    
    public String getPole() { return pole; }
    public void setPole(String pole) { this.pole = pole; }
}