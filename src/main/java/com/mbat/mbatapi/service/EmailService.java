package com.mbat.mbatapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de mot de passe");
        message.setText("Cliquez sur le lien suivant pour réinitialiser votre mot de passe : " + resetLink);
        mailSender.send(message);
    }

    // Méthode pour envoyer l'email de vérification du compte
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Vérification de votre compte");
        message.setText("Bienvenue !\n\nMerci de vous être inscrit. Veuillez vérifier votre adresse e-mail en cliquant sur le lien suivant : \n\n" + verificationLink + "\n\nCe lien expirera dans 24 heures.");
        mailSender.send(message);
    }
}