import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlanEtudeService } from '../../core/services/scolarite/plan-etude.service';
import { PlanEtude } from '../../core/models/plan-etude.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-plan-etudes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './plan-etudes.component.html',
  styleUrls: ['./plan-etudes.component.scss']
})
export class PlanEtudesComponent implements OnInit {
  matricule: string = '';
  selectedSemestreId: number | null = null;
  semestres: any[] = [];
  planEtude: PlanEtude | null = null;
  error: string | null = null;
  loading: boolean = false;
  success: boolean = false;

  constructor(private planEtudeService: PlanEtudeService) {}

  ngOnInit(): void {
    this.loadSemestres();
  }

  /**
   * Charge la liste des semestres disponibles
   */
  loadSemestres(): void {
    this.loading = true;
    this.planEtudeService.getAllSemestres().subscribe({
      next: (data) => {
        this.semestres = data;
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        console.error('Erreur lors du chargement des semestres', err);
        
        if (err.error && err.error.error) {
          // Si le serveur renvoie un message d'erreur spécifique
          this.error = err.error.error;
        } else if (err.status === 403) {
          this.error = 'Vous n\'avez pas les permissions nécessaires pour accéder aux semestres.';
        } else if (err.status === 0) {
          this.error = 'Impossible de se connecter au serveur. Vérifiez votre connexion internet.';
        } else {
          this.error = 'Erreur lors du chargement des semestres. Veuillez réessayer.';
        }
        
        this.loading = false;
      }
    });
  }

  /**
   * Génère le plan d'étude pour l'étudiant et le semestre sélectionnés
   */
  generatePlanEtude(): void {
    this.error = null;
    this.success = false;
    this.loading = true;
    this.planEtude = null;

    if (!this.matricule) {
      this.error = 'Veuillez entrer un matricule';
      this.loading = false;
      return;
    }

    if (!this.selectedSemestreId) {
      this.error = 'Veuillez sélectionner un semestre';
      this.loading = false;
      return;
    }

    this.planEtudeService.getPlanEtude(this.matricule, this.selectedSemestreId).subscribe({
      next: (data) => {
        this.planEtude = data;
        this.success = true;
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        console.error('Erreur lors de la génération du plan d\'étude', err);
        
        if (err.error && err.error.error) {
          // Si le serveur renvoie un message d'erreur spécifique
          this.error = err.error.error;
        } else if (err.status === 404) {
          this.error = 'Étudiant ou semestre non trouvé. Vérifiez le matricule et réessayez.';
        } else if (err.status === 403) {
          this.error = 'Vous n\'avez pas les permissions nécessaires pour accéder à ces données.';
        } else if (err.status === 0) {
          this.error = 'Impossible de se connecter au serveur. Vérifiez votre connexion internet.';
        } else {
          this.error = 'Erreur lors de la génération du plan d\'étude. Vérifiez les données et réessayez.';
        }
        
        this.loading = false;
      }
    });
  }

  /**
   * Réinitialise le formulaire et les résultats
   */
  reset(): void {
    this.matricule = '';
    this.selectedSemestreId = null;
    this.planEtude = null;
    this.error = null;
    this.success = false;
  }
  
  /**
   * Vérifie si un module est validé (directement ou par compensation)
   * @param codeEM Code du module à vérifier
   * @returns true si le module est validé, false sinon
   */
  isModuleValidated(codeEM: string): boolean {
    if (!this.planEtude || !this.planEtude.validationStatuts || !this.planEtude.validationStatuts[codeEM]) {
      return false;
    }
    
    const status = this.planEtude.validationStatuts[codeEM];
    // Le module est validé si le statut est V (validé), VCI (validé par compensation interne)
    // ou VCE (validé par compensation externe)
    return status === 'V' || status === 'VCI' || status === 'VCE';
  }
  
  /**
   * Retourne la classe CSS à appliquer au badge de validation
   * @param codeEM Code du module
   * @returns Classe CSS pour le badge
   */
  getValidationBadgeClass(codeEM: string): string {
    if (!this.planEtude || !this.planEtude.validationStatuts) {
      return '';
    }
    
    const status = this.planEtude.validationStatuts[codeEM];
    switch (status) {
      case 'V': return 'badge-success';  // Validé directement
      case 'VCI': return 'badge-warning'; // Validé par compensation interne
      case 'VCE': return 'badge-info';    // Validé par compensation externe
      default: return '';
    }
  }
  
  /**
   * Retourne le libellé à afficher selon le statut de validation
   * @param codeEM Code du module
   * @returns Libellé du statut de validation
   */
  getValidationLabel(codeEM: string): string {
    if (!this.planEtude || !this.planEtude.validationStatuts) {
      return '';
    }
    
    const status = this.planEtude.validationStatuts[codeEM];
    switch (status) {
      case 'V': return 'Validé';
      case 'VCI': return 'Comp. interne';
      case 'VCE': return 'Comp. externe';
      case 'NV': return 'Non validé';
      default: return '';
    }
  }
} 