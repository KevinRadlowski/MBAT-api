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

    @Value("${mbat.app.jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;
    /**
     * Génère un jeton JWT pour l'utilisateur authentifié.
     *
     * @param authentication L'objet Authentication contenant les informations de l'utilisateur.
     * @return Le jeton JWT généré.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // Générer un refresh token
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur du jeton JWT.
     *
     * @param token Le jeton JWT à partir duquel extraire le nom d'utilisateur.
     * @return Le nom d'utilisateur extrait du jeton.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Valide le jeton JWT fourni.
     *
     * @param authToken Le jeton JWT à valider.
     * @return true si le jeton est valide, false sinon.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }


        return false;
    }
}