package com.mbat.mbatapi.security.jwt;

import java.io.IOException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mbat.mbatapi.security.services.UserDetailsServiceImpl;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filtre pour la gestion de l'authentification JWT. Ce filtre est déclenché à chaque requête et vérifie
 * la validité du jeton JWT. Si le jeton est valide, il configure l'authentification de l'utilisateur dans
 * le contexte de sécurité.
 */
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    /**
     * Méthode principale du filtre qui intercepte chaque requête HTTP.
     *
     * @param request  La requête HTTP entrante.
     * @param response La réponse HTTP sortante.
     * @param filterChain La chaîne de filtres à exécuter.
     * @throws ServletException En cas d'erreur de traitement de la requête.
     * @throws IOException En cas d'erreur d'entrée/sortie.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Récupère et valide le jeton JWT de la requête
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Extrait le nom d'utilisateur du jeton
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // Charge les détails de l'utilisateur à partir du nom d'utilisateur
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Crée un objet d'authentification
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                // Ajoute les détails de la requête à l'objet d'authentification
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Configure l'authentification dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Impossible de configurer l'authentification de l'utilisateur : {}", e.getMessage());
        }

        // Passe au filtre suivant dans la chaîne de filtres
        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le jeton JWT de la requête HTTP.
     *
     * @param request La requête HTTP.
     * @return Le jeton JWT extrait ou null si le jeton est absent ou mal formé.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}