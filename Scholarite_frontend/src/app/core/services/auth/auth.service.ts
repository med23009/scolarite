// src/app/core/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthRequest } from  '../../models/auth-request.model';
import { AuthResponse } from '../../models/auth-response.model';
import { User } from '../../models/user.model';
import { environment } from '../../../../environments/environment';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = environment.apiUrl;
  public currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private _passwordChanged?: boolean;

  constructor(private http: HttpClient, private router: Router) {
    this.loadCurrentUser();
  }

  login(authRequest: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/api/auth/login`, authRequest)
      .pipe(
        tap(response => {
          this.setToken(response.token);
          // Store passwordChanged status in the service
          this.storePasswordChangedStatus(response.passwordChanged);
          // After successful login, we would typically decode the JWT and set user info
          this.loadCurrentUser();
          // Navigation is now handled by the login component based on passwordChanged status
        })
      );
  }

  register(user: User): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/api/admin/register`, user)
      .pipe(
        tap(response => {
          this.setToken(response.token);
          this.loadCurrentUser();
          this.router.navigate(['/admin/users']);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    const token = localStorage.getItem('token');

    // Basic validation of token format
    if (token && token.split('.').length === 3) {
      return token;
    }

    // If token is invalid, clear it and return null
    if (token) {
      console.warn('Invalid token found in storage, clearing it');
      localStorage.removeItem('token');
    }

    return null;
  }

  setToken(token: string): void {
    localStorage.setItem('token', token);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // Store password changed status
  storePasswordChangedStatus(changed: boolean): void {
    // Store in memory for the current session
    this._passwordChanged = changed;
    
    // Also decode from token and update if needed
    const token = this.getToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.password_changed !== undefined) {
          this._passwordChanged = payload.password_changed;
        }
      } catch (e) {
        console.error('Error decoding token for password status:', e);
      }
    }
  }

  // Check if password has been changed
  isPasswordChanged(): boolean {
    // First check the in-memory flag
    if (this._passwordChanged !== undefined) {
      return this._passwordChanged;
    }
    
    // If not set, try to get from token
    const token = this.getToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.password_changed !== undefined) {
          this._passwordChanged = payload.password_changed;
          return payload.password_changed;
        }
      } catch (e) {
        console.error('Error decoding token for password status:', e);
      }
    }
    
    // Default to false if we can't determine
    return false;
  }

  loadCurrentUser(): void {
    const token = this.getToken();
    if (token) {
      try {
        // Decode the JWT token
        const tokenParts = token.split('.');
        if (tokenParts.length === 3) {
          const payload = JSON.parse(atob(tokenParts[1]));

          // Debug - log the complete payload
          console.log('COMPLETE TOKEN PAYLOAD:', JSON.stringify(payload, null, 2));

          // Try to find role in different possible locations
          let role = null;
          if (payload.role) role = payload.role;
          else if (payload.role_membre) role = payload.role_membre; // This is the key line - your token has role_membre
          else if (payload.roles) role = Array.isArray(payload.roles) ? payload.roles[0] : payload.roles;
          else if (payload.authorities && Array.isArray(payload.authorities) && payload.authorities.length > 0) {
            role = typeof payload.authorities[0] === 'object' ?
                  payload.authorities[0].authority : payload.authorities[0];
          }

          console.log('Found role in token:', role);
          console.log('Found nom in token:', payload);
          // Extract user information from the token payload
          const user: User = {
            email: payload.sub || payload.email || '',
            nom: payload.nom || '',
            prenom: payload.prenom || '',
            role: role
          };

          console.log('Final user object with role:', user);
          this.currentUserSubject.next(user);
        } else {
          console.error('Invalid token format');
          this.currentUserSubject.next(null);
        }
      } catch (error) {
        console.error('Error decoding token:', error);
        this.currentUserSubject.next(null);
      }
    } else {
      this.currentUserSubject.next(null);
    }
  }

  // New method to get current user's email
  getCurrentUserEmail(): string {
    const user = this.currentUserSubject.value;
    console.log(user);
    return user ? user.email : '';
  }

  getCurrentUserNom(): string {
    const user = this.currentUserSubject.value;
    console.log(user?.nom );
    return user ? user.nom : '';
  }

  getCurrentUserRole(): string | null {
    const user = this.currentUserSubject.value;
    if (!user) return null;

    // Handle different formats of role storage
    let role = user.role || null;

    // If role is stored as an object with a name property
    if (role && typeof role === 'object') {
      // Use type assertion to bypass TypeScript's concern
      const roleObj = role as any;
      if ('name' in roleObj) {
        role = roleObj.name;
      } else if ('authority' in roleObj) {
        role = roleObj.authority;
      }
    }

    return role;
  }

  // Added debug method to help diagnose token issues
  debugToken(): void {
    const token = this.getToken();
    if (token) {
      try {
        const tokenParts = token.split('.');
        const payload = JSON.parse(atob(tokenParts[1]));
        console.log('Full token payload:', payload);
        console.log('Role information:', {
          role: payload.role,
          role_membre: payload.role_membre,
          authorities: payload.authorities
        });
      } catch (e) {
        console.error('Error decoding token:', e);
      }
    } else {
      console.warn('No token available to debug');
    }
  }

  // New method to debug role access
  debugRoleAccess(testedRoles: string[]): void {
    const userRole = this.getCurrentUserRole();
    console.log('Current user role:', userRole);

    for (const role of testedRoles) {
      console.log(`Has role ${role}:`, this.hasRole(role));
    }
  }

  isAdmin(): boolean {
    const role = this.getCurrentUserRole();
    return this.compareRoles(role, 'ADMIN');
  }

  hasRole(role: string): boolean {
    const userRole = this.getCurrentUserRole();
    

    if (userRole) {
      return this.compareRoles(userRole, role);
    }

    // If no direct role found, check the token payload
    const token = this.getToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));

        // Check if role_membre is present (this is what your token is using)
        if (payload.role_membre) {
          console.log('Found role_membre in token:', payload.role_membre);
          return this.compareRoles(payload.role_membre, role);
        }

        // Check other possible locations as a fallback
        const tokenRole = payload.role ||
                    (payload.authorities && payload.authorities.length > 0 ?
                      (typeof payload.authorities[0] === 'object' ?
                        payload.authorities[0].authority : payload.authorities[0]) : null);

        if (tokenRole) {
          console.log('Found role in token for hasRole check:', tokenRole);
          return this.compareRoles(tokenRole, role);
        }
      } catch (error) {
        console.error('Error parsing token in hasRole:', error);
      }
    }

    return false;
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  // Updated compare roles method
// Update the compareRoles method in auth.service.ts
private compareRoles(userRole: string | null, requiredRole: string): boolean {
  if (!userRole) return false;

  // Clean up roles by removing ROLE_ prefixes for comparison
  const cleanUserRole = userRole.replace('ROLE_', '');
  const cleanRequiredRole = requiredRole.replace('ROLE_', '');

  // Case insensitive comparison of the cleaned roles
  return cleanUserRole.toLowerCase() === cleanRequiredRole.toLowerCase();
}

  // Add this helper method to get the properly formatted role for Spring Security
  getSpringSecurityRole(role: string): string {
    // If the role doesn't already have ROLE_ prefix, add it
    if (!role.startsWith('ROLE_')) {
      return `ROLE_${role}`;
    }
    return role;
  }

  // For DE (Director of Education)
  isDE(): boolean {
    return this.hasRole('DE');
  }

  // For CHEF_DEPT (Chef de Department )
  isChefDept(): boolean {
    return this.hasRole('CHEF_DEPT');
  }

  // For CHEF_POLE (Chef de Pole)
  isChefPole(): boolean {
    return this.hasRole('CHEF_POLE');
  }

  // For RS (Responsable_scolarite)
  isRS(): boolean {
    return this.hasRole('RS');
  }



  changePassword(currentPassword: string, newPassword: string): Observable<any> {
    const email = this.currentUserSubject.value?.email;

    return this.http.post<any>(`${this.API_URL}/api/auth/change-password`, {
      email,
      currentPassword,
      newPassword
    }).pipe(
      tap(() => {
        // Mark password as changed after successful change
        this.storePasswordChangedStatus(true);
      })
    );
  }
}
