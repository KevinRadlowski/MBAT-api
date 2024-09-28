package com.mbat.mbatapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.mbat.mbatapi.entity.ERole;
import com.mbat.mbatapi.entity.Role;
import com.mbat.mbatapi.repository.RoleRepository;

/**
 * Configuration pour initialiser les rôles dans la base de données lors du démarrage de l'application.
 * Cette classe implémente {@link CommandLineRunner} pour exécuter du code après le démarrage du contexte Spring.
 */
@Configuration
public class RoleInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Méthode exécutée au démarrage de l'application.
     * Elle vérifie si les rôles "ROLE_USER" et "ROLE_ADMIN" existent dans la base de données.
     * Si l'un des rôles est absent, il est ajouté à la base de données.
     *
     * @param args les arguments de la ligne de commande.
     * @throws Exception en cas d'erreur lors de l'exécution.
     */
    @Override
    public void run(String... args) throws Exception {
        initializeRole(ERole.ROLE_USER, "Role ROLE_USER ajouté à la base de données.");
        initializeRole(ERole.ROLE_ADMIN, "Role ROLE_ADMIN ajouté à la base de données.");
    }

    /**
     * Vérifie si un rôle existe dans la base de données, sinon l'ajoute.
     *
     * @param roleName Nom du rôle à vérifier.
     * @param message Message à afficher si le rôle est ajouté.
     */
    private void initializeRole(ERole roleName, String message) {
        if (!roleRepository.existsByName(roleName)) {
            roleRepository.save(new Role(roleName));
        }
    }
}
