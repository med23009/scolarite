import { FormArray } from '@angular/forms';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth/auth.service';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { DepartementService } from '../../core/services/admin/departement.service';
import { PoleService } from '../../core/services/admin/pole.service';
import { Departement } from '../../core/models/departement.model';
import { Pole } from '../../core/models/pole.model';
import { FilterPipe } from '../../core/pipes/filter.pipe';
import { DEProgrammeService } from '../../core/services/de/de-programme.service';


@Component({
  selector: 'app-de-programme',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FilterPipe],
  templateUrl: './de-programme.component.html',
  styleUrls: ['./de-programme.component.scss']
})
export class DEProgrammeComponent implements OnInit {
  private readonly API_URL = environment.apiUrl;

  selectionForm: FormGroup;
  departements: Departement[] = [];
  poles: Pole[] = [];
  programmes: any[] = [];

  selectedDepartmentId: string | null = null;
  selectedPoleId: string | null = null;
  selectedType: 'department' | 'pole' = 'department';
  editingProgrammeId: number | null = null;
  editForm: FormGroup;


  private readonly DEFAULT_DEPARTMENT_ID = '1';
  private readonly DEFAULT_POLE_ID = '1';

  loading = false;
  error = '';

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private authService: AuthService,
    private departementService: DepartementService,
    private poleService: PoleService,
    private deProgrammeService: DEProgrammeService,


  ) {
    this.selectionForm = this.fb.group({
      type: ['department'],
      departmentId: [''],
      poleId: ['']
    });

    this.editForm = this.fb.group({
  ue: this.fb.group({
    idUE: [''],
    codeUE: [''],
    intitule: ['']
  }),
  modules: this.fb.array([])
});

  }

ngOnInit(): void {
  this.loadDepartements();
  this.loadPoles();

  this.route.queryParams.subscribe(params => {
    if (params['departmentId']) {
      this.selectedDepartmentId = String(params['departmentId']);
      this.selectedType = 'department';
      this.selectionForm.patchValue({
        type: 'department',
        departmentId: this.selectedDepartmentId
      });
      this.loadProgrammesByDepartment(this.selectedDepartmentId);
    } else if (params['poleId']) {
      this.selectedPoleId = String(params['poleId']);
      this.selectedType = 'pole';
      this.selectionForm.patchValue({
        type: 'pole',
        poleId: this.selectedPoleId
      });
      this.loadProgrammesByPole(this.selectedPoleId);
    }
  });

  this.selectionForm.get('type')?.valueChanges.subscribe(type => {
    this.selectedType = type;
    if (type === 'department') {
      const departmentId = this.selectionForm.get('departmentId')?.value || this.DEFAULT_DEPARTMENT_ID;
      this.selectedDepartmentId = departmentId;
      this.loadProgrammesByDepartment(departmentId);
    } else {
      const poleId = this.selectionForm.get('poleId')?.value || this.DEFAULT_POLE_ID;
      this.selectedPoleId = poleId;
      this.loadProgrammesByPole(poleId);
    }
  });

  this.selectionForm.get('departmentId')?.valueChanges.subscribe(departmentId => {
    if (this.selectedType === 'department' && departmentId) {
      this.selectedDepartmentId = departmentId;
      this.loadProgrammesByDepartment(departmentId);
    }
  });

  this.selectionForm.get('poleId')?.valueChanges.subscribe(poleId => {
    if (this.selectedType === 'pole' && poleId) {
      this.selectedPoleId = poleId;
      this.loadProgrammesByPole(poleId);
    }
  });
}


loadDepartements(): void {
  this.departementService.getAllForDropdown().subscribe({
    next: (data) => {
      this.departements = data;
      let depId: string | null = this.route.snapshot.queryParamMap.get('departmentId');
      if (!depId) {
        depId = localStorage.getItem('selectedDepartmentId') || data[0]?.idDep?.toString() || null;
      }

      if (depId) {
        this.selectedDepartmentId = depId;
        this.selectedType = 'department';
        this.selectionForm.patchValue({
          type: 'department',
          departmentId: depId
        });
        this.loadProgrammesByDepartment(depId);
      }
    },
    error: (err) => {
      this.error = 'Erreur lors du chargement des départements';
      console.error(err);
    }
  });
}


  loadPoles(): void {
    this.poleService.getAll().subscribe({
      next: (data) => {
        this.poles = data;
        if (!this.selectionForm.get('poleId')?.value && data.length > 0) {
          this.selectionForm.patchValue({ poleId: data[0].idPole });
        }
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des pôles';
        console.error(err);
      }
    });
  }

loadProgrammesByDepartment(departmentId: string): void {
  if (!departmentId) return;
  this.loading = true;
  this.programmes = [];

  this.deProgrammeService.getUEByDepartement(departmentId).subscribe({
    next: (ues) => {
      const programmeRequests = ues.map(ue =>
        this.deProgrammeService.getEMByUE(ue.idUE!).toPromise().then(modules => ({ ue, modules }))
      );

      Promise.all(programmeRequests).then(results => {
        this.programmes = results;
        this.loading = false;
      });
    },
    error: (err) => {
      this.error = 'Erreur lors du chargement des programmes du département';
      this.loading = false;
      console.error(err);
    }
  });
}

loadProgrammesByPole(poleId: string): void {
  if (!poleId) return;
  this.loading = true;
  this.programmes = [];

  this.deProgrammeService.getUEByPole(poleId).subscribe({
    next: (ues) => {
      const programmeRequests = ues.map(ue =>
        this.deProgrammeService.getEMByUE(ue.idUE!).toPromise().then(modules => ({ ue, modules }))
      );

      Promise.all(programmeRequests).then(results => {
        this.programmes = results;
        this.loading = false;
      });
    },
    error: (err) => {
      this.error = 'Erreur lors du chargement des programmes du pôle';
      this.loading = false;
      console.error(err);
    }
  });
}

onEditUE(programme: any): void {
  this.editingProgrammeId = programme.ue.idUE;

  this.editForm.get('ue')?.patchValue({
    idUE: programme.ue.idUE,
    codeUE: programme.ue.codeUE,
    intitule: programme.ue.intitule
  });

  const modulesArray = this.editForm.get('modules') as FormArray;
  modulesArray.clear();

  programme.modules.forEach((em: any) => {
    modulesArray.push(this.fb.group({
      idEM: [em.idEM],
      codeEM: [em.codeEM],
      intitule: [em.intitule],
      nombreCredits: [em.nombreCredits],
      semestre: [em.semestre],
      heuresCM: [em.heuresCM],
      heuresTD: [em.heuresTD],
      heuresTP: [em.heuresTP]
    }));
  });
}

onSaveEdit(): void {
  const ueData = this.editForm.value.ue;
  const modules = this.editForm.value.modules;

  const token = this.authService.getToken(); // ou autre méthode pour récupérer le token
  const headers = { headers: { Authorization: `Bearer ${token}` } };

  this.http.put(`${this.API_URL}/api/de/programmes/ue/${ueData.idUE}`, ueData, headers).subscribe({
    next: () => {
      const updates = modules.map((em: any) => {
        return this.http.put(`${this.API_URL}/api/de/programmes/em/${em.idEM}`, em, headers).toPromise();
      });

      Promise.all(updates).then(() => {
        this.editingProgrammeId = null;
        if (this.selectedType === 'department' && this.selectedDepartmentId) {
          this.loadProgrammesByDepartment(this.selectedDepartmentId);
        } else if (this.selectedType === 'pole' && this.selectedPoleId) {
          this.loadProgrammesByPole(this.selectedPoleId);
        }
      });
    },
    error: (err) => {
      this.error = 'Erreur lors de la modification';
      console.error(err);
    }
  });
}

get modulesFormArray() {
  return this.editForm.get('modules') as FormArray;
}

get ueFormGroup() {
  return this.editForm.get('ue') as FormGroup;
}

onDeleteEM(idEM: number): void {
  if (confirm('Confirmer la suppression de cet élément de module ?')) {
    this.http.delete(`${this.API_URL}/api/de/programmes/em/${idEM}`).subscribe({
      next: () => {
        // Rechargement après suppression
        if (this.selectedType === 'department' && this.selectedDepartmentId) {
          this.loadProgrammesByDepartment(this.selectedDepartmentId);
        } else if (this.selectedType === 'pole' && this.selectedPoleId) {
          this.loadProgrammesByPole(this.selectedPoleId);
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression de l\'élément';
        console.error(err);
      }
    });
  }
}

  // Confirmation dialog
  showConfirmDialog = false
  confirmMessage = ""
  deleteCallback: (() => void) | null = null

confirmDelete(): void {
  if (this.deleteCallback) {
    this.deleteCallback()
    this.closeConfirmDialog()
  }
}

cancelDelete(): void {
  this.closeConfirmDialog()
}

closeConfirmDialog(): void {
  this.showConfirmDialog = false
  this.confirmMessage = ""
  this.deleteCallback = null
}


}
