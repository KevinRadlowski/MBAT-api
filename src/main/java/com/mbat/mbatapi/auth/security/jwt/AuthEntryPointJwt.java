package com.mbat.mbatapi.auth.security.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Point d'entrée d'authentification pour les requêtes non autorisées.
 * Ce composant est déclenché chaque fois qu'une demande non authentifiée est faite à une ressource nécessitant une authentification.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    /**
     * Méthode appelée lorsqu'un utilisateur non authentifié tente d'accéder à une ressource protégée.
     *
     * @param request       La requête HTTP.
     * @param response      La réponse HTTP.
     * @param authException L'exception d'authentification déclenchée.
     * @throws IOException      En cas d'erreur d'entrée/sortie.
     * @throws ServletException En cas d'erreur de traitement de la requête.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {
        // Journalise l'erreur d'authentification
        logger.error("Erreur d'authentification : {}", authException.getMessage());
        logger.error("Chemin de la requête : {}", request.getRequestURI());

        // Définir le statut de la réponse et le type de contenu
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Construire le corps de la réponse JSON
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        responseBody.put("error", "Non autorisé");
        responseBody.put("message", authException.getMessage());
        responseBody.put("path", request.getServletPath());

        // Convertir la carte en JSON et l'écrire dans la réponse
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), responseBody);
    }

}