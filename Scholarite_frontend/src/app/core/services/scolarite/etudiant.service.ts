import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Etudiant } from '../../models/etudiant.model';
import { environment } from '../../../../environments/environment';
import { jsPDF } from 'jspdf';

@Injectable({
  providedIn: 'root'
})
export class EtudiantService {
  private readonly API_URL = `${environment.apiUrl}/api/etudiants`;

  constructor(private http: HttpClient) {}

  getAllEtudiants(promotion?: string): Observable<Etudiant[]> {
    let params = new HttpParams();
    if (promotion) {
      params = params.set('promotion', promotion);
    }
    return this.http.get<Etudiant[]>(this.API_URL, { params });
  }

  getEtudiantById(id: number): Observable<Etudiant> {
    return this.http.get<Etudiant>(`${this.API_URL}/${id}`);
  }

  createEtudiant(etudiant: Etudiant): Observable<Etudiant> {
    return this.http.post<Etudiant>(this.API_URL, etudiant);
  }

  updateEtudiant(id: number, etudiant: Etudiant): Observable<Etudiant> {
    return this.http.put<Etudiant>(`${this.API_URL}/${id}`, etudiant);
  }

  deleteEtudiant(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  importEtudiantsFromCSV(file: File, promotion?: string): Observable<Etudiant[]> {
    const formData = new FormData();
    formData.append('file', file);

    // Ajouter la promotion au formData si elle est fournie
    if (promotion) {
      formData.append('promotion', promotion);
    }

    return this.http.post<Etudiant[]>(`${this.API_URL}/import`, formData);
  }

  exportEtudiantAttestation(id: number): Observable<Blob> {
    const headers = new HttpHeaders({
      'Accept': 'application/pdf'
    });

    return this.http.get(`${this.API_URL}/${id}/attestation`, {
      headers: headers,
      responseType: 'blob'
    });
  }

  exportAttestation(etudiant: Etudiant): void {
    // Créer un nouveau document PDF (format A4)
    const doc = new jsPDF('p', 'mm', 'a4');
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 15; // Marge uniforme sur les côtés

    // Couleurs inspirées du logo (estimation RGB)
    const greenColor = [56, 142, 60]; // Dark Green
    const yellowColor = [253, 216, 53]; // Amber
    const blackColor = [0, 0, 0]; // Noir
    const titleColor = greenColor; // Couleur pour le titre

    // Position Y courante pour le placement des éléments
    let currentY = margin;

    // Charger le logo depuis le backend
    const img = new Image();
    // Chemin pour accéder aux fichiers statiques (assurez-vous que votre backend le sert ainsi)
    img.src = '/images/logo_esp.png';

    img.onload = () => {
      // --- En-tête avec Logo et Infos École ---
      const logoWidth = 40;
      const logoHeight = (logoWidth / img.width) * img.height; // Calculer la hauteur pour maintenir le ratio
      const logoX = margin;
      const logoY = currentY; // Positionner en haut, à gauche de la marge

      // Ajouter le logo
      doc.addImage(img, 'PNG', logoX, logoY, logoWidth, logoHeight);

      // Informations de l'école à droite de l'en-tête
      // Style pour le nom de l'école
      doc.setFontSize(12); // Rendre un peu plus grand
      doc.setTextColor(greenColor[0], greenColor[1], greenColor[2]); // Couleur verte
      doc.setFont('helvetica', 'bold'); // Mettre en gras

      const schoolName = 'Ecole Supérieure Polytechnique';
      const schoolInfoX = pageWidth - margin; // Aligner à droite
      let schoolInfoY = currentY + 3; // Positionner légèrement en dessous du haut

      doc.text(schoolName, schoolInfoX, schoolInfoY, { align: 'right' });

      // Style pour les autres informations de l'école
      doc.setFontSize(10); // Revenir à la taille normale
      doc.setTextColor(blackColor[0], blackColor[1], blackColor[2]); // Revenir au noir
      doc.setFont('helvetica', 'normal'); // Revenir au normal

      const otherSchoolInfo = [
        'NOUAKCHOTT',
        'Téléphone : +222 45 29 63 84',
        'Email : contact@esp-nouakchott.mr',
        'Site web : www.esp.mr'
      ];

      otherSchoolInfo.forEach(line => {
        schoolInfoY += 5; // Espacement entre les lignes d'information
        doc.text(line, schoolInfoX, schoolInfoY, { align: 'right' });
      });

      // Mettre à jour la position Y après l'en-tête
      currentY = Math.max(logoY + logoHeight, schoolInfoY) + 15; // Descendre après le plus bas des deux éléments de l'en-tête

      // --- Titre de l'attestation ---
      doc.setFontSize(18);
      doc.setTextColor(titleColor[0], titleColor[1], titleColor[2]);
      doc.setFont('helvetica', 'bold'); // Utilisation de Helvetica en gras
      const titleText = "ATTESTATION D'INSCRIPTION";
      const titleX = pageWidth / 2; // Centrer horizontalement
      const titleY = currentY;
      doc.text(titleText, titleX, titleY, { align: 'center' });

      // Ligne décorative sous le titre (couleur jaune)
      doc.setDrawColor(yellowColor[0], yellowColor[1], yellowColor[2]);
      doc.setLineWidth(0.5);
      doc.line(margin, titleY + 4, pageWidth - margin, titleY + 4); // Ligne horizontale légèrement sous le texte

      currentY = titleY + 10; // Mettre à jour la position Y après le titre et la ligne

      // --- Informations de l'Étudiant (en une seule colonne) ---
      doc.setFontSize(12);
      doc.setTextColor(blackColor[0], blackColor[1], blackColor[2]);
      doc.setFont('helvetica', 'normal'); // Utilisation de Helvetica

      currentY += 10; // Espace avant les infos étudiant

      const studentInfoTitle = "Informations de l'Étudiant :";
      doc.setFont('helvetica', 'bold'); // Titre en gras
      doc.text(studentInfoTitle, margin, currentY);
      currentY += 7;
      doc.setFont('helvetica', 'normal'); // Revenir au normal pour les détails

      const dateNaissance = etudiant.dateNaissance ? new Date(etudiant.dateNaissance).toLocaleDateString('fr-FR') : 'Non spécifiée';
      const dateInscription = etudiant.dateInscription ? new Date(etudiant.dateInscription).toLocaleDateString('fr-FR') : 'Non spécifiée';

      const studentDetails = [
        `Nom : ${etudiant.nom}`,
        `Prénom : ${etudiant.prenom}`,
        `Matricule : ${etudiant.matricule}`,
        `Date de Naissance : ${dateNaissance}`,
        `Lieu de Naissance : ${etudiant.lieuNaissance || 'Non spécifié'}`,
        `Département : ${etudiant.departement?.intitule || 'Non spécifié'}`,
        `Promotion : ${etudiant.promotion || 'Non spécifiée'}`,
        `Date d'Inscription : ${dateInscription}`,
        `Email : ${etudiant.email || 'Non spécifié'}`,
        `Téléphone : ${etudiant.telephoneEtudiant || 'Non spécifié'}`
      ];

       const detailsX = margin + 5; // Indentation pour les détails
       const maxWidthDetails = pageWidth - 2 * margin - 5; // Largeur disponible pour chaque ligne de détail

      studentDetails.forEach(detail => {
          // Utiliser splitTextToSize pour chaque détail individuellement si nécessaire
          const detailLines = doc.splitTextToSize(detail, maxWidthDetails);
          doc.text(detailLines, detailsX, currentY);
          currentY += detailLines.length * (doc.getFontSize() / doc.internal.scaleFactor * 1.5); // Augmenter l'espacement ici aussi
      });

      currentY += 10; // Espace après les infos étudiant

      // --- Corps Principal du Texte ---
      const anneeActuelle = new Date().getFullYear();
      const anneeAcademique = `${anneeActuelle}/${anneeActuelle + 1}`;

       // Texte principal mis à jour avec les informations spécifiques de l'étudiant, le département et le bon responsable
       const mainText = `
Je soussigné MOHAMED, responsable de scolarite à l'École Supérieure Polytechnique de Nouakchott, certifie que l'étudiant ${etudiant.prenom} ${etudiant.nom}, matricule ${etudiant.matricule}, inscrit en ${etudiant.departement?.intitule || 'Non spécifié'}, est régulièrement inscrit pour l'année académique ${anneeAcademique} dans notre établissement.
Cette attestation lui est délivrée pour servir et valoir ce que de droit.
`;
      doc.setFontSize(12);
      doc.setFont('helvetica', 'normal'); // Utilisation de Helvetica
      // splitTextToSize retourne un tableau de lignes
      const textLines = doc.splitTextToSize(mainText.trim(), pageWidth - 2 * margin);
      // Positionner le texte sans justification, avec interligne augmenté
      doc.text(textLines, margin, currentY, { lineHeightFactor: 1.5 }); // Augmenter l'interligne

      // Calculer la position Y après le corps du texte en se basant sur le nombre de lignes et une estimation de l'interligne
      const estimatedLineHeight = doc.getFontSize() / doc.internal.scaleFactor * 1.5; // Utiliser le même facteur d'interligne
      const textBlockHeight = textLines.length * estimatedLineHeight;
      currentY += textBlockHeight + 10; // Ajouter un espace après le bloc de texte

      // --- Lieu et Date de l'attestation ---
      const currentDate = new Date().toLocaleDateString('fr-FR');
      const footerText = `Fait à Nouakchott, le ${currentDate}.`;
      doc.text(footerText, margin, currentY);

      // --- Signature (positionnée en bas à droite) ---
      const signatureLabel = 'Signature et Cachet';
      const signatureWidth = doc.getTextWidth(signatureLabel);
      const signatureX = pageWidth - margin - signatureWidth; // Aligner à droite
      const signatureY = pageHeight - margin - 25; // Positionner vers le bas, avec plus d'espace

      // Ligne pour la signature
      doc.line(signatureX, signatureY - 5, pageWidth - margin, signatureY - 5); // Positionner la ligne plus haut

      doc.setFont('helvetica', 'bold'); // Utilisation de Helvetica en gras
      doc.text(signatureLabel, signatureX, signatureY);

      // Sauvegarde du fichier PDF
      doc.save(`attestation_inscription_${etudiant.matricule}.pdf`);
    };

    img.onerror = () => {
      console.error('Erreur de chargement du logo. Génération du PDF sans logo.');

      // --- Génération sans logo (version simplifiée mais structurée) ---
      let errorY = margin;

       // Informations de l'école en haut à droite
       // Style pour le nom de l'école
       doc.setFontSize(12); // Rendre un peu plus grand
       doc.setTextColor(greenColor[0], greenColor[1], greenColor[2]); // Couleur verte
       doc.setFont('helvetica', 'bold'); // Mettre en gras

       const schoolName = 'Ecole Supérieure Polytechnique';
       const schoolInfoX = pageWidth - margin;
       let schoolInfoY = errorY + 3;

       doc.text(schoolName, schoolInfoX, schoolInfoY, { align: 'right' });

       // Style pour les autres informations de l'école
       doc.setFontSize(10); // Revenir à la taille normale
       doc.setTextColor(blackColor[0], blackColor[1], blackColor[2]); // Revenir au noir
       doc.setFont('helvetica', 'normal'); // Revenir au normal

       const otherSchoolInfo = [
           'NOUAKCHOTT',
           'Téléphone : +222 45 29 63 84',
           'Email : contact@esp-nouakchott.mr',
           'Site web : www.esp.mr'
       ];

        otherSchoolInfo.forEach(line => {
            schoolInfoY += 5;
            doc.text(line, schoolInfoX, schoolInfoY, { align: 'right' });
        });
       errorY = schoolInfoY + 10;

      // Titre de l'attestation
      doc.setFontSize(18);
      doc.setTextColor(titleColor[0], titleColor[1], titleColor[2]);
      doc.setFont('helvetica', 'bold'); // Utilisation de Helvetica en gras
      const titleText = "ATTESTATION D'INSCRIPTION";
      const titleX = pageWidth / 2;
      const titleY = errorY;
      doc.text(titleText, titleX, titleY, { align: 'center' });

      // Ligne décorative sous le titre
      doc.setDrawColor(yellowColor[0], yellowColor[1], yellowColor[2]);
      doc.setLineWidth(0.5);
      doc.line(margin, titleY + 4, pageWidth - margin, titleY + 4);
      errorY = titleY + 10;

      // Informations de l'Étudiant (en une seule colonne)
      doc.setFontSize(12);
      doc.setTextColor(blackColor[0], blackColor[1], blackColor[2]);
      doc.setFont('helvetica', 'normal'); // Utilisation de Helvetica

      errorY += 10;

      const dateNaissance = etudiant.dateNaissance ? new Date(etudiant.dateNaissance).toLocaleDateString('fr-FR') : 'Non spécifiée';
      const dateInscription = etudiant.dateInscription ? new Date(etudiant.dateInscription).toLocaleDateString('fr-FR') : 'Non spécifiée';


      const studentInfoTitle = "Informations de l'Étudiant :";
      doc.setFont('helvetica', 'bold'); // Titre en gras
      doc.text(studentInfoTitle, margin, errorY);
      errorY += 7;
      doc.setFont('helvetica', 'normal'); // Revenir au normal pour les détails

      const studentDetails = [
        `Nom : ${etudiant.nom}`,
        `Prénom : ${etudiant.prenom}`,
        `Matricule : ${etudiant.matricule}`,
        `Date de Naissance : ${dateNaissance}`,
        `Lieu de Naissance : ${etudiant.lieuNaissance || 'Non spécifié'}`,
        `Département : ${etudiant.departement?.intitule || 'Non spécifié'}`,
        `Promotion : ${etudiant.promotion || 'Non spécifiée'}`,
        `Date d'Inscription : ${dateInscription}`,
        `Email : ${etudiant.email || 'Non spécifié'}`,
        `Téléphone : ${etudiant.telephoneEtudiant || 'Non spécifié'}`
      ];

       const detailsX = margin + 5; // Indentation pour les détails
       const maxWidthDetails = pageWidth - 2 * margin - 5; // Largeur disponible pour chaque ligne de détail

      studentDetails.forEach(detail => {
          // Utiliser splitTextToSize pour chaque détail individuellement si nécessaire
          const detailLines = doc.splitTextToSize(detail, maxWidthDetails);
          doc.text(detailLines, detailsX, errorY);
          errorY += detailLines.length * (doc.getFontSize() / doc.internal.scaleFactor * 1.5); // Augmenter l'espacement ici aussi
      });


      errorY += 10;


      // Corps Principal du Texte
      const anneeActuelle = new Date().getFullYear();
      const anneeAcademique = `${anneeActuelle}/${anneeActuelle + 1}`;

       const mainText = `
Je soussigné MOHAMED, responsable de scolarite à l'École Supérieure Polytechnique de Nouakchott, certifie que l'étudiant ${etudiant.prenom} ${etudiant.nom}, matricule ${etudiant.matricule}, inscrit en ${etudiant.departement?.intitule || 'Non spécifié'}, est régulièrement inscrit pour l'année académique ${anneeAcademique} dans notre établissement.
Cette attestation lui est délivrée pour servir et valoir ce que de droit.
`;
      doc.setFontSize(12);
      doc.setFont('helvetica', 'normal'); // Utilisation de Helvetica
      const textLines = doc.splitTextToSize(mainText.trim(), pageWidth - 2 * margin);
      // Positionner le texte sans justification, avec interligne augmenté
      doc.text(textLines, margin, errorY, { lineHeightFactor: 1.5 }); // Augmenter l'interligne


      const estimatedLineHeight = doc.getFontSize() / doc.internal.scaleFactor * 1.5; // Estimer la hauteur d'une ligne avec interligne
      const textBlockHeight = textLines.length * estimatedLineHeight;
      errorY += textBlockHeight + 10; // Ajouter un espace après le bloc de texte


      // Lieu et Date de l'attestation
      const currentDate = new Date().toLocaleDateString('fr-FR');
      const footerText = `Fait à Nouakchott, le ${currentDate}.`;
      doc.text(footerText, margin, errorY);

      // Signature
      const signatureLabel = 'Signature et Cachet';
      const signatureWidth = doc.getTextWidth(signatureLabel);
      const signatureX = pageWidth - margin - signatureWidth;
      const signatureY = pageHeight - margin - 25; // Positionner vers le bas, avec plus d'espace

       doc.line(signatureX, signatureY - 5, pageWidth - margin, signatureY - 5); // Positionner la ligne plus haut

      doc.setFont('helvetica', 'bold'); // Utilisation de Helvetica en gras
      doc.text(signatureLabel, signatureX, signatureY);


      doc.save(`attestation_inscription_${etudiant.matricule}.pdf`);
    };
  }
}