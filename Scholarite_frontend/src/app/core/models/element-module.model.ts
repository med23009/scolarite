// src/app/core/models/element-module.model.ts
import { UniteEnseignement } from './unite-enseignement.model';
import { Semestre } from './semestre.model';

export interface ElementDeModule {
  idEM?: number;
  codeEU?: string;
  codeEM: string;
  intitule: string;
  nombreCredits?: number;
  coefficient?: number;
  semestre?: number;
  heuresCM?: number;
  heuresTD?: number;
  heuresTP?: number;
  responsableEM?: string;
  uniteEnseignement?: UniteEnseignement;
  id_semestre?: Semestre;  // Propriété ajoutée
}