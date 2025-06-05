// src/app/core/models/departement.model.ts

import { User } from './user.model';
export interface Departement {
  idDep?: number;
  codeDep: string;
  intitule: string;
  description?: string;
  nom_responsable?: string;
  responsable?: User;
}