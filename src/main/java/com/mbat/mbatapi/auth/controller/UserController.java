package com.mbat.mbatapi.auth.controller;

import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.auth.entity.VerificationToken;
import com.mbat.mbatapi.auth.exception.InvalidEmailException;
import com.mbat.mbatapi.auth.exception.InvalidPasswordException;
import com.mbat.mbatapi.auth.payload.request.SignupRequest;
import com.mbat.mbatapi.auth.payload.response.MessageResponse;
import com.mbat.mbatapi.auth.repository.UserRepository;
import com.mbat.mbatapi.auth.repository.VerificationTokenRepository;
import com.mbat.mbatapi.auth.security.services.UserDetailsImpl;
import com.mbat.mbatapi.auth.service.EmailService;
import com.mbat.mbatapi.auth.service.EncryptionService;
import com.mbat.mbatapi.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EncryptionService encryptionService;


    @GetMapping("/get-one/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("firstName", user.getFirstName() != null ? encryptionService.decrypt(user.getFirstName()) : null);
            userData.put("lastName", user.getLastName() != null ? encryptionService.decrypt(user.getLastName()) : null);
            userData.put("phone", user.getPhone() != null ? encryptionService.decrypt(user.getPhone()) : null);
            userData.put("verified", user.isVerified());  // Ajouter l'état de vérification
            userData.put("isTwoFactorEnabled", user.isTwoFactorEnabled());
            userData.put("twoFactorMethod", user.getFirstTwoFactorMethod());
            userData.put("passwordLastUpdated", user.getPasswordLastUpdated());
            userData.put("securityQuestion", encryptionService.decrypt(user.getSecurityQuestion()));

            return ResponseEntity.ok(userData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Utilisateur non trouvé"));
        }
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
        return userService.registerUser(signUpRequest);
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
    public ResponseEntity<?> updateInformationUser(@PathVariable("id") Long id, @RequestBody User user)
            throws InvalidEmailException {
        try {
            // Appel au service pour mettre à jour les informations
            ResponseEntity<?> response = userService.updateInformationUser(id, user);

            // Vérification du statut de la réponse pour renvoyer le message approprié
            if (response.getStatusCode() == HttpStatus.OK) {
                return response;
            } else {
                // Si un autre problème survient, on retourne directement le corps de la réponse du service
                return response;
            }
        } catch (InvalidEmailException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur : " + e.getMessage()));
        } catch (Exception e) {
            // Gestion d'erreurs générales pour d'autres cas inattendus
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Une erreur est survenue : " + e.getMessage()));
        }
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
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        ResponseEntity<HttpStatus> response = userService.deleteUser(id);
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            return ResponseEntity.ok(new MessageResponse("Utilisateur supprimé avec succès."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la suppression de l'utilisateur."));
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
     * Réenvoie un e-mail de déverrouillage à l'utilisateur si le compte est verrouillé.
     *
     * @param requestBody Contient l'e-mail de l'utilisateur.
     * @return Un message de succès ou une erreur en cas d'échec.
     */
    @Operation(summary = "Renvoyer l'e-mail de déverrouillage", description = "Réenvoie un e-mail de déverrouillage à l'utilisateur si le compte est verrouillé.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail de déverrouillage envoyé avec succès."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de l'envoi de l'e-mail de déverrouillage.")
    })
    @PostMapping("/resend-unlock-email")
    public ResponseEntity<String> resendUnlockEmail(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("L'e-mail est requis");
            }

            Optional<User> userOptional = userRepository.findByUsername(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("L'utilisateur avec cet e-mail n'existe pas.");
            }

            User user = userOptional.get();

            // Vérifie si le compte est verrouillé
            if (!user.isAccountLocked()) {
                return ResponseEntity.badRequest().body("Le compte n'est pas verrouillé.");
            }

            // Créer un nouveau jeton de déverrouillage
            String unlockToken = UUID.randomUUID().toString();
            user.setUnlockToken(unlockToken);
            userRepository.save(user);

            // Envoyer un nouvel e-mail de déverrouillage avec le lien vers le frontend Angular
            String unlockLink = "http://192.168.56.101:4200/unlock-account?token=" + unlockToken;  // Remplace cette URL par celle de ton frontend
            emailService.sendUnlockEmail(user.getUsername(), unlockLink);

            return ResponseEntity.ok("Un nouvel e-mail de déverrouillage a été envoyé à votre adresse.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'envoi de l'e-mail de déverrouillage : " + e.getMessage());
        }
    }

    /**
     * Réenvoie un e-mail de vérification de compte à l'utilisateur.
     *
     * @param requestBody Contient l'e-mail de l'utilisateur.
     * @return Un message de succès ou une erreur si l'e-mail ne peut pas être envoyé.
     */
    @Operation(summary = "Renvoyer l'e-mail de vérification", description = "Permet à un utilisateur de demander un nouvel e-mail de vérification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail de vérification renvoyé avec succès."),
            @ApiResponse(responseCode = "400", description = "Erreur lors de la demande de renvoi de l'e-mail."),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé.")
    })
    @PostMapping("/resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("L'email est requis.");
            }
            return userService.resendVerificationEmail(email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur lors de l'envoi du mail de vérification : " + e.getMessage()));
        }
    }

    @PatchMapping("/update-theme")
    public ResponseEntity<?> updateTheme(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody Map<String, String> requestBody) {
        String theme = requestBody.get("theme");
        if (theme == null || theme.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Le thème est requis."));
        }

        // Récupérer l'utilisateur actuel
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTheme(theme);  // Mettre à jour le thème de l'utilisateur
            userRepository.save(user);  // Sauvegarder les changements
            return ResponseEntity.ok(new MessageResponse("Thème mis à jour avec succès."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Utilisateur non trouvé."));
        }
    }


    /**
     * Met à jour la question secrète et la réponse de l'utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @param requestBody Contient la nouvelle question secrète et la réponse.
     * @return Un message de succès ou une erreur si la mise à jour échoue.
     */
    @PutMapping("/{userId}/secret-question")
    public ResponseEntity<?> updateSecretQuestion(@PathVariable Long userId, @RequestBody Map<String, String> requestBody) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String newQuestion = requestBody.get("question");
            String answer = requestBody.get("answer");

            if (newQuestion == null || newQuestion.isEmpty() || answer == null || answer.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("La question et la réponse sont obligatoires."));
            }

            user.setSecurityQuestion(encryptionService.encrypt(newQuestion));
            user.setSecurityAnswer(encryptionService.encrypt(answer));
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Question secrète mise à jour avec succès."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    /**
     * Vérifie la réponse à la question secrète d'un utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @param requestBody Contient la réponse fournie par l'utilisateur.
     * @return Un ResponseEntity avec true si la réponse est correcte, sinon false.
     */
    @Operation(summary = "Vérifier la réponse à la question secrète", description = "Permet de vérifier si la réponse à la question secrète d'un utilisateur est correcte.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réponse correcte ou incorrecte."),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé.")
    })
    @PostMapping("/{userId}/verify-secret-answer")
    @PreAuthorize("#userId == principal.id") // Restreint l'accès à l'utilisateur actuel uniquement
    public ResponseEntity<Boolean> verifySecretAnswer(@PathVariable Long userId, @RequestBody Map<String, String> requestBody) {
        String answer = requestBody.get("answer");
        boolean isCorrect = userService.verifySecretAnswer(userId, answer);
        return ResponseEntity.ok(isCorrect);
    }


}
