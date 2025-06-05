import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { SemestreService } from '../../core/services/admin/semestre.service';
import { Semestre } from '../../core/models/semestre.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-semestre',
  standalone: true,
  templateUrl: './semestre.component.html',
  styleUrls: ['./semestre.component.scss'],
  imports: [CommonModule, ReactiveFormsModule]
})
export class SemestreComponent implements OnInit {
  semestres: Semestre[] = [];
  semestreForm!: FormGroup;
  currentSemestreId: number | null = null;

  showForm = false;
  editMode = false;
  submitted = false;
  loading = false;
  success = '';
  error = '';
  selectedFile: File | null = null;
  importInProgress = false;

  constructor(private fb: FormBuilder, private semestreService: SemestreService) {}

  ngOnInit(): void {
    this.initForm();
    this.loadSemestres();
  }

  initForm(): void {
    this.semestreForm = this.fb.group({
      semestre: ['', Validators.required],
      annee: [new Date().getFullYear(), [Validators.required, Validators.min(2000)]],
      nombreSemaines: [15, [Validators.required, Validators.min(1)]],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      creditSpecialite: ['', [Validators.required, Validators.min(0)]],
      creditHE: ['', [Validators.required, Validators.min(0)]],
      creditST: ['', [Validators.required, Validators.min(0)]],
    });
  }

  get f() {
    return this.semestreForm.controls;
  }

  showAddSemestreForm(): void {
    this.showForm = true;
    this.editMode = false;
    this.semestreForm.reset();
    this.semestreForm.enable();

    this.submitted = false;
  }

  editSemestre(sem: Semestre): void {
    this.showForm = true;
    this.editMode = true;
    this.currentSemestreId = sem.idSemestre!;
    this.semestreForm.enable();
    this.semestreForm.get('semestre')?.disable();

    this.semestreForm.patchValue({
      semestre: sem.semestre,
      annee: sem.annee,
      nombreSemaines: sem.nombreSemaines,
      dateDebut: this.formatDateForInput(sem.dateDebut),
      dateFin: this.formatDateForInput(sem.dateFin),
      creditSpecialite: sem.creditSpecialite,
      creditHE: sem.creditHE,
      creditST: sem.creditST,
    }, { emitEvent: false });
  }

  submitSemestreForm(): void {
    this.submitted = true;
    if (this.semestreForm.invalid) return;

    const formData = {
      ...this.semestreForm.getRawValue(),
      semestre: this.semestreForm.get('semestre')?.value
    };

    const request = this.editMode && this.currentSemestreId
      ? this.semestreService.update(this.currentSemestreId, formData)
      : this.semestreService.create(formData);

    this.loading = true;
    request.subscribe({
      next: () => {
        this.success = `Semestre ${this.editMode ? 'modifié' : 'ajouté'} avec succès`;
        this.showForm = false;
        this.semestreForm.reset();
        this.loadSemestres();
        this.loading = false;
        this.submitted = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Erreur lors de la sauvegarde';
      }
    });
  }


  loadSemestres(): void {
    this.loading = true;
    this.semestreService.getAll().subscribe({
      next: (data) => {
        this.semestres = data;
        this.loading = false;
      },
      error: () => this.error = 'Erreur lors du chargement des semestres'
    });
  }

  deleteSemestre(id: number): void {
    if (!confirm('Supprimer ce semestre ?')) return;

    this.semestreService.delete(id).subscribe({
      next: () => {
        this.success = 'Semestre supprimé avec succès';
        this.loadSemestres();
      },
      error: () => {
        this.error = 'Erreur lors de la suppression';
      }
    });
  }

    onFileSelected(event: any): void {
      this.selectedFile = event.target.files[0];
    }

  importSemestreCSV(): void {
    if (!this.selectedFile) return;
    this.importInProgress = true;
    this.semestreService.importCSV(this.selectedFile).subscribe({
      next: () => {
        this.success = 'Importation réussie';
        this.loadSemestres();
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
    this.semestreForm.reset();
    this.submitted = false;
  }

  formatDateForInput(date: string | Date): string {
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }
}
