import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../core/services/auth/user.service';
import { User } from '../../core/models/user.model';
import { DepartementService } from '../../core/services/admin/departement.service';
import { Departement } from '../../core/models/departement.model';
import { PoleService } from '../../core/services/admin/pole.service';
import { Pole } from '../../core/models/pole.model';

@Component({
  selector: 'app-user-edit',
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class UserEditComponent implements OnInit, OnDestroy {
  userForm: FormGroup;
  userId: number | null = null;
  loading = false;
  submitted = false;
  error = '';
  isAddMode = true;
  departements: Departement[] = [];
  poles: Pole[] = [];

  // Map role names to numeric values
  roleMap = {
    'ADMIN': 0,
    'CHEF_DEPT': 1,
    'CHEF_POLE': 2,
    'DE': 3,
    'RS': 4,
  };

  // Map for the reverse lookup
  reverseRoleMap = {
    0: 'ADMIN',
    1: 'CHEF_DEPT',
    2: 'CHEF_POLE',
    3: 'DE',
    4: 'RS',
  };

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private departementService: DepartementService,
    private poleService: PoleService
  ) {
    this.userForm = this.formBuilder.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['', Validators.required],
      departement: [null],
      pole: [null],
      dateNaissance: [''],
      compteBancaire: [''],
      telephone: [''],
      nationalite: [''],
      NNI: [''],
      nomBanque: [''],
      genre: [''],
      specialiste: [''],
      lieuDeNaissance: [''],
    });
  }

  ngOnInit(): void {
    this.userId = this.route.snapshot.params['id'];
    this.isAddMode = !this.userId;
    
    // Charger la liste des départements et des pôles
    this.loadDepartements();
    console.log('[UserEdit] Début du chargement des pôles');
    this.loadPoles();

    // Add a listener for role changes to update validators
    this.userForm.get('role')?.valueChanges.subscribe(role => {
      console.log('[UserEdit] Role changed to:', role);
      this.updateDepartementValidators();
      this.updatePoleValidators();
    });

    // If editing an existing user, load their data
    if (!this.isAddMode) {
      this.loading = true;
      console.log('Loading user data for ID:', this.userId);
      this.userService.getUserById(this.userId!).subscribe({
        next: (user) => {
          console.log('User data received:', user);
          console.log('Role value from backend:', user.role);
          // Convert string role to numeric if needed
          if (user.role && typeof user.role === 'string') {
            // Try to map the role name to its numeric value, fallback to 0 (ADMIN) if not found
            const roleValue = this.roleMap[user.role as keyof typeof this.roleMap] || 0;
            user.role = roleValue.toString(); // Convert to string to match User interface
          }

          this.userForm.patchValue(user);
          
          // Si c'est un chef de département, vérifier s'il a un département associé
          if (user.role === '1' && user.departementId) {
            this.userForm.get('departement')?.setValue(user.departementId);
          }
          
          // Don't require password in edit mode
          this.userForm.get('password')?.setValidators(null);
          this.userForm.get('password')?.updateValueAndValidity();
          
          // Mettre à jour les validateurs pour le département si nécessaire
          this.updateDepartementValidators();
          
          this.loading = false;
        },
        error: (error) => {
          console.error('[UserEdit] Error loading user data:', error);
          this.error = error.message || 'Erreur lors du chargement des données';
          this.loading = false;
        }
      });
    }
  }

  ngOnDestroy(): void {
    // Add any necessary cleanup code here
  }

  get f() { return this.userForm.controls; }

  onSubmit(): void {
    this.submitted = true;
    console.log('[UserEdit] Soumission du formulaire');

    if (this.userForm.invalid) {
      console.error('[UserEdit] Formulaire invalide', this.userForm.errors);
      return;
    }

    const userData = this.prepareUserData();
    console.log('[UserEdit] Données utilisateur préparées:', userData);
    this.loading = true;

    if (this.isAddMode) {
      console.log('[UserEdit] Mode création - Création utilisateur');
      this.createUser(userData);
    } else {
      console.log('[UserEdit] Mode édition - Mise à jour utilisateur');
      this.updateUser(userData);
    }
  }

  private prepareUserData(): User {
    const userData = { ...this.userForm.value };
    console.log('[UserEdit] Préparation des données utilisateur, valeurs du formulaire:', userData);

    // Ensure role is a number
    if (userData.role !== undefined && userData.role !== null) {
      userData.role = Number(userData.role);
      console.log(`[UserEdit] Rôle converti en nombre: ${userData.role}`);
    }
    
    // Si c'est un chef de département, ajouter l'ID du département
    if (this.isChefDepartement() && userData.departement) {
      userData.departementId = userData.departement;
      console.log(`[UserEdit] Chef de département détecté, ID du département associé: ${userData.departementId}`);
    }
    
    // Si c'est un chef de pôle, ajouter l'ID du pôle
    if (this.isChefPole() && userData.pole) {
      userData.poleId = userData.pole;
      console.log(`[UserEdit] Chef de pôle détecté, ID du pôle associé: ${userData.poleId}`);
    }
    
    // Supprimer les champs departement et pole car ce ne sont pas des champs du modèle User
    delete userData.departement;
    delete userData.pole;
    console.log('[UserEdit] Données finales préparées:', userData);
    
    return userData;
  }

  private createUser(userData: User): void {
    console.log('[UserEdit] Appel au service pour créer un utilisateur:', userData);
    this.userService.createUser(userData)
      .subscribe({
        next: (response) => {
          console.log('[UserEdit] Utilisateur créé avec succès, réponse:', response);
          // Forcer un rafraîchissement de la liste des départements et des pôles
          this.departementService.refreshDepartements();
          this.poleService.refreshPoles();
          this.router.navigate(['/admin/users'], { queryParams: { created: true } });
        },
        error: error => {
          console.error('[UserEdit] Erreur lors de la création de l\'utilisateur:', error);
          this.error = error.error?.message || 'Erreur lors de la création';
          this.loading = false;
        }
      });
  }

  private updateUser(userData: User): void {
    console.log(`[UserEdit] Appel au service pour mettre à jour l'utilisateur ID=${this.userId}:`, userData);
    this.userService.updateUser(this.userId!, userData)
      .subscribe({
        next: (response) => {
          console.log('[UserEdit] Utilisateur mis à jour avec succès, réponse:', response);
          // Forcer un rafraîchissement de la liste des départements et des pôles
          this.departementService.refreshDepartements();
          this.poleService.refreshPoles();
          this.router.navigate(['/admin/users'], { queryParams: { updated: true } });
        },
        error: error => {
          console.error('[UserEdit] Erreur lors de la mise à jour de l\'utilisateur:', error);
          this.error = error.error?.message || 'Erreur lors de la mise à jour';
          this.loading = false;
        }
      });
  }

  /**
   * Vérifie si le rôle sélectionné est CHEF_DEPT (valeur 1)
   */
  isChefDepartement(): boolean {
    const roleValue = this.userForm.get('role')?.value;
    return roleValue === '1' || roleValue === 1;
  }
  
  /**
   * Vérifie si le rôle sélectionné est CHEF_POLE (valeur 2)
   */
  isChefPole(): boolean {
    const roleValue = this.userForm.get('role')?.value;
    console.log('[UserEdit] isChefPole() - roleValue:', roleValue, 'type:', typeof roleValue);
  
    // Check for all possible representations of CHEF_POLE role
    const isChefPole = roleValue === '2' || roleValue === 2 || roleValue === 'CHEF_POLE' || roleValue === 'ROLE_CHEF_POLE';
    console.log('[UserEdit] isChefPole() result:', isChefPole);
  
    return isChefPole;
  }

  // Appelé lorsque le rôle est changé dans le formulaire
  onRoleChange(): void {
    console.log('[UserEdit] Changement de rôle:', this.userForm.get('role')?.value);
    this.updateDepartementValidators();
    this.updatePoleValidators();
  }
  
  /**
   * Met à jour les validateurs pour le champ département en fonction du rôle
   */
  private updateDepartementValidators(): void {
    const departementControl = this.userForm.get('departement');
    
    if (this.isChefDepartement()) {
      departementControl?.setValidators([Validators.required]);
    } else {
      departementControl?.clearValidators();
      departementControl?.setValue(null);
    }
    
    departementControl?.updateValueAndValidity();
  }
  
  /**
   * Met à jour les validateurs pour le champ pole en fonction du rôle
   */
  private updatePoleValidators(): void {
    const poleControl = this.userForm.get('pole');
    
    if (this.isChefPole()) {
      poleControl?.setValidators([Validators.required]);
    } else {
      poleControl?.clearValidators();
      poleControl?.setValue(null);
    }
    
    poleControl?.updateValueAndValidity();
  }
  
  /**
   * Charge la liste des départements depuis le service
   */
  private loadDepartements(): void {
    this.departementService.getAll().subscribe({
      next: (departements) => {
        this.departements = departements;
        console.log('[UserEdit] Départements chargés:', this.departements.length);
      },
      error: (error) => {
        console.error('[UserEdit] Erreur lors du chargement des départements:', error);
        this.error = 'Erreur lors du chargement des départements';
      }
    });
  }
  
  /**
   * Charge la liste des pôles depuis le service
   */
  private loadPoles(): void {
    console.log('[UserEdit] Début du chargement des pôles via PoleService.getAll()');
    this.poleService.getAll().subscribe({
      next: (poles) => {
        this.poles = poles;
        console.log('[UserEdit] Pôles chargés avec succès:', poles);
        console.log('[UserEdit] Nombre de pôles chargés:', this.poles.length);
      },
      error: (error) => {
        console.error('[UserEdit] Erreur lors du chargement des pôles:', error);
        console.error('[UserEdit] Détails de l\'erreur:', JSON.stringify(error));
        this.error = 'Erreur lors du chargement des pôles';
      }
    });
  }
}
