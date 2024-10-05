package com.mbat.mbatapi.auth.exception;

public class InvalidEmailException extends Exception {

    public InvalidEmailException(String message) {
        super(message);
    }

    public InvalidEmailException() {
        super("L'adresse email n'est pas au bon format");
    }
}
