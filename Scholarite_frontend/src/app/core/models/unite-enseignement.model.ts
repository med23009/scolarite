// src/app/core/models/unite-enseignement.model.ts
import { ElementDeModule } from "./element-module.model";
import { Semestre } from "./semestre.model";

export interface UniteEnseignement {
    idUE?: number;
    codeUE: string;
    intitule: string;
    nbEM?: number;
    annee?: number;
    semestre?: number;
    elementsDeModule?: ElementDeModule[];
    semestre_obj?: Semestre;  // Propriété ajoutée
}