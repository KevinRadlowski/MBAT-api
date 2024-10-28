package com.mbat.mbatapi.auth.controller;

import com.mbat.mbatapi.auth.entity.RefreshToken;
import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.auth.payload.request.TwoFactorAuthRequest;
import com.mbat.mbatapi.auth.payload.response.JwtResponse;
import com.mbat.mbatapi.auth.payload.response.MessageResponse;
import com.mbat.mbatapi.auth.repository.UserRepository;
import com.mbat.mbatapi.auth.security.jwt.JwtUtils;
import com.mbat.mbatapi.auth.service.RefreshTokenService;
import com.mbat.mbatapi.auth.service.SmsService;
import com.mbat.mbatapi.auth.service.TwoFactorAuthService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/twofactor")
@Tag(name = "Authentification à double facteur", description = "API pour les opérations liées à l'authentification à double facteur.")
public class TwoFactorAuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SmsService smsService;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtUtils jwtUtils;


    /**
     * Active le 2FA pour l'utilisateur via App Authenticator.
     *
     * @param request La requête contenant le nom d'utilisateur pour lequel activer le 2FA via App Authenticator et le code à vérifier.
     * @return Une réponse avec le token JWT et les informations de l'utilisateur en cas de succès, ou une erreur sinon.
     * @throws QrGenerationException En cas d'erreur lors de la validation du QR code pour activer le 2FA via App Authenticator.
     */
    @Operation(summary = "Active le 2FA via App Authenticator", description = "Valide la configuration 2FA via le code de l'app Authenticator.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR Code généré avec succès."),
            @ApiResponse(responseCode = "400", description = "Utilisateur non trouvé ou code invalide.")
    })
    @PostMapping("/enable-2fa/app")
    public ResponseEntity<?> enable2FAApp(@RequestBody TwoFactorAuthRequest request) throws QrGenerationException {
        String username = request.getUsername();
        String code = request.getCode();

        Map<String, Object> update = new HashMap<>(Map.of());
        update.put("twoFactorMethod", "app");
        update.put("isTwoFactorEnabled", true);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Le QR Code est généré à l'appel précédent dans la méthode 'generateQrCode()' et est enregistré pour l'utilisateur.
            boolean isQrCodeVerified = twoFactorAuthService.verifyCode(user.getFirstTwoFactorSecret(), code);

            if (isQrCodeVerified) {

                twoFactorAuthService.updateTwoFactorSettings(user, update);

                String jwt = jwtUtils.generateJwtToken(username);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                List<String> roles = user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList());

                return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), user.getId(), user.getUsername(), roles));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Le code n'est pas valide."));
            }
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    /**
     * Active le 2FA par SMS pour un utilisateur.
     *
     * @param request      La requête contenant le nom d'utilisateur et le code de vérification 2FA.
     * @param phoneNumber  Le numéro de téléphone de l'utilisateur.
     * @return Un message indiquant si le 2FA par SMS a été activé avec succès ou une erreur.
     */
    @Operation(summary = "Active le 2FA par SMS", description = "Envoie un code de vérification par SMS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code de vérification envoyé avec succès."),
            @ApiResponse(responseCode = "400", description = "Utilisateur non trouvé ou code invalide.")
    })
    @PostMapping("/enable-2fa/sms")
    public ResponseEntity<?> enable2FASms(@RequestBody TwoFactorAuthRequest request, @RequestParam String phoneNumber) {
        String username = request.getUsername();
        String code = request.getCode();

        Map<String, Object> update = new HashMap<>(Map.of());
        update.put("twoFactorMethod", "email");
        update.put("isTwoFactorEnabled", true);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Le QR Code est généré à l'appel précédent dans la méthode 'generateQrCode()' et est enregistré pour l'utilisateur.
            boolean isQrCodeVerified = code.equals(user.getFirstTwoFactorSecret());

            if (isQrCodeVerified) {

                twoFactorAuthService.updateTwoFactorSettings(user, update);

                String jwt = jwtUtils.generateJwtToken(username);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                List<String> roles = user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList());

                return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), user.getId(), user.getUsername(), roles));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Le code n'est pas valide."));
            }

        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    /**
     * Active le 2FA par email pour un utilisateur.
     *
     * @param request La requête contenant le nom d'utilisateur et le code de vérification 2FA.
     * @return Un message indiquant si le 2FA par email a été activé avec succès ou une erreur.
     */
    @Operation(summary = "Active le 2FA par Email", description = "Envoie un code de vérification par email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code de vérification envoyé avec succès."),
            @ApiResponse(responseCode = "400", description = "Utilisateur non trouvé ou code invalide.")
    })
    @PostMapping("/enable-2fa/email")
    public ResponseEntity<?> enable2FAEmail(@RequestBody TwoFactorAuthRequest request) {
        String username = request.getUsername();
        String code = request.getCode();

        Map<String, Object> update = new HashMap<>(Map.of());
        update.put("twoFactorMethod", "email");
        update.put("isTwoFactorEnabled", true);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Le QR Code est généré à l'appel précédent dans la méthode 'generateQrCode()' et est enregistré pour l'utilisateur.
            boolean isQrCodeVerified = code.equals(user.getFirstTwoFactorSecret());

            if (isQrCodeVerified) {

                twoFactorAuthService.updateTwoFactorSettings(user, update);

                String jwt = jwtUtils.generateJwtToken(username);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                List<String> roles = user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList());

                return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), user.getId(), user.getUsername(), roles));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Le code n'est pas valide."));
            }

        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    /**
     * Vérifie le code 2FA fourni par l'utilisateur pour l'authentification.
     *
     * @param request La requête contenant le nom d'utilisateur et le code de vérification.
     * @return Une réponse avec un token JWT et les informations de l'utilisateur en cas de succès, ou une erreur sinon.
     */
    @Operation(summary = "Vérifie le code 2FA", description = "Vérifie le code fourni par l'utilisateur pour l'authentification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code vérifié avec succès."),
            @ApiResponse(responseCode = "400", description = "Code invalide ou utilisateur non trouvé.")
    })
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody TwoFactorAuthRequest request) {
        String username = request.getUsername();
        String code = request.getCode();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean isVerified = false;
            if (user.getFirstTwoFactorMethod().equals("app")) {
                isVerified = twoFactorAuthService.verifyCode(user.getFirstTwoFactorSecret(), code);
            } else {
                isVerified = code.equals(user.getFirstTwoFactorSecret());
            }
            if (isVerified) {
                String jwt = jwtUtils.generateJwtToken(username);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                List<String> roles = user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList());

                return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), user.getId(), user.getUsername(), roles));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Le code n'est pas valide."));
            }
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    /**
     * Génère un code de vérification 2FA et l'envoie par email.
     *
     * @param requestBody La requête contenant le nom d'utilisateur.
     * @return Un message de succès indiquant que le code de vérification a été envoyé, ou une erreur.
     */
    @Operation(summary = "Génère un code de vérification 2FA par email", description = "Envoie un code de vérification par email pour l'authentification à deux facteurs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code de vérification envoyé avec succès par email."),
            @ApiResponse(responseCode = "400", description = "Utilisateur non trouvé.")
    })
    @PostMapping("/generate-email-code")
    public ResponseEntity<?> generateEmailCode(@RequestBody Map<String, String> requestBody) throws QrGenerationException {
        String username = requestBody.get("username");
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            String verificationCode = twoFactorAuthService.generateVerificationCode();

            // Envoi du code par email
            twoFactorAuthService.sendEmailVerificationCode(username, verificationCode);
            user.setFirstTwoFactorSecret(verificationCode);
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Code de vérification envoyé par email."));
        } else {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé.");
        }
    }

    /**
     * Génère un QR code pour configurer le 2FA avec Google Authenticator.
     *
     * @param requestBody La requête contenant le nom d'utilisateur.
     * @return Une réponse contenant l'URL du QR code ou un message d'erreur.
     */
    @Operation(summary = "Génère un QR Code pour Google Authenticator", description = "Génère un QR Code permettant de configurer Google Authenticator.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR Code généré avec succès."),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la génération du QR Code.")
    })
    @PostMapping("/generate-qr")
    public ResponseEntity<?> generateQrCode(@RequestBody Map<String, String> requestBody) throws QrGenerationException {
        String username = requestBody.get("username");
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                String secret = twoFactorAuthService.generateSecret();
                String qrCodeUrl = twoFactorAuthService.getAppAuthenticatorQRCode(secret, user.getUsername());

                user.setFirstTwoFactorSecret(secret);
                userRepository.save(user);

                Map<String, String> response = new HashMap<>();
                response.put("qrCodeUrl", qrCodeUrl); // Retourne l'URL du QR code

                return ResponseEntity.ok(response);
            } catch (QrGenerationException e) {
                return ResponseEntity.status(500).body(new MessageResponse("Erreur lors de la génération du QR code."));
            }

        } else {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé.");
        }
    }

    /**
     * Met à jour les paramètres de 2FA pour un utilisateur.
     *
     * @param id      L'ID de l'utilisateur.
     * @param updates Les paramètres à mettre à jour.
     * @return Un message indiquant si la mise à jour a été effectuée avec succès ou une erreur.
     */
    @Operation(summary = "Met à jour les paramètres de 2FA", description = "Met à jour les paramètres de 2FA pour un utilisateur spécifique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mise à jour réussie."),
            @ApiResponse(responseCode = "400", description = "Utilisateur non trouvé.")
    })
    @PutMapping("/update-twofactor/{id}")
    public ResponseEntity<?> updateTwoFactorSettings(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstTwoFactorMethod((String) updates.get("twoFactorMethod"));
            user.setTwoFactorEnabled((Boolean) updates.get("isTwoFactorEnabled"));
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Mise à jour réussie."));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    /**
     * Désactive le 2FA pour un utilisateur.
     *
     * @param id L'ID de l'utilisateur.
     * @return Un message indiquant si le 2FA a été désactivé avec succès ou une erreur.
     */
    @Operation(summary = "Désactive le 2FA", description = "Désactive l'authentification à deux facteurs pour un utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification à deux facteurs désactivée avec succès."),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé.")
    })
    @PutMapping("/disable-twofactor/{id}")
    public ResponseEntity<?> disableTwoFactor(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTwoFactorEnabled(false);
            user.setFirstTwoFactorMethod(null);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Authentification à deux facteurs désactivée avec succès."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Utilisateur non trouvé"));
        }
    }

    /**
     * Active une seconde méthode de 2FA pour un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @param method   La méthode de 2FA à activer en seconde option (App ou Email).
     * @return Un message indiquant si la seconde méthode de 2FA a été activée avec succès ou une erreur.
     * @throws QrGenerationException En cas d'erreur lors de la génération du QR code pour App Authenticator.
     */
    @Operation(summary = "Active une seconde méthode de 2FA", description = "Active une méthode de 2FA alternative pour un utilisateur (App ou Email).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seconde méthode de 2FA activée avec succès."),
            @ApiResponse(responseCode = "400", description = "Méthode déjà configurée ou utilisateur non trouvé.")
    })
    @PostMapping("/enable-second-2fa")
    public ResponseEntity<?> enableSecond2FA(@RequestParam String username, @RequestParam String method) throws QrGenerationException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.isTwoFactorEnabled()) {
                if (method.equals("app") && !"app".equals(user.getFirstTwoFactorMethod())) {
                    String secret = twoFactorAuthService.generateSecret();
                    String qrCodeUrl = twoFactorAuthService.getAppAuthenticatorQRCode(secret, user.getUsername());

                    user.setSecondTwoFactorMethod("app");
                    user.setSecondTwoFactorSecret(secret);
                    userRepository.save(user);

                    return ResponseEntity.ok(new MessageResponse("QR Code pour la seconde méthode : " + qrCodeUrl));
                } else if (method.equals("email") && !"email".equals(user.getFirstTwoFactorMethod())) {
                    String verificationCode = twoFactorAuthService.generateVerificationCode();
                    twoFactorAuthService.sendEmailVerificationCode(user.getUsername(), verificationCode);

                    user.setSecondTwoFactorSecret(verificationCode);
                    user.setSecondTwoFactorMethod("email");
                    userRepository.save(user);

                    return ResponseEntity.ok(new MessageResponse("Code de vérification envoyé par email pour la seconde méthode."));
                } else {
                    return ResponseEntity.badRequest().body(new MessageResponse("La méthode est déjà configurée ou non valide."));
                }
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Aucune méthode de 2FA n'est activée, configurez-en une d'abord."));
            }
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

}
