import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NoteService } from '../../../core/services/chef_dept/note.service';
import { ElementDeModule } from '../../../core/models/element-module.model';
import { Semestre } from '../../../core/models/semestre.model';

@Component({
  selector: 'app-note-import',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './note-import.component.html',
  styleUrls: ['./note-import.component.scss']
})
export class NoteImportComponent implements OnInit {
  @Input() semestres: Semestre[] = [];
  @Input() elementModules: ElementDeModule[] = [];
  @Output() importSuccess = new EventEmitter<any>();
  @Output() importError = new EventEmitter<string>();
  @Output() closeImport = new EventEmitter<void>();

  importForm!: FormGroup;
  filteredElementModules: ElementDeModule[] = [];
  selectedFile: File | null = null;
  submitting = false;
  formSubmitted = false;
  importResult: any;
  currentYear = new Date().getFullYear();

  constructor(
    private formBuilder: FormBuilder,
    private noteService: NoteService
  ) {}

  ngOnInit(): void {
    this.initImportForm();
  }

  initImportForm() {
    this.importForm = this.formBuilder.group({
      elementModule: ['', Validators.required],
      idSemestre: ['', Validators.required]
    });
  }

  onFileSelected(event: any) {
    if (event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
    }
  }

  onSemestreChange() {
    const idSemestre = this.importForm.get('idSemestre')?.value;
    if (!idSemestre) {
      this.filteredElementModules = [];
      return;
    }
    
    // Trouver le semestre sélectionné
    const selectedSemestre = this.semestres.find(s => s.idSemestre === +idSemestre);
    if (!selectedSemestre) {
      this.filteredElementModules = [];
      return;
    }
    
    // Filtrer les éléments de module qui appartiennent au semestre sélectionné
    this.filteredElementModules = this.elementModules.filter(em => {
      // Vérifier les différentes façons dont un EM peut être lié à un semestre
      
      // 1. Vérifier le champ id_semestre (relation directe avec Semestre)
      if (em.id_semestre && typeof em.id_semestre === 'object' && 'idSemestre' in em.id_semestre) {
        if (em.id_semestre.idSemestre === +idSemestre) {
          return true;
        }
      }
      
      // 2. Vérifier si le champ semestre (numéro) correspond au numéro du semestre sélectionné
      const semestreNum = selectedSemestre.semestre;
      if (em.semestre !== undefined && typeof semestreNum === 'number') {
        if (typeof em.semestre === 'number' && em.semestre === semestreNum) {
          return true;
        }
      }
      
      return false;
    });
    
    // Réinitialiser la sélection de l'élément de module
    this.importForm.get('elementModule')?.setValue('');
    
    // Si aucun module n'est trouvé, essayer une approche alternative
    if (this.filteredElementModules.length === 0) {
      // Approche plus simple: montrer tous les modules disponibles
      this.filteredElementModules = this.elementModules;
      
      // Ajouter un message d'information pour l'utilisateur
      setTimeout(() => {
        alert('Aucun module n\'a été trouvé pour ce semestre avec le filtrage automatique. Tous les modules sont affichés - veuillez sélectionner celui qui correspond au semestre ' + selectedSemestre.semestre);
      }, 100);
    }
  }

  onImportSubmit(): void {
    this.formSubmitted = true;
    
    if (!this.importForm.valid || !this.selectedFile) {
      this.importError.emit('Veuillez remplir tous les champs et sélectionner un fichier');
      return;
    }

    this.submitting = true;

    // Récupérer le semestre sélectionné
    const idSemestre = this.importForm.get('idSemestre')?.value;
    if (!idSemestre) {
      this.importError.emit('Veuillez sélectionner un semestre valide');
      this.submitting = false;
      return;
    }
    
    // Trouver le semestre sélectionné pour obtenir les informations nécessaires
    const selectedSemestre = this.semestres.find(s => s.idSemestre === +idSemestre);
    if (!selectedSemestre) {
      this.importError.emit('Semestre invalide');
      this.submitting = false;
      return;
    }

    // Ensure we have valid numbers for all parameters
    const elementModuleId = parseInt(this.importForm.get('elementModule')?.value) || 0;
    const annee = selectedSemestre.annee || new Date().getFullYear();
    // Make sure semestre is a valid number
    let semestreNum = 1; // Default value
    if (typeof selectedSemestre.semestre === 'string') {
      semestreNum = parseInt(selectedSemestre.semestre) || 1;
    } else if (typeof selectedSemestre.semestre === 'number') {
      semestreNum = selectedSemestre.semestre || 1;
    }
    const semestreId = parseInt(idSemestre) || 0;
    
    if (elementModuleId === 0 || semestreId === 0) {
      this.importError.emit('Veuillez sélectionner un module et un semestre valides');
      this.submitting = false;
      return;
    }
    
    this.noteService.importNotesFromExcel(this.selectedFile, 
      elementModuleId,
      annee,
      semestreNum,
      semestreId).subscribe({
      next: (result) => {
        this.submitting = false;
        this.importResult = result;

        if (result.importedNotes?.length > 0) {
          this.importSuccess.emit({
            message: `${result.importedNotes.length} notes importées.`,
            result: result
          });
        } else {
          this.importSuccess.emit({
            message: 'Import réussi, mais aucune note importée.',
            result: result
          });
        }

        (document.getElementById('noteFile') as HTMLInputElement).value = '';
        this.selectedFile = null;
      },
      error: (error) => {
        this.submitting = false;
        this.importError.emit(error.error || 'Erreur lors de l\'import.');
      }
    });
  }

  resetImportForm(): void {
    this.importForm.reset({
      annee: this.currentYear,
      semestre: 1,
      idSemestre: '',
      elementModule: ''
    });
    this.selectedFile = null;
    this.importResult = null;

    const fileInput = document.getElementById('noteFile') as HTMLInputElement;
    if (fileInput) fileInput.value = '';
  }

  close(): void {
    this.closeImport.emit();
  }
}