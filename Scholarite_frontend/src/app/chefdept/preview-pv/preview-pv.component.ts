import { Component, type OnInit, ViewChild, type ElementRef } from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormsModule } from "@angular/forms"
import * as XLSX from "xlsx"

@Component({
  selector: "app-preview-pv",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./preview-pv.component.html",
  styleUrls: ["./preview-pv.component.scss"],
})
export class PreviewPvComponent implements OnInit {
  @ViewChild("excelContainer") excelContainer!: ElementRef

  excelData: any[][] = []
  departement = ""
  semestre = ""
  isLoading = false
  excelFile: ArrayBuffer | null = null

  ngOnInit(): void {
    const data = localStorage.getItem("previewPvData")
    this.departement = localStorage.getItem("previewPvDepartement") || ""
    this.semestre = localStorage.getItem("previewPvSemestre") || ""
    const fileData = localStorage.getItem("previewPvFile")

    if (data) {
      this.excelData = JSON.parse(data)
    }

    if (fileData) {
      this.loadExcelFromBase64(fileData)
    }
  }

  loadExcelFromBase64(base64String: string): void {
    this.isLoading = true
    const base64 = base64String.split(",")[1]
    const binaryString = window.atob(base64)
    const bytes = new Uint8Array(binaryString.length)
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i)
    }
    this.excelFile = bytes.buffer

    setTimeout(() => {
      this.renderExcel()
      this.isLoading = false
    }, 100)
  }

  renderExcel(): void {
    if (!this.excelFile || !this.excelContainer) return

    const workbook = XLSX.read(this.excelFile, { type: "array" })
    const worksheet = workbook.Sheets[workbook.SheetNames[0]]
    const html = XLSX.utils.sheet_to_html(worksheet, { editable: false })

    this.excelContainer.nativeElement.innerHTML = html
    this.applyExcelStyles()
  }

  applyExcelStyles(): void {
    const style = document.createElement("style")
    style.textContent = `
      .excel-viewer table {
        border-collapse: collapse;
        width: 100%;
        font-family: 'Segoe UI', Arial, sans-serif;
      }
      .excel-viewer td, .excel-viewer th {
        border: 1px solid #dee2e6;
        padding: 8px;
        text-align: center;
      }
      .excel-viewer tr:nth-child(even) {
        background-color: #f8f9fa;
      }
      .excel-viewer tr:first-child {
        background-color: #e9ecef;
        font-weight: bold;
      }
      .excel-viewer tr:nth-child(2) td {
        background-color: #cfe2ff;
        font-weight: bold;
      }
      .excel-viewer tr:nth-child(3) td:first-child {
        background-color: #fff3cd;
      }
      .total-credits-30 {
        background-color: #c6efce !important; /* Green background for total credits = 30 */
        font-weight: bold;
      }
    `
    document.head.appendChild(style)
    this.excelContainer.nativeElement.classList.add("excel-viewer")

    // 🔥 Coloration des cellules selon leur contenu
    const cells = this.excelContainer.nativeElement.querySelectorAll("td")
    cells.forEach((cell: HTMLElement) => {
      const text = cell.textContent?.trim()
      if (text === "VCI") {
        cell.style.backgroundColor = "#c6efce" // vert
      } else if (text === "VCE") {
        cell.style.backgroundColor = "#fff2cc" // jaune
      } else if (text === "E") {
        cell.style.backgroundColor = "#f4cccc" // rouge foncé
      } else if (text === "NV") {
        cell.style.backgroundColor = "#f8cbad" // rouge clair
      }
    })

    // Trouver et colorer la cellule de total des crédits si elle est égale à 30
    this.highlightTotalCredits()
  }

  highlightTotalCredits(): void {
    // Find the column that contains "Crédits" in the header
    const table = this.excelContainer.nativeElement.querySelector("table")
    if (!table) return

    // First, try to find the credits column by header text
    let creditsColumnIndex = -1
    const headerRow = table.querySelector("tr")

    if (headerRow) {
      const headerCells = headerRow.querySelectorAll("th, td")
      headerCells.forEach((cell: HTMLElement, index: number) => {
        const text = cell.textContent?.trim().toLowerCase() || ""
        if (text.includes("crédit") || text === "ects" || text === "crédits") {
          creditsColumnIndex = index
        }
      })
    }

    // If we couldn't find by header, look for a column with multiple "30" values
    if (creditsColumnIndex === -1) {
      const rows = table.querySelectorAll("tr")
      const columnCounts: { [key: number]: { count: number; thirtyCount: number } } = {}

      // Count occurrences of "30" in each column
      rows.forEach((row: HTMLElement) => {
        const cells = row.querySelectorAll("td")
        cells.forEach((cell: HTMLElement, index: number) => {
          if (!columnCounts[index]) {
            columnCounts[index] = { count: 0, thirtyCount: 0 }
          }

          const cellText = cell.textContent?.trim()
          if (cellText) {
            columnCounts[index].count++
            if (cellText === "30") {
              columnCounts[index].thirtyCount++
            }
          }
        })
      })

      // Find the column with the highest count of "30" values
      let maxThirtyCount = 0
      Object.keys(columnCounts).forEach((colIndex) => {
        const index = Number.parseInt(colIndex)
        if (columnCounts[index].thirtyCount > maxThirtyCount) {
          maxThirtyCount = columnCounts[index].thirtyCount
          creditsColumnIndex = index
        }
      })
    }

    // Now highlight all cells with "30" in the identified credits column
    if (creditsColumnIndex !== -1) {
      const rows = table.querySelectorAll("tr")
      rows.forEach((row: HTMLElement, rowIndex: number) => {
        // Skip header row
        if (rowIndex === 0) return

        const cells = row.querySelectorAll("td")
        if (cells.length > creditsColumnIndex) {
          const cell = cells[creditsColumnIndex]
          const cellText = cell.textContent?.trim()

          // If the cell value is exactly "30", apply the green background
          if (cellText === "30") {
            cell.classList.add("total-credits-30")
          }
        }
      })
    } else {
      // Fallback: just highlight any cell with exactly "30"
      const cells = this.excelContainer.nativeElement.querySelectorAll("td")
      cells.forEach((cell: HTMLElement) => {
        const cellText = cell.textContent?.trim()
        if (cellText === "30") {
          cell.classList.add("total-credits-30")
        }
      })
    }
  }

  downloadPv(): void {
    const fileData = localStorage.getItem("previewPvFile")
    const semestre = this.semestre || "Semestre"
    const departement = this.departement || "Departement"

    if (fileData) {
      const a = document.createElement("a")
      a.href = fileData
      const fileName = `PV-${semestre.replace(/\s+/g, "")}-${departement.replace(/\s+/g, "")}.xlsx`
      a.download = fileName
      a.click()
    }
  }
}
