package com.mbat.mbatapi.auth.payload.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MessageResponse {
    private String message;
    private String email;  // Ajouter un champ pour l'email
    private boolean requires2FA; // Ajouter un champ pour indiquer si le 2FA est requis
    private String twoFactorMethod;

    public MessageResponse(String message) {
        this.message = message;
    }

    public MessageResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }

    public MessageResponse(String message, boolean requires2FA) {
        this.message = message;
        this.requires2FA = requires2FA;
    }

    public MessageResponse(String message, boolean requires2FA, String twoFactorMethod) {
        this.message = message;
        this.requires2FA = requires2FA;
        this.twoFactorMethod = twoFactorMethod;
    }

}