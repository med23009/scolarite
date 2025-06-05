// src/app/admin/user-list/user-list.component.ts
import { Component, OnInit } from '@angular/core';
import { UserService } from '../../core/services/auth/user.service';
import { User } from '../../core/models/user.model';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  loading = false;
  error = '';

  constructor(
    private userService: UserService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: (error) => {
        this.error = error.message || 'Erreur lors du chargement des utilisateurs';
        this.loading = false;
      }
    });
  }

  editUser(id: number): void {
    this.router.navigate(['/admin/users/edit', id]);
  }

  deleteUser(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      this.userService.deleteUser(id).subscribe({
        next: (response) => {
          console.log('User deleted successfully:', response.message);
          this.loadUsers();
        },
        error: (error) => {
          console.error('Error deleting user:', error);
          this.error = error.error?.message || 'Erreur lors de la suppression de l\'utilisateur';
        }
      });
    }
  }
}