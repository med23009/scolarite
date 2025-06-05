// src/app/admin/note/note.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NoteService } from '../../core/services/chef_dept/note.service';
import { NoteSemestrielle } from '../../core/models/note-semestrielle.model';
import { ElementDeModule } from '../../core/models/element-module.model';
import { Semestre } from '../../core/models/semestre.model';

@Component({
  selector: 'app-note',
  templateUrl: './note.component.html',
  styleUrls: ['./note.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule]
})
export class NoteComponent implements OnInit {
  // Variables pour les deux sections
  elementModules: ElementDeModule[] = [];
  loading = false;
  error = '';
  success = '';

  // Variables pour la section d'importation
  importForm: FormGroup;
  submitting = false;
  selectedFile: File | null = null;
  importResult: any = null;
  currentYear = new Date().getFullYear();
  yearsList: number[] = [];
  showImportForm = false;
  semestres: Semestre[] = [];

  // Variables pour la section de liste
  notes: NoteSemestrielle[] = [];
  filteredNotes: NoteSemestrielle[] = [];
  searchTerm = '';
  selectedModule = '';

  constructor(
    private noteService: NoteService,
    private formBuilder: FormBuilder
  ) {
    // Générer la liste des années
    for (let i = 0; i < 6; i++) {
      this.yearsList.push(this.currentYear - i);
    }

    this.importForm = this.formBuilder.group({
      elementModule: ['', Validators.required],
      idSemestre: ['', Validators.required] // ID de l'objet Semestre (requis par le backend)
    });
  }

  ngOnInit(): void {
    this.loadElementModules();
    this.loadNotes();
    this.loadSemestres();
  }

  // Méthodes pour les deux sections
  loadElementModules(): void {
    this.loading = true;
    this.noteService.getAllModules().subscribe({
      next: (data) => {
        this.elementModules = data;
        this.loading = false;
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors du chargement des éléments de module';
        this.loading = false;
      }
    });
  }
  
  loadSemestres(): void {
    this.noteService.getAllSemestres().subscribe({
      next: (data) => {
        this.semestres = data;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des semestres:', error);
      }
    });
  }

  // Méthodes pour l'importation
  toggleImportForm(): void {
    this.showImportForm = !this.showImportForm;
    if (this.showImportForm) {
      // Réinitialiser le formulaire
      this.resetImportForm();
    }
  }

  onFileSelected(event: Event): void {
    const element = event.target as HTMLInputElement;
    if (element.files && element.files.length > 0) {
      this.selectedFile = element.files[0];
    }
  }

  onImportSubmit(): void {
    if (this.importForm.invalid) {
      return;
    }

    if (!this.selectedFile) {
      this.error = 'Veuillez sélectionner un fichier Excel';
      return;
    }

    this.error = '';
    this.success = '';
    this.submitting = true;
    this.importResult = null;

    const formValues = this.importForm.value;
    const selectedSemestre = this.semestres.find(s => s.idSemestre == formValues.idSemestre);
    
    this.noteService.importNotesFromExcel(
      this.selectedFile,
      formValues.elementModule,
      selectedSemestre?.annee || new Date().getFullYear(),
      parseInt(selectedSemestre?.semestre || '1'),
      formValues.idSemestre // Ajout du paramètre idSemestre
    ).subscribe({
      next: (result) => {
        this.submitting = false;
        this.importResult = result;
        
        if (result.importedNotes && result.importedNotes.length > 0) {
          this.success = `${result.importedNotes.length} notes ont été importées avec succès.`;
          this.loadNotes(); // Actualiser la liste des notes
        } else {
          this.success = 'Import réussi, mais aucune note n\'a été importée.';
        }
        
        // Réinitialiser le champ fichier
        const fileInput = document.getElementById('noteFile') as HTMLInputElement;
        if (fileInput) {
          fileInput.value = '';
        }
        this.selectedFile = null;
      },
      error: (error) => {
        this.submitting = false;
        this.error = error.error || 'Erreur lors de l\'import du fichier Excel';
      }
    });
  }

  resetImportForm(): void {
    this.importForm.reset({
      elementModule: '',
      idSemestre: ''
    });
    this.selectedFile = null;
    this.error = '';
    this.success = '';
    this.importResult = null;
    
    const fileInput = document.getElementById('noteFile') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  // Méthodes pour la liste des notes
  loadNotes(): void {
    this.loading = true;
    this.noteService.getAllNotes().subscribe({
      next: (data) => {
        this.notes = data;
        this.filteredNotes = [...this.notes];
        this.loading = false;
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors du chargement des notes';
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    this.filteredNotes = this.notes.filter(note => {
      // Filtre par module - check both direct property and nested object
      if (this.selectedModule && 
          (note.codeEM !== this.selectedModule && note.elementModule?.codeEM !== this.selectedModule)) {
        return false;
      }
      
      // Filtre par terme de recherche
      if (this.searchTerm) {
        const search = this.searchTerm.toLowerCase();
        return (
          // Check matricule in both places
          (note.matriculeEtudiant?.toLowerCase().includes(search) || false) ||
          (note.etudiant?.matricule?.toLowerCase().includes(search) || false) ||
          // Check module code in both places
          (note.codeEM?.toLowerCase().includes(search) || false) ||
          (note.elementModule?.codeEM?.toLowerCase().includes(search) || false) ||
          // Check module name
          (note.elementModule?.intitule?.toLowerCase().includes(search) || false) ||
          // Check student name
          (note.etudiant?.nom?.toLowerCase().includes(search) || false) ||
          (note.etudiant?.prenom?.toLowerCase().includes(search) || false)
        );
      }
      
      return true;
    });
  }

  onModuleChange(): void {
    this.applyFilter();
  }

  onSearch(): void {
    this.applyFilter();
  }

  deleteNote(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette note ?')) {
      this.noteService.deleteNote(id).subscribe({
        next: () => {
          this.success = 'Note supprimée avec succès';
          this.loadNotes();
        },
        error: (error) => {
          this.error = error.message || 'Erreur lors de la suppression de la note';
        }
      });
    }
  }

  calculateTotal(note: NoteSemestrielle): number {
    // Calculer la note finale selon la logique de votre école
    if (note.noteRattrapage && note.noteRattrapage > 0) {
      return note.noteRattrapage;
    }
    
    const totalDevoir = (note.noteDevoir || 0) * 0.4;
    const totalExamen = (note.noteExamen || 0) * 0.6;
    return +(totalDevoir + totalExamen).toFixed(2);
  }

  getModuleLabel(codeEM: string): string {
    if (!codeEM) return 'Module inconnu';
    const module = this.elementModules.find(m => m.codeEM === codeEM);
    return module ? `${module.codeEM} - ${module.intitule}` : codeEM;
  }
}