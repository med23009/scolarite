// src/app/core/services/pole.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pole } from '../models/pole.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PoleService {
  private readonly API_URL = `${environment.apiUrl}/api/poles`;

  constructor(private http: HttpClient) {}

  getAllPoles(): Observable<Pole[]> {
    return this.http.get<Pole[]>(this.API_URL);
  }

  getPoleById(id: number): Observable<Pole> {
    return this.http.get<Pole>(`${this.API_URL}/${id}`);
  }

  createPole(pole: Pole): Observable<Pole> {
    return this.http.post<Pole>(this.API_URL, pole);
  }

  updatePole(id: number, pole: Pole): Observable<Pole> {
    return this.http.put<Pole>(`${this.API_URL}/${id}`, pole);
  }

  deletePole(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  importPolesFromCSV(file: File): Observable<Pole[]> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<Pole[]>(`${this.API_URL}/import`, formData);
  }
}