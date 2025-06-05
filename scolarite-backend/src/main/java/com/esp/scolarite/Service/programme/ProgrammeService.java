package com.esp.scolarite.Service.programme;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.esp.scolarite.entity.Role.CHEF_DEPT;
import static com.esp.scolarite.entity.Role.CHEF_POLE;

@Service
public class ProgrammeService {

    @Autowired
    private UniteEnseignementRepository uniteEnseignementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ElementDeModuleRepository elementDeModuleRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private PoleRepository poleRepository;

    @Autowired
    private SemestreRepository semestreRepository;

    public List<UniteEnseignement> getUEByDepartmentId(Long depId) {
        return uniteEnseignementRepository.findByDepartementIdDepartement(depId);
    }

    public List<UniteEnseignement> getUEByPoleId(Long poleId) {
        return uniteEnseignementRepository.findByPoleIdPole(poleId);
    }

    public List<ElementDeModule> getEMByDepartmentId(Long idDepartement) {
        return elementDeModuleRepository.findByUniteEnseignement_Departement_IdDepartement(idDepartement);
    }

    public List<ElementDeModule> getEMByPoleId(Long idPole) {
        return elementDeModuleRepository.findByUniteEnseignement_Pole_IdPole(idPole);
    }

    public List<ElementDeModule> getEMByUEId(Long idUE) {
        return elementDeModuleRepository.findByUniteEnseignement_IdUE(idUE);
    }


    // Unités d'Enseignement
    public List<UniteEnseignement> getAllUniteEnseignements(String email) {
        // Find user by email
        Optional<User> user = userRepository.findByEmail(email);

        // Return empty list instead of null if user not found
        if (user.isEmpty()) {
            return new ArrayList<>(); // Return empty list instead of null
        }

        // Get all UEs
        List<UniteEnseignement> UES = uniteEnseignementRepository.findAll();
        User membreAcademique = user.get();

        // For debugging: Print user role and email
        System.out.println("User: " + membreAcademique.getEmail() + ", Role: " + membreAcademique.getRole());

        // CHEF_DEPT: Filter UEs by department where user is responsible
        if (membreAcademique.getRole().equals(CHEF_DEPT)) {
            // For debugging: Print all UEs and their departments
            for (UniteEnseignement ue : UES) {
                System.out.println("UE: " + ue.getCodeUE() +
                        ", Dept ID: " + (ue.getDepartement() != null ? ue.getDepartement().getIdDep() : "null") +
                        ", Dept Responsable: "
                        + (ue.getDepartement() != null && ue.getDepartement().getResponsable() != null
                                ? ue.getDepartement().getResponsable().getEmail()
                                : "null")
                        +
                        ", Dept ResponsableDept: "
                        + (ue.getDepartement() != null && ue.getDepartement().getResponsableDepartement() != null
                                ? ue.getDepartement().getResponsableDepartement().getEmail()
                                : "null")
                        +
                        ", User email: " + membreAcademique.getEmail());
            }

            // Modified filter that checks both possible relationships
            return UES.stream()
                    .filter(ue -> {
                        if (ue.getDepartement() == null)
                            return false;

                        // Check if user is responsible via responsableDepartement
                        boolean isResponsibleViaField = ue.getDepartement().getResponsableDepartement() != null &&
                                membreAcademique.getEmail().equals(
                                        ue.getDepartement().getResponsableDepartement().getEmail());

                        // Check if user is responsible via id_responsable
                        boolean isResponsibleViaId = ue.getDepartement().getResponsable() != null &&
                                membreAcademique.getEmail().equals(
                                        ue.getDepartement().getResponsable().getEmail());

                        return isResponsibleViaField || isResponsibleViaId;
                    })
                    .toList();
        }

        // CHEF_POLE: Filter UEs by pole where user is responsible
        if (membreAcademique.getRole().equals(CHEF_POLE)) {
            // Debug: Print all UEs and their pole information
            System.out.println("DEBUG - Total UEs in database: " + UES.size());
            for (UniteEnseignement ue : UES) {
                System.out.println("DEBUG - UE: " + ue.getCodeUE() + " - " + ue.getIntitule() +
                        ", Pole: " + (ue.getPole() != null ? ue.getPole().getIntitule() : "null") +
                        ", Dept: " + (ue.getDepartement() != null ? ue.getDepartement().getIntitule() : "null") +
                        ", Pole Responsable: "
                        + (ue.getPole() != null && ue.getPole().getResponsable() != null
                                ? ue.getPole().getResponsable().getEmail()
                                : "null"));
            }

            String userEmail = membreAcademique.getEmail();

            // Get UEs that are explicitly associated with the user's pole and NOT with any
            // department
            List<UniteEnseignement> strictlyPoleUEs = UES.stream()
                    .filter(ue -> ue.getPole() != null &&
                            ue.getPole().getResponsable() != null &&
                            userEmail.equals(ue.getPole().getResponsable().getEmail()) &&
                            ue.getDepartement() == null) // Ensure it's not associated with a department
                    .collect(Collectors.toList());

            System.out.println("Strictly pole UEs found: " + strictlyPoleUEs.size() + " for pole chef: " + userEmail);

            // If we have UEs that are strictly for this pole, return them
            if (!strictlyPoleUEs.isEmpty()) {
                return strictlyPoleUEs;
            }

            // If no strictly pole UEs found, try a more lenient approach
            List<UniteEnseignement> lenientPoleUEs = UES.stream()
                    .filter(ue -> ue.getPole() != null &&
                            ue.getPole().getResponsable() != null &&
                            userEmail.equals(ue.getPole().getResponsable().getEmail()))
                    .collect(Collectors.toList());

            System.out.println("Lenient pole UEs found: " + lenientPoleUEs.size() + " for pole chef: " + userEmail);

            // If we have UEs with this pole (even if they also have a department), return
            // them
            if (!lenientPoleUEs.isEmpty()) {
                return lenientPoleUEs;
            }

            // If still no results, try to find the user's pole and associate UEs
            List<Pole> userPoles = poleRepository.findAll().stream()
                    .filter(p -> p.getResponsable() != null && userEmail.equals(p.getResponsable().getEmail()))
                    .collect(Collectors.toList());

            if (!userPoles.isEmpty()) {
                Pole userPole = userPoles.get(0);
                System.out.println("Found user's pole: " + userPole.getIntitule());

                // Find UEs that have no department and no pole, and associate them with this
                // pole
                List<UniteEnseignement> orphanUEs = UES.stream()
                        .filter(ue -> ue.getPole() == null && ue.getDepartement() == null)
                        .collect(Collectors.toList());

                System.out.println("Found " + orphanUEs.size() + " orphan UEs to associate with pole");

                List<UniteEnseignement> newlyAssociatedUEs = new ArrayList<>();
                for (UniteEnseignement ue : orphanUEs) {
                    ue.setPole(userPole);
                    uniteEnseignementRepository.save(ue);
                    System.out.println("Associated UE " + ue.getCodeUE() + " with pole " + userPole.getIntitule());
                    newlyAssociatedUEs.add(ue);
                }

                return newlyAssociatedUEs;
            }

            // If all else fails, return an empty list
            System.out.println("No UEs found for pole chef: " + userEmail);
            return new ArrayList<>();
        }

        // For ADMIN or DE roles, return all UEs
        return UES;
    }

    public Optional<UniteEnseignement> getUniteEnseignementById(Long id) {

        return uniteEnseignementRepository.findById(id);
    }

    public UniteEnseignement saveUniteEnseignement(UniteEnseignement ue) {
        // Vérifie si un semestre est associé à l'UE
        if (ue.getSemestreNum() != null && ue.getSemestreNum() > 0) {
            // Recherche du semestre correspondant
            List<Semestre> semestres = semestreRepository.findBySemestre("S" + ue.getSemestreNum());
            if (!semestres.isEmpty()) {
                ue.setSemestre(semestres.get(0));
            } else {
                // Si aucun semestre correspondant n'est trouvé, chercher par ID
                List<Semestre> allSemestres = semestreRepository.findAll();
                for (Semestre sem : allSemestres) {
                    if (sem.getSemestre() != null && sem.getSemestre().equals("S" + ue.getSemestreNum())) {
                        ue.setSemestre(sem);
                        break;
                    }
                }
            }
        }
        return uniteEnseignementRepository.save(ue);
    }

    public void deleteUniteEnseignement(Long id) {
        uniteEnseignementRepository.deleteById(id);
    }

    // Éléments de Module
    public List<ElementDeModule> getAllElementsDeModule() {
        return elementDeModuleRepository.findAll();
    }

    public Optional<ElementDeModule> getElementDeModuleById(Long id) {
        return elementDeModuleRepository.findById(id);
    }

    public ElementDeModule saveElementDeModule(ElementDeModule em) {
        // Associer l'élément de module au semestre correspondant
        if (em.getSemestre() > 0) {
            List<Semestre> semestres = semestreRepository.findBySemestre("S" + em.getSemestre());
            if (!semestres.isEmpty()) {
                // Si un semestre correspondant est trouvé, on l'associe
                em.setId_semestre(semestres.get(0));
            } else {
                // Essayer de trouver le semestre par son ID (Si les semestres sont numérotés de
                // 1 à 5)
                Optional<Semestre> semestreOpt = semestreRepository.findById((long) em.getSemestre());
                if (semestreOpt.isPresent()) {
                    em.setId_semestre(semestreOpt.get());
                }
            }
        }
        return elementDeModuleRepository.save(em);
    }

    public void deleteElementDeModule(Long id) {
        elementDeModuleRepository.deleteById(id);
    }

    // Récupérer tous les semestres
    public List<Semestre> getAllSemestres() {
        return semestreRepository.findAll();
    }

    // Récupérer un semestre par son ID
    public Optional<Semestre> getSemestreById(Long id) {
        return semestreRepository.findById(id);
    }

    // Récupérer les semestres par année
    public List<Semestre> getSemestresByAnnee(int annee) {
        return semestreRepository.findByAnnee(annee);
    }

    // Associer un semestre à une unité d'enseignement
    public UniteEnseignement associateSemestreToUE(Long ueId, Long semestreId) {
        Optional<UniteEnseignement> ueOpt = uniteEnseignementRepository.findById(ueId);
        Optional<Semestre> semOpt = semestreRepository.findById(semestreId);

        if (ueOpt.isPresent() && semOpt.isPresent()) {
            UniteEnseignement ue = ueOpt.get();
            Semestre semestre = semOpt.get();

            ue.setSemestre(semestre);

            // Mettre à jour le numéro de semestre si disponible
            if (semestre.getSemestre() != null && semestre.getSemestre().startsWith("S")) {
                try {
                    int semestreNum = Integer.parseInt(semestre.getSemestre().substring(1));
                    ue.setSemestre(semestreNum);
                    ue.setSemestreNum(semestreNum);
                } catch (NumberFormatException e) {
                    // Ne rien faire si le format n'est pas respecté
                }
            }

            return uniteEnseignementRepository.save(ue);
        }
        return null;
    }

    // Associer un semestre à un élément de module
    public ElementDeModule associateSemestreToEM(Long emId, Long semestreId) {
        Optional<ElementDeModule> emOpt = elementDeModuleRepository.findById(emId);
        Optional<Semestre> semOpt = semestreRepository.findById(semestreId);

        if (emOpt.isPresent() && semOpt.isPresent()) {
            ElementDeModule em = emOpt.get();
            Semestre semestre = semOpt.get();

            em.setId_semestre(semestre);

            // Mettre à jour le numéro de semestre si disponible
            if (semestre.getSemestre() != null && semestre.getSemestre().startsWith("S")) {
                try {
                    int semestreNum = Integer.parseInt(semestre.getSemestre().substring(1));
                    em.setSemestre(semestreNum);
                } catch (NumberFormatException e) {
                    // Ne rien faire si le format n'est pas respecté
                }
            }

            return elementDeModuleRepository.save(em);
        }
        return null;
    }

    // Récupérer les unités d'enseignement par semestre
    public List<UniteEnseignement> getUEBySemestre(Long semestreId) {
        Optional<Semestre> semOpt = semestreRepository.findById(semestreId);

        if (semOpt.isPresent()) {
            Semestre semestre = semOpt.get();
            List<UniteEnseignement> ues = new ArrayList<>();

            // Récupérer toutes les UE dont le semestre correspond
            List<UniteEnseignement> allUEs = uniteEnseignementRepository.findAll();
            for (UniteEnseignement ue : allUEs) {
                if (ue.getSemestre() != null && ue.getSemestre().equals(semestre)) {
                    ues.add(ue);
                } else if (semestre.getSemestre() != null && semestre.getSemestre().startsWith("S")) {
                    try {
                        int semestreNum = Integer.parseInt(semestre.getSemestre().substring(1));
                        if (ue.getSemestreNum() != null && ue.getSemestreNum() == semestreNum) {
                            ues.add(ue);
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer les erreurs de format
                    }
                }
            }

            return ues;
        }

        return Collections.emptyList();
    }

    // Récupérer les éléments de module par semestre
    public List<ElementDeModule> getEMBySemestre(Long semestreId) {
        Optional<Semestre> semOpt = semestreRepository.findById(semestreId);

        if (semOpt.isPresent()) {
            Semestre semestre = semOpt.get();

            // Récupérer tous les EM dont le semestre correspond
            List<ElementDeModule> allEMs = elementDeModuleRepository.findAll();
            List<ElementDeModule> ems = new ArrayList<>();

            for (ElementDeModule em : allEMs) {
                if (em.getId_semestre() != null && em.getId_semestre().equals(semestre)) {
                    ems.add(em);
                } else if (semestre.getSemestre() != null && semestre.getSemestre().startsWith("S")) {
                    try {
                        int semestreNum = Integer.parseInt(semestre.getSemestre().substring(1));
                        if (em.getSemestre() == semestreNum) {
                            ems.add(em);
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer les erreurs de format
                    }
                }
            }

            return ems;
        }

        return Collections.emptyList();
    }

    // Importer à partir d'un fichier Excel avec gestion des semestres
    public Map<String, Object> importFromExcel(MultipartFile file) throws IOException {
        return importFromExcel(file, null, null);
    }

    // Version surchargée qui accepte l'email et le rôle de l'utilisateur
    public Map<String, Object> importFromExcel(MultipartFile file, String userEmail, Role userRole) throws IOException {
        System.out.println("Importing Excel with user email: " + userEmail + " and role: " + userRole);

        // Debug: Check if poles exist in the database
        List<Pole> dbPoles = poleRepository.findAll();
        System.out.println("Total poles in database: " + dbPoles.size());
        for (Pole pole : dbPoles) {
            System.out.println("Pole: " + pole.getIdPole() + " - " + pole.getIntitule() +
                    ", Responsable: " + (pole.getResponsable() != null ? pole.getResponsable().getEmail() : "null"));
        }
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
                                if (cellValue.equalsIgnoreCase("UE"))
                                    hasUE = true;
                                if (cellValue.equalsIgnoreCase("Semestre"))
                                    hasSemestre = true;
                                if (cellValue.contains("Code"))
                                    hasCode = true;
                                if (cellValue.equalsIgnoreCase("Intitulé"))
                                    hasIntitule = true;
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

                        if (header.equalsIgnoreCase("Semestre"))
                            columnIndices.put("semestre", i);
                        else if (header.equalsIgnoreCase("UE"))
                            columnIndices.put("ue", i);
                        else if (header.contains("Description") || header.contains("(UE)"))
                            columnIndices.put("description", i);
                        else if (header.contains("Code"))
                            columnIndices.put("code", i);
                        else if (header.equalsIgnoreCase("Intitulé"))
                            columnIndices.put("intitule", i);
                        else if (header.equalsIgnoreCase("Crédit"))
                            columnIndices.put("credit", i);
                        else if (header.equalsIgnoreCase("Coef"))
                            columnIndices.put("coef", i);
                        else if (header.equalsIgnoreCase("CM"))
                            columnIndices.put("cm", i);
                        else if (header.equalsIgnoreCase("TD"))
                            columnIndices.put("td", i);
                        else if (header.equalsIgnoreCase("TP"))
                            columnIndices.put("tp", i);
                    }
                }

                System.out.println("Colonnes identifiées: " + columnIndices);

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
                    if (row == null)
                        continue;

                    // Vérifier si la ligne contient des données pertinentes
                    boolean hasData = false;
                    for (Cell cell : row) {
                        if (cell != null && cell.getCellType() != CellType.BLANK) {
                            hasData = true;
                            break;
                        }
                    }
                    if (!hasData)
                        continue;

                    // Lecture des valeurs de la ligne
                    String semestre = getCellValueAsString(row, columnIndices.getOrDefault("semestre", -1));
                    String ue = getCellValueAsString(row, columnIndices.getOrDefault("ue", -1));
                    String description = getCellValueAsString(row, columnIndices.getOrDefault("description", -1));
                    String code = getCellValueAsString(row, columnIndices.getOrDefault("code", -1));
                    String intitule = getCellValueAsString(row, columnIndices.getOrDefault("intitule", -1));

                    // Mettre à jour les valeurs actuelles si elles sont présentes
                    if (semestre != null && !semestre.isEmpty())
                        currentSemestre = semestre;
                    if (ue != null && !ue.isEmpty())
                        currentUE = ue;
                    if (description != null && !description.isEmpty())
                        currentUEDescription = description;

                    // Si nous avons un code et un intitulé, nous avons un élément de module
                    if (code != null && !code.isEmpty() && intitule != null && !intitule.isEmpty()) {
                        try {
                            // Extraire les informations du code EM
                            EMCodeInfo codeInfo = extractEMCodeInfo(code);

                            // Créer l'UE si elle n'existe pas déjà
                            if (currentUE != null) {
                                String ueCode = currentUE;
                                String ueDescription = currentUEDescription != null ? currentUEDescription : ueCode;

                                UniteEnseignement uniteEnseignement = null;
                                Optional<UniteEnseignement> existingUE = uniteEnseignementRepository
                                        .findByCodeUE(ueCode);

                                if (existingUE.isPresent()) {
                                    uniteEnseignement = existingUE.get();
                                } else {
                                    uniteEnseignement = new UniteEnseignement();
                                    uniteEnseignement.setCodeUE(ueCode);
                                    uniteEnseignement.setIntitule(ueDescription);

                                    // Extraire le numéro de semestre de currentSemestre (S1, S2, etc.) ou du code
                                    // EM
                                    int semestreNum = 0;
                                    if (currentSemestre != null && currentSemestre.startsWith("S")) {
                                        try {
                                            semestreNum = Integer.parseInt(currentSemestre.substring(1));
                                        } catch (NumberFormatException e) {
                                            // Utiliser le semestre extrait du code EM
                                            semestreNum = codeInfo.semestreNum;
                                        }
                                    } else {
                                        // Utiliser le semestre extrait du code EM
                                        semestreNum = codeInfo.semestreNum;
                                    }

                                    uniteEnseignement.setSemestre(semestreNum);
                                    uniteEnseignement.setSemestreNum(semestreNum);

                                    // Associer au semestre correspondant si disponible
                                    Semestre semObj = semestreMap.get(semestreNum);
                                    if (semObj != null) {
                                        uniteEnseignement.setSemestre(semObj);
                                    }

                                    // Associer au département ou au pôle en fonction du rôle de l'utilisateur
                                    if (userEmail != null && userRole != null) {
                                        if (userRole == Role.CHEF_DEPT) {
                                            // Trouver le département dont l'utilisateur est responsable
                                            System.out.println(
                                                    "Recherche du département pour l'utilisateur: " + userEmail);
                                            List<Departement> userDepartments = departementRepository
                                                    .findByResponsableEmail(userEmail);
                                            System.out.println("Départements trouvés: " + userDepartments.size());

                                            if (!userDepartments.isEmpty()) {
                                                uniteEnseignement.setDepartement(userDepartments.get(0));
                                                System.out.println("UE associée au département: "
                                                        + userDepartments.get(0).getIntitule());
                                            } else {
                                                System.out.println(
                                                        "AUCUN DÉPARTEMENT TROUVÉ pour l'utilisateur: " + userEmail);
                                                // Recherche manuelle en cas d'échec de la méthode du repository
                                                List<Departement> allDepts = departementRepository.findAll();
                                                System.out.println("Nombre total de départements: " + allDepts.size());
                                                for (Departement dept : allDepts) {
                                                    if (dept.getResponsable() != null) {
                                                        System.out.println("Département: " + dept.getIntitule()
                                                                + ", Responsable: " + dept.getResponsable().getEmail());
                                                        if (userEmail.equals(dept.getResponsable().getEmail())) {
                                                            uniteEnseignement.setDepartement(dept);
                                                            System.out
                                                                    .println("UE associée manuellement au département: "
                                                                            + dept.getIntitule());
                                                            break;
                                                        }
                                                    } else {
                                                        System.out.println(
                                                                "Département sans responsable: " + dept.getIntitule());
                                                    }
                                                }
                                            }
                                        } else if (userRole == Role.CHEF_POLE) {
                                            // Trouver le pôle dont l'utilisateur est responsable
                                            System.out.println("Recherche du pôle pour l'utilisateur: " + userEmail);

                                            // Recherche directe dans les pôles de la base de données
                                            boolean poleFound = false;
                                            for (Pole pole : dbPoles) {
                                                if (pole.getResponsable() != null) {
                                                    System.out.println("Vérification du pôle: " + pole.getIntitule()
                                                            + ", Responsable: " + pole.getResponsable().getEmail());
                                                    if (userEmail.equals(pole.getResponsable().getEmail())) {
                                                        uniteEnseignement.setPole(pole);
                                                        System.out.println("UE associée directement au pôle: "
                                                                + pole.getIntitule());
                                                        poleFound = true;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (!poleFound) {
                                                System.out.println(
                                                        "Aucun pôle trouvé directement, essai avec le repository...");
                                                List<Pole> userPoles = poleRepository.findByResponsableEmail(userEmail);
                                                System.out.println("Pôles trouvés par repository: " + userPoles.size());

                                                if (!userPoles.isEmpty()) {
                                                    uniteEnseignement.setPole(userPoles.get(0));
                                                    System.out.println(
                                                            "UE associée au pôle: " + userPoles.get(0).getIntitule());
                                                } else {
                                                    System.out.println(
                                                            "AUCUN PÔLE TROUVÉ pour l'utilisateur: " + userEmail);
                                                    // Si aucun pôle n'est trouvé mais qu'il y a des pôles dans la base,
                                                    // prendre le premier
                                                    if (!dbPoles.isEmpty()) {
                                                        Pole firstPole = dbPoles.get(0);
                                                        uniteEnseignement.setPole(firstPole);
                                                        System.out.println("UE associée au premier pôle disponible: "
                                                                + firstPole.getIntitule());
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Comportement par défaut: associer au département si trouvé
                                        if (codeInfo.departementCode != null) {
                                            List<Departement> departments = departementRepository
                                                    .findByCodeDepContaining(codeInfo.departementCode);
                                            if (!departments.isEmpty()) {
                                                uniteEnseignement.setDepartement(departments.get(0));
                                            }
                                        }
                                    }

                                    // Associer manuellement au pôle si c'est un CHEF_POLE et qu'aucun pôle n'a été
                                    // trouvé précédemment
                                    if (userRole == Role.CHEF_POLE && uniteEnseignement.getPole() == null
                                            && userEmail != null) {
                                        // Créer un nouveau pôle si nécessaire
                                        if (dbPoles.isEmpty()) {
                                            System.out.println(
                                                    "Aucun pôle trouvé dans la base de données. Création d'un nouveau pôle pour "
                                                            + userEmail);
                                            // Trouver l'utilisateur
                                            Optional<User> user = userRepository.findByEmail(userEmail);
                                            if (user.isPresent()) {
                                                Pole newPole = new Pole();
                                                newPole.setIntitule("Pôle " + user.get().getNom());
                                                newPole.setCodePole("P" + user.get().getNom().substring(0,
                                                        Math.min(3, user.get().getNom().length())));
                                                newPole.setResponsable(user.get());
                                                newPole = poleRepository.save(newPole);
                                                System.out.println("Nouveau pôle créé: " + newPole.getIntitule()
                                                        + " pour " + userEmail);
                                                uniteEnseignement.setPole(newPole);
                                            }
                                        } else {
                                            // Chercher un pôle existant qui pourrait correspondre
                                            for (Pole pole : dbPoles) {
                                                if (pole.getResponsable() != null
                                                        && userEmail.equals(pole.getResponsable().getEmail())) {
                                                    System.out.println("Association manuelle de l'UE au pôle: "
                                                            + pole.getIntitule());
                                                    uniteEnseignement.setPole(pole);
                                                    break;
                                                }
                                            }

                                            // Si toujours pas de pôle, prendre le premier disponible
                                            if (uniteEnseignement.getPole() == null && !dbPoles.isEmpty()) {
                                                System.out.println("Association de l'UE au premier pôle disponible: "
                                                        + dbPoles.get(0).getIntitule());
                                                uniteEnseignement.setPole(dbPoles.get(0));
                                            }
                                        }
                                    }

                                    // Sauvegarder l'UE avant de créer l'EM
                                    uniteEnseignement = uniteEnseignementRepository.save(uniteEnseignement);
                                    importedUEs.add(uniteEnseignement);

                                    // Vérifier si l'UE a été correctement associée à un pôle ou département
                                    if (uniteEnseignement.getPole() != null) {
                                        System.out.println("UE " + uniteEnseignement.getCodeUE() + " associée au pôle: "
                                                + uniteEnseignement.getPole().getIntitule());
                                    } else if (uniteEnseignement.getDepartement() != null) {
                                        System.out.println(
                                                "UE " + uniteEnseignement.getCodeUE() + " associée au département: "
                                                        + uniteEnseignement.getDepartement().getIntitule());
                                    } else {
                                        System.out.println("UE " + uniteEnseignement.getCodeUE()
                                                + " n'est associée ni à un pôle ni à un département!");
                                    }
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
                                    elementDeModule
                                            .setNombreCredits(getCellValueAsInt(row, columnIndices.get("credit")));
                                }

                                if (columnIndices.containsKey("coef")) {
                                    elementDeModule.setCoefficient(getCellValueAsFloat(row, columnIndices.get("coef")));
                                }

                                if (columnIndices.containsKey("cm")) {
                                    elementDeModule.setHeuresCM(getCellValueAsInt(row, columnIndices.get("cm")));
                                }

                                if (columnIndices.containsKey("td")) {
                                    elementDeModule.setHeuresTD(getCellValueAsInt(row, columnIndices.get("td")));
                                }

                                if (columnIndices.containsKey("tp")) {
                                    elementDeModule.setHeuresTP(getCellValueAsInt(row, columnIndices.get("tp")));
                                }

                                // Vérifier si l'EM existe déjà
                                Optional<ElementDeModule> existingEM = elementDeModuleRepository.findByCodeEM(code);
                                if (existingEM.isPresent()) {
                                    elementDeModule.setIdEM(existingEM.get().getIdEM());
                                }

                                // Vérifier si l'EM hérite correctement du pôle ou département de l'UE
                                if (elementDeModule.getUniteEnseignement() != null) {
                                    if (elementDeModule.getUniteEnseignement().getPole() != null) {
                                        System.out.println("EM " + code + " hérite du pôle: "
                                                + elementDeModule.getUniteEnseignement().getPole().getIntitule());
                                    } else if (elementDeModule.getUniteEnseignement().getDepartement() != null) {
                                        System.out.println("EM " + code + " hérite du département: " + elementDeModule
                                                .getUniteEnseignement().getDepartement().getIntitule());
                                    } else {
                                        System.out
                                                .println("EM " + code + " n'hérite ni d'un pôle ni d'un département!");
                                    }
                                }

                                ElementDeModule savedEM = elementDeModuleRepository.save(elementDeModule);
                                importedEMs.add(savedEM);
                                System.out.println("EM créé: " + savedEM.getCodeEM() + " - " + savedEM.getIntitule());
                            } else {
                                errors.add("Ligne " + (rowIndex + 1)
                                        + ": Impossible de déterminer l'UE pour l'élément de module");
                            }
                        } catch (Exception e) {
                            errors.add(
                                    "Erreur lors du traitement de la ligne " + (rowIndex + 1) + ": " + e.getMessage());
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

    // Classe pour stocker les informations extraites du code EM
    private static class EMCodeInfo {
        String departementCode;
        int semestreNum;

        public EMCodeInfo(String departementCode, int semestreNum) {
            this.departementCode = departementCode;
            this.semestreNum = semestreNum;
        }
    }

    // Méthode pour extraire les informations du code EM
    private EMCodeInfo extractEMCodeInfo(String code) {
        if (code == null || code.isEmpty()) {
            return new EMCodeInfo("inconnu", 0);
        }

        // Recherche de motif comme SID11, IRT24, etc.
        // Format: [lettres][chiffre pour semestre][chiffre(s) pour EM]
        Pattern pattern = Pattern.compile("([A-Za-z]+)(\\d)(\\d+)");
        Matcher matcher = pattern.matcher(code);

        if (matcher.find()) {
            String departementCode = matcher.group(1);
            int semestreNum = Integer.parseInt(matcher.group(2));

            return new EMCodeInfo(departementCode, semestreNum);
        }

        // Si le pattern ne correspond pas, essayons de faire de notre mieux
        // Extraire les lettres pour le département
        String departementCode = code.replaceAll("[^A-Za-z]", "");

        // Extraire les chiffres
        String numbers = code.replaceAll("[^0-9]", "");
        int semestreNum = 0;

        if (numbers.length() > 0) {
            // Premier chiffre = semestre
            semestreNum = Character.getNumericValue(numbers.charAt(0));
        }

        return new EMCodeInfo(departementCode, semestreNum);
    }

    public List<String> analyzeExcelFile(MultipartFile file) throws IOException {
        List<String> analysis = new ArrayList<>();

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            analysis.add("Nombre de feuilles : " + workbook.getNumberOfSheets());

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                analysis.add("Feuille " + sheetIndex + " : " + sheet.getSheetName());

                // Analyser les 10 premières lignes
                for (int rowIndex = 0; rowIndex <= Math.min(10, sheet.getLastRowNum()); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        StringBuilder rowContent = new StringBuilder("Ligne " + rowIndex + " : ");

                        for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                            Cell cell = row.getCell(colIndex);
                            if (cell != null && cell.getCellType() != CellType.BLANK) {
                                rowContent.append("[Col ").append(colIndex).append(": ");

                                switch (cell.getCellType()) {
                                    case STRING:
                                        rowContent.append(cell.getStringCellValue());
                                        break;
                                    case NUMERIC:
                                        rowContent.append(cell.getNumericCellValue());
                                        break;
                                    case BOOLEAN:
                                        rowContent.append(cell.getBooleanCellValue());
                                        break;
                                    default:
                                        rowContent.append("(autre type)");
                                }

                                rowContent.append("] ");
                            }
                        }

                        analysis.add(rowContent.toString());
                    }
                }

                analysis.add("---");
            }
        }

        return analysis;
    }

    private String getCellValueAsString(Row row, int cellIndex) {
        if (cellIndex < 0)
            return null;

        Cell cell = row.getCell(cellIndex);
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Utiliser toString au lieu de cast pour éviter les problèmes de décimales
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

    private int getCellValueAsInt(Row row, int cellIndex) {
        if (cellIndex < 0)
            return 0;

        Cell cell = row.getCell(cellIndex);
        if (cell == null)
            return 0;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    String value = cell.getStringCellValue().trim();
                    // Supprimer tout caractère non numérique sauf le point décimal
                    value = value.replaceAll("[^0-9.]", "");
                    if (value.isEmpty())
                        return 0;
                    return (int) Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }

    private float getCellValueAsFloat(Row row, int cellIndex) {
        if (cellIndex < 0)
            return 0f;

        Cell cell = row.getCell(cellIndex);
        if (cell == null)
            return 0f;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (float) cell.getNumericCellValue();
            case STRING:
                try {
                    String value = cell.getStringCellValue().trim();
                    // Supprimer tout caractère non numérique sauf le point décimal
                    value = value.replaceAll("[^0-9.]", "");
                    if (value.isEmpty())
                        return 0f;
                    return Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    return 0f;
                }
            default:
                return 0f;
        }
    }
}