package com.mbat.mbatapi.auth.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

/**
 * Service pour envoyer des SMS contenant un code de vérification 2FA via Twilio.
 */
@Service
public class SmsService {
    private static final String ACCOUNT_SID = "your_twilio_account_sid"; // À remplacer par ton SID Twilio
    private static final String AUTH_TOKEN = "your_twilio_auth_token"; // À remplacer par ton token Twilio

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Envoie un code de vérification par SMS à un numéro de téléphone spécifique.
     *
     * @param phoneNumber Le numéro de téléphone du destinataire.
     * @param code        Le code de vérification à envoyer.
     */
    public void sendVerificationCode(String phoneNumber, String code) {
        Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber("your_twilio_phone_number"), // Remplace par ton numéro Twilio
                "Votre code de vérification est : " + code
        ).create();
    }
}
