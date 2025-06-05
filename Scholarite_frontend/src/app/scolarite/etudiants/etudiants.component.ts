import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { EtudiantService } from '../../core/services/scolarite/etudiant.service';
import { DepartementService } from '../../core/services/admin/departement.service';
import { Etudiant } from '../../core/models/etudiant.model';
import { Departement } from '../../core/models/departement.model';
import { jsPDF } from 'jspdf';

@Component({
  selector: 'app-etudiants',
  templateUrl: './etudiants.component.html',
  styleUrls: ['./etudiants.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule]
})
export class Etudiants implements OnInit {
  etudiants: Etudiant[] = [];
  loading = false;
  error = '';
  success = '';
  selectedFile: File | null = null;
  importInProgress = false;
  showForm = false;
  editMode = false;
  currentEtudiantId: number | null = null;
  etudiantForm: FormGroup;
  submitted = false;
  searchTerm = '';
  filteredEtudiants: Etudiant[] = [];
  departements: Departement[] = [];
  selectedDepartementId = '';
  selectedPromotion = '';
  importPromotion = ''; // Promotion pour l'import
  availablePromotions: string[] = []; // Liste des promotions disponibles

  constructor(
    private etudiantService: EtudiantService,
    private departementService: DepartementService,
    private formBuilder: FormBuilder
  ) {
    this.etudiantForm = this.formBuilder.group({
      matricule: ['', Validators.required],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      prenomAR: [''],
      nomAR: [''],
      dateNaissance: [''],
      lieuNaissance: [''],
      lieuNaissanceAR: [''],
      sexe: [''],
      NNI: [''],
      email: ['', [Validators.email]],
      photo: [''],
      dateInscription: [''],
      telephoneEtudiant: [''],
      telephoneCorrespondant: [''],
      adresseResidence: [''],
      anneeObtentionBac: [''],
      departement: [''],
      promotion: ['']
    });
  }

  ngOnInit(): void {
    // D'abord charger les départements, puis les étudiants
    this.loadDepartements();
    // Les étudiants seront chargés dans la méthode loadDepartements
  }

  applyFilter(): void {
    let filtered = [...this.etudiants];

    // Filtre par terme de recherche
    if (this.searchTerm) {
      const searchTermLower = this.searchTerm.toLowerCase();
      filtered = filtered.filter(etudiant =>
        etudiant.matricule?.toLowerCase().includes(searchTermLower) ||
        etudiant.nom?.toLowerCase().includes(searchTermLower) ||
        etudiant.prenom?.toLowerCase().includes(searchTermLower) ||
        etudiant.email?.toLowerCase().includes(searchTermLower)
      );
    }

    // Filtre par département
    if (this.selectedDepartementId) {
      filtered = filtered.filter(etudiant =>
        etudiant.departement?.idDep?.toString() === this.selectedDepartementId
      );
    }
    
    // Filtre par promotion
    if (this.selectedPromotion) {
      filtered = filtered.filter(etudiant =>
        etudiant.promotion === this.selectedPromotion
      );
    }

    this.filteredEtudiants = filtered;
  }

  loadDepartements(): void {
    this.loading = true;
    this.departementService.getAll().subscribe({
      next: (data) => {
        this.departements = data;
        // Une fois les départements chargés, nous chargeons les étudiants
        this.loadEtudiants();
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors du chargement des départements';
        this.loading = false;
      }
    });
  }

  loadEtudiants(): void {
    this.loading = true;
    this.etudiantService.getAllEtudiants().subscribe({
      next: (data) => {
        this.etudiants = data;
        
        // Extraire les promotions uniques pour le filtre
        this.availablePromotions = [...new Set(data
          .filter(e => e.promotion && e.promotion.trim() !== '')
          .map(e => e.promotion as string))].sort();
        
        // Enrichir les objets étudiants avec les données complètes de département
        this.etudiants.forEach(etudiant => {
          if (etudiant.departement && etudiant.departement.idDep) {
            const departementComplet = this.departements.find(
              dep => dep.idDep === etudiant.departement?.idDep
            );
            if (departementComplet) {
              etudiant.departement = departementComplet;
            }
          }
        });
        
        this.filteredEtudiants = [...this.etudiants]; // Initialiser filteredEtudiants avec tous les étudiants
        
        // Extraire les promotions uniques pour le filtre
        this.availablePromotions = this.etudiants
          .filter(e => e.promotion) // Filtrer les étudiants qui ont une promotion
          .map(e => e.promotion as string) // Extraire la promotion
          .filter((value, index, self) => self.indexOf(value) === index) // Supprimer les doublons
          .sort(); // Trier par ordre alphabétique
        
        this.applyFilter(); // Appliquer les filtres actuels
        this.loading = false;
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors du chargement des étudiants';
        this.loading = false;
      }
    });
  }

  showAddForm(): void {
    this.showForm = true;
    this.editMode = false;
    this.currentEtudiantId = null;
    this.etudiantForm.reset();
    // Réinitialiser les erreurs et messages de succès
    this.error = '';
    this.success = '';
  }
  formatDate(date: string | Date): string {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

editEtudiant(etudiant: Etudiant): void {
  this.showForm = true;
  this.editMode = true;
  this.currentEtudiantId = etudiant.idEtudiant!;
  
  // Formatage de la date pour l'affichage dans le formulaire
  const dateNaissance = etudiant.dateNaissance ? this.formatDate(etudiant.dateNaissance) : '';
  const dateInscription = etudiant.dateInscription ? this.formatDate(etudiant.dateInscription) : '';
  
  this.etudiantForm.patchValue({
    matricule: etudiant.matricule,
    nom: etudiant.nom,
    prenom: etudiant.prenom,
    prenomAR: etudiant.prenomAR,
    nomAR: etudiant.nomAR,
    dateNaissance: dateNaissance,
    lieuNaissance: etudiant.lieuNaissance,
    lieuNaissanceAR: etudiant.lieuNaissanceAR,
    sexe: etudiant.sexe,
    NNI: etudiant.NNI,
    email: etudiant.email,
    departement: etudiant.departement?.idDep,
    promotion: etudiant.promotion,
    telephoneEtudiant: etudiant.telephoneEtudiant,
    telephoneCorrespondant: etudiant.telephoneCorrespondant,
    adresseResidence: etudiant.adresseResidence,
    anneeObtentionBac: etudiant.anneeObtentionBac,
    dateInscription: dateInscription
  });
}

  cancelForm(): void {
    this.showForm = false;
    this.etudiantForm.reset();
    this.submitted = false;
    this.error = '';
    this.success = '';
  }



  private createEtudiant(etudiantData: any): void {
    this.etudiantService.createEtudiant(etudiantData)
      .subscribe({
        next: () => {
          this.success = 'Étudiant créé avec succès';
          this.loading = false;
          this.showForm = false;
          this.loadEtudiants(); // Recharger la liste des étudiants
          this.etudiantForm.reset();
          this.submitted = false;
        },
        error: error => {
          console.error('Erreur lors de la création:', error);
          this.error = error.error?.message || 'Erreur lors de la création de l\'étudiant';
          this.loading = false;
        }
      });
  }

  private updateEtudiant(etudiantData: any): void {
    if (!this.currentEtudiantId) {
      this.error = 'ID de l\'étudiant manquant';
      this.loading = false;
      return;
    }

    etudiantData.idEtudiant = this.currentEtudiantId;

    this.etudiantService.updateEtudiant(this.currentEtudiantId, etudiantData)
      .subscribe({
        next: () => {
          this.success = 'Étudiant mis à jour avec succès';
          this.loading = false;
          this.showForm = false;
          this.loadEtudiants(); // Recharger la liste des étudiants
          this.etudiantForm.reset();
          this.submitted = false;
        },
        error: error => {
          console.error('Erreur lors de la mise à jour:', error);
          this.error = error.error?.message || 'Erreur lors de la mise à jour de l\'étudiant';
          this.loading = false;
        }
      });
  }

  onFileSelected(event: Event): void {
    const element = event.target as HTMLInputElement;
    if (element.files && element.files.length > 0) {
      this.selectedFile = element.files[0];
    }
  }
// Fix for the Angular component (etudiants.component.ts)


  onSubmit(): void {
    this.submitted = true;
    if (this.etudiantForm.invalid) {
      this.error = 'Veuillez remplir tous les champs obligatoires';
      return;
    }
    
    const formData = this.etudiantForm.value;
    const etudiantData: any = {
      matricule: formData.matricule,
      nom: formData.nom,
      prenom: formData.prenom,
      prenomAR: formData.prenomAR,
      nomAR: formData.nomAR,
      email: formData.email,
      sexe: formData.sexe,
      NNI: formData.NNI,
      telephoneEtudiant: formData.telephoneEtudiant,
      telephoneCorrespondant: formData.telephoneCorrespondant,
      adresseResidence: formData.adresseResidence,
      lieuNaissance: formData.lieuNaissance,
      lieuNaissanceAR: formData.lieuNaissanceAR,
      promotion: formData.promotion
    };
    
    // Traitement des dates
    if (formData.dateNaissance) {
      etudiantData.dateNaissance = new Date(formData.dateNaissance);
    }
    
    if (formData.dateInscription) {
      etudiantData.dateInscription = new Date(formData.dateInscription);
    }
    
    // Traitement de l'année du bac
    if (formData.anneeObtentionBac) {
      const annee = parseInt(formData.anneeObtentionBac);
      if (!isNaN(annee)) {
        etudiantData.anneeObtentionBac = annee;
      }
    }
    
    // Gestion du département
    if (formData.departement) {
      const depId = parseInt(formData.departement);
      if (!isNaN(depId)) {
        etudiantData.departement = { 
          idDep: depId,
          codeDep: "", // Valeur par défaut
          intitule: "" // Valeur par défaut
        };
      }
    }

    this.loading = true;
    this.error = '';

    if (this.editMode && this.currentEtudiantId) {
      // Mise à jour d'un étudiant existant
      this.etudiantService.updateEtudiant(this.currentEtudiantId, etudiantData).subscribe({
        next: () => {
          this.success = 'Étudiant mis à jour avec succès';
          this.afterSave();
        },
        error: (error) => {
          console.error('Erreur lors de la mise à jour:', error);
          this.error = error.error?.message || 'Erreur lors de la mise à jour de l\'étudiant';
          this.loading = false;
        }
      });
    } else {
      // Création d'un nouvel étudiant
      this.etudiantService.createEtudiant(etudiantData).subscribe({
        next: () => {
          this.success = 'Étudiant créé avec succès';
          this.afterSave();
        },
        error: (error) => {
          console.error('Erreur lors de la création:', error);
          this.error = error.error?.message || 'Erreur lors de la création de l\'étudiant';
          this.loading = false;
        }
      });
    }
  }
afterSave(): void {
  this.loading = false;
  this.showForm = false;
  this.etudiantForm.reset();
  this.submitted = false;
  this.loadEtudiants();
}

  importCSV(): void {
    if (!this.selectedFile) {
      this.error = 'Veuillez sélectionner un fichier CSV';
      return;
    }
    
    if (!this.importPromotion) {
      this.error = 'Veuillez spécifier une promotion pour cet import';
      return;
    }

    this.importInProgress = true;
    this.error = '';
    this.success = '';

    // Ajouter la promotion au formData
    this.etudiantService.importEtudiantsFromCSV(this.selectedFile, this.importPromotion).subscribe({
      next: (data) => {
        this.importInProgress = false;
        this.success = `${data.length} étudiants importés avec succès`;
        this.loadEtudiants();
        this.selectedFile = null;
        const fileInput = document.getElementById('csvFile') as HTMLInputElement;
        if (fileInput) {
          fileInput.value = '';
        }
      },
      error: (error) => {
        this.importInProgress = false;
        console.error('Erreur lors de l\'import:', error);
        this.error = error.error?.message || 'Erreur lors de l\'import du fichier CSV';
      }
    });
  }

  deleteEtudiant(id: number): void {
    if (!id) {
      this.error = 'ID de l\'étudiant invalide';
      return;
    }
    
    if (confirm('Êtes-vous sûr de vouloir supprimer cet étudiant ?')) {
      this.loading = true;
      this.error = '';
      this.success = '';
      
      this.etudiantService.deleteEtudiant(id).subscribe({
        next: () => {
          this.success = 'Étudiant supprimé avec succès';
          this.loading = false;
          this.loadEtudiants(); // Recharger la liste après suppression
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
          this.error = error.error?.message || 'Erreur lors de la suppression de l\'étudiant';
          this.loading = false;
        }
      });
    }
  }

  exportAttestation(etudiant: Etudiant): void {
    if (!etudiant || !etudiant.idEtudiant) {
      this.error = 'Données de l\'étudiant invalides';
      return;
    }
    
    try {
      this.etudiantService.exportAttestation(etudiant);
      // Pas besoin de message de succès ici car le PDF se télécharge automatiquement
    } catch (error) {
      console.error('Erreur lors de l\'export PDF:', error);
      this.error = 'Erreur lors de la génération de l\'attestation PDF';
    }
  }

  get f() { return this.etudiantForm.controls; }

  submitEtudiantForm(): void {
    this.submitted = true;
    if (this.etudiantForm.invalid) {
      this.error = 'Veuillez remplir tous les champs obligatoires';
      return;
    }
    
    const formData = this.etudiantForm.value;
    const etudiantData: any = {
      matricule: formData.matricule,
      nom: formData.nom,
      prenom: formData.prenom,
      prenomAR: formData.prenomAR,
      nomAR: formData.nomAR,
      email: formData.email,
      sexe: formData.sexe,
      NNI: formData.NNI,
      telephoneEtudiant: formData.telephoneEtudiant,
      telephoneCorrespondant: formData.telephoneCorrespondant,
      adresseResidence: formData.adresseResidence,
      lieuNaissance: formData.lieuNaissance,
      lieuNaissanceAR: formData.lieuNaissanceAR,
      promotion: formData.promotion
    };
    
    // Traitement des dates
    if (formData.dateNaissance) {
      etudiantData.dateNaissance = new Date(formData.dateNaissance);
    }
    
    if (formData.dateInscription) {
      etudiantData.dateInscription = new Date(formData.dateInscription);
    }
    
    // Traitement de l'année du bac
    if (formData.anneeObtentionBac) {
      const annee = parseInt(formData.anneeObtentionBac);
      if (!isNaN(annee)) {
        etudiantData.anneeObtentionBac = annee;
      }
    }
    
    // Gestion du département
    if (formData.departement) {
      const depId = parseInt(formData.departement);
      if (!isNaN(depId)) {
        etudiantData.departement = { 
          idDep: depId
        };
      }
    }

    this.loading = true;
    this.error = '';

    const req = this.editMode && this.currentEtudiantId
      ? this.etudiantService.updateEtudiant(this.currentEtudiantId, etudiantData)
      : this.etudiantService.createEtudiant(etudiantData);

    req.subscribe({
      next: () => {
        this.success = `Étudiant ${this.editMode ? 'modifié' : 'ajouté'} avec succès`;
        this.showForm = false;
        this.submitted = false;
        this.loadEtudiants();
        this.loading = false;
      },
      error: (error) => {
        console.error(`Erreur lors de la ${this.editMode ? 'modification' : 'création'}:`, error);
        this.error = error.error?.message || `Erreur lors de la ${this.editMode ? 'modification' : 'création'} de l'étudiant`;
        this.loading = false;
      }
    });
  }
  showAddEtudiantForm(): void {
    this.showForm = true;
    this.editMode = false;
    this.etudiantForm.reset();
    this.error = '';
    this.success = '';
  }
}