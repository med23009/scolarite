// src/app/admin/note/note.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NoteService } from '../../core/services/chef_dept/note.service';
import { NoteSemestrielle } from '../../core/models/note-semestrielle.model';
import { ElementDeModule } from '../../core/models/element-module.model';
import { Semestre } from '../../core/models/semestre.model';

// Importer les nouveaux composants
import { NoteListComponent } from './note-list/note-list.component';
import { NoteImportComponent } from './note-import/note-import.component';
import { NoteEditComponent } from './note-edit/note-edit.component';

@Component({
  selector: 'app-note',
  templateUrl: './note.component.html',
  styleUrls: ['./note.component.scss'],
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    NoteListComponent, // Ajouter ici
    NoteImportComponent, // Ajouter ici
    NoteEditComponent // Ajouter ici
  ]
})
export class NoteComponent implements OnInit {
  elementModules: ElementDeModule[] = [];
  semestres: Semestre[] = [];
  notes: NoteSemestrielle[] = [];
  
  error: string | null = null;
  success: string | null = null;
  loading = false;

  showImportForm = false;
  isEditing = false;
  selectedNoteForEdit: NoteSemestrielle | null = null;

  constructor(
    private noteService: NoteService
  ) {}

  ngOnInit(): void {
    this.loadElementModules();
    this.loadSemestres();
    this.loadNotes();
  }

  loadElementModules(): void {
    this.loading = true;
    this.noteService.getAllModules().subscribe({
      next: (data) => {
        this.elementModules = data;
        if (this.semestres.length > 0) this.loadNotes(); 
        else this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Erreur lors du chargement des modules.';
        this.loading = false;
      }
    });
  }

  loadSemestres(): void {
    this.noteService.getAllSemestres().subscribe({
      next: (data) => {
        this.semestres = data;
        if (this.elementModules.length > 0) this.loadNotes();
      },
      error: (err) => {
        this.error = err.message || 'Erreur lors du chargement des semestres.';
        this.loading = false; 
      }
    });
  }

  loadNotes(): void {
    this.loading = true;
    this.noteService.getAllNotes().subscribe({
      next: (data) => {
        this.notes = data;
        this.loading = false;
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error = err.message || 'Erreur lors du chargement des notes.';
        this.loading = false;
        this.clearMessagesAfterDelay();
      }
    });
  }

  toggleImportForm(): void {
    this.showImportForm = !this.showImportForm;
    if (this.showImportForm) {
      this.isEditing = false;
      this.selectedNoteForEdit = null;
    }
    this.clearMessages();
  }

  handleImportSuccess(event: { message: string, result: any }): void {
    this.success = event.message;
    this.showImportForm = false;
    this.loadNotes();
    this.clearMessagesAfterDelay();
  }

  handleImportError(errorMessage: string): void {
    this.error = errorMessage;
    this.clearMessagesAfterDelay();
  }

  handleCloseImport(): void {
    this.showImportForm = false;
    this.clearMessages();
  }

  handleEditNoteEvent(note: NoteSemestrielle): void {
    this.selectedNoteForEdit = { ...note };
    this.isEditing = true;
    this.showImportForm = false;
    this.clearMessages();
  }

  handleSaveNote(noteToSave: NoteSemestrielle): void {
    if (!noteToSave || noteToSave.idNote === undefined) {
      this.error = 'Note sélectionnée invalide pour la mise à jour.';
      this.clearMessagesAfterDelay();
      return;
    }
    this.loading = true;
    this.noteService.updateNote(noteToSave.idNote, noteToSave).subscribe({
      next: () => {
        this.success = 'Note mise à jour avec succès.';
        this.isEditing = false;
        this.selectedNoteForEdit = null;
        this.loadNotes();
        this.loading = false;
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error = err.message || 'Erreur lors de la mise à jour de la note.';
        this.loading = false;
        this.clearMessagesAfterDelay();
      }
    });
  }

  handleCancelEdit(): void {
    this.isEditing = false;
    this.selectedNoteForEdit = null;
    this.clearMessages();
  }

  private clearMessages(): void {
    this.error = null;
    this.success = null;
  }

  private clearMessagesAfterDelay(delay: number = 5000): void {
    setTimeout(() => {
      this.clearMessages();
    }, delay);
  }
}
