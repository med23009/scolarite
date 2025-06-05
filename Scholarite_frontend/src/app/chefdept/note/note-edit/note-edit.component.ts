import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NoteSemestrielle } from '../../../core/models/note-semestrielle.model';

@Component({
  selector: 'app-note-edit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './note-edit.component.html',
  styleUrls: ['./note-edit.component.scss']
})
export class NoteEditComponent implements OnInit, OnChanges {
  @Input() note: NoteSemestrielle | null = null;
  @Output() saveEvent = new EventEmitter<NoteSemestrielle>();
  @Output() cancelEvent = new EventEmitter<void>();

  editedNote: NoteSemestrielle | null = null;
  totalNote: number = 0;

  constructor() { }

  ngOnInit(): void {
    if (this.note) {
      this.editedNote = { ...this.note };
      this.updateTotal();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['note'] && changes['note'].currentValue) {
      this.editedNote = { ...changes['note'].currentValue };
      this.updateTotal();
    }
  }

  updateTotal(): void {
    if (!this.editedNote) return;

    const devoir = this.editedNote.noteDevoir || 0;
    const examen = this.editedNote.noteExamen || 0;
    const rattrapage = this.editedNote.noteRattrapage;

    this.totalNote = (rattrapage !== undefined && rattrapage > 0)
      ? rattrapage
      : +(devoir * 0.4 + examen * 0.6).toFixed(2);
  }

  saveNote(): void {
    if (this.editedNote) {
      this.saveEvent.emit(this.editedNote);
    }
  }

  cancel(): void {
    this.cancelEvent.emit();
  }
}