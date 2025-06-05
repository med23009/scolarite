package com.esp.scolarite.Service.admin;

import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.repository.SemestreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SemestreService {

    private final SemestreRepository semestreRepository;

    public SemestreService(SemestreRepository semestreRepository) {
        this.semestreRepository = semestreRepository;
    }

    public Semestre createSemestre(Semestre semestre) {
        return semestreRepository.save(semestre);
    }

    public Semestre updateSemestre(Long id, Semestre updated) {
        Optional<Semestre> existing = semestreRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Aucun semestre trouvé avec l'id : " + id);
        }


        updated.setIdSemestre(id);
        return semestreRepository.save(updated);
    }

    public void deleteSemestre(Long id) {
        if (!semestreRepository.existsById(id)) {
            throw new IllegalArgumentException("Le semestre avec l'id " + id + " n'existe pas.");
        }
        semestreRepository.deleteById(id);
    }

    public List<Semestre> getAllSemestres() {
        return semestreRepository.findAll();
    }

}
