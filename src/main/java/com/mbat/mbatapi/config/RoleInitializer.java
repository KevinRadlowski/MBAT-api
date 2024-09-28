package com.mbat.mbatapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.mbat.mbatapi.entity.ERole;
import com.mbat.mbatapi.entity.Role;
import com.mbat.mbatapi.repository.RoleRepository;

@Configuration
public class RoleInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Vérifie si le rôle "ROLE_USER" existe, sinon l'ajoute
        if (!roleRepository.existsByName(ERole.ROLE_USER)) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            System.out.println("Role ROLE_USER ajouté à la base de données.");
        }

        // Vérifie si le rôle "ROLE_ADMIN" existe, sinon l'ajoute
        if (!roleRepository.existsByName(ERole.ROLE_ADMIN)) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            System.out.println("Role ROLE_ADMIN ajouté à la base de données.");
        }
    }
}
