package com.esp.scolarite.Service;
import com.esp.scolarite.repository.DepartementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class DepartementService {

    @Autowired
    private DepartementRepository departementRepository;

    public void deleteDepartement(Long id) {
        // Supprimer le département
        departementRepository.deleteById(id);
    }
}

