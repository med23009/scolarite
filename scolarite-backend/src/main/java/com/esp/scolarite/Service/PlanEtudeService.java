package com.esp.scolarite.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esp.scolarite.dto.PlanEtudeDTO;
import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.ElementDeModule;
import com.esp.scolarite.entity.Etudiant;
import com.esp.scolarite.entity.NoteSemestrielle;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.entity.UniteEnseignement;
import com.esp.scolarite.repository.ElementDeModuleRepository;
import com.esp.scolarite.repository.EtudiantRepository;
import com.esp.scolarite.repository.NoteSemestrielleRepository;
import com.esp.scolarite.repository.SemestreRepository;

@Service
public class PlanEtudeService {
    private static final Logger logger = LoggerFactory.getLogger(PlanEtudeService.class);

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private SemestreRepository semestreRepository;

    @Autowired
    private ElementDeModuleRepository elementDeModuleRepository;

    @Autowired
    private NoteSemestrielleRepository noteSemestrielleRepository;
    

    /**
     * Récupère les éléments de modules non validés pour un étudiant et un semestre donnés
     * @param matricule Matricule de l'étudiant
     * @param semestreId ID du semestre
     * @return DTO contenant les éléments de modules non validés et le nombre total de crédits
     */
    public PlanEtudeDTO getPlanEtude(String matricule, Long semestreId) {
        try {
            // Récupérer l'étudiant
            logger.info("Recherche de l'étudiant avec matricule: {}", matricule);
            Etudiant etudiant = etudiantRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec le matricule: " + matricule));
            logger.info("Étudiant trouvé: {} {}", etudiant.getNom(), etudiant.getPrenom());
            
            // Récupérer le département de l'étudiant
            Departement departementEtudiant = etudiant.getDepartement();
            if (departementEtudiant == null) {
                logger.warn("L'étudiant {} n'est associé à aucun département", matricule);
                throw new RuntimeException("L'étudiant n'est associé à aucun département");
            }
            logger.info("Département de l'étudiant: {}", departementEtudiant.getCodeDep());

            // Récupérer le semestre
            logger.info("Recherche du semestre avec ID: {}", semestreId);
            Semestre semestre = semestreRepository.findById(semestreId)
                    .orElseThrow(() -> new RuntimeException("Semestre non trouvé avec l'ID: " + semestreId));
            logger.info("Semestre trouvé: {}", semestre.getSemestre());

            // Extraire le numéro de semestre
            int semestreNumber = extractSemestreNumber(semestre.getSemestre());
            logger.info("Numéro de semestre: {}", semestreNumber);
            
            // Vérifier si on demande S3 ou S4 (dans ce cas, vérifier les crédits non validés de S1+S2)
            if (semestreNumber == 3 || semestreNumber == 4) {
                // Calcul des crédits non validés de S1 et S2
                int creditsNonValidesS1S2 = calculerCreditsNonValidesS1S2(etudiant);
                logger.info("Crédits non validés de S1+S2: {}", creditsNonValidesS1S2);
                
                // Si plus de 15 crédits non validés, l'étudiant ne peut pas accéder à S3/S4
                if (creditsNonValidesS1S2 >= 15) {
                    PlanEtudeDTO planEtudeDTO = new PlanEtudeDTO();
                    planEtudeDTO.setMatricule(matricule);
                    planEtudeDTO.setNomEtudiant(etudiant.getNom());
                    planEtudeDTO.setPrenomEtudiant(etudiant.getPrenom());
                    planEtudeDTO.setSemestre(semestre.getSemestre());
                    planEtudeDTO.setDepartementCode(departementEtudiant.getCodeDep());
                    planEtudeDTO.setDepartementNom(departementEtudiant.getIntitule());
                    planEtudeDTO.setTotalCredits(creditsNonValidesS1S2);
                    planEtudeDTO.setSemestreBloque(true);
                    planEtudeDTO.setMessageBloquage("Accès au semestre " + semestre.getSemestre() + 
                                                   " bloqué: cet étudiant a " + creditsNonValidesS1S2 + 
                                                   " crédits non validés de S1+S2 (maximum autorisé: 14 crédits)");
                    return planEtudeDTO;
                }
            }
            
            // Déterminer les semestres à inclure dans le plan d'étude
            List<Integer> semestresAInclure = new ArrayList<>();
            semestresAInclure.add(semestreNumber);
            
            // Logique d'inclusion des semestres précédents
            // Si S3, inclure aussi S1; Si S4, inclure aussi S2
            if (semestreNumber == 3) {
                semestresAInclure.add(1);
                logger.info("Pour S3, on inclut également S1");
            } else if (semestreNumber == 4) {
                semestresAInclure.add(2);
                logger.info("Pour S4, on inclut également S2");
            }
            
            logger.info("Semestres à inclure dans le plan d'étude: {}", semestresAInclure);
            
            // Récupérer tous les modules pertinents pour l'étudiant
            List<ElementDeModule> modulesForSemestre = new ArrayList<>();
            
            try {
                // Pour chaque semestre à inclure
                for (int currentSemestreNumber : semestresAInclure) {
                    logger.info("Recherche des modules pour le semestre numéro: {}", currentSemestreNumber);
                    
                    // 1. Récupérer tous les modules du semestre courant
                    List<ElementDeModule> modulesForCurrentSemestre = elementDeModuleRepository.findBySemestre(currentSemestreNumber);
                    logger.info("Nombre total de modules trouvés pour le semestre {}: {}", 
                        currentSemestreNumber, modulesForCurrentSemestre.size());
                    
                    // 2. Filtrer les modules qui appartiennent:
                    // - Au département de l'étudiant
                    // - Aux pôles communs (HE et ST)
                    List<ElementDeModule> filteredModulesForCurrentSemestre = modulesForCurrentSemestre.stream()
                        .filter(module -> {
                            if (module.getUniteEnseignement() == null) {
                                return false;
                            }
                            
                            UniteEnseignement ue = module.getUniteEnseignement();
                            
                            // Ajout de log pour debugging GC
                            if ("GC".equals(departementEtudiant.getCodeDep())) {
                                logger.info("Évaluation du module {} pour étudiant GC", module.getCodeEM());
                                if (ue.getDepartement() != null) {
                                    logger.info("Module {} appartient au département {}", 
                                        module.getCodeEM(), ue.getDepartement().getCodeDep());
                                }
                            }
                            
                            // Appartient au département de l'étudiant
                            if (ue.getDepartement() != null && 
                                ue.getDepartement().getCodeDep().equals(departementEtudiant.getCodeDep())) {
                                return true;
                            }
                            
                            // Cas spécial pour GC-HE (Génie Civil avec modules de HE)
                            if ("GC".equals(departementEtudiant.getCodeDep())) {
                                // Inclure tous les modules HE pour les étudiants GC
                                if (ue.getPole() != null && "HE".equals(ue.getPole().getCodePole())) {
                                    logger.info("Inclusion du module HE {} pour l'étudiant GC", module.getCodeEM());
                                    return true;
                                }
                            }
                            
                            // Appartient à un pôle (HE ou ST)
                            if (ue.getPole() != null) {
                                String poleCode = ue.getPole().getCodePole();
                                return "HE".equals(poleCode) || "ST".equals(poleCode);
                            }
                            
                            return false;
                        })
                        .collect(Collectors.toList());
                    
                    logger.info("Nombre de modules pertinents pour le semestre {} (département {} et pôles HE/ST): {}", 
                        currentSemestreNumber, departementEtudiant.getCodeDep(), filteredModulesForCurrentSemestre.size());
                    
                    // Ajouter à la liste globale
                    modulesForSemestre.addAll(filteredModulesForCurrentSemestre);
                }
                
                logger.info("Nombre total de modules pertinents trouvés pour tous les semestres: {}", 
                    modulesForSemestre.size());
                
            } catch (NumberFormatException e) {
                logger.warn("Impossible de convertir le semestre en nombre, utilisation de l'ID du semestre");
                modulesForSemestre = Collections.emptyList();
            }
            
            if (modulesForSemestre.isEmpty()) {
                logger.warn("Aucun module pertinent trouvé pour l'étudiant {} aux semestres inclus", 
                    matricule);
            }

            // Récupérer toutes les notes de l'étudiant pour ce semestre et les semestres précédents inclus
            List<NoteSemestrielle> allNotes = new ArrayList<>();
            for (Semestre s : semestreRepository.findAll()) {
                if (semestresAInclure.contains(extractSemestreNumber(s.getSemestre()))) {
                    NoteSemestrielle[] notesForSemestre = noteSemestrielleRepository.findByEtudiantAndSemestre(etudiant, s);
                    if (notesForSemestre != null && notesForSemestre.length > 0) {
                        for (NoteSemestrielle note : notesForSemestre) {
                            allNotes.add(note);
                        }
                        logger.info("Ajout de {} notes du semestre {}", notesForSemestre.length, s.getSemestre());
                    } else {
                        logger.info("Aucune note trouvée pour l'étudiant {} au semestre {}", etudiant.getMatricule(), s.getSemestre());
                    }
                }
            }
            
            logger.info("Nombre total de notes trouvées pour tous les semestres inclus: {}", allNotes.size());

            // Grouper les notes par unité d'enseignement pour calculer les moyennes
            Map<String, List<NoteSemestrielle>> notesParUE = allNotes.stream()
                    .filter(note -> note.getElementModule() != null && note.getElementModule().getUniteEnseignement() != null)
                    .collect(Collectors.groupingBy(note -> note.getElementModule().getCodeEU()));
            
            // Calculer les moyennes par UE
            Map<String, Double> moyennesUE = new HashMap<>();
            for (Map.Entry<String, List<NoteSemestrielle>> entry : notesParUE.entrySet()) {
                String codeUE = entry.getKey();
                List<NoteSemestrielle> notesUE = entry.getValue();
                
                double sommeCredits = notesUE.stream()
                        .mapToDouble(note -> note.getElementModule().getNombreCredits())
                        .sum();
                
                double sommeNotes = notesUE.stream()
                        .mapToDouble(note -> note.getNoteGenerale() * note.getElementModule().getNombreCredits())
                        .sum();
                
                double moyenneUE = sommeCredits > 0 ? sommeNotes / sommeCredits : 0;
                moyennesUE.put(codeUE, moyenneUE);
                logger.info("Moyenne UE {}: {}", codeUE, moyenneUE);
            }
            
            // Calculer la moyenne générale
            double moyenneGenerale = 0;
            if (!allNotes.isEmpty()) {
                double sommeCreditsTotale = allNotes.stream()
                        .filter(note -> note.getElementModule() != null)
                        .mapToDouble(note -> note.getElementModule().getNombreCredits())
                        .sum();
                
                double sommeNotesTotale = allNotes.stream()
                        .filter(note -> note.getElementModule() != null)
                        .mapToDouble(note -> note.getNoteGenerale() * note.getElementModule().getNombreCredits())
                        .sum();
                
                moyenneGenerale = sommeCreditsTotale > 0 ? sommeNotesTotale / sommeCreditsTotale : 0;
                logger.info("Moyenne générale: {}", moyenneGenerale);
            }
            
            // Identifier les modules validés en prenant en compte les différentes règles
            Map<String, String> modulesValidationStatus = new HashMap<>();
            List<String> validatedModuleCodes = new ArrayList<>();
            
            for (NoteSemestrielle note : allNotes) {
                if (note.getElementModule() == null) continue;
                
                String codeEM = note.getElementModule().getCodeEM();
                String codeUE = note.getElementModule().getCodeEU();
                double noteEM = note.getNoteGenerale();
                double moyenneUE = moyennesUE.getOrDefault(codeUE, 0.0);
                
                // Règle 1: Note directe >= 10
                if (noteEM >= 10) {
                    validatedModuleCodes.add(codeEM);
                    modulesValidationStatus.put(codeEM, "V");
                    logger.info("Module {} validé directement avec note {}", codeEM, noteEM);
                }
                // Règle 2: Compensation interne - Note >= 6 et moyenne UE >= 10
                else if (noteEM >= 6 && moyenneUE >= 10) {
                    validatedModuleCodes.add(codeEM);
                    modulesValidationStatus.put(codeEM, "VCI");
                    logger.info("Module {} validé par compensation interne, note: {}, moyenne UE: {}", codeEM, noteEM, moyenneUE);
                }
                // Règle 3: Compensation externe - Note >= 6, moyenne UE >= 8 et moyenne générale >= 10
                else if (noteEM >= 6 && moyenneUE >= 8 && moyenneGenerale >= 10) {
                    validatedModuleCodes.add(codeEM);
                    modulesValidationStatus.put(codeEM, "VCE");
                    logger.info("Module {} validé par compensation externe, note: {}, moyenne UE: {}, moyenne générale: {}", 
                        codeEM, noteEM, moyenneUE, moyenneGenerale);
                }
                // Module non validé
                else {
                    modulesValidationStatus.put(codeEM, "NV");
                    logger.info("Module {} non validé, note: {}", codeEM, noteEM);
                }
            }
            
            // Filtrer les modules non validés
            List<ElementDeModule> modulesNonValides = modulesForSemestre.stream()
                    .filter(module -> !validatedModuleCodes.contains(module.getCodeEM()))
                    .collect(Collectors.toList());
            logger.info("Nombre de modules non validés: {}", modulesNonValides.size());

            // Log détaillé pour vérification
            logger.info("Modules validés: {}", validatedModuleCodes);
            logger.info("Modules dans modulesForSemestre: {}", 
                modulesForSemestre.stream().map(ElementDeModule::getCodeEM).collect(Collectors.toList()));
            logger.info("Modules non validés après filtrage: {}", 
                modulesNonValides.stream().map(ElementDeModule::getCodeEM).collect(Collectors.toList()));

            // Calculer le nombre total de crédits pour les modules non validés
            int totalCredits = (int) modulesNonValides.stream()
                    .mapToDouble(ElementDeModule::getNombreCredits)
                    .sum();
            logger.info("Total des crédits non validés: {}", totalCredits);

            // Regrouper les modules non validés par unité d'enseignement
            Map<String, List<ElementDeModule>> modulesParUE = modulesNonValides.stream()
                    .collect(Collectors.groupingBy(ElementDeModule::getCodeEU));

            // Créer et remplir le DTO
            PlanEtudeDTO planEtudeDTO = new PlanEtudeDTO();
            planEtudeDTO.setMatricule(matricule);
            planEtudeDTO.setNomEtudiant(etudiant.getNom());
            planEtudeDTO.setPrenomEtudiant(etudiant.getPrenom());
            planEtudeDTO.setSemestre(semestre.getSemestre());
            planEtudeDTO.setTotalCredits(totalCredits);
            planEtudeDTO.setDepartementCode(departementEtudiant.getCodeDep());
            planEtudeDTO.setDepartementNom(departementEtudiant.getIntitule());
            planEtudeDTO.setModulesNonValides(modulesNonValides);
            planEtudeDTO.setModulesParUE(modulesParUE);
            
            // Ajouter les notes pour chaque module
            Map<String, Double> notesParModule = new HashMap<>();
            // Récupérer toutes les notes de l'étudiant pour les modules concernés
            for (NoteSemestrielle note : allNotes) {
                if (note.getElementModule() != null) {
                    String codeEM = note.getElementModule().getCodeEM();
                    double noteGenerale = note.getNoteGenerale();
                    notesParModule.put(codeEM, noteGenerale);
                    logger.info("Note pour le module {}: {}", codeEM, noteGenerale);
                }
            }
            planEtudeDTO.setNotesModules(notesParModule);
            planEtudeDTO.setValidationStatuts(modulesValidationStatus);

            return planEtudeDTO;
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du plan d'étude", e);
            throw new RuntimeException("Erreur lors de la génération du plan d'étude: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extrait le numéro de semestre à partir d'une chaîne de caractères
     * @param semestreString Chaîne contenant le numéro de semestre (ex: "S3 - 4")
     * @return Le numéro de semestre
     */
    private int extractSemestreNumber(String semestreString) {
        try {
            // Si c'est juste un nombre
            return Integer.parseInt(semestreString);
        } catch (NumberFormatException e) {
            // Si c'est au format "S3 - 4", extraire le chiffre après le S
            if (semestreString.startsWith("S") && semestreString.length() > 1) {
                try {
                    // Prendre le premier chiffre après le S
                    return Integer.parseInt(semestreString.substring(1, 2));
                } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
                    // Si format inconnu, lancer exception
                    throw new NumberFormatException("Format de semestre non reconnu: " + semestreString);
                }
            }
            // Si aucun format reconnu
            throw new NumberFormatException("Format de semestre non reconnu: " + semestreString);
        }
    }

    /**
     * Calcule le nombre total de crédits non validés pour les semestres S1 et S2
     * @param etudiant L'étudiant concerné
     * @return Le nombre total de crédits non validés pour S1 et S2
     */
    private int calculerCreditsNonValidesS1S2(Etudiant etudiant) {
        try {
            logger.info("Calcul des crédits non validés de S1+S2 pour l'étudiant: {}", etudiant.getMatricule());
            
            // Trouver les semestres S1 et S2
            List<Semestre> semestresS1S2 = new ArrayList<>();
            for (Semestre s : semestreRepository.findAll()) {
                int semNum = extractSemestreNumber(s.getSemestre());
                if (semNum == 1 || semNum == 2) {
                    semestresS1S2.add(s);
                    logger.info("Semestre trouvé pour le calcul: {}", s.getSemestre());
                }
            }
            
            if (semestresS1S2.isEmpty()) {
                logger.warn("Aucun semestre S1 ou S2 trouvé dans la base de données");
                return 0;
            }
            
            // Récupérer tous les modules de S1 et S2 pertinents pour l'étudiant
            List<ElementDeModule> modulesS1S2 = new ArrayList<>();
            for (int semNum : Arrays.asList(1, 2)) {
                List<ElementDeModule> modulesForSemestre = elementDeModuleRepository.findBySemestre(semNum);
                logger.info("Nombre de modules trouvés pour S{}: {}", semNum, modulesForSemestre.size());
                
                // Filtrer pour ne garder que les modules pertinents pour l'étudiant
                List<ElementDeModule> filteredModules = modulesForSemestre.stream()
                    .filter(module -> {
                        if (module.getUniteEnseignement() == null) {
                            return false;
                        }
                        
                        UniteEnseignement ue = module.getUniteEnseignement();
                        
                        // Appartient au département de l'étudiant
                        if (ue.getDepartement() != null && 
                            ue.getDepartement().getCodeDep().equals(etudiant.getDepartement().getCodeDep())) {
                            return true;
                        }
                        
                        // Cas spécial pour GC-HE
                        if ("GC".equals(etudiant.getDepartement().getCodeDep())) {
                            if (ue.getPole() != null && "HE".equals(ue.getPole().getCodePole())) {
                                return true;
                            }
                        }
                        
                        // Appartient à un pôle (HE ou ST)
                        if (ue.getPole() != null) {
                            String poleCode = ue.getPole().getCodePole();
                            return "HE".equals(poleCode) || "ST".equals(poleCode);
                        }
                        
                        return false;
                    })
                    .collect(Collectors.toList());
                
                modulesS1S2.addAll(filteredModules);
                logger.info("Nombre de modules filtrés pour S{}: {}", semNum, filteredModules.size());
            }
            
            // Récupérer les notes de l'étudiant pour S1 et S2
            List<NoteSemestrielle> notesS1S2 = new ArrayList<>();
            for (Semestre s : semestresS1S2) {
                notesS1S2.addAll(Arrays.asList(noteSemestrielleRepository.findByEtudiantAndSemestre(etudiant, s)));
            }
            logger.info("Nombre de notes trouvées pour S1+S2: {}", notesS1S2.size());
            
            // Grouper les notes par unité d'enseignement pour calculer les moyennes
            Map<String, List<NoteSemestrielle>> notesParUE = notesS1S2.stream()
                    .filter(note -> note.getElementModule() != null && note.getElementModule().getUniteEnseignement() != null)
                    .collect(Collectors.groupingBy(note -> note.getElementModule().getCodeEU()));
            
            // Calculer les moyennes par UE
            Map<String, Double> moyennesUE = new HashMap<>();
            for (Map.Entry<String, List<NoteSemestrielle>> entry : notesParUE.entrySet()) {
                String codeUE = entry.getKey();
                List<NoteSemestrielle> notesUE = entry.getValue();
                
                double sommeCredits = notesUE.stream()
                        .mapToDouble(note -> note.getElementModule().getNombreCredits())
                        .sum();
                
                double sommeNotes = notesUE.stream()
                        .mapToDouble(note -> note.getNoteGenerale() * note.getElementModule().getNombreCredits())
                        .sum();
                
                double moyenneUE = sommeCredits > 0 ? sommeNotes / sommeCredits : 0;
                moyennesUE.put(codeUE, moyenneUE);
                logger.info("Moyenne UE {}: {}", codeUE, moyenneUE);
            }
            
            // Calculer la moyenne générale
            double moyenneGenerale = 0;
            if (!notesS1S2.isEmpty()) {
                double sommeCreditsTotale = notesS1S2.stream()
                        .filter(note -> note.getElementModule() != null)
                        .mapToDouble(note -> note.getElementModule().getNombreCredits())
                        .sum();
                
                double sommeNotesTotale = notesS1S2.stream()
                        .filter(note -> note.getElementModule() != null)
                        .mapToDouble(note -> note.getNoteGenerale() * note.getElementModule().getNombreCredits())
                        .sum();
                
                moyenneGenerale = sommeCreditsTotale > 0 ? sommeNotesTotale / sommeCreditsTotale : 0;
                logger.info("Moyenne générale S1+S2: {}", moyenneGenerale);
            }
            
            // Identifier les modules validés en prenant en compte les différentes règles
            List<String> validatedModuleCodes = new ArrayList<>();
            
            for (NoteSemestrielle note : notesS1S2) {
                if (note.getElementModule() == null) continue;
                
                String codeEM = note.getElementModule().getCodeEM();
                String codeUE = note.getElementModule().getCodeEU();
                double noteEM = note.getNoteGenerale();
                double moyenneUE = moyennesUE.getOrDefault(codeUE, 0.0);
                
                // Règle 1: Note directe >= 10
                if (noteEM >= 10) {
                    validatedModuleCodes.add(codeEM);
                    logger.info("Module {} validé directement avec note {}", codeEM, noteEM);
                }
                // Règle 2: Compensation interne - Note >= 6 et moyenne UE >= 10
                else if (noteEM >= 6 && moyenneUE >= 10) {
                    validatedModuleCodes.add(codeEM);
                    logger.info("Module {} validé par compensation interne, note: {}, moyenne UE: {}", codeEM, noteEM, moyenneUE);
                }
                // Règle 3: Compensation externe - Note >= 6, moyenne UE >= 8 et moyenne générale >= 10
                else if (noteEM >= 6 && moyenneUE >= 8 && moyenneGenerale >= 10) {
                    validatedModuleCodes.add(codeEM);
                    logger.info("Module {} validé par compensation externe, note: {}, moyenne UE: {}, moyenne générale: {}", 
                        codeEM, noteEM, moyenneUE, moyenneGenerale);
                }
                // Module non validé
                else {
                    logger.info("Module {} non validé, note: {}", codeEM, noteEM);
                }
            }
            
            logger.info("Modules validés en S1+S2: {}", validatedModuleCodes);
            
            // Filtrer les modules non validés
            List<ElementDeModule> modulesNonValides = modulesS1S2.stream()
                .filter(module -> !validatedModuleCodes.contains(module.getCodeEM()))
                .collect(Collectors.toList());
            logger.info("Nombre de modules non validés en S1+S2: {}", modulesNonValides.size());
            
            // Calculer le nombre total de crédits pour les modules non validés
            int totalCredits = (int) modulesNonValides.stream()
                .mapToDouble(ElementDeModule::getNombreCredits)
                .sum();
            logger.info("Total des crédits non validés en S1+S2: {}", totalCredits);
            
            return totalCredits;
            
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des crédits non validés S1+S2", e);
            return 0; // En cas d'erreur, on retourne 0 pour ne pas bloquer l'accès
        }
    }
} 