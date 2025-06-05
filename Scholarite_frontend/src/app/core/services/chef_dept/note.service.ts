// src/app/core/services/note.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { NoteSemestrielle } from '../../models/note-semestrielle.model';
import { ElementDeModule } from '../../models/element-module.model';
import { Semestre } from '../../models/semestre.model';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class NoteService {
  private readonly API_URL = `${environment.apiUrl}/api/notes`;

  constructor(private http: HttpClient,
    private authService: AuthService
  ) {}

  getAllNotes(): Observable<NoteSemestrielle[]> {
    const currentUser = this.authService.currentUserSubject.value;
    const userEmail = currentUser ? currentUser.email : '';
    
    return this.http.get<NoteSemestrielle[]>(this.API_URL, {
      params: { email: userEmail }
    });
  }

  getNoteById(id: number): Observable<NoteSemestrielle> {
    return this.http.get<NoteSemestrielle>(`${this.API_URL}/${id}`);
  }
  
  getNotesByMatricule(matricule: string): Observable<NoteSemestrielle[]> {
    return this.http.get<NoteSemestrielle[]>(`${this.API_URL}/etudiant/${matricule}`);
  }
  
  getNotesByModule(codeEM: string): Observable<NoteSemestrielle[]> {
    return this.http.get<NoteSemestrielle[]>(`${this.API_URL}/module/${codeEM}`);
  }
  
  getAllModules(): Observable<ElementDeModule[]> {
    const currentUser = this.authService.currentUserSubject.value;
    const userEmail = currentUser ? currentUser.email : '';
    const userRole = currentUser ? currentUser.role : '';
    
    return this.http.get<ElementDeModule[]>(`${this.API_URL}/modules`, {
      params: { 
        email: userEmail || '',
        role: userRole || ''
      }
    });
  }
  
  getAllSemestres(): Observable<Semestre[]> {
    return this.http.get<Semestre[]>(`${this.API_URL}/semestres`);
  }

  createNote(note: NoteSemestrielle): Observable<NoteSemestrielle> {
    return this.http.post<NoteSemestrielle>(this.API_URL, note);
  }

  updateNote(id: number, note: NoteSemestrielle): Observable<NoteSemestrielle> {
    // Create a new object with only the fields that should be updated
    const updatedNote = {
      noteDevoir: note.noteDevoir,
      noteExamen: note.noteExamen,
      noteRattrapage: note.noteRattrapage
    };
    
    return this.http.put<NoteSemestrielle>(`${this.API_URL}/${id}`, updatedNote);
  }

  deleteNote(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  importNotesFromExcel(file: File, idElementModule: number, annee: number, semestre: number = 1, idSemestre?: number): Observable<any> {
    const currentUser = this.authService.currentUserSubject.value;
    const userEmail = currentUser ? currentUser.email : '';
    const userRole = currentUser ? currentUser.role : '';
    
    // Ensure all parameters are valid numbers
    const validElementModuleId = isNaN(idElementModule) ? 0 : idElementModule;
    const validAnnee = isNaN(annee) ? new Date().getFullYear() : annee;
    const validSemestre = isNaN(semestre) ? 1 : semestre;
    const validSemestreId = idSemestre && !isNaN(idSemestre) ? idSemestre : 0;
    
    if (validElementModuleId === 0 || validSemestreId === 0) {
      return throwError(() => new Error('Module ou semestre invalide'));
    }
    
    console.log('NoteService - Sending import with params:', {
      idElementModule: validElementModuleId,
      annee: validAnnee,
      semestre: validSemestre,
      idSemestre: validSemestreId
    });
    
    const formData = new FormData();
    formData.append('file', file);
    formData.append('idElementModule', validElementModuleId.toString());
    formData.append('annee', validAnnee.toString());
    formData.append('semestre', validSemestre.toString());
    formData.append('idSemestre', validSemestreId.toString());
    formData.append('userEmail', userEmail || '');
    formData.append('userRole', userRole || '');
    
    return this.http.post<any>(`${this.API_URL}/import`, formData);
  }

  
}