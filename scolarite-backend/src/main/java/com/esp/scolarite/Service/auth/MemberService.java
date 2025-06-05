package com.esp.scolarite.Service.auth;

import com.esp.scolarite.entity.ElementDeModule;
import com.esp.scolarite.entity.Etudiant;
import com.esp.scolarite.entity.UniteEnseignement;
import com.esp.scolarite.entity.User;
import com.esp.scolarite.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository repository;
    private final ElementDeModuleRepository EM_repository;
    private final EtudiantRepository Etudiant_repository;
    private final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);


    //Enseignant {}
    public Collection<Etudiant> linkingEnseignant_Etudiants(long id)
    {
        Optional<User> Potentielenseignant = repository.findById(id);
        List<Etudiant> etudiants = Etudiant_repository.findAll();

        if (Potentielenseignant.isPresent())
        {
            User enseignant = Potentielenseignant.get();
            List<ElementDeModule> EMs = EM_repository.findAll();
            for (ElementDeModule em : EMs)
            {
                if (em.getResponsableEM().getEmail().equals(enseignant.getEmail()))
                {
                    UniteEnseignement UE = em.getUniteEnseignement();
                    try{

                        boolean isPolaire = UE.getPole()!=null;
                        boolean isDepartemental = UE.getDepartement()!=null;

                        LOGGER.info("The type of the UE is {}", isPolaire ? "Polaire" : "Depatementale");

                        if (isPolaire)
                        {
                            int anneePromotion = em.getId_semestre().getAnnee();
                            List<Etudiant> etudiantsConcernes = new ArrayList<>();
                            for (Etudiant etudiant : etudiants)
                                {
                                    if (etudiant.getDossier().getAnneeCourante()==anneePromotion)
                                        etudiantsConcernes.add(etudiant);
                                }
                            return etudiantsConcernes;
                        } else if (isDepartemental)
                        {
                            int anneePromotion = em.getId_semestre().getAnnee();
                            List<Etudiant> etudiantsConcernes = new ArrayList<>();
                            for (Etudiant etudiant : etudiants)
                            {
                                if (etudiant.getDossier()
                                        .getAnneeCourante()==anneePromotion &&
                                         UE.getDepartement().getCodeDep().
                                                equals(etudiant.getDepartement().getCodeDep())
                                )
                                {
                                    etudiantsConcernes.add(etudiant);
                                }
                            }
                            return  etudiantsConcernes;

                        }

                    } catch (Exception e)
                    {
                        LOGGER.warn("A non blocking error has occurred, {}",e.getMessage());
                    }
                }
            }
        }
        return null;
    }


    //Enseignant saisir des notes

    public int attribuerNote(String matricule_etudiant, long id)
    {
        Optional<Etudiant> etudiant = Etudiant_repository.findByMatricule(matricule_etudiant);
        if (etudiant.isPresent())
        {

        }
        return 0;

    }
}


