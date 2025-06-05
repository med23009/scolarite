// src/app/core/models/user.model.ts
export interface User {
    idMembre?: any;
    nom: string;
    prenom: string;
    fullName?: string; // Nom complet retourné par le backend
    dateNaissance?: string;
    compteBancaire?: string;
    telephone?: string;
    email: string;
    nationalite?: string;
    NNI?: string;
    nomBanque?: string;
    role?: string;
    genre?: string;
    specialiste?: string;
    lieuDeNaissance?: string;
    password?: string;
    departementId?: number; // ID du département associé (pour les CHEF_DEPT)
  }