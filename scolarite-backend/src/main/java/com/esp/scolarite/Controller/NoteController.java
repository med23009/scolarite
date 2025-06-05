package com.esp.scolarite.Controller;

import com.esp.scolarite.Service.NoteService;
import com.esp.scolarite.Service.admin.HistoriqueService;
import com.esp.scolarite.Config.JwtService;
import com.esp.scolarite.entity.ElementDeModule;
import com.esp.scolarite.entity.NoteSemestrielle;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.repository.ElementDeModuleRepository;
import com.esp.scolarite.repository.SemestreRepository;
import com.esp.scolarite.util.SemestreUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private HistoriqueService historiqueService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ElementDeModuleRepository elementDeModuleRepository;

    @Autowired
    private SemestreRepository semestreRepository;

    /**
     * Récupère toutes les notes, éventuellement filtrées par email d'utilisateur
     * @param email Email de l'utilisateur pour filtrer par département/pôle
     * @return Liste des notes accessibles
     */
    @GetMapping
    public ResponseEntity<List<NoteSemestrielle>> getAllNotes(@RequestParam(required = false) String email) {
        try {
            List<NoteSemestrielle> notes;
            if (email != null && !email.isEmpty()) {
                notes = noteService.getNotesByDepartment(email);
            } else {
                notes = noteService.getAllNotes();
            }

            // Récupérer les semestres de la parité en cours
            List<Semestre> currentPeriodSemestres = SemestreUtil.getCurrentPariteSemestres(semestreRepository.findAll());
            List<String> currentPeriodSemestresCodes = currentPeriodSemestres.stream()
                .map(Semestre::getSemestre)
                .collect(Collectors.toList());

            // Filtrer les notes pour ne garder que celles des EM des semestres en cours
            notes = notes.stream()
                .filter(note -> 
                    note.getElementModule() != null && 
                    note.getElementModule().getId_semestre() != null &&
                    currentPeriodSemestresCodes.contains(note.getElementModule().getId_semestre().getSemestre())
                )
                .collect(Collectors.toList());

            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une note par son ID
     * @param id ID de la note
     * @return La note correspondante ou 404 si non trouvée
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoteSemestrielle> getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère les notes d'un étudiant par son matricule
     * @param matricule Matricule de l'étudiant
     * @return Liste des notes de l'étudiant
     */
    @GetMapping("/etudiant/{matricule}")
    public ResponseEntity<List<NoteSemestrielle>> getNotesByMatricule(@PathVariable String matricule) {
        try {
            List<NoteSemestrielle> notes = noteService.getNotesByMatricule(matricule);
            
            // Récupérer les semestres de la parité en cours
            List<Semestre> currentPeriodSemestres = SemestreUtil.getCurrentPariteSemestres(semestreRepository.findAll());
            List<String> currentPeriodSemestresCodes = currentPeriodSemestres.stream()
                .map(Semestre::getSemestre)
                .collect(Collectors.toList());

            // Filtrer les notes pour ne garder que celles des EM des semestres en cours
            notes = notes.stream()
                .filter(note -> 
                    note.getElementModule() != null && 
                    note.getElementModule().getId_semestre() != null &&
                    currentPeriodSemestresCodes.contains(note.getElementModule().getId_semestre().getSemestre())
                )
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les notes d'un module par son code
     * @param codeEM Code de l'élément module
     * @return Liste des notes pour ce module
     */
    @GetMapping("/module/{codeEM}")
    public ResponseEntity<List<NoteSemestrielle>> getNotesByModule(@PathVariable String codeEM) {
        List<NoteSemestrielle> notes = noteService.getNotesByModule(codeEM);
        return ResponseEntity.ok(notes);
    }

    /**
     * Récupère les modules accessibles à un utilisateur
     * @param email Email de l'utilisateur
     * @param role Rôle de l'utilisateur
     * @return Liste des modules filtrés selon le rôle
     */
    @GetMapping("/modules")
    public ResponseEntity<List<ElementDeModule>> getAllModules(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role) {
        try {
            List<ElementDeModule> modules;
            if (email != null && !email.isEmpty()) {
                // Filtrer les modules en fonction du rôle
                if (role != null && role.equals("CHEF_POLE")) {
                    // Pour les chefs de pôle, filtrer par pôle
                    modules = noteService.getModulesByPole(email);
                } else {
                    // Pour les chefs de département, filtrer par département
                    modules = noteService.getModulesByDepartment(email);
                }
            } else {
                modules = elementDeModuleRepository.findAll();
            }

            // Récupérer les semestres de la parité en cours
            List<Semestre> currentPeriodSemestres = SemestreUtil.getCurrentPariteSemestres(semestreRepository.findAll());
            List<String> currentPeriodSemestresCodes = currentPeriodSemestres.stream()
                .map(Semestre::getSemestre)
                .collect(Collectors.toList());

            // Filtrer les modules pour ne garder que ceux des semestres en cours
            modules = modules.stream()
                .filter(module -> 
                    module.getId_semestre() != null &&
                    currentPeriodSemestresCodes.contains(module.getId_semestre().getSemestre())
                )
                .collect(Collectors.toList());

            return ResponseEntity.ok(modules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère tous les semestres
     * @return Liste de tous les semestres
     */
    @GetMapping("/semestres")
    public ResponseEntity<List<Semestre>> getAllSemestres() {
        List<Semestre> allSemestres = semestreRepository.findAll();
        List<Semestre> filteredSemestres = SemestreUtil.getCurrentPariteSemestres(allSemestres);
        return ResponseEntity.ok(filteredSemestres);
    }

    /**
     * Crée une nouvelle note
     * @param note Note à créer
     * @param token Token JWT pour l'historique
     * @return La note créée
     */
    @PostMapping
    public ResponseEntity<NoteSemestrielle> createNote(@RequestBody NoteSemestrielle note,
                                                       @RequestHeader("Authorization") String token) {
        NoteSemestrielle saved = noteService.saveNote(note);
        String utilisateur = jwtService.extractUsername(token.substring(7));
        String matricule = (note.getEtudiant() != null && note.getEtudiant().getMatricule() != null) ? note.getEtudiant().getMatricule() : "(inconnu)";
        historiqueService.enregistrerAction(utilisateur, "Ajout note", "Ajout de note pour matricule : " + matricule);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Met à jour une note existante
     * @param id ID de la note à mettre à jour
     * @param note Données de la note
     * @param token Token JWT pour l'historique
     * @return La note mise à jour ou 404 si non trouvée
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteSemestrielle> updateNote(@PathVariable Long id,
                                                       @RequestBody NoteSemestrielle note,
                                                       @RequestHeader("Authorization") String token) {
        return noteService.getNoteById(id)
                .map(existingNote -> {
                    // Mettre à jour uniquement les champs modifiables
                    existingNote.setNoteDevoir(note.getNoteDevoir());
                    existingNote.setNoteExamen(note.getNoteExamen());
                    existingNote.setNoteRattrapage(note.getNoteRattrapage());

                    NoteSemestrielle updated = noteService.saveNote(existingNote);

                    String utilisateur = jwtService.extractUsername(token.substring(7));
                    historiqueService.enregistrerAction(utilisateur, "Modification note", "Modification de la note ID : " + id);

                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Supprime une note
     * @param id ID de la note à supprimer
     * @param token Token JWT pour l'historique
     * @return 204 si suppression réussie, 404 si note non trouvée
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id,
                                           @RequestHeader("Authorization") String token) {
        return noteService.getNoteById(id)
                .map(note -> {
                    noteService.deleteNote(id);
                    String utilisateur = jwtService.extractUsername(token.substring(7));
                    historiqueService.enregistrerAction(utilisateur, "Suppression note", "Suppression de la note ID : " + id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Importe des notes à partir d'un fichier Excel
     * @param file Fichier Excel
     * @param idElementModule ID de l'élément module
     * @param annee Année académique
     * @param semestre Numéro du semestre (pour compatibilité)
     * @param idSemestre ID de l'objet Semestre
     * @param userEmail Email de l'utilisateur pour vérification d'accès
     * @param userRole Rôle de l'utilisateur pour vérification d'accès
     * @param token Token JWT pour l'historique
     * @return Résultat de l'importation avec notes importées et erreurs éventuelles
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importNotesFromExcel(@RequestParam("file") MultipartFile file,
                                              @RequestParam("idElementModule") Long idElementModule,
                                              @RequestParam("annee") int annee,
                                              @RequestParam("semestre") int semestre,
                                              @RequestParam(value = "idSemestre", required = false) Long idSemestre,
                                              @RequestParam(value = "userEmail", required = false) String userEmail,
                                              @RequestParam(value = "userRole", required = false) String userRole,
                                              @RequestHeader("Authorization") String token) {
        try {
            // Vérifie que le module existe
            Optional<ElementDeModule> elementModuleOpt = elementDeModuleRepository.findById(idElementModule);
            if (elementModuleOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Élément de module non trouvé avec ID: " + idElementModule);
            }

            ElementDeModule elementModule = elementModuleOpt.get();

            // Vérifie que le semestre est fourni si nécessaire
            if (idSemestre == null) {
                return ResponseEntity.badRequest().body("L'ID du semestre est requis pour l'importation.");
            }

            // Extraire le username et le rôle du token JWT
            String username = jwtService.extractUsername(token.substring(7));
            String role = jwtService.extractRole(token.substring(7));
            
            System.out.println("Importing notes for user: " + username + " with role: " + role);
            
            // Vérifier les permissions pour les rôles CHEF_DEPT et CHEF_DEPARTEMENT
            if (role != null && (role.equals("CHEF_DEPT") || role.equals("CHEF_DEPARTEMENT"))) {
                boolean hasPermission = noteService.canAccessModule(username, elementModule);
                if (!hasPermission) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Vous n'avez pas les permissions pour importer des notes pour ce module.");
                }
            }

            // Appelle le service pour importer les notes
            Map<String, Object> result = noteService.importFromExcel(file, elementModule, annee, semestre, idSemestre);

            // Enregistrement de l'action dans l'historique
            historiqueService.enregistrerAction(username, "Import notes", "Import des notes pour le module ID : " + idElementModule);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'import du fichier Excel : " + e.getMessage());
        }
    }
}