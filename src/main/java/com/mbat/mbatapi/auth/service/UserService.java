package com.mbat.mbatapi.auth.service;

import com.mbat.mbatapi.auth.ChangePasswordDto;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

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

    /**
     * Change le mot de passe de l'utilisateur.
     *
     * @param passwordDto Les informations de changement de mot de passe.
     * @param user        L'utilisateur pour lequel changer le mot de passe.
     * @throws InvalidPasswordException Si l'ancien mot de passe est incorrect.
     */
    public void changePassword(ChangePasswordDto passwordDto, User user) throws InvalidPasswordException {
        if (!encoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        user.setPassword(encoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Authentifie un utilisateur avec ses informations de connexion.
     *
     * @param loginRequest Les informations de connexion de l'utilisateur.
     * @return Une réponse avec le jeton JWT et les informations de l'utilisateur.
     */
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Identifiant ou mot de passe incorrect."));
        }

        User user = userOpt.get();
        // Assure-toi que tu récupères bien le thème de l'utilisateur
        String theme = user.getTheme();
        System.out.println("Thème de l'utilisateur récupéré : " + theme);  // Debug

        // Si le compte n'est pas vérifié, renvoyer un message d'erreur
        if (!user.isVerified()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("compte non validé", user.getUsername()));
        }

        if (user.isAccountLocked()) {
            if (isLockTimeExpired(user)) {
                unlockAccount(user);
            } else {
                long lockTimeRemainingInSeconds = (user.getLockTime().getTime() + LOCK_TIME_DURATION - System.currentTimeMillis()) / 1000;

                long minutes = lockTimeRemainingInSeconds / 60;
                long seconds = lockTimeRemainingInSeconds % 60;

                String responseMessage = String.format(
                        "⚠️ Votre compte est actuellement verrouillé pour des raisons de sécurité. Il sera déverrouillé dans %d minutes et %d secondes.<br>" +
                                "<br>🔑 Si vous ne voulez pas attendre, cliquez sur le bouton ci-dessous pour demander un nouveau mail de déverrouillage.<br>",
                        minutes, seconds
                );

                // Utiliser le constructeur avec l'email
                MessageResponse messageResponse = new MessageResponse(responseMessage, user.getUsername());
                System.out.println(messageResponse.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageResponse);
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication.getName());

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Générer et ajouter un refresh token à la réponse
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser());

            resetFailedAttempts(user);
            return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getUsername(), roles, theme));

        } catch (BadCredentialsException e) {
            increaseFailedAttempts(user);
            int attemptsRemaining = MAX_FAILED_ATTEMPTS - user.getFailedAttempts();
            if (attemptsRemaining > 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Identifiant ou mot de passe incorrect. Tentatives restantes : " + attemptsRemaining));
            } else {
                String resendUnlockLink = "http://localhost:4200/unlock-account?token=" + user.getUnlockToken();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Votre compte est bloqué pour " + (LOCK_TIME_DURATION / 1000 / 60) + " minutes. Un lien de déblocage a été envoyé sur votre adresse email."));
            }
        } catch (InvalidEmailException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Incrémente les tentatives échouées et verrouille le compte si le maximum est atteint.
     */
    @Async
    private void increaseFailedAttempts(User user) {
        int newFailedAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newFailedAttempts);

        if (newFailedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(user);
        } else {
            try {
                Thread.sleep(2000); // Ajoute un délai de 2 secondes entre chaque tentative
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        userRepository.save(user);
    }

    /**
     * Réinitialise les tentatives échouées après une connexion réussie.
     */
    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        userRepository.save(user);
    }

    /**
     * Verrouille le compte de l'utilisateur.
     */
    private void lockAccount(User user) {
        user.setAccountLocked(true);
        user.setLockTime(new Date());
        String unlockToken = UUID.randomUUID().toString();
        user.setUnlockToken(unlockToken);
        userRepository.save(user);

        // Envoi d'email pour déverrouillage
        String unlockUrl = "http://192.168.56.101:4200/unlock-account?token=" + unlockToken;
        emailService.sendUnlockEmail(user.getUsername(), unlockUrl);
    }

    /**
     * Vérifie si le temps de verrouillage est expiré.
     */
    private boolean isLockTimeExpired(User user) {
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();
        return currentTimeInMillis - lockTimeInMillis > LOCK_TIME_DURATION;
    }

    /**
     * Déverrouille le compte et réinitialise les tentatives échouées.
     */
    private void unlockAccount(User user) {
        user.setAccountLocked(false);
        user.setFailedAttempts(0);
        user.setLockTime(null);
        user.setUnlockToken(null);
        userRepository.save(user);
    }

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
    public ResponseEntity<HttpStatus> deleteUser(Integer id) {
        try {

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
    public ResponseEntity<User> updateInformationUser(Integer id, User updatedUser) throws InvalidParameterException, InvalidEmailException {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (!user.getUsername().equals(updatedUser.getUsername())) {

                // Met à jour l'adresse email
                user.setUsername(updatedUser.getUsername());

                // Réinitialise le statut de vérification
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



            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gère le processus de réinitialisation du mot de passe.
     *
     * @param email L'email de l'utilisateur pour lequel réinitialiser le mot de passe.
     */
    public void processForgotPassword(String email) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://192.168.56.101:4200/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(user.getUsername(), resetLink);
    }

    /**
     * Met à jour le mot de passe d'un utilisateur.
     *
     * @param token       Le jeton de réinitialisation de mot de passe.
     * @param newPassword Le nouveau mot de passe.
     * @throws InvalidPasswordException Si le mot de passe est invalide.
     */
    public void updatePassword(String token, String newPassword) throws InvalidPasswordException {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            throw new IllegalArgumentException("Jeton invalide ou expiré");
        }

        User user = resetToken.getUser();

        // Validez le nouveau mot de passe s'il y a des règles spécifiques (par exemple, longueur, caractères spéciaux, etc.)
        if (newPassword == null || newPassword.isEmpty()) {
            throw new InvalidPasswordException("Le mot de passe ne peut pas être vide");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken); // Supprimez le jeton après utilisation
    }

    public void updateUserPassword(Integer id, String newPassword) throws InvalidPasswordException {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Validez le nouveau mot de passe ici (par exemple, vérifiez sa complexité)
            if (newPassword == null || newPassword.isEmpty()) {
                throw new InvalidPasswordException("Le mot de passe ne peut pas être vide.");
            }

            // Encodage et mise à jour du mot de passe
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new InvalidPasswordException("Utilisateur non trouvé.");
        }
    }

    public boolean checkOldPassword(User user, String oldPassword) {
        // Utilise le PasswordEncoder pour comparer le mot de passe fourni et celui dans la base de données
        return encoder.matches(oldPassword, user.getPassword());
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
