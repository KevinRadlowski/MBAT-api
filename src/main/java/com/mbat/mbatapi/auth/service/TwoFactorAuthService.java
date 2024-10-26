package com.mbat.mbatapi.auth.service;

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
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * Service pour gérer l'authentification à deux facteurs (2FA) avec Google Authenticator (TOTP).
 */
@Service
public class TwoFactorAuthService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);

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

//    public boolean verifyCode(String secret, String code) {
//        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
//        return verifier.isValidCode(secret, code);
//    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            throw new IllegalArgumentException("Le secret ou le code est null.");
        }
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        return verifier.isValidCode(secret, code);
    }
    public String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }

//    /**
//     * Génère un lien pour le QR code de Google Authenticator.
//     *
//     * @param secret Le secret TOTP.
//     * @param accountName Le nom de l'utilisateur.
//     * @return Le lien pour générer le QR code.
//     */
//    public String getGoogleAuthenticatorQRCode(String secret, String accountName) throws QrGenerationException {
//        QrData data = new QrData.Builder()
//                .label(accountName)
//                .secret(secret)
//                .issuer("MBAT")
//                .algorithm(HashingAlgorithm.SHA1)
//                .digits(6)
//                .period(30)
//                .build();
//
//        QrGenerator generator = new ZxingPngQrGenerator();
//        byte[] imageData = generator.generate(data);
//        return getDataUriForImage(imageData, generator.getImageMimeType());
//    }
//
//    /**
//     * Vérifie le code TOTP fourni par l'utilisateur.
//     *
//     * @param secret Le secret TOTP.
//     * @param code   Le code TOTP fourni par l'utilisateur.
//     * @return true si le code est valide, false sinon.
//     */
//
//    public boolean verifyCode(String secret, String code) {
//        DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());
//        return verifier.isValidCode(secret, code);
//    }
}
