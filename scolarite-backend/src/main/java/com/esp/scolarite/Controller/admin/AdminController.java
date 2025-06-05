package com.esp.scolarite.Controller.admin;

import com.esp.scolarite.Service.admin.AdminService;
import com.esp.scolarite.dto.AuthResponse;
import com.esp.scolarite.dto.UserDto;
import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.Pole;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.entity.User;
import com.esp.scolarite.exception.NoRoleSelectedException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==== UTILISATEURS ====
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = adminService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<AuthResponse> createUser(@RequestBody UserDto dto) throws NoRoleSelectedException {
        return ResponseEntity.ok(adminService.register(dto));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserDto dto) throws NoRoleSelectedException {
        return adminService.updateUser(id, dto);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return adminService.deleteUser(id);
    }
    
    @PostMapping("/users/change-password")
    public ResponseEntity<Boolean> changePassword(@RequestParam String email, 
                                               @RequestParam String currentPassword, 
                                               @RequestParam String newPassword) {
        boolean result = adminService.changePassword(email, currentPassword, newPassword);
        return ResponseEntity.ok(result);
    }

    // ==== DEPARTEMENTS ====
    @GetMapping("/departements")
    public List<Departement> getAllDepartements() {
        return adminService.getAllDepartements();
    }
    
    @GetMapping("/departements/{id}")
    public ResponseEntity<Departement> getDepartementById(@PathVariable Long id) {
        return adminService.getDepartementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/departements/by-email")
    public ResponseEntity<Departement> getDepartementByUserEmail(@RequestParam String email) {
        return adminService.getDepartementByUserEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/departements")
    public Departement createDepartement(@RequestBody Departement d) {
        return adminService.saveDepartement(d);
    }
    
    @PostMapping("/departements/import")
    public ResponseEntity<List<Departement>> importDepartementsFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            List<Departement> importedDepartements = adminService.importDepartementsFromCSV(file);
            return ResponseEntity.ok(importedDepartements);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/departements/{id}")
    public ResponseEntity<Departement> updateDepartement(@PathVariable Long id, @RequestBody Departement d) {
        try {
            Optional<Departement> existingDept = adminService.getDepartementById(id);
            if (existingDept.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Update the existing department with new values
            Departement dept = existingDept.get();
            if (d.getCodeDep() != null) dept.setCodeDep(d.getCodeDep());
            if (d.getIntitule() != null) dept.setIntitule(d.getIntitule());
            if (d.getDescription() != null) dept.setDescription(d.getDescription());
            if (d.getNom_responsable() != null) dept.setNom_responsable(d.getNom_responsable());
            if (d.getResponsable() != null) dept.setResponsable(d.getResponsable());
            
            return ResponseEntity.ok(adminService.saveDepartement(dept));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/departements/{id}")
    public ResponseEntity<?> deleteDepartement(@PathVariable Long id) {
        adminService.deleteDepartement(id);
        return ResponseEntity.ok().build();
    }

    // ==== POLES ====
    @GetMapping("/poles")
    public List<Pole> getAllPoles() {
        return adminService.getAllPoles();
    }
    
    @GetMapping("/poles/{id}")
    public ResponseEntity<Pole> getPoleById(@PathVariable Long id) {
        return adminService.getPoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/poles")
    public Pole createPole(@RequestBody Pole p) {
        return adminService.savePole(p);
    }
    
    @PostMapping("/poles/import")
    public ResponseEntity<List<Pole>> importPolesFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            List<Pole> importedPoles = adminService.importPolesFromCSV(file);
            return ResponseEntity.ok(importedPoles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/poles/{id}")
    public ResponseEntity<Pole> updatePole(@PathVariable Long id, @RequestBody Pole p) {
        try {
            Optional<Pole> existingPole = adminService.getPoleById(id);
            if (existingPole.isEmpty()) {
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
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/poles/{id}")
    public ResponseEntity<?> deletePole(@PathVariable Long id) {
        adminService.deletePole(id);
        return ResponseEntity.ok().build();
    }

    // ==== SEMESTRES ====
    @GetMapping("/semestres")
    public List<Semestre> getAllSemestres() {
        return adminService.getAllSemestres();
    }
    
    @GetMapping("/semestres/{id}")
    public ResponseEntity<Semestre> getSemestreById(@PathVariable Long id) {
        return adminService.getSemestreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/semestres")
    public Semestre createSemestre(@RequestBody Semestre s) {
        return adminService.saveSemestre(s);
    }
    
    @PostMapping("/semestres/import")
    public ResponseEntity<List<Semestre>> importSemestresFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            List<Semestre> importedSemestres = adminService.importSemestresFromCSV(file);
            return ResponseEntity.ok(importedSemestres);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/semestres/{id}")
    public ResponseEntity<Semestre> updateSemestre(@PathVariable Long id, @RequestBody Semestre s) {
        try {
            Optional<Semestre> existingSemestre = adminService.getSemestreById(id);
            if (existingSemestre.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Update the existing semester with new values
            Semestre semestre = existingSemestre.get();
            if (s.getSemestre() != null) semestre.setSemestre(s.getSemestre());
            if (s.getAnnee() != 0) semestre.setAnnee(s.getAnnee());
            if (s.getDateDebut() != null) semestre.setDateDebut(s.getDateDebut());
            if (s.getDateFin() != null) semestre.setDateFin(s.getDateFin());
            if (s.getNombreSemaines() != 0) semestre.setNombreSemaines(s.getNombreSemaines());
            
            return ResponseEntity.ok(adminService.saveSemestre(semestre));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/semestres/{id}")
    public ResponseEntity<?> deleteSemestre(@PathVariable Long id) {
        adminService.deleteSemestre(id);
        return ResponseEntity.ok().build();
    }
}