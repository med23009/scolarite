import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ChefPoleProgrammeService } from '../../core/services/chef_pole/programme.service';
import { UniteEnseignement } from '../../core/models/unite-enseignement.model';
import { ElementDeModule } from '../../core/models/element-module.model';
import { ElementDeModuleDTO } from '../../core/models/element-module-dto.model';
import { Semestre } from '../../core/models/semestre.model';
import { AuthService } from '../../core/services/auth/auth.service';

// Interface pour le regroupement UE/EM
interface ProgrammeGroup {
  ue: UniteEnseignement;
  modules: ElementDeModule[];
}

@Component({
  selector: 'app-programme',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
  templateUrl: './programme.component.html',
  styleUrl: './programme.component.scss'
})
export class ProgrammeComponent implements OnInit {
  // Données
  uniteEnseignements: UniteEnseignement[] = [];
  elementsModule: ElementDeModule[] = [];
  groupedProgrammes: ProgrammeGroup[] = [];
  semestres: Semestre[] = [];

  // États
  loading = false;
  error = '';
  success = '';
  selectedFile: File | null = null;
  importInProgress = false;
  selectedSemestre: number | null = null;

  // Mode d'affichage
  searchTerm: string = '';

  // Formulaires
  showForm = false;
  editMode = false;
  formType: 'ue' | 'em' = 'ue';
  ueForm: FormGroup;
  emForm: FormGroup;
  currentUEId: number | null = null;
  currentEMId: number | null = null;
  submittedUE = false;
  submittedEM = false;

  // Confirmation dialog
  showConfirmDialog = false;
  confirmMessage = '';
  deleteCallback: (() => void) | null = null;

  constructor(
    private programmeService: ChefPoleProgrammeService,
    private formBuilder: FormBuilder,
    private authService: AuthService
  ) {
    // Initialiser le formulaire UE
    this.ueForm = this.formBuilder.group({
      codeUE: ['', [Validators.required, Validators.maxLength(30)]],
      intitule: ['', Validators.required],
      semestre: [null]
    });

    // Initialiser le formulaire EM
    this.emForm = this.formBuilder.group({
      codeEU: ['', Validators.required],
      codeEM: ['', [Validators.required, Validators.maxLength(30)]],
      intitule: ['', Validators.required],
      nombreCredits: [0],
      coefficient: [0],
      semestre: [null],
      responsableEM: ['']
    });
  }

  ngOnInit(): void {
    this.loadSemestres();
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    // Charger les UEs
    const email = this.authService.getCurrentUserEmail();
    this.programmeService.getAllUniteEnseignements(email).subscribe({
      next: (data) => {
        this.uniteEnseignements = data;

        // Charger les EMs après les UEs
        this.programmeService.getAllElementsDeModule().subscribe({
          next: (modules) => {
            this.elementsModule = modules;
            this.groupProgrammes();
            this.loading = false;
          },
          error: (error) => {
            this.error = error.message || 'Erreur lors du chargement des éléments de module';
            this.loading = false;
          }
        });
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors du chargement des unités d\'enseignement';
        this.loading = false;
      }
    });
  }

  loadSemestres(): void {
    this.programmeService.getAllSemestres().subscribe({
      next: (data) => {
        this.semestres = data;
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors du chargement des semestres';
      }
    });
  }

  // Regrouper les éléments de module par unité d'enseignement
  groupProgrammes(): void {
    // Regrouper les EM par UE, en filtrant aussi par semestre sélectionné
    this.groupedProgrammes = this.uniteEnseignements.map(ue => {
      const modules = this.elementsModule.filter(em => {
        return em.codeEU === ue.codeUE &&
          (this.selectedSemestre == null || em.semestre === this.selectedSemestre);
      });
      return {
        ue,
        modules
      };
    })
    // Optionnel : n'afficher que les UE ayant des EM
    .filter(group => group.modules.length > 0);
  }

  // Filtrer les données
  applyFilter(): void {
    if (this.searchTerm.trim() === '' && !this.selectedSemestre) {
      this.groupProgrammes();
    } else {
      this.filterGroupedProgrammes();
    }
  }

  // Fonction de filtrage des groupes
  filterGroupedProgrammes(): void {
    this.groupedProgrammes = [];
  
    const searchLower = this.searchTerm.trim().toLowerCase();
  
    // 1. Filtrer les EMs uniquement
    let filteredEMs = this.elementsModule;
  
    if (searchLower !== '') {
      filteredEMs = filteredEMs.filter(em =>
        em.codeEM.toLowerCase().includes(searchLower) ||
        em.intitule.toLowerCase().includes(searchLower) ||
        (em.responsableEM && em.responsableEM.toLowerCase().includes(searchLower))
      );
    }
  
    if (this.selectedSemestre) {
      filteredEMs = filteredEMs.filter(em => em.semestre === this.selectedSemestre);
    }
  
    // 2. Filtrer les UEs selon la recherche (si besoin)
    let filteredUEs = this.uniteEnseignements;
  
    if (searchLower !== '') {
      filteredUEs = filteredUEs.filter(ue =>
        ue.codeUE.toLowerCase().includes(searchLower) ||
        ue.intitule.toLowerCase().includes(searchLower)
      );
    }
  
    // 3. Associer chaque UE aux EM filtrés et n'ajouter que si modules.length > 0
    filteredUEs.forEach(ue => {
      const modules = filteredEMs.filter(em => em.codeEU === ue.codeUE);
      if (modules.length > 0) {
        this.groupedProgrammes.push({ ue, modules });
      }
    });
  }    
  
  // Gestion des semestres
  onSemestreChange(): void {
    this.applyFilter();
  }

  // Fonction pour obtenir le nom du semestre
  getSemestreName(semestreId: number | undefined): string {
    if (!semestreId) return 'Non assigné';

    const semestre = this.semestres.find(s => s.idSemestre === semestreId);
    if (semestre) {
      return `${semestre.semestre} (${semestre.annee})`;
    } else {
      return 'Semestre inconnu';
    }
  }

  // Gestion des formulaires
  showAddForm(): void {
    this.showForm = true;
    this.editMode = false;
    this.formType = 'ue';
    this.resetForms();
  }

  showAddEMForUE(ue: UniteEnseignement): void {
    this.showForm = true;
    this.editMode = false;
    this.formType = 'em';
    this.resetForms();

    // Pré-remplir le code UE
    this.emForm.patchValue({
      codeEU: ue.codeUE
    });
  }

  editUE(ue: UniteEnseignement): void {
    this.showForm = true;
    this.editMode = true;
    this.formType = 'ue';
    this.currentUEId = ue.idUE!;

    this.ueForm.patchValue({
      codeUE: ue.codeUE,
      intitule: ue.intitule,
      semestre: ue.semestre
    });
  }

  editEM(em: ElementDeModule): void {
    this.showForm = true;
    this.editMode = true;
    this.formType = 'em';
    this.currentEMId = em.idEM!;

    this.emForm.patchValue({
      codeEU: em.codeEU,
      codeEM: em.codeEM,
      intitule: em.intitule,
      nombreCredits: em.nombreCredits,
      coefficient: em.coefficient,
      semestre: em.semestre,
      responsableEM: em.responsableEM
    });
  }

  submitUEForm(): void {
    this.submittedUE = true;

    if (this.ueForm.invalid) {
      return;
    }

    this.loading = true;

    if (this.editMode && this.currentUEId) {
      this.updateUE();
    } else {
      this.createUE();
    }
  }

  submitEMForm(): void {
    this.submittedEM = true;

    if (this.emForm.invalid) {
      return;
    }

    this.loading = true;

    if (this.editMode && this.currentEMId) {
      this.updateEM();
    } else {
      this.createEM();
    }
  }

  private createUE(): void {
    this.programmeService.createUniteEnseignement(this.ueForm.value)
      .subscribe({
        next: () => {
          this.success = 'Unité d\'enseignement créée avec succès';
          this.loading = false;
          this.showForm = false;
          this.loadData();
          this.resetForms();
        },
        error: error => {
          this.error = error.error?.message || 'Erreur lors de la création de l\'unité d\'enseignement';
          this.loading = false;
        }
      });
  }

  private updateUE(): void {
    this.programmeService.updateUniteEnseignement(this.currentUEId!, this.ueForm.value)
      .subscribe({
        next: () => {
          this.success = 'Unité d\'enseignement mise à jour avec succès';
          this.loading = false;
          this.showForm = false;
          this.loadData();
          this.resetForms();
        },
        error: error => {
          this.error = error.error?.message || 'Erreur lors de la mise à jour de l\'unité d\'enseignement';
          this.loading = false;
        }
      });
  }

  private createEM(): void {
    // Convert form value to DTO
    const emDTO: ElementDeModuleDTO = {
      codeEM: this.emForm.value.codeEM,
      codeEU: this.emForm.value.codeEU,
      intitule: this.emForm.value.intitule,
      nombreCredits: this.emForm.value.nombreCredits || 0,
      coefficient: this.emForm.value.coefficient || 0,
      semestre: this.emForm.value.semestre || 1,
      heuresCM: this.emForm.value.heuresCM || 0,
      heuresTD: this.emForm.value.heuresTD || 0,
      heuresTP: this.emForm.value.heuresTP || 0,
      responsableEM: this.emForm.value.responsableEM || 'Non assigné'
    };
    
    this.programmeService.createElementDeModule(emDTO)
      .subscribe({
        next: () => {
          this.success = 'Élément de module créé avec succès';
          this.loading = false;
          this.showForm = false;
          this.loadData();
          this.resetForms();
        },
        error: error => {
          this.error = error.error?.message || 'Erreur lors de la création de l\'élément de module';
          this.loading = false;
        }
      });
  }

  private updateEM(): void {
    // Convert form value to DTO
    const emDTO: ElementDeModuleDTO = {
      idEM: this.currentEMId !== null ? this.currentEMId : undefined,
      codeEM: this.emForm.value.codeEM,
      codeEU: this.emForm.value.codeEU,
      intitule: this.emForm.value.intitule,
      nombreCredits: this.emForm.value.nombreCredits,
      coefficient: this.emForm.value.coefficient,
      semestre: this.emForm.value.semestre,
      heuresCM: this.emForm.value.heuresCM || 0,
      heuresTD: this.emForm.value.heuresTD || 0,
      heuresTP: this.emForm.value.heuresTP || 0,
      responsableEM: this.emForm.value.responsableEM || 'Non assigné'
    };
    
    this.programmeService.updateElementDeModule(this.currentEMId!, emDTO)
      .subscribe({
        next: () => {
          this.success = 'Élément de module mis à jour avec succès';
          this.loading = false;
          this.showForm = false;
          this.loadData();
          this.resetForms();
        },
        error: error => {
          this.error = error.error?.message || 'Erreur lors de la mise à jour de l\'élément de module';
          this.loading = false;
        }
      });
  }
  

  
  
  
  // Import Excel
  onFileSelected(event: Event): void {
    const element = event.target as HTMLInputElement;
    if (element.files && element.files.length > 0) {
      this.selectedFile = element.files[0];
      // Auto-import après sélection
      this.importExcel();
    }
  }

  importExcel(): void {
    if (!this.selectedFile) {
      this.error = 'Veuillez sélectionner un fichier Excel';
      return;
    }

    this.importInProgress = true;
    this.error = '';
    this.success = '';

    this.programmeService.importFromExcel(this.selectedFile).subscribe({
      next: (data) => {
        this.importInProgress = false;
        const ues = data.uniteEnseignements?.length || 0;
        const ems = data.elementsDeModule?.length || 0;
        this.success = `Import réussi: ${ues} unités d'enseignement et ${ems} éléments de module importés`;

        if (data.errors && data.errors.length > 0) {
          this.error = `Avertissements: ${data.errors.join('\n')}`;
        }

        this.loadData();
        this.selectedFile = null;
        const fileInput = document.getElementById('excelFile') as HTMLInputElement;
        if (fileInput) {
          fileInput.value = '';
        }
      },
      error: (error) => {
        this.importInProgress = false;
        this.error = error.error || 'Erreur lors de l\'import du fichier Excel';
      }
    });
  }

  // Utilitaires
  cancelForm(): void {
    this.showForm = false;
    this.resetForms();
  }

  resetForms(): void {
    this.ueForm.reset({
      semestre: null
    });
    this.emForm.reset({
      nombreCredits: 0,
      coefficient: 0,
      semestre: null
    });
    this.submittedUE = false;
    this.submittedEM = false;
    this.currentUEId = null;
    this.currentEMId = null;
  }

  get ueF() { return this.ueForm.controls; }
  get emF() { return this.emForm.controls; }

  getSemestreNumber(semestre: string | number): number {
    if (typeof semestre === 'string' && semestre.startsWith('S')) {
      return parseInt(semestre.substring(1), 10);
    }
    return typeof semestre === 'number' ? semestre : 0;
  }



// Modify your existing delete methods to use the confirmation dialog
deleteUE(id: number): void {
  this.confirmMessage = 'Êtes-vous sûr de vouloir supprimer cette unité d\'enseignement ?';
  this.showConfirmDialog = true;
  this.deleteCallback = () => {
    this.programmeService.deleteUniteEnseignement(id).subscribe({
      next: () => {
        this.success = 'Unité d\'enseignement supprimée avec succès';
        this.loadData();
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors de la suppression de l\'unité d\'enseignement';
      }
    });
  };
}

deleteEM(id: number): void {
  this.confirmMessage = 'Êtes-vous sûr de vouloir supprimer cet élément de module ?';
  this.showConfirmDialog = true;
  this.deleteCallback = () => {
    this.programmeService.deleteElementDeModule(id).subscribe({
      next: () => {
        this.success = 'Élément de module supprimé avec succès';
        this.loadData();
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors de la suppression de l\'élément de module';
      }
    });
  };
}

// Add these methods to handle the confirmation dialog actions
confirmDelete(): void {
  if (this.deleteCallback) {
    this.deleteCallback();
    this.closeConfirmDialog();
  }
}

cancelDelete(): void {
  this.closeConfirmDialog();
}

closeConfirmDialog(): void {
  this.showConfirmDialog = false;
  this.confirmMessage = '';
  this.deleteCallback = null;
}
  
}
