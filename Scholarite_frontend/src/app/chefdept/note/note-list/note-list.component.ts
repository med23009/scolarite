import { Component, EventEmitter, Input, type OnChanges, type OnInit, Output, type SimpleChanges } from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormsModule } from "@angular/forms"
import type { NoteSemestrielle } from "../../../core/models/note-semestrielle.model"
import type { ElementDeModule } from "../../../core/models/element-module.model"

@Component({
  selector: "app-note-list",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./note-list.component.html",
  styleUrls: ["./note-list.component.scss"],
})
export class NoteListComponent implements OnInit, OnChanges {
  @Input() notes: NoteSemestrielle[] = []
  @Input() elementModules: ElementDeModule[] = []
  @Input() loading = false
  @Output() editNoteEvent = new EventEmitter<NoteSemestrielle>()

  filteredNotes: NoteSemestrielle[] = []
  searchTerm = ""
  selectedModule = ""
  
  // Propriétés de pagination
  currentPage = 1
  itemsPerPage = 20
  totalPages = 1
  paginatedNotes: NoteSemestrielle[] = []
  currentModuleIndex = 0
  modulesWithNotes: ElementDeModule[] = []

  constructor() {}

  ngOnInit(): void {
    this.applyFilter()
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["notes"]) {
      this.applyFilter()
    }
  }

  onModuleChange(): void {
    this.currentPage = 1
    this.currentModuleIndex = 0
    this.applyFilter()
  }

  onSearch(): void {
    this.currentPage = 1
    this.currentModuleIndex = 0
    this.applyFilter()
  }

  editNote(note: NoteSemestrielle): void {
    this.editNoteEvent.emit(note)
  }

  applyFilter(): void {
    const search = this.searchTerm.toLowerCase()

    this.filteredNotes = this.notes.filter((note) => {
      const matchesModule =
        !this.selectedModule ||
        note.codeEM === this.selectedModule ||
        note.elementModule?.codeEM === this.selectedModule
      const matchesSearch =
        !this.searchTerm ||
        note.matriculeEtudiant?.toLowerCase().includes(search) ||
        note.etudiant?.matricule?.toLowerCase().includes(search) ||
        note.codeEM?.toLowerCase().includes(search) ||
        note.elementModule?.codeEM?.toLowerCase().includes(search) ||
        note.elementModule?.intitule?.toLowerCase().includes(search) ||
        note.etudiant?.nom?.toLowerCase().includes(search) ||
        note.etudiant?.prenom?.toLowerCase().includes(search)
      return matchesModule && matchesSearch
    })

    // Filtrer les modules qui ont des notes
    this.modulesWithNotes = this.elementModules.filter(module => 
      this.filteredNotes.some(note => 
        note.codeEM === module.codeEM || note.elementModule?.codeEM === module.codeEM
      )
    )

    this.updatePagination()
  }

  updatePagination(): void {
    if (this.selectedModule) {
      // Si un module est sélectionné, on utilise la pagination normale avec limite de 20 éléments
      const moduleNotes = this.filteredNotes.filter(
        note => note.codeEM === this.selectedModule || note.elementModule?.codeEM === this.selectedModule
      )
      this.totalPages = Math.ceil(moduleNotes.length / this.itemsPerPage)
      const startIndex = (this.currentPage - 1) * this.itemsPerPage
      const endIndex = startIndex + this.itemsPerPage
      this.paginatedNotes = moduleNotes.slice(startIndex, endIndex)
    } else {
      // Si aucun module n'est sélectionné, on gère la pagination par module
      if (this.modulesWithNotes.length === 0) {
        this.paginatedNotes = []
        this.totalPages = 1
        return
      }

      // Récupérer les notes du module courant
      const currentModule = this.modulesWithNotes[this.currentModuleIndex]
      const moduleNotes = this.filteredNotes.filter(
        note => note.codeEM === currentModule.codeEM || note.elementModule?.codeEM === currentModule.codeEM
      )

      // Appliquer la pagination de 20 éléments par page pour chaque module
      this.totalPages = Math.ceil(moduleNotes.length / this.itemsPerPage)
      const startIndex = (this.currentPage - 1) * this.itemsPerPage
      const endIndex = startIndex + this.itemsPerPage
      this.paginatedNotes = moduleNotes.slice(startIndex, endIndex)
    }
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page
      this.updatePagination()
    }
  }

  changeModule(direction: 'next' | 'prev'): void {
    if (direction === 'next' && this.currentModuleIndex < this.modulesWithNotes.length - 1) {
      this.currentModuleIndex++
      this.currentPage = 1
      this.updatePagination()
    } else if (direction === 'prev' && this.currentModuleIndex > 0) {
      this.currentModuleIndex--
      this.currentPage = 1
      this.updatePagination()
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = []
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i)
    }
    return pages
  }

  getCurrentModuleInfo(): string {
    if (this.selectedModule) {
      const module = this.elementModules.find(m => m.codeEM === this.selectedModule)
      return module ? `${module.codeEM} - ${module.intitule}` : ''
    }
    if (this.currentModuleIndex < this.modulesWithNotes.length) {
      const module = this.modulesWithNotes[this.currentModuleIndex]
      return `${module.codeEM} - ${module.intitule}`
    }
    return ''
  }

  calculateTotal(note: NoteSemestrielle): number {
    if (note.noteRattrapage !== undefined && note.noteRattrapage > 0) return note.noteRattrapage
    const devoir = note.noteDevoir || 0
    const examen = note.noteExamen || 0
    return +(devoir * 0.4 + examen * 0.6).toFixed(2)
  }

  getStudentFullName(note: NoteSemestrielle): string {
    if (note.etudiant?.nom && note.etudiant?.prenom) {
      return `${note.etudiant.nom} ${note.etudiant.prenom}`
    }
    return ""
  }

  hasStudentInfo(note: NoteSemestrielle): boolean {
    return !!(note.etudiant?.nom && note.etudiant?.prenom)
  }
}
