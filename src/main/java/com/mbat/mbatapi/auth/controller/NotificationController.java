package com.mbat.mbatapi.auth.controller;

import com.mbat.mbatapi.auth.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    /**
     * Envoie une notification par email en fonction de l'événement indiqué par la méthode.
     *
     * @param email  L'adresse email de l'utilisateur.
     * @param method L'événement déclenchant la notification (par exemple : "2fa-app-enabled", "2fa-email-enabled", "2fa-email-desactivated", "email-modified", "password-modified").
     * @return Une réponse HTTP avec le statut de l'envoi.
     */
    @PostMapping("/send-email-{method}")
    public ResponseEntity<String> sendNotificationEmail(
            @RequestBody Map<String, String> requestBody, // Map pour recevoir le JSON { "email": "email@example.com" }
            @PathVariable("method") String method) {

        String email = requestBody.get("email");

        // Vérification si l'email est présent et non vide
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email non fourni");
        }

        // Votre logique pour envoyer les notifications
        switch (method) {
            case "2fa-app-enabled":
                emailService.sendTwoFactorEnabledEmail(email, "app");
                break;
            case "2fa-email-enabled":
                emailService.sendTwoFactorEnabledEmail(email, "email");
                break;
            case "2fa-email-desactivated":
                emailService.sendTwoFactorDisabledEmail(email);
                break;
            case "email-modified":
                emailService.sendEmailModificationNotification(email);
                break;
            case "password-modified":
                emailService.sendPasswordModificationNotification(email);
                break;
            default:
                return ResponseEntity.badRequest().body("Méthode de notification non reconnue.");
        }

        return ResponseEntity.ok("Email de notification envoyé pour l'événement : " + method);
    }
}
