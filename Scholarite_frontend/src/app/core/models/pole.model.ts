// src/app/core/models/pole.model.ts
import { User } from './user.model';

export interface Pole {
  idPole?: number;
  intitule: string;
  codePole: string;
  description?: string;
  nom_responsable?: string;
  responsable?: User;
}