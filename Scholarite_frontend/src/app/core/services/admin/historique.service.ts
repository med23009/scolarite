import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Historique } from '../../models/historique.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HistoriqueService {
  private apiUrl = `${environment.apiUrl}/api/historique`;

  constructor(private http: HttpClient) {}

  getAllHistorique(): Observable<Historique[]> {
    return this.http.get<Historique[]>(this.apiUrl);
  }
}
