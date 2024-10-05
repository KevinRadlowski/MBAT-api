package com.mbat.mbatapi.auth.payload.response;

public class MessageResponse {
    private String message;
    private String email;  // Ajouter un champ pour l'email

    public MessageResponse(String message) {
        this.message = message;
    }

    public MessageResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}