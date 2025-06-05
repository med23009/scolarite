package com.esp.scolarite.Controller.chefdept;


import com.esp.scolarite.Service.admin.HistoriqueService;
import com.esp.scolarite.Service.programme.ProgrammeServicedept;
import com.esp.scolarite.Config.JwtService;
import com.esp.scolarite.dto.ElementDeModuleDTO;
import com.esp.scolarite.dto.UniteEnseignementDTO;
import com.esp.scolarite.entity.ElementDeModule;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.entity.UniteEnseignement;
import com.esp.scolarite.repository.SemestreRepository;
//import com.esp.scolarite.util.SemestreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController("chefDeptProgrammeController")
@RequestMapping("/api/chef-dept/programmes")
@PreAuthorize("hasRole('CHEF_DEPT') or @jwtService.hasRoleFromToken(authentication, 'CHEF_DEPT')")
public class ProgrammeController {

    @Autowired
    private SemestreRepository semestreRepository;

    @Autowired
    private ProgrammeServicedept programmeServiceDept;

    @Autowired
    private HistoriqueService historiqueService;

    @Autowired
    private JwtService jwtService;

    // Unités d'Enseignement spécifiques au département
    @GetMapping("/ue")
    public ResponseEntity<List<UniteEnseignementDTO>> getDepartementUniteEnseignements(
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        List<UniteEnseignement> ues = programmeServiceDept.getDepartmentUniteEnseignements(email);
        List<UniteEnseignementDTO> ueDTOs = ues.stream()
                .map(this::convertToUeDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ueDTOs);
    }

    @GetMapping("/ue/{id}")
    public ResponseEntity<UniteEnseignement> getUniteEnseignementById(@PathVariable Long id) {
        return programmeServiceDept.getUniteEnseignementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ue")
    public ResponseEntity<UniteEnseignement> createUniteEnseignement(
            @RequestBody UniteEnseignement ue,
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        // Associer l'UE au département du chef de département
        UniteEnseignement saved = programmeServiceDept.associateUEWithDepartment(ue, email);
        historiqueService.enregistrerAction(email, "Ajout UE", "Ajout UE : " + ue.getIntitule());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/ue/{id}")
    public ResponseEntity<UniteEnseignement> updateUniteEnseignement(
            @PathVariable Long id,
            @RequestBody UniteEnseignement ue,
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        return programmeServiceDept.getUniteEnseignementById(id)
                .map(existingUE -> {
                    ue.setIdUE(id);
                    // Associer l'UE au département du chef de département
                    UniteEnseignement updated = programmeServiceDept.associateUEWithDepartment(ue, email);
                    historiqueService.enregistrerAction(email, "Modification UE", "Modification de l'UE : " + ue.getIntitule());
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/ue/{id}")
    public ResponseEntity<Void> deleteUniteEnseignement(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        return programmeServiceDept.getUniteEnseignementById(id)
                .map(ue -> {
                    programmeServiceDept.deleteUniteEnseignement(id);
                    historiqueService.enregistrerAction(email, "Suppression UE", "Suppression de l'UE : " + ue.getIntitule());
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Éléments de Module spécifiques au département
    @GetMapping("/em")
    public ResponseEntity<List<ElementDeModuleDTO>> getDepartementElementsDeModule(
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        
        // Utiliser le service spécifique au département pour récupérer les EMs
        List<ElementDeModule> ems = programmeServiceDept.getDepartmentElementsDeModule(email);
        
        // Convertir en DTOs
        List<ElementDeModuleDTO> emDTOs = ems.stream()
                .map(this::convertToEmDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(emDTOs);
    }

    @GetMapping("/em/{id}")
    public ResponseEntity<ElementDeModule> getElementDeModuleById(@PathVariable Long id) {
        return programmeServiceDept.getElementDeModuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/em")
    public ResponseEntity<ElementDeModuleDTO> createElementDeModule(
            @RequestBody ElementDeModuleDTO emDTO,
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        
        // Convert DTO to entity
        ElementDeModule em = new ElementDeModule();
        em.setCodeEM(emDTO.getCodeEM());
        em.setIntitule(emDTO.getIntitule());
        em.setCodeEU(emDTO.getCodeEU());
        em.setNombreCredits(emDTO.getNombreCredits());
        em.setCoefficient(emDTO.getCoefficient());
        em.setSemestre(emDTO.getSemestre());
        em.setHeuresCM(emDTO.getHeuresCM());
        em.setHeuresTD(emDTO.getHeuresTD());
        em.setHeuresTP(emDTO.getHeuresTP());
        // Do not set responsableEM from string value
        
        // Find and associate with the UE based on codeEU
        if (emDTO.getCodeEU() != null && !emDTO.getCodeEU().isEmpty()) {
            programmeServiceDept.getUniteEnseignementByCode(emDTO.getCodeEU())
                .ifPresent(em::setUniteEnseignement);
        }
        
        // Find and associate with the Semestre if needed
        if (emDTO.getSemestre() > 0) {
            programmeServiceDept.getSemestreByNumero(emDTO.getSemestre())
                .ifPresent(em::setId_semestre);
        }

        if (emDTO.getIdSemestre() != null) {
            semestreRepository.findById(emDTO.getIdSemestre())
                .ifPresent(em::setId_semestre);
        }

        
        ElementDeModule saved = programmeServiceDept.saveElementDeModule(em, email);
        historiqueService.enregistrerAction(email, "Ajout EM", "Ajout EM : " + em.getIntitule());
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToEmDTO(saved));
    }

    @PutMapping("/em/{id}")
    public ResponseEntity<ElementDeModuleDTO> updateElementDeModule(
            @PathVariable Long id,
            @RequestBody ElementDeModuleDTO emDTO,
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        return programmeServiceDept.getElementDeModuleById(id)
                .map(existingEM -> {
                    // Convert DTO to entity
                    existingEM.setIdEM(id);
                    existingEM.setCodeEU(emDTO.getCodeEU());
                    existingEM.setCodeEM(emDTO.getCodeEM());
                    existingEM.setIntitule(emDTO.getIntitule());
                    existingEM.setNombreCredits(emDTO.getNombreCredits());
                    existingEM.setCoefficient(emDTO.getCoefficient());
                    existingEM.setSemestre(emDTO.getSemestre());
                    existingEM.setHeuresCM(emDTO.getHeuresCM());
                    existingEM.setHeuresTD(emDTO.getHeuresTD());
                    existingEM.setHeuresTP(emDTO.getHeuresTP());
                    // Don't update responsableEM from DTO string
                    
                    ElementDeModule updated = programmeServiceDept.saveElementDeModule(existingEM, email);
                    historiqueService.enregistrerAction(email, "Modification EM", "Modification de l'EM : " + emDTO.getIntitule());
                    return ResponseEntity.ok(convertToEmDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/em/{id}")
    public ResponseEntity<Void> deleteElementDeModule(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        return programmeServiceDept.getElementDeModuleById(id)
                .map(em -> {
                    programmeServiceDept.deleteElementDeModule(id);
                    historiqueService.enregistrerAction(email, "Suppression EM", "Suppression de l'EM : " + em.getIntitule());
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoints pour les semestres
    @GetMapping("/semestres")
    public ResponseEntity<List<Semestre>> getAllSemestres() {
        List<Semestre> allSemestres = programmeServiceDept.getAllSemestres();
        //List<Semestre> filteredSemestres = SemestreUtil.getCurrentPariteSemestres(allSemestres);
        return ResponseEntity.ok(allSemestres);
    }
    
    @GetMapping("/semestres/{id}")
    public ResponseEntity<Semestre> getSemestreById(@PathVariable Long id) {
        return programmeServiceDept.getSemestreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/semestres/annee/{annee}")
    public ResponseEntity<List<Semestre>> getSemestresByAnnee(@PathVariable int annee) {
        return ResponseEntity.ok(programmeServiceDept.getSemestresByAnnee(annee));
    }
    
    // Import Excel spécifique au département
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            // Utiliser le service spécifique au département pour l'import Excel
            Map<String, Object> result = programmeServiceDept.importFromExcelForDepartment(file, email);
            historiqueService.enregistrerAction(email, "Import programme", "Import du fichier Excel de programme");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'import du fichier Excel: " + e.getMessage());
        }
    }

    // Unités d'Enseignement spécifiques au département
    @GetMapping("/ue/current-period")
    public ResponseEntity<List<UniteEnseignementDTO>> getDepartementUniteEnseignementsCurrentPeriod(
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
       /* List<Semestre> currentPeriodSemestres = SemestreUtil.getCurrentPariteSemestres(programmeServiceDept.getAllSemestres());
        List<Integer> currentPeriodSemestreNums = currentPeriodSemestres.stream()
            .map(sem -> Integer.parseInt(sem.getSemestre().substring(1)))
            .collect(Collectors.toList());
        List<String> currentPeriodSemestresCodes = currentPeriodSemestres.stream()
            .map(Semestre::getSemestre)
            .collect(Collectors.toList());
 */
        List<UniteEnseignement> deptUEs = programmeServiceDept.getDepartmentUniteEnseignements(email);
/* 
        List<UniteEnseignement> filteredUEs = deptUEs.stream()
            .filter(ue -> {
                Integer num = ue.getSemestreNum();
                String code = null;
                if (ue.getSemestre() != null) {
                    code = ue.getSemestre().getSemestre();
                }
                return (num != null && currentPeriodSemestreNums.contains(num))
                    || (code != null && currentPeriodSemestresCodes.contains(code));
            })
            .collect(Collectors.toList());
*/
        List<UniteEnseignementDTO> ueDTOs = deptUEs.stream()
            .map(this::convertToUeDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ueDTOs);
    }

    // Éléments de Module spécifiques au département
    @GetMapping("/em/current-period")
    public ResponseEntity<List<ElementDeModuleDTO>> getDepartementElementsDeModuleCurrentPeriod(
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
       /*  List<Semestre> currentPeriodSemestres = SemestreUtil.getCurrentPariteSemestres(programmeServiceDept.getAllSemestres());
        List<Integer> currentPeriodSemestreNums = currentPeriodSemestres.stream()
            .map(sem -> Integer.parseInt(sem.getSemestre().substring(1)))
            .collect(Collectors.toList());
        List<String> currentPeriodSemestresCodes = currentPeriodSemestres.stream()
            .map(Semestre::getSemestre)
            .collect(Collectors.toList());
*/
        List<ElementDeModule> ems = programmeServiceDept.getDepartmentElementsDeModule(email);
/* 
        List<ElementDeModule> filteredEMs = ems.stream()
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
        List<ElementDeModuleDTO> emDTOs = ems.stream()
                .map(this::convertToEmDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(emDTOs);
    }

    // Méthodes utilitaires pour convertir les entités en DTOs
    private UniteEnseignementDTO convertToUeDTO(UniteEnseignement ue) {
        UniteEnseignementDTO dto = new UniteEnseignementDTO();
        dto.setIdUE(ue.getIdUE());
        dto.setCodeUE(ue.getCodeUE());
        dto.setIntitule(ue.getIntitule());
        dto.setNbEM(ue.getNbEM());
        dto.setAnnee(ue.getAnnee());
        dto.setSemestre(ue.getSemestreNum());

        if (ue.getDepartement() != null) {
            dto.setDepartementCode(ue.getDepartement().getCodeDep());
            dto.setDepartementNom(ue.getDepartement().getIntitule());
        }

        return dto;
    }

    private ElementDeModuleDTO convertToEmDTO(ElementDeModule em) {
        ElementDeModuleDTO dto = new ElementDeModuleDTO();
        dto.setIdEM(em.getIdEM());
        dto.setCodeEM(em.getCodeEM());
        dto.setCodeEU(em.getCodeEU());
        dto.setIntitule(em.getIntitule());
        dto.setNombreCredits(em.getNombreCredits());
        dto.setCoefficient(em.getCoefficient());
        dto.setSemestre(em.getSemestre());
        dto.setHeuresCM(em.getHeuresCM());
        dto.setHeuresTD(em.getHeuresTD());
        dto.setHeuresTP(em.getHeuresTP());
        if (em.getResponsableEM() != null) {
            dto.setResponsableEM(em.getResponsableEM().getNom() + " " + em.getResponsableEM().getPrenom());
        } else {
            dto.setResponsableEM("Non assigné");
        }
        
        if (em.getUniteEnseignement() != null) {
            dto.setUeIntitule(em.getUniteEnseignement().getIntitule());
        }

        return dto;
    }
}
