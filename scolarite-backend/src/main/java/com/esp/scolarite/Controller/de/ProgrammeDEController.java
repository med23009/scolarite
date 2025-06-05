package com.esp.scolarite.Controller.de;

import com.esp.scolarite.Config.*;
import com.esp.scolarite.Service.programme.ProgrammeService;
import com.esp.scolarite.Service.admin.HistoriqueService;
import com.esp.scolarite.Service.NoteService;
import com.esp.scolarite.dto.ElementDeModuleDTO;
import com.esp.scolarite.dto.UniteEnseignementDTO;
import com.esp.scolarite.entity.ElementDeModule;
import com.esp.scolarite.entity.NoteSemestrielle;
import com.esp.scolarite.entity.UniteEnseignement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/de/programmes")
public class ProgrammeDEController {

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HistoriqueService historiqueService;
    
    @Autowired
    private NoteService noteService;

    // 🔹 UE par Département
    @GetMapping("/ue/department/{id}")
    public ResponseEntity<List<UniteEnseignementDTO>> getUEsByDepartment(@PathVariable Long id) {
        List<UniteEnseignement> ues = programmeService.getUEByDepartmentId(id);
        List<UniteEnseignementDTO> ueDTOs = ues.stream().map(this::convertToUeDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ueDTOs);
    }

    // 🔹 UE par Pôle
    @GetMapping("/ue/pole/{id}")
    public ResponseEntity<List<UniteEnseignementDTO>> getUEsByPole(@PathVariable Long id) {
        List<UniteEnseignement> ues = programmeService.getUEByPoleId(id);
        List<UniteEnseignementDTO> ueDTOs = ues.stream().map(this::convertToUeDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ueDTOs);
    }

    // 🔹 EM par UE
    @GetMapping("/em/ue/{id}")
    public ResponseEntity<List<ElementDeModuleDTO>> getEMsByUE(@PathVariable Long id) {
        List<ElementDeModule> ems = programmeService.getEMByUEId(id);
        List<ElementDeModuleDTO> emDTOs = ems.stream().map(this::convertToEmDTO).collect(Collectors.toList());
        return ResponseEntity.ok(emDTOs);
    }
    
    // 🔹 EM par Département
    @GetMapping("/em/department/{id}")
    public ResponseEntity<List<ElementDeModuleDTO>> getEMsByDepartment(@PathVariable Long id) {
        // Get EMs for this department through UEs
        List<UniteEnseignement> ues = programmeService.getUEByDepartmentId(id);
        List<ElementDeModule> allEms = ues.stream()
            .flatMap(ue -> programmeService.getEMByUEId(ue.getIdUE()).stream())
            .collect(Collectors.toList());
        
        List<ElementDeModuleDTO> emDTOs = allEms.stream().map(this::convertToEmDTO).collect(Collectors.toList());
        return ResponseEntity.ok(emDTOs);
    }
    
    // 🔹 EM par Pôle
    @GetMapping("/em/pole/{id}")
    public ResponseEntity<List<ElementDeModuleDTO>> getEMsByPole(@PathVariable Long id) {
        // Get EMs for this pole through UEs
        List<UniteEnseignement> ues = programmeService.getUEByPoleId(id);
        List<ElementDeModule> allEms = ues.stream()
            .flatMap(ue -> programmeService.getEMByUEId(ue.getIdUE()).stream())
            .collect(Collectors.toList());
        
        List<ElementDeModuleDTO> emDTOs = allEms.stream().map(this::convertToEmDTO).collect(Collectors.toList());
        return ResponseEntity.ok(emDTOs);
    }
    
    // 🔹 Notes par Element Module
    @GetMapping("/notes/elementmodule/{codeEM}")
    public ResponseEntity<?> getNotesByElementModule(@PathVariable String codeEM) {
        try {
            // Use the NoteService to get notes for this element module
            List<NoteSemestrielle> notes = noteService.getNotesByModule(codeEM);
            return ResponseEntity.ok(notes);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching notes for element module: " + e.getMessage());
        }
    }

    // 🔹 Ajouter UE
    @PostMapping("/ue")
    public ResponseEntity<UniteEnseignement> createUE(@RequestBody UniteEnseignement ue,
                                                      @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        UniteEnseignement saved = programmeService.saveUniteEnseignement(ue);
        historiqueService.enregistrerAction(email, "Ajout UE", "Ajout UE : " + ue.getIntitule());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 🔹 Modifier UE
    @PutMapping("/ue/{id}")
    public ResponseEntity<UniteEnseignement> updateUE(@PathVariable Long id,
                                                      @RequestBody UniteEnseignement ue,
                                                      @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        ue.setIdUE(id);
        UniteEnseignement updated = programmeService.saveUniteEnseignement(ue);
        historiqueService.enregistrerAction(email, "Modification UE", "Modification UE : " + ue.getIntitule());
        return ResponseEntity.ok(updated);
    }

    // 🔹 Supprimer UE
    @DeleteMapping("/ue/{id}")
    public ResponseEntity<Void> deleteUE(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        programmeService.deleteUniteEnseignement(id);
        historiqueService.enregistrerAction(email, "Suppression UE", "Suppression de l'UE : ID=" + id);
        return ResponseEntity.noContent().build();
    }

    // 🔹 Ajouter EM
    @PostMapping("/em")
    public ResponseEntity<ElementDeModule> createEM(@RequestBody ElementDeModule em,
                                                    @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        ElementDeModule saved = programmeService.saveElementDeModule(em);
        historiqueService.enregistrerAction(email, "Ajout EM", "Ajout EM : " + em.getIntitule());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 🔹 Modifier EM
    @PutMapping("/em/{id}")
    public ResponseEntity<ElementDeModule> updateEM(@PathVariable Long id,
                                                    @RequestBody ElementDeModule em,
                                                    @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        em.setIdEM(id);
        ElementDeModule updated = programmeService.saveElementDeModule(em);
        historiqueService.enregistrerAction(email, "Modification EM", "Modification EM : " + em.getIntitule());
        return ResponseEntity.ok(updated);
    }

    // 🔹 Supprimer EM
    @DeleteMapping("/em/{id}")
    public ResponseEntity<Void> deleteEM(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        programmeService.deleteElementDeModule(id);
        historiqueService.enregistrerAction(email, "Suppression EM", "Suppression de l'EM : ID=" + id);
        return ResponseEntity.noContent().build();
    }

    // =======================
    // 🔁 Méthodes de conversion
    // =======================

    private UniteEnseignementDTO convertToUeDTO(UniteEnseignement ue) {
        UniteEnseignementDTO dto = new UniteEnseignementDTO();
        dto.setIdUE(ue.getIdUE());
        dto.setCodeUE(ue.getCodeUE());
        dto.setIntitule(ue.getIntitule());
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
        dto.setResponsableEM(
                em.getResponsableEM() != null
                        ? em.getResponsableEM().getNom() + " " + em.getResponsableEM().getPrenom()
                        : "Non assigné"
        );
        dto.setUeIntitule(em.getUniteEnseignement() != null
                ? em.getUniteEnseignement().getIntitule()
                : null);
        return dto;
    }
}

