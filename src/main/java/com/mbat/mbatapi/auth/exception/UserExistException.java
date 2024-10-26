package com.mbat.mbatapi.auth.exception;

public class UserExistException extends Exception {

    public UserExistException(String message) {
        super(message);
    }
    public UserExistException() {
        super("L'utilisateur existe déjà");
    }

}

