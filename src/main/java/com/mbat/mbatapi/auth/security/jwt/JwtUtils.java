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

    @Value("${bezkoder.app.jwtSecret}")
    private String jwtSecret;

    @Value("${bezkoder.app.jwtExpirationMs}")
    private int jwtExpirationMs;

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
            Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Signature JWT invalide : {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Jeton JWT mal formé : {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Le jeton JWT a expiré : {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Le jeton JWT n'est pas supporté : {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("La chaîne de revendications JWT est vide : {}", e.getMessage());
        }

        return false;
    }
}