package com.esp.scolarite.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

@Entity 
@Table(name = "Etudiant")
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEtudiant;

    private int anneeObtentionBac;
    private String nom;
    private String prenom;
    private String prenomAR;
    private String nomAR;
    @Column(name = "matricule", unique = true, updatable = false)
    private String matricule;
    private Date dateNaissance;
    private String lieuNaissance;
    private String lieuNaissanceAR;
    private String sexe;
    @JsonProperty("NNI")
    private String NNI;
    private String email;
    private String photo;
    private Date dateInscription;
    private String telephoneEtudiant;
    private String telephoneCorrespondant;
    private String adresseResidence;
    private String promotion;
    @OneToOne(mappedBy = "etudiant")
    @JsonIgnore
    private DossierEtudiant dossier;

    @OneToMany(mappedBy = "etudiant")
    @JsonIgnore
    private List<BulletinSemestrielle> bulletins;

   @ManyToOne
   @JsonIgnoreProperties({"etudiants"})
   @JoinColumn(name = "departement_id_departement") // 💡 mettre le vrai nom de la colonne SQL
   private Departement departement;

    // Getters et Setters
    public Departement getDepartement() {
        return departement;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }  // Getters and setters
    public Long getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(Long idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public int getAnneeObtentionBac() {
        return anneeObtentionBac;
    }

    public void setAnneeObtentionBac(int anneeObtentionBac) {
        this.anneeObtentionBac = anneeObtentionBac;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPrenomAR() {
        return prenomAR;
    }

    public void setPrenomAR(String prenomAR) {
        this.prenomAR = prenomAR;
    }

    public String getNomAR() {
        return nomAR;
    }

    public void setNomAR(String nomAR) {
        this.nomAR = nomAR;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getLieuNaissanceAR() {
        return lieuNaissanceAR;
    }

    public void setLieuNaissanceAR(String lieuNaissanceAR) {
        this.lieuNaissanceAR = lieuNaissanceAR;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getNNI() {
        return NNI;
    }

    public void setNNI(String NNI) {
        this.NNI = NNI;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getTelephoneEtudiant() {
        return telephoneEtudiant;
    }

    public void setTelephoneEtudiant(String telephoneEtudiant) {
        this.telephoneEtudiant = telephoneEtudiant;
    }

    public String getTelephoneCorrespondant() {
        return telephoneCorrespondant;
    }

    public void setTelephoneCorrespondant(String telephoneCorrespondant) {
        this.telephoneCorrespondant = telephoneCorrespondant;
    }

    public String getAdresseResidence() {
        return adresseResidence;
    }

    public void setAdresseResidence(String adresseResidence) {
        this.adresseResidence = adresseResidence;
    }

    public DossierEtudiant getDossier() {
        return dossier;
    }

    public void setDossier(DossierEtudiant dossier) {
        this.dossier = dossier;
    }

    public List<BulletinSemestrielle> getBulletins() {
        return bulletins;
    }

    public void setBulletins(List<BulletinSemestrielle> bulletins) {
        this.bulletins = bulletins;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }
    
    public String getPromotion() {
        return promotion;
    }
}
