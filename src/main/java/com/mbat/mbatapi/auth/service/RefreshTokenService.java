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

@Service
public class RefreshTokenService {

    @Value("${mbat.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;


    public void revokeAllTokens(String username) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserUsername(username); // Assuming there's a relation between User and RefreshToken
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Aucun token trouvé pour cet utilisateur.");
        }
        for (RefreshToken token : tokens) {
            token.setExpired(true); // On marque tous les tokens comme expirés
            token.setRevoked(true);  // Ajoute cette propriété si nécessaire pour revocation explicite
            refreshTokenRepository.save(token);
        }
    }


    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Date.from(Instant.now().plusMillis(refreshTokenDurationMs)));
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public void verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Le token de rafraîchissement a expiré.");
        }
    }
}