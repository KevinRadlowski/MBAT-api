package com.mbat.mbatapi.auth.service;

import com.mbat.mbatapi.auth.entity.RefreshToken;
import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.auth.repository.RefreshTokenRepository;
import com.mbat.mbatapi.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les opérations sur les Refresh Tokens.
 */
@Service
public class RefreshTokenService {

    @Value("${mbat.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    /**
     * Trouve un refresh token par sa valeur.
     *
     * @param token Le refresh token.
     * @return Un Optional contenant le RefreshToken s'il est trouvé.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Crée un refresh token pour un utilisateur donné.
     *
     * @param user L'utilisateur pour lequel le refresh token doit être créé.
     * @return Le refresh token créé.
     */
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Date.from(Instant.now().plusMillis(refreshTokenDurationMs)));
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    /**
     * Supprime les refresh tokens d'un utilisateur.
     *
     * @param user L'utilisateur dont les tokens doivent être supprimés.
     */
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Vérifie si le refresh token est expiré.
     *
     * @param token Le refresh token à vérifier.
     * @return Le refresh token s'il n'est pas expiré.
     * @throws RuntimeException si le token a expiré.
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().before(Date.from(Instant.now()))) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Le refresh token a expiré.");
        }
        return token;
    }

    public void revokeAllTokens(String username) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserUsername(username);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("No tokens found for this user.");
        }
        for (RefreshToken token : tokens) {
            token.setExpired(true);
            token.setRevoked(true);  // Tu pourrais ajouter un champ `revoked` pour une gestion explicite de la révocation
            refreshTokenRepository.save(token);
        }
    }
}
