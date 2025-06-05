import { Etudiant } from './etudiant.model';
import { NoteSemestrielle } from './note-semestrielle.model';

export interface ReleveDeNotes {
  etudiant: Etudiant;
  semestre: {
    semestre: string;
    anneeUniversitaire: string;
  };
  notesParUE: {
    [codeUE: string]: NoteSemestrielle[];
  };
  semestreValide: boolean;
  creditsAccumules: number;
  creditsTotaux: number;
  dateSignature: string;
    moyenneGenerale?: number; // ✅ AJOUT ICI

}
