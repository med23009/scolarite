import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Departement } from '../../models/departement.model';

@Injectable({
  providedIn: 'root',
})
export class ReleveNoteService {
  private apiUrl = `${environment.apiUrl}/api/bulletins`;

  constructor(private http: HttpClient) {}
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }
  
  getBulletinData(matricule: string, semestreId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/data/${matricule}/${semestreId}`, {
      headers: this.getHeaders() 
    });
  }
  getSemestres(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/api/bulletins/semestres`);
  }
getEtudiantsByDeptAndPromo(idDepartement: number, promotion: string, semestreId: number): Observable<any[]> {
  return this.http.get<any[]>(`${environment.apiUrl}/api/bulletins/departement/${idDepartement}/${promotion}?semestreId=${semestreId}`);
}


  getDepartements(): Observable<Departement[]> {
  return this.http.get<Departement[]>(`${environment.apiUrl}/api/bulletins/forselction/departements`);
}
sendRelevesParEmail(payload: { email: string, pdfBase64: string }[]): Observable<any> {
  return this.http.post(`${environment.apiUrl}/api/bulletins/send-emails`, payload);
}

  
}