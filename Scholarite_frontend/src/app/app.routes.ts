import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { ChangePasswordGuard } from './core/guards/change-password.guard';
import { NavbarComponent } from './navbar/navbar.component';
import { UserListComponent } from './admin/user-list/user-list.component';
import { UserEditComponent } from './admin/user-edit/user-edit.component';
import { HistoriqueComponent } from './admin/historique/historique.component';
import { DepartementComponent } from './admin/departement/departement.component';
import { PoleComponent } from './admin/pole/pole.component';
import { SemestreComponent } from './admin/semestre/semestre.component';
import { LoginComponent } from './auth/login/login.component';
import { ChangePasswordComponent } from './auth/change-password/change-password.component';

// Import ChefDept components
import { ProgrammeComponent as ChefDeptProgrammeComponent } from './chefdept/programme/programme.component';
import { PvComponent as ChefDeptPVComponent } from './chefdept/pv/pv.component';
import { DEProgrammeComponent } from './de/programme/de-programme.component';
// Import ChefPole components
import { ProgrammeComponent as ChefPoleProgrammeComponent } from './chefpole/programme/programme.component';
import { ReleveNoteComponent } from './scolarite/releve-note/releve-note.component';
import { Etudiants } from './scolarite/etudiants/etudiants.component';
import { NoteComponent } from './chefdept/note/note.component';
import { DENotesComponent } from './de/notes/de-notes.component';
import { PreviewPvComponent } from './chefdept/preview-pv/preview-pv.component';
import { PlanEtudesComponent } from './scolarite/plan-etudes/plan-etudes.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/auth/login',
    pathMatch: 'full',
  },
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        component: LoginComponent
      },
      {
        path: 'change-password',
        component: ChangePasswordComponent,
        canActivate: [AuthGuard]
      }
    ]
  },
  // Routes for ADMIN role
  {
    path: 'admin',
    component: NavbarComponent,
    canActivate: [AuthGuard],
    children: [
      // Utilisateurs
      {
        path: 'users',
        component: UserListComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'users/new',
        component: UserEditComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'users/edit/:id',
        component: UserEditComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },

      // Départements
      {
        path: 'departements',
        component: DepartementComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },

      // Pôles
      {
        path: 'poles',
        component: PoleComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },

      // Semestres
      {
        path: 'semestres',
        component: SemestreComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },

      // Historique des actions
      {
        path: 'historique',
        component: HistoriqueComponent,
        canActivate: [AuthGuard],
        data: { roles: ['ROLE_ADMIN'] }
      },
      
    ]
  },

  // Routes for CHEF_DEPT role
  {
    path: 'chefdept',
    component: NavbarComponent,
    canActivate: [AuthGuard],
    data: { roles: ['CHEF_DEPT'] },
    children: [
      {
        path: '',
        redirectTo: 'programmes',
        pathMatch: 'full'
      },
      {
        path: 'programmes',
        component: ChefDeptProgrammeComponent,
        canActivate: [AuthGuard],
        data: { roles: ['CHEF_DEPT'] }
      },
      {
        path: 'pv',
        component: ChefDeptPVComponent,
        canActivate: [AuthGuard],
        data: { roles: ['CHEF_DEPT'] }
      },
      {
        path: 'notes',
        component: NoteComponent,
        canActivate: [AuthGuard],
        data: { roles: ['CHEF_DEPT'] }
      },
      {
        path: 'preview-pv',
        component: PreviewPvComponent,
        canActivate: [AuthGuard],
        data: { roles: ['CHEF_DEPT'] }
      }
    ]
  },

  // Routes for CHEF_POLE role
  {
    path: 'chefpole',
    component: NavbarComponent,
    canActivate: [AuthGuard],
    data: { roles: ['CHEF_POLE'] },
    children: [
      {
        path: '',
        redirectTo: 'programmes',
        pathMatch: 'full'
      },
      {
        path: 'programmes',
        component: ChefPoleProgrammeComponent,
        canActivate: [AuthGuard],
        data: { roles: ['CHEF_POLE'] }
      },
      {
        path: 'notes',
        component: NoteComponent,
        canActivate: [AuthGuard],
        data: { roles: ['CHEF_POLE'] }
      },
    ]
  },

  // Routes for RELEVE_NOTE role
  // Routes for SCOLARITE role
{
  path: 'scolarite',
  component: NavbarComponent,
  canActivate: [AuthGuard],
  data: { roles: ['RS'] },
  children: [
    {
      path: '',
      redirectTo: 'etudiants',
      pathMatch: 'full'
    },
    {
      path: 'releve-note',
      component: ReleveNoteComponent,
      canActivate: [AuthGuard],
      data: { roles: ['RS'] }
    },
    {
      path: 'etudiants',
      component: Etudiants,
      canActivate: [AuthGuard],
      data: { roles: ['RS'] }
    },
    {
      path: 'plan-etudes',
      component: PlanEtudesComponent,
      canActivate: [AuthGuard],
      data: { roles: ['RS'] }
    }
  ]

},
// Routes for DE role
{
  path: 'de',
  component: NavbarComponent,
  canActivate: [AuthGuard],
  data: { roles: ['DE'] },
  children: [
    {
      path: '',
      redirectTo: 'programmes',
      pathMatch: 'full'
    },
    {
      path: 'programmes',
      component: DEProgrammeComponent,
      canActivate: [AuthGuard],
      data: { roles: ['DE'] }
    },
    {
      path: 'departements',
      component: DepartementComponent,
      canActivate: [AuthGuard],
      data: { roles: ['DE'] }
    },
    {
      path: 'poles',
      component: PoleComponent,
      canActivate: [AuthGuard],
      data: { roles: ['DE'] }
    },
    {
      path: 'semestres',
      component: SemestreComponent,
      canActivate: [AuthGuard],
      data: { roles: ['DE'] }
    },

    {
      path: 'notes',
      component: DENotesComponent,
      canActivate: [AuthGuard],
      data: { roles: ['DE'] }
    }
  ]
},
  // Fallback
  { path: '**', redirectTo: '/auth/login' }
];
