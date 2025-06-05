// auth.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Skip token for login and public endpoints
    if (request.url.includes('/api/auth/login') || request.url.includes('/api/auth/change-password')) {
      return next.handle(request);
    }
    
    // Get the auth token - do NOT modify it
    const token = this.authService.getToken();
    
    if (token) {
      // Clone and modify the request to add the authorization header
      const authReq = request.clone({
        headers: request.headers.set('Authorization', `Bearer ${token}`)
      });
      
      return next.handle(authReq).pipe(
        tap({
          error: (error: HttpErrorResponse) => {
            if (error.status === 403) {
              console.error('Access Forbidden (403) for URL:', request.url);
              console.error('Current user role:', this.authService.getCurrentUserRole());
              
              try {
                if (token) {
                  const tokenPayload = JSON.parse(atob(token.split('.')[1]));
                  console.error('Token payload:', {
                    sub: tokenPayload.sub,
                    role: tokenPayload.role,
                    role_membre: tokenPayload.role_membre,
                    authorities: tokenPayload.authorities,
                    exp: new Date(tokenPayload.exp * 1000).toLocaleString()
                  });
                }
              } catch (e) {
                console.error('Error decoding token:', e);
              }
            }
          }
        }),
        catchError((error: HttpErrorResponse) => {
          if (error.status === 401) {
            console.error('Unauthorized (401) - Token expired or invalid');
            this.authService.logout();
            this.router.navigate(['/auth/login']);
          } else if (error.status === 403) {
            console.error('You do not have permission to access this resource. Please contact your administrator if you believe this is an error.');
          }
          
          return throwError(() => error);
        })
      );
    }
    
    // If we get here without a token for a protected route, redirect to login
    if (!request.url.includes('/auth/')) {
      console.warn('Redirecting to login due to missing token');
      this.router.navigate(['/auth/login']);
    }
    
    return next.handle(request);
  }
}