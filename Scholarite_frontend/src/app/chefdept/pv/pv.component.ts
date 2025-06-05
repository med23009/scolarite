import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PvService } from '../../core/services/chef_dept/pv.service';
import { Semestre } from '../../core/models/semestre.model';
import { Departement } from '../../core/models/departement.model';
import * as XLSX from 'xlsx';

// Interface for PV history
interface PvHistoryItem {
  id: string;
  date: Date;
  semestre: string;
  type: string;
  fileData: string; // base64 encoded file
}

@Component({
  selector: 'app-pv',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pv.component.html',
  styleUrls: ['./pv.component.scss']
})
export class PvComponent implements OnInit {
  // Data
  semestres: Semestre[] = [];
  departement: Departement | null = null;
  pvHistory: PvHistoryItem[] = [];
  
  // Form state
  selectedSemestreId: number | null = null;
  selectedPvType: string = 'complet';
  formSubmitted: boolean = false;
  
  // UI state
  loading: boolean = false;
  error: string = '';
  success: string = '';
  generating: boolean = false;

  constructor(private pvService: PvService) {}

  ngOnInit(): void {
    this.loadSemestres();
    this.loadDepartement();
    this.loadPvHistory();
  }

  /**
   * Load all available semesters
   */
  loadSemestres(): void {
    this.loading = true;
    this.pvService.getAllSemestres().subscribe({
      next: (data) => {
        this.semestres = data;
        // Sort semesters by year and semester number
        this.semestres.sort((a, b) => {
          if (a.annee !== b.annee) {
            return b.annee - a.annee; // Most recent year first
          }
          // Extract semester number for comparison
          const aNum = this.extractSemesterNumber(a.semestre);
          const bNum = this.extractSemesterNumber(b.semestre);
          return bNum - aNum; // Higher semester first
        });
        this.loading = false;
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors du chargement des semestres';
        this.loading = false;
      }
    });
  }

  /**
   * Extract semester number from semester string
   */
  private extractSemesterNumber(semesterStr: string): number {
    const match = semesterStr.match(/\d+/);
    return match ? parseInt(match[0], 10) : 0;
  }

  /**
   * Load department information
   */
  loadDepartement(): void {
    this.pvService.getDepartement().subscribe({
      next: (data) => {
        this.departement = data;
      },
      error: (error) => {
        this.error = error.error?.error || 'Erreur lors du chargement du département';
      }
    });
  }

  /**
   * Load PV generation history from localStorage
   */
  loadPvHistory(): void {
    const historyJson = localStorage.getItem('pvHistory');
    if (historyJson) {
      try {
        this.pvHistory = JSON.parse(historyJson);
        // Sort by date (most recent first)
        this.pvHistory.sort((a, b) => 
          new Date(b.date).getTime() - new Date(a.date).getTime()
        );
      } catch (e) {
        console.error('Error parsing PV history:', e);
        this.pvHistory = [];
      }
    }
  }

  /**
   * Generate PV based on selected semester and type
   */
  generatePv(): void {
    this.formSubmitted = true;
    
    if (!this.selectedSemestreId || !this.departement) {
      this.error = 'Veuillez sélectionner un semestre';
      return;
    }
  
    this.error = '';
    this.success = '';
    this.generating = true;
  
    this.pvService.exportPv(this.selectedSemestreId, this.departement.codeDep).subscribe({
      next: (blob) => {
        this.generating = false;
        this.success = 'PV généré avec succès';
  
        const reader = new FileReader();
        reader.onload = (e: any) => {
          const data = new Uint8Array(e.target.result);
          const workbook = XLSX.read(data, { type: 'array' });
          const sheetName = workbook.SheetNames[0];
          const worksheet = workbook.Sheets[sheetName];
          const excelData = XLSX.utils.sheet_to_json(worksheet, { header: 1 }); // header:1 => tableau simple
  
          // Store Excel data in localStorage for preview
          localStorage.setItem('previewPvData', JSON.stringify(excelData));
  
          // Store Excel file as base64 for download
          const fileReader = new FileReader();
          fileReader.onload = (event: any) => {
            const fileData = event.target.result; // base64
            localStorage.setItem('previewPvFile', fileData);
            localStorage.setItem('previewPvSemestre', this.getSelectedSemestreName());
            localStorage.setItem('previewPvDepartement', this.departement?.codeDep || 'Departement');

            // Add to history
            this.addToHistory(fileData);
            
            // Open preview in new tab
            window.open('/chefdept/preview-pv', '_blank');
          };
          fileReader.readAsDataURL(blob);
        };
        reader.readAsArrayBuffer(blob);
      },
      error: (error) => {
        this.generating = false;
        this.error = error.error?.message || 'Erreur lors de la génération du PV';
        console.error(error);
      }
    });
  }

  /**
   * Preview PV without generating a new one
   */
  previewPv(): void {
    this.formSubmitted = true;
    
    if (!this.selectedSemestreId || !this.departement) {
      this.error = 'Veuillez sélectionner un semestre';
      return;
    }
    
    // Set preview data
    localStorage.setItem('previewPvSemestre', this.getSelectedSemestreName());
    localStorage.setItem('previewPvDepartement', this.departement?.codeDep || 'Departement');
    
    // Open preview page
    window.open('/chefdept/preview-pv', '_blank');
  }

  /**
   * Add generated PV to history
   */
  private addToHistory(fileData: string): void {
    const selectedSemestre = this.semestres.find(s => s.idSemestre === this.selectedSemestreId);
    if (!selectedSemestre) return;
    
    const historyItem: PvHistoryItem = {
      id: this.generateId(),
      date: new Date(),
      semestre: this.getSemestreLabel(selectedSemestre),
      type: this.getPvTypeLabel(),
      fileData: fileData
    };
    
    // Add to history array
    this.pvHistory.unshift(historyItem);
    
    // Limit history to 10 items
    if (this.pvHistory.length > 10) {
      this.pvHistory = this.pvHistory.slice(0, 10);
    }
    
    // Save to localStorage
    localStorage.setItem('pvHistory', JSON.stringify(this.pvHistory));
  }

  /**
   * Generate unique ID for history items
   */
  private generateId(): string {
    return 'pv_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  /**
   * Download a PV from history
   */
  downloadPv(pv: PvHistoryItem): void {
    const link = document.createElement('a');
    link.href = pv.fileData;
    link.download = `PV_${pv.semestre.replace(/\s+/g, '_')}_${this.formatDate(new Date(pv.date))}.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /**
   * Open preview for a PV from history
   */
  openPreview(pv: PvHistoryItem): void {
    localStorage.setItem('previewPvFile', pv.fileData);
    localStorage.setItem('previewPvSemestre', pv.semestre.replace(/\s+/g, ''));
    localStorage.setItem('previewPvDepartement', this.departement?.codeDep || 'Departement');
    
    window.open('/chefdept/preview-pv', '_blank');
  }

  /**
   * Format date for filename
   */
  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  /**
   * Get selected semester name
   */
  getSelectedSemestreName(): string {
    const selected = this.semestres.find(s => s.idSemestre === this.selectedSemestreId);
    return selected ? selected.semestre.replace(/\s+/g, '') : 'Semestre';
  }

  /**
   * Get semester label for display
   */
  getSemestreLabel(semestre: Semestre): string {
    return `${semestre.semestre} (Année: ${semestre.annee})`;
  }

  /**
   * Get PV type label
   */
  getPvTypeLabel(): string {
    switch (this.selectedPvType) {
      case 'anonyme': return 'PV Anonyme';
      case 'deliberation': return 'PV de Délibération';
      default: return 'PV Complet';
    }
  }
}