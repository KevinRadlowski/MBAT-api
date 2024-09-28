package com.mbat.mbatapi.controller;

import javax.validation.Valid;

import com.mbat.mbatapi.entity.VerificationToken;
import com.mbat.mbatapi.repository.UserRepository;
import com.mbat.mbatapi.repository.VerificationTokenRepository;
import com.mbat.mbatapi.service.EmailService;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserBusiness business;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService; // Service d'envoi d'email

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws UserExistException {
        return business.authenticateUser(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest)
            throws InvalidPasswordException, InvalidEmailException {

        return business.registerUser(signUpRequest);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody ChangePasswordDto body, @AuthenticationPrincipal User user) {
        try {
            business.changePassword(body, user);
        } catch (InvalidPasswordException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'ancien mot de passe ne correspond pas");
        }
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<User> updateInformationUser(@PathVariable("id") Integer id, @RequestBody User user)
            throws InvalidEmailException {
        return business.updateInformationUser(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") Integer id) {
        return business.deleteUser(id);
    }


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

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> requestBody) {
        try {
            String token = requestBody.get("token");
            String newPassword = requestBody.get("newPassword");
            business.updatePassword(token, newPassword);

            // Retourner une réponse JSON structurée
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mot de passe mis à jour avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors de la réinitialisation du mot de passe : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userRepository.existsByUsername(email);
        return ResponseEntity.ok(exists);
    }

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
