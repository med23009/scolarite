package com.esp.scolarite.Controller.chefdept;

import com.esp.scolarite.Service.UserService;
import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.repository.SemestreRepository;
import com.esp.scolarite.Service.PVExportService;
import com.esp.scolarite.util.SemestreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chefdept/pv")
public class PVController {
    
    @Autowired
    private PVExportService pvExportService;
    
    @Autowired
    private SemestreRepository semestreRepository;
    
    
    @Autowired
    private UserService userService;
    
    /**
     * Get all available semesters for PV generation
     * @return List of semesters
     */
    @GetMapping("/semestres")
    public ResponseEntity<List<Semestre>> getAllSemestres() {
        List<Semestre> allSemestres = semestreRepository.findAll();
        List<Semestre> filteredSemestres = SemestreUtil.getCurrentPariteSemestres(allSemestres);
        return ResponseEntity.ok(filteredSemestres);
    }
    
    /**
     * Get department for the current chef de département
     * @param email Email of the current user
     * @return Department information
     */
    @GetMapping("/departement")
    public ResponseEntity<?> getDepartement(@RequestParam String email) {
        Departement departement = userService.getDepartementByChefEmail(email);
        if (departement == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Aucun département trouvé pour cet utilisateur");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(departement);
    }
    
    /**
     * Generate PV Excel file for a specific semester and department
     * @param semestreId ID of the semester
     * @param codeDep Department code
     * @return Excel file as byte array
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPv(
            @RequestParam Long semestreId,
            @RequestParam String codeDep) {
        try {
            byte[] excelContent = pvExportService.exportPvExcel(semestreId, codeDep);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "PV_" + codeDep + "_Semestre_" + semestreId + ".xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
