package com.mbat.mbatapi.auth.repository;
import com.mbat.mbatapi.auth.entity.RefreshToken;
import com.mbat.mbatapi.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    List<RefreshToken> findByUserUsername(String username);
}