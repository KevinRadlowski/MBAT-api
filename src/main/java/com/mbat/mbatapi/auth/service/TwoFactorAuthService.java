package com.mbat.mbatapi.auth.service;

import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.auth.payload.response.MessageResponse;
import com.mbat.mbatapi.auth.repository.UserRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Optional;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * Service pour gérer l'authentification à deux facteurs (2FA) avec Google Authenticator (TOTP).
 */
@Service
public class TwoFactorAuthService {

    @Autowired
    EmailService emailService;

    @Autowired
    private UserRepository userRepository;


    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);

    public void updateTwoFactorSettings(User user, Map<String, Object> updates) {
        user.setTwoFactorMethod((String) updates.get("twoFactorMethod"));
        user.setTwoFactorEnabled((Boolean) updates.get("isTwoFactorEnabled"));
        userRepository.save(user);
    }


    /**
     * Génère un secret pour l'utilisateur.
     *
     * @return Le secret TOTP.
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }


    public String getGoogleAuthenticatorQRCode(String secret, String accountName) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(accountName)
                .secret(secret)
                .issuer("MBAT")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);
        return getDataUriForImage(imageData, generator.getImageMimeType());
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            throw new IllegalArgumentException("Le secret ou le code est null.");
        }
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        return verifier.isValidCode(secret, code);
    }
    public String generateVerificationCode() {
        // Génération d'un code à 6 chiffres
        return String.format("%06d", (int) (Math.random() * 1000000));
    }


    @Async
    public void sendEmailVerificationCode(String email, String code) {
        // Appel du service d'email pour envoyer le code
        emailService.sendVerificationCode(email, code);
    }

}
