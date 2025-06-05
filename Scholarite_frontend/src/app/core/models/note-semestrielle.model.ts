import { ElementDeModule } from './element-module.model';
import { Etudiant } from './etudiant.model';
import { Semestre } from './semestre.model';

export interface NoteSemestrielle {
  idNote?: number;
  noteRattrapage?: number;
  noteDevoir?: number;
  noteExamen?: number;
  semestre?: number;
  annee?: number;
  codeEM: string;
  matriculeEtudiant?: string;
  compensationInterne?: number;
  compensationExterne?: number;
  noteGenerale?: number;
  isValide?: boolean;
  credit?: number;

  elementModule?: ElementDeModule;
  etudiant?: Etudiant;         // ✅ nécessaire pour l'affichage du nom, prénom, matricule
  id_semestre?: Semestre;
}
