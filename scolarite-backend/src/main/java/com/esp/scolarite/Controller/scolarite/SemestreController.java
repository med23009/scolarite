package com.esp.scolarite.Controller.scolarite;

import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.Service.admin.SemestreService;
import com.esp.scolarite.util.SemestreUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semestres")
public class SemestreController {

    private final SemestreService semestreService;

    public SemestreController(SemestreService semestreService) {
        this.semestreService = semestreService;
    }

    @PostMapping
    public Semestre createSemestre(@RequestBody Semestre semestre) {
        return semestreService.createSemestre(semestre);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Semestre> updateSemestre(@PathVariable Long id, @RequestBody Semestre semestre) {
        return ResponseEntity.ok(semestreService.updateSemestre(id, semestre));
    }


    @GetMapping
    public List<Semestre> getCurrentPariteSemestres() {
        List<Semestre> allSemestres = semestreService.getAllSemestres();
        return SemestreUtil.getCurrentPariteSemestres(allSemestres);
    }

    @GetMapping("/all")
    public List<Semestre> getAllSemestres() {
        return semestreService.getAllSemestres();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSemestre(@PathVariable Long id) {
        semestreService.deleteSemestre(id);
        return ResponseEntity.noContent().build();
    }

}
