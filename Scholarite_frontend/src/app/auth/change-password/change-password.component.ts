// src/app/auth/change-password/change-password.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth/auth.service';
import { Router, NavigationStart } from '@angular/router';
import { filter } from 'rxjs/operators';
import { Location } from '@angular/common';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class ChangePasswordComponent implements OnInit {
  changePasswordForm: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  success = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private location: Location
  ) {
    this.changePasswordForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validator: this.mustMatch('newPassword', 'confirmPassword')
    });

    // Prevent going back if password has been changed
    if (this.authService.isPasswordChanged()) {
      this.navigateBasedOnRole();
    }
  }

  ngOnInit() {
    // Subscribe to navigation events
    this.router.events.pipe(
      filter(event => event instanceof NavigationStart)
    ).subscribe((event: any) => {
      // If trying to navigate back and password has been changed
      if (event.navigationTrigger === 'popstate' && this.authService.isPasswordChanged()) {
        // Prevent the navigation
        this.location.go(this.router.url);
        // Navigate to appropriate page based on role
        this.navigateBasedOnRole();
      }
    });
  }

  mustMatch(controlName: string, matchingControlName: string) {
    return (formGroup: FormGroup) => {
      const control = formGroup.controls[controlName];
      const matchingControl = formGroup.controls[matchingControlName];

      if (matchingControl.errors && !matchingControl.errors['mustMatch']) {
        return;
      }

      if (control.value !== matchingControl.value) {
        matchingControl.setErrors({ mustMatch: true });
      } else {
        matchingControl.setErrors(null);
      }
    };
  }

  get f() { return this.changePasswordForm.controls; }

  // Navigate based on user role
  private navigateBasedOnRole(): void {
    const role = this.authService.getCurrentUserRole();
    console.log('Navigating based on role after password change:', role);
  
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
  }

  onSubmit() {
    this.submitted = true;

    if (this.changePasswordForm.invalid) {
      return;
    }

    this.loading = true;
    this.authService.changePassword(
      this.f['currentPassword'].value,
      this.f['newPassword'].value
    ).subscribe({
      next: () => {
        this.success = 'Password changed successfully';
        this.loading = false;
        // Replace the current history state to prevent going back
        this.location.replaceState('/auth/change-password');
        // Navigate after a short delay
        setTimeout(() => {
          this.navigateBasedOnRole();
        },0);
      },
      error: error => {
        this.error = error.error?.message || 'An error occurred while changing the password';
        this.loading = false;
      }
    });
  }
}