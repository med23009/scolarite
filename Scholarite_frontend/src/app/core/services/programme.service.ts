// src/app/core/services/programme.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UniteEnseignement } from '../models/unite-enseignement.model';
import { ElementDeModule } from '../models/element-module.model';
import { Semestre } from '../models/semestre.model';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth/auth.service'; // Import AuthService
import { ChefDeptProgrammeService } from './chef_dept/programme.service';
import { ChefPoleProgrammeService } from './chef_pole/programme.service';

@Injectable({
  providedIn: 'root'
})
export class ProgrammeService {
  private readonly API_URL = `${environment.apiUrl}/api/programmes`;

  constructor(
    private http: HttpClient,
    private authService: AuthService, // Inject AuthService
    private chefDeptService: ChefDeptProgrammeService,
    private chefPoleService: ChefPoleProgrammeService
  ) {}

  // Unités d'Enseignement
  getAllUniteEnseignements(email?: string): Observable<UniteEnseignement[]> {
    // Get the current user's email from the auth service
    const currentUser = this.authService.currentUserSubject.value;
    const userEmail = email || (currentUser ? currentUser.email : '') || '';
    
    console.log('Making request with email:', userEmail); // Debug log
    
    // Route to the appropriate service based on user role
    if (this.authService.isChefDept()) {
      console.log('Routing to ChefDept service');
      return this.chefDeptService.getAllUniteEnseignements(userEmail);
    } else if (this.authService.isChefPole()) {
      console.log('Routing to ChefPole service');
      return this.chefPoleService.getAllUniteEnseignements(userEmail);
    } else {
      // Default to the original endpoint for other roles
      return this.http.get<UniteEnseignement[]>(`${this.API_URL}/ue`, {
        params: { email: userEmail }
      });
    }
  }

  getUniteEnseignementById(id: number): Observable<UniteEnseignement> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.getUniteEnseignementById(id);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.getUniteEnseignementById(id);
    } else {
      return this.http.get<UniteEnseignement>(`${this.API_URL}/ue/${id}`);
    }
  }

  createUniteEnseignement(ue: UniteEnseignement): Observable<UniteEnseignement> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.createUniteEnseignement(ue);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.createUniteEnseignement(ue);
    } else {
      return this.http.post<UniteEnseignement>(`${this.API_URL}/ue`, ue);
    }
  }

  updateUniteEnseignement(id: number, ue: UniteEnseignement): Observable<UniteEnseignement> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.updateUniteEnseignement(id, ue);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.updateUniteEnseignement(id, ue);
    } else {
      return this.http.put<UniteEnseignement>(`${this.API_URL}/ue/${id}`, ue);
    }
  }

  deleteUniteEnseignement(id: number): Observable<void> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.deleteUniteEnseignement(id);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.deleteUniteEnseignement(id);
    } else {
      return this.http.delete<void>(`${this.API_URL}/ue/${id}`);
    }
  }

  // Éléments de Module
  getAllElementsDeModule(): Observable<ElementDeModule[]> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.getAllElementsDeModule();
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.getAllElementsDeModule();
    } else {
      return this.http.get<ElementDeModule[]>(`${this.API_URL}/em`);
    }
  }

  getElementDeModuleById(id: number): Observable<ElementDeModule> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.getElementDeModuleById(id);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.getElementDeModuleById(id);
    } else {
      return this.http.get<ElementDeModule>(`${this.API_URL}/em/${id}`);
    }
  }

  createElementDeModule(em: ElementDeModule): Observable<ElementDeModule> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.createElementDeModule(em);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.createElementDeModule(em);
    } else {
      return this.http.post<ElementDeModule>(`${this.API_URL}/em`, em);
    }
  }

  updateElementDeModule(id: number, em: ElementDeModule): Observable<ElementDeModule> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.updateElementDeModule(id, em);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.updateElementDeModule(id, em);
    } else {
      return this.http.put<ElementDeModule>(`${this.API_URL}/em/${id}`, em);
    }
  }

  deleteElementDeModule(id: number): Observable<void> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.deleteElementDeModule(id);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.deleteElementDeModule(id);
    } else {
      return this.http.delete<void>(`${this.API_URL}/em/${id}`);
    }
  }

  // Import Excel
  importFromExcel(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    
    if (this.authService.isChefDept()) {
      return this.chefDeptService.importFromExcel(file);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.importFromExcel(file);
    } else {
      return this.http.post<any>(`${this.API_URL}/import`, formData);
    }
  }

  // Semestres
  getAllSemestres(): Observable<Semestre[]> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.getAllSemestres();
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.getAllSemestres();
    } else {
      return this.http.get<Semestre[]>(`${this.API_URL}/semestres`);
    }
  }

  getSemestreById(id: number): Observable<Semestre> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.getSemestreById(id);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.getSemestreById(id);
    } else {
      return this.http.get<Semestre>(`${this.API_URL}/semestres/${id}`);
    }
  }

  getSemestresByAnnee(annee: number): Observable<Semestre[]> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.getSemestresByAnnee(annee);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.getSemestresByAnnee(annee);
    } else {
      return this.http.get<Semestre[]>(`${this.API_URL}/semestres/annee/${annee}`);
    }
  }

  // Association des semestres
  associateSemestreToUE(ueId: number, semestreId: number): Observable<UniteEnseignement> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.associateSemestreToUE(ueId, semestreId);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.associateSemestreToUE(ueId, semestreId);
    } else {
      return this.http.post<UniteEnseignement>(`${this.API_URL}/ue/${ueId}/semestre/${semestreId}`, {});
    }
  }

  associateSemestreToEM(emId: number, semestreId: number): Observable<ElementDeModule> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.associateSemestreToEM(emId, semestreId);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.associateSemestreToEM(emId, semestreId);
    } else {
      return this.http.post<ElementDeModule>(`${this.API_URL}/em/${emId}/semestre/${semestreId}`, {});
    }
  }

  // Filtrage par semestre
  getUEBySemestre(semestreId: number): Observable<UniteEnseignement[]> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.getUEBySemestre(semestreId);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.getUEBySemestre(semestreId);
    } else {
      return this.http.get<UniteEnseignement[]>(`${this.API_URL}/ue/semestre/${semestreId}`);
    }
  }

  getEMBySemestre(semestreId: number): Observable<ElementDeModule[]> {
    if (this.authService.isChefDept()) {
      return this.chefDeptService.getEMBySemestre(semestreId);
    } else if (this.authService.isChefPole()) {
      return this.chefPoleService.getEMBySemestre(semestreId);
    } else {
      return this.http.get<ElementDeModule[]>(`${this.API_URL}/em/semestre/${semestreId}`);
    }
  }
}