// src/app/core/services/directeur/de-programme.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { UniteEnseignement } from '../../models/unite-enseignement.model';
import { ElementDeModuleDTO } from '../../models/element-module-dto.model';

@Injectable({
  providedIn: 'root'
})
export class DEProgrammeService {
  private readonly API_URL = `${environment.apiUrl}/api/de/programmes`;

  constructor(private http: HttpClient) {}

  getUEByDepartement(id: string): Observable<UniteEnseignement[]> {
    return this.http.get<UniteEnseignement[]>(`${this.API_URL}/ue/department/${id}`);
  }

  getUEByPole(id: string): Observable<UniteEnseignement[]> {
    return this.http.get<UniteEnseignement[]>(`${this.API_URL}/ue/pole/${id}`);
  }

  getEMByUE(idUE: number): Observable<ElementDeModuleDTO[]> {
    return this.http.get<ElementDeModuleDTO[]>(`${this.API_URL}/em/ue/${idUE}`);
  }
}
