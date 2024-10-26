package com.mbat.mbatapi.auth.controller;

import com.mbat.mbatapi.auth.ChangePasswordDto;
import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.auth.exception.InvalidPasswordException;
import com.mbat.mbatapi.auth.payload.response.MessageResponse;
import com.mbat.mbatapi.auth.repository.UserRepository;
import com.mbat.mbatapi.auth.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/password")
@Tag(name = "Mot de passe", description = "API pour les opérations liées aux mots de passe.")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Envoie un e-mail de réinitialisation de mot de passe à l'utilisateur en fonction de son e-mail.
     *
     * @param requestBody Contient l'e-mail de l'utilisateur.
     * @return Un message de succès si l'e-mail a été envoyé, ou un message d'erreur en cas d'échec.
     */
    @Operation(summary = "Demande de réinitialisation du mot de passe",
            description = "Envoie un lien de réinitialisation de mot de passe à l'utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lien de réinitialisation envoyé."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de la demande de réinitialisation.")
    })
    @PostMapping("/request-reset") // Avant : forgot-password
    public ResponseEntity<MessageResponse> requestPasswordReset(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("L'email est requis.");
            }

            Optional<User> userOptional = userRepository.findByUsername(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("L'utilisateur n'existe pas."));
            }

            passwordService.processForgotPassword(email);
            return ResponseEntity.ok(new MessageResponse("Lien de réinitialisation du mot de passe envoyé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur lors de la demande de réinitialisation du mot de passe."));
        }
    }

    /**
     * Réinitialise le mot de passe de l'utilisateur avec un jeton de réinitialisation.
     *
     * @param requestBody Contient le jeton de réinitialisation et le nouveau mot de passe.
     * @return Un message de succès ou d'erreur en fonction du résultat de la réinitialisation.
     */
    @Operation(summary = "Réinitialiser le mot de passe",
            description = "Réinitialise le mot de passe de l'utilisateur à l'aide d'un jeton de réinitialisation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe mis à jour avec succès."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de la réinitialisation du mot de passe.")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody Map<String, String> requestBody) {
        try {
            String token = requestBody.get("token");
            String newPassword = requestBody.get("newPassword");
            passwordService.updatePassword(token, newPassword);
            return ResponseEntity.ok(new MessageResponse("Mot de passe mis à jour avec succès."));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur lors de la réinitialisation du mot de passe : " + e.getMessage()));
        }
    }

    /**
     * Met à jour le mot de passe d'un utilisateur spécifique en fonction de son ID (pour les administrateurs).
     *
     * @param id          L'identifiant de l'utilisateur dont le mot de passe doit être modifié.
     * @param passwordDto Contient le nouveau mot de passe.
     * @return Un message de succès ou d'erreur.
     */
    @Operation(summary = "Mettre à jour le mot de passe d'un utilisateur",
            description = "Permet à un administrateur de mettre à jour le mot de passe d'un utilisateur spécifique en fonction de son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mot de passe mis à jour avec succès."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de la mise à jour du mot de passe.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/admin/update/{id}") // avant : update-password/{id}
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<MessageResponse> adminUpdateUserPassword(@PathVariable("id") Long id, @RequestBody ChangePasswordDto passwordDto) {
        try {
            passwordService.updateUserPassword(id, passwordDto.getNewPassword());
            return ResponseEntity.noContent().build(); // Statut 204 : succès sans contenu
        } catch (InvalidPasswordException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur : " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la mise à jour du mot de passe pour l'utilisateur avec l'id : " + id));
        }
    }



    /**
     * Vérifie si l'ancien mot de passe fourni est correct pour un utilisateur donné.
     *
     * @param userId     L'identifiant de l'utilisateur.
     * @param oldPassword L'ancien mot de passe à vérifier.
     * @return Un message indiquant si le mot de passe est correct ou non.
     */
    @Operation(summary = "Vérifier l'ancien mot de passe",
            description = "Vérifie si l'ancien mot de passe est correct pour un utilisateur donné.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe valide."),
            @ApiResponse(responseCode = "400", description = "L'ancien mot de passe est incorrect."),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé.")
    })
    @GetMapping("/validate-old-password") // ancien : check-old-password
    public ResponseEntity<MessageResponse> validateOldPassword(@RequestParam("userId") Long userId, @RequestParam("oldPassword") String oldPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean isValid = passwordService.checkOldPassword(user, oldPassword);
            if (isValid) {
                return ResponseEntity.ok(new MessageResponse("Mot de passe valide."));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("L'ancien mot de passe est incorrect."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    /**
     * Permet à l'utilisateur authentifié de changer son propre mot de passe.
     *
     * @param body Contient l'ancien et le nouveau mot de passe.
     */
    @Operation(summary = "Changer le mot de passe de l'utilisateur authentifié",
            description = "Permet à l'utilisateur actuel de changer son propre mot de passe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mot de passe changé avec succès."),
            @ApiResponse(responseCode = "400", description = "L'ancien mot de passe est incorrect.")
    })
    @PatchMapping("/change") // Ancien : password
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeAuthenticatedUserPassword(@RequestBody ChangePasswordDto body) {
        try {
            // Récupération de l'utilisateur depuis le contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

            // Maintenant que l'utilisateur est récupéré, appelez le service
            passwordService.changePassword(body, user);
        } catch (InvalidPasswordException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'ancien mot de passe ne correspond pas");
        }
    }
}
