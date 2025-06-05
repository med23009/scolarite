import { Injectable } from "@angular/core"
import { PDFDocument, type PDFPage, type PDFFont, rgb, StandardFonts, type PDFImage } from "pdf-lib"
import type { ReleveDeNotes } from "../../models/ReleveDeNotes"
import type { NoteSemestrielle } from "../../models/note-semestrielle.model"

@Injectable({ providedIn: "root" })
export class PdfRelveNoteService {
  // Palette de couleurs minimaliste verte
  private primaryColor = rgb(0 / 255, 128 / 255, 96 / 255) // Vert teal foncé
  private secondaryColor = rgb(235 / 255, 248 / 255, 242 / 255) // Vert menthe très clair
  private whiteColor = rgb(1, 1, 1) // Blanc
  private successColor = rgb(0 / 255, 128 / 255, 96 / 255) // Vert pour validé
  private dangerColor = rgb(220 / 255, 53 / 255, 69 / 255) // Rouge pour non validé
  private textColor = rgb(33 / 255, 37 / 255, 41 / 255) // Noir pour le texte
  private textMutedColor = rgb(108 / 255, 117 / 255, 125 / 255) // Gris pour le texte secondaire
  private yellowColor = rgb(255 / 255, 193 / 255, 7 / 255) // Jaune pour soulignement

  private formatDate(timestamp: number | string | Date): string {
    if (!timestamp) return ""
    const date = new Date(timestamp)
    return date.toLocaleDateString("fr-FR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    })
  }

  private drawRectangle(page: PDFPage, x: number, y: number, width: number, height: number, color: any): void {
    page.drawRectangle({ x, y, width, height, color })
  }

  async generateReleveDeNotesPDF(releveDeNotes: ReleveDeNotes, returnBytes = false): Promise<void | Uint8Array> {
    const pdfDoc = await PDFDocument.create()
    const page = pdfDoc.addPage([595, 842]) // A4 size

    const fontRegular = await pdfDoc.embedFont(StandardFonts.Helvetica)
    const fontBold = await pdfDoc.embedFont(StandardFonts.HelveticaBold)

    const logoUrl = "/images/logo_esp.png"
    const logoResponse = await fetch(logoUrl)
    const logoImage = await pdfDoc.embedPng(await logoResponse.arrayBuffer())

    const { width } = page.getSize()
    const margin = 36 // Marges standard
    let y = 800 // Position de départ en haut de la page

    // Fond blanc pour toute la page
    this.drawRectangle(page, 0, 0, width, 842, this.whiteColor)

    // En-tête avec logo et titre
    y = this.drawHeader(page, y, fontBold, fontRegular, width, margin, logoImage)

    // Informations de l'étudiant
    y = this.drawStudentInfo(page, releveDeNotes, y, fontBold, fontRegular, width, margin)

    // Tableau des notes - design minimaliste
    y = this.drawNotesTable(page, releveDeNotes, y, fontBold, fontRegular, width, margin)

    // Section de validation
    y = this.drawValidation(page, releveDeNotes, y, fontBold, fontRegular, width, margin)

    // Pied de page
    this.drawFooter(page, width, margin, fontRegular)

    const pdfBytes = await pdfDoc.save()
    if (returnBytes) return pdfBytes

    const blob = new Blob([pdfBytes], { type: "application/pdf" })
    const link = document.createElement("a")
    link.href = window.URL.createObjectURL(blob)
    link.download = "releve_de_notes.pdf"
    link.click()
  }

  private drawHeader(
    page: PDFPage,
    y: number,
    fontBold: PDFFont,
    fontRegular: PDFFont,
    width: number,
    margin: number,
    logoImage: PDFImage,
  ): number {
    // Fond vert clair pour l'en-tête
    this.drawRectangle(page, margin, y - 40, width - 2 * margin, 40, this.secondaryColor)

    // Logo - TAILLE AUGMENTÉE
    page.drawImage(logoImage, {
      x: margin + 10,
      y: y - 35,
      width: 30, // Augmenté de 20 à 30
      height: 30, // Augmenté de 20 à 30
    })

    // Titre de l'école avec soulignement jaune
    page.drawText("École Supérieure Polytechnique", {
      x: margin + 50, // Ajusté pour le logo plus grand
      y: y - 18,
      size: 12,
      font: fontBold,
      color: this.primaryColor,
    })

    // Soulignement jaune sous le titre de l'école
    this.drawRectangle(page, margin + 50, y - 20, 180, 1.5, this.yellowColor)

    // Sous-titre
    page.drawText("Année universitaire", {
      x: margin + 50, // Ajusté pour le logo plus grand
      y: y - 30,
      size: 8,
      font: fontRegular,
      color: this.textMutedColor,
    })

    // Titre du document avec soulignement jaune
    page.drawText("RELEVÉ DE NOTES", {
      x: width - margin - 120,
      y: y - 18,
      size: 12,
      font: fontBold,
      color: this.textColor,
    })

    // Soulignement jaune sous le titre du document
    this.drawRectangle(page, width - margin - 120, y - 20, 110, 1.5, this.yellowColor)

    // Sous-titre du document
    page.drawText("Document officiel", {
      x: width - margin - 120,
      y: y - 30,
      size: 8,
      font: fontRegular,
      color: this.textMutedColor,
    })

    return y - 50
  }

  private drawStudentInfo(
    page: PDFPage,
    data: ReleveDeNotes,
    y: number,
    fontBold: PDFFont,
    fontRegular: PDFFont,
    width: number,
    margin: number,
  ): number {
    const colWidth = (width - 2 * margin) / 3

    // Première ligne d'informations
    // Nom de Famille
    page.drawText("Nom de Famille", {
      x: margin,
      y: y,
      size: 8,
      font: fontRegular,
      color: this.textMutedColor,
    })
    page.drawText(data.etudiant.nom || "", {
      x: margin,
      y: y - 15,
      size: 10,
      font: fontBold,
      color: this.textColor,
    })

    // Prénom du Père
    page.drawText("Prénom du Père", {
      x: margin + colWidth,
      y: y,
      size: 8,
      font: fontRegular,
      color: this.textMutedColor,
    })
    page.drawText(data.etudiant.prenom || "", {
      x: margin + colWidth,
      y: y - 15,
      size: 10,
      font: fontBold,
      color: this.textColor,
    })

    // Matricule
    page.drawText("Matricule", {
      x: margin + 2 * colWidth,
      y: y,
      size: 8,
      font: fontRegular,
      color: this.textMutedColor,
    })
    page.drawText(data.etudiant.matricule || "", {
      x: margin + 2 * colWidth,
      y: y - 15,
      size: 10,
      font: fontBold,
      color: this.textColor,
    })

    y -= 40

    // Deuxième ligne d'informations
    // Spécialité
    page.drawText("Spécialité", {
      x: margin,
      y: y,
      size: 8,
      font: fontRegular,
      color: this.textMutedColor,
    })
    page.drawText(data.etudiant.departement?.intitule || "Non définie", {
      x: margin,
      y: y - 15,
      size: 10,
      font: fontBold,
      color: this.textColor,
    })

    // Semestre
    page.drawText("Semestre", {
      x: margin + colWidth,
      y: y,
      size: 8,
      font: fontRegular,
      color: this.textMutedColor,
    })
    page.drawText(data.semestre?.semestre || "Non défini", {
      x: margin + colWidth,
      y: y - 15,
      size: 10,
      font: fontBold,
      color: this.textColor,
    })

    // Email
    page.drawText("Email", {
      x: margin + 2 * colWidth,
      y: y,
      size: 8,
      font: fontRegular,
      color: this.textMutedColor,
    })
    page.drawText(data.etudiant.email || "", {
      x: margin + 2 * colWidth,
      y: y - 15,
      size: 10,
      font: fontRegular,
      color: this.textColor,
    })

    return y - 30
  }

  private drawTableHeader(page: PDFPage, y: number, fontBold: PDFFont, width: number, margin: number): number {
    // En-tête du tableau avec fond vert clair
    this.drawRectangle(page, margin, y - 20, width - 2 * margin, 20, this.secondaryColor)

    const headers = ["Code", "Élément Module", "Crédit", "Note", "Statut"]
    const colWidths = [60, width - 2 * margin - 240, 40, 40, 100]
    let xPos = margin + 10

    headers.forEach((header, index) => {
      page.drawText(header, {
        x: xPos,
        y: y - 13,
        size: 9,
        font: fontBold,
        color: this.textColor,
      })
      xPos += colWidths[index]
    })

    return y - 20
  }

  private drawNotesTable(
    page: PDFPage,
    data: ReleveDeNotes,
    y: number,
    fontBold: PDFFont,
    fontRegular: PDFFont,
    width: number,
    margin: number,
  ): number {
    const rowHeight = 15
    const colWidths = [60, width - 2 * margin - 240, 40, 40, 100]

    // Fonction pour déterminer la priorité des UE
    const getUEPriority = (ueName: string): number => {
      // Vérifier si l'UE contient "HE" ou est liée au développement personnel/entrepreneuriat
      if (
        ueName.includes("HE") ||
        ueName.includes("Développement Personnel") ||
        ueName.includes("Entreprenariat") ||
        ueName.includes("UE01") ||
        ueName.includes("UE02") ||
        ueName.includes("UE06")
      ) {
        return 0 // Priorité la plus haute
      }

      // Vérifier si l'UE contient "ST" ou est liée aux sciences/théories
      if (ueName.includes("ST") || ueName.includes("UE03") || ueName.includes("UE04") || ueName.includes("UE88")) {
        return 1 // Priorité moyenne
      }

      // Toutes les autres UE (spécialités)
      return 2 // Priorité la plus basse
    }

    // Trier les UE de manière stricte et cohérente
    const sortedUEs = Object.entries(data.notesParUE).sort(([ue1Name, _], [ue2Name, __]) => {
      const priority1 = getUEPriority(ue1Name)
      const priority2 = getUEPriority(ue2Name)

      // Si les priorités sont différentes, trier par priorité
      if (priority1 !== priority2) {
        return priority1 - priority2
      }

      // Si les priorités sont identiques, trier par nom d'UE pour garantir un ordre constant
      return ue1Name.localeCompare(ue2Name)
    })

    for (const [ue, notes] of sortedUEs) {
      // Fonction pour déterminer la priorité des éléments de module
      const getElementPriority = (code: string): number => {
        if (code.startsWith("HE")) return 0 // HE en premier
        if (code.startsWith("ST")) return 1 // ST en deuxième
        return 2 // Autres spécialités en dernier
      }

      // Trier les éléments de module de manière stricte et cohérente
      const sortedNotes = (notes as NoteSemestrielle[]).sort((a, b) => {
        const priorityA = getElementPriority(a.codeEM)
        const priorityB = getElementPriority(b.codeEM)

        // Si les priorités sont différentes, trier par priorité
        if (priorityA !== priorityB) {
          return priorityA - priorityB
        }

        // Si les priorités sont identiques, trier par code pour garantir un ordre constant
        return a.codeEM.localeCompare(b.codeEM)
      })

      // Titre de l'UE avec soulignement jaune
      this.drawRectangle(page, margin, y - 20, width - 2 * margin, 20, this.secondaryColor)
      page.drawText(`UE: ${ue}`, {
        x: margin + 10,
        y: y - 13,
        size: 9,
        font: fontBold,
        color: this.primaryColor,
      })

      // Soulignement jaune sous le titre de l'UE
      const ueTextWidth = fontBold.widthOfTextAtSize(`UE: ${ue}`, 9)
      this.drawRectangle(page, margin + 10, y - 15, ueTextWidth, 1, this.yellowColor)

      y -= 20

      // Affichage des éléments triés
      sortedNotes.forEach((note) => {
        this.drawRectangle(page, margin, y - rowHeight, width - 2 * margin, rowHeight, this.whiteColor)
        this.drawRectangle(page, margin, y - rowHeight, width - 2 * margin, 0.5, rgb(0.95, 0.95, 0.95))

        let xPos = margin + 10

        page.drawText(note.codeEM, {
          x: xPos,
          y: y - 10,
          size: 8,
          font: fontRegular,
          color: this.textColor,
        })
        xPos += colWidths[0]

        page.drawText(note.elementModule?.intitule || "N/A", {
          x: xPos,
          y: y - 10,
          size: 8,
          font: fontRegular,
          color: this.textColor,
        })
        xPos += colWidths[1]

        page.drawText((note.credit || 0).toString(), {
          x: xPos,
          y: y - 10,
          size: 8,
          font: fontRegular,
          color: this.textColor,
        })
        xPos += colWidths[2]

        const noteTxt = (note.noteGenerale ?? 0).toFixed(2)
        page.drawText(noteTxt, {
          x: xPos,
          y: y - 10,
          size: 8,
          font: fontRegular,
          color: this.textColor,
        })
        xPos += colWidths[3]

        const statut = (note.noteGenerale ?? 0) >= 10 ? "Validé" : "Non Validé"
        const statusColor = (note.noteGenerale ?? 0) >= 10 ? this.successColor : this.dangerColor

        this.drawRectangle(page, xPos, y - 12, 50, 10, statusColor)
        page.drawText(statut, {
          x: xPos + 5,
          y: y - 10,
          size: 7,
          font: fontBold,
          color: this.whiteColor,
        })

        y -= rowHeight
      })

      // Augmentation de l'espace entre les unités d'enseignement
      y -= 15 // Augmenté de 5 à 15 pour plus d'espace entre les UE
    }

    return y
  }

  private drawValidation(
    page: PDFPage,
    data: ReleveDeNotes,
    y: number,
    fontBold: PDFFont,
    fontRegular: PDFFont,
    width: number,
    margin: number,
  ): number {
    // Titre VALIDATION avec fond vert clair et soulignement jaune
    this.drawRectangle(page, margin, y - 20, width - 2 * margin, 20, this.secondaryColor)

    page.drawText("VALIDATION", {
      x: margin + 10,
      y: y - 13,
      size: 10,
      font: fontBold,
      color: this.primaryColor,
    })

    // Soulignement jaune sous le titre VALIDATION
    this.drawRectangle(page, margin + 10, y - 15, 80, 1.5, this.yellowColor)

    y -= 30

    // Badge de validation du semestre
    const badgeText = data.semestreValide ? "SEMESTRE VALIDÉ" : "SEMESTRE NON VALIDÉ"
    const badgeColor = data.semestreValide ? this.successColor : this.dangerColor
    const badgeWidth = 150
    const badgeHeight = 20

    this.drawRectangle(page, margin, y - badgeHeight, badgeWidth, badgeHeight, badgeColor)

    // Texte dans le badge
    page.drawText(badgeText, {
      x: margin + 10,
      y: y - 13,
      size: 9,
      font: fontBold,
      color: this.whiteColor,
    })

    // Semestre concerné
    page.drawText(`Semestre concerné : ${data.semestre?.semestre || "Non précisé"}`, {
      x: margin,
      y: y - 35,
      size: 9,
      font: fontBold,
      color: this.textColor,
    })

    // Informations sur les crédits
    page.drawText(`Crédits obtenus : ${data.creditsAccumules}/${data.creditsTotaux}`, {
      x: margin,
      y: y - 50,
      size: 9,
      font: fontBold,
      color: this.textColor,
    })

    // Moyenne générale
    page.drawText(`Moyenne générale : ${data.moyenneGenerale?.toFixed(2) || "N/A"}`, {
      x: margin,
      y: y - 65,
      size: 9,
      font: fontBold,
      color: this.textColor,
    })

    // Signature
    const signatureX = width - margin - 200
    page.drawText("Le Directeur des Études", {
      x: signatureX,
      y: y - 15,
      size: 9,
      font: fontRegular,
      color: this.textColor,
    })

    // Ligne de signature
    this.drawRectangle(page, signatureX, y - 45, 150, 1, this.primaryColor)

    // Date
    const formattedDate = this.formatDate(data.dateSignature || new Date())
    page.drawText(`FAIT À NOUAKCHOTT, LE ${formattedDate}`, {
      x: width - margin - 200,
      y: y - 60,
      size: 7,
      font: fontRegular,
      color: this.textMutedColor,
    })

    return y - 80
  }

  private drawFooter(page: PDFPage, width: number, margin: number, font: PDFFont): void {
    // Barre verte en bas de page
    this.drawRectangle(page, margin, margin, width - 2 * margin, 20, this.primaryColor)

    // Texte d'avertissement
    const footerText = "CE DOCUMENT N'EST PAS VALABLE SANS SIGNATURE"
    const textWidth = font.widthOfTextAtSize(footerText, 8)

    page.drawText(footerText, {
      x: (width - textWidth) / 2,
      y: margin + 7,
      size: 8,
      font,
      color: this.whiteColor,
    })

    // Numéro de page
    page.drawText("Page 1/1", {
      x: width - margin - 30,
      y: margin + 7,
      size: 7,
      font,
      color: this.whiteColor,
    })
  }
}
