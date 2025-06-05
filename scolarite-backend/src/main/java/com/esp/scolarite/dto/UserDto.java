package com.esp.scolarite.dto;

import java.time.LocalDate;

import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.Pole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore les champs inconnus dans le JSON
public class UserDto {
    private Long idMembre;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String compteBancaire;
    private String telephone;
    private String email;
    private String nationalite;
    private String NNI; // Assurez-vous que ce champ existe
    private String password;
    private String nomBanque;
    private Integer role; // Changed from int to Integer to allow null checks
    private String genre;
    private String specialiste;
    private String lieuDeNaissance;
    private Departement departement; // Added to match AdminService references
    private Pole pole; // Added to match AdminService references
    private Long departementId; // ID du département pour les chefs de département
    private Long poleId; // ID du pôle pour les chefs de pôle

    public UserDto() {
    }
}