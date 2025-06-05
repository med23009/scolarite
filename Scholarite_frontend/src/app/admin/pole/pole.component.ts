import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { PoleService } from '../../core/services/admin/pole.service';
import { UserService } from '../../core/services/auth/user.service';
import { Pole } from '../../core/models/pole.model';
import { User } from '../../core/models/user.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pole',
  standalone: true,
  templateUrl: './pole.component.html',
  styleUrls: ['./pole.component.scss'],
  imports: [CommonModule, ReactiveFormsModule]
})
export class PoleComponent implements OnInit {
  poles: Pole[] = [];
  poleForm!: FormGroup;
  users: User[] = [];
  currentPoleId: number | null = null;

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
    private poleService: PoleService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadPoles();
    this.loadUsers();
  }

  initForm(): void {
    this.poleForm = this.fb.group({
      codePole: ['', [Validators.required, Validators.maxLength(10)]],
      intitule: ['', Validators.required],
      description: [''],
      nom_responsable: [''],
      responsable: [null]
    });
  }

  get f() {
    return this.poleForm.controls;
  }

  loadPoles(): void {
    this.loading = true;
    this.poleService.getAll().subscribe({
      next: (data) => {
        this.poles = data;
        this.loading = false;
      },
      error: () => this.error = 'Erreur lors du chargement des pôles'
    });
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (data) => this.users = data,
      error: () => this.error = 'Erreur lors du chargement des utilisateurs'
    });
  }

  showAddPoleForm(): void {
    this.showForm = true;
    this.editMode = false;
    this.poleForm.reset();
    this.submitted = false;
  }

  editPole(pole: Pole): void {
    this.showForm = true;
    this.editMode = true;
    this.currentPoleId = pole.idPole!;
    this.poleForm.patchValue({
      codePole: pole.codePole,
      intitule: pole.intitule,
      description: pole.description,
      nom_responsable: pole.nom_responsable,
      responsable: pole.responsable?.idMembre || null
    });
  }

  submitPoleForm(): void {
    this.submitted = true;
    if (this.poleForm.invalid) return;

    const data = { ...this.poleForm.value };
    if (data.responsable) data.responsable = { idMembre: data.responsable };

    const req = this.editMode && this.currentPoleId
      ? this.poleService.update(this.currentPoleId, data)
      : this.poleService.create(data);

    this.loading = true;
    req.subscribe({
      next: () => {
        this.success = `Pôle ${this.editMode ? 'modifié' : 'ajouté'} avec succès`;
        this.showForm = false;
        this.submitted = false;
        this.poleForm.reset();
        this.loadPoles();
        this.loading = false;
      },
      error: () => this.error = 'Erreur lors de la sauvegarde'
    });
  }

  deletePole(id: number): void {
    if (!confirm('Supprimer ce pôle ?')) return;
    this.poleService.delete(id).subscribe({
      next: () => this.loadPoles(),
      error: () => this.error = 'Erreur lors de la suppression'
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
  }

  importPoleCSV(): void {
    if (!this.selectedFile) return;
    this.importInProgress = true;
    this.poleService.importCSV(this.selectedFile).subscribe({
      next: () => {
        this.success = 'Importation réussie';
        this.loadPoles();
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
    this.poleForm.reset();
    this.submitted = false;
  }

  getUserFullName(user: User): string {
    return `${user.prenom} ${user.nom}`;
  }
}
