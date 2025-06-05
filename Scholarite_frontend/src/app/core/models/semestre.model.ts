// src/app/core/models/semestre.model.ts
export interface Semestre {
  idSemestre?: number;
  nombreSemaines: number;
  dateDebut: Date | string;
  dateFin: Date | string;
  creditSpecialite: number;
  creditHE: number;
  creditST: number;
  annee: number;
  semestre: string;
}
