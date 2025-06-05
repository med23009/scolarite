// src/app/core/services/chef_pole/programme.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UniteEnseignement } from '../../models/unite-enseignement.model';
import { ElementDeModule } from '../../models/element-module.model';
import { Semestre } from '../../models/semestre.model';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ChefPoleProgrammeService {
  private readonly API_URL = `${environment.apiUrl}/api/chef-pole/programmes`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // Unités d'Enseignement de la période courante
  getAllUniteEnseignements(email?: string): Observable<UniteEnseignement[]> {
    const currentUser = this.authService.currentUserSubject.value;
    const userEmail = email || (currentUser ? currentUser.email : '') || '';
    return this.http.get<UniteEnseignement[]>(`${this.API_URL}/ue/current-period`, {
      params: { email: userEmail }
    });
  }

  getUniteEnseignementById(id: number): Observable<UniteEnseignement> {
    return this.http.get<UniteEnseignement>(`${this.API_URL}/ue/${id}`);
  }

  createUniteEnseignement(ue: UniteEnseignement): Observable<UniteEnseignement> {
    return this.http.post<UniteEnseignement>(`${this.API_URL}/ue`, ue);
  }

  updateUniteEnseignement(id: number, ue: UniteEnseignement): Observable<UniteEnseignement> {
    return this.http.put<UniteEnseignement>(`${this.API_URL}/ue/${id}`, ue);
  }

  deleteUniteEnseignement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/ue/${id}`);
  }

  // Éléments de Module de la période courante
  getAllElementsDeModule(): Observable<ElementDeModule[]> {
    return this.http.get<ElementDeModule[]>(`${this.API_URL}/em/current-period`);
  }

  getElementDeModuleById(id: number): Observable<ElementDeModule> {
    return this.http.get<ElementDeModule>(`${this.API_URL}/em/${id}`);
  }

  createElementDeModule(em: ElementDeModule): Observable<ElementDeModule> {
    return this.http.post<ElementDeModule>(`${this.API_URL}/em`, em);
  }

  updateElementDeModule(id: number, em: ElementDeModule): Observable<ElementDeModule> {
    return this.http.put<ElementDeModule>(`${this.API_URL}/em/${id}`, em);
  }

  deleteElementDeModule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/em/${id}`);
  }

  // Import Excel
  importFromExcel(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<any>(`${this.API_URL}/import`, formData);
  }

  // Semestres
  getAllSemestres(): Observable<Semestre[]> {
    return this.http.get<Semestre[]>(`${this.API_URL}/semestres`);
  }

  getSemestreById(id: number): Observable<Semestre> {
    return this.http.get<Semestre>(`${this.API_URL}/semestres/${id}`);
  }

  getSemestresByAnnee(annee: number): Observable<Semestre[]> {
    return this.http.get<Semestre[]>(`${this.API_URL}/semestres/annee/${annee}`);
  }

  // Association des semestres
  associateSemestreToUE(ueId: number, semestreId: number): Observable<UniteEnseignement> {
    return this.http.post<UniteEnseignement>(`${this.API_URL}/ue/${ueId}/semestre/${semestreId}`, {});
  }

  associateSemestreToEM(emId: number, semestreId: number): Observable<ElementDeModule> {
    return this.http.post<ElementDeModule>(`${this.API_URL}/em/${emId}/semestre/${semestreId}`, {});
  }

  // Filtrage par semestre
  getUEBySemestre(semestreId: number): Observable<UniteEnseignement[]> {
    return this.http.get<UniteEnseignement[]>(`${this.API_URL}/ue/semestre/${semestreId}`);
  }

  getEMBySemestre(semestreId: number): Observable<ElementDeModule[]> {
    return this.http.get<ElementDeModule[]>(`${this.API_URL}/em/semestre/${semestreId}`);
  }
}
