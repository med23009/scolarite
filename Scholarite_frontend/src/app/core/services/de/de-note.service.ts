import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { NoteSemestrielle } from '../../models/note-semestrielle.model';
import { ElementDeModule } from '../../models/element-module.model';
import { Semestre } from '../../models/semestre.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DENoteService {
  private apiUrl = `${environment.apiUrl}/api/de`;

  constructor(private http: HttpClient) { }

  getNotesByDepartment(departmentId: string): Observable<NoteSemestrielle[]> {
    return this.http.get<NoteSemestrielle[]>(`${this.apiUrl}/notes/department/${departmentId}`).pipe(
      tap(data => console.log(`fetched notes for department id=${departmentId}`, data)),
      catchError(this.handleError<NoteSemestrielle[]>(`getNotesByDepartment id=${departmentId}`, []))
    );
  }

  getNotesByPole(poleId: string): Observable<NoteSemestrielle[]> {
    return this.http.get<NoteSemestrielle[]>(`${this.apiUrl}/notes/pole/${poleId}`).pipe(
      tap(data => console.log(`fetched notes for pole id=${poleId}`, data)),
      catchError(this.handleError<NoteSemestrielle[]>(`getNotesByPole id=${poleId}`, []))
    );
  }

  updateNote(idNote: number, noteData: Partial<NoteSemestrielle>): Observable<NoteSemestrielle> {
    const httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    return this.http.put<NoteSemestrielle>(`${this.apiUrl}/notes/${idNote}`, noteData, httpOptions).pipe(
      tap((updatedNote) => console.log(`updated note id=${idNote} with data:`, updatedNote)),
      catchError(this.handleError<NoteSemestrielle>('updateNote'))
    );
  }

  importNotes(file: File, type: 'department' | 'pole', id: string): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);
    let params = new HttpParams().set('type', type).set('id', id);
    return this.http.post<any>(`${this.apiUrl}/notes/import`, formData, { params }).pipe(
      tap(response => console.log('notes imported successfully', response)),
      catchError(this.handleError<any>('importNotes'))
    );
  }

  getElementModules(): Observable<ElementDeModule[]> {
    return this.http.get<ElementDeModule[]>(`${environment.apiUrl}/api/elementmodules`).pipe(
      tap(data => console.log('fetched all element modules', data)),
      catchError(this.handleError<ElementDeModule[]>('getElementModules', []))
    );
  }

  getSemestres(): Observable<Semestre[]> {
    return this.http.get<Semestre[]>(`${environment.apiUrl}/api/semestres`).pipe(
      tap(data => console.log('fetched all semestres', data)),
      catchError(this.handleError<Semestre[]>('getSemestres', []))
    );
  }

  getElementModulesByDepartmentId(departmentId: number): Observable<ElementDeModule[]> {
    // Use the programmes endpoint which is authorized for DE role
    return this.http.get<ElementDeModule[]>(`${environment.apiUrl}/api/de/programmes/em/department/${departmentId}`).pipe(
      tap(data => console.log(`fetched element modules for department id=${departmentId}`, data)),
      catchError(this.handleError<ElementDeModule[]>(`getElementModulesByDepartmentId id=${departmentId}`, []))
    );
  }

  getElementModulesByPoleId(poleId: number): Observable<ElementDeModule[]> {
    // Use the programmes endpoint which is authorized for DE role
    return this.http.get<ElementDeModule[]>(`${environment.apiUrl}/api/de/programmes/em/pole/${poleId}`).pipe(
      tap(data => console.log(`fetched element modules for pole id=${poleId}`, data)),
      catchError(this.handleError<ElementDeModule[]>(`getElementModulesByPoleId id=${poleId}`, []))
    );
  }

  getNotesByElementModuleCode(codeEM: string): Observable<NoteSemestrielle[]> {
    // Use the programmes endpoint which is authorized for DE role
    return this.http.get<NoteSemestrielle[]>(`${environment.apiUrl}/api/de/programmes/notes/elementmodule/${codeEM}`).pipe(
      tap(data => console.log(`fetched notes for element module code=${codeEM}`, data)),
      catchError(this.handleError<NoteSemestrielle[]>(`getNotesByElementModuleCode code=${codeEM}`, []))
    );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`, error); 
      return of(result as T);
    };
  }
}
