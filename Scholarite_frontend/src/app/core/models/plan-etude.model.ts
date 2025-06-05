import { ElementDeModule } from './element-module.model';

/**
 * Modèle pour le plan d'étude d'un étudiant
 */
export interface PlanEtude {
    matricule: string;
    nomEtudiant: string;
    prenomEtudiant: string;
    semestre: string;
    totalCredits: number;
    departementCode: string;
    departementNom: string;
    modulesNonValides: ElementDeModule[];
    modulesParUE: { [key: string]: ElementDeModule[] };
    semestreBloque?: boolean;
    messageBloquage?: string;
    notesModules?: { [codeEM: string]: number };
    validationStatuts?: { [codeEM: string]: string };
} 