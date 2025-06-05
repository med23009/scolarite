import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Departement } from '../../models/departement.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DepartementService {
  private API_URL = `${environment.apiUrl}/api/admin/departements`;
  private refreshSubject = new Subject<void>();

  getDepartementByUserEmail(email: string) {
    throw new Error('Method not implemented.');
  }

  constructor(private http: HttpClient) {}

  // Observable pour notifier les composants qu'un rafraîchissement est nécessaire
  get refresh$(): Observable<void> {
    return this.refreshSubject.asObservable();
  }
// Dans departement.service.ts, ajoutez cette méthode :
getAllDepartements(): Observable<Departement[]> {
  return this.http.get<Departement[]>(`${this.API_URL}/departements`);
}
  // Méthode pour forcer un rafraîchissement des départements
  refreshDepartements(): void {
    console.log('[DepartementService] Demande de rafraîchissement des départements');
    this.refreshSubject.next();
  }

  getAll(): Observable<Departement[]> {
    // Ajouter un timestamp pour éviter la mise en cache
    const timestamp = new Date().getTime();
    console.log(`[DepartementService] Récupération de tous les départements avec timestamp: ${timestamp}`);
    return this.http.get<Departement[]>(`${this.API_URL}?_=${timestamp}`);
  }

  getAllForDropdown(): Observable<Departement[]> {
    return this.http.get<Departement[]>(`${environment.apiUrl}/api/departements/all`);
  }

  getById(id: number): Observable<Departement> {
    return this.http.get<Departement>(`${this.API_URL}/${id}`);
  }

  create(departement: Departement): Observable<Departement> {
    return this.http.post<Departement>(this.API_URL, departement);
  }

  update(id: number, departement: Departement): Observable<Departement> {
    return this.http.put<Departement>(`${this.API_URL}/${id}`, departement);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  importCSV(file: File): Observable<Departement[]> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Departement[]>(`${this.API_URL}/import`, formData);
  }

  getByUserEmail(email: string): Observable<Departement> {
    return this.http.get<Departement>(`${this.API_URL}/user`, {
      params: { email }
    });
  }
}
