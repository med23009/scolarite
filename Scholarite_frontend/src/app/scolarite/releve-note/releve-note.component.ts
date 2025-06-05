import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReleveNoteService } from '../../core/services/scolarite/releve-note.service';
import { PdfRelveNoteService } from '../../core/services/scolarite/pdf-relve-note.service';
import { ReleveDeNotes } from '../../core/models/ReleveDeNotes';
import { Etudiant } from '../../core/models/etudiant.model';
import { NoteSemestrielle } from '../../core/models/note-semestrielle.model';
import { Semestre } from '../../core/models/semestre.model';

import JSZip from 'jszip';
import { saveAs } from 'file-saver';
import { Departement } from '../../core/models/departement.model';

@Component({
  selector: 'app-releve-note',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './releve-note.component.html',
  styleUrls: ['./releve-note.component.scss']
})
export class ReleveNoteComponent implements OnInit {
  matricule: string = '';
  idSemestre: number | null = null;
  // Messages d'alerte
error: string | null = null;
success: string | null = null;
  loading: boolean = false;
  semestres: Semestre[] = [];
  submitted = false;
  matriculesTextarea: string = '';


loadingIndividuel = false
  loadingMasse = false
  loadingDepartement = false
  loadingEmail = false
matriculesList: string[] = [];
promotion: string = '';
idDepartement: number | null = null;

departements: Departement[] = [];
modeSelection: 'individuel' | 'masse' | 'departement' = 'individuel';



  constructor(
    private releveNoteService: ReleveNoteService,
    private pdfService: PdfRelveNoteService
  ) {}

  ngOnInit() {
    this.loadSemestres();
    this.loadDepartements();
  }
loadDepartements() {
  this.releveNoteService.getDepartements().subscribe({
    next: (data) => {
      this.departements = data;
    },
    error: () => {
      this.error = 'Erreur lors du chargement des départements.';
    }
  });
}
  loadSemestres() {
    this.releveNoteService.getSemestres().subscribe({
      next: (data) => {
        this.semestres = data;
      },
      error: () => {
        this.error = 'Impossible de charger les semestres.';
      }
    });
    
  }
onTextareaChange(): void {
  const clean = this.matriculesTextarea
    .split(/[\s,;\n]+/)
    .map(m => m.trim())
    .filter(m => m.length > 0);
  this.matriculesList = Array.from(new Set(clean));
}
  generateReleve(): void {
    this.submitted = true

    if (!this.matricule || !this.idSemestre) {
      this.error = "Veuillez remplir tous les champs."
      return
    }

    this.loading = true
    this.error = ""
    this.success = ""

    this.releveNoteService.getBulletinData(this.matricule, Number(this.idSemestre)).subscribe({
      next: (bulletin) => {
        if (!bulletin.etudiant || !bulletin.semestre) {
          this.error = "Données du bulletin incomplètes"
          this.loading = false
          return
        }

        const notesParUE = this.organiserNotesParUE(bulletin.notes || [])
        const creditsValides = this.calculerCreditsValides(notesParUE)

        const releveDeNotes: ReleveDeNotes = {
          etudiant: {
            matricule: bulletin.etudiant.matricule || "Non défini",
            nom: bulletin.etudiant.nom || "Non défini",
            prenom: bulletin.etudiant.prenom || "Non défini",
            dateNaissance: bulletin.etudiant.dateNaissance || "Non défini",
            specialite: bulletin.etudiant.filiere?.intitule || "Non définie",
            dateInscription: bulletin.etudiant.dateInscription || "Non défini",
            email: bulletin.etudiant.email || "Non défini",
          },
          semestre: {
            semestre: bulletin.semestre.semestre || "Non défini",
            anneeUniversitaire: bulletin.semestre.annee?.toString() || "Non défini",
          },
          notesParUE: notesParUE,
semestreValide: this.calculerCreditsValides(notesParUE) === this.calculerCreditsTotaux(bulletin.notes || []),

          creditsAccumules: creditsValides,
          creditsTotaux: this.calculerCreditsTotaux(bulletin.notes || []),
          moyenneGenerale: this.calculerMoyenneGenerale(bulletin.notes || []),

          dateSignature: new Date().toISOString(),
        }

        this.pdfService
          .generateReleveDeNotesPDF(releveDeNotes)
          .then(() => {
            this.success = "Relevé de notes généré et téléchargé avec succès."
            this.loading = false
          })
          .catch((error) => {
            console.error("Erreur PDF:", error)
            this.error = "Erreur lors de la génération du PDF."
            this.loading = false
          })
      },
      error: (error) => {
        console.error("Erreur lors de la récupération des données:", error)
        this.loading = false

        if (error.status === 404) {
          this.error = `Aucun relevé trouvé pour le matricule ${this.matricule} et le semestre sélectionné.`
        } else {
          this.error = "Erreur lors de la récupération des données. Veuillez réessayer."
        }
      },
    })
  }

  private organiserNotesParUE(notes: any[]): { [key: string]: NoteSemestrielle[] } {
    const notesParUE: { [key: string]: NoteSemestrielle[] } = {};

    notes.forEach((note) => {
      const ueCode = note.elementModule?.codeEU || 'UE Non définie';
    
      if (!ueCode) {
        console.warn(`UE non définie pour le module :`, note.elementModule);
        return;
      }
    
      const moyenneEM = note.noteGenerale || 0;
      const moyenneUE = this.calculerMoyenneUE(ueCode, notes);
      const moyenneGenerale = this.calculerMoyenneGenerale(notes);
    
      const isValide = moyenneUE >= 10 || moyenneEM >= 10 ||
                       (moyenneEM >= 6 && moyenneUE >= 10) ||
                       (moyenneUE >= 8 && moyenneGenerale >= 10);
    
      if (!notesParUE[ueCode]) notesParUE[ueCode] = [];
    
      notesParUE[ueCode].push({
        codeEM: note.elementModule.codeEM,
        elementModule: {
          codeEM: note.elementModule.codeEM,
          intitule: note.elementModule.intitule,
          nombreCredits: note.elementModule.nombreCredits
        },
        noteGenerale: moyenneEM,
        credit: note.elementModule.nombreCredits,
        isValide
      });
    });
    ;

    return notesParUE;
  }

  private calculerMoyenneUE(ueCode: string, notes: any[]): number {
    const notesUE = notes.filter(n => n.elementModule?.ue?.code === ueCode);
    const total = notesUE.reduce((sum, n) => sum + (n.noteGenerale || 0) * (n.elementModule?.nombreCredits || 0), 0);
    const totalCredits = notesUE.reduce((sum, n) => sum + (n.elementModule?.nombreCredits || 0), 0);
    return totalCredits > 0 ? Math.round((total / totalCredits) * 100) / 100 : 0;
  }

  private calculerMoyenneGenerale(notes: any[]): number {
    const total = notes.reduce((sum, n) => sum + (n.noteGenerale || 0) * (n.elementModule?.nombreCredits || 0), 0);
    const totalCredits = notes.reduce((sum, n) => sum + (n.elementModule?.nombreCredits || 0), 0);
    return totalCredits > 0 ? Math.round((total / totalCredits) * 100) / 100 : 0;
  }

  private calculerCreditsTotaux(notes: any[]): number {
    return notes.reduce((total, note) => total + (note.elementModule?.nombreCredits || 0), 0);
  }
  private calculerCreditsValides(notesParUE: { [key: string]: NoteSemestrielle[] }): number {
    let total = 0;
    Object.values(notesParUE).forEach(notes => {
      notes.forEach(note => {
        if (note.isValide) {
          total += note.credit || 0;
        }
      });
    });
    return total;
  }
  generateRelevesEnMasse(): void {
  if (!this.idSemestre || this.matriculesList.length === 0) {
    this.error = "Veuillez sélectionner un semestre et entrer au moins un matricule.";
    return;
  }

  const zip = new JSZip();

  const requests = this.matriculesList
    .filter(m => m.trim() !== '')
    .map(matricule =>
      this.releveNoteService.getBulletinData(matricule.trim(), this.idSemestre!).toPromise()
        .then(bulletin => {
          const notesParUE = this.organiserNotesParUE(bulletin.notes || []);
          const creditsValides = this.calculerCreditsValides(notesParUE);

          const releve: ReleveDeNotes = {
            etudiant: {
              matricule: bulletin.etudiant.matricule,
              nom: bulletin.etudiant.nom,
              prenom: bulletin.etudiant.prenom,
              dateNaissance: bulletin.etudiant.dateNaissance,
              specialite: bulletin.etudiant.filiere?.intitule,
              dateInscription: bulletin.etudiant.dateInscription,
              email: bulletin.etudiant.email,
            },
            semestre: {
              semestre: bulletin.semestre.semestre,
              anneeUniversitaire: bulletin.semestre.annee?.toString(),
            },
            notesParUE,
semestreValide: this.calculerCreditsValides(notesParUE) === this.calculerCreditsTotaux(bulletin.notes || []),
            creditsAccumules: creditsValides,
            creditsTotaux: this.calculerCreditsTotaux(bulletin.notes || []),
            moyenneGenerale: this.calculerMoyenneGenerale(bulletin.notes || []),

            dateSignature: new Date().toISOString(),
          };

return this.pdfService.generateReleveDeNotesPDF(releve, true) as Promise<Uint8Array>;
        })
        .then(pdfBytes => {
          zip.file(`${matricule.trim()}_bulletin.pdf`, pdfBytes);
        })
    );

  Promise.all(requests).then(() => {
    zip.generateAsync({ type: 'blob' }).then(blob => {
      saveAs(blob, 'bulletins.zip');
    });
  }).catch(err => {
    console.error("Erreur lors de la génération en masse :", err);
    this.error = "Erreur lors de la génération de certains bulletins.";
  });
}
generateRelevesByPromoAndDept(): void {
  this.submitted = true;

  if (!this.promotion || !this.idDepartement || !this.idSemestre) {
    this.error = 'Veuillez renseigner la promotion, le département et le semestre.';
    return;
  }
    this.loadingDepartement = true

  this.loading = true;
  this.error = '';
  this.success = '';

  this.releveNoteService.getEtudiantsByDeptAndPromo(
    this.idDepartement!,
    this.promotion!,
    this.idSemestre!
  ).subscribe({
    next: (etudiants) => {
      console.log("Étudiants récupérés :", etudiants);
      if (!etudiants || etudiants.length === 0) {
        this.error = "Aucun étudiant trouvé pour les critères sélectionnés.";
    this.loadingDepartement = false
        return;
      }

      const zip = new JSZip();

      const requests = etudiants.map(etudiant => {
        const matricule = etudiant.matricule || etudiant.code || null;
        if (!matricule) {
          console.warn("Étudiant sans matricule :", etudiant);
          return Promise.resolve(); // ignore cet étudiant
        }

        return this.releveNoteService.getBulletinData(matricule, this.idSemestre!)
          .toPromise()
          .then(bulletin => {
            const notesParUE = this.organiserNotesParUE(bulletin.notes || []);
            const creditsValides = this.calculerCreditsValides(notesParUE);

            const releve: ReleveDeNotes = {
              etudiant: {
                matricule,
                nom: etudiant.nom,
                prenom: etudiant.prenom,
                dateNaissance: etudiant.dateNaissance,
                specialite: etudiant.departement?.intitule,
                dateInscription: etudiant.dateInscription,
                email: etudiant.email,
              },
              semestre: {
                semestre: bulletin.semestre.semestre,
                anneeUniversitaire: bulletin.semestre.annee?.toString(),
              },
              notesParUE,
              semestreValide: creditsValides === this.calculerCreditsTotaux(bulletin.notes || []),
              creditsAccumules: creditsValides,
              creditsTotaux: this.calculerCreditsTotaux(bulletin.notes || []),
              moyenneGenerale: this.calculerMoyenneGenerale(bulletin.notes || []),
              dateSignature: new Date().toISOString(),
            };

            return this.pdfService.generateReleveDeNotesPDF(releve, true) as Promise<Uint8Array>;
          })
          .then(pdfBytes => {
            if (pdfBytes) zip.file(`${matricule}_releve.pdf`, pdfBytes);
          });
      });

      Promise.all(requests)
        .then(() => zip.generateAsync({ type: 'blob' }))
        .then(blob => {
          saveAs(blob, `releves-${this.promotion}-dept-${this.idDepartement}.zip`);
          this.success = 'Relevés générés et téléchargés avec succès.';
    this.loadingDepartement = false
        })
        .catch(error => {
          console.error("Erreur lors de la génération des PDF :", error);
          this.error = "Erreur lors de la génération de certains bulletins.";
    this.loadingDepartement = false
        });
    },
    error: (err) => {
      console.error("Erreur chargement étudiants :", err);
      this.error = "Impossible de récupérer les étudiants pour cette promotion et ce département.";
    this.loadingDepartement = false
    }
  });
}
extraireMatricules(input: string): string[] {
  return input
    .split(/[\s,;\n]+/)   // Sépare par espace, virgule, point-virgule ou retour à la ligne
    .map(m => m.trim())
    .filter(m => m.length > 0);
}


envoyerRelevesParEmail(): void {
  this.submitted = true;

  if (!this.promotion || !this.idDepartement || !this.idSemestre) {
    this.error = 'Veuillez renseigner la promotion, le département et le semestre.';
    return;
  }
    this.loadingEmail = true

  this.loading = true;
  this.error = '';
  this.success = '';

  this.releveNoteService.getEtudiantsByDeptAndPromo(this.idDepartement!, this.promotion!, this.idSemestre!).subscribe({
    next: (etudiants) => {
      const requests = etudiants.map(etudiant =>
        this.releveNoteService.getBulletinData(etudiant.matricule, this.idSemestre!)
          .toPromise()
          .then(bulletin => {
            const notesParUE = this.organiserNotesParUE(bulletin.notes || []);
            const creditsValides = this.calculerCreditsValides(notesParUE);

            const releve: ReleveDeNotes = {
              etudiant: {
                matricule: etudiant.matricule,
                nom: etudiant.nom,
                prenom: etudiant.prenom,
                dateNaissance: etudiant.dateNaissance,
                specialite: etudiant.departement?.intitule,
                dateInscription: etudiant.dateInscription,
                email: etudiant.email,
              },
              semestre: {
                semestre: bulletin.semestre.semestre,
                anneeUniversitaire: bulletin.semestre.annee?.toString(),
              },
              notesParUE,
              semestreValide: creditsValides === this.calculerCreditsTotaux(bulletin.notes || []),
              creditsAccumules: creditsValides,
              creditsTotaux: this.calculerCreditsTotaux(bulletin.notes || []),
              moyenneGenerale: this.calculerMoyenneGenerale(bulletin.notes || []),
              dateSignature: new Date().toISOString(),
            };

            return this.pdfService.generateReleveDeNotesPDF(releve, true).then(pdfBytes => {
              if (!pdfBytes) throw new Error("PDF non généré pour " + etudiant.email);

              const base64 = btoa(
                Array.from(pdfBytes as Uint8Array)
                  .map(byte => String.fromCharCode(byte))
                  .join('')
              );

              return {
                email: etudiant.email,
                pdfBase64: base64
              };
            });
          })
      );

      Promise.all(requests)
        .then(payloads => {
          this.releveNoteService.sendRelevesParEmail(payloads).subscribe({
            next: () => {
              this.success = 'Relevés envoyés par email avec succès.';
    this.loadingEmail = false
            },
            error: err => {
              console.error('Erreur backend envoi mails :', err);
              this.error = "Erreur lors de l'envoi des emails.";
    this.loadingEmail = false
            }
          });
        })
        .catch(err => {
          console.error("Erreur dans la génération des bulletins :", err);
          this.error = "Erreur pendant la génération ou l'encodage des PDF.";
    this.loadingEmail = false
        });
    },
    error: err => {
      console.error("Erreur chargement étudiants :", err);
      this.error = "Impossible de récupérer les étudiants pour cette promotion et ce département.";
      this.loading = false;
    }
  });
}

envoyerRelevesEnMasseParEmail(): void {
  this.submitted = true;

  if (!this.matriculesTextarea || !this.idSemestre) {
    this.error = "Veuillez renseigner les matricules et le semestre.";
    return;
  }

  const matricules = this.extraireMatricules(this.matriculesTextarea);
  this.loading = true;
  this.error = '';
  this.success = '';

  const requests = matricules.map((matricule: string) =>
    this.releveNoteService.getBulletinData(matricule, this.idSemestre!).toPromise()
      .then(bulletin => this.genererPayloadEmail(bulletin.etudiant.email, bulletin))
  );

  Promise.all(requests).then(payloads => {
    this.releveNoteService.sendRelevesParEmail(payloads).subscribe({
      next: () => {
        this.success = 'Relevés envoyés par email avec succès.';
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.error = "Erreur lors de l'envoi des emails.";
        this.loading = false;
      }
    });
  }).catch(error => {
    console.error("Erreur dans la génération des bulletins :", error);
    this.error = "Erreur lors de la génération des bulletins.";
    this.loading = false;
  });
}

envoyerReleveIndividuelParEmail(): void {
  this.submitted = true;

  if (!this.matricule || !this.idSemestre) {
    this.error = 'Veuillez saisir le matricule et sélectionner le semestre.';
    return;
  }

  this.loading = true;
  this.error = '';
  this.success = '';

  this.releveNoteService.getBulletinData(this.matricule, this.idSemestre!).toPromise()
    .then(bulletin => {
      return this.genererPayloadEmail(bulletin.etudiant.email, bulletin).then(payload => {
        this.releveNoteService.sendRelevesParEmail([payload]).subscribe({
          next: () => {
            this.success = 'Relevé envoyé par email avec succès.';
            this.loading = false;
          },
          error: err => {
            console.error("Erreur lors de l'envoi de l'email :", err);
            this.error = "Erreur lors de l'envoi de l'email.";
            this.loading = false;
          }
        });
      });
    })
    .catch(err => {
      console.error("Erreur dans la génération du bulletin :", err);
      this.error = "Impossible de générer ou d'envoyer le relevé.";
      this.loading = false;
    });
}

private genererPayloadEmail(email: string, bulletin: any): Promise<{ email: string, pdfBase64: string }> {
  const notesParUE = this.organiserNotesParUE(bulletin.notes || []);
  const creditsValides = this.calculerCreditsValides(notesParUE);

  const releve: ReleveDeNotes = {
    etudiant: {
      matricule: bulletin.etudiant.matricule,
      nom: bulletin.etudiant.nom,
      prenom: bulletin.etudiant.prenom,
      dateNaissance: bulletin.etudiant.dateNaissance,
      specialite: bulletin.etudiant.departement?.intitule,
      dateInscription: bulletin.etudiant.dateInscription,
      email: bulletin.etudiant.email,
    },
    semestre: {
      semestre: bulletin.semestre.semestre,
      anneeUniversitaire: bulletin.semestre.annee?.toString()
    },
    notesParUE,
    semestreValide: creditsValides === this.calculerCreditsTotaux(bulletin.notes || []),
    creditsAccumules: creditsValides,
    creditsTotaux: this.calculerCreditsTotaux(bulletin.notes || []),
    moyenneGenerale: this.calculerMoyenneGenerale(bulletin.notes || []),
    dateSignature: new Date().toISOString()
  };

  return this.pdfService.generateReleveDeNotesPDF(releve, true).then(pdfBytes => {
    if (!pdfBytes) throw new Error("PDF non généré pour " + email);
    const base64 = btoa(String.fromCharCode(...new Uint8Array(pdfBytes)));
    return {
      email,
      pdfBase64: base64
    };
  });
}
// Méthodes pour effacer les alertes
clearError(): void {
  this.error = null;
}

clearSuccess(): void {
  this.success = null;
}
}