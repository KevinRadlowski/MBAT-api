package com.mbat.mbatapi.auth.controller;

import com.mbat.mbatapi.auth.entity.BackupCode;
import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.auth.payload.response.MessageResponse;
import com.mbat.mbatapi.auth.repository.UserRepository;
import com.mbat.mbatapi.auth.service.BackupCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backup-codes")
@Tag(name = "Codes de secours", description = "API pour la gestion des codes de secours d'authentification à deux facteurs.")
public class BackupCodeController {

    @Autowired
    private BackupCodeService backupCodeService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Récupère la liste des codes de secours pour un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @return La liste des codes de secours.
     */
    @Operation(summary = "Récupérer les codes de secours", description = "Récupère la liste des codes de secours pour un utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des codes récupérée avec succès."),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé.")
    })
    @GetMapping
    public ResponseEntity<?> getBackupCodes(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<BackupCode> codes = backupCodeService.getBackupCodesForUser(user);
        return ResponseEntity.ok(codes);
    }

    /**
     * Génère une nouvelle liste de codes de secours pour un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @return La nouvelle liste de codes de secours.
     */
    @Operation(summary = "Générer des codes de secours", description = "Génère une nouvelle liste de 10 codes de secours pour un utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Codes de secours générés avec succès."),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé.")
    })
    @PostMapping("/generate")
    public ResponseEntity<?> generateBackupCodes(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<BackupCode> newCodes = backupCodeService.generateBackupCodesForUser(user);
        return ResponseEntity.ok(newCodes);
    }

    /**
     * Utilise un code de secours.
     *
     * @param username Le nom d'utilisateur.
     * @param code Le code à utiliser.
     * @return Un message indiquant si le code a été utilisé avec succès ou s'il est invalide.
     */
    @Operation(summary = "Utiliser un code de secours", description = "Utilise un code de secours valide pour un utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code utilisé avec succès."),
            @ApiResponse(responseCode = "400", description = "Code invalide ou déjà utilisé."),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé.")
    })
    @PostMapping("/use")
    public ResponseEntity<?> useBackupCode(@RequestParam String username, @RequestParam String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean success = backupCodeService.useBackupCode(user, code);
        if (success) {
            return ResponseEntity.ok(new MessageResponse("Code utilisé avec succès"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Code invalide ou déjà utilisé"));
        }
    }
}
