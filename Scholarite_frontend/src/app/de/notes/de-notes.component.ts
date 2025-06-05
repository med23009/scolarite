import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms'; 
import { Observable, of } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

import { Departement } from '../../core/models/departement.model';
import { Pole } from '../../core/models/pole.model';
import { NoteSemestrielle } from '../../core/models/note-semestrielle.model';
import { ElementDeModule } from '../../core/models/element-module.model';

import { DepartementService } from '../../core/services/admin/departement.service';
import { PoleService } from '../../core/services/admin/pole.service';
import { DENoteService } from '../../core/services/de/de-note.service';
// Removed MessageService import

@Component({
  selector: 'app-de-notes',
  standalone: true, 
  imports: [
    CommonModule, 
    ReactiveFormsModule 
  ],
  templateUrl: './de-notes.component.html',
  styleUrls: ['./de-notes.component.scss']
})
export class DENotesComponent implements OnInit {
  noteManagementForm: FormGroup;
  departments$: Observable<Departement[]> = of([]);
  poles$: Observable<Pole[]> = of([]);
  elementModules: ElementDeModule[] = [];
  notes$: Observable<NoteSemestrielle[]> = of([]);

  isLoadingDepartments = false;
  isLoadingPoles = false;
  isLoadingElementModules = false;
  isLoadingNotes = false;
  isImporting = false; 
  isUpdating = false; 

  selectedDepartmentId: string | null = null;
  selectedPoleId: string | null = null;
  selectedElementModuleCode: string | null = null;

  editingNote: NoteSemestrielle | null = null;
  editForm: FormGroup;

  // Added for direct message handling
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private readonly fb: FormBuilder,
    private readonly departementService: DepartementService,
    private readonly poleService: PoleService,
    private readonly deNoteService: DENoteService
    // Removed messageService parameter
  ) {
    this.noteManagementForm = this.fb.group({
      filterType: ['department', Validators.required],
      department: [null], 
      pole: [null],       
      elementModule: [null, Validators.required] 
    });

    this.editForm = this.fb.group({
      idNote: [{ value: '', disabled: true }],
      noteDevoir: ['', [Validators.min(0), Validators.max(20)]],
      noteExamen: ['', [Validators.min(0), Validators.max(20)]],
      noteRattrapage: ['', [Validators.min(0), Validators.max(20)]]
    });
  }

  ngOnInit(): void {
    this.loadInitialData();
    this.setupFormListeners();
  }

  // Helper method to replace messageService.add
  private showMessage(message: string, type: 'success' | 'error' | 'info' | 'warn'): void {
    console.log(`[${type.toUpperCase()}] ${message}`);
    
    if (type === 'error') {
      this.errorMessage = message;
      // Auto-clear after 5 seconds
      setTimeout(() => this.errorMessage = null, 5000);
    } else if (type === 'success') {
      this.successMessage = message;
      // Auto-clear after 5 seconds
      setTimeout(() => this.successMessage = null, 5000);
    }
    // For info and warn, we just log to console for now
  }

  loadInitialData(): void {
    this.isLoadingDepartments = true;
    this.departments$ = this.departementService.getAll().pipe(
      finalize(() => this.isLoadingDepartments = false),
      catchError(err => {
        this.showMessage('Erreur lors du chargement des départements.', 'error');
        return of([]);
      })
    );

    this.isLoadingPoles = true;
    this.poles$ = this.poleService.getAll().pipe(
      finalize(() => this.isLoadingPoles = false),
      catchError(err => {
        this.showMessage('Erreur lors du chargement des pôles.', 'error');
        return of([]);
      })
    );
  }

  setupFormListeners(): void {
    this.noteManagementForm.get('filterType')?.valueChanges.subscribe(type => {
      this.resetSelectionsForFilterTypeChange();
      if (type === 'department') {
        this.noteManagementForm.get('pole')?.setValue(null, { emitEvent: false });
        this.noteManagementForm.get('pole')?.clearValidators();
        this.noteManagementForm.get('department')?.setValidators(Validators.required);
      } else if (type === 'pole') {
        this.noteManagementForm.get('department')?.setValue(null, { emitEvent: false });
        this.noteManagementForm.get('department')?.clearValidators();
        this.noteManagementForm.get('pole')?.setValidators(Validators.required);
      }
      this.noteManagementForm.get('pole')?.updateValueAndValidity({ emitEvent: false });
      this.noteManagementForm.get('department')?.updateValueAndValidity({ emitEvent: false });
    });

    this.noteManagementForm.get('department')?.valueChanges.subscribe(departmentId => {
      this.elementModules = [];
      this.notes$ = of([]);
      this.selectedElementModuleCode = null;
      this.noteManagementForm.get('elementModule')?.setValue(null, { emitEvent: false });
      if (departmentId) {
        this.selectedDepartmentId = departmentId;
        this.selectedPoleId = null; 
        this.loadElementModulesForDepartment(departmentId);
      } else {
        this.selectedDepartmentId = null;
      }
    });

    this.noteManagementForm.get('pole')?.valueChanges.subscribe(poleId => {
      this.elementModules = [];
      this.notes$ = of([]);
      this.selectedElementModuleCode = null;
      this.noteManagementForm.get('elementModule')?.setValue(null, { emitEvent: false });
      if (poleId) {
        this.selectedPoleId = poleId;
        this.selectedDepartmentId = null; 
        this.loadElementModulesForPole(poleId);
      } else {
        this.selectedPoleId = null;
      }
    });

    this.noteManagementForm.get('elementModule')?.valueChanges.subscribe(codeEM => {
      this.notes$ = of([]);
      if (codeEM) {
        this.selectedElementModuleCode = codeEM;
        this.loadNotesForElementModule(codeEM);
      } else {
        this.selectedElementModuleCode = null;
      }
    });
  }

  resetSelectionsForFilterTypeChange(): void {
    this.selectedDepartmentId = null;
    this.selectedPoleId = null;
    this.selectedElementModuleCode = null;
    this.elementModules = [];
    this.notes$ = of([]);
    this.noteManagementForm.get('department')?.setValue(null);
    this.noteManagementForm.get('pole')?.setValue(null, { emitEvent: false });
    this.noteManagementForm.get('elementModule')?.setValue(null, { emitEvent: false });
    this.errorMessage = null;
    this.successMessage = null;
  }

  loadElementModulesForDepartment(departmentId: string): void {
    this.isLoadingElementModules = true;
    this.deNoteService.getElementModulesByDepartmentId(+departmentId).pipe(
      tap(data => {
        this.elementModules = data;
        if (data.length === 0) {
          this.showMessage('Aucun élément de module trouvé pour ce département.', 'info');
        }
      }),
      catchError(err => {
        this.showMessage('Erreur lors du chargement des éléments de module.', 'error');
        return of([]);
      }),
      finalize(() => this.isLoadingElementModules = false)
    ).subscribe();
  }

  loadElementModulesForPole(poleId: string): void {
    this.isLoadingElementModules = true;
    this.deNoteService.getElementModulesByPoleId(+poleId).pipe(
      tap(data => {
        this.elementModules = data;
        if (data.length === 0) {
          this.showMessage('Aucun élément de module trouvé pour ce pôle.', 'info');
        }
      }),
      catchError(err => {
        this.showMessage('Erreur lors du chargement des éléments de module.', 'error');
        return of([]);
      }),
      finalize(() => this.isLoadingElementModules = false)
    ).subscribe();
  }

  loadNotesForElementModule(codeEM: string): void {
    this.isLoadingNotes = true;
    this.notes$ = this.deNoteService.getNotesByElementModuleCode(codeEM).pipe(
      tap(data => {
        if (data.length === 0) {
          this.showMessage('Aucune note trouvée pour cet élément de module.', 'info');
        }
      }),
      catchError(err => {
        this.showMessage('Erreur lors du chargement des notes.', 'error');
        return of([]);
      }),
      finalize(() => this.isLoadingNotes = false)
    );
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const filterType = this.noteManagementForm.get('filterType')?.value;
      const id = filterType === 'department' ? this.selectedDepartmentId : this.selectedPoleId;

      if (file && id && (this.selectedDepartmentId || this.selectedPoleId)) {
        this.isImporting = true;
        this.deNoteService.importNotes(file, filterType, id).pipe(
          tap(() => {
            this.showMessage('Notes importées avec succès!', 'success');
            if (this.selectedElementModuleCode) {
              this.loadNotesForElementModule(this.selectedElementModuleCode); 
            }
          }),
          catchError(err => {
            this.showMessage('Erreur lors de l\'importation des notes.', 'error');
            return of(null);
          }),
          finalize(() => {
            this.isImporting = false;
            if (input) input.value = ''; 
          })
        ).subscribe();
      } else {
        this.showMessage('Veuillez sélectionner un département ou un pôle avant d\'importer.', 'warn');
      }
    }
  }

  openEditModal(note: NoteSemestrielle): void {
    this.editingNote = { ...note };
    this.editForm.patchValue({
      idNote: note.idNote,
      noteDevoir: note.noteDevoir,
      noteExamen: note.noteExamen,
      noteRattrapage: note.noteRattrapage
    });
    // Logic to show modal 
    // For example: this.showEditModal = true;
  }

  cancelEdit(): void {
    this.editingNote = null;
    this.editForm.reset();
    // Logic to hide modal
    // For example: this.showEditModal = false;
  }

  submitEdit(): void {
    if (!this.editingNote || !this.editingNote.idNote) {
      this.showMessage('Aucune note sélectionnée pour la modification.', 'error');
      return;
    }
    if (this.editForm.invalid) {
      this.showMessage('Formulaire de modification invalide.', 'error');
      // Optionally mark fields as touched to show errors
      Object.values(this.editForm.controls).forEach(control => control.markAsTouched());
      return;
    }

    const updatedNoteData: Partial<NoteSemestrielle> = this.editForm.value;

    this.isUpdating = true;
    this.deNoteService.updateNote(this.editingNote.idNote, updatedNoteData).pipe(
      tap(() => {
        this.showMessage('Note mise à jour avec succès!', 'success');
        if (this.selectedElementModuleCode) {
          this.loadNotesForElementModule(this.selectedElementModuleCode); 
        }
        this.cancelEdit(); 
      }),
      catchError(err => {
        this.showMessage('Erreur lors de la mise à jour de la note.', 'error');
        return of(null);
      }),
      finalize(() => this.isUpdating = false)
    ).subscribe();
  }
}
