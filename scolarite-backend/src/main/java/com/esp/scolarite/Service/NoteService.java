package com.esp.scolarite.Service;

import com.esp.scolarite.entity.*;
import com.esp.scolarite.repository.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.esp.scolarite.entity.Role.CHEF_DEPT;
import static com.esp.scolarite.entity.Role.CHEF_POLE;

@Service
public class NoteService {

    @Autowired
    private NoteSemestrielleRepository noteSemestrielleRepository;
    
    @Autowired
    private EtudiantRepository etudiantRepository;
    
    @Autowired
    private SemestreRepository semestreRepository;
    
    @Autowired
    private ElementDeModuleRepository elementDeModuleRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Récupère toutes les notes semestrielles
     * @return Liste de toutes les notes
     */
    public List<NoteSemestrielle> getAllNotes() {
        return noteSemestrielleRepository.findAll();
    }

    /**
     * Récupère une note par son ID
     * @param id ID de la note
     * @return Optional contenant la note si trouvée
     */
    public Optional<NoteSemestrielle> getNoteById(Long id) {
        return noteSemestrielleRepository.findById(id);
    }
    
    /**
     * Récupère les notes d'un étudiant par son matricule
     * @param matricule Matricule de l'étudiant
     * @return Liste des notes de l'étudiant
     */
    public List<NoteSemestrielle> getNotesByMatricule(String matricule) {
        return noteSemestrielleRepository.findByMatriculeEtudiant(matricule);
    }
    
    /**
     * Récupère les notes d'un module par son code
     * @param codeEM Code de l'élément de module
     * @return Liste des notes pour ce module
     */
    public List<NoteSemestrielle> getNotesByModule(String codeEM) {
        return noteSemestrielleRepository.findByCodeEM(codeEM);
    }

    /**
     * Enregistre ou met à jour une note
     * @param note Note à sauvegarder
     * @return Note sauvegardée
     */
    public NoteSemestrielle saveNote(NoteSemestrielle note) {
        return noteSemestrielleRepository.save(note);
    }

    /**
     * Supprime une note par son ID
     * @param id ID de la note à supprimer
     */
    public void deleteNote(Long id) {
        noteSemestrielleRepository.deleteById(id);
    }
    
    /**
     * Filtre les notes par département selon le membre académique
     * @param email Email du membre académique
     * @return Liste des notes accessibles par le membre académique
     */
    public List<NoteSemestrielle> getNotesByDepartment(String email) {
        // Find user by email
        Optional<User> user = userRepository.findByEmail(email);
        
        // Return empty list if user not found
        if (user.isEmpty()) {
            return new ArrayList<>();
        }
        
        User membreAcademique = user.get();
        List<NoteSemestrielle> allNotes = noteSemestrielleRepository.findAll();
        
        // CHEF_DEPT: Filter notes by department
        if (membreAcademique.getRole().equals(CHEF_DEPT)) {
            return allNotes.stream()
                    .filter(note -> {
                        if (note.getElementModule() == null || 
                            note.getElementModule().getUniteEnseignement() == null || 
                            note.getElementModule().getUniteEnseignement().getDepartement() == null) {
                            return false;
                        }
                        
                        Departement dept = note.getElementModule().getUniteEnseignement().getDepartement();
                        
                        // Utiliser une seule méthode pour récupérer le responsable
                        if (dept.getResponsable() != null) {
                            return membreAcademique.getEmail().equals(dept.getResponsable().getEmail());
                        }
                        
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        // CHEF_POLE: Filter notes by pole
        if (membreAcademique.getRole().equals(CHEF_POLE)) {
            return allNotes.stream()
                    .filter(note -> {
                        if (note.getElementModule() == null || 
                            note.getElementModule().getUniteEnseignement() == null || 
                            note.getElementModule().getUniteEnseignement().getPole() == null) {
                            return false;
                        }
                        
                        Pole pole = note.getElementModule().getUniteEnseignement().getPole();
                        
                        return pole.getResponsable() != null && 
                            membreAcademique.getEmail().equals(pole.getResponsable().getEmail());
                    })
                    .collect(Collectors.toList());
        }
        
        // For ADMIN or DE roles, return all notes
        return allNotes;
    }

    /**
     * Récupère les modules filtrés par département
     * @param email Email du membre académique
     * @return Liste des modules accessibles par le membre académique
     */
    public List<ElementDeModule> getModulesByDepartment(String email) {
        // Find user by email
        Optional<User> user = userRepository.findByEmail(email);
        
        // Return empty list if user not found
        if (user.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get all modules
        List<ElementDeModule> allModules = elementDeModuleRepository.findAll();
        User membreAcademique = user.get();
        
        // For debugging
        System.out.println("User: " + membreAcademique.getEmail() + ", Role: " + membreAcademique.getRole());
        
        // CHEF_DEPT: Filter modules by department where user is responsible
        if (membreAcademique.getRole().equals(CHEF_DEPT)) {
            return allModules.stream()
                    .filter(module -> {
                        if (module.getUniteEnseignement() == null || 
                            module.getUniteEnseignement().getDepartement() == null) {
                            return false;
                        }
                        
                        // Utiliser une seule méthode pour récupérer le responsable
                        if (module.getUniteEnseignement().getDepartement().getResponsable() != null) {
                            return membreAcademique.getEmail().equals(
                                module.getUniteEnseignement().getDepartement().getResponsable().getEmail()
                            );
                        }
                        
                        return false;
                    })
                    .toList();
        }
        
        // CHEF_POLE: Filter modules by pole
        if (membreAcademique.getRole().equals(CHEF_POLE)) {
            return allModules.stream()
                    .filter(module -> {
                        if (module.getUniteEnseignement() == null || 
                            module.getUniteEnseignement().getPole() == null) {
                            return false;
                        }
                        
                        return module.getUniteEnseignement().getPole().getResponsable() != null && 
                            membreAcademique.getEmail().equals(
                                module.getUniteEnseignement().getPole().getResponsable().getEmail()
                            );
                    })
                    .toList();
        }
        
        // For ADMIN or DE roles, return all modules
        return allModules;
    }

    /**
     * Importe des notes à partir d'un fichier Excel
     * @param file Fichier Excel
     * @param elementModule Module concerné
     * @param annee Année académique
     * @param semestre Numéro du semestre 
     * @param idSemestre ID de l'objet Semestre
     * @return Map contenant les notes importées et les éventuelles erreurs
     */
    public Map<String, Object> importFromExcel(MultipartFile file, ElementDeModule elementModule, int annee, int semestre, Long idSemestre) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<NoteSemestrielle> importedNotes = new ArrayList<>();
        List<String> errors = new ArrayList<>();
    
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
    
            Sheet sheet = workbook.getSheetAt(0);
    
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add("Le fichier est vide ou ne contient pas d'en-tête.");
                result.put("errors", errors);
                return result;
            }
    
            int matriculeIdx = -1;
            int devoirIdx = -1;
            int examenIdx = -1;
            int rattrapageIdx = -1;
    
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String header = cell.getStringCellValue().trim().toLowerCase();
                    if (header.contains("matricule")) matriculeIdx = i;
                    else if (header.contains("devoir")) devoirIdx = i;
                    else if (header.contains("examen") || header.contains("exam")) examenIdx = i;
                    else if (header.contains("rattrapage")) rattrapageIdx = i;
                }
            }
    
            if (matriculeIdx == -1 || (devoirIdx == -1 && examenIdx == -1)) {
                errors.add("Colonnes manquantes. Besoin de 'matricule' et au moins 'devoir' ou 'examen'.");
                result.put("errors", errors);
                return result;
            }
    
            Semestre semestreObj = null;
            if (idSemestre != null) {
                Optional<Semestre> optSemestre = semestreRepository.findById(idSemestre);
                if (optSemestre.isPresent()) {
                    semestreObj = optSemestre.get();
                } else {
                    errors.add("Semestre introuvable avec ID : " + idSemestre);
                    result.put("errors", errors);
                    return result;
                }
            }
    
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;
    
                Cell matriculeCell = row.getCell(matriculeIdx);
                if (matriculeCell == null) continue;
    
            String matricule = getCellValueAsString(matriculeCell);
            if (matricule == null || matricule.trim().isEmpty()) continue;
    
            try {
                Optional<Etudiant> etudiantOpt = etudiantRepository.findByMatricule(matricule);
                if (etudiantOpt.isEmpty()) {
                    errors.add("Étudiant avec matricule " + matricule + " introuvable (ligne " + (rowIdx + 1) + ")");
                    continue;
                }
                
                // Vérifier que le semestre est défini
                if (semestreObj == null) {
                    errors.add("Semestre non défini pour l'étudiant " + matricule + " (ligne " + (rowIdx + 1) + ")");
                    continue;
                }
                
                // Créer un nouvel objet NoteSemestrielle
                NoteSemestrielle note = new NoteSemestrielle();
                note.setEtudiant(etudiantOpt.get());
                note.setElementModule(elementModule);
                note.setAnnee(annee);
                
                // Définir le numéro de semestre (1 ou 2)
                note.setSemestreNumero(semestre);
                
                // Log pour débogage
                System.out.println("Setting semestreNumero: " + semestre + " for student: " + matricule);
                
                // Définir explicitement l'objet semestre
                if (semestreObj != null) {
                    System.out.println("Found Semestre object: ID = " + semestreObj.getIdSemestre() + ", Libelle = " + semestreObj.getSemestre() + " for student: " + matricule);
                    note.setSemestre(semestreObj);
                }

                if (devoirIdx >= 0) {
                    double noteDevoir = getCellValueAsDouble(row.getCell(devoirIdx));
                    note.setNoteDevoir(noteDevoir);
                }
                
                if (examenIdx >= 0) {
                    double noteExamen = getCellValueAsDouble(row.getCell(examenIdx));
                    note.setNoteExamen(noteExamen);
                }

                if (rattrapageIdx >= 0) {
                    double noteRattrapage = getCellValueAsDouble(row.getCell(rattrapageIdx));
                    note.setNoteRattrapage(noteRattrapage);
                }
                
                // Vérification finale avant sauvegarde
                if (note.getSemestre() == null) {
                    errors.add("Erreur: Le semestre n'est pas défini pour l'étudiant " + matricule + " (ligne " + (rowIdx + 1) + ")");
                    continue;
                }

                NoteSemestrielle savedNote = noteSemestrielleRepository.save(note);
                importedNotes.add(savedNote);
            } catch (Exception e) {
                errors.add("Erreur à la ligne " + (rowIdx + 1) + ": " + e.getMessage());
            }
        }
    } catch (Exception e) {
            errors.add("Erreur globale lors du traitement : " + e.getMessage());
            throw e;
        }
    
        result.put("importedNotes", importedNotes);
        result.put("errors", errors);
        return result;
    }
    
    /**
     * Convertit une cellule Excel en chaîne de caractères
     * @param cell Cellule Excel
     * @return Valeur de la cellule en string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING: 
                return cell.getStringCellValue().trim();
            case NUMERIC: 
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.valueOf((int) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default: 
                return null;
        }
    }
    
    /**
     * Convertit une cellule Excel en valeur numérique
     * @param cell Cellule Excel
     * @return Valeur numérique de la cellule
     */
    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0;
        
        switch (cell.getCellType()) {
            case NUMERIC: 
                return cell.getNumericCellValue();
            case STRING: 
                try {
                    String value = cell.getStringCellValue().trim();
                    // Remplacer la virgule par un point si nécessaire
                    value = value.replace(',', '.');
                    // Supprimer tout caractère non numérique sauf le point décimal
                    value = value.replaceAll("[^0-9.]", "");
                    if (value.isEmpty()) return 0;
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    return 0;
                }
            default: 
                return 0;
        }
    }
    
    /**
     * Vérifie si un utilisateur (chef de département) a accès à un module spécifique
     * @param userEmail Email de l'utilisateur
     * @param elementModule Module à vérifier
     * @return true si l'utilisateur a accès, false sinon
     */
    public boolean canAccessModule(String userEmail, ElementDeModule elementModule) {
        // Trouver l'utilisateur par email
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Vérifier le rôle de l'utilisateur
        if (user.getRole().equals(CHEF_DEPT)) {
            return canChefDeptAccessModule(user, elementModule);
        } else if (user.getRole().equals(CHEF_POLE)) {
            return canChefPoleAccessModule(user, elementModule);
        }
        
        return false;
    }
    
    /**
     * Vérifie si un chef de département a accès à un module spécifique
     * @param user Utilisateur chef de département
     * @param elementModule Module à vérifier
     * @return true si l'utilisateur a accès, false sinon
     */
    private boolean canChefDeptAccessModule(User user, ElementDeModule elementModule) {
        // Vérifier si le module appartient au département de l'utilisateur
        if (elementModule == null || 
            elementModule.getUniteEnseignement() == null || 
            elementModule.getUniteEnseignement().getDepartement() == null) {
            return false;
        }
        
        Departement dept = elementModule.getUniteEnseignement().getDepartement();
        
        // Utiliser uniquement getResponsable() pour la cohérence
        return dept.getResponsable() != null && 
            user.getEmail().equals(dept.getResponsable().getEmail());
    }
    
    /**
     * Vérifie si un chef de pôle a accès à un module spécifique
     * @param user Utilisateur chef de pôle
     * @param elementModule Module à vérifier
     * @return true si l'utilisateur a accès, false sinon
     */
    private boolean canChefPoleAccessModule(User user, ElementDeModule elementModule) {
        // Vérifier si le module appartient au pôle de l'utilisateur
        if (elementModule == null || 
            elementModule.getUniteEnseignement() == null || 
            elementModule.getUniteEnseignement().getPole() == null) {
            return false;
        }
        
        Pole pole = elementModule.getUniteEnseignement().getPole();
        
        // Vérifier si l'utilisateur est responsable de ce pôle
        return pole.getResponsable() != null && 
            user.getEmail().equals(pole.getResponsable().getEmail());
    }

    /**
     * Méthode pour obtenir les modules filtrés par pôle
     * @param email Email de l'utilisateur chef de pôle
     * @return Liste des modules appartenant au pôle de l'utilisateur
     */
    public List<ElementDeModule> getModulesByPole(String email) {
        // Find user by email
        Optional<User> user = userRepository.findByEmail(email);
        
        // Return empty list if user not found
        if (user.isEmpty()) {
            return new ArrayList<>();
        }
        
        User membreAcademique = user.get();
        List<ElementDeModule> allModules = elementDeModuleRepository.findAll();
        
        // CHEF_POLE: Filter modules by pole
        if (membreAcademique.getRole().equals(CHEF_POLE)) {
            return allModules.stream()
                    .filter(module -> {
                        if (module.getUniteEnseignement() == null || 
                            module.getUniteEnseignement().getPole() == null) {
                            return false;
                        }
                        
                        Pole pole = module.getUniteEnseignement().getPole();
                        
                        return pole.getResponsable() != null && 
                            membreAcademique.getEmail().equals(pole.getResponsable().getEmail());
                    })
                    .collect(Collectors.toList());
        }
        
        // Default: return all modules
        return allModules;
    }

    
}