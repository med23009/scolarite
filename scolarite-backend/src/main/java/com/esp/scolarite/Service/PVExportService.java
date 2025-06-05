package com.esp.scolarite.Service;

import com.esp.scolarite.entity.*;
import com.esp.scolarite.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class PVExportService {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private SemestreRepository semestreRepository;

    @Autowired
    private UniteEnseignementRepository ueRepository;

    @Autowired
    private ElementDeModuleRepository emRepository;

    @Autowired
    private NoteSemestrielleRepository noteRepository;

    public byte[] exportPvExcel(Long semestreId, String codeDep) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PV Semestre");

        // Styles
        CellStyle styleCenterBold = createStyle(workbook);
        CellStyle styleNormal = createNormalStyle(workbook); // Style sans couleur pour V
        CellStyle styleVCI = createStyle(workbook, IndexedColors.LIGHT_GREEN);
        CellStyle styleVCE = createStyle(workbook, IndexedColors.YELLOW);
        CellStyle styleE = createStyle(workbook, IndexedColors.DARK_RED);
        CellStyle styleNV = createStyle(workbook, IndexedColors.RED);
        CellStyle styleVerticalText = createVerticalTextStyle(workbook);
        CellStyle styleHeader = createHeaderStyle(workbook);
        CellStyle styleUEHeader = createUEHeaderStyle(workbook);
        CellStyle styleHEHeader = createHEHeaderStyle(workbook);
        CellStyle styleSTHeader = createSTHeaderStyle(workbook);
        CellStyle styleSessionHeader = createSessionHeaderStyle(workbook);

        Semestre semestre = semestreRepository.findById(semestreId).orElseThrow();
        Departement departement = departementRepository.findByCodeDep(codeDep).orElseThrow();

        List<Etudiant> etudiants = etudiantRepository.findByDepartement(departement)
        .stream()
        .sorted(Comparator.comparing(Etudiant::getMatricule))
        .toList();

        // Préparation des UE et EM
        List<UniteEnseignement> selectedUEs = new ArrayList<>();
        for (UniteEnseignement ue : ueRepository.findAll()) {
            if ((ue.getDepartement() != null && ue.getDepartement().equals(departement)) || ue.getPole() != null) {
                if (ue.getSemestre() != null && ue.getSemestre().getIdSemestre().equals(semestreId)) {
                    selectedUEs.add(ue);
                }
            }
        }
        
        // Regrouper les UEs par pôle
        Map<String, List<UniteEnseignement>> poleUEMap = new LinkedHashMap<>();
        for (UniteEnseignement ue : selectedUEs) {
            String pole = ue.getPole() != null ? ue.getPole().getCodePole() : ue.getDepartement().getCodeDep();
            if (!poleUEMap.containsKey(pole)) {
                poleUEMap.put(pole, new ArrayList<>());
            }
            poleUEMap.get(pole).add(ue);
        }
        
        Map<UniteEnseignement, List<ElementDeModule>> ueEmMap = new LinkedHashMap<>();
        for (UniteEnseignement ue : selectedUEs) {
            ueEmMap.put(ue, emRepository.findByUniteEnseignement(ue));
        }

        // Création des lignes d'en-tête
        Row rowSem = sheet.createRow(0);       // Ligne 1: Semestre + Pôles
        Row rowDept = sheet.createRow(1);      // Ligne 2: Département + UEs
        Row rowSession = sheet.createRow(2);   // Ligne 3: Session + Crédits
        Row rowCoef = sheet.createRow(3);      // Ligne 4: Coefficients
        Row rowEM = sheet.createRow(4);        // Ligne 5: EMs (vertical)
        
        // Cellules fixes pour les premières colonnes
        Cell cellSemLabel = rowSem.createCell(0);
        cellSemLabel.setCellValue("Semestre");
        cellSemLabel.setCellStyle(styleHeader);
        
        Cell cellSemValue = rowSem.createCell(1);
        cellSemValue.setCellValue(semestre.getSemestre());
        cellSemValue.setCellStyle(styleNormal);
        
        Cell cellDeptLabel = rowDept.createCell(0);
        cellDeptLabel.setCellValue("Departement");
        cellDeptLabel.setCellStyle(styleHeader);
        
        Cell cellDeptValue = rowDept.createCell(1);
        cellDeptValue.setCellValue(departement.getCodeDep());
        cellDeptValue.setCellStyle(styleNormal);
        
        Cell cellSessionLabel = rowSession.createCell(0);
        cellSessionLabel.setCellValue("Session de rattrapage");
        cellSessionLabel.setCellStyle(styleSessionHeader);
        
        // Créer les en-têtes fixes pour les colonnes Dept, Matricule, Prenom, Nom
        List<String> fixedHeaders = Arrays.asList("Dept", "Matricule", "Prenom", "Nom");
        for (int i = 0; i < fixedHeaders.size(); i++) {
            Cell cell = rowEM.createCell(i);
            cell.setCellValue(fixedHeaders.get(i));
            //cell.setCellStyle(styleHeader);
        }
        
        // Ajouter "Credit" et "Coef" dans les lignes 3 et 4
        Cell creditLabel = rowSession.createCell(3);
        creditLabel.setCellValue("Credit");
        //creditLabel.setCellStyle(styleHeader);
        
        Cell coefLabel = rowCoef.createCell(3);
        coefLabel.setCellValue("Coef");
        //coefLabel.setCellStyle(styleHeader);

        Map<ElementDeModule, Integer> emColIndex = new LinkedHashMap<>();
        Map<ElementDeModule, Integer> emMentionColIndex = new LinkedHashMap<>();
        Map<UniteEnseignement, Integer> ueMoyColIndex = new LinkedHashMap<>();
        Map<UniteEnseignement, List<Integer>> ueColSpan = new LinkedHashMap<>();

        int col = 4; // Commencer après les en-têtes fixes
        
        // Traitement des pôles et UEs
        LinkedHashMap<String, List<UniteEnseignement>> orderedPoleUEMap = new LinkedHashMap<>();
        if (poleUEMap.containsKey("HE")) orderedPoleUEMap.put("HE", poleUEMap.get("HE"));
        if (poleUEMap.containsKey("ST")) orderedPoleUEMap.put("ST", poleUEMap.get("ST"));
        for (Map.Entry<String, List<UniteEnseignement>> entry : poleUEMap.entrySet()) {
            if (!entry.getKey().equals("HE") && !entry.getKey().equals("ST")) {
                orderedPoleUEMap.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<String, List<UniteEnseignement>> poleEntry : orderedPoleUEMap.entrySet()) {
            String pole = poleEntry.getKey();
            List<UniteEnseignement> uesInPole = poleEntry.getValue();
            
            int poleStartCol = col;
            
            for (UniteEnseignement ue : uesInPole) {
                int ueStartCol = col;
                List<Integer> colSpans = new ArrayList<>();
                List<ElementDeModule> ems = ueEmMap.get(ue);
                
                // Créer les colonnes pour chaque EM
                for (ElementDeModule em : ems) {
                    // Colonne pour la note EM
                    Cell cellCredit = rowSession.createCell(col);
                    cellCredit.setCellValue(em.getNombreCredits());
                    cellCredit.setCellStyle(styleCenterBold);
                    
                    Cell cellCoef = rowCoef.createCell(col);
                    cellCoef.setCellValue(em.getNombreCredits());
                    cellCoef.setCellStyle(styleCenterBold);
                    
                    Cell cellEM = rowEM.createCell(col);
                    cellEM.setCellValue(em.getCodeEM() + em.getIntitule());
                    cellEM.setCellStyle(styleVerticalText);
                    
                    emColIndex.put(em, col);
                    colSpans.add(col);
                    col++;
                    
                    // Colonne pour la mention "Validé"
                    Cell cellValidLabel = rowEM.createCell(col);
                    cellValidLabel.setCellValue("EM Validé");
                    cellValidLabel.setCellStyle(styleVerticalText);
                    
                    rowSession.createCell(col).setCellValue("");
                    rowCoef.createCell(col).setCellValue("");
                    
                    emMentionColIndex.put(em, col);
                    colSpans.add(col);
                    col++;
                }
                
                // Colonne pour la moyenne UE
                Cell cellMoyUE = rowEM.createCell(col);
                cellMoyUE.setCellValue("MOYENNE UE");
                cellMoyUE.setCellStyle(styleVerticalText);
                
                rowSession.createCell(col).setCellValue("");
                rowCoef.createCell(col).setCellValue("");
                
                ueMoyColIndex.put(ue, col);
                colSpans.add(col);
                col++;
                
                ueColSpan.put(ue, colSpans);
                
                // Ajouter le nom de l'UE dans la ligne département (ligne 2)
                Cell cellUEName = rowDept.createCell(ueStartCol);
                cellUEName.setCellValue(ue.getCodeUE() + ": " + ue.getIntitule());
                cellUEName.setCellStyle(styleUEHeader);
                
                // Fusionner les cellules pour le nom de l'UE
                if (ueStartCol < col - 1) {
                    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, ueStartCol, col - 1));
                }
                
                // Appliquer le style à toutes les cellules fusionnées de l'UE
                for (int i = ueStartCol + 1; i < col; i++) {
                    Cell cell = rowDept.createCell(i);
                    cell.setCellStyle(styleUEHeader);
                }
            }
            
            // Fusion pour l'en-tête du pôle
            if (poleStartCol < col - 1) {
                // Ajouter le nom du pôle dans la ligne semestre
                Cell cellPoleName = rowSem.createCell(poleStartCol);
                
                // Déterminer le style en fonction du pôle
                CellStyle poleStyle;
                if (pole.equals("HE")) {
                    cellPoleName.setCellValue("HE");
                    poleStyle = styleHEHeader;
                } else if (pole.equals("ST")) {
                    cellPoleName.setCellValue("ST");
                    poleStyle = styleSTHeader;
                } else {
                    cellPoleName.setCellValue(pole);
                    poleStyle = styleHeader;
                }
                
                cellPoleName.setCellStyle(poleStyle);
                
                // Fusionner les cellules pour le nom du pôle
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, poleStartCol, col - 1));
                
                // Appliquer le style à toutes les cellules fusionnées
                for (int i = poleStartCol + 1; i < col; i++) {
                    Cell cell = rowSem.createCell(i);
                    cell.setCellStyle(poleStyle);
                }
            }
        }

        // Moyenne Semestre et Crédit Validé
        Cell moyenneSemestreCell = rowEM.createCell(col);
        moyenneSemestreCell.setCellValue("Moyenne du semestre");
        moyenneSemestreCell.setCellStyle(styleVerticalText);
        int moySemStart = col;
        col++;

        Cell creditsCell = rowEM.createCell(col);
        creditsCell.setCellValue("Crédits");
        creditsCell.setCellStyle(styleVerticalText);
        int creditsStart = col;
        col++;

        // Remplissage des étudiants
        int rowNum = 5;
        for (Etudiant etu : etudiants) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cellDept = row.createCell(0);
            cellDept.setCellValue(departement.getCodeDep());
            cellDept.setCellStyle(styleNormal);
            
            Cell cellMatricule = row.createCell(1);
            cellMatricule.setCellValue(etu.getMatricule());
            cellMatricule.setCellStyle(styleNormal);
            
            Cell cellPrenom = row.createCell(2);
            cellPrenom.setCellValue(etu.getPrenom());
            cellPrenom.setCellStyle(styleNormal);
            
            Cell cellNom = row.createCell(3);
            cellNom.setCellValue(etu.getNom());
            cellNom.setCellStyle(styleNormal);

            Map<ElementDeModule, Double> emMoyennes = new HashMap<>();
            Map<UniteEnseignement, Double> ueTotalWeighted = new HashMap<>();
            Map<UniteEnseignement, Double> ueTotalCredits = new HashMap<>();

            for (NoteSemestrielle note : noteRepository.findByEtudiantAndSemestre(etu, semestre)) {
                double moyenne = round2(0.4 * note.getNoteDevoir() + 0.6 * Math.max(note.getNoteRattrapage(), note.getNoteExamen()));
                ElementDeModule em = note.getElementModule();
                emMoyennes.put(em, moyenne);

                UniteEnseignement ue = em.getUniteEnseignement();
                ueTotalWeighted.put(ue, ueTotalWeighted.getOrDefault(ue, 0.0) + moyenne * em.getNombreCredits());
                ueTotalCredits.put(ue, ueTotalCredits.getOrDefault(ue, 0.0) + em.getNombreCredits());
            }

            // Calculer les moyennes UE d'abord
            Map<UniteEnseignement, Double> ueMoyennes = new HashMap<>();
            for (UniteEnseignement ue : ueMoyColIndex.keySet()) {
                double moyUE = 0;
                if (ueTotalCredits.getOrDefault(ue, 0.0) > 0) {
                    moyUE = round2(ueTotalWeighted.getOrDefault(ue, 0.0) / ueTotalCredits.getOrDefault(ue, 0.0));
                }
                ueMoyennes.put(ue, moyUE);
            }
            
            // Calculer la moyenne générale pondérée par les crédits
            double sommePonderee = 0;
            double totalCreditsPourMoyenne = 0;

            for (UniteEnseignement ue : ueMoyennes.keySet()) {
                double moyUE = ueMoyennes.get(ue);
                double creditsTotalUE = 0;
                
                // Calculer le total des crédits pour cette UE
                List<ElementDeModule> emsDeUE = ueEmMap.get(ue);
                if (emsDeUE != null) {
                    for (ElementDeModule em : emsDeUE) {
                        creditsTotalUE += em.getNombreCredits();
                    }
                }
                
                // Ajouter à la somme pondérée
                sommePonderee += moyUE * creditsTotalUE;
                totalCreditsPourMoyenne += creditsTotalUE;
            }

            // Diviser par 30 (total des crédits d'un semestre)
            double moyenneGenerale = (totalCreditsPourMoyenne > 0) ? round2(sommePonderee / 30) : 0;

            double totalCredits = 0;

            // Maintenant traiter les EM avec les moyennes UE déjà calculées
            for (ElementDeModule em : emColIndex.keySet()) {
                Double moyenneEM = emMoyennes.getOrDefault(em, 0.0);
                int colEM = emColIndex.get(em);
                int colMention = emMentionColIndex.get(em);
                UniteEnseignement ue = em.getUniteEnseignement();
                double moyenneUE = ueMoyennes.getOrDefault(ue, 0.0);

                Cell cellMoyenneEM = row.createCell(colEM);
                cellMoyenneEM.setCellValue(moyenneEM);
                cellMoyenneEM.setCellStyle(styleNormal);

                String mention = "";
                CellStyle styleMention = styleNormal;
                boolean valide = false;

                // Nouvelles règles de mention
                if (moyenneEM >= 10) {
                    // Si note EM supérieure à 10 alors V sans couleur
                    mention = "V";
                    valide = true;
                } 
                else if (moyenneEM < 6) {
                    // E si la note de EM inférieure à 6 couleur rouge
                    mention = "E";
                    styleMention = styleE;
                    valide = false;
                } else if (moyenneEM >= 6 && moyenneEM < 10 && moyenneUE >= 10) {
                    // VCI : note EM entre 6 et 10 et moyenne UE supérieure à 10 avec couleur vert
                    mention = "VCI";
                    styleMention = styleVCI;
                    valide = true;
                } else if (moyenneUE >= 8 && moyenneUE < 10 && moyenneGenerale >= 10) {
                    // VCE moyenne UE entre 8 et 10 et moyenne générale supérieure à 10 avec couleur jaune
                    mention = "VCE";
                    styleMention = styleVCE;
                    valide = true;
                } else {
                    mention = "NV";
                    styleMention = styleNV;
                    valide = false;
                }

                if (valide) {
                    totalCredits += em.getNombreCredits();
                }

                Cell cellMention = row.createCell(colMention);
                cellMention.setCellValue(mention);
                cellMention.setCellStyle(styleMention);
            }

            // Afficher les moyennes UE
            for (UniteEnseignement ue : ueMoyColIndex.keySet()) {
                double moyUE = ueMoyennes.getOrDefault(ue, 0.0);
                Cell cellMoyUE = row.createCell(ueMoyColIndex.get(ue));
                cellMoyUE.setCellValue(moyUE);
                cellMoyUE.setCellStyle(styleNormal);
            }

            // Moyenne semestre et crédits
            Cell cellMoyGen = row.createCell(moySemStart);
            cellMoyGen.setCellValue(moyenneGenerale);
            cellMoyGen.setCellStyle(styleNormal);
            
            Cell cellCredits = row.createCell(creditsStart);
            double creditsFinal = totalCredits;
            if (creditsFinal == 30) {
                cellCredits.setCellStyle(createCreditsValidesStyle(workbook));
                cellCredits.setCellValue(creditsFinal);
            } else {
                cellCredits.setCellStyle(styleNormal);
                cellCredits.setCellValue(creditsFinal);
            }
        }

        // Ajuster la largeur des colonnes
        for (int i = 0; i <= col; i++) {
            sheet.autoSizeColumn(i);
        }

        // Appliquer des bordures à toutes les cellules
        for (int i = 0; i <= rowNum; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < col; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) {
                        cell = row.createCell(j);
                        cell.setCellStyle(styleNormal);
                    }
                }
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return bos.toByteArray();
    }

    private CellStyle createStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        //style.setAlignment(HorizontalAlignment.CENTER);
        //style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        //style.setAlignment(HorizontalAlignment.CENTER);
        //style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = createStyle(workbook);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createUEHeaderStyle(Workbook workbook) {
        CellStyle style = createStyle(workbook);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private CellStyle createHEHeaderStyle(Workbook workbook) {
        CellStyle style = createStyle(workbook);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createSTHeaderStyle(Workbook workbook) {
        CellStyle style = createStyle(workbook);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createSessionHeaderStyle(Workbook workbook) {
        CellStyle style = createStyle(workbook);
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = createStyle(workbook);
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createVerticalTextStyle(Workbook workbook) {
        CellStyle style = createStyle(workbook);
        style.setRotation((short) 90);
        return style;
    }

    private CellStyle createCreditsValidesStyle(Workbook workbook) {
        CellStyle style = createNormalStyle(workbook);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());       
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);        
        return style;      
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
