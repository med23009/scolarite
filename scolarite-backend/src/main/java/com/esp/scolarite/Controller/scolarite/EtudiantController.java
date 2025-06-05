package com.esp.scolarite.Controller.scolarite;

import com.esp.scolarite.Service.EtudiantService;
import com.esp.scolarite.entity.Etudiant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.List;
// Fix for the EtudiantController.java

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    @Autowired
    private EtudiantService etudiantService;

    @GetMapping
    public ResponseEntity<List<Etudiant>> getAllEtudiants() {
        return ResponseEntity.ok(etudiantService.getAllEtudiants());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RS')")
    public ResponseEntity<Etudiant> getEtudiantById(@PathVariable Long id) {
        return etudiantService.getEtudiantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_RS')")
    public ResponseEntity<?> createEtudiant(@RequestBody Etudiant etudiant) {
        try {
            Etudiant saved = etudiantService.saveEtudiant(etudiant);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace(); // For logging
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Erreur lors de la création: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RS')")
    public ResponseEntity<?> updateEtudiant(@PathVariable Long id, @RequestBody Etudiant etudiant) {
        try {
            Etudiant updated = etudiantService.updateEtudiant(id, etudiant);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace(); // For logging
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RS')")
    public ResponseEntity<Void> deleteEtudiant(@PathVariable Long id) {
        try {
            etudiantService.deleteEtudiant(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<?> importEtudiantsFromCSV(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "promotion", required = false) String promotion) {
        try {
            List<Etudiant> importedEtudiants = etudiantService.importFromCSV(file, promotion);
            return ResponseEntity.ok(importedEtudiants);
        } catch (Exception e) {
            e.printStackTrace(); // For logging
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Erreur lors de l'import: " + e.getMessage()));
        }
    }
}