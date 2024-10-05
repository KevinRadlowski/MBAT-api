package com.mbat.mbatapi.auth.exception;

public class InvalidPasswordException extends Exception {
    
    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException() {
        super("Le mot de passe n'est pas au bon format");
    }
}

