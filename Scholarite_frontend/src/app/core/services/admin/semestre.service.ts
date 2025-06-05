import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Semestre } from '../../models/semestre.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SemestreService {
  private readonly API_URL = `${environment.apiUrl}/api/semestres`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Semestre[]> {
    return this.http.get<Semestre[]>(`${this.API_URL}/all`);
  }

  getById(id: number): Observable<Semestre> {
    return this.http.get<Semestre>(`${this.API_URL}/${id}`);
  }

  create(semestre: Semestre): Observable<Semestre> {
    return this.http.post<Semestre>(this.API_URL, semestre);
  }

  update(id: number, semestre: Semestre): Observable<Semestre> {
    return this.http.put<Semestre>(`${this.API_URL}/${id}`, semestre);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  importCSV(file: File): Observable<Semestre[]> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Semestre[]>(`${this.API_URL}/import`, formData);
  }
}
