package com.esp.scolarite.Config;

import com.esp.scolarite.Service.admin.AdminService;
import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.Pole;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.repository.DepartementRepository;
import com.esp.scolarite.repository.PoleRepository;
import com.esp.scolarite.repository.SemestreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;

/**
 * DataLoader pour charger automatiquement les données initiales au démarrage de l'application
 */
@Configuration
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final AdminService adminService;
    private final PoleRepository poleRepository;
    private final DepartementRepository departementRepository;
    private final SemestreRepository semestreRepository;

    @Autowired
    public DataLoader(AdminService adminService, 
                      PoleRepository poleRepository, 
                      DepartementRepository departementRepository, 
                      SemestreRepository semestreRepository) {
        this.adminService = adminService;
        this.poleRepository = poleRepository;
        this.departementRepository = departementRepository;
        this.semestreRepository = semestreRepository;
    }

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            logger.info("Chargement des données initiales...");
            
            try {
                // Importer les pôles
                importPoles();
                
                // Importer les départements
                importDepartements();
                
                // Importer les semestres
                importSemestres();
                
                logger.info("Données initiales chargées avec succès!");
            } catch (Exception e) {
                logger.error("Erreur lors du chargement des données initiales: " + e.getMessage(), e);
            }
        };
    }

    private void importPoles() {
        try {
            // Vérifier si des pôles existent déjà
            long poleCount = poleRepository.count();
            if (poleCount > 0) {
                logger.info("Il y a déjà {} pôles dans la base de données. Import ignoré.", poleCount);
                return;
            }
            
            Resource resource = new ClassPathResource("data/poles.csv");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    List<Pole> poles = adminService.importPolesFromCSV(inputStream);
                    logger.info("{} pôles importés avec succès", poles.size());
                }
            } else {
                logger.warn("Le fichier poles.csv n'a pas été trouvé dans le répertoire data");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'importation des pôles: " + e.getMessage(), e);
        }
    }

    private void importDepartements() {
        try {
            // Vérifier si des départements existent déjà
            long departementCount = departementRepository.count();
            if (departementCount > 0) {
                logger.info("Il y a déjà {} départements dans la base de données. Import ignoré.", departementCount);
                return;
            }
            
            Resource resource = new ClassPathResource("data/departements.csv");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    List<Departement> departements = adminService.importDepartementsFromCSV(inputStream);
                    logger.info("{} départements importés avec succès", departements.size());
                }
            } else {
                logger.warn("Le fichier departements.csv n'a pas été trouvé dans le répertoire data");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'importation des départements: " + e.getMessage(), e);
        }
    }

    private void importSemestres() {
        try {
            // Vérifier si des semestres existent déjà
            long semestreCount = semestreRepository.count();
            if (semestreCount > 0) {
                logger.info("Il y a déjà {} semestres dans la base de données. Import ignoré.", semestreCount);
                return;
            }
            
            Resource resource = new ClassPathResource("data/semestres.csv");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    List<Semestre> semestres = adminService.importSemestresFromCSV(inputStream);
                    logger.info("{} semestres importés avec succès", semestres.size());
                }
            } else {
                logger.warn("Le fichier semestres.csv n'a pas été trouvé dans le répertoire data");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'importation des semestres: " + e.getMessage(), e);
        }
    }
}
