// src/app/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-login',  
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup; // Use definite assignment assertion
  loading = false;
  submitted = false;
  error = '';
  returnUrl: string = '/admin/'; // Initialize with a default value

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
    // Redirect if already logged in
    if (this.authService.isLoggedIn()) {
      this.navigateBasedOnRole();
      return;
    }

    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/admin/';
  }

  ngOnInit(): void {}

  get f() { return this.loginForm.controls; }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.authService.login({
      email: this.f['email'].value,
      password: this.f['password'].value
    })
    .pipe(
      finalize(() => {
        this.loading = false;
      })
    )
    .subscribe({
      next: () => {
        // Check if the initial password change has been done using the AuthService
        if (!this.authService.isPasswordChanged()) {
          // First login or password not changed yet, force password change
          console.log('Password change required, navigating to change password page.');
          this.router.navigate(['/auth/change-password']);
        } else {
          // Password already changed, navigate based on role
          console.log('Password already changed, navigating based on role.');
          this.navigateBasedOnRole();
        }
        
        console.log('Login successful');
      },
      error: error => {
        this.error = error.error?.message || 'Une erreur est survenue lors de la connexion.';
      }
    });
  }

  // New method to navigate based on user role
  private navigateBasedOnRole(): void {
    // Get the current user role
    const role = this.authService.getCurrentUserRole();
    console.log('Navigating based on role:', role);
  
    // Navigate based on role
    if (this.authService.isAdmin()) {
      this.router.navigate(['/admin/users']);
    } else if (this.authService.isChefDept()) {
      this.router.navigate(['/chefdept/programmes']);
    } else if (this.authService.isChefPole()) {
      this.router.navigate(['/chefpole/programmes']);
    } else if (this.authService.isRS()) {
      this.router.navigate(['/scolarite/etudiants']);
    } else if (this.authService.isDE()) {
      this.router.navigate(['/scolarite/etudiants']);
    } else {
      // Default fallback route
      this.router.navigate([this.returnUrl]);
    }
  }
}