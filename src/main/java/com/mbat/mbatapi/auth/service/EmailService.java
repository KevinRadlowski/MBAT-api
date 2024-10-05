package com.mbat.mbatapi.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de mot de passe");
        message.setText("Cliquez sur le lien suivant pour réinitialiser votre mot de passe : " + resetLink);
        mailSender.send(message);
    }

    // Méthode pour envoyer l'email de vérification du compte
    @Async
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Vérification de votre compte");
        message.setText("Bienvenue !\n\nMerci de vous être inscrit. Veuillez vérifier votre adresse e-mail en cliquant sur le lien suivant : \n\n" + verificationLink + "\n\nCe lien expirera dans 24 heures.");
        mailSender.send(message);
    }

    @Async
    public void sendUnlockEmail(String to, String unlockUrl) {
        logger.info("Envoi d'email de déverrouillage à " + to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Déverrouillage de votre compte");
        message.setText("Votre compte a été verrouillé après plusieurs tentatives échouées. " +
                "Veuillez cliquer sur le lien suivant pour déverrouiller votre compte : " + unlockUrl);

        mailSender.send(message);
    }
}