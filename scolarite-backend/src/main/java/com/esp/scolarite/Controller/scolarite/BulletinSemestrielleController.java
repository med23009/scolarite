package com.esp.scolarite.Controller.scolarite;

import com.esp.scolarite.entity.BulletinSemestrielle;
import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.Etudiant;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.repository.DepartementRepository;
import com.esp.scolarite.Service.BulletinSemestrielleService;
import com.esp.scolarite.Service.EmailService;
import com.esp.scolarite.dto.EmailBulletinDTO;

import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/bulletins")
public class BulletinSemestrielleController {

    private final BulletinSemestrielleService bulletinService;
    private  DepartementRepository departementRepository;
     private EmailService emailService;
    public BulletinSemestrielleController(BulletinSemestrielleService bulletinService,DepartementRepository departementRepository,EmailService emailService) {
        this.bulletinService = bulletinService;
        this.departementRepository=departementRepository;
        this.emailService=emailService;
    }

 
  @GetMapping("/data/{matricule}/{semestreId}")
public ResponseEntity<BulletinSemestrielle> getBulletinData(
        @PathVariable String matricule,
        @PathVariable Long semestreId) {

    try {
        BulletinSemestrielle bulletin = bulletinService.getOrCreateBulletin(matricule, semestreId);

        if (bulletin == null) {
            System.err.println("❌ Bulletin introuvable pour matricule: " + matricule + ", semestre: " + semestreId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        bulletin.calculerMoyennes();
        return ResponseEntity.ok(bulletin);

    } catch (Exception e) {
        System.err.println("❌ Erreur lors de la récupération du bulletin:");
        e.printStackTrace();  // Affiche la stack trace complète

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
    }
}

@GetMapping("/departement/{idDepartement}/{promotion}")
public ResponseEntity<List<Etudiant>> getEtudiantsByDeptAndPromo(
        @PathVariable Long idDepartement,
        @PathVariable String promotion,
        @RequestParam Long semestreId) {

    // Récupère tous les étudiants, même sans bulletin
    List<Etudiant> etudiants = bulletinService.getEtudiantsByDeptPromo(idDepartement, promotion);

    return ResponseEntity.ok(etudiants);
}



@GetMapping("/forselction/departements")
public List<Departement> getAllDepartements() {
    return departementRepository.findAll();
}

   @PostMapping("/send-emails")
    public ResponseEntity<?> sendReleves(@RequestBody List<EmailBulletinDTO> dtos) {
        for (EmailBulletinDTO dto : dtos) {
            try {
                byte[] pdfBytes = Base64.getDecoder().decode(dto.getPdfBase64());
                emailService.sendBulletinEmail(dto.getEmail(), pdfBytes);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de l'envoi du mail à : " + dto.getEmail());
            }
        }
        return ResponseEntity.ok("Emails envoyés avec succès.");
    }
    @GetMapping("/semestres")
    public List<Semestre> getAllSemestres() {
        return bulletinService.getAllSemestres();
    }

}