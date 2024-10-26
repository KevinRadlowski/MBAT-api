package com.mbat.mbatapi.auth.exception;

/**
 * Exception lancée lorsque le refresh token est invalide ou expiré.
 */
public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String message) {
        super(message);
    }

    public TokenRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
