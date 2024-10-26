package com.mbat.mbatapi.auth.service;

import com.mbat.mbatapi.auth.entity.RefreshToken;
import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.auth.exception.InvalidEmailException;
import com.mbat.mbatapi.auth.payload.request.LoginRequest;
import com.mbat.mbatapi.auth.payload.response.JwtResponse;
import com.mbat.mbatapi.auth.payload.response.MessageResponse;
import com.mbat.mbatapi.auth.repository.UserRepository;
import com.mbat.mbatapi.auth.security.jwt.JwtUtils;
import com.mbat.mbatapi.auth.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private static final int MAX_FAILED_ATTEMPTS = 5; // Verrouille le compte après 5 tentatives
    public static final long LOCK_TIME_DURATION = 15 * 60 * 1000; // Durée du verrouillage (15 minutes)

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private EmailService emailService;

    /**
     * Authentifie un utilisateur avec ses informations de connexion.
     *
     * @param loginRequest Les informations de connexion de l'utilisateur.
     * @return Une réponse avec le jeton JWT et les informations de l'utilisateur.
     */
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) throws InvalidEmailException {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Identifiant ou mot de passe incorrect."));
        }

        User user = userOpt.get();
        String theme = user.getTheme();

        // Si le compte n'est pas vérifié, renvoyer un message d'erreur
        if (!user.isVerified()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Compte non validé", user.getUsername()));
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

            // Vérifier si le 2FA est activé après la validation du mot de passe
            if (user.isTwoFactorEnabled()) {
                return ResponseEntity.ok(new MessageResponse("2FA requis", true));
            }

            // Génère le token JWT après authentification réussie et validation du 2FA

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
        }}

    public ResponseEntity<?> refreshToken(HttpServletResponse response, String refreshToken) {
        Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findByToken(refreshToken);
        if (refreshTokenOptional.isPresent()) {
            RefreshToken token = refreshTokenOptional.get();
            refreshTokenService.verifyExpiration(token);
            String newAccessToken = jwtUtils.generateJwtToken(token.getUser().getUsername());
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(token.getUser());

            Cookie cookie = new Cookie("refreshToken", newRefreshToken.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            response.addCookie(cookie);

            return ResponseEntity.ok(new JwtResponse(newAccessToken, newRefreshToken.getToken(),
                    token.getUser().getId(), token.getUser().getUsername(),
                    token.getUser().getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList())));
        } else {
            return ResponseEntity.status(401).body("Refresh token invalide ou expiré.");
        }
    }

    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Déconnexion réussie."));
    }

    public ResponseEntity<?> logoutAllDevices() {
        // Logique pour déconnecter l'utilisateur de tous ses appareils actuels
        return ResponseEntity.ok(new MessageResponse("Déconnexion de tous les appareils réussie."));
    }

    public ResponseEntity<?> logoutAllDevicesGlobally() {
        // Logique pour révoquer tous les tokens à l'échelle mondiale
        return ResponseEntity.ok(new MessageResponse("Déconnexion globale réussie."));
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
}
