import { Injectable } from '@angular/core';
import { 
  CanActivate, 
  ActivatedRouteSnapshot, 
  RouterStateSnapshot, 
  Router 
} from '@angular/router';
import { AuthService } from '../services/auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ChangePasswordGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    // If user is not logged in, redirect to login
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login']);
      return false;
    }

    // If password has already been changed, redirect to appropriate page based on role
    if (this.authService.isPasswordChanged()) {
      if (this.authService.isAdmin()) {
        this.router.navigate(['/admin/users']);
      } else if (this.authService.isChefDept()) {
        this.router.navigate(['/chefdept/programmes']);
      } else if (this.authService.isChefPole()) {
        this.router.navigate(['/chefpole/programmes']);
      } else if (this.authService.isRS() || this.authService.isDE()) {
        this.router.navigate(['/scolarite/etudiants']);
      } else {
        this.router.navigate(['/admin']);
      }
      return false;
    }

    // Allow access to change password page only if password hasn't been changed
    return true;
  }
} 