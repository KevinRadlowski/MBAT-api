package com.mbat.mbatapi.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Méthode pour envoyer un email en HTML pour la réinitialisation de mot de passe.
     */
    @Async
    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        String subject = "Réinitialisation de mot de passe";
        String htmlContent = generateEmailTemplate(
                "Réinitialisation de mot de passe",
                "Vous avez demandé à réinitialiser votre mot de passe.",
                "Cliquez sur le bouton ci-dessous pour réinitialiser votre mot de passe :",
                resetLink,
                "Réinitialiser mon mot de passe"
        );

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Méthode pour envoyer un email en HTML pour la vérification du compte.
     */
    @Async
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        String subject = "Vérification de votre compte";
        String htmlContent = generateEmailValidationTemplate(
                "Vérification de votre compte",
                "Merci de vous être inscrit !",
                "Veuillez cliquer sur le bouton ci-dessous pour vérifier votre adresse e-mail :",
                verificationLink,
                "Vérifier mon compte"
        );

        sendHtmlEmail(toEmail, subject, htmlContent);

    }

    /**
     * Méthode pour envoyer un email en HTML pour la vérification du compte, en cas de changement d'email de l'utilisateur.
     */
    @Async
    public void sendVerificationEmailChanged(String toEmail, String verificationLink) {
        String subject = "Vérification de votre compte";
        String htmlContent = generateEmailTemplate(
                "Vérification de votre compte",
                "Vous avez effectué un changement d'adresse email.",
                "Veuillez cliquer sur le bouton ci-dessous pour vérifier votre nouvelle adresse e-mail :",
                verificationLink,
                "Vérifier mon compte"
        );

        sendHtmlEmail(toEmail, subject, htmlContent);

    }

    /**
     * Méthode pour envoyer un email en HTML pour déverrouiller un compte.
     */
    @Async
    public void sendUnlockEmail(String to, String unlockUrl) {
        String subject = "Déverrouillage de votre compte";
        String htmlContent = generateEmailTemplate(
                "Déverrouillage de votre compte",
                "Votre compte a été verrouillé après plusieurs tentatives échouées.",
                "Veuillez cliquer sur le bouton ci-dessous pour déverrouiller votre compte :",
                unlockUrl,
                "Déverrouiller mon compte"
        );

        sendHtmlEmail(to, subject, htmlContent);

    }

    /**
     * Méthode pour envoyer un email en HTML après la suppression de compte.
     */
    @Async
    public void sendAccountDeletionEmail(String to) {
        String subject = "Suppression de compte";
        String htmlContent = generateEmailTemplate(
                "Suppression de compte",
                "Bonjour,",
                "Nous vous confirmons que votre compte ainsi que toutes les données associées ont été supprimés.",
                null,
                null
        );

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Méthode générique pour envoyer un email au format HTML.
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);  // Le second paramètre "true" indique que c'est du HTML
            // Ajout de l'image en pièce jointe avec le cid 'logo'
            ClassPathResource logoImage = new ClassPathResource("static/images/logo-mbat-transparent-redimension.png");
            helper.addInline("logo", logoImage);

            mailSender.send(message);
            logger.info("Email envoyé à " + to);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Génère le template HTML pour les emails.
     */
    private String generateEmailTemplate(String title, String greeting, String message, String link, String buttonText) {
        String button = (link != null && buttonText != null)
                ? "<a href='" + link + "' class='btn' style='color:#fff;text-decoration:none;padding:12px 24px;'>" + buttonText + "</a>"
                : "";

        // URL de l'image hébergée publiquement ou via un CID si c'est une image en pièce jointe
        String logoUrl = "http://ton-domaine.com/images/logo-mbat-transparent-redimension.png";  // Remplace cette URL avec celle de ton image

        return "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; }" +
                ".email-container { padding: 30px; margin: 30px auto; border-radius: 10px; background-color: #fff; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); max-width: 600px; text-align: left; }" +
                "h1 { color: #007461; text-align: center; font-size: 20px; margin-bottom: 20px; }" +
                "p { color: #000809; font-size: 14px; margin-bottom: 20px; }" +
                ".btn { display: inline-block; background-color: #004652; color: #fff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-size: 14px; transition: background-color 0.3s ease; margin: 20px 0; }" +
                ".btn:hover { background-color: #007461; }" +
                ".logo { display: block; margin: 0 auto 20px; text-align: center; }" +
                ".logo img { max-height: 60px; width: auto; }" +  // Style pour limiter la taille de l'image
                "ul { list-style-type: none; padding: 0; font-size: 14px; margin-bottom: 20px; }" +
                "ul li { margin-bottom: 10px; }" +
                "</style></head><body>" +
                "<div class='email-container'>" +
                "<div class='logo'><img src='cid:logo' alt='Logo'></div>" +  // Utilise 'cid' pour les images en pièce jointe
//                "<div class='logo'><img src='" + logoUrl + "' alt='Logo'></div>" +  // Utilise une URL ou un CID pour l'image
                "<h1>" + title + "</h1>" +
                "<p>Bonjour,</p>" +
                "<p>" + greeting + "</p>" +
                "<p>" + message + "</p>" +
                button +
                "<p>Cordialement,<br>L'équipe MBAT.</p>" +
                "</div></body></html>";
    }


    private String generateEmailValidationTemplate(String title, String greeting, String message, String link, String buttonText) {
        String button = (link != null && buttonText != null)
                ? "<a href='" + link + "' class='btn' style='color:#fff;text-decoration:none;padding:12px 24px;'>" + buttonText + "</a>"
                : "";

        return "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; }" +
                ".email-container { padding: 30px; margin: 30px auto; border-radius: 10px; background-color: #fff; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); max-width: 600px; text-align: left; }" +
                "h1 { color: #007461; text-align: center; font-size: 20px; margin-bottom: 20px; }" +
                "p { color: #000809; font-size: 14px; margin-bottom: 20px; }" +
                ".btn { display: inline-block; background-color: #004652; color: #fff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-size: 14px; transition: background-color 0.3s ease; margin: 20px 0; }" +
                ".btn:hover { background-color: #007461; }" +
                ".logo { display: block; margin: 0 auto 20px; text-align: center; }" +
                ".logo img { max-height: 60px; width: auto; }" +  // Style pour limiter la taille de l'image
                "ul { list-style-type: none; padding: 0; font-size: 14px; margin-bottom: 20px; }" +
                "ul li { margin-bottom: 10px; }" +
                "</style></head><body>" +
                "<div class='email-container'>" +
                "<div class='logo'><img src='cid:logo' alt='Logo'></div>" +  // Utilise 'cid' pour les images en pièce jointe
                "<h1>" + title + "</h1>" +
                "<p>Bonjour,</p>" +
                "<p>" + greeting + "</p>" +
                "<p>" + message + "</p>" +
                button +
                "<p>Veuillez noter que ce lien à usage unique n'est valable que 24 heures.</p>" +
                "<p>Une fois l'activation de votre compte terminée, vous pouvez consulter et modifier toutes les informations nécessaires depuis votre espace personnel :</p>" +
                "<ul>" +
                "<li>• Vos identifiants et mot de passe</li>" +
                "<li>• Votre abonnement</li>" +
                "<li>• Vos accès et confidentialité </li>" +
                "<li>• Vos comptes</li>" +
                "</ul>" +
                "<p>MBAT prend votre sécurité très au sérieux et veille à ce que vos données personnelles soient traitées de manière confidentielle.</p>" +
                "<p>À très vite sur MBAT !</p>" +
                "<p>Cordialement,<br>L'équipe MBAT.</p>" +
                "</div></body></html>";
    }

}
