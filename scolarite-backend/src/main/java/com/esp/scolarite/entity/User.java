package com.esp.scolarite.entity;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
public class User implements UserDetails  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMembre;

    private String nom;
    private String prenom;
    
    // Méthode pour faciliter la sérialisation JSON
    public String getFullName() {
        return prenom + " " + nom;
    }
    
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;
    
    @Column(name = "compte_bancaire")
    private String compteBancaire;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "responsableEM" , cascade = CascadeType.DETACH)
    @JsonIgnore
    private ElementDeModule elementDeModule;

    @Getter
    @Setter
    @OneToOne(mappedBy = "responsableDepartement")
    @JsonIgnore
    private Departement departement;

    @Getter
    @Setter
    @OneToOne(mappedBy = "responsable") 
    @JsonIgnore
    private Pole pole;


    private String telephone;
    private String email;
    private String nationalite;
    private String NNI;
    
   
    @Column(name = "nom_banque")
    private String nomBanque;
    
    private String genre;
    private String specialiste;
    
    @Column(name = "lieu_de_naissance")
    private String lieuDeNaissance;
    private String password;
    
    @Column(name = "password_changed", nullable = false)
    private boolean passwordChanged = false; // Default to false for new users
    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(String nom, String prenom, LocalDate dateNaissance, String compteBancaire,
                String telephone, String email, String nationalite, String NNI,
                String password, String nomBanque, Role role, String genre,
                String specialiste, String lieuDeNaissance) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.compteBancaire = compteBancaire;
        this.telephone = telephone;
        this.email = email;
        this.nationalite = nationalite;
        this.NNI = NNI;
        this.password = password;
        this.nomBanque = nomBanque;
        this.role = role;
        this.genre = genre;
        this.specialiste = specialiste;
        this.lieuDeNaissance = lieuDeNaissance;
        this.passwordChanged = false; // Default to false for new users
    }

    // Getters and Setters
    public Long getIdMembre() {
        return idMembre;
    }

    public void setIdMembre(Long idMembre) {
        this.idMembre = idMembre;
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

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getCompteBancaire() {
        return compteBancaire;
    }

    public void setCompteBancaire(String compteBancaire) {
        this.compteBancaire = compteBancaire;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    public String getNNI() {
        return NNI;
    }

    public void setNNI(String NNI) {
        this.NNI = NNI;
    }

   
    public String getNomBanque() {
        return nomBanque;
    }

    public void setNomBanque(String nomBanque) {
        this.nomBanque = nomBanque;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSpecialiste() {
        return specialiste;
    }

    public void setSpecialiste(String specialiste) {
        this.specialiste = specialiste;
    }

    public String getLieuDeNaissance() {
        return lieuDeNaissance;
    }

    public void setLieuDeNaissance(String lieuDeNaissance) {
        this.lieuDeNaissance = lieuDeNaissance;
    }

    public Role getMemberRole() {
        return role;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role != null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
   
    @Override
    public String getUsername() {
        return email; // Use email as the username for Spring Security
    }
    
    public boolean isPasswordChanged() {
        return passwordChanged;
    }
    
    public void setPasswordChanged(boolean passwordChanged) {
        this.passwordChanged = passwordChanged;
    }
}
