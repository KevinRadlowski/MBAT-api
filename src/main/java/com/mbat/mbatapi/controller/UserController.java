package com.mbat.mbatapi.controller;

import javax.validation.Valid;

import com.mbat.mbatapi.entity.VerificationToken;
import com.mbat.mbatapi.repository.UserRepository;
import com.mbat.mbatapi.repository.VerificationTokenRepository;
import com.mbat.mbatapi.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.mbat.mbatapi.auth.ChangePasswordDto;
import com.mbat.mbatapi.business.UserBusiness;
import com.mbat.mbatapi.entity.User;
import com.mbat.mbatapi.exception.InvalidEmailException;
import com.mbat.mbatapi.exception.InvalidPasswordException;
import com.mbat.mbatapi.exception.UserExistException;
import com.mbat.mbatapi.payload.request.LoginRequest;
import com.mbat.mbatapi.payload.request.SignupRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrôleur pour gérer les opérations utilisateur telles que l'inscription, la connexion,
 * la réinitialisation de mot de passe, et la vérification d'e-mail.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
@Tag(name = "Utilisateur", description = "API pour les opérations liées aux utilisateurs.")
 class UserController {

    @Autowired
    private UserBusiness business;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService; // Service d'envoi d'email

    /**
     * Authentifie un utilisateur avec son nom d'utilisateur et son mot de passe.
     *
     * @param loginRequest Les informations de connexion de l'utilisateur.
     * @return Une réponse contenant le token JWT en cas de succès, ou une erreur en cas d'échec.
     * @throws UserExistException Si l'utilisateur n'existe pas.
     */
    @Operation(summary = "Authentifie un utilisateur", description = "Vérifie les informations de connexion d'un utilisateur et retourne un token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie."),
            @ApiResponse(responseCode = "401", description = "Identifiant ou mot de passe incorrect."),
            @ApiResponse(responseCode = "400", description = "Données de requête invalides.")
    })
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws UserExistException {
        return business.authenticateUser(loginRequest);
    }

    /**
     * Inscrit un nouvel utilisateur.
     *
     * @param signUpRequest Les informations d'inscription de l'utilisateur.
     * @return Un message de succès ou une erreur si l'inscription échoue.
     * @throws InvalidPasswordException Si le mot de passe ne respecte pas les règles de validation.
     * @throws InvalidEmailException    Si l'e-mail est invalide ou déjà utilisé.
     */
    @Operation(summary = "Inscription d'un utilisateur", description = "Enregistre un nouvel utilisateur avec un nom d'utilisateur et un mot de passe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur enregistré avec succès."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de l'inscription.")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest)
            throws InvalidPasswordException, InvalidEmailException {
        return business.registerUser(signUpRequest);
    }

    /**
     * Change le mot de passe de l'utilisateur authentifié.
     *
     * @param body Les informations de changement de mot de passe.
     * @param user L'utilisateur authentifié.
     */
    @Operation(summary = "Changer le mot de passe", description = "Permet de changer le mot de passe de l'utilisateur authentifié.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mot de passe changé avec succès."),
            @ApiResponse(responseCode = "400", description = "L'ancien mot de passe est incorrect.")
    })
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody ChangePasswordDto body, @AuthenticationPrincipal User user) {
        try {
            business.changePassword(body, user);
        } catch (InvalidPasswordException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'ancien mot de passe ne correspond pas");
        }
    }

    /**
     * Met à jour les informations d'un utilisateur.
     *
     * @param id   L'ID de l'utilisateur à mettre à jour.
     * @param user Les nouvelles informations de l'utilisateur.
     * @return L'utilisateur mis à jour.
     * @throws InvalidEmailException Si l'e-mail est invalide ou déjà utilisé.
     */
    @Operation(summary = "Mettre à jour les informations d'un utilisateur", description = "Permet de mettre à jour les informations personnelles d'un utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur mis à jour avec succès."),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé."),
            @ApiResponse(responseCode = "400", description = "Informations invalides.")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<User> updateInformationUser(@PathVariable("id") Integer id, @RequestBody User user)
            throws InvalidEmailException {
        return business.updateInformationUser(id, user);
    }

    /**
     * Supprime un utilisateur par son ID.
     *
     * @param id L'ID de l'utilisateur à supprimer.
     * @return Un statut 204 en cas de succès ou 500 en cas d'erreur.
     */
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur en fonction de son identifiant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé avec succès."),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") Integer id) {
        return business.deleteUser(id);
    }


    /**
     * Envoie un e-mail de réinitialisation de mot de passe.
     *
     * @param requestBody Contient l'e-mail de l'utilisateur.
     * @return Un message de succès ou une erreur si la demande échoue.
     */
    @Operation(summary = "Réinitialisation du mot de passe", description = "Envoie un lien de réinitialisation de mot de passe à l'utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lien de réinitialisation envoyé."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de la demande de réinitialisation.")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            business.processForgotPassword(email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Lien de réinitialisation envoyé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors de la demande de réinitialisation du mot de passe");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Réinitialise le mot de passe de l'utilisateur en utilisant un jeton.
     *
     * @param requestBody Contient le jeton de réinitialisation et le nouveau mot de passe.
     * @return Un message de succès ou une erreur en cas d'échec.
     */
    @Operation(summary = "Réinitialiser le mot de passe", description = "Réinitialise le mot de passe de l'utilisateur à l'aide d'un jeton de réinitialisation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe mis à jour avec succès."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de la réinitialisation du mot de passe.")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> requestBody) {
        try {
            String token = requestBody.get("token");
            String newPassword = requestBody.get("newPassword");
            business.updatePassword(token, newPassword);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Mot de passe mis à jour avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors de la réinitialisation du mot de passe : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Vérifie si un e-mail est déjà utilisé.
     *
     * @param email L'e-mail à vérifier.
     * @return Vrai si l'e-mail existe, faux sinon.
     */
    @Operation(summary = "Vérifier l'existence d'un e-mail", description = "Vérifie si un e-mail est déjà utilisé dans le système.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vérification réussie."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de la vérification de l'e-mail.")
    })
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userRepository.existsByUsername(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * Vérifie un compte utilisateur à l'aide d'un jeton.
     *
     * @param token Le jeton de vérification.
     * @return Un message de succès ou une erreur si le jeton est invalide.
     */
    @Operation(summary = "Vérification de compte", description = "Vérifie un compte utilisateur à l'aide d'un jeton de vérification envoyé par e-mail.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compte vérifié avec succès."),
            @ApiResponse(responseCode = "400", description = "Jeton de vérification invalide ou expiré.")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
            if (verificationToken == null || verificationToken.isExpired()) {
                throw new IllegalArgumentException("Jeton de vérification invalide ou expiré.");
            }

            User user = verificationToken.getUser();
            user.setVerified(true);
            userRepository.save(user);

            verificationTokenRepository.delete(verificationToken);

            return ResponseEntity.ok("Compte vérifié avec succès !");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la vérification du compte : " + e.getMessage());
        }
    }

    /**
     * Réenvoie un e-mail de vérification à l'utilisateur.
     *
     * @param requestBody Contient l'e-mail de l'utilisateur.
     * @return Un message de succès ou une erreur en cas d'échec.
     */
    @Operation(summary = "Renvoyer l'e-mail de vérification", description = "Réenvoie un e-mail de vérification à l'utilisateur si le compte n'est pas encore vérifié.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail de vérification envoyé avec succès."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de l'envoi de l'e-mail de vérification.")
    })
    @PostMapping("/resend-verification-email")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }

            Optional<User> userOptional = userRepository.findByUsername(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("L'utilisateur avec cet e-mail n'existe pas.");
            }

            User user = userOptional.get();
            if (user.isVerified()) {
                return ResponseEntity.badRequest().body("Ce compte est déjà vérifié.");
            }

            // Créer un nouveau jeton de vérification
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(token, user);
            verificationTokenRepository.save(verificationToken);

            // Envoyer un nouvel e-mail de vérification
            String verificationLink = "http://localhost:4200/verify-email?token=" + token;
            emailService.sendVerificationEmail(user.getUsername(), verificationLink);

            return ResponseEntity.ok("Un nouvel e-mail de vérification a été envoyé à votre adresse.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'envoi de l'e-mail de vérification : " + e.getMessage());
        }
    }

}
