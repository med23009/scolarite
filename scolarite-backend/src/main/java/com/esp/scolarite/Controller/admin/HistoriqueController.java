package com.esp.scolarite.Controller.admin;

import com.esp.scolarite.Service.admin.HistoriqueService;
import com.esp.scolarite.entity.ActionHistorique;
import com.esp.scolarite.entity.ActionHistorique.TypeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/historique")
public class HistoriqueController {

    @Autowired
    private HistoriqueService historiqueService;

    /**
     * Récupère toutes les actions de l'historique
     */
    @GetMapping
    public ResponseEntity<List<ActionHistorique>> getAllHistorique() {
        return ResponseEntity.ok(historiqueService.getAllHistorique());
    }

    /**
     * Récupère les actions avec pagination et tri
     */
    @GetMapping("/page")
    public ResponseEntity<Page<ActionHistorique>> getHistoriquePagine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateAction") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        return ResponseEntity.ok(historiqueService.getAllHistorique(pageable));
    }

    /**
     * Filtre par utilisateur
     */
    @GetMapping("/utilisateur/{email}")
    public ResponseEntity<List<ActionHistorique>> getByUtilisateur(@PathVariable String email) {
        return ResponseEntity.ok(historiqueService.getHistoriqueByUtilisateur(email));
    }
    
    /**
     * Filtre par type d'action
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ActionHistorique>> getByType(@PathVariable TypeAction type) {
        return ResponseEntity.ok(historiqueService.getHistoriqueByType(type));
    }
    
    /**
     * Liste tous les types d'actions disponibles
     */
    @GetMapping("/types")
    public ResponseEntity<List<TypeAction>> getAllTypes() {
        return ResponseEntity.ok(Arrays.asList(TypeAction.values()));
    }
    
    /**
     * Recherche avancée avec filtres multiples
     */
    @GetMapping("/recherche")
    public ResponseEntity<Page<ActionHistorique>> rechercheAvancee(
            @RequestParam(required = false) String utilisateur,
            @RequestParam(required = false) TypeAction type,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateAction") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        return ResponseEntity.ok(historiqueService.rechercheAvancee(
                utilisateur, type, action, debut, fin, pageable));
    }
}
