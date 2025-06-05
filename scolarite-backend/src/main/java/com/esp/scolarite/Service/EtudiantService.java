package com.esp.scolarite.Service;

import com.esp.scolarite.entity.*;
import com.esp.scolarite.repository.DepartementRepository;
import com.esp.scolarite.repository.EtudiantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class EtudiantService {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private DepartementRepository departementRepository;

// Modification du service EtudiantService.java pour assurer le chargement correct des départements

public List<Etudiant> getAllEtudiants() {
  List<Etudiant> etudiants = etudiantRepository.findAll();
  
  // S'assurer que chaque département est complètement chargé
  for (Etudiant etudiant : etudiants) {
      if (etudiant.getDepartement() != null && etudiant.getDepartement().getIdDep() != null) {
          Long depId = etudiant.getDepartement().getIdDep();
          departementRepository.findById(depId).ifPresent(etudiant::setDepartement);
      }
  }
  
  return etudiants;
}

    public Optional<Etudiant> getEtudiantById(Long id) {
        return etudiantRepository.findById(id);
    }

 public Etudiant updateEtudiant(Long id, Etudiant newData) {
    Etudiant existing = etudiantRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

    existing.setMatricule(newData.getMatricule());
    existing.setNom(newData.getNom());
    existing.setPrenom(newData.getPrenom());
    existing.setEmail(newData.getEmail());
    existing.setSexe(newData.getSexe());
    existing.setDateNaissance(newData.getDateNaissance());
    existing.setLieuNaissance(newData.getLieuNaissance());
    existing.setTelephoneEtudiant(newData.getTelephoneEtudiant());
    existing.setTelephoneCorrespondant(newData.getTelephoneCorrespondant());
    existing.setAdresseResidence(newData.getAdresseResidence());
    existing.setAnneeObtentionBac(newData.getAnneeObtentionBac());
    existing.setDateInscription(newData.getDateInscription());
    existing.setDepartement(newData.getDepartement());
    existing.setPromotion(newData.getPromotion());

    return etudiantRepository.save(existing);
}

    public void deleteEtudiant(Long id) {
        etudiantRepository.deleteById(id);
    }

    public List<Etudiant> searchEtudiants(String query) {
        String searchTerm = "%" + query.toLowerCase() + "%";
        return etudiantRepository.findByMatriculeContainingOrNomContainingOrPrenomContainingOrEmailContaining(
                searchTerm, searchTerm, searchTerm, searchTerm);
    }

    public List<Etudiant> importFromCSV(MultipartFile file, String defaultPromotion) throws Exception {
        List<Etudiant> importedEtudiants = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            Map<String, Integer> headers = new HashMap<>();

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    // Lire les en-têtes du fichier CSV
                    String[] headerValues = line.split(",");
                    for (int i = 0; i < headerValues.length; i++) {
                        headers.put(headerValues[i].trim().toLowerCase(), i);
                    }
                    firstLine = false;
                    continue;
                }

                // Séparer les valeurs du CSV et valider
                String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (values.length < 3) {
                    throw new Exception("Format CSV invalide, attendu au minimum: matricule,nom,prenom");
                }

                Etudiant etudiant = new Etudiant();
                
                // Récupérer les données du CSV en vérifiant que les headers existent
                safeSetField(headers, values, "matricule", etudiant::setMatricule);
                safeSetField(headers, values, "nom", etudiant::setNom);
                safeSetField(headers, values, "prenom", etudiant::setPrenom);
                safeSetField(headers, values, "prenomar", etudiant::setPrenomAR);
                safeSetField(headers, values, "nomar", etudiant::setNomAR);
                safeSetField(headers, values, "lieunaissance", etudiant::setLieuNaissance);
                safeSetField(headers, values, "lieunaissancear", etudiant::setLieuNaissanceAR);
                safeSetField(headers, values, "sexe", etudiant::setSexe);
                safeSetField(headers, values, "nni", etudiant::setNNI);
                safeSetField(headers, values, "email", etudiant::setEmail);
                safeSetField(headers, values, "telephoneetudiant", etudiant::setTelephoneEtudiant);
                safeSetField(headers, values, "telephonecorrespondant", etudiant::setTelephoneCorrespondant);
                safeSetField(headers, values, "adresseresidence", etudiant::setAdresseResidence);
                
                // Traitement sécurisé des dates
                safeSetDate(headers, values, "datenaissance", dateFormat, etudiant::setDateNaissance);
                safeSetDate(headers, values, "dateinscription", dateFormat, etudiant::setDateInscription);
                
                // Traitement sécurisé des nombres
                if (headers.containsKey("anneeobtentionbac") && headers.get("anneeobtentionbac") < values.length) {
                    String value = values[headers.get("anneeobtentionbac")].trim();
                    if (!value.isEmpty()) {
                        try {
                            etudiant.setAnneeObtentionBac(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            System.err.println("Format de nombre invalide pour anneeobtentionbac: " + e.getMessage());
                        }
                    }
                }

                // Récupérer le code du département et associer le département à l'étudiant
                if (headers.containsKey("departement") && headers.get("departement") < values.length) {
                    String codeDepartement = values[headers.get("departement")].trim();
                    if (!codeDepartement.isEmpty()) {
                        // D'abord, essayer de trouver le département par son code
                        Optional<Departement> departementOpt = departementRepository.findByCodeDep(codeDepartement);
                        
                        // Si pas trouvé par code, essayer par intitulé
                        if (departementOpt.isEmpty()) {
                            departementOpt = departementRepository.findByIntitule(codeDepartement);
                        }
                        
                        // Si trouvé, associer à l'étudiant
                        departementOpt.ifPresent(etudiant::setDepartement);
                    }
                }
                
                // Récupérer la promotion (priorité au CSV, sinon utiliser la promotion par défaut)
                if (headers.containsKey("promotion") && headers.get("promotion") < values.length) {
                    String promotion = values[headers.get("promotion")].trim();
                    if (!promotion.isEmpty()) {
                        etudiant.setPromotion(promotion);
                    } else if (defaultPromotion != null && !defaultPromotion.isEmpty()) {
                        etudiant.setPromotion(defaultPromotion);
                    }
                } else if (defaultPromotion != null && !defaultPromotion.isEmpty()) {
                    etudiant.setPromotion(defaultPromotion);
                }

                // Sauvegarder l'étudiant
                importedEtudiants.add(etudiantRepository.save(etudiant));
            }
        }

        return importedEtudiants;
    }

    // Méthode utilitaire pour définir un champ de manière sécurisée
    private void safeSetField(Map<String, Integer> headers, String[] values, String headerName, java.util.function.Consumer<String> setter) {
        if (headers.containsKey(headerName) && headers.get(headerName) < values.length) {
            String value = values[headers.get(headerName)].trim();
            if (!value.isEmpty()) {
                setter.accept(value);
            }
        }
    }
    
    public Etudiant saveEtudiant(Etudiant etudiant) {
        if (etudiant.getDepartement() != null && etudiant.getDepartement().getIdDep() != null) {
            Long departementId = etudiant.getDepartement().getIdDep();
            Optional<Departement> departementOpt = departementRepository.findById(departementId);
            if (departementOpt.isPresent()) {
                etudiant.setDepartement(departementOpt.get());
            } else {
                etudiant.setDepartement(null); // Aucun département trouvé
            }
        }
        return etudiantRepository.save(etudiant);
    }
    
    // Méthode utilitaire pour définir une date de manière sécurisée
    private void safeSetDate(Map<String, Integer> headers, String[] values, String headerName, SimpleDateFormat dateFormat, java.util.function.Consumer<Date> setter) {
        if (headers.containsKey(headerName) && headers.get(headerName) < values.length) {
            String value = values[headers.get(headerName)].trim();
            if (!value.isEmpty()) {
                try {
                    Date date = dateFormat.parse(value);
                    setter.accept(date);
                } catch (Exception e) {
                    System.err.println("Format de date invalide pour " + headerName + ": " + e.getMessage());
                }
            }
        }
    }
}