package com.esp.scolarite.Service.programme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.esp.scolarite.entity.ElementDeModule;
import com.esp.scolarite.entity.Pole;
import com.esp.scolarite.entity.Role;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.entity.UniteEnseignement;
import com.esp.scolarite.entity.User;
import com.esp.scolarite.repository.ElementDeModuleRepository;
import com.esp.scolarite.repository.PoleRepository;
import com.esp.scolarite.repository.SemestreRepository;
import com.esp.scolarite.repository.UniteEnseignementRepository;
import com.esp.scolarite.repository.UserRepository;
import com.esp.scolarite.util.ExcelImporter;

/**
 * Service dédié aux fonctionnalités spécifiques aux Chefs de Pôle
 */
@Service
public class ProgrammeServicepole {
    
    @Autowired
    private UniteEnseignementRepository uniteEnseignementRepository;
    
    @Autowired
    private ElementDeModuleRepository elementDeModuleRepository;
    
    @Autowired
    private PoleRepository poleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SemestreRepository semestreRepository;
    
    /**
     * Récupère une unité d'enseignement par son ID
     * @param id ID de l'unité d'enseignement
     * @return Unité d'enseignement optionnelle
     */
    public Optional<UniteEnseignement> getUniteEnseignementById(Long id) {
        return uniteEnseignementRepository.findById(id);
    }

    /**
     * Récupère une unité d'enseignement par son code
     * @param code Code de l'unité d'enseignement
     * @return Unité d'enseignement optionnelle
     */
    public Optional<UniteEnseignement> getUniteEnseignementByCode(String code) {
        return uniteEnseignementRepository.findByCodeUE(code);
    }
    /**
     * Supprime une unité d'enseignement
     * @param id ID de l'unité d'enseignement à supprimer
     */
    public void deleteUniteEnseignement(Long id) {
        uniteEnseignementRepository.deleteById(id);
    }
    /**
     * Récupère un élément de module par son ID
     * @param id ID de l'élément de module
     * @return Élément de module optionnel
     */
    public Optional<ElementDeModule> getElementDeModuleById(Long id) {
        return elementDeModuleRepository.findById(id);
    }
    
    /**
     * Enregistre un élément de module
     * @param em Élément de module à enregistrer
     * @return Élément de module enregistré
     */
    public ElementDeModule saveElementDeModule(ElementDeModule em) {
        return elementDeModuleRepository.save(em);
    }
    
    /**
     * la mise à jour d'un element de module
     **/
    public ElementDeModule updateElementDeModule(Long id,ElementDeModule em, String email) {
    // Vérifie que l’EM existe
            Optional<ElementDeModule> existingOpt = elementDeModuleRepository.findById(id);
            if (existingOpt.isEmpty()) {
                throw new RuntimeException("Élément de module non trouvé");
            }

            ElementDeModule existing = existingOpt.get();

        // Vérifie que l'EM appartient à une UE du pôle du chef
        List<UniteEnseignement> poleUEs = getPoleUniteEnseignements(email);
        if (existing.getUniteEnseignement() == null || 
            !poleUEs.contains(existing.getUniteEnseignement())) {
            throw new RuntimeException("Cet EM n'appartient pas à votre pôle");
        }

        // Met à jour les champs principaux
        existing.setCodeEM(em.getCodeEM());
        existing.setIntitule(em.getIntitule());
        existing.setCodeEU(em.getCodeEU());
        existing.setNombreCredits(em.getNombreCredits());
        existing.setCoefficient(em.getCoefficient());
        existing.setSemestre(em.getSemestre());
        existing.setHeuresCM(em.getHeuresCM());
        existing.setHeuresTD(em.getHeuresTD());
        existing.setHeuresTP(em.getHeuresTP());

       // Gère le semestre si précisé (optionnel)
        if (em.getId_semestre() != null) {
            existing.setId_semestre(em.getId_semestre());
        }

        // Gère le responsable s’il est bien référencé
        if (em.getResponsableEM() != null && em.getResponsableEM().getIdMembre() != null) {
            Optional<User> respOpt = userRepository.findById(em.getResponsableEM().getIdMembre());
            respOpt.ifPresent(existing::setResponsableEM);
    }

    return elementDeModuleRepository.save(existing);
}

    /**
     * Supprime un élément de module
     * @param id ID de l'élément de module à supprimer
     */
    public void deleteElementDeModule(Long id) {
        elementDeModuleRepository.deleteById(id);
    }
    
    /**
     * Récupère tous les semestres
     * @return Liste des semestres
     */
    public List<Semestre> getAllSemestres() {
        return semestreRepository.findAll();
    }
    
    /**
     * Récupère un semestre par son ID
     * @param id ID du semestre
     * @return Semestre optionnel
     */
    public Optional<Semestre> getSemestreById(Long id) {
        return semestreRepository.findById(id);
    }
    
    /**
     * Récupère les semestres par année
     * @param annee Année des semestres
     * @return Liste des semestres de l'année spécifiée
     */
    public List<Semestre> getSemestresByAnnee(int annee) {
        return semestreRepository.findByAnnee(annee);
    }
    /**
     * Récupère un semestre par son numéro
     * @param numero Numéro du semestre (1-6)
     * @return Semestre optionnel
     */
    public Optional<Semestre> getSemestreByNumero(int numero) {
        String semestreCode = "S" + numero;
        List<Semestre> semestres = semestreRepository.findBySemestre(semestreCode);
        return semestres.isEmpty() ? Optional.empty() : Optional.of(semestres.get(0));
    }

    /**
     * Récupère toutes les unités d'enseignement associées au pôle du chef de pôle
     * @param email Email du chef de pôle
     * @return Liste des unités d'enseignement du pôle
     */
    public List<UniteEnseignement> getPoleUniteEnseignements(String email) {
        // Débug: Afficher les informations sur l'utilisateur
        System.out.println("Recherche des UEs pour le chef de pôle: " + email);
        
        // Trouver le pôle dont l'utilisateur est responsable
        List<Pole> userPoles = poleRepository.findAll().stream()
                .filter(p -> p.getResponsable() != null && 
                       email.equals(p.getResponsable().getEmail()))
                .collect(Collectors.toList());
        
        if (userPoles.isEmpty()) {
            System.out.println("Aucun pôle trouvé pour l'utilisateur: " + email);
            return new ArrayList<>();
        }
        
        Pole userPole = userPoles.get(0);
        System.out.println("Pôle trouvé: " + userPole.getIntitule());
        
        // Récupérer toutes les UEs associées à ce pôle
        List<UniteEnseignement> poleUEs = uniteEnseignementRepository.findAll().stream()
                .filter(ue -> ue.getPole() != null && 
                       ue.getPole().getIdPole().equals(userPole.getIdPole()))
                .collect(Collectors.toList());
        
        System.out.println("Trouvé " + poleUEs.size() + " UEs pour le pôle: " + userPole.getIntitule());
        
        return poleUEs;
    }
    
    /**
     * Récupère tous les éléments de module associés au pôle du chef de pôle
     * @param email Email du chef de pôle
     * @return Liste des éléments de module du pôle
     */
    public List<ElementDeModule> findElementsDeModuleByPoleChef(String email) {
        System.out.println("Recherche des EMs pour le chef de pôle: " + email);
        
        // Récupérer les UEs du pôle
        List<UniteEnseignement> poleUEs = getPoleUniteEnseignements(email);
        
        if (poleUEs.isEmpty()) {
            System.out.println("Aucune UE trouvée pour le pôle du chef: " + email);
            return new ArrayList<>();
        }
        
        // Créer une liste d'IDs d'UEs pour une recherche plus rapide
        List<Long> poleUEIds = new ArrayList<>();
        for (UniteEnseignement ue : poleUEs) {
            poleUEIds.add(ue.getIdUE());
        }
        
        // Récupérer tous les EMs associés à ces UEs
        List<ElementDeModule> poleEMs = new ArrayList<>();
        List<ElementDeModule> allEMs = elementDeModuleRepository.findAll();
        
        for (ElementDeModule em : allEMs) {
            if (em.getUniteEnseignement() != null && 
                poleUEIds.contains(em.getUniteEnseignement().getIdUE())) {
                poleEMs.add(em);
            }
        }
        
        System.out.println("Trouvé " + poleEMs.size() + " EMs pour le pôle du chef: " + email);
        return poleEMs;
    }
    
    /**
     * Associe une UE au pôle du chef de pôle
     * @param ue Unité d'enseignement à associer
     * @param email Email du chef de pôle
     * @return Unité d'enseignement mise à jour
     */
    public UniteEnseignement associateUEWithPole(UniteEnseignement ue, String email) {
        // Trouver le pôle dont l'utilisateur est responsable
        List<Pole> userPoles = poleRepository.findAll().stream()
                .filter(p -> p.getResponsable() != null && 
                       email.equals(p.getResponsable().getEmail()))
                .collect(Collectors.toList());
        
        if (userPoles.isEmpty()) {
            System.out.println("Aucun pôle trouvé pour l'utilisateur: " + email);
            return ue;
        }
        
        Pole userPole = userPoles.get(0);
        ue.setPole(userPole);
        return uniteEnseignementRepository.save(ue);
    }
    
    /**
     * Importe des UEs et EMs à partir d'un fichier Excel pour un chef de pôle
     * @param file Fichier Excel à importer
     * @param userEmail Email du chef de pôle
     * @return Résultat de l'importation
     */
    public Map<String, Object> importFromExcelForPole(MultipartFile file, String userEmail) {
        Map<String, Object> result = new HashMap<>();
        List<UniteEnseignement> importedUEs = new ArrayList<>();
        List<ElementDeModule> importedEMs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // Vérifier que l'utilisateur est bien un chef de pôle
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty() || !userOpt.get().getRole().equals(Role.CHEF_POLE)) {
                errors.add("Erreur: Seuls les chefs de pôle peuvent importer des données");
                result.put("errors", errors);
                return result;
            }
            
            // Trouver le pôle dont l'utilisateur est responsable
            List<Pole> userPoles = poleRepository.findAll().stream()
                    .filter(p -> p.getResponsable() != null && 
                           userEmail.equals(p.getResponsable().getEmail()))
                    .collect(Collectors.toList());
            
            if (userPoles.isEmpty()) {
                errors.add("Erreur: Aucun pôle trouvé pour cet utilisateur");
                result.put("errors", errors);
                return result;
            }
            
            Pole userPole = userPoles.get(0);
            
            // Récupérer tous les semestres pour les associer plus tard
            Map<String, Semestre> semestreMap = semestreRepository.findAll().stream()
                    .collect(Collectors.toMap(Semestre::getSemestre, s -> s, (s1, s2) -> s1));
            
            try (InputStream is = file.getInputStream();
                 Workbook workbook = new XSSFWorkbook(is)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                
                // Identifier les colonnes par leur en-tête
                Map<String, Integer> columnIndices = new HashMap<>();
                Row headerRow = sheet.getRow(0);
                
                // Débogage: Afficher les informations sur le fichier Excel
                System.out.println("Analyse du fichier Excel pour l'import pôle: " + file.getOriginalFilename());
                System.out.println("Nombre de lignes dans la feuille: " + sheet.getLastRowNum());
                
                // Assignation directe des colonnes basée sur le format spécifique de votre fichier Excel
                // Colonnes: UE(0), Code EM(1), Intitulé(2), Semestre(3), Crédit(4), CM(5), TD(6), TP(7)
                columnIndices.put("ue", 0);       // Colonne A: UE
                columnIndices.put("code", 1);     // Colonne B: Code EM
                columnIndices.put("intitule", 2); // Colonne C: Intitulé
                columnIndices.put("semestre", 3); // Colonne D: Semestre
                columnIndices.put("credit", 4);   // Colonne E: Crédit
                columnIndices.put("cm", 5);       // Colonne F: CM
                columnIndices.put("td", 6);       // Colonne G: TD
                columnIndices.put("tp", 7);       // Colonne H: TP
                
                // Débogage: Afficher les indices de colonnes assignés
                System.out.println("Indices de colonnes assignés: " + columnIndices);
                
                // Vérifier quand même les en-têtes pour le débogage
                if (headerRow != null) {
                    System.out.println("En-têtes détectés:");
                    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                        String header = ExcelImporter.getCellValueAsString(headerRow, i);
                        System.out.println("  Colonne " + i + ": '" + header + "'");
                    }
                }
                
                // Pour chaque ligne de données (après l'en-tête)
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    
                    try {
                        // Débogage: Afficher les informations sur la ligne en cours de traitement
                        System.out.println("Traitement de la ligne " + (rowIndex+1));
                        
                        // Vérifier si la ligne est vide (toutes les cellules sont vides)
                        boolean isEmptyRow = true;
                        for (int i = 0; i < 8; i++) { // Vérifie les 8 premières colonnes (A-H)
                            String cellValue = ExcelImporter.getCellValueAsString(row, i);
                            if (cellValue != null && !cellValue.trim().isEmpty()) {
                                isEmptyRow = false;
                                break;
                            }
                        }
                        
                        // Si la ligne est vide, arrêter l'importation
                        if (isEmptyRow) {
                            System.out.println("Ligne vide détectée à la ligne " + (rowIndex+1) + ", arrêt de l'importation.");
                            break;
                        }
                        
                        // Extraire le code et l'intitulé
                        String code = ExcelImporter.getCellValueAsString(row, columnIndices.getOrDefault("code", -1));
                        String intitule = ExcelImporter.getCellValueAsString(row, columnIndices.getOrDefault("intitule", -1));
                        String ueCode = ExcelImporter.getCellValueAsString(row, columnIndices.getOrDefault("ue", -1));
                        
                        System.out.println("  UE: '" + ueCode + "', Code EM: '" + code + "', Intitulé: '" + intitule + "'");
                        
                        // Si le code est vide mais qu'il y a des données dans la première colonne, essayer de l'utiliser comme code
                        if ((code == null || code.isEmpty()) && row.getLastCellNum() > 0) {
                            code = ExcelImporter.getCellValueAsString(row, 1); // Colonne B: Code EM
                            System.out.println("  Tentative avec la colonne B comme code: '" + code + "'");
                        }
                        
                        // Si l'UE est vide mais qu'il y a des données dans la première colonne, essayer de l'utiliser comme UE
                        if ((ueCode == null || ueCode.isEmpty()) && row.getLastCellNum() > 0) {
                            ueCode = ExcelImporter.getCellValueAsString(row, 0); // Colonne A: UE
                            System.out.println("  Tentative avec la colonne A comme UE: '" + ueCode + "'");
                        }
                        
                        // Si l'intitulé est vide mais qu'il y a des données dans la troisième colonne, essayer de l'utiliser comme intitulé
                        if ((intitule == null || intitule.isEmpty()) && row.getLastCellNum() > 2) {
                            intitule = ExcelImporter.getCellValueAsString(row, 2); // Colonne C: Intitulé
                            System.out.println("  Tentative avec la colonne C comme intitulé: '" + intitule + "'");
                        }
                        
                        // Vérifier si au moins le code est présent
                        if (code != null && !code.isEmpty()) {
                            // Extraire les informations du code pour déterminer le semestre
                            ExcelImporter.EMCodeInfo codeInfo = ExcelImporter.extractEMCodeInfo(code);
                            
                            // ueCode a déjà été extrait plus haut
                            UniteEnseignement uniteEnseignement = null;
                            
                            // Débogage: Afficher les informations extraites du code EM

                            
                            if (ueCode != null && !ueCode.isEmpty()) {
                                // Chercher l'UE par code
                                Optional<UniteEnseignement> existingUE = uniteEnseignementRepository.findByCodeUE(ueCode);
                                
                                if (existingUE.isPresent()) {
                                    uniteEnseignement = existingUE.get();
                                    // Associer au pôle de l'utilisateur si ce n'est pas déjà fait
                                    if (uniteEnseignement.getPole() == null) {
                                        uniteEnseignement.setPole(userPole);
                                        uniteEnseignement = uniteEnseignementRepository.save(uniteEnseignement);
                                    }
                                } else {
                                    // Créer une nouvelle UE
                                    uniteEnseignement = new UniteEnseignement();
                                    uniteEnseignement.setCodeUE(ueCode);
                                    uniteEnseignement.setIntitule("UE pour " + intitule);
                                    uniteEnseignement.setPole(userPole);
                                    
                                    // Associer au semestre correspondant si disponible
                                    // Convertir le numéro de semestre en chaîne (S1, S2, etc.)
                                    String semestreCode = "S" + codeInfo.semestreNum;
                                    Semestre semUE = semestreMap.get(semestreCode);
                                    if (semUE != null) {
                                        uniteEnseignement.setSemestre(semUE);
                                    }
                                    
                                    uniteEnseignement = uniteEnseignementRepository.save(uniteEnseignement);
                                    importedUEs.add(uniteEnseignement);
                                }
                                
                                // Créer l'élément de module
                                ElementDeModule elementDeModule = new ElementDeModule();
                                elementDeModule.setCodeEM(code);
                                elementDeModule.setIntitule(intitule);
                                elementDeModule.setCodeEU(ueCode);
                                elementDeModule.setUniteEnseignement(uniteEnseignement);
                                
                                // Définir le numéro de semestre à partir du code
                                elementDeModule.setSemestre(codeInfo.semestreNum);
                                
                                // Associer au semestre correspondant si disponible
                                // Convertir le numéro de semestre en chaîne (S1, S2, etc.)
                                String semestreCode = "S" + codeInfo.semestreNum;
                                Semestre semElem = semestreMap.get(semestreCode);
                                if (semElem != null) {
                                    elementDeModule.setId_semestre(semElem);
                                }
                                
                                // Extraire les autres informations disponibles
                                if (columnIndices.containsKey("credit")) {
                                    elementDeModule.setNombreCredits(ExcelImporter.getCellValueAsInt(row, columnIndices.get("credit")));
                                }
                                
                                if (columnIndices.containsKey("coef")) {
                                    elementDeModule.setCoefficient(ExcelImporter.getCellValueAsFloat(row, columnIndices.get("coef")));
                                }
                                
                                if (columnIndices.containsKey("cm")) {
                                    elementDeModule.setHeuresCM(ExcelImporter.getCellValueAsInt(row, columnIndices.get("cm")));
                                }
                                
                                if (columnIndices.containsKey("td")) {
                                    elementDeModule.setHeuresTD(ExcelImporter.getCellValueAsInt(row, columnIndices.get("td")));
                                }
                                
                                if (columnIndices.containsKey("tp")) {
                                    elementDeModule.setHeuresTP(ExcelImporter.getCellValueAsInt(row, columnIndices.get("tp")));
                                }
                                
                                // Vérifier si l'EM existe déjà
                                Optional<ElementDeModule> existingEM = elementDeModuleRepository.findByCodeEM(code);
                                if (existingEM.isPresent()) {
                                    elementDeModule.setIdEM(existingEM.get().getIdEM());
                                }
                                
                                ElementDeModule savedEM = elementDeModuleRepository.save(elementDeModule);
                                importedEMs.add(savedEM);
                            } else {
                                errors.add("Ligne " + (rowIndex+1) + ": Impossible de déterminer l'UE pour l'élément de module");
                            }
                        } else {
                            errors.add("Ligne " + (rowIndex+1) + ": Code ou intitulé manquant");
                        }
                    } catch (Exception e) {
                        errors.add("Erreur lors du traitement de la ligne " + (rowIndex+1) + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            errors.add("Erreur d'importation: " + e.getMessage());
            e.printStackTrace();
        }
        
        result.put("uniteEnseignements", importedUEs);
        result.put("elementsDeModule", importedEMs);
        result.put("errors", errors);
        return result;
    }
    
    /**
     * Associe un semestre à une unité d'enseignement du pôle
     * @param ueId ID de l'unité d'enseignement
     * @param semestreId ID du semestre
     * @param email Email du chef de pôle
     * @return Unité d'enseignement mise à jour
     */
    public UniteEnseignement associateSemestreToUE(Long ueId, Long semestreId, String email) {
        // Vérifier que l'UE appartient au pôle de l'utilisateur
        Optional<UniteEnseignement> ueOpt = uniteEnseignementRepository.findById(ueId);
        if (ueOpt.isEmpty()) {
            throw new RuntimeException("Unité d'enseignement non trouvée");
        }
        
        UniteEnseignement ue = ueOpt.get();
        
        // Vérifier que l'UE appartient au pôle de l'utilisateur
        List<UniteEnseignement> poleUEs = getPoleUniteEnseignements(email);
        if (!poleUEs.contains(ue)) {
            throw new RuntimeException("Cette unité d'enseignement n'appartient pas à votre pôle");
        }
        
        // Associer le semestre
        Optional<Semestre> semestreOpt = semestreRepository.findById(semestreId);
        if (semestreOpt.isEmpty()) {
            throw new RuntimeException("Semestre non trouvé");
        }
        
        Semestre semestre = semestreOpt.get();
        ue.setSemestre(semestre);
        
        return uniteEnseignementRepository.save(ue);
    }
}