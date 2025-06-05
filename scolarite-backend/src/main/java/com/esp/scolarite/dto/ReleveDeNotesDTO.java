package com.esp.scolarite.dto;

import com.esp.scolarite.entity.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleveDeNotesDTO {
    private Etudiant etudiant;
    private String anneeUniversitaire;
    private Map<String, List<NoteSemestrielle>> notesParUE = new HashMap<>();
    private Double moyenneGeneraleSemestre;
    private Boolean semestreValide;
    private float creditsAccumules;
    private float creditsTotaux;
    private String dateSignature;
    private Semestre semestre;

    public void calculerStatistiques() {
        this.creditsAccumules = 0;
        this.creditsTotaux = 0;
        double sommePonderee = 0;
        float totalCredits = 0;

        for (List<NoteSemestrielle> notes : notesParUE.values()) {
            for (NoteSemestrielle note : notes) {
                float credits = note.getElementModule() != null ? note.getElementModule().getNombreCredits() : 0;
                this.creditsTotaux += credits;
                
                if (note.getNoteGenerale() >= 10) {
                    this.creditsAccumules += credits;
                }

                sommePonderee += note.getNoteGenerale() * credits;
                totalCredits += credits;
            }
        }

        this.moyenneGeneraleSemestre = totalCredits > 0 ? sommePonderee / totalCredits : 0;
        this.semestreValide = this.moyenneGeneraleSemestre >= 10;
    }

    public static ReleveDeNotesDTO fromBulletinSemestrielle(BulletinSemestrielle bulletin) {
        ReleveDeNotesDTO dto = new ReleveDeNotesDTO();
        dto.setEtudiant(bulletin.getEtudiant());
        dto.setSemestre(bulletin.getSemestre());
        dto.setAnneeUniversitaire(String.valueOf(bulletin.getSemestre().getAnnee()));
        
        // Grouper les notes par UE en utilisant Collectors.groupingBy
        Map<String, List<NoteSemestrielle>> notesParUE = bulletin.getNotes().stream()
            .collect(Collectors.groupingBy(
                note -> note.getElementModule().getUniteEnseignement().getCodeUE(),
                HashMap::new,
                Collectors.toList()
            ));
        dto.setNotesParUE(notesParUE);
        
        dto.calculerStatistiques();
        dto.setDateSignature(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        
        return dto;
    }
}