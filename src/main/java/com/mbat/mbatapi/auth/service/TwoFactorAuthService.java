package com.mbat.mbatapi.auth.service;

import com.mbat.mbatapi.auth.entity.User;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * Service pour gérer l'authentification à deux facteurs (2FA) avec l'app Authenticator (TOTP), Email ou SMS.
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

    /**
     * Met à jour les paramètres de 2FA pour un utilisateur donné.
     *
     * @param user    L'utilisateur pour lequel les paramètres doivent être mis à jour.
     * @param updates Les paramètres à mettre à jour, incluant la méthode de 2FA et l'état d'activation.
     */
    public void updateTwoFactorSettings(User user, Map<String, Object> updates) {
        user.setFirstTwoFactorMethod((String) updates.get("twoFactorMethod"));
        user.setTwoFactorEnabled((Boolean) updates.get("isTwoFactorEnabled"));
        userRepository.save(user);
    }

    /**
     * Génère un secret pour l'utilisateur. Ce secret est utilisé pour le TOTP avec l'App Authenticator.
     *
     * @return Le secret TOTP généré.
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * Génère un QR Code permettant à l'utilisateur de configurer l'App Authenticator avec le secret TOTP.
     *
     * @param secret      Le secret TOTP de l'utilisateur.
     * @param accountName Le nom de compte associé (généralement le nom d'utilisateur).
     * @return L'URI du QR Code généré en base64.
     * @throws QrGenerationException En cas d'erreur lors de la génération du QR Code.
     */
    public String getAppAuthenticatorQRCode(String secret, String accountName) throws QrGenerationException, IOException {
        QrData data = new QrData.Builder()
                .label(accountName)
                .secret(secret)
                .issuer("MBAT")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] qrImageData = generator.generate(data);

        // Charger le QR code en tant qu'image BufferedImage
        BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrImageData));

        // Charger le logo
        BufferedImage logo = ImageIO.read(new File("src/main/resources/static/images/logo-mbat.png"));

        // Superposer le logo sur le QR code
        int logoWidth = qrImage.getWidth() / 5;
        int logoHeight = qrImage.getHeight() / 5;
        int logoX = (qrImage.getWidth() - logoWidth) / 2;
        int logoY = (qrImage.getHeight() - logoHeight) / 2;

        Graphics2D g = qrImage.createGraphics();
        g.drawImage(logo, logoX, logoY, logoWidth, logoHeight, null);
        g.dispose();

        // Convertir l'image finale en base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        byte[] finalImageData = baos.toByteArray();

        return getDataUriForImage(finalImageData, "image/png");
    }

    /**
     * Vérifie le code TOTP fourni par l'utilisateur.
     *
     * @param secret Le secret TOTP de l'utilisateur.
     * @param code   Le code TOTP à vérifier.
     * @return True si le code est valide, sinon False.
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            throw new IllegalArgumentException("Le secret ou le code est null.");
        }
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        return verifier.isValidCode(secret, code);
    }

    /**
     * Génère un code de vérification aléatoire de 6 chiffres pour l'authentification par email.
     *
     * @return Un code de vérification de 6 chiffres sous forme de chaîne de caractères.
     */
    public String generateVerificationCode() {
        // Génération d'un code à 6 chiffres
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * Envoie un code de vérification par email de manière asynchrone.
     *
     * @param email L'adresse email de l'utilisateur.
     * @param code  Le code de vérification à envoyer.
     */
    @Async
    public void sendEmailVerificationCode(String email, String code) {
        // Appel du service d'email pour envoyer le code
        emailService.sendVerificationCode(email, code);
    }

}
