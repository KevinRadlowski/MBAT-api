package com.mbat.mbatapi.auth.service;

import com.mbat.mbatapi.auth.entity.*;
import com.mbat.mbatapi.auth.exception.InvalidEmailException;
import com.mbat.mbatapi.auth.exception.InvalidPasswordException;
import com.mbat.mbatapi.auth.payload.request.LoginRequest;
import com.mbat.mbatapi.auth.payload.request.SignupRequest;
import com.mbat.mbatapi.auth.payload.response.JwtResponse;
import com.mbat.mbatapi.auth.payload.response.MessageResponse;
import com.mbat.mbatapi.auth.repository.*;
import com.mbat.mbatapi.auth.security.jwt.JwtUtils;
import com.mbat.mbatapi.auth.security.services.UserDetailsImpl;
import com.mbat.mbatapi.auth.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de gestion des opérations liées aux utilisateurs.
 */
@Service
public class UserService {
    private static final int MAX_FAILED_ATTEMPTS = 5; // Verrouille le compte après 5 tentatives
    public static final long LOCK_TIME_DURATION = 15 * 60 * 1000; // Durée du verrouillage (15 minutes)

    @Autowired
    private UserRepository userRepository;


    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    /**
     * Enregistre un nouvel utilisateur.
     *
     * @param signUpRequest Les informations d'inscription de l'utilisateur.
     * @return Un message indiquant le succès ou l'échec de l'inscription.
     * @throws InvalidPasswordException Si le mot de passe est invalide.
     * @throws InvalidEmailException    Si l'email est invalide.
     */
    public ResponseEntity<?> registerUser(SignupRequest signUpRequest)
            throws InvalidPasswordException, InvalidEmailException {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Cet utilisateur existe déjà."));
        }

        // Crée un nouveau compte utilisateur
        User user = new User(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()));
        user.setVerified(false); // Par défaut, le compte est non vérifié

        // Définir le rôle de l'utilisateur sur ROLE_USER par défaut
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Le rôle USER n'est pas défini."));
        user.setRoles(Collections.singleton(userRole));

        // Sauvegarde l'utilisateur dans la base de données
        userRepository.save(user);


        // Créer un jeton de vérification
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);

        // Envoyer un email de vérification
        String verificationLink = "http://192.168.56.101:4200/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getUsername(), verificationLink);

        return ResponseEntity.ok(new MessageResponse("Utilisateur enregistré avec succès! Veuillez vérifier votre email pour activer votre compte."));
    }

    /**
     * Supprime un utilisateur par son ID.
     *
     * @param id L'ID de l'utilisateur à supprimer.
     * @return Le statut de la réponse.
     */
    public ResponseEntity<HttpStatus> deleteUser(Long id) {
        try {
            // Supprime tous les refresh tokens associés à l'utilisateur
            refreshTokenService.deleteByUserId(id);

            // Supprimer les jetons de réinitialisation de mot de passe associés
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace(); // Affiche les détails complets de l'erreur dans les logs
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Met à jour les informations d'un utilisateur.
     *
     * @param id   L'ID de l'utilisateur à mettre à jour.
     * @param updatedUser Les nouvelles informations de l'utilisateur.
     * @return Les informations mises à jour de l'utilisateur.
     * @throws InvalidParameterException Si les informations fournies sont invalides.
     * @throws InvalidEmailException     Si l'email est invalide.
     */
    public ResponseEntity<?> updateInformationUser(Long id, User updatedUser) throws InvalidParameterException, InvalidEmailException {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (!user.getUsername().equals(updatedUser.getUsername())) {
                // Met à jour l'adresse email
                user.setUsername(updatedUser.getUsername());
                user.setVerified(false);

                // Crée un nouveau jeton de vérification pour l'utilisateur
                String token = UUID.randomUUID().toString();
                VerificationToken verificationToken = new VerificationToken(token, user);
                verificationTokenRepository.save(verificationToken);

                // Envoie l'e-mail de confirmation avec le nouveau lien de vérification
                String verificationLink = "http://192.168.56.101:4200/verify-email?token=" + token;
                emailService.sendVerificationEmailChanged(user.getUsername(), verificationLink);
            }

            // Sauvegarde les modifications
            userRepository.save(user);

            // Génère un nouveau JWT avec le nouvel email
            String newJwt = jwtUtils.generateJwtToken(user.getUsername());

            // Renvoie la réponse avec le nouveau JWT
            Map<String, String> response = new HashMap<>();
            response.put("message", "Informations de l'utilisateur mises à jour avec succès.");
            response.put("jwt", newJwt);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> resendVerificationEmail(String email) {
        // Rechercher l'utilisateur par son email
        Optional<User> userOpt = userRepository.findByUsername(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Utilisateur non trouvé."));
        }

        User user = userOpt.get();
        // Vérifier si l'utilisateur est déjà vérifié
        if (user.isVerified()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Cet utilisateur a déjà vérifié son compte."));
        }

        // Supprimer tous les anciens tokens de vérification pour cet utilisateur
        List<VerificationToken> tokens = verificationTokenRepository.findAllByUser(user);
        if (!tokens.isEmpty()) {
            verificationTokenRepository.deleteAll(tokens);
        }

        // Créer un nouveau jeton de vérification
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);

        // Sauvegarder le nouveau token
        verificationTokenRepository.save(verificationToken);

        // Générer un lien de vérification
        String verificationLink = "http://192.168.56.101:4200/verify-email?token=" + token;

        // Envoyer un nouvel email de vérification
        emailService.sendVerificationEmail(user.getUsername(), verificationLink);

        return ResponseEntity.ok(new MessageResponse("Un nouveau mail de vérification a été envoyé."));
    }
}
