// src/app/core/models/element-module-dto.model.ts
export interface ElementDeModuleDTO {
  idEM?: number;
  codeEM: string;
  codeEU?: string;
  intitule: string;
  nombreCredits: number;
  coefficient: number;
  semestre: number;
  heuresCM: number;
  heuresTD: number;
  heuresTP: number;
  responsableEM: string;
  ueIntitule?: string;
  departement?: string;
  pole?: string;
}
