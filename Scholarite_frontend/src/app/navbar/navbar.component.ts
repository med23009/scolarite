import { Component, OnInit, HostListener } from '@angular/core';
import { AuthService } from './../core/services/auth/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule]
})
export class NavbarComponent implements OnInit {
  isScrolled = false;
  isMobileMenuOpen = false;
  isDepartmentSubmenuOpen = false;
  isUserDropdownOpen = false;
  showDepartmentDropdown = false;
  isEnglish = false;
  currentUser: any;
  searchTerm: string = '';
  isUserMenuOpen = false;

  constructor(
    public authService: AuthService // Changed to public for template access
  ) { }

  ngOnInit(): void {
    // Récupérer l'utilisateur courant
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.isScrolled = window.scrollY > 10;
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
    if (!this.isMobileMenuOpen) {
      this.isDepartmentSubmenuOpen = false;
    }
    // Empêcher le défilement du body quand le menu est ouvert
    document.body.style.overflow = this.isMobileMenuOpen ? 'hidden' : '';
  }

  toggleDepartmentSubmenu(): void {
    this.isDepartmentSubmenuOpen = !this.isDepartmentSubmenuOpen;
  }

  toggleUserDropdown(): void {
    this.isUserDropdownOpen = !this.isUserDropdownOpen;
  }

  switchLanguage(isEnglish: boolean): void {
    this.isEnglish = isEnglish;
    // Implémentez ici la logique pour changer la langue
  }
  
  logout(): void {
    this.authService.logout();
  }

  // Fermer les dropdowns quand on clique ailleurs sur la page
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const userMenuElement = (event.target as HTMLElement).closest('.user-menu');
    if (!userMenuElement) {
      this.isUserMenuOpen = false;
    }
  }

  toggleUserMenu(): void {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }
}
