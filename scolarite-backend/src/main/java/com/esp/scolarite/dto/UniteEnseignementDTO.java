// src/main/java/com/esp/scolarite/dto/UniteEnseignementDTO.java
package com.esp.scolarite.dto;

public class UniteEnseignementDTO {
    private Long idUE;
    private String codeUE;
    private String intitule;
    private int nbEM;
    private int annee;
    private int semestre;
    private String departementCode;
    private String departementNom;
    private String poleCode;
    private String poleNom;
    
    // Constructeurs, getters et setters
    public UniteEnseignementDTO() {}
    
    public UniteEnseignementDTO(Long idUE, String codeUE, String intitule, int nbEM, int annee, int semestre, 
                               String departementCode, String departementNom, String poleCode, String poleNom) {
        this.idUE = idUE;
        this.codeUE = codeUE;
        this.intitule = intitule;
        this.nbEM = nbEM;
        this.annee = annee;
        this.semestre = semestre;
        this.departementCode = departementCode;
        this.departementNom = departementNom;
        this.poleCode = poleCode;
        this.poleNom = poleNom;
    }
    
    // Getters et setters
    public Long getIdUE() { return idUE; }
    public void setIdUE(Long idUE) { this.idUE = idUE; }
    
    public String getCodeUE() { return codeUE; }
    public void setCodeUE(String codeUE) { this.codeUE = codeUE; }
    
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    
    public int getNbEM() { return nbEM; }
    public void setNbEM(int nbEM) { this.nbEM = nbEM; }
    
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    
    public int getSemestre() { return semestre; }
    public void setSemestre(int semestre) { this.semestre = semestre; }
    
    public String getDepartementCode() { return departementCode; }
    public void setDepartementCode(String departementCode) { this.departementCode = departementCode; }
    
    public String getDepartementNom() { return departementNom; }
    public void setDepartementNom(String departementNom) { this.departementNom = departementNom; }
    
    public String getPoleCode() { return poleCode; }
    public void setPoleCode(String poleCode) { this.poleCode = poleCode; }
    
    public String getPoleNom() { return poleNom; }
    public void setPoleNom(String poleNom) { this.poleNom = poleNom; }
}