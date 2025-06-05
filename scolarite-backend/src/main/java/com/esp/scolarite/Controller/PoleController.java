package com.esp.scolarite.Controller;

import com.esp.scolarite.Service.admin.AdminService;
import com.esp.scolarite.entity.Pole;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/poles")
@RequiredArgsConstructor
public class PoleController {

    private final AdminService adminService;

    @GetMapping
    public List<Pole> getAllPoles() {
        System.out.println("[PoleController] Récupération de tous les pôles");
        return adminService.getAllPoles();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Pole> getPoleById(@PathVariable Long id) {
        System.out.println("[PoleController] Récupération du pôle avec ID=" + id);
        return adminService.getPoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Pole createPole(@RequestBody Pole p) {
        System.out.println("[PoleController] Création d'un nouveau pôle: " + p.getIntitule());
        return adminService.savePole(p);
    }
    
    @PostMapping("/import")
    public ResponseEntity<List<Pole>> importPolesFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("[PoleController] Import de pôles depuis un fichier CSV");
            List<Pole> importedPoles = adminService.importPolesFromCSV(file);
            return ResponseEntity.ok(importedPoles);
        } catch (Exception e) {
            System.err.println("[PoleController] Erreur lors de l'import de pôles: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pole> updatePole(@PathVariable Long id, @RequestBody Pole p) {
        try {
            System.out.println("[PoleController] Mise à jour du pôle avec ID=" + id);
            Optional<Pole> existingPole = adminService.getPoleById(id);
            if (existingPole.isEmpty()) {
                System.err.println("[PoleController] Pôle non trouvé avec ID=" + id);
                return ResponseEntity.notFound().build();
            }
            
            // Update the existing pole with new values
            Pole pole = existingPole.get();
            if (p.getCodePole() != null) pole.setCodePole(p.getCodePole());
            if (p.getIntitule() != null) pole.setIntitule(p.getIntitule());
            if (p.getDescription() != null) pole.setDescription(p.getDescription());
            if (p.getNom_responsable() != null) pole.setNom_responsable(p.getNom_responsable());
            if (p.getResponsable() != null) pole.setResponsable(p.getResponsable());
            
            return ResponseEntity.ok(adminService.savePole(pole));
        } catch (Exception e) {
            System.err.println("[PoleController] Erreur lors de la mise à jour du pôle: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePole(@PathVariable Long id) {
        System.out.println("[PoleController] Suppression du pôle avec ID=" + id);
        adminService.deletePole(id);
        return ResponseEntity.ok().build();
    }
}
