package com.esp.scolarite.Controller.scolarite;

import com.esp.scolarite.Service.PlanEtudeService;
import com.esp.scolarite.dto.PlanEtudeDTO;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.repository.SemestreRepository;
import com.esp.scolarite.util.SemestreUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour gérer les fonctionnalités du plan d'étude
 */
@RestController
@RequestMapping("/api/plan-etude")
public class PlanEtudeController {
    private static final Logger logger = LoggerFactory.getLogger(PlanEtudeController.class);

    @Autowired
    private PlanEtudeService planEtudeService;
    
    @Autowired
    private SemestreRepository semestreRepository;

    /**
     * Récupère les éléments de modules non validés pour un étudiant et un semestre donnés
     * @param matricule Matricule de l'étudiant
     * @param semestreId ID du semestre
     * @return Liste des éléments de modules non validés et nombre total de crédits
     */
    @GetMapping
    public ResponseEntity<?> getPlanEtude(@RequestParam String matricule, 
                                         @RequestParam Long semestreId) {
        try {
            logger.info("Demande de plan d'étude pour matricule={}, semestreId={}", matricule, semestreId);
            PlanEtudeDTO planEtude = planEtudeService.getPlanEtude(matricule, semestreId);
            logger.info("Plan d'étude généré avec succès pour matricule={}", matricule);
            return ResponseEntity.ok(planEtude);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du plan d'étude", e);
            Map<String, String> response = new HashMap<>();
            
            // Analyse du message d'erreur pour déterminer la cause
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Étudiant non trouvé")) {
                response.put("error", "Étudiant non trouvé avec le matricule: " + matricule);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (errorMessage.contains("Semestre non trouvé")) {
                response.put("error", "Semestre non trouvé avec l'ID: " + semestreId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                response.put("error", "Erreur lors de la génération du plan d'étude. Vérifiez les données et réessayez.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
    }
    
    /**
     * Récupère la liste des semestres disponibles
     * @return Liste de tous les semestres
     */
    @GetMapping("/semestres")
    public ResponseEntity<?> getAllSemestres() {
        try {
            logger.info("Demande de liste des semestres");
            List<Semestre> allSemestres = semestreRepository.findAll();
            List<Semestre> filteredSemestres = SemestreUtil.getCurrentPariteSemestres(allSemestres);
            logger.info("Liste des semestres récupérée avec succès, {} semestres trouvés", filteredSemestres.size());
            return ResponseEntity.ok(filteredSemestres);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des semestres", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors de la récupération des semestres");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 