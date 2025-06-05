package com.esp.scolarite.Service.programme;

import com.esp.scolarite.entity.*;
import com.esp.scolarite.repository.*;
import com.esp.scolarite.util.ExcelImporter;
//import com.esp.scolarite.util.SemestreUtil;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service dédié aux fonctionnalités spécifiques aux Chefs de Département
 */
@Service
public class ProgrammeServicedept {
    
    @Autowired
    private UniteEnseignementRepository uniteEnseignementRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ElementDeModuleRepository elementDeModuleRepository;
    
    @Autowired
    private DepartementRepository departementRepository;
    
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
    public ElementDeModule saveElementDeModule(ElementDeModule em, String userEmail) {
    // Vérification du semestre
    if (em.getId_semestre() == null || em.getId_semestre().getIdSemestre() == null) {
        throw new IllegalArgumentException("Le semestre de l'élément de module est obligatoire.");
    }

    Long semestreId = em.getId_semestre().getIdSemestre();

    // ✅ Étape B : Vérification des crédits pour le rôle CHEF_DEPT
    boolean isValid = isValidSpecialiteCredit(semestreId, userEmail);
    if (!isValid) {
        throw new IllegalStateException("Le total des crédits de spécialité pour ce semestre dépasse la limite autorisée.");
    }

    // Si tout est OK, on enregistre
    return elementDeModuleRepository.save(em);
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
     * Récupère toutes les unités d'enseignement associées au département du chef de département
     * @param email Email du chef de département
     * @return Liste des unités d'enseignement du département
     */
    public List<UniteEnseignement> getDepartmentUniteEnseignements(String email) {
        // Find user by email
        Optional<User> user = userRepository.findByEmail(email);
        
        // Return empty list instead of null if user not found
        if (user.isEmpty()) {
            return new ArrayList<>();
        }
        
        User membreAcademique = user.get();
        System.out.println("User: " + membreAcademique.getEmail() + ", Role: " + membreAcademique.getRole());
        
        // Get all UEs
        List<UniteEnseignement> allUEs = uniteEnseignementRepository.findAll();
        
        // For debugging: Print all UEs and their departments
        for (UniteEnseignement ue : allUEs) {
            System.out.println("UE: " + ue.getCodeUE() + 
                ", Dept ID: " + (ue.getDepartement() != null ? ue.getDepartement().getIdDep() : "null") + 
                ", Dept Responsable: " + (ue.getDepartement() != null && ue.getDepartement().getResponsable() != null ? 
                    ue.getDepartement().getResponsable().getEmail() : "null") +
                ", Dept ResponsableDept: " + (ue.getDepartement() != null && ue.getDepartement().getResponsableDepartement() != null ? 
                    ue.getDepartement().getResponsableDepartement().getEmail() : "null") +
                ", User email: " + membreAcademique.getEmail());
        }
        
        // Modified filter that checks both possible relationships
        return allUEs.stream()
                .filter(ue -> {
                    if (ue.getDepartement() == null) return false;
                    
                    // Check if user is responsible via responsableDepartement
                    boolean isResponsibleViaField = ue.getDepartement().getResponsableDepartement() != null && 
                        membreAcademique.getEmail().equals(
                            ue.getDepartement().getResponsableDepartement().getEmail()
                        );
                    
                    // Check if user is responsible via id_responsable
                    boolean isResponsibleViaId = ue.getDepartement().getResponsable() != null && 
                        membreAcademique.getEmail().equals(
                            ue.getDepartement().getResponsable().getEmail()
                        );
                    
                    return isResponsibleViaField || isResponsibleViaId;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère tous les éléments de module associés aux départements dont l'utilisateur est responsable
     * @param email Email du chef de département
     * @return Liste des éléments de module du département
     */
    public List<ElementDeModule> getDepartmentElementsDeModule(String email) {
        // Récupérer les UEs du département
        List<UniteEnseignement> deptUEs = getDepartmentUniteEnseignements(email);
        
        if (deptUEs.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Créer un ensemble d'IDs d'UEs pour une recherche plus efficace
        List<Long> deptUEIds = deptUEs.stream()
                .map(UniteEnseignement::getIdUE)
                .collect(Collectors.toList());
        
        // Récupérer tous les EMs associés à ces UEs
        List<ElementDeModule> deptEMs = elementDeModuleRepository.findAll().stream()
                .filter(em -> em.getUniteEnseignement() != null && 
                       deptUEIds.contains(em.getUniteEnseignement().getIdUE()))
                .collect(Collectors.toList());
        
        System.out.println("Trouvé " + deptEMs.size() + " EMs pour le département du chef: " + email);
        
        return deptEMs;
    }
    
    /**
     * Associe une UE au département du chef de département
     * @param ue Unité d'enseignement à associer
     * @param email Email du chef de département
     * @return Unité d'enseignement mise à jour
     */
    public UniteEnseignement associateUEWithDepartment(UniteEnseignement ue, String email) {
        // Trouver le département dont l'utilisateur est responsable
        List<Departement> userDepartments = departementRepository.findByResponsableEmail(email);
        
        if (userDepartments.isEmpty()) {
            System.out.println("Aucun département trouvé pour l'utilisateur: " + email);
            // Recherche manuelle en cas d'échec de la méthode du repository
            List<Departement> allDepts = departementRepository.findAll();
            for (Departement dept : allDepts) {
                if (dept.getResponsable() != null && email.equals(dept.getResponsable().getEmail())) {
                    ue.setDepartement(dept);
                    return uniteEnseignementRepository.save(ue);
                }
            }
            return ue;
        }
        
        ue.setDepartement(userDepartments.get(0));
        return uniteEnseignementRepository.save(ue);
    }

    /**
     * Importe des données d'un fichier Excel spécifiquement pour un département
     */
    public Map<String, Object> importFromExcelForDepartment(MultipartFile file, String userEmail) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<UniteEnseignement> importedUEs = new ArrayList<>();
        List<ElementDeModule> importedEMs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        // Récupérer tous les semestres disponibles
        List<Semestre> semestres = semestreRepository.findAll();
        Map<Integer, Semestre> semestreMap = new HashMap<>();
        
        // Créer une map pour un accès facile aux semestres par leur numéro
        for (Semestre sem : semestres) {
            if (sem.getSemestre() != null && sem.getSemestre().startsWith("S")) {
                try {
                    int semestreNum = Integer.parseInt(sem.getSemestre().substring(1));
                    semestreMap.put(semestreNum, sem);
                } catch (NumberFormatException e) {
                    // Ignorer les erreurs de format
                }
            }
        }
        
        // Trouver le département dont l'utilisateur est responsable
        List<Departement> userDepartments = departementRepository.findByResponsableEmail(userEmail);
        if (userDepartments.isEmpty()) {
            errors.add("Aucun département trouvé pour l'utilisateur: " + userEmail);
            result.put("errors", errors);
            return result;
        }
        
        Departement userDepartment = userDepartments.get(0);
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                System.out.println("Traitement de la feuille: " + sheet.getSheetName());
                
                // Identifier la ligne d'en-tête
                int headerRowIndex = -1;
                for (int i = 0; i <= Math.min(20, sheet.getLastRowNum()); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        boolean hasUE = false;
                        boolean hasSemestre = false;
                        boolean hasCode = false;
                        boolean hasIntitule = false;
                        
                        for (Cell cell : row) {
                            if (cell != null && cell.getCellType() == CellType.STRING) {
                                String cellValue = cell.getStringCellValue().trim();
                                if (cellValue.equalsIgnoreCase("UE")) hasUE = true;
                                if (cellValue.equalsIgnoreCase("Semestre")) hasSemestre = true;
                                if (cellValue.contains("Code")) hasCode = true;
                                if (cellValue.equalsIgnoreCase("Intitulé")) hasIntitule = true;
                            }
                        }
                        
                        if ((hasUE && hasCode && hasIntitule) || (hasSemestre && hasCode && hasIntitule)) {
                            headerRowIndex = i;
                            break;
                        }
                    }
                }
                
                if (headerRowIndex == -1) {
                    errors.add("Feuille " + sheet.getSheetName() + ": En-tête non trouvé");
                    continue;
                }
                
                // Trouver les indices des colonnes importantes
                Row headerRow = sheet.getRow(headerRowIndex);
                Map<String, Integer> columnIndices = new HashMap<>();
                
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        String header = cell.getStringCellValue().trim();
                        
                        if (header.equalsIgnoreCase("Semestre")) columnIndices.put("semestre", i);
                        else if (header.equalsIgnoreCase("UE")) columnIndices.put("ue", i);
                        else if (header.contains("Description") || header.contains("(UE)")) columnIndices.put("description", i);
                        else if (header.contains("Code")) columnIndices.put("code", i);
                        else if (header.equalsIgnoreCase("Intitulé")) columnIndices.put("intitule", i);
                        else if (header.equalsIgnoreCase("Crédit")) columnIndices.put("credit", i);
                        else if (header.equalsIgnoreCase("Coef")) columnIndices.put("coef", i);
                        else if (header.equalsIgnoreCase("CM")) columnIndices.put("cm", i);
                        else if (header.equalsIgnoreCase("TD")) columnIndices.put("td", i);
                        else if (header.equalsIgnoreCase("TP")) columnIndices.put("tp", i);
                    }
                }
                
                // Vérifier si nous avons trouvé les colonnes essentielles
                if (!columnIndices.containsKey("code") || !columnIndices.containsKey("intitule")) {
                    errors.add("Feuille " + sheet.getSheetName() + ": Colonnes essentielles manquantes");
                    continue;
                }
                
                // Traitement des lignes de données
                String currentSemestre = null;
                String currentUE = null;
                String currentUEDescription = null;
                
                for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    
                    // Vérifier si la ligne contient des données pertinentes
                    boolean hasData = false;
                    for (Cell cell : row) {
                        if (cell != null && cell.getCellType() != CellType.BLANK) {
                            hasData = true;
                            break;
                        }
                    }
                    if (!hasData) continue;
                    
                    // Lecture des valeurs de la ligne
                    String semestre = ExcelImporter.getCellValueAsString(row, columnIndices.getOrDefault("semestre", -1));
                    String ue = ExcelImporter.getCellValueAsString(row, columnIndices.getOrDefault("ue", -1));
                    String description = ExcelImporter.getCellValueAsString(row, columnIndices.getOrDefault("description", -1));
                    String code = ExcelImporter.getCellValueAsString(row, columnIndices.getOrDefault("code", -1));
                    String intitule = ExcelImporter.getCellValueAsString(row, columnIndices.getOrDefault("intitule", -1));
                    
                    // Mettre à jour les valeurs actuelles si elles sont présentes
                    if (semestre != null && !semestre.isEmpty()) currentSemestre = semestre;
                    if (ue != null && !ue.isEmpty()) currentUE = ue;
                    if (description != null && !description.isEmpty()) currentUEDescription = description;
                    
                    // Si nous avons un code et un intitulé, nous avons un élément de module
                    if (code != null && !code.isEmpty() && intitule != null && !intitule.isEmpty()) {
                        try {
                            // Extraire les informations du code EM
                            ExcelImporter.EMCodeInfo codeInfo = ExcelImporter.extractEMCodeInfo(code);
                            
                            // Créer l'UE si elle n'existe pas déjà
                            if (currentUE != null) {
                                String ueCode = currentUE;
                                String ueDescription = currentUEDescription != null ? currentUEDescription : ueCode;
                                
                                UniteEnseignement uniteEnseignement = null;
                                Optional<UniteEnseignement> existingUE = uniteEnseignementRepository.findByCodeUE(ueCode);
                                
                                if (existingUE.isPresent()) {
                                    uniteEnseignement = existingUE.get();
                                } else {
                                    uniteEnseignement = new UniteEnseignement();
                                    uniteEnseignement.setCodeUE(ueCode);
                                    uniteEnseignement.setIntitule(ueDescription);
                                    
                                    // Extraire le numéro de semestre
                                    int semestreNum = 0;
                                    if (currentSemestre != null && currentSemestre.startsWith("S")) {
                                        try {
                                            semestreNum = Integer.parseInt(currentSemestre.substring(1));
                                        } catch (NumberFormatException e) {
                                            semestreNum = codeInfo.semestreNum;
                                        }
                                    } else {
                                        semestreNum = codeInfo.semestreNum;
                                    }
                                    
                                    uniteEnseignement.setSemestre(semestreNum);
                                    uniteEnseignement.setSemestreNum(semestreNum);
                                    
                                    // Associer au semestre correspondant si disponible
                                    Semestre semObj = semestreMap.get(semestreNum);
                                    if (semObj != null) {
                                        uniteEnseignement.setSemestre(semObj);
                                    }
                                    
                                    // Associer au département de l'utilisateur
                                    uniteEnseignement.setDepartement(userDepartment);
                                    
                                    // Sauvegarder l'UE avant de créer l'EM
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
                                Semestre semElem = semestreMap.get(codeInfo.semestreNum);
                                if (semElem != null) {
                                    elementDeModule.setId_semestre(semElem);
                                }
                                
                                // Extraire les autres informations disponibles
                                if (columnIndices.containsKey("credit")) {
                                    elementDeModule.setNombreCredits(ExcelImporter.getCellValueAsFloat(row, columnIndices.get("credit")));
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
                        } catch (Exception e) {
                            errors.add("Erreur lors du traitement de la ligne " + (rowIndex+1) + ": " + e.getMessage());
                            e.printStackTrace();
                        }
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
     * Récupère les unités d'enseignement du département
     * @param email Email du chef de département
     * @return Liste des unités d'enseignement
     */
    public List<UniteEnseignement> getDepartmentUniteEnseignementsCurrentPeriod(String email) {
        // Récupérer le département du chef
        Optional<User> chefOpt = userRepository.findByEmail(email);
        if (chefOpt.isEmpty() || chefOpt.get().getDepartement() == null) {
            return new ArrayList<>();
        }
        Departement departement = chefOpt.get().getDepartement();
/* 
        // Récupérer les semestres de la période actuelle
        List<Semestre> currentPeriodSemestres = SemestreUtil.getCurrentPariteSemestres(semestreRepository.findAll());
        List<Integer> currentPeriodSemestreNums = currentPeriodSemestres.stream()
                .map(sem -> Integer.parseInt(sem.getSemestre().substring(1)))
                .collect(Collectors.toList());

        // Filtrer les UEs par semestre
        return deptUEs.stream()
                .filter(ue -> ue.getSemestreNum() != null && currentPeriodSemestreNums.contains(ue.getSemestreNum()))
                .collect(Collectors.toList());
        */
        return uniteEnseignementRepository.findByDepartement(departement);
    }

    /**
     * Récupère les éléments de module du département
     * @param email Email du chef de département
     * @return Liste des éléments de module
     */
    public List<ElementDeModule> getDepartmentElementsDeModuleCurrentPeriod(String email) {
        // Récupérer les UEs du département
        List<UniteEnseignement> deptUEs = getDepartmentUniteEnseignementsCurrentPeriod(email);
        if (deptUEs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> deptUEIds = deptUEs.stream()
                .map(UniteEnseignement::getIdUE)
                .collect(Collectors.toList());
/* 
        // Récupérer les semestres de la période actuelle
        List<Semestre> currentPeriodSemestres = SemestreUtil.getCurrentPariteSemestres(semestreRepository.findAll());
        List<Integer> currentPeriodSemestreNums = currentPeriodSemestres.stream()
                .map(sem -> Integer.parseInt(sem.getSemestre().substring(1)))
                .collect(Collectors.toList());
        List<String> currentPeriodSemestresCodes = currentPeriodSemestres.stream()
                .map(Semestre::getSemestre)
                .collect(Collectors.toList());

        // Récupérer tous les EMs associés à ces UEs
        List<ElementDeModule> ems = elementDeModuleRepository.findByUniteEnseignement_IdUEIn(deptUEIds);

        // Filtrer les EMs par semestre de la période courante
        return ems.stream()
                .filter(em -> {
                    Integer num = em.getSemestre();
                    String code = null;
                    if (em.getId_semestre() != null) {
                        code = em.getId_semestre().getSemestre();
                    }
                    return (num != null && currentPeriodSemestreNums.contains(num))
                        || (code != null && currentPeriodSemestresCodes.contains(code));
                })
                .collect(Collectors.toList());
*/
        return elementDeModuleRepository.findByUniteEnseignement_IdUEIn(deptUEIds);
    }
    /**
    * Vérifie si la somme des crédits des EM d’un semestre est inférieure ou égale au crédit spécialité défini
    * @param semestreId ID du semestre
    * @param userEmail Email du chef de département
    * @return true si la somme est valide, sinon false
    */
    public boolean isValidSpecialiteCredit(Long semestreId, String userEmail) {
        Optional<Semestre> semestreOpt = semestreRepository.findById(semestreId);
        if (semestreOpt.isEmpty()) return false;

        Semestre semestre = semestreOpt.get();
        Float maxCredit = Float.valueOf(semestre.getCreditSpecialite());

        // Récupérer les EM du département pour ce semestre
        List<ElementDeModule> ems = getDepartmentElementsDeModule(userEmail).stream()
            .filter(em -> em.getId_semestre() != null && em.getId_semestre().getIdSemestre().equals(semestreId))
            .collect(Collectors.toList());

        float totalCredit = (float) ems.stream()
        .map(em -> {
            Float credits = em.getNombreCredits();
            return credits != null ? credits : 0f;
        })
        .reduce(0f, Float::sum);

        return totalCredit <= maxCredit;
    }

}