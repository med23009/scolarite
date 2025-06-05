package com.esp.scolarite.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelImporter {
    
    // Classe pour stocker les informations extraites du code EM
    public static class EMCodeInfo {
        public String departementCode;
        public int semestreNum;
        public int emNum;
        
        public EMCodeInfo(String departementCode, int semestreNum, int emNum) {
            this.departementCode = departementCode;
            this.semestreNum = semestreNum;
            this.emNum = emNum;
        }
    }
    
    // Méthode pour extraire les informations du code EM
    public static EMCodeInfo extractEMCodeInfo(String code) {
        if (code == null || code.isEmpty()) {
            return new EMCodeInfo("inconnu", 0, 0);
        }
        
        // Recherche de motif comme SID11, IRT24, etc.
        // Format: [lettres][chiffre pour semestre][chiffre(s) pour EM]
        Pattern pattern = Pattern.compile("([A-Za-z]+)(\\d)(\\d+)");
        Matcher matcher = pattern.matcher(code);
        
        if (matcher.find()) {
            String departementCode = matcher.group(1);
            int semestreNum = Integer.parseInt(matcher.group(2));
            int emNum = Integer.parseInt(matcher.group(3));
            
            return new EMCodeInfo(departementCode, semestreNum, emNum);
        }
        
        // Si le pattern ne correspond pas, essayons de faire de notre mieux
        // Extraire les lettres pour le département
        String departementCode = code.replaceAll("[^A-Za-z]", "");
        
        // Extraire les chiffres
        String numbers = code.replaceAll("[^0-9]", "");
        int semestreNum = 0;
        int emNum = 0;
        
        if (numbers.length() > 0) {
            // Premier chiffre = semestre
            semestreNum = Character.getNumericValue(numbers.charAt(0));
            
            // Reste = numéro EM
            if (numbers.length() > 1) {
                try {
                    emNum = Integer.parseInt(numbers.substring(1));
                } catch (NumberFormatException e) {
                    emNum = 0;
                }
            }
        }
        
        return new EMCodeInfo(departementCode, semestreNum, emNum);
    }

    public static String getCellValueAsString(Row row, int cellIndex) {
        if (cellIndex < 0) return null;
        
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        
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

    public static int getCellValueAsInt(Row row, int cellIndex) {
        if (cellIndex < 0) return 0;
        
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return 0;
        
        switch (cell.getCellType()) {
            case NUMERIC: 
                return (int) cell.getNumericCellValue();
            case STRING: 
                try {
                    String value = cell.getStringCellValue().trim();
                    // Supprimer tout caractère non numérique sauf le point décimal
                    value = value.replaceAll("[^0-9.]", "");
                    if (value.isEmpty()) return 0;
                    return (int) Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    return 0;
                }
            default: 
                return 0;
        }
    }

    public static float getCellValueAsFloat(Row row, int cellIndex) {
        if (cellIndex < 0) return 0f;
        
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return 0f;
        
        switch (cell.getCellType()) {
            case NUMERIC: 
                return (float) cell.getNumericCellValue();
            case STRING: 
                try {
                    String value = cell.getStringCellValue().trim();
                    // Supprimer tout caractère non numérique sauf le point décimal
                    value = value.replaceAll("[^0-9.]", "");
                    if (value.isEmpty()) return 0f;
                    return Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    return 0f;
                }
            default: 
                return 0f;
        }
    }
    
    public static List<String> analyzeExcelFile(MultipartFile file) throws IOException {
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
}