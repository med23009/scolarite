package com.esp.scolarite.Config;

import com.esp.scolarite.Service.auth.AuthService;
import com.esp.scolarite.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class StartWithTheseUsers {

    private final AuthService service;

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {

            UserDto userAdmin = new UserDto();
            userAdmin.setNNI("888222333");
            userAdmin.setNationalite("Mauritannien");
            userAdmin.setNom("NIANG");
            userAdmin.setGenre("M");
            userAdmin.setRole(0);
            userAdmin.setEmail("admin@esp.mr");
            userAdmin.setCompteBancaire("XXX-XXX-XXX");
            userAdmin.setNomBanque("BMCI");
            userAdmin.setLieuDeNaissance("SomeWhere");
            userAdmin.setDateNaissance(LocalDate.of(1997, 11, 30));
            userAdmin.setPassword("123456");
            userAdmin.setTelephone("12345678");
            userAdmin.setSpecialiste("Informaticien");
            userAdmin.setPrenom("Oumar");

            // Let's register these user

            service.register(userAdmin);

            // Création de plusieurs chefs de département à partir d'une liste d'emails
            String[] chefDeptEmails = {
                    "Sidi.biha@esp.mr",
                    "helmi.aloui@esp.mr",
                    "med_cheikh.teguedy@esp.mr",
                    "didi.maghlah@esp.mr",
                    "hafedh.mohamed-babou@esp.mr"
            };
            for (String email : chefDeptEmails) {
                UserDto chefDept = new UserDto();
                chefDept.setEmail(email);
                chefDept.setRole(1); // 1 = CHEF_DEPT
                chefDept.setPassword("123456");
                chefDept.setNationalite("Mauritanien");
                chefDept.setGenre("M");
                chefDept.setNNI("000000000");
                chefDept.setCompteBancaire("XXX-XXX-XXX");
                chefDept.setNomBanque("BMCI");
                chefDept.setLieuDeNaissance("SomeWhere");
                chefDept.setDateNaissance(LocalDate.of(1990, 1, 1));
                chefDept.setTelephone("00000000");
                chefDept.setSpecialiste("Enseignant");
                // Extraire nom et prénom à partir de l'email (avant le @, séparé par . ou _ ou
                // -)
                String localPart = email.split("@")[0];
                String[] parts = localPart.split("[._-]");
                if (parts.length >= 2) {
                    chefDept.setPrenom(capitalize(parts[0]));
                    chefDept.setNom(capitalize(parts[1]));
                } else {
                    chefDept.setPrenom(capitalize(localPart));
                    chefDept.setNom("CHEFDEPT");
                }
                service.register(chefDept);
            }

        };
    }

    // Méthode utilitaire pour mettre la première lettre en majuscule
    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

}
