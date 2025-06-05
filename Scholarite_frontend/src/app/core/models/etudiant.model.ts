// src/app/core/models/etudiant.model.ts
import { Departement } from './departement.model';

export interface Etudiant {
    idEtudiant?: number;
    matricule: string;
    nom: string;
    prenom: string;
    prenomAR?: string;
    nomAR?: string;
      dateNaissance?: Date;
    lieuNaissance?: string;
    lieuNaissanceAR?: string;
    sexe?: string;
    NNI?: string;
    email?: string;
    photo?: string;
    dateInscription?: Date;
    telephoneEtudiant?: string;
    telephoneCorrespondant?: string;
    adresseResidence?: string;
    anneeObtentionBac?: number;
    specialite?: string;
    departement?: Departement;
    promotion?: string;
  }