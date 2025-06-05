package com.esp.scolarite.util;

import com.esp.scolarite.entity.Semestre;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SemestreUtil {
    
    /**
     * Détermine si un semestre est pair
     * @param semestre Le semestre à vérifier
     * @return true si le semestre est pair (S2, S4, etc.), false sinon
     */
    public static boolean isSemestrePair(Semestre semestre) {
        if (semestre.getSemestre() == null) return false;
        String numStr = semestre.getSemestre().replaceAll("[^0-9]", "");
        if (numStr.isEmpty()) return false;
        int num = Integer.parseInt(numStr);
        return num % 2 == 0;
    }

    /**
     * Détermine la parité du semestre actuel
     * @param semestres Liste des semestres disponibles
     * @return true si le semestre actuel est pair, false sinon
     */
    public static boolean getCurrentSemestreParite(List<Semestre> semestres) {
        Date currentDate = new Date();
        Semestre currentSemestre = semestres.stream()
            .filter(s -> s.getDateDebut() != null && s.getDateFin() != null &&
                        currentDate.after(s.getDateDebut()) && currentDate.before(s.getDateFin()))
            .findFirst()
            .orElse(null);
        
        if (currentSemestre == null) {
            // Par défaut, si aucun semestre n'est trouvé, on considère que c'est un semestre pair
            return true;
        }
        
        return isSemestrePair(currentSemestre);
    }

    /**
     * Filtre les semestres par parité
     * @param semestres Liste des semestres à filtrer
     * @param paritePair true pour les semestres pairs, false pour les impairs
     * @return Liste des semestres filtrés
     */
    public static List<Semestre> filterSemestresByParite(List<Semestre> semestres, boolean paritePair) {
        return semestres.stream()
            .filter(s -> isSemestrePair(s) == paritePair)
            .collect(Collectors.toList());
    }

    /**
     * Filtre les semestres pour n'afficher que ceux de la parité actuelle
     * @param semestres Liste des semestres à filtrer
     * @return Liste des semestres de la parité actuelle
     */
    public static List<Semestre> getCurrentPariteSemestres(List<Semestre> semestres) {
        boolean currentParitePair = getCurrentSemestreParite(semestres);
        return filterSemestresByParite(semestres, currentParitePair);
    }
} 