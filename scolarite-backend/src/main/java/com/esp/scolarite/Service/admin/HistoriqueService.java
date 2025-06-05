package com.esp.scolarite.Service.admin;

import com.esp.scolarite.entity.ActionHistorique;
import com.esp.scolarite.entity.ActionHistorique.TypeAction;
import com.esp.scolarite.repository.ActionHistoriqueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoriqueService {

    private final ActionHistoriqueRepository historiqueRepository;

    public HistoriqueService(ActionHistoriqueRepository historiqueRepository) {
        this.historiqueRepository = historiqueRepository;
    }

    /**
     * Enregistre une action dans l'historique
     * 
     * @param utilisateur L'utilisateur qui a effectué l'action (email ou nom)
     * @param type Le type d'action (LOGIN, AJOUT, etc.)
     * @param action Description de l'action (ex: "Création étudiant")
     * @param details Détails supplémentaires sur l'action
     */
    public void enregistrerAction(String utilisateur, TypeAction type, String action, String details) {
        ActionHistorique historique = new ActionHistorique();
        historique.setUtilisateur(utilisateur);
        historique.setType(type);
        historique.setAction(action);
        historique.setDetails(details);
        historique.setDateAction(LocalDateTime.now());
        historiqueRepository.save(historique);
    }
    
    /**
     * Méthode de compatibilité pour l'ancien format sans type d'action
     */
    public void enregistrerAction(String utilisateur, String action, String details) {
        enregistrerAction(utilisateur, TypeAction.AUTRE, action, details);
    }
    
    /**
     * Enregistre une action de connexion
     */
    public void enregistrerConnexion(String utilisateur) {
        enregistrerAction(utilisateur, TypeAction.LOGIN, "Connexion", "Utilisateur connecté");
    }
    
    /**
     * Enregistre une action de déconnexion
     */
    public void enregistrerDeconnexion(String utilisateur) {
        enregistrerAction(utilisateur, TypeAction.LOGOUT, "Déconnexion", "Utilisateur déconnecté");
    }
    
    /**
     * Récupère toutes les actions de l'historique
     */
    public List<ActionHistorique> getAllHistorique() {
        return historiqueRepository.findAll();
    }
    
    /**
     * Récupère les actions de l'historique avec pagination
     */
    public Page<ActionHistorique> getAllHistorique(Pageable pageable) {
        return historiqueRepository.findAll(pageable);
    }
    
    /**
     * Filtre l'historique par utilisateur
     */
    public List<ActionHistorique> getHistoriqueByUtilisateur(String utilisateur) {
        return historiqueRepository.findByUtilisateur(utilisateur);
    }
    
    /**
     * Filtre l'historique par type d'action
     */
    public List<ActionHistorique> getHistoriqueByType(TypeAction type) {
        return historiqueRepository.findByType(type);
    }
    
    /**
     * Recherche avancée dans l'historique avec plusieurs critères
     */
    public Page<ActionHistorique> rechercheAvancee(
            String utilisateur, 
            TypeAction type, 
            String action, 
            LocalDateTime debut, 
            LocalDateTime fin, 
            Pageable pageable) {
        return historiqueRepository.rechercheAvancee(utilisateur, type, action, debut, fin, pageable);
    }
}
