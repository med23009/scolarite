package com.esp.scolarite.Controller.admin;

import com.esp.scolarite.Service.admin.AdminService;
import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.repository.DepartementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departements")
public class DepartementController {

   @Autowired
   private AdminService adminService;
   
   @Autowired
   private DepartementRepository departementRepository;
   
   @GetMapping("/all")
   public ResponseEntity<List<Departement>> getAllDepartements() {
       try {
           List<Departement> departements = departementRepository.findAll();
           return ResponseEntity.ok(departements);
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
       }
   }
 @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartement(@PathVariable Long id) {
        try {
            // Appel de la méthode de suppression dans AdminService
            adminService.deleteDepartement(id);
            return ResponseEntity.noContent().build(); // Retourne un code 204 (No Content) après suppression
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Si une erreur se produit, retourne 404
        }
    }
}
