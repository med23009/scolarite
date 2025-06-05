import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { Historique } from '../../core/models/historique.model';
import { HistoriqueService } from '../../core/services/admin/historique.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-historique',
  standalone: true, // si tu utilises des composants autonomes
  imports: [CommonModule ,FormsModule ],
  
  templateUrl: './historique.component.html',
  styleUrls: ['./historique.component.scss']
})
export class HistoriqueComponent implements OnInit {
  historiques: any[] = [];  // on utilise `any` pour ajouter le champ dateFormatee
  filtered: any[] = [];

  filtreAction: string = '';
  filtreDate: string = '';

  constructor(private historiqueService: HistoriqueService) {}

  ngOnInit(): void {
    this.historiqueService.getAllHistorique().subscribe(data => {
      this.historiques = data.map(h => ({
        ...h,
        dateFormatee: this.formaterDate(h.dateAction)
      }));
      this.filtered = this.historiques;
    });
  }

  formaterDate(dateStr: string): string {
    const date = new Date(dateStr);
  
    if (isNaN(date.getTime())) {
      // Tentative de parser manuellement si la date est invalide
      try {
        // Si le format est du type "yyyy-MM-dd HH:mm:ss", on le convertit
        const isoString = dateStr.replace(" ", "T");
        const newDate = new Date(isoString);
        return newDate.toLocaleString('fr-FR', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (e) {
        return "Date invalide";
      }
    }
  
    return date.toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  

  filtrer() {
    this.filtered = this.historiques.filter(h =>
      (this.filtreAction === '' || h.action.toLowerCase().includes(this.filtreAction.toLowerCase())) &&
      (this.filtreDate === '' || h.dateAction.startsWith(this.filtreDate))
    );
  }

  reset() {
    this.filtreAction = '';
    this.filtreDate = '';
    this.filtered = this.historiques;
  }
}
