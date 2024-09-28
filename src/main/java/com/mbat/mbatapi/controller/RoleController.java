
package com.mbat.mbatapi.controller;

import com.mbat.mbatapi.entity.ERole;
import com.mbat.mbatapi.entity.Role;
import com.mbat.mbatapi.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Contrôleur pour gérer les opérations liées aux rôles dans l'application.
 */
@RestController
@RequestMapping("/api/roles")
@Tag(name = "Rôles", description = "API pour la gestion des rôles dans l'application.")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * Récupère un rôle par son nom.
     *
     * @param name Le nom du rôle à récupérer (ex: ROLE_USER, ROLE_ADMIN).
     * @return Le rôle correspondant ou une réponse 404 si le rôle n'existe pas.
     */
    @Operation(summary = "Récupérer un rôle par nom", description = "Permet de récupérer les informations d'un rôle en utilisant son nom.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rôle trouvé avec succès."),
            @ApiResponse(responseCode = "404", description = "Rôle non trouvé.")
    })
    @GetMapping("/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable("name") ERole name) {
        Optional<Role> role = roleService.findByName(name);
        return role.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Crée un nouveau rôle dans l'application.
     *
     * @param role L'objet rôle à créer.
     * @return Le rôle créé.
     */
    @Operation(summary = "Créer un nouveau rôle", description = "Permet de créer un nouveau rôle dans l'application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rôle créé avec succès."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de la création du rôle.")
    })
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role createdRole = roleService.save(role);
        return ResponseEntity.ok(createdRole);
    }
}
