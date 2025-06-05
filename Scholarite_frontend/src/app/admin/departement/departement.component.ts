import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { DepartementService } from '../../core/services/admin/departement.service';
import { UserService } from '../../core/services/auth/user.service';
import { Departement } from '../../core/models/departement.model';
import { User } from '../../core/models/user.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-departement',
  standalone: true,
  templateUrl: './departement.component.html',
  styleUrls: ['./departement.component.scss'],
  imports: [CommonModule, ReactiveFormsModule]
})
export class DepartementComponent implements OnInit {
  departements: Departement[] = [];
  departementForm!: FormGroup;
  users: User[] = [];
  currentDepartementId: number | null = null;

  showForm = false;
  editMode = false;
  submitted = false;
  loading = false;
  success = '';
  error = '';
  selectedFile: File | null = null;
  importInProgress = false;

  constructor(
    private fb: FormBuilder,
    private departementService: DepartementService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDepartements();
    this.loadUsers();

    // S'abonner aux événements de rafraîchissement
    this.departementService.refresh$.subscribe(() => {
      console.log('[DepartementComponent] Notification de rafraîchissement reçue');
      this.loadDepartements();
    });
  }

  initForm(): void {
    this.departementForm = this.fb.group({
      codeDep: ['', [Validators.required, Validators.maxLength(10)]],
      intitule: ['', Validators.required],
      description: [''],
      nom_responsable: [''],
      responsable: [null]
    });
  }

  get f() {
    return this.departementForm.controls;
  }

  loadDepartements(): void {
    this.loading = true;
    // Ajouter un timestamp pour éviter la mise en cache
    this.departementService.getAll().subscribe({
      next: (data) => {
        console.log('Départements chargés:', data);
        this.departements = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des départements:', err);
        this.error = 'Erreur lors du chargement des départements';
        this.loading = false;
      }
    });
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (data) => this.users = data,
      error: () => this.error = 'Erreur lors du chargement des utilisateurs'
    });
  }

  showAddDepartementForm(): void {
    this.showForm = true;
    this.editMode = false;
    this.departementForm.reset();
  }

  editDepartement(dep: Departement): void {
    this.showForm = true;
    this.editMode = true;
    this.currentDepartementId = dep.idDep!;
    this.departementForm.patchValue({
      codeDep: dep.codeDep,
      intitule: dep.intitule,
      description: dep.description,
      nom_responsable: dep.nom_responsable,
      responsable: dep.responsable?.idMembre || null
    });
  }

  submitDepartementForm(): void {
    this.submitted = true;
    if (this.departementForm.invalid) return;

    const data = { ...this.departementForm.value };
    if (data.responsable) data.responsable = { idMembre: data.responsable };

    const req = this.editMode && this.currentDepartementId
      ? this.departementService.update(this.currentDepartementId, data)
      : this.departementService.create(data);

    this.loading = true;
    req.subscribe({
      next: () => {
        this.success = `Département ${this.editMode ? 'modifié' : 'ajouté'} avec succès`;
        this.showForm = false;
        this.submitted = false;
        this.loadDepartements();
        this.loading = false;
      },
      error: () => this.error = 'Erreur lors de la sauvegarde'
    });
  }

  deleteDepartement(id: number): void {
    if (!confirm('Supprimer ce département ?')) return;
    this.departementService.delete(id).subscribe({
      next: () => this.loadDepartements(),
      error: () => this.error = 'Erreur lors de la suppression'
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
  }

  importDepartementCSV(): void {
    if (!this.selectedFile) return;
    this.importInProgress = true;
    this.departementService.importCSV(this.selectedFile).subscribe({
      next: () => {
        this.loadDepartements();
        this.success = 'Importation réussie';
        this.importInProgress = false;
      },
      error: () => {
        this.error = "Erreur lors de l'import";
        this.importInProgress = false;
      }
    });
  }

  cancelForm(): void {
    this.showForm = false;
    this.submitted = false;
    this.departementForm.reset();
  }

  getUserFullName(user: User): string {
    return `${user.prenom} ${user.nom}`;
  }
}
