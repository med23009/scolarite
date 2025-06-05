// src/app/core/models/pv.model.ts
import { Departement } from './departement.model';
import { Semestre } from './semestre.model';

export interface PVExportParams {
  idDepartement: number;
  idSemestre: number;
}

export interface PVSelectionData {
  departements: Departement[];
  semestres: Semestre[];
}