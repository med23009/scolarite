import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Pole } from '../../models/pole.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PoleService {
  private readonly API_URL = `${environment.apiUrl}/api/poles`;
  private refreshSubject = new Subject<void>();

  constructor(private http: HttpClient) {}

  getAll(): Observable<Pole[]> {
    console.log('[PoleService] Fetching all poles from:', this.API_URL);
    return this.http.get<Pole[]>(this.API_URL)
      .pipe(
        tap(poles => console.log('[PoleService] Poles fetched successfully:', poles)),
        catchError(error => {
          console.error('[PoleService] Error fetching poles:', error);
          return throwError(() => error);
        })
      );
  }

  getById(id: number): Observable<Pole> {
    return this.http.get<Pole>(`${this.API_URL}/${id}`);
  }

  create(pole: Pole): Observable<Pole> {
    return this.http.post<Pole>(this.API_URL, pole);
  }

  update(id: number, pole: Pole): Observable<Pole> {
    return this.http.put<Pole>(`${this.API_URL}/${id}`, pole);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  importCSV(file: File): Observable<Pole[]> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Pole[]>(`${this.API_URL}/import`, formData);
  }
  
  // Méthode pour notifier les composants qu'ils doivent rafraîchir les données des pôles
  refreshPoles(): void {
    console.log('[PoleService] Notification de rafraîchissement des pôles');
    this.refreshSubject.next();
  }
  
  // Méthode pour obtenir l'Observable pour s'abonner aux notifications de rafraîchissement
  getRefreshObservable(): Observable<void> {
    return this.refreshSubject.asObservable();
  }
}
