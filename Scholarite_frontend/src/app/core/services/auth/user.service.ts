// src/app/core/services/user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../../models/user.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = environment.apiUrl;
  
  constructor(private http: HttpClient) {}
  
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.API_URL}/api/users`);
  }
  
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/api/users/${id}`);
  }
  
  createUser(user: User): Observable<User> {
    console.log('[UserService] Création d\'utilisateur:', user);
    return this.http.post<User>(`${this.API_URL}/api/users`, user);
  }
  
  updateUser(id: number, user: User): Observable<User> {
    console.log('[UserService] Mise à jour de l\'utilisateur ID=' + id, user);
    return this.http.put<User>(`${this.API_URL}/api/users/${id}`, user);
  }
  
  deleteUser(id: number): Observable<{message: string}> {
    return this.http.delete<{message: string}>(`${this.API_URL}/api/users/${id}`, {
      responseType: 'json'
    });
  }
}