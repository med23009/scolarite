import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Semestre } from '../../models/semestre.model';
import { Departement } from '../../models/departement.model';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class PvService {
  private readonly API_URL = `${environment.apiUrl}/api/chefdept/pv`;

  constructor(
    private readonly http: HttpClient,
    private authService: AuthService
  ) {}

  /**
   * Get all available semesters for PV generation
   */
  getAllSemestres(): Observable<Semestre[]> {
    return this.http.get<Semestre[]>(`${this.API_URL}/semestres`);
  }

  /**
   * Get department information for the current chef de département
   */
  getDepartement(): Observable<Departement> {
    const currentUser = this.authService.currentUserSubject.value;
    const userEmail = currentUser ? currentUser.email : '';
    
    return this.http.get<Departement>(`${this.API_URL}/departement`, {
      params: { email: userEmail }
    });
  }

  /**
   * Generate PV Excel file for a specific semester and department
   * Returns a blob that can be used to create a download link
   */
  exportPv(semestreId: number, codeDep: string): Observable<Blob> {
    return this.http.get(`${this.API_URL}/export`, {
      params: {
        semestreId: semestreId.toString(),
        codeDep: codeDep
      },
      responseType: 'blob'
    });
  }
}