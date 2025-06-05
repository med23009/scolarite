import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PlanEtude } from '../../models/plan-etude.model';

@Injectable({
  providedIn: 'root'
})
export class PlanEtudeService {
  private apiUrl = `${environment.apiUrl}/api/plan-etude`;

  constructor(private http: HttpClient) { }

  /**
   * Récupère le plan d'étude pour un étudiant et un semestre donnés
   * @param matricule Matricule de l'étudiant
   * @param semestreId ID du semestre
   * @returns Observable contenant les données du plan d'étude
   */
  getPlanEtude(matricule: string, semestreId: number): Observable<PlanEtude> {
    return this.http.get<PlanEtude>(`${this.apiUrl}?matricule=${matricule}&semestreId=${semestreId}`);
  }

  /**
   * Récupère la liste des semestres disponibles
   * @returns Observable contenant la liste des semestres
   */
  getAllSemestres(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/semestres`);
  }
} 