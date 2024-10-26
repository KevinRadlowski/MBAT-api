package com.mbat.mbatapi.auth.controller;

import com.mbat.mbatapi.auth.entity.RefreshToken;
import com.mbat.mbatapi.auth.exception.InvalidEmailException;
import com.mbat.mbatapi.auth.exception.UserExistException;
import com.mbat.mbatapi.auth.payload.request.LoginRequest;
import com.mbat.mbatapi.auth.payload.response.JwtResponse;
import com.mbat.mbatapi.auth.payload.response.MessageResponse;
import com.mbat.mbatapi.auth.security.jwt.JwtUtils;
import com.mbat.mbatapi.auth.security.services.UserDetailsImpl;
import com.mbat.mbatapi.auth.service.AuthService;
import com.mbat.mbatapi.auth.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtUtils jwtUtils;

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
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws InvalidEmailException {
        return authService.authenticateUser(loginRequest);
    }

    /**
     * Rafraîchit le token JWT à l'aide du refresh token.
     *
     * @param requestBody Contient le refresh token.
     * @return Un nouveau token JWT si le refresh token est valide.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletResponse response, @RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findByToken(refreshToken);

        if (refreshTokenOptional.isPresent()) {
            RefreshToken token = refreshTokenOptional.get();
            refreshTokenService.verifyExpiration(token);  // Vérifie si le refresh token a expiré

            // Générer un nouveau token d'accès en utilisant le nom d'utilisateur
            String newAccessToken = jwtUtils.generateJwtToken(token.getUser().getUsername());
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(token.getUser());

            // Conversion des rôles de Set<Role> à List<String>
            List<String> roles = token.getUser().getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList());

            // Ajouter le nouveau refresh token dans un cookie sécurisé
            Cookie cookie = new Cookie("refreshToken", newRefreshToken.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);  // S'assurer que le cookie est sécurisé
            cookie.setPath("/"); // Chemin d'application
            response.addCookie(cookie);

            return ResponseEntity.ok(new JwtResponse(newAccessToken, newRefreshToken.getToken(), token.getUser().getId(), token.getUser().getUsername(), roles));
//            return ResponseEntity.ok(new JwtResponse(newAccessToken, token.getUser().getId(), token.getUser().getUsername(), roles, token.getToken()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalide ou expiré.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletResponse response) throws InvalidEmailException {

        if (userDetails == null) {
            System.out.println("UserDetails is null, user not authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Utilisateur non authentifié."));
        }
        System.out.println("User authenticated: " + userDetails.getUsername());
        try {
            refreshTokenService.deleteByUser(userDetails.getUser());

            // Supprimer le cookie de refresh token
            Cookie cookie = new Cookie("refreshToken", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);  // Assurez-vous que c'est correct pour votre environnement
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            return ResponseEntity.ok(new MessageResponse("Déconnexion réussie."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Erreur lors de la déconnexion : " + e.getMessage()));
        }
//
//        refreshTokenService.deleteByUser(userDetails.getUser());
//        return ResponseEntity.ok(new MessageResponse("Déconnexion réussie."));
    }

    @PostMapping("/logout-all-devices")
    public ResponseEntity<?> logoutFromAllDevices(@AuthenticationPrincipal UserDetailsImpl userDetails) throws InvalidEmailException {
        // Révoquer tous les tokens liés à cet utilisateur
        refreshTokenService.deleteByUser(userDetails.getUser());
        return ResponseEntity.ok("Déconnexion de tous les appareils réussie.");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAllDevices(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            refreshTokenService.revokeAllTokens(userDetails.getUsername());  // Révoquer tous les tokens de l'utilisateur
            return ResponseEntity.ok(new MessageResponse("Déconnexion réussie sur tous les appareils."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur lors de la déconnexion de tous les appareils : " + e.getMessage()));
        }
    }
}