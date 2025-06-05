package com.esp.scolarite.Service;

import com.esp.scolarite.entity.*;
import com.esp.scolarite.repository.BulletinSemestrielleRepository;
import com.esp.scolarite.repository.ElementDeModuleRepository;
import com.esp.scolarite.repository.EtudiantRepository;
import com.esp.scolarite.repository.NoteSemestrielleRepository;
import com.esp.scolarite.repository.SemestreRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BulletinSemestrielleService {

    private final BulletinSemestrielleRepository bulletinRepository;
    private final ElementDeModuleRepository elementModuleRepository;
    private final EtudiantRepository etudiantRepository;
    private final SemestreRepository semestreRepository;


 private final NoteSemestrielleRepository noteRepository;

public BulletinSemestrielleService(
    BulletinSemestrielleRepository bulletinRepository,
    ElementDeModuleRepository elementModuleRepository,
    EtudiantRepository etudiantRepository,
    SemestreRepository semestreRepository,
    NoteSemestrielleRepository noteRepository
) {
    this.bulletinRepository = bulletinRepository;
    this.elementModuleRepository = elementModuleRepository;
    this.etudiantRepository = etudiantRepository;
    this.semestreRepository = semestreRepository;
    this.noteRepository = noteRepository;
}
 public List<Semestre> getAllSemestres() {
        return semestreRepository.findAll();
    }
    public BulletinSemestrielle findByMatriculeAndSemestre(String matricule, Long semestreId) {
        return bulletinRepository.findByEtudiantMatriculeAndSemestreIdSemestre(matricule, semestreId)
                .orElse(null);
    }

public BulletinSemestrielle getOrCreateBulletin(String matricule, Long semestreId) {
    // 🔹 Étudiant et Semestre
    Etudiant etudiant = etudiantRepository.findByMatricule(matricule)
        .orElseThrow(() -> new EntityNotFoundException("Étudiant non trouvé"));

    Semestre semestre = semestreRepository.findById(semestreId)
        .orElseThrow(() -> new EntityNotFoundException("Semestre non trouvé"));

    // 🔹 Supprimer l'ancien bulletin + ses notes
    bulletinRepository.findByEtudiantMatriculeAndSemestreIdSemestre(matricule, semestreId)
        .ifPresent(bulletinExist -> {
            // Supprimer les notes associées
            if (bulletinExist.getNotes() != null) {
                noteRepository.deleteAll(bulletinExist.getNotes());
            }
            bulletinRepository.delete(bulletinExist);
        });

    // 🔹 Nouveau bulletin
    BulletinSemestrielle bulletin = new BulletinSemestrielle();
    bulletin.setEtudiant(etudiant);
    bulletin.setSemestre(semestre);
    bulletin.setNotes(new ArrayList<>());

    Departement departement = etudiant.getDepartement();
    if (departement == null)
        throw new IllegalStateException("Étudiant sans département.");

    // 🔹 Modules à traiter
    List<ElementDeModule> modulesDept = elementModuleRepository.findBySemestreAndDepartement(semestre, departement);
    List<ElementDeModule> modulesCommuns = elementModuleRepository.findBySemestreAndPoleCommun(semestre);

    Set<ElementDeModule> allModules = new HashSet<>();
    allModules.addAll(modulesDept);
    allModules.addAll(modulesCommuns);

    // 🔹 Anciennes notes (à recopier si présentes)
    List<NoteSemestrielle> anciennesNotes = noteRepository.findAllByEtudiantAndSemestre(etudiant, semestre);
    Map<Long, NoteSemestrielle> notesParModule = anciennesNotes.stream()
        .filter(n -> n.getElementModule() != null)
        .collect(Collectors.toMap(
            n -> n.getElementModule().getIdEM(),
            n -> n,
            (n1, n2) -> n1 // éviter duplicate key
        ));

    for (ElementDeModule module : allModules) {
        NoteSemestrielle note = new NoteSemestrielle();
        note.setElementModule(module);
        note.setEtudiant(etudiant);
        note.setSemestre(semestre);
        note.setBulletinSemestrielle(bulletin);

        // Copier les anciennes valeurs si présentes
        NoteSemestrielle ancienne = notesParModule.get(module.getIdEM());
        if (ancienne != null) {
            note.setNoteDevoir(ancienne.getNoteDevoir());
            note.setNoteExamen(ancienne.getNoteExamen());
            note.setNoteRattrapage(ancienne.getNoteRattrapage());
        } else {
            note.setNoteDevoir(0);
            note.setNoteExamen(0);
            note.setNoteRattrapage(0);
        }

        bulletin.getNotes().add(note);
    }

    // 🔹 Calcul de la moyenne
    bulletin.calculerMoyennes();

    try {
        return bulletinRepository.save(bulletin); // cascade de sauvegarde automatique des notes
    } catch (Exception e) {
        System.err.println("❌ Erreur dans getOrCreateBulletin : " + e.getMessage());
        e.printStackTrace();
        throw e;
    }
}



    
 
@Transactional
public List<BulletinSemestrielle> generateBulletinsForDepartementAndPromotion(
        Long idDepartement, String promotion, Long semestreId) {

    // 🔍 Étape 1 : récupérer les étudiants selon le département et la promotion
    List<Etudiant> etudiants = etudiantRepository.findEtudiantsByDepartementAndPromotion(idDepartement, promotion);

    if (etudiants.isEmpty()) {
        throw new EntityNotFoundException("Aucun étudiant trouvé pour ce département et cette promotion.");
    }

    List<BulletinSemestrielle> bulletins = new ArrayList<>();

    // 🔄 Étape 2 : pour chaque étudiant, supprimer l'ancien bulletin s'il existe puis en créer un nouveau
    for (Etudiant etudiant : etudiants) {
        // Supprimer l'ancien bulletin s’il existe
        bulletinRepository.findByEtudiantMatriculeAndSemestreIdSemestre(etudiant.getMatricule(), semestreId)
            .ifPresent(bulletinRepository::delete);

        // Créer un nouveau bulletin
        BulletinSemestrielle nouveauBulletin = getOrCreateBulletin(etudiant.getMatricule(), semestreId);
        bulletins.add(nouveauBulletin);
    }

    return bulletins;
}



   public List<Etudiant> getEtudiantsByDeptPromo(Long idDepartement, String promotion) {
    return etudiantRepository.findEtudiantsByDepartementAndPromotion(idDepartement, promotion);
}



    
}
    