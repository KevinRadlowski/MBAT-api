package com.mbat.mbatapi.auth.security.jwt;

import com.mbat.mbatapi.auth.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Classe utilitaire pour la gestion des opérations JWT, telles que la génération, la validation et l'extraction d'informations du token.
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${mbat.app.jwtSecret}")
    private String jwtSecret;

    @Value("${mbat.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Génère un jeton JWT pour un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @return Le token JWT.
     */
    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur du token JWT.
     *
     * @param token Le token JWT.
     * @return Le nom d'utilisateur extrait du token.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Valide un jeton JWT.
     *
     * @param authToken Le token JWT.
     * @return true si le token est valide, false sinon.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }
}