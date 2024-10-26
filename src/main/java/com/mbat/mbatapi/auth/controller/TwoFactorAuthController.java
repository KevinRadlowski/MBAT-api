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
     * Active le 2FA pour l'utilisateur via Google Authenticator.
     *
     * @param username Le nom d'utilisateur pour lequel activer le 2FA.
     * @return Le lien du QR code pour configurer Google Authenticator.
     */
    @PostMapping("/enable-2fa/google")
    public ResponseEntity<?> enable2FAGoogle(@RequestParam String username) throws QrGenerationException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String secret = twoFactorAuthService.generateSecret();
            String qrCodeUrl = twoFactorAuthService.getGoogleAuthenticatorQRCode(secret, user.getUsername());

            user.setTwoFactorMethod("google_authenticator");
            user.setTwoFactorSecret(secret);
            user.setTwoFactorEnabled(true);
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("QR Code : " + qrCodeUrl));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    /**
     * Endpoint pour activer le 2FA par SMS.
     *
     * @param username Le nom d'utilisateur pour lequel activer le 2FA.
     * @param phoneNumber Le numéro de téléphone de l'utilisateur.
     * @return Un message de succès.
     */
    @Operation(summary = "Active le 2FA par SMS.")
    @PostMapping("/enable-2fa/sms")
    public ResponseEntity<?> enable2FASms(@RequestParam String username, @RequestParam String phoneNumber) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String verificationCode = twoFactorAuthService.generateVerificationCode();

            smsService.sendVerificationCode(phoneNumber, verificationCode);

            user.setTwoFactorMethod("sms");
            user.setTwoFactorEnabled(true);
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Code de vérification envoyé par SMS."));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    /**
     * Vérifie le code TOTP fourni par l'utilisateur.
     *
     * @param request Le contenu de la requête contenant le username et le code d'authentification 2FA.
     * @return Un message indiquant si la vérification a réussi ou échoué.
     */
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody TwoFactorAuthRequest request) {
        String username = request.getUsername();
        String code = request.getCode();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean isVerified = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);
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
     * Generates a QR code for Google Authenticator.
     *
     * @return A response containing the QR code URL or an error message.
     */
    @PostMapping("/generate-qr")
    public ResponseEntity<?> generateQrCode(@RequestBody Map<String, String> requestBody) throws QrGenerationException {
        String username = requestBody.get("username");
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                String secret = twoFactorAuthService.generateSecret();
                String qrCodeUrl = twoFactorAuthService.getGoogleAuthenticatorQRCode(secret, user.getUsername());

                user.setTwoFactorSecret(secret);
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


    @PutMapping("/update-twofactor/{id}")
    public ResponseEntity<?> updateTwoFactorSettings(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTwoFactorMethod((String) updates.get("twoFactorMethod"));
            user.setTwoFactorEnabled((Boolean) updates.get("isTwoFactorEnabled"));
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Mise à jour réussie."));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Utilisateur non trouvé."));
        }
    }

    @PutMapping("/disable-twofactor/{id}")
    public ResponseEntity<?> disableTwoFactor(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTwoFactorEnabled(false);
            user.setTwoFactorMethod(null);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Authentification à deux facteurs désactivée avec succès."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Utilisateur non trouvé"));
        }
    }

}
